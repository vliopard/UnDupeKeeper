import os
import json
import shutil
import argparse
import constants

from tqdm import tqdm

from methods import timed
from methods import get_hash

from openpyxl import Workbook
from methods import comparison_test

from methods import create_random_binary_file

import logging
show = logging.getLogger(constants.DEBUG_DIR)

TEST_HASH = ''


def count_files(target_directory):
    total_files = 0
    file_counter = constants.COUNTER_FILE
    current_directory = constants.config.get('PATHS', 'LOAD_PATH')

    if os.path.isfile(file_counter):
        with open(file_counter, constants.READ, encoding=constants.UTF8) as file_count:
            count_data = json.load(file_count)
            if current_directory == count_data['current_dir']:
                total_files = count_data['file_count']
                return total_files

    for root, dirs, files in tqdm(os.walk(target_directory), desc="SCANNING"):
        total_files += len(files)

    with open(file_counter, constants.WRITE, encoding=constants.UTF8) as file_count:
        count_data = {'current_dir': current_directory,
                      'file_count': total_files}
        json.dump(count_data, file_count)

    constants.config.set('PATHS', 'LOAD_PATH', current_directory)
    with open(constants.SETTINGS_FILE, constants.WRITE, encoding=constants.UTF8) as configfile:
        constants.config.write(configfile)

    return total_files


@timed
def hash_directory_files(current_directory):
    print(f"SCANNING FILES: [{current_directory}]")

    hard_disk_drive_hash_list = {}

    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"

    total_files = count_files(current_directory)

    with tqdm(total=total_files, bar_format=status_bar_format) as tqdm_progress_bar:
        database_file_count = 0
        hash_count = 0
        for root, dirs, files in os.walk(current_directory):
            for file in files:
                database_file_count += 1
                tqdm_progress_bar.update(1)
                file_name = os.path.join(root, file)
                file_name = os.path.normpath(file_name)
                file_hash = get_hash(file_name, TEST_HASH)
                if file_hash in hard_disk_drive_hash_list:
                    hard_disk_drive_hash_list[file_hash].append(file_name)
                else:
                    hash_count += 1
                    hard_disk_drive_hash_list[file_hash] = [file_name]
    return hard_disk_drive_hash_list, hash_count, database_file_count


@timed
def save_database(hard_disk_drive_hash_list, hash_count, database_file_count):
    print(f'SAVING DATABASE...')
    json_file = constants.STORAGE_FILE

    file_count = 0
    for hash_item in hard_disk_drive_hash_list:
        hash_total = 0
        for _ in hard_disk_drive_hash_list[hash_item]:
            hash_total += 1
            file_count += 1

    database_hash_count = len(hard_disk_drive_hash_list)

    count_difference = 0
    if database_hash_count != hash_count:
        count_difference += 1
        print(f'HASH [{database_hash_count:,}]')
    print(f'HASH [{hash_count:,}]')

    if database_file_count != file_count:
        count_difference += 1
        print(f'FILE [{database_file_count:,}]')
    print(f'FILE [{file_count:,}]')

    if count_difference > 0:
        print(f'DIFF [{(database_file_count - database_hash_count):,}]')
    print(f'DIFF [{(file_count - hash_count):,}]')

    print('SAVING...')
    with open(f'{TEST_HASH}_{json_file}', constants.WRITE, encoding=constants.UTF8) as save_file:
        json.dump(hard_disk_drive_hash_list, save_file)
    print('DONE.')


def create_random_binary_files():
    for file in constants.test_files:
        file_size = file.replace('.bin', '').replace(f'{constants.test_directory}/file', '')
        create_random_binary_file(file, int(file_size) * constants.test_magnitude)

    if not os.path.exists(constants.test_directory):
        print(f'Creating [{constants.test_directory}]...')
        os.mkdir(constants.test_directory)

    for file_name in constants.test_files:
        position = int(os.stat(file_name).st_size / 6)
        data = bytes(56)

        new_name = file_name.replace(constants.DOT, '_dupe.')
        shutil.copyfile(file_name, new_name)

        new_name = file_name.replace(constants.DOT, '_start.')
        shutil.copyfile(file_name, new_name)
        with open(new_name, constants.READ_WRITE_BINARY) as file_writer:
            file_writer.seek(position)
            file_writer.write(data)

        new_name = file_name.replace(constants.DOT, '_middle.')
        shutil.copyfile(file_name, new_name)
        with open(new_name, constants.READ_WRITE_BINARY) as file_writer:
            file_writer.seek(position * 2)
            file_writer.write(data)

        new_name = file_name.replace(constants.DOT, '_end.')
        shutil.copyfile(file_name, new_name)
        with open(new_name, constants.READ_WRITE_BINARY) as file_writer:
            file_writer.seek(position * 5)
            file_writer.write(data)


def comparison_hash():
    print('Started...')
    trials = [constants.BUFFER,
              constants.COMPARE,
              constants.ZIP_LONGEST,
              constants.NATIVE,
              constants.EXECUTABLE,
              constants.HASH_MD5,
              constants.HASH_MD5_FAST,
              constants.HASH_MD5_CHUNK,
              constants.HASH_SHA,
              constants.HASH_SHA256,
              constants.HASH_SHA512]

    excel_workbook = Workbook()
    excel_worksheet = excel_workbook.active
    excel_worksheet.title = 'File Comparison'

    print('Workbook...')
    excel_worksheet.append(tuple(['TIME', 'CATEGORY', 'SIZE', 'RESULT', 'METHOD']))

    for file_path in constants.test_files:
        file = os.path.join(os.getcwd(), file_path.replace('/', '\\'))

        print(f'Working on [{file}]...')
        comparison_test(trials, excel_worksheet, file, file.replace('.', '_dupe.'), f'EQUAL:{os.stat(file).st_size:0,.0f}')
        comparison_test(trials, excel_worksheet, file, file.replace('.', '_start.'), f'NOT EQUAL/SAME SIZE/START:{os.stat(file).st_size:0,.0f}')
        comparison_test(trials, excel_worksheet, file, file.replace('.', '_middle.'), f'NOT EQUAL/SAME SIZE/MIDDLE:{os.stat(file).st_size:0,.0f}')
        comparison_test(trials, excel_worksheet, file, file.replace('.', '_end.'), f'NOT EQUAL/SAME SIZE/END : {os.stat(file).st_size:0,.0f}')

    # test(f1, f2, 'NOT EQUAL, SIZE DIFFERENT')

    excel_workbook.save(constants.COMPARISON_REPORT)


def main(directory):
    global TEST_HASH
    test = [constants.BUFFER,
            constants.COMPARE,
            constants.ZIP_LONGEST,
            constants.NATIVE,
            constants.EXECUTABLE,
            constants.HASH_MD5,
            constants.HASH_MD5_FAST,
            constants.HASH_MD5_CHUNK,
            constants.HASH_SHA,
            constants.HASH_SHA256,
            constants.HASH_SHA512]

    for item in test:
        TEST_HASH = item
        print(f'TESTING [{TEST_HASH}]')
        database, hashes, files = hash_directory_files(directory)
        save_database(database, hashes, files)


if __name__ == '__main__':
    argument_parser = argparse.ArgumentParser(description='Hash files in a directory and identify duplicates.')
    argument_parser.add_argument('-d', '--directory', type=str, default=None, help='Directory to scan for files')
    arguments = argument_parser.parse_args()

    main(arguments.directory)
