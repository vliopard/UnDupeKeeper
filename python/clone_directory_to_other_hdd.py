import os
import stat
import shutil
import argparse
import constants
import database

from tqdm import tqdm
from methods import section_line

import logging
show = logging.getLogger(constants.DEBUG_COPY)


def str2bool(v):
    if isinstance(v, bool):
        return v
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')


def get_size(data_list):
    print('Getting total size...')
    total_size = 0
    total_files = 0
    with tqdm(total=len(data_list), bar_format=constants.STATUS_BAR_FORMAT) as tqdm_progress_bar:
        for file_list in data_list:
            source_file = sorted(file_list[constants.FILE_LIST])[0]
            try:
                if not os.path.islink(source_file):
                    total_size += os.path.getsize(source_file)
                total_files += 1
                tqdm_progress_bar.update(1)
            except Exception as e:
                print('_'*100)
                print(f"Error counting: [{source_file}]\n[{e}]")
    return total_size, total_files


def copy_files(args):
    print('Getting parameters...')
    check_size = args.calculate_size
    target_location = args.target_location if args.target_location.endswith(os.sep) else f'{args.target_location}{os.sep}'
    source_location = args.source_location
    source_location_query = r"{}".format(args.source_location.replace("\\", "\\\\"))

    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    print(f'COPY FROM SOURCE [{source_location}]')
    print(f'       TO TARGET [{target_location}]')
    print(f'      WITH QUERY [{source_location_query}]')
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')

    print('Checking length...')
    data_length = database.count_files(source_location_query)

    print('Getting files...')
    data = database.get_item_by_file(source_location_query)
    data_list = list(data)
    if check_size:
        need_space, total_files = get_size(data_list)
        free_space = shutil.disk_usage(target_location).free
        print(f'FREE: [{free_space:,}]')
        print(f'NEED: [{need_space:,}]')
        if need_space >= free_space:
            print('Free Space Unavailable')
            return

    print('Copying files...')
    with tqdm(total=data_length, bar_format=constants.STATUS_BAR_FORMAT) as tqdm_progress_bar:
        for hash_file in data_list:
            file_list = sorted(hash_file[constants.FILE_LIST], key=lambda x: x.replace(constants.DOS_SLASH, constants.UNIX_SLASH))[0]
            tqdm_progress_bar.update(1)
            if file_list.startswith(source_location):
                _, drive_tail = os.path.splitdrive(file_list)
                drive_tail = drive_tail.lstrip(os.path.sep)
                target_file = os.path.join(target_location, drive_tail)
                os.makedirs(os.path.dirname(target_file), exist_ok=True)
                try:
                    shutil.copy2(file_list, target_file)
                except PermissionError:
                    os.chmod(target_file, stat.S_IWRITE)
                    shutil.copy2(file_list, target_file)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error copying:\n   [{file_list}]\nto [{target_file}]\n[{exception}]")


if __name__ == constants.MAIN:
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('-s', '--source_location', type=str, help='The source location to copy files from')
    parser.add_argument('-t', '--target_location', type=str, help='The target location to copy files to')
    parser.add_argument('-c', '--calculate_size', type=str2bool, nargs='?', const=True, default=True, help='Calculate the size needed to copy files to')
    copy_files(parser.parse_args())
