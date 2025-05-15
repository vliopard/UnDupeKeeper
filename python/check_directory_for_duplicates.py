import os
import time
import stat
import shutil
import constants
import argparse as arg_parse

from methods import get_hash
from methods import line_number
from methods import file_equals
from methods import section_line

from pymongo import MongoClient

from queue import Queue

from os import path as os_path
from os import walk as os_walk

from os import unlink as delete_link
from os import remove as delete_file

from os.path import islink as is_link
from os.path import isfile as is_file
from os.path import exists as uri_exists

from logging.handlers import RotatingFileHandler

import logging
show = logging.getLogger(constants.DEBUG_CORE)

thread_started = time.time()
system_tray_icon = None


class DataBase:
    def __init__(self):
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} DATABASE: STARTED')

        self.mongo_client = MongoClient(constants.DATABASE_URL)
        self.mongo_database = self.mongo_client[constants.DATABASE_NAME]
        self.mongo_collection = self.mongo_database[constants.DATABASE_COLLECTION]

    def database_get_item(self, sha):
        return self.mongo_collection.find_one({constants.DOC_ID: sha})


class FileList:
    def __init__(self):
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} FILE LIST: HANDLER STARTED')
        self._file_database = DataBase()
        self.thread_start_time = None
        self.running_now = False
        self.running_thread = None
        self.end_task_thread = None
        self.jobs = Queue()
        self.time_is_now_less_started = time.time()

    @staticmethod
    def file_operation(mode, source_file, target_file):
        function_name = 'OPERATION:'
        _, drive_tail = os.path.splitdrive(source_file)
        drive_tail = drive_tail.lstrip(constants.DOS_SLASH)
        drive_tail = drive_tail.lstrip(constants.UNIX_SLASH)
        target_file = os.path.join(target_file, drive_tail)
        os.makedirs(os.path.dirname(target_file), exist_ok=True)
        if mode == constants.COMMAND_COPY:
            show.info(f'{line_number()} {function_name} COPY [{source_file}] TO [{target_file}]')
            shutil.copy2(source_file, target_file)
        elif mode == constants.COMMAND_MOVE:
            show.info(f'{line_number()} {function_name} MOVE [{source_file}] TO [{target_file}]')
            shutil.move(source_file, target_file)

    def add_file(self, add_uri, restrictions):
        function_name = 'FILE:'
        add_uri = add_uri.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        new_file = FileHolder(add_uri)
        file_with_sha = self._file_database.database_get_item(new_file.file_sha)
        check1 = True if file_with_sha else False
        check2 = False
        check3 = False
        old_uri = constants.EMPTY
        old_uri_sha = constants.BLANK * constants.SHA_SIZE
        if check1:
            old_uri = file_with_sha[constants.FILE_LIST][0].replace(constants.DOS_SLASH, constants.UNIX_SLASH)
            check2 = add_uri != old_uri
            if restrictions[constants.NO_COMP]:
                check3 = True
            else:
                try:
                    check3 = file_equals(add_uri, old_uri, constants.COMPARISON_METHOD)
                except PermissionError as permission_error:
                    print(f'ERROR: CANNOT ACCESS FILE [{add_uri}]')
                    print(f'ERROR: CANNOT ACCESS FILE [{old_uri}]')
                    check3 = False
            old_uri_sha = f'{file_with_sha[constants.DOC_ID][0:constants.SHA_SIZE].upper()}'

        show.info(f'{line_number()} {section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
        show.info(f'{line_number()} {function_name} SOURCE [{old_uri_sha}] [{old_uri}]')
        if check1 and check2 and check3:
            try:
                show.info(f'{line_number()} {function_name} DELETE [{new_file.file_sha[0:constants.SHA_SIZE].upper()}] [{check1}][{check2}][{check3}] [{add_uri}]')
                delete_file(add_uri)
            except PermissionError as permission_error:
                os.chmod(add_uri, stat.S_IWRITE)
                show.error(f'{line_number()} {function_name} DELETE [{new_file.file_sha[0:constants.SHA_SIZE].upper()}] [{check1}][{check2}][{check3}] [{add_uri}] [{permission_error}]')
                delete_file(add_uri)
        else:
            if not restrictions[constants.NO_MOVE]:
                if is_link(add_uri):
                    show.info(f'{line_number()} {function_name} MOVLNK [{new_file.file_sha[0:constants.SHA_SIZE].upper()}] [{check1}][{check2}][{check3}] [{add_uri}] [{constants.TARGET_PATH}]')
                    uri_file = os.readlink(add_uri)
                    self.file_operation(constants.COMMAND_COPY, uri_file, constants.TARGET_PATH)
                    delete_link(add_uri)
                elif is_file(add_uri):
                    show.info(f'{line_number()} {function_name} MOVARQ [{new_file.file_sha[0:constants.SHA_SIZE].upper()}] [{check1}][{check2}][{check3}] [{add_uri}] [{constants.TARGET_PATH}]')
                    self.file_operation(constants.COMMAND_MOVE, add_uri, constants.TARGET_PATH)
                else:
                    show.info(f'{line_number()} {function_name} NO VALID ACTION FOR [{new_file.file_sha[0:constants.SHA_SIZE].upper()}] [{add_uri}]')
            else:
                show.info(f'{line_number()} {function_name} NOMOVE [{new_file.file_sha[0:constants.SHA_SIZE].upper()}] [{check1}][{check2}][{check3}] [{add_uri}] [{constants.TARGET_PATH}]')

        show.info(f'{line_number()} {section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')


class FileHolder:
    def __init__(self, file_path):
        self._file_uri = None
        self._file_sha = None
        self.set_sha(file_path)

    @property
    def file_uri(self):
        return self._file_uri

    @file_uri.setter
    def file_uri(self, universal_resource_indicator):
        self.set_sha(universal_resource_indicator)

    @property
    def file_sha(self):
        return self._file_sha

    def set_sha(self, file_path):
        self._file_uri = file_path
        self._file_sha = get_hash(file_path, constants.HASH_MD5)

    def __repr__(self):
        return self._file_uri


if __name__ == constants.MAIN:
    log_handler = [RotatingFileHandler(constants.LOG_FILE, maxBytes=10000000, backupCount=20000, encoding=constants.UTF8)]
    log_format = '%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s'
    log_formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    date_format = constants.DATE_FORMAT

    logging.basicConfig(format=log_format, datefmt=date_format, level=constants.DEBUG_LEVEL, handlers=log_handler)

    console = logging.StreamHandler()
    console.setLevel(constants.DEBUG_LEVEL)
    console.setFormatter(log_formatter)
    logging.getLogger().addHandler(console)

    logging.getLogger(constants.WATCHDOG).setLevel(logging.CRITICAL)

    argument_parser = arg_parse.ArgumentParser()
    argument_parser.add_argument(constants.PARAMETER_PATH, required=False)
    argument_parser.add_argument(constants.PARAMETER_NO_MOVE, required=False, action=constants.STORE_TRUE, help='Do not move file')
    argument_parser.add_argument(constants.PARAMETER_NO_COMP, required=False, action=constants.STORE_TRUE, help='Do not compare file')
    arguments = argument_parser.parse_args()
    no_move_file = str(not arguments.no_move)
    no_comp_file = str(not arguments.no_comp)
    print(f'MOVE [{no_move_file.upper()}]')
    print(f'COMP [{no_comp_file.upper()}]')

    directory_paths = None
    if arguments.path:
        directory_paths = [arguments.path.strip().replace(constants.DOUBLE_QUOTES, constants.EMPTY)]
        print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
        print(f'WATCH OBSERVER: [{directory_paths[0]}]')
        print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')
    else:
        print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
        print(f'WATCH OBSERVER: [DIRECTORY LIST]')
        print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')
        with open(constants.directory_list, constants.READ, encoding=constants.UTF8) as directory_file_list:
            directory_paths = [line.strip() for line in directory_file_list]

    show.warning(f'Starting {constants.LABEL_MAIN} System...')
    file_set = FileList()

    previous_uri = None

    for directory_path in directory_paths:
        print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
        print(f'WORKING ON [{directory_path}]')
        print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')
        for root, dirs, files in os_walk(directory_path, topdown=True):
            for name in files:
                current_uri = str(os_path.join(root, name))
                previous_uri = current_uri
                if uri_exists(current_uri):
                    do_not_move_file = True if arguments.no_move else False
                    do_not_compare_file = True if arguments.no_comp else False
                    try:
                        file_set.add_file(current_uri, {constants.NO_MOVE: do_not_move_file,
                                                        constants.NO_COMP: do_not_compare_file})
                    except Exception as exception:
                        print(f'ERROR: [{current_uri}] [{exception}]')
                if previous_uri == current_uri:
                    current_uri = constants.NONE

    show.warning('Bye...')
