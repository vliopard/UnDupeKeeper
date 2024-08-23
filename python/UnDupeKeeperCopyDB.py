import os
import stat
import shutil
import argparse
import constants
import UnDupeKeeperDatabase
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


def get_size(data):
    print('Getting total size...')
    total_size = 0
    total_files = 0

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
    print('Getting parameters...')
    check_size = args.calculate_size
    target_location = args.target_location if args.target_location.endswith(os.sep) else f'{args.target_location}{os.sep}'
    source_location = args.source_location
    source_location_query = r"{}".format(args.source_location.replace("\\", "\\\\"))

    print('Checking length...')
    data_length = UnDupeKeeperDatabase.count_files(source_location_query)

    print('Getting files...')
    data = UnDupeKeeperDatabase.get_item_by_file(source_location_query)
    if check_size:
        need_space, total_files = get_size(data)
        free_space = shutil.disk_usage(target_location).free
        print(f'FREE: [{free_space:,}]')
        print(f'NEED: [{need_space:,}]')
        if need_space >= free_space:
            print('Free Space Unavailable')
            return

    print('Copying files...')
    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
    with tqdm(total=data_length, bar_format=status_bar_format) as tqdm_progress_bar:
        for hash_file in data:
            source_file = sorted(hash_file[constants.FILE_LIST], key=lambda x: x.replace(constants.DOS_SLASH, constants.UNIX_SLASH))[0]
            tqdm_progress_bar.update(1)
            if source_file.startswith(source_location):
                _, drive_tail = os.path.splitdrive(source_file)
                drive_tail = drive_tail.lstrip(os.path.sep)
                target_file = os.path.join(target_location, drive_tail)
                os.makedirs(os.path.dirname(target_file), exist_ok=True)
                try:
                    shutil.copy2(source_file, target_file)
                except PermissionError:
                    os.chmod(target_file, stat.S_IWRITE)
                    shutil.copy2(source_file, target_file)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error copying:\n   [{source_file}]\nto [{target_file}]\n[{exception}]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('-s', '--source_location', type=str, help='The source location to copy files from')
    parser.add_argument('-t', '--target_location', type=str, help='The target location to copy files to')
    parser.add_argument('-c', '--calculate_size', type=str2bool, nargs='?', const=True, default=True, help='Calculate the size needed to copy files to')
    copy_files(parser.parse_args())
