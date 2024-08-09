import os
import json
import stat
import shutil
import argparse
import constants
from tqdm import tqdm

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


def get_size():
    print('Getting total size...')
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


def copy_files(args):
    print('Copying files...')

    check_size = args.calculate_size
    target_location = args.target_location
    json_file = constants.STORAGE_FILE

    if check_size:
        need_space, total_files = get_size()
        free_space = shutil.disk_usage(target_location).free
        print(f'FREE: [{free_space:,}]')
        print(f'NEED: [{need_space:,}]')
        if need_space >= free_space:
            print('Free Space Unavailable')
            return

    print('Opening JSON Database...')
    with open(json_file, constants.READ, encoding=constants.UTF8) as hdd_hl:
        data = json.load(hdd_hl)

        status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
        with tqdm(total=len(data), bar_format=status_bar_format) as tqdm_progress_bar:
            for hash_file in data:
                source_file = sorted(data[hash_file], key=lambda x: x.replace('\\', '/'))[0]
                try:
                    _, drive_tail = os.path.splitdrive(source_file)
                    drive_tail = drive_tail.lstrip(os.path.sep)
                    target_file = os.path.join(target_location, drive_tail)
                    os.makedirs(os.path.dirname(target_file), exist_ok=True)
                    shutil.copy2(source_file, target_file)
                except PermissionError:
                    os.chmod(target_file, stat.S_IWRITE)
                    shutil.copy2(source_file, target_file)
                    tqdm_progress_bar.update(1)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error copying:\n   [{source_file}]\nto [{target_file}]\n[{exception}]")
                finally:
                    tqdm_progress_bar.update(1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('-t', '--target_location', type=str, help='The target location to copy files to')
    parser.add_argument('-c', '--calculate_size', type=str2bool, nargs='?', const=True, default=True, help='Calculate the size needed to copy files to')
    copy_files(parser.parse_args())
