import os
import time
import pystray
import hashlib
import inspect
import threading
import configparser

from PIL import Image
from queue import Queue
from itertools import zip_longest

import argparse as arg_parse
import pandas as pandas_dataframe

from sys import stdin as sys_standard_in
from sys import platform as sys_platform

from filecmp import cmp as file_compare
from filecmp import clear_cache as clear_cache

from os import name as os_name
from os import path as os_path
from os import stat as os_stat
from os import walk as os_walk

from os import unlink as delete_link
from os import remove as delete_file
from os import makedirs as make_dirs

from os.path import isdir as is_dir
from os.path import isfile as is_file
from os.path import islink as is_link
from os.path import abspath as abs_path
from os.path import dirname as dir_name
from os.path import exists as uri_exists

from subprocess import PIPE
from subprocess import STDOUT
from subprocess import CalledProcessError
from subprocess import run as run_command

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

from logging.handlers import RotatingFileHandler

import logging
logger = logging.getLogger('MAIN')

thread_started = time.time()
system_tray_icon = None

MAX_SECONDS = 7

OS_X = 'OS X'
LINUX = 'Linux'
WINDOWS = 'Windows'
WINDOWS_NT = 'nt'

NATIVE = 'Operating_SystemNative'
EXECUTABLE = 'OperatingSystem_Executable'

BUFFER = 'Python_Buffer'
ZIP_LONGEST = 'Python_Zip'
COMPARE = 'Python_FileCmp'

HASH_MD5 = 'Hash_MD5'
HASH_SHA = 'Hash_SHA'
HASH_SHA256 = 'Hash_SHA256'
HASH_SHA512 = 'Hash_SHA512'

LINUX_LINK = 'ln -s'
LINUX_DIFF = 'diff --brief'
LINUX_CMP = 'cmp -b'

WINDOWS_LINK = 'mklink'
WINDOWS_FC = 'fc /B'
WINDOWS_COMP = 'comp /m'

SLASH = 92

SHA = 'SHA'
URI = 'URI'
KIND = 'KIND'

REMOVED = 'remo'
SYMLINK = 'link'
DELETED_PARENT = 'lynk'
FILE = 'file'
MOVED_FILE = 'move'
DATA_TABLE = 'UnDupyKeeper.pkl'

config = configparser.ConfigParser()
config.read('UnDupeKeeper.ini')

UI = config.get('VALUES', 'UI')
MAX_FILES = config.getint('VALUES', 'MAX_FILES')
BUFFER_SIZE = config.getint('VALUES', 'BUFFER_SIZE')
COMPARISON_METHOD = config.get('VALUES', 'COMPARISON_METHOD')

LOG_FILE = config.get('PATHS', 'LOG_FILE')
LINK_TABLE = config.get('PATHS', 'LINK_TABLE')
FILE_TABLE = config.get('PATHS', 'FILE_TABLE')

DEBUG_LEVEL = config.get('DEBUG', 'DEBUG_LEVEL')
DEBUG_TEST = config.getboolean('DEBUG', 'DEBUG_TEST')

SHA_SIZE = 10

clear_ca = False

if os_name == WINDOWS_NT:
    import msvcrt
else:
    import termios
    import atexit
    from select import select


class KBHit:

    def __init__(self):
        if os_name == WINDOWS_NT:
            pass
        else:
            self.fd = sys_standard_in.fileno()
            self.new_term = termios.tcgetattr(self.fd)
            self.old_term = termios.tcgetattr(self.fd)
            self.new_term[3] = (self.new_term[3] & ~termios.ICANON & ~termios.ECHO)
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.new_term)
            atexit.register(self.set_normal_term)

    def set_normal_term(self):
        if os_name == WINDOWS_NT:
            pass
        else:
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.old_term)

    @staticmethod
    def get_character():
        if os_name == WINDOWS_NT:
            return msvcrt.getch().decode('utf-8')
        else:
            return sys_standard_in.read(1)

    @staticmethod
    def get_arrow_key():
        if os_name == WINDOWS_NT:
            msvcrt.getch()
            c = msvcrt.getch()
            arrows = [72, 77, 80, 75]
        else:
            c = sys_standard_in.read(3)[2]
            arrows = [65, 67, 66, 68]
        return arrows.index(ord(c.decode('utf-8')))

    @staticmethod
    def keyboard_hit():
        if os_name == WINDOWS_NT:
            return msvcrt.kbhit()
        else:
            dr, dw, de = select([sys_standard_in], [], [], 0)
            return dr != []

    def check(self):
        if self.keyboard_hit():
            c = self.get_character()
            if ord(c) == SLASH:
                self.set_normal_term()
                return True
        return False


def update_icon(task_event):
    global thread_started
    global system_tray_icon

    paused = False
    check_out = False
    tray_icon_thread_running = True
    icons = ["icons/UnDupyKeeper01.png",
             "icons/UnDupyKeeper02.png",
             "icons/UnDupyKeeper03.png",
             "icons/UnDupyKeeper04.png"]

    logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: STARTED')

    loop_interaction = 0
    while tray_icon_thread_running:
        time.sleep(1)
        if not task_event.empty():
            logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: CHECKING QUEUE - SIZE [{task_event.qsize()}]')
            value = task_event.get()
            if value == 'pause':
                logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: THREAD PAUSED')
                system_tray_icon.icon = Image.open('icons/done.png')
                paused = True
            if value == 'continue':
                logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: THREAD CONTINUE')
                paused = False
            if value == 'terminate':
                logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: THREAD TERMINATE')
                tray_icon_thread_running = False
            if value == 'recheck':
                logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: THREAD RECHECK')
                check_out = True

            task_event.task_done()
        else:
            logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: JOB LIST IS EMPTY')

        time_time = time.time()
        check_time_now = time_time - thread_started
        logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: CHECK TIME [{check_time_now}] = [{time_time}] - [{thread_started}]')
        if check_out and check_time_now > MAX_SECONDS:
            logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: THREAD CHECKED = ICON DONE')
            system_tray_icon.icon = Image.open('icons/done.png')
            paused = True
            check_out = False

            with task_event.mutex:
                task_event.queue.clear()

        if not paused and tray_icon_thread_running:
            logger.debug(f'{line_number()} %%%%%%%%%%%%%%%%%%%%%%%%%%% UPDATE ICON: UPDATE ICON: SET ICON [{loop_interaction}][{icons[loop_interaction]}]')
            system_tray_icon.icon = Image.open(icons[loop_interaction])
            loop_interaction += 1
            if loop_interaction == 3:
                loop_interaction = 0


def line_number():
    return f"{inspect.currentframe().f_back.f_lineno:04d}"


def get_platform():
    logger.info(f'{line_number()} - def get_platform():')
    platforms = {'linux': LINUX,
                 'linux1': LINUX,
                 'linux2': LINUX,
                 'darwin': OS_X,
                 'win32': WINDOWS}
    if sys_platform not in platforms:
        logger.info(f'{line_number()} - if sys_platform not in platforms:')
        logger.info(f'{line_number()} - return sys_platform')
        return sys_platform
    logger.info(f'{line_number()} - platforms[sys_platform]')
    return platforms[sys_platform]


def get_hash(uri_file, digest):
    sha_file = None
    if digest == HASH_SHA:
        digest_method = hashlib.sha1()
    elif digest == HASH_SHA256:
        digest_method = hashlib.sha256()
    elif digest == HASH_SHA512:
        digest_method = hashlib.sha512()
    else:
        digest_method = hashlib.md5()
    memory_view = memoryview(bytearray(128 * 1024))
    retry = True
    while retry:
        try:
            if is_link(uri_file):
                uri_file = os.readlink(uri_file)
            with open(uri_file, 'rb', buffering=0) as uri_locator:
                for element in iter(lambda: uri_locator.readinto(memory_view), 0):
                    digest_method.update(memory_view[:element])
            sha_file = digest_method.hexdigest()
            retry = False
            logger.info(f'{line_number()} - Successful: - [{sha_file[0:6]}] [{uri_file}]')
        except PermissionError as permission_error:
            logger.error(f'{line_number()} - ERROR: PermissionError - {permission_error}')
            time.sleep(1)
    return sha_file


def hash_comparison(first_file, second_file, hash_method):
    return get_hash(first_file, hash_method) == get_hash(second_file, hash_method)


def buffer_comparison(first_file, second_file):
    with open(first_file, 'rb') as first_file_descriptor, open(second_file, 'rb') as second_file_descriptor:
        while True:
            first_file_bytes = first_file_descriptor.read(BUFFER_SIZE)
            second_file_bytes = second_file_descriptor.read(BUFFER_SIZE)
            if first_file_bytes != second_file_bytes:
                return False
            if not first_file_bytes:
                return True


def compare_binaries(first_file, second_file):
    with open(first_file, 'rb') as first_file_descriptor, open(second_file, 'rb') as second_file_descriptor:
        for first_file_line, second_file_line in zip_longest(first_file_descriptor, second_file_descriptor, fillvalue=None):
            if first_file_line == second_file_line:
                continue
            else:
                return False
        return True


def file_equals(first_file, second_file, comparison_method):
    file_not_exist = False
    file_name1 = ""
    file_name2 = ""
    
    if not uri_exists(first_file):
        file_name1 = first_file
        file_not_exist = True
        
    if not uri_exists(second_file):
        file_name2 = second_file
        file_not_exist = True
    
    if file_not_exist:
        logger.error(f'{line_number()} $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ file_equals missing [{file_name1}] [{file_name2}] ')
        return False
        
    if os_stat(first_file).st_size != os_stat(second_file).st_size:
        return False

    if comparison_method == NATIVE:
        validation_string = {LINUX: "",
                             WINDOWS: "FC: no differences encountered"}
        comparison_command = {LINUX: f'{LINUX_CMP} "{second_file}" "{first_file}"',
                              WINDOWS: f'{WINDOWS_FC} "{first_file}" "{second_file}"'}

    elif comparison_method == EXECUTABLE:
        validation_string = {LINUX: "",
                             WINDOWS: "Files compare OK"}
        comparison_command = {LINUX: f'{LINUX_DIFF} "{second_file}" "{first_file}"',
                              WINDOWS: f'{WINDOWS_COMP} "{first_file}" "{second_file}"'}

    elif comparison_method == ZIP_LONGEST:
        return compare_binaries(first_file, second_file)

    elif comparison_method == BUFFER:
        return buffer_comparison(first_file, second_file)

    elif comparison_method == HASH_MD5:
        return hash_comparison(first_file, second_file, HASH_MD5)

    elif comparison_method == HASH_SHA:
        return hash_comparison(first_file, second_file, HASH_SHA)

    elif comparison_method == HASH_SHA256:
        return hash_comparison(first_file, second_file, HASH_SHA256)

    elif comparison_method == HASH_SHA512:
        return hash_comparison(first_file, second_file, HASH_SHA512)

    else:
        return file_compare(first_file, second_file, shallow=False)

    current_platform = get_platform()
    command = comparison_command[current_platform]

    try:
        logger.warning(f'{line_number()} - process = run_command({command},')
        process = run_command(command,
                              shell=True,
                              check=True,
                              stdout=PIPE,
                              stderr=STDOUT,
                              universal_newlines=True)
        return_value = process.stdout.split('\n')
        if return_value[1] == validation_string[current_platform]:
            return True

    except CalledProcessError as called_process_error:
        # f'CalledProcessError       : {called_process_error}'
        # f'CalledProcessError CMD   : {called_process_error.cmd}'
        # f'CalledProcessError OUTPUT: {called_process_error.output}'
        logger.error(f'CalledProcessError RETURN CODE: {called_process_error.returncode}')
        logger.error(f'{line_number()} - Source is File? [{first_file}], Target Exists? [{second_file}]')
    return False


class DataBase:
    def __init__(self):
        logger.info(f'{line_number()} - def __init__(self):')
        self._file_allocation_table = pandas_dataframe.DataFrame(columns=[SHA, KIND, URI])

    def get_total_files_count(self):
        return len(self._file_allocation_table.index)

    def get_unique_files_count(self):
        return self._file_allocation_table[SHA].nunique()

    def get_all_files_rows(self):
        return self._file_allocation_table.iterrows()

    def get_all_uri_rows(self):
        return self._file_allocation_table[URI].values

    def get_file_list(self):
        return self._file_allocation_table.loc[self._file_allocation_table[KIND] == FILE]
        # return self._file_allocation_table.loc[(self._file_allocation_table[KIND] == FILE)]

    def get_link_list(self):
        return self._file_allocation_table.loc[self._file_allocation_table[KIND] == SYMLINK]
        # return self._file_allocation_table.loc[(self._file_allocation_table[KIND] == SYMLINK)]

    def get_deleted_list(self):
        return self._file_allocation_table.loc[self._file_allocation_table[KIND] == REMOVED]
        # return self._file_allocation_table.loc[(self._file_allocation_table[KIND] == REMOVED)]

    def get_deleted_parents(self):
        return self._file_allocation_table.loc[self._file_allocation_table[KIND] == DELETED_PARENT]

    def save_database(self):
        self._file_allocation_table.to_pickle(DATA_TABLE)

    def load_database(self):
        self._file_allocation_table = pandas_dataframe.read_pickle(DATA_TABLE)

    def add_file_to_database(self, file_name):
        self._file_allocation_table = self._file_allocation_table.append(file_name, ignore_index=True)

    def delete_file_from_database(self, file_index):
        self._file_allocation_table.drop(file_index.index, inplace=True)

    def get_index_values(self, index_file):
        return self._file_allocation_table[index_file].values

    def change_from_link_to_moved(self, file_hash):
        self._file_allocation_table.loc[(self._file_allocation_table[SHA] == file_hash) & (self._file_allocation_table[KIND] == SYMLINK), KIND] = MOVED_FILE

    def change_from_moved_to_link(self, file_uri):
        self._file_allocation_table.loc[(self._file_allocation_table[URI] == file_uri) & (self._file_allocation_table[KIND] == MOVED_FILE), KIND] = SYMLINK

    def change_from_link_to_deleted(self, file_hash):
        self._file_allocation_table.loc[(self._file_allocation_table[SHA] == file_hash) & (self._file_allocation_table[KIND] == SYMLINK), KIND] = DELETED_PARENT

    def changed_from_deleted_to_link(self, file_hash):
        self._file_allocation_table.loc[(self._file_allocation_table[SHA] == file_hash) & (self._file_allocation_table[KIND] == DELETED_PARENT), KIND] = SYMLINK

    def move_hash_location(self, source_hash, target_hash):
        self._file_allocation_table.loc[(self._file_allocation_table[SHA] == source_hash), SHA] = target_hash
        # self._file_allocation_table.loc[(self._file_allocation_table[SHA] == uri_addr), SHA] = sha

    def move_file_location(self, source_file, target_file):
        self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file), URI] = target_file

    def update_file_location(self, source_file, target_file):
        self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file) & (self._file_allocation_table[KIND] == FILE), URI] = target_file

    def get_file_index(self, source_file, kind):
        return self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file) & (self._file_allocation_table[KIND] == kind)]

    def get_file_index_by_kind(self, index_file, key_index, kind):
        return self._file_allocation_table.loc[(self._file_allocation_table[index_file] == key_index) & (self._file_allocation_table[KIND] == kind)]


class FileList:
    def __init__(self):
        logger.info(f'{line_number()} - def __init__(self):')
        self._save_count = 0
        self._file_database = DataBase()
        self.load_data()
        self.thread_start_time = None
        self.running_now = False
        self.running_thread = None
        self.end_task_thread = None
        self.jobs = Queue()
        self.time_is_now_less_started = time.time()

    def __repr__(self):
        logger.info(f'{line_number()} - def __repr__(self):')
        return self.print_table()

    def start_thread(self):
        if not self.running_now:
            logger.debug(f'{line_number()} >>>>>>>>>>>>>>>>>>>>>>>> START THREAD: TRAY NOT RUNNING: STARTING')
            self.running_now = True
            self.thread_start_time = time.time()
            self.jobs.put('pause')
            self.running_thread = threading.Thread(name='UnDupyKeeperThread', target=update_icon, args=(self.jobs,))
            self.running_thread.start()

    def update_thread_started_time(self):
        logger.debug(f'{line_number()} ================== UPDATE TIME: CONTINUE THREAD')
        global thread_started
        thread_started = self.thread_start_time = time.time()
        self.jobs.put('continue')

    def terminate(self):
        logger.debug(f'{line_number()} <<<<<<<<<<<<<<<<<<< TERMINATE THREAD')
        self.jobs.put('terminate')

    def pause_thread(self):
        self.time_is_now_less_started = time.time() - self.thread_start_time
        logger.debug(f'{line_number()} ||||||||||||||||||||||| PAUSE THREAD: TIME NOW IN SECOND [{self.time_is_now_less_started}]')
        if self.time_is_now_less_started > MAX_SECONDS:
            logger.debug(f'{line_number()} ||||||||||||||||||||||| PAUSE THREAD: SET ICON TO GREEN')
            system_tray_icon.icon = Image.open("icons/done.png")
            self.jobs.put('pause')
        else:
            logger.debug(f'{line_number()} ||||||||||||||||||||||| PAUSE THREAD: SET ICON TO CHANGE LATER')
            self.jobs.put('recheck')

    def print_table(self):
        logger.info(f'{line_number()} - def print_table(self):')
        return_string = ''
        for index, row in self._file_database.get_all_files_rows():
            return_string += f'FILE TABLE: [{row[KIND]}] [{row[SHA][0:SHA_SIZE]}] [{row[URI]}]\n'
        logger.info(f'{line_number()} - return return_string')
        return return_string

    def save_data(self, now=False):
        logger.info(f'{line_number()} - def save_data(self):')

        if DEBUG_TEST:
            logger.info(f'{line_number()} - with open(FILE_TABLE, "w") as file_table_handler:')
            with open(FILE_TABLE, 'w', encoding='utf-8') as file_table_handler:
                save_data_index = self._file_database.get_file_list()
                if save_data_index is not None and not save_data_index.empty:
                    for value in save_data_index[URI].values:
                        file_table_handler.write(value + '\n')
                    file_table_handler.flush()

            logger.info(f'{line_number()} - with open(LINK_TABLE, "w") as link_table_handler:')
            with open(LINK_TABLE, 'w', encoding='utf-8') as link_table_handler:
                save_data_index = self._file_database.get_link_list()
                if save_data_index is not None and not save_data_index.empty:
                    for value in save_data_index[URI].values:
                        link_table_handler.write(value + '\n')
                    link_table_handler.flush()
                save_data_index = self._file_database.get_deleted_parents()
                if save_data_index is not None and not save_data_index.empty:
                    for value in save_data_index[URI].values:
                        link_table_handler.write(value + '\n')
                    link_table_handler.flush()

        logger.warning(f'{line_number()} - SAVED DATA:')
        self.report_data()

        self._save_count += 1
        if self._save_count > MAX_FILES or now:
            logger.info(f'{line_number()} - if self._save_count > {MAX_FILES}:')
            logger.info(f'{line_number()} - self._file_allocation_table.to_pickle(DATA_TABLE)')
            self._file_database.save_database()
            self._save_count = 0
            if clear_ca:
                clear_cache()

    def load_data(self):
        logger.info(f'{line_number()} - def load_data(self):')
        try:
            if uri_exists(DATA_TABLE):
                logger.info(f'{line_number()} - self._file_allocation_table = pd.read_pickle(DATA_TABLE)')
                self._file_database.load_database()

                logger.warning(f'{line_number()} - LOADED DATA:')
                self.report_data()
            else:
                logger.warning(f'{line_number()} - FIRST EXECUTION - NO DATABASE YET.')

        except Exception as exception:
            logger.info(f'{line_number()} - FILE NOT FOUND: {exception}')

    @staticmethod
    def execute(target_link, source_file):
        logger.info(f'{line_number()} - def execute(self, {target_link}, {source_file}):')
        return_value = None

        source_is_file = is_file(source_file)
        target_exists = uri_exists(target_link)
        if source_is_file and not target_exists:
            logger.info(f'{line_number()} - if source_is_file and not target_exists:')
            dir_name_path = dir_name(abs_path(target_link))
            if not uri_exists(dir_name_path):
                logger.info(f'{line_number()} - if not uri_exists({dir_name_path}):')
                logger.info(f'{line_number()} - make_dirs({dir_name_path})')
                make_dirs(dir_name_path)

            link_command = {LINUX: f'{LINUX_LINK} "{source_file}" "{target_link}"',
                            WINDOWS: f'{WINDOWS_LINK} "{target_link}" "{source_file}"'}
            command = link_command[get_platform()]

            try:
                logger.warning(f'{line_number()} - process = run_command({command},')
                process = run_command(command,
                                      shell=True,
                                      check=True,
                                      stdout=PIPE,
                                      universal_newlines=True)
                return_value = process.stdout
            except CalledProcessError as called_process_error:
                logger.error(f'{line_number()} - CalledProcessError {called_process_error}')
                logger.error(f'{line_number()} - Source is File? [{source_is_file}], Target Exists? [{target_exists}]')
        else:
            logger.error(f'{line_number()} - ERROR: Source is File? [{source_is_file}], Target Exists? [{target_exists}]')

        logger.info(f'{line_number()} - return {return_value}')
        return return_value

    def new_row(self, new_file, kind):
        logger.info(f'{line_number()} - def new_row(self, {new_file}, {kind}):')
        new_row = {SHA: new_file.file_sha,
                   KIND: kind,
                   URI: new_file.file_uri}
        logger.info(f'{line_number()} - self._file_allocation_table = self._file_allocation_table._append(new_row, ignore_index=True)')
        self._file_database.add_file_to_database(new_row)

    @staticmethod
    def delete_row(row):
        logger.info(f'{line_number()} - def delete_row(self, {row}):')
        try:
            logger.info(f'{line_number()} - delete_file({row})')
            if not is_dir(row):
                if is_link(row):
                    logger.info(f'{line_number()} - DELETE LINK: {row}')
                    delete_link(row)
                else:
                    logger.info(f'{line_number()} - DELETE FILE: {row}')
                    delete_file(row)
            else:
                logger.error(f'{line_number()} - CRITICAL ERROR: TRYING TO DELETE DIRECTORY: {row}')
        except FileNotFoundError as file_not_found_error:
            logger.error(f'{line_number()} - FileNotFoundError: {row} {file_not_found_error}')

    def get_file_index(self, index_uri, kind):
        logger.info(f'{line_number()} - def get_file_index(self, {index_uri}, {kind}):')
        if index_uri in self._file_database.get_all_uri_rows():
            index_file = self._file_database.get_file_index(index_uri, kind)
            if index_file is not None and not index_file.empty:
                logger.info(f'{line_number()} - return file_index')
                return index_file
        logger.info(f'{line_number()} - return None')
        return None

    def get_file(self, get_uri, kind):
        logger.info(f'{line_number()} - def get_file(self, {get_uri}, {kind}):')
        index_file = self.get_file_index(get_uri, kind)
        if index_file is not None and not index_file.empty:
            logger.info(f'{line_number()} - return file_index[URI].values[0]')
            return index_file[URI].values[0]
        logger.info(f'{line_number()} - return None')
        return None

    def get_files(self, key_index, kind, index_file):
        logger.info(f'{line_number()} - def get_files(self, {key_index[0:SHA_SIZE]}, {kind}, {index_file}):')
        if key_index in self._file_database.get_index_values(index_file):
            index_file = self._file_database.get_file_index_by_kind(index_file, key_index, kind)
            if index_file is not None and not index_file.empty:
                logger.info(f'{line_number()} - return file_index[URI].values')
                return index_file[URI].values
        logger.info(f'{line_number()} - return None')
        return None

    def report_data(self):
        total = self._file_database.get_total_files_count()
        unique = self._file_database.get_unique_files_count()
        logger.warning(f'{line_number()} - TOTAL        FILES: {total}')
        logger.warning(f'{line_number()} - TOTAL DUPE   FILES: {total - unique}')
        logger.warning(f'{line_number()} - TOTAL UNIQUE FILES: {unique}')
        if DEBUG_LEVEL == 'INFO':
            logger.info(f'{line_number()} - =================================================================================================')
            for database_item in self._file_database.get_all_files_rows():
                logger.info(f'{line_number()} - [{database_item[1][KIND]}] [{database_item[1][SHA][0:SHA_SIZE]}] [{database_item[1][URI]}]')
            logger.info(f'{line_number()} - =================================================================================================')

    def update_junk(self):
        logger.info(f'{line_number()} - def update_junk(self):')
        index_file = self._file_database.get_deleted_list()
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                delete_index = self.get_file_index(uri_index, REMOVED)
                if delete_index is not None:
                    logger.warning(f'{line_number()} - DROP JUNK: {uri_index}')
                    self._file_database.delete_file_from_database(delete_index)
        logger.info(f'{line_number()} - def update_junk(self): RETURN')

    def update_files(self):
        logger.info(f'{line_number()} - def update_files(self):')
        index_file = self._file_database.get_file_list()
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                if not uri_exists(uri_index):
                    delete_index = self.get_file_index(uri_index, FILE)
                    delete_sha = delete_index[SHA].values[0]
                    if delete_index is not None:
                        logger.warning(f'{line_number()} - DROP FILE: {uri_index}')
                        self._file_database.delete_file_from_database(delete_index)
                        delete_index = self.get_files(delete_sha, SYMLINK, SHA)
                        if delete_index is not None:
                            for row in delete_index:
                                logger.warning(f'{line_number()} - self.delete_row({row})')
                                self.delete_row(row)
                        self._file_database.change_from_link_to_deleted(delete_sha)

        logger.info(f'{line_number()} - def update_files(self): RETURN')

    def update_links(self):
        logger.info(f'{line_number()} - def update_links(self):')
        index_file = self._file_database.get_link_list()
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                if not is_link(uri_index) and not uri_exists(uri_index):
                    delete_index = self.get_file_index(uri_index, SYMLINK)
                    if delete_index is not None:
                        logger.warning(f'{line_number()} - DROP LINK: {uri_index}')
                        self._file_database.delete_file_from_database(delete_index)
        logger.info(f'{line_number()} - def update_links(self): RETURN')

    def add_file(self, add_uri):
        self.update_thread_started_time()
        logger.warning(f'{line_number()} - ==============================================')
        logger.warning(f'{line_number()} - def add_file(self, {add_uri}):')

        if is_link(add_uri):
            logger.info(f'{line_number()} - if is_link({add_uri}):')
            new_file = FileHolder(add_uri)
            logger.info(f'{line_number()} - line = self.get_files({new_file.file_sha[0:SHA_SIZE]}, FILE, SHA)')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is not None:
                logger.info(f'{line_number()} - if line is not None:')
                logger.info(f'{line_number()} - line = self.get_files({new_file.file_uri}, SYMLINK, URI)')
                line = self.get_files(new_file.file_uri, SYMLINK, URI)
                if line is None:
                    logger.info(f'{line_number()} - if line is None:')
                    logger.info(f'{line_number()} - self.new_row({new_file}, SYMLINK)')
                    self.new_row(new_file, SYMLINK)
            self._file_database.change_from_moved_to_link(add_uri)

        elif is_dir(add_uri):
            logger.warning(f'{line_number()} - elif is_dir({add_uri}):')
            logger.warning(f'{line_number()} - return')

        elif is_file(add_uri):
            logger.info(f'{line_number()} - elif is_file({add_uri}):')
            new_file = FileHolder(add_uri)
            logger.info(f'{line_number()} - line = self.get_files({new_file.file_sha[0:SHA_SIZE]}, FILE, SHA)')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is None:
                logger.info(f'{line_number()} - if line is None:')
                logger.info(f'{line_number()} - self.new_row({new_file}, FILE)')
                self.new_row(new_file, FILE)

                logger.info(f'{line_number()} - line = self.get_files({new_file.file_sha[0:SHA_SIZE]}, DELETED_PARENT, SHA)')
                line = self.get_files(new_file.file_sha, DELETED_PARENT, SHA)
                if line is not None:
                    logger.info(f'{line_number()} - if line is not None:')
                    for row in line:
                        logger.info(f'{line_number()} - self.execute({row}, {new_file.file_uri})')
                        self.execute(row, new_file.file_uri)
                self._file_database.changed_from_deleted_to_link(new_file.file_sha)
            else:
                line = line[0]
                logger.info(f'{line_number()} - line_uri = self.get_file({new_file.file_uri}, SYMLINK)')
                line_uri = self.get_file(new_file.file_uri, SYMLINK)
                if line_uri is None:
                    logger.info(f'{line_number()} - if line_uri is None:')
                    logger.info(f'{line_number()} - and file_equals({new_file.file_uri}, {line}, COMPARISON_METHOD):')
                    if new_file.file_uri != line and file_equals(new_file.file_uri, line, COMPARISON_METHOD):
                        logger.info(f'{line_number()} - if {new_file.file_uri} != {line}')
                        logger.info(f'{line_number()} - if file_equals({new_file.file_uri}, {line}, {COMPARISON_METHOD}):')

                        logger.info(f'{line_number()} - self.new_row({new_file}, kind=SYMLINK)')
                        self.new_row(new_file, kind=SYMLINK)
                        logger.info(f'{line_number()} - self.new_row({new_file}, kind=REMOVED)')
                        self.new_row(new_file, kind=REMOVED)

                        logger.info(f'{line_number()} - self.delete_row({new_file.file_uri})')
                        self.delete_row(new_file.file_uri)
                        logger.info(f'{line_number()} - self.execute({new_file.file_uri}, {line})')
                        self.execute(new_file.file_uri, line)
                else:
                    logger.error(f'{line_number()} - else: {line_uri}')

        logger.info(f'{line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{line_number()} - return')
        self.pause_thread()

    def mod_file(self, mod_uri):
        self.update_thread_started_time()
        logger.warning(f'{line_number()} - ==============================================')
        logger.warning(f'{line_number()} - def mod_file(self, {mod_uri}):')
        if is_link(mod_uri):
            logger.warning(f'{line_number()} - if is_link({mod_uri}):')
            logger.info(f'{line_number()} - return')
        elif is_dir(mod_uri):
            logger.warning(f'{line_number()} - if is_dir({mod_uri}):')
            logger.warning(f'{line_number()} - return')
        elif is_file(mod_uri):
            logger.info(f'{line_number()} - elif is_file({mod_uri}):')
            new_file = FileHolder(mod_uri)

            logger.info(f'{line_number()} - gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.info(f'{line_number()} - if gotten_by_sha is not None:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.info(f'{line_number()} - if {new_file.file_uri} == {gotten_by_sha[0]}:')
                    logger.info(f'{line_number()} - FILE MOVED - NO HANDLING HERE')
                    logger.info(f'{line_number()} - FILE UNCHANGED {new_file.file_uri}')
                    logger.info(f'{line_number()} - URI    CHANGED {gotten_by_sha[0]}')

            logger.info(f'{line_number()} - gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.info(f'{line_number()} - if gotten_by_uri is not None:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.info(f'{line_number()} - if {new_file.file_sha[0:SHA_SIZE]} != {gotten_by_uri[SHA].values[0][0:SHA_SIZE]}:')
                    logger.info(f'{line_number()} - CONTENT CHANGED - MUST UPDATE LINKS WHEN APPLICABLE')
                    logger.info(f'{line_number()} - OLD: {gotten_by_uri[SHA].values[0][0:SHA_SIZE]}')
                    logger.info(f'{line_number()} - NEW: {new_file.file_sha[0:SHA_SIZE]}')
                    self._file_database.move_hash_location(gotten_by_uri[SHA].values[0], new_file.file_sha)

            if gotten_by_sha is None and gotten_by_uri is None:
                logger.info(f'{line_number()} - if gotten_by_sha is None and gotten_by_uri is None:')
                logger.info(f'{line_number()} - self.add_file({mod_uri})')
                self.add_file(mod_uri)

        logger.info(f'{line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{line_number()} - return')
        self.pause_thread()

    def move_file(self, source_file, target_file):
        self.update_thread_started_time()
        logger.warning(f'{line_number()} - ==============================================')
        logger.warning(f'{line_number()} - def move_file(self, {source_file}, {target_file}):')

        if is_link(target_file):
            logger.info(f'{line_number()} - TRUE is_link({target_file}):')
            logger.info(f'{line_number()} - self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file), URI] = target_file')
            self._file_database.move_file_location(source_file, target_file)

        elif is_file(target_file):
            logger.info(f'{line_number()} - TRUE is_file({target_file}):')
            new_file = FileHolder(target_file)
            logger.info(f'{line_number()} - gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.info(f'{line_number()} - if gotten_by_uri is not None:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.info(f'{line_number()} - if new_file.file_sha != gotten_by_uri[SHA].values[0]:')
                    # TODO: MUST CHECK IF NEW SHA ALREADY EXIST ON DATABASE
                    # IF NOT EXIST, GO AHEAD
                    # IF EXIST, MANAGE DUPE
                    logger.info(f'{line_number()} - SHA CHANGED FROM {gotten_by_uri[SHA].values[0][0:SHA_SIZE]}')
                    logger.info(f'{line_number()} - SHA CHANGED TO   {new_file.file_sha[0:SHA_SIZE]}')
                    logger.info(f'{line_number()} - self._file_allocation_table.loc[(self._file_allocation_table[SHA] == \
                                                                                    gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha')
                    self._file_database.move_hash_location(gotten_by_uri[SHA].values[0], new_file.file_sha)

            logger.info(f'{line_number()} - gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.info(f'{line_number()} - if gotten_by_sha is not None:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.info(f'{line_number()} - if if new_file.file_uri != gotten_by_sha[0]:')
                    logger.info(f'{line_number()} - URI CHANGED FROM {gotten_by_sha[0]}')
                    logger.info(f'{line_number()} - URI CHANGED TO   {new_file.file_uri}')
                    logger.info(f'{line_number()} - CHANGING MAIN FILE ADDRESS BEFORE')
                    logger.debug(f'{line_number()} - \n{self.print_table()}')
                    self._file_database.update_file_location(gotten_by_sha[0], new_file.file_uri)
                    logger.info(f'{line_number()} - CHANGING MAIN FILE ADDRESS AFTER')
                    logger.debug(f'{line_number()} - \n{self.print_table()}')
                    self._file_database.change_from_link_to_moved(new_file.file_sha)
                    logger.info(f'{line_number()} - CHANGING LINK FILE ADDRESS')
                    logger.debug(f'{line_number()} - \n{self.print_table()}')
                    line = self.get_files(new_file.file_sha, MOVED_FILE, SHA)
                    if line is not None:
                        logger.info(f'{line_number()} - if line is not None:')
                        for row in line:
                            logger.info(f'{line_number()} - self.delete_row({row})')
                            self.delete_row(row)
                            logger.info(f'{line_number()} - self.execute({row}, {new_file.file_uri})')
                            self.execute(row, new_file.file_uri)
        logger.info(f'{line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{line_number()} - return')
        self.pause_thread()

    def del_file(self, del_uri):
        self.update_thread_started_time()
        logger.warning(f'{line_number()} - ==============================================')
        logger.warning(f'{line_number()} - def del_file(self, {del_uri}):')

        logger.info(f'{line_number()} - delete_index = self.get_file_index(uri, REMOVED)')
        delete_index = self.get_file_index(del_uri, REMOVED)
        logger.info(f'{line_number()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{line_number()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_database.delete_file_from_database(delete_index)
            logger.info(f'{line_number()} - self.save_data()')
            self.save_data()
            logger.info(f'{line_number()} - return')
            return

        logger.info(f'{line_number()} - delete_index = self.get_file_index(uri, SYMLINK)')
        delete_index = self.get_file_index(del_uri, SYMLINK)
        logger.info(f'{line_number()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{line_number()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_database.delete_file_from_database(delete_index)
            logger.info(f'{line_number()} - self.save_data()')
            self.save_data()
            logger.info(f'{line_number()} - return')
            return

        logger.info(f'{line_number()} - delete_index = self.get_file_index(uri, FILE)')
        delete_index = self.get_file_index(del_uri, FILE)
        logger.info(f'{line_number()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{line_number()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_database.delete_file_from_database(delete_index)
            sha = delete_index[SHA].values[0]
            logger.info(f'{line_number()} - delete_index = self.get_files(sha, SYMLINK, SHA)')
            delete_index = self.get_files(sha, SYMLINK, SHA)
            logger.info(f'{line_number()} - if delete_index is not None: {delete_index is not None}')
            if delete_index is not None:
                for row in delete_index:
                    logger.warning(f'{line_number()} - self.delete_row({row})')
                    self.delete_row(row)
                self._file_database.change_from_link_to_deleted(sha)

        logger.info(f'{line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{line_number()} - return')
        self.pause_thread()


class FileHolder:
    def __init__(self, file_path):
        logger.info(f'{line_number()} - FileHolder {file_path}')
        self._file_uri = file_path
        self._file_sha = None
        self.set_sha()

    @property
    def file_uri(self):
        logger.info(f'{line_number()} - file_uri {self._file_uri}')
        return self._file_uri

    @file_uri.setter
    def file_uri(self, universal_resource_indicator):
        self._file_uri = universal_resource_indicator
        self.set_sha()
        logger.info(f'{line_number()} - file_uri [{self._file_sha[0:SHA_SIZE]}] [{self._file_uri}]')

    @property
    def file_sha(self):
        logger.info(f'{line_number()} - file_sha [{self._file_sha[0:SHA_SIZE]}]')
        return self._file_sha

    def set_sha(self):
        logger.info(f'{line_number()} - set_sha')
        if self._file_uri:
            self._file_sha = get_hash(self._file_uri, HASH_SHA512)

    def __repr__(self):
        logger.info(f'{line_number()} - def __repr__(self):')
        logger.info(f'{line_number()} - return self._file_uri')
        return self._file_uri


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        file_list.add_file(event.src_path)
        logger.debug(f'{line_number()} - ON_CREATED\n{file_list}')

    def on_modified(self, event):
        file_list.mod_file(event.src_path)
        logger.debug(f'{line_number()} - ON_MODIFIED\n{file_list}')

    def on_moved(self, event):
        file_list.move_file(event.src_path, event.dest_path)
        logger.debug(f'{line_number()} - ON_MOVED\n{file_list}')

    def on_deleted(self, event):
        file_list.del_file(event.src_path)
        logger.debug(f'{line_number()} - ON_DELETED\n{file_list}')


def tray_icon_click(_, selected_tray_item):
    global system_tray_icon
    logger.warning(f'{line_number()} - Tray Icon Click: [{str(selected_tray_item)}]')
    if str(selected_tray_item) == "Exit":
        file_list.save_data(True)
        file_list.terminate()
        logger.warning(f'{line_number()} - Terminating UnDupyKeeper System...')
        system_tray_icon.stop()
    logger.warning(f'{line_number()} - Tray Icon Done...')


if __name__ == "__main__":
    log_handler = [RotatingFileHandler(LOG_FILE, maxBytes=10000000, backupCount=20000, encoding='utf-8')]
    log_format = '%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s'
    log_formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    date_format = '%H:%M:%S'

    logging.basicConfig(format=log_format,
                        datefmt=date_format,
                        level=DEBUG_LEVEL,
                        handlers=log_handler)

    console = logging.StreamHandler()
    console.setLevel(DEBUG_LEVEL)
    console.setFormatter(log_formatter)
    logging.getLogger().addHandler(console)

    logging.getLogger("watchdog").setLevel(logging.CRITICAL)

    argument_parser = arg_parse.ArgumentParser()
    argument_parser.add_argument('--path', required=True)
    argument_parser.add_argument('--scan', required=False, default=False)
    arguments = argument_parser.parse_args()

    src_path = arguments.path
    src_scan = arguments.scan

    state = False
    system_tray_image = Image.open("icons/done.png")
    system_tray_icon = pystray.Icon("UnDupyKeeper 1",
                                    system_tray_image,
                                    "UnDupyKeeper 2",
                                    menu=pystray.Menu(
                                        pystray.MenuItem("Done", tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem("Pause", tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem("Error", tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem("Resume", tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem("Exit", tray_icon_click, checked=lambda item: state)))

    logger.warning(f'{line_number()} - Starting UnDupyKeeper System...')
    file_list = FileList()
    file_list.start_thread()

    if src_scan:
        file_list.update_links()
        file_list.update_files()
        for root, dirs, files in os_walk(src_path, topdown=True):
            for name in files:
                uri = str(os_path.join(root, name))
                if uri_exists(uri):
                    file_list.add_file(uri)
        file_list.update_junk()
        file_list.pause_thread()

    logger.warning(f'========== Initialized... ===================')
    file_list.report_data()
    logger.warning(f'=============================================')

    event_handler = MonitorFolder()
    observer = Observer()
    observer.schedule(event_handler, path=src_path, recursive=True)

    observer.start()

    if UI == 'GUI':
        logger.warning(f'========== Tray Initialized... ==============')
        system_tray_icon.run()

    if UI == 'CLI':
        logger.warning(f'========== Keyboard Initialized... ==========')
        keyboard_listening = True
        try:
            keyboard = KBHit()
            while keyboard_listening:
                time.sleep(1)
                if keyboard.check():
                    file_list.save_data(True)
                    logger.warning(f'{line_number()} - Terminating UnDupyKeeper System...')
                    keyboard_listening = False

        except KeyboardInterrupt as keyboard_interrupt:
            logger.info(f'{line_number()} -  KeyboardInterrupt: [{keyboard_interrupt}]')

    file_list.terminate()
    observer.stop()
    observer.join()

    logger.warning(f'========== Bye... ===========================')
