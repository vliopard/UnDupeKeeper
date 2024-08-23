import os
import time
import ctypes
import hashlib
import inspect
import constants

from functools import wraps
from datetime import datetime, timedelta

from operator import itemgetter
from filecmp import clear_cache

from subprocess import PIPE
from subprocess import STDOUT

from tqdm import tqdm
from md5hash import scan
from os import stat as os_stat
from itertools import zip_longest
from os.path import islink as is_link
from filecmp import cmp as file_compare
from os.path import exists as uri_exists
from sys import platform as sys_platform
from subprocess import CalledProcessError
from subprocess import run as run_command

import logging
show = logging.getLogger(constants.DEBUG_UTIL)


def timed(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        time_start = time.time()
        result = func(*args, **kwargs)
        time_end = time.time()
        end_time = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        time_report = [f'Start time: {start_time}', f'End time:   {end_time}', f"Function {func.__name__} ran in {timedelta(seconds=(time_end - time_start))}"]
        print('=' * 100)
        for time_detail in time_report:
            print(time_detail)
        print('=' * 100)
        return result
    return wrapper


def section_line(style, size):
    return style * size


def line_number():
    return f'[{inspect.currentframe().f_back.f_lineno:04d}]'


def get_platform():
    function_name = 'GET PLATFORM:'
    show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} STARTED')
    platforms = {constants.PLATFORM_LINUX0: constants.LINUX,
                 constants.PLATFORM_LINUX1: constants.LINUX,
                 constants.PLATFORM_LINUX2: constants.LINUX,
                 constants.PLATFORM_DARWIN: constants.OS_X,
                 constants.PLATFORM_WIN32: constants.WINDOWS}
    if sys_platform not in platforms:
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} UNDESIRED END')
        show.info(f'{line_number()} {function_name} RETURN [{sys_platform}]')
        return sys_platform
    show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} NORMAL END')
    return platforms[sys_platform]


@timed
def scan_files(current_path):
    set_of_files = set()
    for entry in os.scandir(current_path):
        if entry.is_file():
            set_of_files.add(os.path.normpath(entry.path))
        elif entry.is_dir():
            set_of_files.update(scan_files(entry.path))
    return set_of_files


@timed
def walk_files(current_path):
    set_of_files = set()
    for root, dirs, files in tqdm(os.walk(current_path), desc='SCANNING'):
        for file in files:
            set_of_files.add(os.path.normpath(os.path.join(root, file)))
    return set_of_files


def create_random_binary_file(file_path, size):
    with open(file_path, constants.WRITE) as random_file:
        random_bytes = bytearray(os.urandom(size))
        random_file.write(random_bytes)


def create_checksum_fixed_chunks(file_name):
    hash_digest = hashlib.md5()
    with open(file_name, constants.READ_BINARY) as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_digest.update(chunk)
    return hash_digest.hexdigest()


def get_hash(uri_file, digest):
    function_name = 'GET HASH:'
    sha_file = None
    if digest == constants.HASH_MD5_FAST:
        digest_method = constants.HASH_MD5_FAST
    elif digest == constants.HASH_MD5_CHUNK:
        digest_method = constants.HASH_MD5_CHUNK
    elif digest == constants.HASH_SHA:
        digest_method = hashlib.sha1()
    elif digest == constants.HASH_SHA256:
        digest_method = hashlib.sha256()
    elif digest == constants.HASH_SHA512:
        digest_method = hashlib.sha512()
    else:
        digest_method = hashlib.md5()
    memory_view = memoryview(bytearray(128 * 1024))

    trials = 0

    while sha_file is None:
        try:

            trials += 1
            if trials > 5:
                return sha_file

            if is_link(uri_file):
                uri_file = os.readlink(uri_file)
            if digest_method == constants.HASH_MD5_FAST:
                sha_file = scan(uri_file)
            elif digest_method == constants.HASH_MD5_CHUNK:
                sha_file = create_checksum_fixed_chunks(uri_file)
            else:
                with open(uri_file, constants.READ_BINARY, buffering=0) as uri_locator:
                    for element in iter(lambda: uri_locator.readinto(memory_view), 0):
                        digest_method.update(memory_view[:element])
                sha_file = digest_method.hexdigest()
        except PermissionError as permission_error:
            show.error(f'{line_number()} {function_name} HASH GENERATION ERROR - PermissionError [{permission_error}]')
        except FileNotFoundError as file_not_found_error:
            show.error(f'{line_number()} {function_name} HASH GENERATION ERROR - FileNotFoundError [{file_not_found_error}]')
        except Exception as exception:
            show.error(f'{line_number()} {function_name} HASH GENERATION ERROR - Exception [{exception}]')

        finally:
            if trials > 1:
                time.sleep(0.25)

    return sha_file


def hash_comparison(first_file, second_file, hash_method):
    return get_hash(first_file, hash_method) == get_hash(second_file, hash_method)


def buffer_comparison(first_file, second_file):
    with open(first_file, constants.READ_BINARY) as first_file_descriptor, open(second_file, constants.READ_BINARY) as second_file_descriptor:
        while True:
            first_file_bytes = first_file_descriptor.read(constants.BUFFER_SIZE)
            second_file_bytes = second_file_descriptor.read(constants.BUFFER_SIZE)
            if first_file_bytes != second_file_bytes:
                return False
            if not first_file_bytes:
                return True


def compare_binaries(first_file, second_file):
    with open(first_file, constants.READ_BINARY) as first_file_descriptor, open(second_file, constants.READ_BINARY) as second_file_descriptor:
        for first_file_line, second_file_line in zip_longest(first_file_descriptor, second_file_descriptor, fillvalue=None):
            if first_file_line == second_file_line:
                continue
            else:
                return False
        return True


def file_equals(first_file, second_file, comparison_method):
    function_name = 'FILE EQUALS:'
    file_not_exist = False
    file_name1 = constants.EMPTY
    file_name2 = constants.EMPTY

    section_message = section_line(constants.SYMBOL_MONEY, constants.LINE_LEN)

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

    if comparison_method == constants.NATIVE:
        validation_string = {constants.LINUX: constants.EMPTY,
                             constants.WINDOWS: constants.FCB_EQUAL}
        comparison_command = {constants.LINUX: f'{constants.LINUX_CMP} "{second_file}" "{first_file}"',
                              constants.WINDOWS: f'{constants.WINDOWS_FC} "{first_file}" "{second_file}"'}

    elif comparison_method == constants.EXECUTABLE:
        validation_string = {constants.LINUX: constants.EMPTY,
                             constants.WINDOWS: constants.EXE_EQUAL}
        comparison_command = {constants.LINUX: f'{constants.LINUX_DIFF} "{second_file}" "{first_file}"',
                              constants.WINDOWS: f'{constants.WINDOWS_COMP} "{first_file}" "{second_file}"'}

    elif comparison_method == constants.ZIP_LONGEST:
        return compare_binaries(first_file, second_file)

    elif comparison_method == constants.BUFFER:
        return buffer_comparison(first_file, second_file)

    elif comparison_method == constants.HASH_MD5:
        return hash_comparison(first_file, second_file, constants.HASH_MD5)

    elif comparison_method == constants.HASH_MD5_FAST:
        return hash_comparison(first_file, second_file, constants.HASH_MD5_FAST)

    elif comparison_method == constants.HASH_MD5_CHUNK:
        return hash_comparison(first_file, second_file, constants.HASH_MD5_CHUNK)

    elif comparison_method == constants.HASH_SHA:
        return hash_comparison(first_file, second_file, constants.HASH_SHA)

    elif comparison_method == constants.HASH_SHA256:
        return hash_comparison(first_file, second_file, constants.HASH_SHA256)

    elif comparison_method == constants.HASH_SHA512:
        return hash_comparison(first_file, second_file, constants.HASH_SHA512)

    else:
        return file_compare(first_file, second_file, shallow=False)

    current_platform = get_platform()
    command = comparison_command[current_platform]

    try:
        section_message = section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message}')
        show.info(f'{line_number()} {function_name} RUNNING [{command}]')
        process = run_command(command,
                              shell=True,
                              check=True,
                              stdout=PIPE,
                              stderr=STDOUT,
                              universal_newlines=True)
        return_value = process.stdout.split(constants.NEW_LINE)
        if return_value[1] == validation_string[current_platform]:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} [TRUE] [{return_value[1]}] == [{validation_string[current_platform]}] [{command}]')
            return True

    except CalledProcessError as called_process_error:
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} [FALSE] CalledProcessError RETURN CODE [{called_process_error.returncode}] [{called_process_error.cmd}]')

        output = str(called_process_error.output).strip()
        standard_output = str(called_process_error.stdout).strip()

        if not (output.startswith(constants.COMPARE_TEXT) and standard_output.startswith(constants.COMPARE_TEXT)):
            if output != standard_output:
                show.error(f'{line_number()} [{output}]')
            show.error(f'{line_number()} [{standard_output}]')

    return False


def comparison_test(trials_local, excel_worksheet_local, first_file, second_file, category):
    results = []
    for trial in trials_local:
        clear_cache()
        start_time = time.time()
        answer = file_equals(first_file, second_file, trial)
        results.append([str(timedelta(seconds=time.time() - start_time)), category, trial, answer])
    report = sorted(results, key=itemgetter(0))
    for trial in report:
        print(f"{trial[0]}  {trial[1].split(':')[0]}  {trial[1].split(':')[1]}  {trial[3]}  {trial[2]}")
        excel_worksheet_local.append(tuple([trial[0], trial[1].split(':')[0], int(trial[1].split(':')[1].replace(',', '')), trial[3], trial[2]]))
    print(constants.NEW_LINE)


def scan_directory_count(working_directory):
    print(f'SCANNING [{working_directory}]...')
    total_files = 0
    for root, dirs, files in tqdm(os.walk(working_directory), desc="SCANNING"):
        total_files += len(files)
    print(f'TOTAL FILES: [{total_files:,}]')


def get_level(path, n):
    path, _ = os.path.split(path)
    is_windows = constants.DOS_SLASH in path
    normalized_path = os.path.normpath(path)
    directories = normalized_path.split(os.sep)

    if os.path.isabs(path):
        result = os.sep + os.sep.join(directories[1:n + 1])
    else:
        result = os.sep.join(directories[:n])

    if is_windows:
        result = result.replace(constants.UNIX_SLASH, constants.DOS_SLASH)
    else:
        result = result.replace(constants.DOS_SLASH, constants.UNIX_SLASH)

    return result


def change_dir_time(target_directory):
    try:
        time_accessed = os.path.getatime(min((os.path.join(target_directory, file) for file in os.listdir(target_directory)), key=os.path.getatime))
        time_modified = os.path.getmtime(min((os.path.join(target_directory, file) for file in os.listdir(target_directory)), key=os.path.getmtime))
        time_created = os.path.getctime(min((os.path.join(target_directory, file) for file in os.listdir(target_directory)), key=os.path.getctime))
        min_time = min(time_accessed, time_modified, time_created)

        # os.utime(target_directory, (min_time, min_time))
        change_time(target_directory, min_time, min_time, min_time)
    except ValueError:
        pass


@timed
def count_directories(target_directory):
    total_files = 0
    for root, dirs, files in tqdm(os.walk(target_directory), desc="SCANNING"):
        total_files += len(dirs)
    return total_files


@timed
def count_files(directory):
    return sum(len(files) for _, _, files in os.walk(directory))


def copy_time(source_name, target_name):
    change_time(target_name, os.path.getctime(source_name), os.path.getatime(source_name), os.path.getmtime(source_name))


def change_time(target_name, time_created, time_accessed, time_modified):
    iet = 1e7
    iiga = 116444736000000000

    set_file_time = ctypes.windll.kernel32.SetFileTime
    handle = ctypes.windll.kernel32.CreateFileW(
        target_name,
        256,
        0,
        None,
        3,
        0x02000000,  # 128,
        None)

    creation_time = ctypes.c_int64(int(time_created * iet + iiga))
    access_time = ctypes.c_int64(int(time_accessed * iet + iiga))
    modify_time = ctypes.c_int64(int(time_modified * iet + iiga))

    set_file_time(handle, ctypes.byref(creation_time), ctypes.byref(access_time), ctypes.byref(modify_time))
    ctypes.windll.kernel32.CloseHandle(handle)
