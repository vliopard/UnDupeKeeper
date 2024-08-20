import os
import stat
import methods
import argparse
import constants
from tqdm import tqdm

import logging
show = logging.getLogger(constants.DEBUG_COPY)


def count_files(target_directory):
    total_files = 0
    for root, dirs, files in tqdm(os.walk(target_directory), desc="SCANNING"):
        total_files += len(dirs)
    return total_files


def change_files(args):
    print('Changing files...')

    target_location = args.target_location

    data = count_files(target_location)
    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
    with tqdm(total=data, bar_format=status_bar_format) as tqdm_progress_bar:
        for root, dirs, files in os.walk(target_location):
            for target_dir in dirs:
                file_name = os.path.join(root, target_dir)
                file_name = os.path.normpath(file_name)
                try:
                    methods.change_dir_time(file_name)
                except PermissionError:
                    os.chmod(file_name, stat.S_IWRITE)
                    methods.change_dir_time(file_name)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error changing:\n   [{target_dir}]\n[{exception}]")
                finally:
                    tqdm_progress_bar.update(1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Change files on a target location.')
    parser.add_argument('-t', '--target_location', type=str, help='The target location to change files')
    change_files(parser.parse_args())
