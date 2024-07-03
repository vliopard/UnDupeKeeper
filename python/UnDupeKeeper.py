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
show = logging.getLogger('UnDupyKeeper')

# DEBUG=10
# INFO=20
# WARN=30
# ERROR=40
# CRITICAL=50

thread_started = time.time()
system_tray_icon = None

GUI = 'GUI'
CLI = 'CLI'
DATE_FORMAT = '%H:%M:%S'
LEVEL_INFO = 'INFO'

ICON_DONE = 'icons/done.png'
ICON_ERROR = 'icons/error.png'
ICON_PAUSE = 'icons/pause.png'

LABEL_MAIN = 'UnDupyKeeper'
LABEL_DONE = 'Done'
LABEL_PAUSE = 'Pause'
LABEL_ERROR = 'Error'
LABEL_EXIT = 'Exit'

SYMBOL_PERCENT = '%'
SYMBOL_MONEY = '$'
SYMBOL_UNDERLINE = '_'
SYMBOL_GT = '>'
SYMBOL_EQ = '='
SYMBOL_LT = '<'
SYMBOL_PIPE = '|'

DEBUG_MARKER = 'DEBUG:'

PARAMETER_PATH = '--path'
PARAMETER_SCAN = '--scan'

PLATFORM_LINUX0 = 'linux'
PLATFORM_LINUX1 = 'linux1'
PLATFORM_LINUX2 = 'linux2'
PLATFORM_DARWIN = 'darwin'
PLATFORM_WIN32 = 'win32'

THREAD_NAME = f'{LABEL_MAIN}Thread'

FCB_EQUAL = 'FC: no differences encountered'
EXE_EQUAL = 'Files compare OK'
COMPARE_TEXT = 'Comparing'

WATCHDOG = 'watchdog'

UTF8 = 'utf-8'

NEW_LINE = '\n'
DOS_SLASH = '\\'
UNIX_SLASH = '/'

EMPTY = ''
WRITE = 'w'
READ_BINARY = 'rb'

DOS_DRIVE = 'c:'
OS_X = 'OS X'
LINUX = 'Linux'
WINDOWS = 'Windows'
WINDOWS_NT = 'nt'

PAUSE = 'pause'
CONTINUE = 'continue'
TERMINATE = 'terminate'
RECHECK = 'recheck'

NATIVE = 'Operating_SystemNative'
EXECUTABLE = 'OperatingSystem_Executable'

BUFFER = 'Python_Buffer'
ZIP_LONGEST = 'Python_Zip'
COMPARE = 'Python_FileCmp'

HASH_MD5 = 'Hash_MD5'
HASH_SHA = 'Hash_SHA'
HASH_SHA256 = 'Hash_SHA256'
HASH_SHA512 = 'Hash_SHA512'

MOVE_ICO1 = 'icons/UnDupyKeeper01.png'
MOVE_ICO2 = 'icons/UnDupyKeeper02.png'
MOVE_ICO3 = 'icons/UnDupyKeeper03.png'
MOVE_ICO4 = 'icons/UnDupyKeeper04.png'

LINUX_LINK = 'ln -s'
LINUX_DIFF = 'diff --brief'
LINUX_CMP = 'cmp -b'

WINDOWS_LINK = 'mklink'
WINDOWS_FC = 'fc /B'
WINDOWS_COMP = 'comp /m'

SLASH = 92
SHA_SIZE = 10
LINE_LEN = 30
MAX_SECONDS = 7

SHA = 'SHA'
URI = 'URI'
KIND = 'KIND'

REMOVED = 'remo'
SYMLINK = 'link'
DELETED_PARENT = 'lynk'
FILE = 'file'
MOVED_FILE = 'move'
DATA_TABLE = f'{LABEL_MAIN}.pkl'

SETTINGS_FILE = 'UnDupeKeeper.ini'

config = configparser.ConfigParser()
config.read(SETTINGS_FILE)

UI = config.get('VALUES', 'UI')
MAX_FILES = config.getint('VALUES', 'MAX_FILES')
BUFFER_SIZE = config.getint('VALUES', 'BUFFER_SIZE')
COMPARISON_METHOD = config.get('VALUES', 'COMPARISON_METHOD')

MAIN_PATH = config.get('PATHS', 'MAIN_PATH')
LOG_FILE = config.get('PATHS', 'LOG_FILE')
LINK_TABLE = config.get('PATHS', 'LINK_TABLE')
FILE_TABLE = config.get('PATHS', 'FILE_TABLE')

DEBUG_LEVEL = config.get('DEBUG', 'DEBUG_LEVEL')
DEBUG_TEST = config.getboolean('DEBUG', 'DEBUG_TEST')

CLEAR_CACHE = False

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
            return msvcrt.getch().decode(UTF8)
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
        return arrows.index(ord(c.decode(UTF8)))

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


def section_line(style, size):
    return style * size


def update_icon(task_event):
    global thread_started
    global system_tray_icon

    function_name = 'UPDATE ICON:'
    section_message = section_line(SYMBOL_PERCENT, LINE_LEN)

    paused = False
    check_out = False
    tray_icon_thread_running = True
    icons = [MOVE_ICO1, MOVE_ICO2, MOVE_ICO3, MOVE_ICO4]

    show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} STARTED')

    loop_interaction = 0
    while tray_icon_thread_running:
        time.sleep(1)
        if not task_event.empty():
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} CHECKING QUEUE - SIZE [{task_event.qsize()}]')
            value = task_event.get()
            if value == PAUSE:
                show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} THREAD PAUSED')
                system_tray_icon.icon = Image.open(ICON_DONE)
                paused = True
            if value == CONTINUE:
                show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} THREAD CONTINUE')
                paused = False
            if value == TERMINATE:
                show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} THREAD TERMINATE')
                tray_icon_thread_running = False
            if value == RECHECK:
                show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} THREAD RECHECK')
                check_out = True

            task_event.task_done()
        else:
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} JOB LIST IS EMPTY')

        time_time = time.time()
        check_time_now = time_time - thread_started
        show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} CHECK TIME [{check_time_now}] = [{time_time}] - [{thread_started}]')
        if check_out and check_time_now > MAX_SECONDS:
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} THREAD CHECKED = ICON DONE')
            system_tray_icon.icon = Image.open(ICON_DONE)
            paused = True
            check_out = False

            with task_event.mutex:
                task_event.queue.clear()

        if not paused and tray_icon_thread_running:
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} {function_name} SET ICON [{loop_interaction}][{icons[loop_interaction]}]')
            system_tray_icon.icon = Image.open(icons[loop_interaction])
            loop_interaction += 1
            if loop_interaction == 3:
                loop_interaction = 0


def line_number():
    return f'[{inspect.currentframe().f_back.f_lineno:04d}]'


def get_platform():
    function_name = 'GET PLATFORM:'
    show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} STARTED')
    platforms = {PLATFORM_LINUX0: LINUX,
                 PLATFORM_LINUX1: LINUX,
                 PLATFORM_LINUX2: LINUX,
                 PLATFORM_DARWIN: OS_X,
                 PLATFORM_WIN32: WINDOWS}
    if sys_platform not in platforms:
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} UNDESIRED END')
        show.info(f'{line_number()} {function_name} RETURN [{sys_platform}]')
        return sys_platform
    show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} NORMAL END')
    return platforms[sys_platform]


def get_hash(uri_file, digest):
    function_name = 'GET HASH:'
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
            with open(uri_file, READ_BINARY, buffering=0) as uri_locator:
                for element in iter(lambda: uri_locator.readinto(memory_view), 0):
                    digest_method.update(memory_view[:element])
            sha_file = digest_method.hexdigest()
            retry = False
            show.info(f'{line_number()} {function_name} HASH OBTAINED [{sha_file[0:SHA_SIZE]}] [{uri_file}]')
        except PermissionError as permission_error:
            show.error(f'{line_number()} {function_name} HASH GENERATION ERROR - PermissionError [{permission_error}]')
            time.sleep(1)
    return sha_file


def hash_comparison(first_file, second_file, hash_method):
    return get_hash(first_file, hash_method) == get_hash(second_file, hash_method)


def buffer_comparison(first_file, second_file):
    with open(first_file, READ_BINARY) as first_file_descriptor, open(second_file, READ_BINARY) as second_file_descriptor:
        while True:
            first_file_bytes = first_file_descriptor.read(BUFFER_SIZE)
            second_file_bytes = second_file_descriptor.read(BUFFER_SIZE)
            if first_file_bytes != second_file_bytes:
                return False
            if not first_file_bytes:
                return True


def compare_binaries(first_file, second_file):
    with open(first_file, READ_BINARY) as first_file_descriptor, open(second_file, READ_BINARY) as second_file_descriptor:
        for first_file_line, second_file_line in zip_longest(first_file_descriptor, second_file_descriptor, fillvalue=None):
            if first_file_line == second_file_line:
                continue
            else:
                return False
        return True


def file_equals(first_file, second_file, comparison_method):
    function_name = 'FILE EQUALS:'
    file_not_exist = False
    file_name1 = EMPTY
    file_name2 = EMPTY

    section_message = section_line(SYMBOL_MONEY, LINE_LEN)
    
    if not uri_exists(first_file):
        file_name1 = first_file
        file_not_exist = True
        
    if not uri_exists(second_file):
        file_name2 = second_file
        file_not_exist = True
    
    if file_not_exist:
        show.error(f'{line_number()} {section_message} {function_name} MISSING [{file_name1}] [{file_name2}] ')
        return False
        
    if os_stat(first_file).st_size != os_stat(second_file).st_size:
        return False

    if comparison_method == NATIVE:
        validation_string = {LINUX: EMPTY,
                             WINDOWS: FCB_EQUAL}
        comparison_command = {LINUX: f'{LINUX_CMP} "{second_file}" "{first_file}"',
                              WINDOWS: f'{WINDOWS_FC} "{first_file}" "{second_file}"'}

    elif comparison_method == EXECUTABLE:
        validation_string = {LINUX: EMPTY,
                             WINDOWS: EXE_EQUAL}
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
        section_message = section_line(SYMBOL_UNDERLINE, LINE_LEN)
        show.debug(f'{line_number()} {DEBUG_MARKER} {section_message}')
        show.info(f'{line_number()} {function_name} RUNNING [{command}]')
        process = run_command(command,
                              shell=True,
                              check=True,
                              stdout=PIPE,
                              stderr=STDOUT,
                              universal_newlines=True)
        return_value = process.stdout.split(NEW_LINE)
        if return_value[1] == validation_string[current_platform]:
            show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} [TRUE] [{return_value[1]}] == [{validation_string[current_platform]}] [{command}]')
            return True

    except CalledProcessError as called_process_error:
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} [FALSE] CalledProcessError RETURN CODE [{called_process_error.returncode}] [{called_process_error.cmd}]')

        output = str(called_process_error.output).strip()
        standard_output = str(called_process_error.stdout).strip()

        if not (output.startswith(COMPARE_TEXT) and standard_output.startswith(COMPARE_TEXT)):
            if output != standard_output:
                show.error(f'{line_number()} [{output}]')
            show.error(f'{line_number()} [{standard_output}]')

    return False


class DataBase:
    def __init__(self):
        show.debug(f'{line_number()} {DEBUG_MARKER} DATABASE: STARTED')
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
        self._file_allocation_table = pandas_dataframe.concat([pandas_dataframe.DataFrame([file_name]), self._file_allocation_table], ignore_index=True)
        # self._file_allocation_table = self._file_allocation_table._append(file_name, ignore_index=True)

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

    def changed_from_deleted_to_file(self, file_uri):
        self._file_allocation_table.loc[(self._file_allocation_table[URI] == file_uri) & (self._file_allocation_table[KIND] == DELETED_PARENT), KIND] = FILE

    def change_hash_file(self, file_uri, file_kind, file_hash):
        self._file_allocation_table.loc[(self._file_allocation_table[URI] == file_uri) & (self._file_allocation_table[KIND] == file_kind), SHA] = file_hash

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
        show.debug(f'{line_number()} {DEBUG_MARKER} FILE LIST: HANDLER STARTED')
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
        show.debug(f'{line_number()} {DEBUG_MARKER} FILE LIST: PRINT TABLE')
        return self.print_table()

    def start_thread(self):
        if not self.running_now:
            section_message = section_line(SYMBOL_GT, LINE_LEN)
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} START THREAD: TRAY NOT RUNNING - STARTING')
            self.running_now = True
            self.thread_start_time = time.time()
            self.jobs.put(PAUSE)
            self.running_thread = threading.Thread(name=THREAD_NAME, target=update_icon, args=(self.jobs,))
            self.running_thread.start()

    def update_thread_started_time(self):
        section_message = section_line(SYMBOL_EQ, LINE_LEN)
        show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} UPDATE TIME: CONTINUE THREAD')
        global thread_started
        thread_started = self.thread_start_time = time.time()
        self.jobs.put(CONTINUE)

    def terminate(self):
        section_message = section_line(SYMBOL_LT, LINE_LEN)
        show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} TERMINATE THREAD')
        self.jobs.put(TERMINATE)

    def pause_thread(self):
        self.time_is_now_less_started = time.time() - self.thread_start_time
        section_message = section_line(SYMBOL_PIPE, LINE_LEN)
        show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} PAUSE THREAD: TIME NOW IN SECOND [{self.time_is_now_less_started}]')
        if self.time_is_now_less_started > MAX_SECONDS:
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} PAUSE THREAD: SET ICON TO GREEN')
            system_tray_icon.icon = Image.open(ICON_DONE)
            self.jobs.put(PAUSE)
        else:
            show.debug(f'{line_number()} {DEBUG_MARKER} {section_message} PAUSE THREAD: SET ICON TO CHANGE LATER')
            self.jobs.put(RECHECK)

    def print_table(self):
        function_name = 'PRINT TABLE:'
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} STARTED')
        return_string = EMPTY
        for index, row in self._file_database.get_all_files_rows():
            return_string += f'FILE TABLE: [{row[KIND]}] [{row[SHA][0:SHA_SIZE]}] [{row[URI]}]{NEW_LINE}'
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} ENDED')
        return return_string

    def save_data(self, now=False):
        function_name = 'SAVE DATA:'
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} STARTED')

        if DEBUG_TEST:
            show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} OPEN FILE TABLE')
            with open(FILE_TABLE, WRITE, encoding=UTF8) as file_table_handler:
                save_data_index = self._file_database.get_file_list()
                if save_data_index is not None and not save_data_index.empty:
                    for value in save_data_index[URI].values:
                        if get_platform() == WINDOWS:
                            value = value.replace(UNIX_SLASH, DOS_SLASH)
                        show.info(f'{line_number()} ===> FILE_TABLE: [{value}]')
                        file_table_handler.write(value + NEW_LINE)
                    file_table_handler.flush()

            show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} OPEN LINK TABLE')
            with open(LINK_TABLE, WRITE, encoding=UTF8) as link_table_handler:

                save_data_index = self._file_database.get_link_list()
                if save_data_index is not None and not save_data_index.empty:
                    for value in save_data_index[URI].values:
                        if get_platform() == WINDOWS:
                            value = value.replace(UNIX_SLASH, DOS_SLASH)
                        show.info(f'{line_number()} ===> LINK_TABLE LINKED: [{value}]')
                        link_table_handler.write(value + NEW_LINE)
                    link_table_handler.flush()

                save_data_index = self._file_database.get_deleted_parents()
                if save_data_index is not None and not save_data_index.empty:
                    for value in save_data_index[URI].values:
                        if get_platform() == WINDOWS:
                            value = value.replace(UNIX_SLASH, DOS_SLASH)
                        show.info(f'{line_number()} ===> FILE_TABLE DELETED: [{value}]')
                        link_table_handler.write(value + NEW_LINE)
                    link_table_handler.flush()

        show.warning(f'SAVED DATA:')
        self.report_data()

        self._save_count += 1
        if self._save_count > MAX_FILES or now:
            show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} SAVE COUNT > [{MAX_FILES}]')
            show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} DUMPING DATA TABLE TO PICKLE FILE')
            self._file_database.save_database()
            self._save_count = 0
            if CLEAR_CACHE:
                clear_cache()

    def load_data(self):
        function_name = "LOAD DATA:"
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} STARTED')
        try:
            if uri_exists(DATA_TABLE):
                show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} LOADING DATABASE')
                self._file_database.load_database()

                show.warning(f'LOADED DATA:')
                self.report_data()
            else:
                show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} FIRST EXECUTION - NO DATABASE YET')

        except Exception as exception:
            show.info(f'{line_number()} {function_name} FILE NOT FOUND [{exception}]')

    @staticmethod
    def execute(target_link, source_file):
        function_name = 'CREATE LINK:'
        show.info(f'{line_number()} {function_name} TARGET [{target_link}] SOURCE [{source_file}]')
        return_value = None

        source_is_file = is_file(source_file)
        target_exists = uri_exists(target_link)
        if source_is_file and not target_exists:
            show.info(f'{line_number()} {function_name} TARGET FILE DOES NOT EXIST')
            dir_name_path = dir_name(abs_path(target_link))
            if not uri_exists(dir_name_path):
                show.info(f'{line_number()} {function_name} DIRECTORY DOES NOT EXIST [{dir_name_path}]')
                show.info(f'{line_number()} {function_name} MAKING DIRECTORY [{dir_name_path}]')
                make_dirs(dir_name_path)

            target_dos = DOS_DRIVE + target_link.replace(UNIX_SLASH, DOS_SLASH)
            source_dos = DOS_DRIVE + source_file.replace(UNIX_SLASH, DOS_SLASH)
            link_command = {LINUX: f'{LINUX_LINK} "{source_file}" "{target_link}"',
                            WINDOWS: f'{WINDOWS_LINK} "{target_dos}" "{source_dos}"'}
            command = link_command[get_platform()]

            try:
                show.warning(f'{line_number()} {function_name} RUNNING COMMAND [{command}]')
                process = run_command(command,
                                      shell=True,
                                      check=True,
                                      stdout=PIPE,
                                      universal_newlines=True)
                return_value = process.stdout
            except CalledProcessError as called_process_error:
                show.error(f'{line_number()} {function_name} ERROR - CalledProcessError {called_process_error}')
                show.error(f'{line_number()} {function_name} ERROR - SOURCE [{source_is_file}] TARGET [{target_exists}]')
        else:
            show.error(f'{line_number()} {function_name} ERROR - SOURCE [{source_is_file}] TARGET [{target_exists}]')

        show.info(f'{line_number()} {function_name} ENDED [{return_value}]')
        return return_value

    def new_row(self, new_file, kind):
        function_name = 'NEW ROW:'
        show.info(f'{line_number()} {function_name} [{kind}] [{new_file}]')
        new_row = {SHA: new_file.file_sha,
                   KIND: kind,
                   URI: new_file.file_uri}
        show.info(f'{line_number()} {function_name} ADD FILE TO DATABASE [{new_row[SHA][0:SHA_SIZE]}] [{new_row[KIND]}] [{new_row[URI]}]')
        self._file_database.add_file_to_database(new_row)

    @staticmethod
    def delete_row(row):
        function_name = 'DELETE ROW:'
        show.info(f'{line_number()} {function_name} [{row}]')
        try:
            show.info(f'{line_number()} {function_name} STARTED [{row}]')
            if not is_dir(row):
                if is_link(row):
                    show.info(f'{line_number()} {function_name} DELETE LINK [{row}]')
                    delete_link(row)
                else:
                    show.info(f'{line_number()} {function_name} DELETE FILE [{row}]')
                    delete_file(row)
            else:
                show.error(f'{line_number()} {function_name} CRITICAL ERROR - TRYING TO DELETE DIRECTORY [{row}]')
        except FileNotFoundError as file_not_found_error:
            show.error(f'{line_number()} {function_name} FileNotFoundError [{row}] [{file_not_found_error}]')

    def get_file_index(self, index_uri, kind):
        function_name = 'GET FILE INDEX:'
        show.info(f'{line_number()} {function_name} [{kind}] [{index_uri}]')
        if index_uri in self._file_database.get_all_uri_rows():
            index_file = self._file_database.get_file_index(index_uri, kind)
            if index_file is not None and not index_file.empty:
                show.info(f'{line_number()} {function_name} FOUND [{index_file[SHA].values[0][0:SHA_SIZE]}][{index_file[KIND].values[0]}][{index_file[URI].values[0]}]')
                return index_file
        show.info(f'{line_number()} {function_name} NO FILE FOUND')
        return None

    def get_file(self, get_uri, kind):
        function_name = 'GET FILE:'
        show.info(f'{line_number()} {function_name} [{kind}] [{get_uri}]')
        index_file = self.get_file_index(get_uri, kind)
        if index_file is not None and not index_file.empty:
            show.info(f'{line_number()} {function_name} FOUND [{index_file[URI].values[0]}]')
            return index_file[URI].values[0]
        show.info(f'{line_number()} {function_name} NO FILE FOUND')
        return None

    def get_files(self, key_index, kind, index_file):
        function_name = 'GET FILES:'
        show.info(f'{line_number()} {function_name} [{key_index[0:SHA_SIZE]}] [{kind}] [{index_file}]')
        if key_index in self._file_database.get_index_values(index_file):
            index_file = self._file_database.get_file_index_by_kind(index_file, key_index, kind)
            if index_file is not None and not index_file.empty:
                show.info(f'{line_number()} {function_name} FILES FOUND')
                return index_file[URI].values
        show.info(f'{line_number()} {function_name} NO FILE FOUND')
        return None

    def report_data(self):
        total = self._file_database.get_total_files_count()
        unique = self._file_database.get_unique_files_count()
        show.warning(f'TOTAL        FILES: {total}')
        show.warning(f'TOTAL DUPE   FILES: {total - unique}')
        show.warning(f'TOTAL UNIQUE FILES: {unique}')
        if DEBUG_LEVEL == LEVEL_INFO:
            section_message = section_line(SYMBOL_EQ, 100)
            show.info(f'{section_message}')
            for database_item in self._file_database.get_all_files_rows():
                show.info(f'[{database_item[1][KIND]}] [{database_item[1][SHA][0:SHA_SIZE]}] [{database_item[1][URI]}]')
            show.info(f'{section_message}')

    def update_junk(self):
        function_name = 'UPDATE JUNK:'
        show.info(f'{line_number()} {function_name} STARTED')
        index_file = self._file_database.get_deleted_list()
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                delete_index = self.get_file_index(uri_index, REMOVED)
                if delete_index is not None:
                    show.warning(f'{line_number()} {function_name} DROP JUNK [{uri_index}]')
                    self._file_database.delete_file_from_database(delete_index)
        show.info(f'{line_number()} {function_name} END')

    def update_files(self):
        function_name = 'UPDATE FILES:'
        show.info(f'{line_number()} {function_name} STARTED')
        index_file = self._file_database.get_file_list()
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                if not uri_exists(uri_index):
                    delete_index = self.get_file_index(uri_index, FILE)
                    delete_sha = delete_index[SHA].values[0]
                    if delete_index is not None:
                        show.warning(f'{line_number()} {function_name} DROP FILE [{uri_index}]')
                        self._file_database.delete_file_from_database(delete_index)
                        delete_index = self.get_files(delete_sha, SYMLINK, SHA)
                        if delete_index is not None:
                            for row in delete_index:
                                show.warning(f'{line_number()} {function_name} DELETE ROW [{row}]')
                                self.delete_row(row)
                        self._file_database.change_from_link_to_deleted(delete_sha)

        show.info(f'{line_number()} {function_name} END')

    def update_links(self):
        function_name = 'UPDATE LINKS:'
        show.info(f'{line_number()} {function_name} STARTED')
        index_file = self._file_database.get_link_list()
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                if not is_link(uri_index) and not uri_exists(uri_index):
                    delete_index = self.get_file_index(uri_index, SYMLINK)
                    if delete_index is not None:
                        show.warning(f'{line_number()} {function_name} DROP LINK [{uri_index}]')
                        self._file_database.delete_file_from_database(delete_index)
        show.info(f'{line_number()} {function_name} ENDED')

    def add_file(self, add_uri):
        function_name = 'ADD FILE:'
        add_uri = add_uri.replace(DOS_SLASH, UNIX_SLASH)
        self.update_thread_started_time()
        show.warning(f'{line_number()} {section_line(SYMBOL_EQ, LINE_LEN)}')
        show.warning(f'{line_number()} {function_name} [{add_uri}]')

        if is_link(add_uri):
            show.info(f'{line_number()} {function_name} IS LINK? [{add_uri}]')
            new_file = FileHolder(add_uri)
            show.info(f'{line_number()} {function_name} GET FILES [FILE] [SHA] [{new_file.file_sha[0:SHA_SIZE]}]')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is not None:
                show.info(f'{line_number()} {function_name} FOUND')
                show.info(f'{line_number()} {function_name} GET FILES [SYMLINK] [URI] [{new_file.file_uri}]')
                line = self.get_files(new_file.file_uri, SYMLINK, URI)
                if line is None:
                    show.info(f'{line_number()} {function_name} FOUND')
                    show.info(f'{line_number()} {function_name} NEW ROW [SYMLINK] [{new_file}]')
                    self.new_row(new_file, SYMLINK)
            self._file_database.change_from_moved_to_link(add_uri)

        elif is_dir(add_uri):
            show.warning(f'{line_number()} {function_name} IS DIRECTORY? [{add_uri}]')
            show.warning(f'{line_number()} {function_name} ENDED')

        elif is_file(add_uri):
            show.info(f'{line_number()} {function_name} IS FILE? [{add_uri}]')
            new_file = FileHolder(add_uri)
            show.info(f'{line_number()} {function_name} GET FILES [FILE] [SHA] [{new_file.file_sha[0:SHA_SIZE]}]')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is None:
                show.info(f'{line_number()} {function_name} NOT FOUND')

                file_without_parent = self.get_file(new_file.file_uri, DELETED_PARENT)
                show.info(f'{line_number()} {function_name} GET FILE INDEX - URI [{new_file.file_uri}] DELETED_PARENT <= [{file_without_parent}]')
                if file_without_parent:
                    show.info(f'{line_number()} {function_name} CHANGE FROM DELETED TO FILE [{new_file.file_uri}]')
                    self._file_database.changed_from_deleted_to_file(new_file.file_uri)
                    show.info(f'{line_number()} {function_name} CHANGE HASH FILE [{FILE}] [{new_file.file_sha[0:SHA_SIZE]}] [{new_file.file_uri}]')
                    self._file_database.change_hash_file(new_file.file_uri, FILE, new_file.file_sha)
                else:
                    show.info(f'{line_number()} {function_name} NEW ROW [FILE] [{new_file}]')
                    self.new_row(new_file, FILE)

                show.info(f'{line_number()} {function_name} GET FILES [SHA] [DELETED_PARENT] [{new_file.file_sha[0:SHA_SIZE]}]')
                line = self.get_files(new_file.file_sha, DELETED_PARENT, SHA)
                if line is not None:
                    show.info(f'{line_number()} {function_name} GET FILES - NO FILE FOUND')
                    for row in line:
                        show.info(f'{line_number()} {function_name} GET FILES - FILE FOUND [{row}] [{new_file.file_uri}]')
                        self.execute(row, new_file.file_uri)
                self._file_database.changed_from_deleted_to_link(new_file.file_sha)
            else:
                line = line[0]
                show.info(f'{line_number()} {function_name} GET FILE [SYMLINK] [{new_file.file_uri}]')
                line_uri = self.get_file(new_file.file_uri, SYMLINK)
                if line_uri is None:
                    show.info(f'{line_number()} {function_name} GET FILE - SYMLINK NOT FOUND')
                    show.info(f'{line_number()} {function_name} FILE COMPARISON [{COMPARISON_METHOD}] [{new_file.file_uri}] [{line}]')
                    if new_file.file_uri != line and file_equals(new_file.file_uri, line, COMPARISON_METHOD):
                        show.info(f'{line_number()} if {new_file.file_uri} != {line}')
                        show.info(f'{line_number()} {function_name} FILE COMPARISON [{COMPARISON_METHOD}] [{new_file.file_uri}] [{line}]')
                        show.info(f'{line_number()} {function_name} NEW ROW [SYMLINK] [{new_file}]')
                        self.new_row(new_file, kind=SYMLINK)
                        show.info(f'{line_number()} {function_name} NEW ROW [REMOVED] [{new_file}]')
                        self.new_row(new_file, kind=REMOVED)

                        show.info(f'{line_number()} {function_name} DELETE ROW [{new_file.file_uri}]')
                        self.delete_row(new_file.file_uri)
                        show.info(f'{line_number()} {function_name} CREATE LINK [{new_file.file_uri}] [{line}]')
                        self.execute(new_file.file_uri, line)
                else:
                    show.error(section_line(SYMBOL_EQ, 100))
                    show.error(f'{line_number()} {function_name} =====> ERROR: [{line_uri}] IT SHOULD NEVER HAPPENED! <=====')
                    show.error(section_line(SYMBOL_EQ, 100))

        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} SAVE DATA')
        self.save_data()
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} END')
        self.pause_thread()

    def mod_file(self, mod_uri):
        function_name = 'MODIFY FILE:'
        mod_uri = mod_uri.replace(DOS_SLASH, UNIX_SLASH)
        self.update_thread_started_time()
        show.warning(f'{line_number()} {section_line(SYMBOL_EQ, LINE_LEN)}')
        show.warning(f'{line_number()} {function_name} MODIFY FILE [{mod_uri}]')
        if is_link(mod_uri):
            show.warning(f'{line_number()} {function_name} IS FILE LINK? [{mod_uri}]')
            show.info(f'{line_number()} {function_name} DO NOTHING, YET')
        elif is_dir(mod_uri):
            show.warning(f'{line_number()} {function_name} IS FILE DIR? [{mod_uri}]')
            show.warning(f'{line_number()} {function_name} DO NOTHING, YET')
        elif is_file(mod_uri):
            show.info(f'{line_number()} {function_name} IS FILE? [{mod_uri}]')
            new_file = FileHolder(mod_uri)

            show.info(f'{line_number()} {function_name} GET FILES [SHA] [FILE] [{new_file.file_sha[0:SHA_SIZE]}]')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                show.info(f'{line_number()} {function_name} GET FILES [SHA] FOUND')
                if new_file.file_uri != gotten_by_sha[0]:
                    show.info(f'{line_number()} {function_name} FILE MOVED - NO HANDLING HERE')
                    show.info(f'{line_number()} {function_name} FILE UNCHANGED {new_file.file_uri}')
                    show.info(f'{line_number()} {function_name} URI    CHANGED {gotten_by_sha[0]}')

            show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] [{new_file.file_uri}]')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] - FOUND')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    show.info(f'{line_number()} COMPARISON IS DIFFERENT [{new_file.file_sha[0:SHA_SIZE]}] [{gotten_by_uri[SHA].values[0][0:SHA_SIZE]}]')
                    show.info(f'{line_number()} CONTENT CHANGED - MUST UPDATE LINKS WHEN APPLICABLE')
                    show.info(f'{line_number()} OLD: {gotten_by_uri[SHA].values[0][0:SHA_SIZE]}')
                    show.info(f'{line_number()} NEW: {new_file.file_sha[0:SHA_SIZE]}')
                    self._file_database.move_hash_location(gotten_by_uri[SHA].values[0], new_file.file_sha)

            if gotten_by_sha is None and gotten_by_uri is None:
                show.info(f'{line_number()} {function_name} GET FILES AND GET FILE INDEX DID NOT FIND RESOURCES')
                show.info(f'{line_number()} {function_name} ADD FILE [{mod_uri}]')
                self.add_file(mod_uri)

        show.info(f'{line_number()} {function_name} SAVE DATA')
        self.save_data()
        show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} ENDED')
        self.pause_thread()

    def move_file(self, source_file, target_file):
        function_name = 'MOVE FILE:'
        source_file = source_file.replace(DOS_SLASH, UNIX_SLASH)
        target_file = target_file.replace(DOS_SLASH, UNIX_SLASH)
        self.update_thread_started_time()
        section_message = section_line(SYMBOL_EQ, LINE_LEN)
        show.warning(f'{line_number()} {section_message}')
        show.warning(f'{line_number()} {function_name} MOVE FILE [{source_file}] [{target_file}]')

        if is_link(target_file):
            show.info(f'{line_number()} {function_name} IS LINK [{target_file}]')
            show.info(f'{line_number()} {function_name} MOVE FILE LOCATION [URI] [{source_file}] [{target_file}]')
            self._file_database.move_file_location(source_file, target_file)

        elif is_file(target_file):
            show.info(f'{line_number()} {function_name} IS FILE [{target_file}]')
            new_file = FileHolder(target_file)
            show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] [{new_file.file_uri}]')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                show.info(f'{line_number()} {function_name} GET FILE INDEX - NOT FOUND')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    show.info(f'{line_number()} {function_name} SHA DIFFERS [{new_file.file_sha[0:SHA_SIZE]}] [{gotten_by_uri[SHA].values[0][0:SHA_SIZE]}]')
                    # TODO: MUST CHECK IF NEW SHA ALREADY EXIST ON DATABASE
                    # IF NOT EXIST, GO AHEAD
                    # IF EXIST, MANAGE DUPE
                    show.info(f'{line_number()} {function_name} CHANGING SHA FROM [{gotten_by_uri[SHA].values[0][0:SHA_SIZE]}]')
                    show.info(f'{line_number()} {function_name} CHANGING SHA TO   [{new_file.file_sha[0:SHA_SIZE]}]')
                    self._file_database.move_hash_location(gotten_by_uri[SHA].values[0], new_file.file_sha)

            show.info(f'{line_number()} {function_name} GET FILES [SHA] [FILE] [{new_file.file_sha}]')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                show.info(f'{line_number()} {function_name} GET FILES - NOT FOUND')
                if new_file.file_uri != gotten_by_sha[0]:
                    show.info(f'{line_number()} {function_name} URI DIFFERS:')
                    show.info(f'{line_number()} {function_name} URI CHANGED FROM [{gotten_by_sha[0]}]')
                    show.info(f'{line_number()} {function_name} URI CHANGED TO   [{new_file.file_uri}]')
                    show.info(f'{line_number()} {function_name} CHANGING MAIN FILE ADDRESS BEFORE')
                    show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} {NEW_LINE}{self.print_table()}')
                    self._file_database.update_file_location(gotten_by_sha[0], new_file.file_uri)
                    show.info(f'{line_number()} {function_name} CHANGING MAIN FILE ADDRESS AFTER')
                    show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} {NEW_LINE}{self.print_table()}')
                    self._file_database.change_from_link_to_moved(new_file.file_sha)
                    show.info(f'{line_number()} {function_name} CHANGING LINK FILE ADDRESS')
                    show.debug(f'{line_number()} {DEBUG_MARKER} {function_name} {NEW_LINE}{self.print_table()}')

                    show.info(f'{line_number()} {function_name} GET FILES [SHA] [MOVED_FILE] [{new_file.file_sha[0:SHA_SIZE]}]')
                    line = self.get_files(new_file.file_sha, MOVED_FILE, SHA)
                    if line is not None:
                        show.info(f'{line_number()} {function_name} GET FILES - NOT FOUND')
                        for row in line:
                            show.info(f'{line_number()} {function_name} DELETE ROW [{row}]')
                            self.delete_row(row)
                            show.info(f'{line_number()} {function_name} CREATE LINK [{row}] [{new_file.file_uri}]')
                            self.execute(row, new_file.file_uri)

        show.info(f'{line_number()} {function_name} SAVE DATA')
        self.save_data()
        show.info(f'{line_number()} {function_name} ENDED')
        self.pause_thread()

    def del_file(self, del_uri):
        function_name = 'DELETE FILE:'
        del_uri = del_uri.replace(DOS_SLASH, UNIX_SLASH)
        self.update_thread_started_time()
        section_message = section_line(SYMBOL_EQ, LINE_LEN)
        show.warning(f'{line_number()} {section_message}')
        show.warning(f'{line_number()} {function_name} DELETE FILE [{del_uri})]')

        show.info(f'{line_number()} {function_name} GET FILE INDEX [REMOVED] [{del_uri}]')
        delete_index = self.get_file_index(del_uri, REMOVED)
        if delete_index is not None:
            show.info(f'{line_number()} {function_name} DELETE INDEX - NOT FOUND')
            show.info(f'{line_number()} {function_name} DROP [{delete_index.index}]')
            self._file_database.delete_file_from_database(delete_index)
            show.info(f'{line_number()} {function_name} SAVE DATA')
            self.save_data()
            show.info(f'{line_number()} {function_name} ENDED')
            return

        show.info(f'{line_number()} {function_name} DELETE INDEX - FOUND')
        show.info(f'{line_number()} {function_name} GET FILE INDEX [SYMLINK] [{del_uri}]')
        delete_index = self.get_file_index(del_uri, SYMLINK)
        if delete_index is not None:
            show.info(f'{line_number()} {function_name} DELETE INDEX FOUND')
            show.info(f'{line_number()} {function_name} DROP [{delete_index.index}]')
            self._file_database.delete_file_from_database(delete_index)
            show.info(f'{line_number()} {function_name} SAVE DATA')
            self.save_data()
            show.info(f'{line_number()} {function_name} ENDED')
            return

        show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] [{del_uri}]')
        delete_index = self.get_file_index(del_uri, FILE)
        if delete_index is not None:
            show.info(f'{line_number()} {function_name} GET FILE INDEX - FOUND')
            show.info(f'{line_number()} {function_name} DROP [{delete_index.index}]')
            self._file_database.delete_file_from_database(delete_index)
            sha = delete_index[SHA].values[0]
            show.info(f'{line_number()} {function_name} GET FILES [SHA] [SYMLINK] [{sha[0:SHA_SIZE]}]')
            delete_index = self.get_files(sha, SYMLINK, SHA)
            if delete_index is not None:
                show.info(f'{line_number()} {function_name} GET FILES [SHA] [SYMLINK] - FOUND')
                for row in delete_index:
                    show.warning(f'{line_number()} {function_name} DELETE ROW [{row}]')
                    self.delete_row(row)
                self._file_database.change_from_link_to_deleted(sha)

        show.info(f'{line_number()} {function_name} SAVE DATA')
        self.save_data()
        show.info(f'{line_number()} {function_name} ENDED')
        self.pause_thread()


class FileHolder:
    def __init__(self, file_path):
        show.info(f'{line_number()} FILE HOLDER: [{file_path}]')
        self._file_uri = file_path
        self._file_sha = None
        self.set_sha()

    @property
    def file_uri(self):
        show.info(f'{line_number()} FILE HOLDER: GET URI [{self._file_uri}]')
        return self._file_uri

    @file_uri.setter
    def file_uri(self, universal_resource_indicator):
        self._file_uri = universal_resource_indicator
        self.set_sha()
        show.info(f'{line_number()} FILE HOLDER: CREATE URI [{self._file_sha[0:SHA_SIZE]}] [{self._file_uri}]')

    @property
    def file_sha(self):
        show.info(f'{line_number()} FILE HOLDER: GET SHA [{self._file_sha[0:SHA_SIZE]}]')
        return self._file_sha

    def set_sha(self):
        show.info(f'{line_number()} FILE HOLDER: CREATING SHA')
        if self._file_uri:
            self._file_sha = get_hash(self._file_uri, HASH_SHA512)

    def __repr__(self):
        show.debug(f'{line_number()} {DEBUG_MARKER} FILE HOLDER REPORT')
        return self._file_uri


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        show.info(f'{line_number()} ON_CREATED')
        file_list.add_file(event.src_path)
        show.debug(f'{line_number()} {DEBUG_MARKER}  {file_list}')

    def on_modified(self, event):
        show.info(f'{line_number()} ON_MODIFIED')
        file_list.mod_file(event.src_path)
        show.debug(f'{line_number()} {DEBUG_MARKER}  {file_list}')

    def on_moved(self, event):
        show.info(f'{line_number()} ON_MOVED')
        file_list.move_file(event.src_path, event.dest_path)
        show.debug(f'{line_number()} {DEBUG_MARKER}  {file_list}')

    def on_deleted(self, event):
        show.info(f'{line_number()} ON_DELETED')
        file_list.del_file(event.src_path)
        show.debug(f'{line_number()} {DEBUG_MARKER}  {file_list}')


def tray_icon_click(_, selected_tray_item):
    global system_tray_icon
    show.debug(f'{line_number()} Tray Icon Click: [{str(selected_tray_item)}]')
    if str(selected_tray_item) == LABEL_DONE:
        system_tray_icon.icon = Image.open(ICON_DONE)
    if str(selected_tray_item) == LABEL_ERROR:
        system_tray_icon.icon = Image.open(ICON_ERROR)
    if str(selected_tray_item) == LABEL_PAUSE:
        system_tray_icon.icon = Image.open(ICON_PAUSE)
    if str(selected_tray_item) == LABEL_EXIT:
        file_list.save_data(True)
        file_list.terminate()
        show.warning(f'Terminating {LABEL_MAIN} System...')
        system_tray_icon.stop()
    show.warning(f'Tray Icon Done...')


if __name__ == "__main__":
    log_handler = [RotatingFileHandler(LOG_FILE, maxBytes=10000000, backupCount=20000, encoding=UTF8)]
    log_format = '%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s'
    log_formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    date_format = DATE_FORMAT

    logging.basicConfig(format=log_format,
                        datefmt=date_format,
                        level=DEBUG_LEVEL,
                        handlers=log_handler)

    console = logging.StreamHandler()
    console.setLevel(DEBUG_LEVEL)
    console.setFormatter(log_formatter)
    logging.getLogger().addHandler(console)

    logging.getLogger(WATCHDOG).setLevel(logging.CRITICAL)

    argument_parser = arg_parse.ArgumentParser()
    argument_parser.add_argument(PARAMETER_PATH, required=False, default=MAIN_PATH)
    argument_parser.add_argument(PARAMETER_SCAN, required=False, default=False)
    arguments = argument_parser.parse_args()

    event_source_path = arguments.path
    event_source_scan = arguments.scan

    state = False
    system_tray_image = Image.open(ICON_DONE)
    system_tray_icon = pystray.Icon(f'{LABEL_MAIN} 1',
                                    system_tray_image,
                                    LABEL_MAIN,
                                    menu=pystray.Menu(
                                        pystray.MenuItem(LABEL_DONE, tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem(LABEL_PAUSE, tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem(LABEL_ERROR, tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem(LABEL_EXIT, tray_icon_click, checked=lambda item: state)))

    show.warning(f'Starting {LABEL_MAIN} System...')
    file_list = FileList()
    file_list.start_thread()

    if event_source_scan:
        file_list.update_links()
        file_list.update_files()
        for root, dirs, files in os_walk(event_source_path, topdown=True):
            for name in files:
                uri = str(os_path.join(root, name))
                if uri_exists(uri):
                    show.info(f'{line_number()} SCAN: FILE LIST ADD FILE: [{uri}]')
                    file_list.add_file(uri)
        file_list.update_junk()
        file_list.pause_thread()

    show.warning(f'{LABEL_MAIN} Initialized...')
    file_list.report_data()

    event_handler = MonitorFolder()
    observer = Observer()
    observer.schedule(event_handler, path=event_source_path, recursive=True)

    observer.start()

    if UI == GUI:
        show.warning(f'{LABEL_MAIN} Tray Initialized...')
        system_tray_icon.run()

    if UI == CLI:
        show.warning(f'{section_line(SYMBOL_EQ, 20)} Keyboard Initialized... {section_line(SYMBOL_EQ, 20)}')
        keyboard_listening = True
        try:
            keyboard = KBHit()
            while keyboard_listening:
                time.sleep(1)
                if keyboard.check():
                    file_list.save_data(True)
                    show.warning(f'Terminating {LABEL_MAIN} System...')
                    keyboard_listening = False

        except KeyboardInterrupt as keyboard_interrupt:
            show.debug(f'{line_number()} {DEBUG_MARKER} KeyboardInterrupt: [{keyboard_interrupt}]')

    file_list.terminate()
    observer.stop()
    observer.join()

    show.warning(f'Bye...')
