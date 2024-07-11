import os
import json
import shutil
import argparse
import constants
from tqdm import tqdm

import logging
show = logging.getLogger(constants.DEBUG_CP)


def get_size():
    total_size = 0
    total_files = 0
    json_file = constants.STORAGE_FILE
    with open(json_file, constants.READ, encoding=constants.UTF8) as hdd_hl:
        data = json.load(hdd_hl)
        status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
        with tqdm(total=len(data), bar_format=status_bar_format) as tqdm_progress_bar:
            for hash_file in data:
                source_file = sorted(data[hash_file])[0]
                try:
                    if not os.path.islink(source_file):
                        total_size += os.path.getsize(source_file)
                    total_files += 1
                    tqdm_progress_bar.update(1)
                except Exception as e:
                    print('_'*100)
                    print(f"Error counting: [{source_file}]\n[{e}]")
    return total_size, total_files


def copy_files(target_location):
    json_file = constants.STORAGE_FILE

    need_space, total_files = get_size()
    free_space = shutil.disk_usage(target_location).free
    print(f'FREE: [{free_space:,}]')
    print(f'NEED: [{need_space:,}]')
    if need_space >= free_space:
        print('Free Space Unavailable')
        return

    with open(json_file, constants.READ, encoding=constants.UTF8) as hdd_hl:
        data = json.load(hdd_hl)

        status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"

        with tqdm(total=len(data), bar_format=status_bar_format) as tqdm_progress_bar:
            for hash_file in data:
                source_file = sorted(data[hash_file], key=lambda x: x.replace('\\', '/'))[0]
                try:
                    _, drive_tail = os.path.splitdrive(os.path.relpath(source_file, '/'))
                    target_file = os.path.join(target_location, drive_tail)
                    os.makedirs(os.path.dirname(target_file), exist_ok=True)
                    shutil.copyfile(source_file, target_file)
                    tqdm_progress_bar.update(1)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error copying:\n   [{source_file}]\nto [{target_file}]\n[{exception}]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('target_location', type=str, help='The target location to copy files to')
    args = parser.parse_args()
    copy_files(args.target_location)
