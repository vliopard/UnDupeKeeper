import os
import stat
import methods
import argparse
import constants
from tqdm import tqdm

import logging
show = logging.getLogger(constants.DEBUG_COPY)


def copy_files(args):
    source_location = args.from_source
    print(f'Copying files from [{source_location}]...')
    target_location = args.to_target
    print(f'to [{target_location}]...')

    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"

    print('COUNTING FILES...')
    data = methods.count_files(source_location)
    print('MODIFYING FILES...')
    with tqdm(total=data, bar_format=status_bar_format) as tqdm_progress_bar:
        for root, dirs, files in os.walk(source_location):
            for file in files:
                tqdm_progress_bar.update(1)
                source_file = os.path.join(root, file)
                source_file = os.path.normpath(source_file)
                target_file = target_location[:2] + source_file[2:]

                try:
                    # os.utime(target_file, (time_accessed, time_modified))
                    methods.copy_time(source_file, target_file)
                except PermissionError:
                    os.chmod(target_file, stat.S_IWRITE)
                    # os.utime(target_file, (time_accessed, time_modified))
                    methods.copy_time(source_file, target_file)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error changing:\n   [{target_file}]\n[{exception}]")

    print('COUNTING DIRECTORIES...')
    data = methods.count_directories(source_location)
    print('MODIFYING DIRECTORIES...')
    with tqdm(total=data, bar_format=status_bar_format) as tqdm_progress_bar:
        for root, dirs, files in os.walk(source_location):
            for directory in dirs:
                tqdm_progress_bar.update(1)
                source_file = os.path.join(root, directory)
                source_file = os.path.normpath(source_file)
                target_file = target_location[:2] + source_file[2:]

                try:
                    # os.utime(target_file, (time_accessed, time_modified))
                    methods.copy_time(source_file, target_file)
                except PermissionError:
                    os.chmod(target_file, stat.S_IWRITE)
                    # os.utime(target_file, (time_accessed, time_modified))
                    methods.copy_time(source_file, target_file)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error changing:\n   [{target_file}]\n[{exception}]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('-f', '--from_source', type=str, help='The source location to copy metadata from')
    parser.add_argument('-t', '--to_target', type=str, help='The target location to copy metadata to')
    copy_files(parser.parse_args())
