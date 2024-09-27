import os
import stat
import methods
import argparse
import constants
from tqdm import tqdm
from methods import section_line

import logging
show = logging.getLogger(constants.DEBUG_COPY)


def change_files(args):
    target_location = args.target_location
    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    print(f'Changing files on [{target_location}]')
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')

    data = methods.count_directories(target_location)

    with tqdm(total=data, bar_format=constants.STATUS_BAR_FORMAT) as tqdm_progress_bar:
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


if __name__ == constants.MAIN:
    parser = argparse.ArgumentParser(description='Change files on a target location.')
    parser.add_argument('-t', '--target_location', type=str, help='The target location to change files')
    change_files(parser.parse_args())
