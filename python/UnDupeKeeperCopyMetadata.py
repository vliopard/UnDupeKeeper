import os
import stat
import ctypes
import methods
import argparse
import constants
from tqdm import tqdm

import logging
show = logging.getLogger(constants.DEBUG_COPY)


def change_time(source_name, target_name):
    iet = 1e7
    iiga = 116444736000000000

    time_accessed = os.path.getatime(source_name)
    time_modified = os.path.getmtime(source_name)
    time_created = os.path.getctime(source_name)

    set_file_time = ctypes.windll.kernel32.SetFileTime
    handle = ctypes.windll.kernel32.CreateFileW(target_name, 256, 0, None, 3, 128, None)

    creation_time = ctypes.c_int64(int(time_created * iet + iiga))
    access_time = ctypes.c_int64(int(time_accessed * iet + iiga))
    modify_time = ctypes.c_int64(int(time_modified * iet + iiga))

    set_file_time(handle, ctypes.byref(creation_time), ctypes.byref(access_time), ctypes.byref(modify_time))
    ctypes.windll.kernel32.CloseHandle(handle)


def copy_files(args):
    source_location = args.from_source
    print(f'Copying files from [{source_location}]...')
    target_location = args.to_target
    print(f'to [{target_location}]...')

    data = methods.count_files(source_location)
    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
    with tqdm(total=data, bar_format=status_bar_format) as tqdm_progress_bar:
        for root, dirs, files in os.walk(source_location):
            for file in files:
                tqdm_progress_bar.update(1)
                source_file = os.path.join(root, file)
                source_file = os.path.normpath(source_file)
                target_file = target_location[:2] + source_file[2:]

                try:
                    # os.utime(target_file, (time_accessed, time_modified))
                    change_time(source_file, target_file)
                except PermissionError:
                    os.chmod(target_file, stat.S_IWRITE)
                    # os.utime(target_file, (time_accessed, time_modified))
                    change_time(source_file, target_file)
                except Exception as exception:
                    print('_'*100)
                    print(f"Error changing:\n   [{target_file}]\n[{exception}]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('-f', '--from_source', type=str, help='The source location to copy metadata from')
    parser.add_argument('-t', '--to_target', type=str, help='The target location to copy metadata to')
    copy_files(parser.parse_args())
