import os
import json
import constants

from tqdm import tqdm
from methods import timed
from methods import get_hash
from methods import get_level

from os.path import exists as uri_exists

hash_count = 0
database_file_count = 0


@timed
def setup():
    if uri_exists(constants.STORAGE_FILE):
        print('Loading data...')
        with open(constants.STORAGE_FILE, constants.READ, encoding=constants.UTF8) as fix:
            hard_disk_drive_hash_list_local = json.load(fix)
    else:
        print('Starting with empty data...')
        hard_disk_drive_hash_list_local = {}

    print('Getting total hashes...')
    tot = len(hard_disk_drive_hash_list_local)

    print('Generating file list...')
    file_list_local = []

    bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
    with tqdm(total=tot, bar_format=bar_format) as tqdm_progress:
        for x in hard_disk_drive_hash_list_local:
            tqdm_progress.update(1)
            file_list_local += hard_disk_drive_hash_list_local[x]

    # with open('filelist.txt', 'w', encoding='UTF8') as fl:
    #     fl.write(str(file_list_local))
    return hard_disk_drive_hash_list_local, file_list_local


@timed
def count_files(target_directory):
    current_directory = constants.config.get('PATHS', 'LOAD_PATH')
    file_counter = constants.COUNTER_FILE
    total_files = 0
    if os.path.isfile(file_counter):
        with open(file_counter, constants.READ, encoding=constants.UTF8) as file_count:
            count_data = json.load(file_count)
            print(f'Checking [{current_directory}]')
            print(f"Checking [{count_data['current_dir']}]")
            if current_directory == count_data['current_dir']:
                total_files = count_data['file_count']
                print('Returning previous results...')
                return total_files

    print('Generating new results...')
    for root, dirs, files in tqdm(os.walk(target_directory), desc="SCANNING"):
        total_files += len(files)

    print('Saving new results...')
    with open(file_counter, constants.WRITE, encoding=constants.UTF8) as file_count:
        count_data = {'current_dir': current_directory, 'file_count': total_files}
        json.dump(count_data, file_count)


@timed
def hash_directory_files(current_directory):
    global file_list
    global hash_count
    global database_file_count
    global hard_disk_drive_hash_list
    print(f"SCANNING FILES: [{current_directory}]")

    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"

    total_files = count_files(current_directory)
    with tqdm(total=total_files, bar_format=status_bar_format) as tqdm_progress_bar:
        database_file_count = 0
        hash_count = 0
        cdir = ''
        for root, dirs, files in os.walk(current_directory):
            for file in files:
                database_file_count += 1
                level = get_level(root, 3)
                if cdir != level:
                    cdir = level
                    tqdm_progress_bar.set_postfix({'DIR': cdir})
                tqdm_progress_bar.update(1)
                file_name = os.path.join(root, file)
                file_name = os.path.normpath(file_name)

                if file_name not in file_list:
                    file_hash = get_hash(file_name, constants.HASH_MD5)
                    if file_hash in hard_disk_drive_hash_list:
                        hard_disk_drive_hash_list[file_hash].append(file_name)
                    else:
                        hash_count += 1
                        hard_disk_drive_hash_list[file_hash] = [file_name]


@timed
def save_database():
    global hash_count
    global database_file_count
    global hard_disk_drive_hash_list

    print(f'SAVING DATABASE...')
    # json_file = constants.STORAGE_FILE
    json_file = 'UnDupeKeeperFixed.json'
    
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
    with open(json_file, constants.WRITE, encoding=constants.UTF8) as save_file:
        json.dump(hard_disk_drive_hash_list, save_file)
    print('DONE.')


hard_disk_drive_hash_list, file_list = setup()
hash_directory_files('e:\\vliopard\\')
save_database()
