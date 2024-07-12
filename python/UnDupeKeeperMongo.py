import os
import time
import pystray
import threading
import constants

from methods import get_hash
from methods import line_number
from methods import file_equals
from methods import section_line
from methods import get_platform

import argparse as arg_parse
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError

from PIL import Image
from queue import Queue

from sys import stdin as sys_standard_in

from os import name as os_name
from os import path as os_path
from os import walk as os_walk

from os import unlink as delete_link
from os import remove as delete_file
from os import makedirs as make_dirs

from os.path import isdir as is_dir
from os.path import islink as is_link
from os.path import isfile as is_file
from os.path import abspath as abs_path
from os.path import dirname as dir_name
from os.path import exists as uri_exists

from subprocess import PIPE
from subprocess import CalledProcessError
from subprocess import run as run_command

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

from logging.handlers import RotatingFileHandler

import logging
show = logging.getLogger(constants.DEBUG_CORE)

# DEBUG=10
# INFO=20
# WARN=30
# ERROR=40
# CRITICAL=50


thread_started = time.time()
system_tray_icon = None


if os_name == constants.WINDOWS_NT:
    import msvcrt
else:
    import termios
    import atexit
    from select import select


def upsert(result, document_id):
    if result.upserted_id is not None:
        print(f'[{document_id}] INSERTED.')
    elif result.modified_count > 0:
        print(f'[{document_id}] UPDATED.')
    else:
        print(f'[{document_id}] ERROR.')


class KBHit:

    def __init__(self):
        if os_name == constants.WINDOWS_NT:
            pass
        else:
            self.fd = sys_standard_in.fileno()
            self.new_term = termios.tcgetattr(self.fd)
            self.old_term = termios.tcgetattr(self.fd)
            self.new_term[3] = (self.new_term[3] & ~termios.ICANON & ~termios.ECHO)
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.new_term)
            atexit.register(self.set_normal_term)

    def set_normal_term(self):
        if os_name == constants.WINDOWS_NT:
            pass
        else:
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.old_term)

    @staticmethod
    def get_character():
        if os_name == constants.WINDOWS_NT:
            return msvcrt.getch().decode(constants.UTF8)
        else:
            return sys_standard_in.read(1)

    @staticmethod
    def get_arrow_key():
        if os_name == constants.WINDOWS_NT:
            msvcrt.getch()
            c = msvcrt.getch()
            arrows = [72, 77, 80, 75]
        else:
            c = sys_standard_in.read(3)[2]
            arrows = [65, 67, 66, 68]
        return arrows.index(ord(c.decode(constants.UTF8)))

    @staticmethod
    def keyboard_hit():
        if os_name == constants.WINDOWS_NT:
            return msvcrt.kbhit()
        else:
            dr, dw, de = select([sys_standard_in], [], [], 0)
            return dr != []

    def check(self):
        if self.keyboard_hit():
            c = self.get_character()
            if ord(c) == constants.SLASH:
                self.set_normal_term()
                return True
        return False


def update_icon(task_event):
    global thread_started
    global system_tray_icon

    function_name = 'UPDATE ICON:'
    section_message = section_line(constants.SYMBOL_PERCENT, constants.LINE_LEN)

    paused = False
    check_out = False
    tray_icon_thread_running = True
    icons = [constants.MOVE_ICO1, constants.MOVE_ICO2, constants.MOVE_ICO3, constants.MOVE_ICO4]

    show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} STARTED')

    loop_interaction = 0
    while tray_icon_thread_running:
        time.sleep(1)
        if not task_event.empty():
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} CHECKING QUEUE - SIZE [{task_event.qsize()}]')
            value = task_event.get()
            if value == constants.PAUSE:
                show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} THREAD PAUSED')
                system_tray_icon.icon = Image.open(constants.ICON_DONE)
                paused = True
            if value == constants.CONTINUE:
                show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} THREAD CONTINUE')
                paused = False
            if value == constants.TERMINATE:
                show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} THREAD TERMINATE')
                tray_icon_thread_running = False
            if value == constants.RECHECK:
                show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} THREAD RECHECK')
                check_out = True

            task_event.task_done()
        else:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} JOB LIST IS EMPTY')

        time_time = time.time()
        check_time_now = time_time - thread_started
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} CHECK TIME [{check_time_now}] = [{time_time}] - [{thread_started}]')
        if check_out and check_time_now > constants.MAX_SECONDS:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} THREAD CHECKED = ICON DONE')
            system_tray_icon.icon = Image.open(constants.ICON_DONE)
            paused = True
            check_out = False

            with task_event.mutex:
                task_event.queue.clear()

        if not paused and tray_icon_thread_running:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} {function_name} SET ICON [{loop_interaction}][{icons[loop_interaction]}]')
            system_tray_icon.icon = Image.open(icons[loop_interaction])
            loop_interaction += 1
            if loop_interaction == 3:
                loop_interaction = 0


class DataBase:
    def __init__(self):
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} DATABASE: STARTED')

        self.mongo_client = MongoClient('mongodb://localhost:27017/')
        self.mongo_database = self.mongo_client[constants.DATABASE_NAME]
        self.mongo_collection = self.mongo_database['UnDupyKeeperFiles']

    def get_list(self, list_type):
        element_list = []
        results = self.mongo_collection.find({list_type: {"$exists": True}})
        for element in results:
            element_list += element[list_type]
        return element_list

    def add_file_to_database(self, new_row):
        for file_sha in new_row:
            element = self.mongo_collection.find_one({constants.DOC_ID: file_sha})
            if element:
                for item in new_row[file_sha]:
                    if new_row[file_sha][item][0] not in element[item]:
                        element[item].append(new_row[file_sha][item][0])
                        result = self.mongo_collection.update_one({constants.DOC_ID: file_sha}, {'$set': {item: element[item]}}, upsert=True)
                        upsert(result, file_sha)
                    else:
                        print(f'ERROR: [{file_sha}][{new_row[file_sha][item][0]}] already exists.')
            else:
                try:
                    for item in new_row[file_sha]:
                        self.mongo_collection.insert_one({constants.DOC_ID: file_sha, 'file_size': os.path.getsize(new_row[file_sha][item][0]), constants.FILE: new_row[file_sha][item]})
                except DuplicateKeyError as duplicate_key_error:
                    print(f'[{duplicate_key_error}]')
                break

    def insert(self, element):
        self.mongo_collection.insert_one({constants.DOC_ID: element[constants.DOC_ID], 'file_size': os.path.getsize(element[constants.FILE][0]), constants.FILE: element[constants.FILE]})

    def delete(self, file_uri, source):
        element = self.mongo_collection.find_one({source: {'$elemMatch': {'$eq': file_uri}}})
        if element:
            if file_uri in element[source]:
                element[source].remove(file_uri)
                result = self.mongo_collection.update_one({constants.DOC_ID: element[constants.DOC_ID]}, {'$set': {source: element[source]}}, upsert=True)
                upsert(result, element[constants.DOC_ID])
            else:
                print(f'ERROR: [{file_uri}] not in list.')
        else:
            print(f'ERROR: [{file_uri}] not found.')

    def get_all_files_rows(self):
        return_list = []
        cursor = self.mongo_collection.find({})
        elements = [constants.REMOVED, constants.SYMLINK, constants.DELETED_PARENT, constants.FILE, constants.MOVED_FILE]
        for document in cursor:
            for elem in document:
                if elem in elements:
                    for item in document[elem]:
                        return_list.append(f'[{document[constants.DOC_ID]}] [{elem}] [{item}]')
        return return_list

    def move_file(self, from_source, to_target):
        actions = [constants.REMOVED, constants.SYMLINK, constants.DELETED_PARENT, constants.FILE, constants.MOVED_FILE]
        query = {'$or': [{action: {'$elemMatch': {'$eq': from_source}}} for action in actions]}
        result = self.mongo_collection.find(query)
        for element in result:
            for source, value in element.items():
                if isinstance(value, list) and from_source in value:
                    element[source].remove(from_source)
                    element[source].append(to_target)
                    result = self.mongo_collection.update_one({constants.DOC_ID: element[constants.DOC_ID]}, {'$set': {source: element[source]}}, upsert=True)
                    upsert(result, element[constants.DOC_ID])
                    break

    def delete_file_from_database(self, file_id):
        actions = [constants.REMOVED, constants.SYMLINK, constants.DELETED_PARENT, constants.FILE, constants.MOVED_FILE]
        query = {'$or': [{action: {'$elemMatch': {'$eq': file_id}}} for action in actions]}
        elements = self.mongo_collection.find(query)
        for elem in elements:
            source = None
            for key, value in elem.items():
                if isinstance(value, list) and file_id in value:
                    source = key
            if source:
                elem[source].remove(file_id)
                result = self.mongo_collection.update_one({constants.DOC_ID: elem[constants.DOC_ID]}, {'$set': {source: elem[source]}}, upsert=True)
                upsert(result, elem[constants.DOC_ID])
            else:
                print(f'ERROR [{file_id}] NOT FOUND')

    def get_total_files_count(self):
        actions = [constants.REMOVED, constants.SYMLINK, constants.DELETED_PARENT, constants.FILE, constants.MOVED_FILE]
        pipeline = [{"$project": {action: {"$size": {"$ifNull": [f"${action}", []]}} for action in actions}},
                    {"$project": {"totalSize": {"$sum": [f"${action}" for action in actions]}}}, {"$group": {constants.DOC_ID: None, "totalCount": {"$sum": "$totalSize"}}}]
        documents = self.mongo_collection.aggregate(pipeline)
        for return_value in documents:
            return return_value['totalCount']

    def get_unique_files_count(self):
        return self.mongo_collection.count_documents({})

    def is_file_with_sha(self, sha):
        cursor = self.mongo_collection.find({constants.DOC_ID: sha, constants.FILE: {'$size': 1}})
        for x in cursor:
            return x
        return None

    def is_element_with_sha(self, sha, elem):
        elements = []
        cursor = self.mongo_collection.find({constants.DOC_ID: sha, elem: {'$gt': 0}})
        for x in cursor:
            elements.append(x)
        return elements

    def is_file_with(self, file_uri, element):
        return self.mongo_collection.find_one({element: {'$elemMatch': {'$eq': file_uri}}})

    def change_uri_from_to(self, file_uri, source, target):
        element = self.mongo_collection.find_one({source: {'$elemMatch': {'$eq': file_uri}}})
        if element:
            element = dict(element)
            if file_uri in element[source]:
                element[source].remove(file_uri)
                result = self.mongo_collection.update_one({constants.DOC_ID: element[constants.DOC_ID]}, {'$set': {source: element[source]}}, upsert=True)
                upsert(result, element[constants.DOC_ID])
            if target in element:
                if file_uri not in element[target]:
                    element[target].append(file_uri)
                else:
                    print(f'[{file_uri}] is already on links.')
            else:
                element[target] = [file_uri]
            result = self.mongo_collection.update_one({constants.DOC_ID: element[constants.DOC_ID]}, {'$set': {target: element[target]}}, upsert=True)
            upsert(result, element[constants.DOC_ID])
        else:
            print(f'[{file_uri}] not found.')

    def change_sha_from_to(self, file_sha, source, target):
        element = self.mongo_collection.find_one({constants.DOC_ID: file_sha})
        if element:
            element = dict(element)
            element_move = []
            if source in element:
                element_move = element[source]
                element[source] = []
                result = self.mongo_collection.update_one({constants.DOC_ID: element[constants.DOC_ID]}, {'$set': {source: element[source]}}, upsert=True)
                upsert(result, element[constants.DOC_ID])
            if target in element:
                element[target] = element[target] + element_move
            else:
                element[target] = element_move
                print(f'[{file_sha}] is already on links.')
            result = self.mongo_collection.update_one({constants.DOC_ID: element[constants.DOC_ID]}, {'$set': {target: element[target]}}, upsert=True)
            upsert(result, element[constants.DOC_ID])
        else:
            print(f'[{file_sha}] not found.')

    def change_hash_file(self, old_id, new_id):
        result = self.mongo_collection.find({constants.DOC_ID: old_id})
        for element in result:
            element[constants.DOC_ID] = new_id
            self.mongo_collection.insert_one(element)
            break
        self.mongo_collection.delete_one({constants.DOC_ID: old_id})


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

    def __repr__(self):
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} FILE LIST: PRINT TABLE')
        return self.print_table()

    def start_thread(self):
        if not self.running_now:
            section_message = section_line(constants.SYMBOL_GT, constants.LINE_LEN)
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} START THREAD: TRAY NOT RUNNING - STARTING')
            self.running_now = True
            self.thread_start_time = time.time()
            self.jobs.put(constants.PAUSE)
            self.running_thread = threading.Thread(name=constants.THREAD_NAME, target=update_icon, args=(self.jobs,))
            self.running_thread.start()

    def update_thread_started_time(self):
        section_message = section_line(constants.SYMBOL_EQ, constants.LINE_LEN)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} UPDATE TIME: CONTINUE THREAD')
        global thread_started
        thread_started = self.thread_start_time = time.time()
        self.jobs.put(constants.CONTINUE)

    def terminate(self):
        section_message = section_line(constants.SYMBOL_LT, constants.LINE_LEN)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} TERMINATE THREAD')
        self.jobs.put(constants.TERMINATE)

    def pause_thread(self):
        self.time_is_now_less_started = time.time() - self.thread_start_time
        section_message = section_line(constants.SYMBOL_PIPE, constants.LINE_LEN)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} PAUSE THREAD: TIME NOW IN SECOND [{self.time_is_now_less_started}]')
        if self.time_is_now_less_started > constants.MAX_SECONDS:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} PAUSE THREAD: SET ICON TO GREEN')
            system_tray_icon.icon = Image.open(constants.ICON_DONE)
            self.jobs.put(constants.PAUSE)
        else:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {section_message} PAUSE THREAD: SET ICON TO CHANGE LATER')
            self.jobs.put(constants.RECHECK)

    def print_table(self):
        function_name = 'PRINT TABLE:'
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} STARTED')
        return_string = constants.EMPTY
        for row in self._file_database.get_all_files_rows():
            return_string += f'FILE TABLE: {row.replace("][","] [")}{constants.NEW_LINE}'
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} ENDED')
        return return_string

    def save_data(self):
        function_name = 'SAVE DATA:'
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} STARTED')

        if constants.DEBUG_TEST:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} OPEN FILE TABLE')
            with open(constants.FILE_TABLE, constants.WRITE, encoding=constants.UTF8) as file_table_handler:
                save_data_index = self._file_database.get_list(constants.FILE)
                for value in save_data_index:
                    if get_platform() == constants.WINDOWS:
                        value = value.replace(constants.UNIX_SLASH, constants.DOS_SLASH)
                    show.info(f'{line_number()} ===> FILE_TABLE: [{value}]')
                    file_table_handler.write(value + constants.NEW_LINE)
                file_table_handler.flush()

            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} OPEN LINK TABLE')
            with open(constants.LINK_TABLE, constants.WRITE, encoding=constants.UTF8) as link_table_handler:
                save_data_index = self._file_database.get_list(constants.SYMLINK)
                for value in save_data_index:
                    if get_platform() == constants.WINDOWS:
                        value = value.replace(constants.UNIX_SLASH, constants.DOS_SLASH)
                    show.info(f'{line_number()} ===> LINK_TABLE LINKED: [{value}]')
                    link_table_handler.write(value + constants.NEW_LINE)
                link_table_handler.flush()

                save_data_index = self._file_database.get_list(constants.DELETED_PARENT)
                for value in save_data_index:
                    if get_platform() == constants.WINDOWS:
                        value = value.replace(constants.UNIX_SLASH, constants.DOS_SLASH)
                    show.info(f'{line_number()} ===> FILE_TABLE DELETED: [{value}]')
                    link_table_handler.write(value + constants.NEW_LINE)
                link_table_handler.flush()

        show.warning(f'SAVED DATA:')
        self.report_data()

    def load_data(self):
        function_name = "LOAD DATA:"
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} STARTED')
        try:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} LOADING DATABASE')
            show.warning(f'LOADED DATA:')
            self.report_data()
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

            target_dos = constants.DOS_DRIVE + target_link.replace(constants.UNIX_SLASH, constants.DOS_SLASH)
            source_dos = constants.DOS_DRIVE + source_file.replace(constants.UNIX_SLASH, constants.DOS_SLASH)
            link_command = {constants.LINUX: f'{constants.LINUX_LINK} "{source_file}" "{target_link}"',
                            constants.WINDOWS: f'{constants.WINDOWS_LINK} "{target_dos}" "{source_dos}"'}
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
        show.info(f'{line_number()} {function_name} ADD FILE TO DATABASE [{new_file.file_sha[0:constants.SHA_SIZE]}] [{kind}] [{new_file.file_uri}]')
        self._file_database.add_file_to_database({new_file.file_sha: {kind: [new_file.file_uri]}})

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

    def report_data(self):
        total = self._file_database.get_total_files_count()
        unique = self._file_database.get_unique_files_count()
        show.warning(f'TOTAL        FILES: {total}')
        show.warning(f'TOTAL DUPE   FILES: {total - unique}')
        show.warning(f'TOTAL UNIQUE FILES: {unique}')
        if constants.DEBUG_LEVEL == constants.LEVEL_INFO:
            section_message = section_line(constants.SYMBOL_EQ, 100)
            show.info(f'{section_message}')
            for database_item in self._file_database.get_all_files_rows():
                show.info(f'{database_item.replace("][","] [")}')
            show.info(f'{section_message}')

    def update_junk(self):
        function_name = 'UPDATE JUNK:'
        show.info(f'{line_number()} {function_name} STARTED')
        index_file = self._file_database.get_list(constants.REMOVED)
        for uri_index in index_file:
            delete_index = self._file_database.is_file_with(uri_index, constants.REMOVED)
            if delete_index:
                show.warning(f'{line_number()} {function_name} DROP JUNK [{uri_index}]')
                self._file_database.delete_file_from_database(delete_index)
        show.info(f'{line_number()} {function_name} END')

    def update_files(self):
        function_name = 'UPDATE FILES:'
        show.info(f'{line_number()} {function_name} STARTED')
        index_file = self._file_database.get_list(constants.FILE)
        for uri_index in index_file:
            if not uri_exists(uri_index):
                delete_index = self._file_database.is_file_with(uri_index, constants.FILE)
                if delete_index:
                    delete_sha = delete_index[constants.DOC_ID]
                    show.warning(f'{line_number()} {function_name} DROP FILE [{uri_index}]')
                    self._file_database.delete_file_from_database(delete_index)
                    delete_index = self._file_database.is_element_with_sha(delete_sha, constants.SYMLINK)
                    for row in delete_index:
                        show.warning(f'{line_number()} {function_name} DELETE ROW [{row}]')
                        self.delete_row(row)
                    self._file_database.change_sha_from_to(delete_sha, constants.SYMLINK, constants.DELETED_PARENT)
        show.info(f'{line_number()} {function_name} END')

    def update_links(self):
        function_name = 'UPDATE LINKS:'
        show.info(f'{line_number()} {function_name} STARTED')
        index_file = self._file_database.get_list(constants.SYMLINK)
        for uri_index in index_file:
            if not is_link(uri_index) and not uri_exists(uri_index):
                delete_index = self._file_database.is_file_with(uri_index, constants.SYMLINK)
                if delete_index:
                    show.warning(f'{line_number()} {function_name} DROP LINK [{uri_index}]')
                    self._file_database.delete_file_from_database(delete_index)
        show.info(f'{line_number()} {function_name} ENDED')

    def add_file(self, add_uri):
        function_name = 'ADD FILE:'
        add_uri = add_uri.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        self.update_thread_started_time()
        show.warning(f'{line_number()} {section_line(constants.SYMBOL_EQ, constants.LINE_LEN)}')
        show.warning(f'{line_number()} {function_name} [{add_uri}]')

        if is_link(add_uri):
            show.info(f'{line_number()} {function_name} IS LINK? [{add_uri}]')
            new_file = FileHolder(add_uri)
            show.info(f'{line_number()} {function_name} GET FILES [FILE] [SHA] [{new_file.file_sha[0:constants.SHA_SIZE]}]')
            line = self._file_database.is_file_with_sha(new_file.file_sha)
            if line:
                show.info(f'{line_number()} {function_name} FOUND')
                show.info(f'{line_number()} {function_name} GET FILES [SYMLINK] [URI] [{new_file.file_uri}]')
                line = self._file_database.is_file_with(new_file.file_uri, constants.SYMLINK)
                if not line:
                    show.info(f'{line_number()} {function_name} FOUND')
                    show.info(f'{line_number()} {function_name} NEW ROW [SYMLINK] [{new_file}]')
                    self.new_row(new_file, constants.SYMLINK)
            self._file_database.change_uri_from_to(add_uri, constants.MOVED_FILE, constants.SYMLINK)

        elif is_dir(add_uri):
            show.warning(f'{line_number()} {function_name} IS DIRECTORY? [{add_uri}]')
            show.warning(f'{line_number()} {function_name} ENDED')

        elif is_file(add_uri):
            show.info(f'{line_number()} {function_name} IS FILE? [{add_uri}]')
            new_file = FileHolder(add_uri)
            show.info(f'{line_number()} {function_name} GET FILES [FILE] [SHA] [{new_file.file_sha[0:constants.SHA_SIZE]}]')
            line = self._file_database.is_file_with_sha(new_file.file_sha)
            if not line:
                show.info(f'{line_number()} {function_name} NOT FOUND')
                file_without_parent = self._file_database.is_file_with(new_file.file_uri, constants.DELETED_PARENT)
                show.info(f'{line_number()} {function_name} GET FILE INDEX - URI [{new_file.file_uri}] DELETED_PARENT <= [{file_without_parent}]')
                if file_without_parent:
                    show.info(f'{line_number()} {function_name} CHANGE FROM DELETED TO FILE [{new_file.file_uri}]')
                    self._file_database.delete(new_file.file_uri, constants.DELETED_PARENT)
                    show.info(f'{line_number()} {function_name} CHANGE HASH FILE [{constants.FILE}] [{new_file.file_sha[0:constants.SHA_SIZE]}] [{new_file.file_uri}]')
                    self._file_database.insert(file_without_parent)
                else:
                    show.info(f'{line_number()} {function_name} NEW ROW [FILE] [{new_file}]')
                    self.new_row(new_file, constants.FILE)

                show.info(f'{line_number()} {function_name} GET FILES [SHA] [DELETED_PARENT] [{new_file.file_sha[0:constants.SHA_SIZE]}]')
                line = self._file_database.is_element_with_sha(new_file.file_sha, constants.DELETED_PARENT)
                if line:
                    show.info(f'{line_number()} {function_name} GET FILES - NO FILE FOUND')
                    for row in line:
                        show.info(f'{line_number()} {function_name} GET FILES - FILE FOUND [{row}] [{new_file.file_uri}]')
                        self.execute(row, new_file.file_uri)
                self._file_database.change_uri_from_to(new_file.file_uri, constants.DELETED_PARENT, constants.SYMLINK)
            else:
                line = line[constants.FILE][0]
                show.info(f'{line_number()} {function_name} GET FILE [SYMLINK] [{new_file.file_uri}]')
                line_uri = self._file_database.is_file_with(new_file.file_uri, constants.SYMLINK)
                if not line_uri:
                    show.info(f'{line_number()} {function_name} GET FILE - SYMLINK NOT FOUND')
                    show.info(f'{line_number()} {function_name} FILE COMPARISON [{constants.COMPARISON_METHOD}] [{new_file.file_uri}] [{line}]')
                    if new_file.file_uri != line and file_equals(new_file.file_uri, line, constants.COMPARISON_METHOD):
                        show.info(f'{line_number()} if {new_file.file_uri} != {line}')
                        show.info(f'{line_number()} {function_name} FILE COMPARISON [{constants.COMPARISON_METHOD}] [{new_file.file_uri}] [{line}]')
                        show.info(f'{line_number()} {function_name} NEW ROW [SYMLINK] [{new_file}]')
                        self.new_row(new_file, kind=constants.SYMLINK)
                        show.info(f'{line_number()} {function_name} NEW ROW [REMOVED] [{new_file}]')
                        self.new_row(new_file, kind=constants.REMOVED)

                        show.info(f'{line_number()} {function_name} DELETE ROW [{new_file.file_uri}]')
                        self.delete_row(new_file.file_uri)
                        show.info(f'{line_number()} {function_name} CREATE LINK [{new_file.file_uri}] [{line}]')
                        self.execute(new_file.file_uri, line)
                else:
                    show.error(section_line(constants.SYMBOL_EQ, 100))
                    show.error(f'{line_number()} {function_name} =====> ERROR: [{line_uri}] IT SHOULD NEVER HAPPENED! <=====')
                    show.error(section_line(constants.SYMBOL_EQ, 100))

        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} SAVE DATA')
        self.save_data()
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} END')
        self.pause_thread()

    def mod_file(self, mod_uri):
        function_name = 'MODIFY FILE:'
        mod_uri = mod_uri.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        self.update_thread_started_time()
        show.warning(f'{line_number()} {section_line(constants.SYMBOL_EQ, constants.LINE_LEN)}')
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

            show.info(f'{line_number()} {function_name} GET FILES [SHA] [FILE] [{new_file.file_sha[0:constants.SHA_SIZE]}]')
            gotten_by_sha = self._file_database.is_element_with_sha(new_file.file_sha, constants.FILE)
            if gotten_by_sha:
                show.info(f'{line_number()} {function_name} GET FILES [SHA] FOUND')
                if len(gotten_by_sha[0][constants.FILE]) == 1:
                    if new_file.file_uri != gotten_by_sha[0][constants.FILE][0]:
                        show.info(f'{line_number()} {function_name} FILE MOVED - NO HANDLING HERE')
                        show.info(f'{line_number()} {function_name} FILE UNCHANGED {new_file.file_uri}')
                        show.info(f'{line_number()} {function_name} URI    CHANGED {gotten_by_sha[0][constants.FILE][0]}')
                else:
                    show.error(f'{line_number()} {function_name} TO MANY FILES IN [{gotten_by_sha[0][constants.FILE]}]')
            show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] [{new_file.file_uri}]')
            gotten_by_uri = self._file_database.is_file_with(new_file.file_uri, constants.FILE)
            if gotten_by_uri:
                show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] - FOUND')
                if new_file.file_sha != gotten_by_uri[constants.DOC_ID]:
                    show.info(f'{line_number()} COMPARISON IS DIFFERENT [{new_file.file_sha[0:constants.SHA_SIZE]}] [{gotten_by_uri[constants.SHA].values[0][0:constants.SHA_SIZE]}]')
                    show.info(f'{line_number()} CONTENT CHANGED - MUST UPDATE LINKS WHEN APPLICABLE')
                    show.info(f'{line_number()} OLD: {gotten_by_uri[constants.SHA].values[0][0:constants.SHA_SIZE]}')
                    show.info(f'{line_number()} NEW: {new_file.file_sha[0:constants.SHA_SIZE]}')
                    self._file_database.change_hash_file(gotten_by_uri[constants.DOC_ID], new_file.file_sha)

            if gotten_by_sha is None and gotten_by_uri is None:
                show.info(f'{line_number()} {function_name} GET FILES AND GET FILE INDEX DID NOT FIND RESOURCES')
                show.info(f'{line_number()} {function_name} ADD FILE [{mod_uri}]')
                self.add_file(mod_uri)

        show.info(f'{line_number()} {function_name} SAVE DATA')
        self.save_data()
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} ENDED')
        self.pause_thread()

    def move_file(self, source_file, target_file):
        function_name = 'MOVE FILE:'
        source_file = source_file.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        target_file = target_file.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        self.update_thread_started_time()
        section_message = section_line(constants.SYMBOL_EQ, constants.LINE_LEN)
        show.warning(f'{line_number()} {section_message}')
        show.warning(f'{line_number()} {function_name} MOVE FILE [{source_file}] [{target_file}]')

        if is_link(target_file):
            show.info(f'{line_number()} {function_name} IS LINK [{target_file}]')
            show.info(f'{line_number()} {function_name} MOVE FILE LOCATION [URI] [{source_file}] [{target_file}]')
            self._file_database.move_file(source_file, target_file)

        elif is_file(target_file):
            show.info(f'{line_number()} {function_name} IS FILE [{target_file}]')
            new_file = FileHolder(target_file)
            show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] [{new_file.file_uri}]')
            gotten_by_uri = self._file_database.is_file_with(new_file.file_uri, constants.FILE)
            if gotten_by_uri:
                show.info(f'{line_number()} {function_name} GET FILE INDEX - NOT FOUND')
                if new_file.file_sha != gotten_by_uri[constants.DOC_ID]:
                    show.info(f'{line_number()} {function_name} SHA DIFFERS [{new_file.file_sha[0:constants.SHA_SIZE]}] [{gotten_by_uri[constants.DOC_ID][0:constants.SHA_SIZE]}]')
                    # TODO: MUST CHECK IF NEW SHA ALREADY EXIST ON DATABASE # IF NOT EXIST, GO AHEAD # IF EXIST, MANAGE DUPE
                    show.info(f'{line_number()} {function_name} CHANGING SHA FROM [{gotten_by_uri[constants.DOC_ID][0:constants.SHA_SIZE]}]')
                    show.info(f'{line_number()} {function_name} CHANGING SHA TO   [{new_file.file_sha[0:constants.SHA_SIZE]}]')
                    self._file_database.change_hash_file(gotten_by_uri[constants.DOC_ID], new_file.file_sha)

            show.info(f'{line_number()} {function_name} GET FILES [SHA] [FILE] [{new_file.file_sha}]')
            gotten_by_sha = self._file_database.is_element_with_sha(new_file.file_sha, constants.FILE)
            if gotten_by_sha:
                show.info(f'{line_number()} {function_name} GET FILES - NOT FOUND')
                if new_file.file_uri != gotten_by_sha[0][constants.FILE][0]:
                    show.info(f'{line_number()} {function_name} URI DIFFERS:')
                    show.info(f'{line_number()} {function_name} URI CHANGED FROM [{gotten_by_sha[0][constants.FILE][0]}]')
                    show.info(f'{line_number()} {function_name} URI CHANGED TO   [{new_file.file_uri}]')
                    show.info(f'{line_number()} {function_name} CHANGING MAIN FILE ADDRESS BEFORE')
                    show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} {constants.NEW_LINE}{self.print_table()}')
                    self._file_database.move_file(gotten_by_sha[0][constants.FILE][0], new_file.file_uri)
                    show.info(f'{line_number()} {function_name} CHANGING MAIN FILE ADDRESS AFTER')
                    show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} {constants.NEW_LINE}{self.print_table()}')
                    self._file_database.change_sha_from_to(new_file.file_sha, constants.SYMLINK, constants.MOVED_FILE)
                    show.info(f'{line_number()} {function_name} CHANGING LINK FILE ADDRESS')
                    show.debug(f'{line_number()} {constants.DEBUG_MARKER} {function_name} {constants.NEW_LINE}{self.print_table()}')

                    show.info(f'{line_number()} {function_name} GET FILES [SHA] [MOVED_FILE] [{new_file.file_sha[0:constants.SHA_SIZE]}]')
                    line = self._file_database.is_element_with_sha(new_file.file_sha, constants.MOVED_FILE)
                    if line:
                        show.info(f'{line_number()} {function_name} GET FILES - NOT FOUND')
                        for row in line:
                            show.info(f'{line_number()} {function_name} DELETE ROW [{row[constants.FILE]}]')
                            self.delete_row(row[constants.FILE])
                            show.info(f'{line_number()} {function_name} CREATE LINK [{row[constants.FILE]}] [{new_file.file_uri}]')
                            self.execute(row[constants.FILE], new_file.file_uri)

        show.info(f'{line_number()} {function_name} SAVE DATA')
        self.save_data()
        show.info(f'{line_number()} {function_name} ENDED')
        self.pause_thread()

    def del_file(self, del_uri):
        function_name = 'DELETE FILE:'
        del_uri = del_uri.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        self.update_thread_started_time()
        section_message = section_line(constants.SYMBOL_EQ, constants.LINE_LEN)
        show.warning(f'{line_number()} {section_message}')
        show.warning(f'{line_number()} {function_name} DELETE FILE [{del_uri})]')

        show.info(f'{line_number()} {function_name} GET FILE INDEX [REMOVED] [{del_uri}]')
        delete_index = self._file_database.is_file_with(del_uri, constants.REMOVED)
        if delete_index is not None:
            show.info(f'{line_number()} {function_name} DELETE INDEX - NOT FOUND')
            show.info(f'{line_number()} {function_name} DROP [{delete_index[constants.DOC_ID]}]')
            self._file_database.delete_file_from_database(delete_index)
            show.info(f'{line_number()} {function_name} SAVE DATA')
            self.save_data()
            show.info(f'{line_number()} {function_name} ENDED')
            return

        show.info(f'{line_number()} {function_name} DELETE INDEX - FOUND')
        show.info(f'{line_number()} {function_name} GET FILE INDEX [SYMLINK] [{del_uri}]')
        delete_index = self._file_database.is_file_with(del_uri, constants.SYMLINK)
        if delete_index is not None:
            show.info(f'{line_number()} {function_name} DELETE INDEX FOUND')
            show.info(f'{line_number()} {function_name} DROP [{delete_index[constants.DOC_ID]}]')
            self._file_database.delete_file_from_database(delete_index)
            show.info(f'{line_number()} {function_name} SAVE DATA')
            self.save_data()
            show.info(f'{line_number()} {function_name} ENDED')
            return

        show.info(f'{line_number()} {function_name} GET FILE INDEX [FILE] [{del_uri}]')
        delete_index = self._file_database.is_file_with(del_uri, constants.FILE)
        if delete_index is not None:
            show.info(f'{line_number()} {function_name} GET FILE INDEX - FOUND')
            show.info(f'{line_number()} {function_name} DROP [{delete_index[constants.DOC_ID]}]')
            self._file_database.delete_file_from_database(delete_index)
            sha = delete_index[constants.SHA].values[0]
            show.info(f'{line_number()} {function_name} GET FILES [SHA] [SYMLINK] [{sha[0:constants.SHA_SIZE]}]')
            delete_index = self._file_database.is_element_with_sha(sha, constants.SYMLINK)
            if delete_index is not None:
                show.info(f'{line_number()} {function_name} GET FILES [SHA] [SYMLINK] - FOUND')
                for row in delete_index:
                    show.warning(f'{line_number()} {function_name} DELETE ROW [{row}]')
                    self.delete_row(row)
                self._file_database.change_sha_from_to(sha, constants.SYMLINK, constants.DELETED_PARENT)

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
        show.info(f'{line_number()} FILE HOLDER: CREATE URI [{self._file_sha[0:constants.SHA_SIZE]}] [{self._file_uri}]')

    @property
    def file_sha(self):
        show.info(f'{line_number()} FILE HOLDER: GET SHA [{self._file_sha[0:constants.SHA_SIZE]}]')
        return self._file_sha

    def set_sha(self):
        show.info(f'{line_number()} FILE HOLDER: CREATING SHA')
        if self._file_uri:
            self._file_sha = get_hash(self._file_uri, constants.HASH_SHA512)

    def __repr__(self):
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} FILE HOLDER REPORT')
        return self._file_uri


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        show.info(f'{line_number()} ON_CREATED')
        file_list.add_file(event.src_path)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_list}')

    def on_modified(self, event):
        show.info(f'{line_number()} ON_MODIFIED')
        file_list.mod_file(event.src_path)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_list}')

    def on_moved(self, event):
        show.info(f'{line_number()} ON_MOVED')
        file_list.move_file(event.src_path, event.dest_path)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_list}')

    def on_deleted(self, event):
        show.info(f'{line_number()} ON_DELETED')
        file_list.del_file(event.src_path)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_list}')


def tray_icon_click(_, selected_tray_item):
    global system_tray_icon
    show.debug(f'{line_number()} Tray Icon Click: [{str(selected_tray_item)}]')
    if str(selected_tray_item) == constants.LABEL_DONE:
        system_tray_icon.icon = Image.open(constants.ICON_DONE)
    if str(selected_tray_item) == constants.LABEL_ERROR:
        system_tray_icon.icon = Image.open(constants.ICON_ERROR)
    if str(selected_tray_item) == constants.LABEL_PAUSE:
        system_tray_icon.icon = Image.open(constants.ICON_PAUSE)
    if str(selected_tray_item) == constants.LABEL_EXIT:
        file_list.save_data()
        file_list.terminate()
        show.warning(f'Terminating {constants.LABEL_MAIN} System...')
        system_tray_icon.stop()
    show.warning(f'Tray Icon Done...')


if __name__ == "__main__":
    log_handler = [RotatingFileHandler(constants.LOG_FILE, maxBytes=10000000, backupCount=20000, encoding=constants.UTF8)]
    log_format = '%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s'
    log_formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    date_format = constants.DATE_FORMAT

    logging.basicConfig(format=log_format,
                        datefmt=date_format,
                        level=constants.DEBUG_LEVEL,
                        handlers=log_handler)

    console = logging.StreamHandler()
    console.setLevel(constants.DEBUG_LEVEL)
    console.setFormatter(log_formatter)
    logging.getLogger().addHandler(console)

    logging.getLogger(constants.WATCHDOG).setLevel(logging.CRITICAL)

    argument_parser = arg_parse.ArgumentParser()
    argument_parser.add_argument(constants.PARAMETER_PATH, required=False, default=constants.MAIN_PATH)
    argument_parser.add_argument(constants.PARAMETER_SCAN, required=False, default=False)
    arguments = argument_parser.parse_args()

    event_source_path = arguments.path
    event_source_scan = arguments.scan

    state = False
    system_tray_image = Image.open(constants.ICON_DONE)
    system_tray_icon = pystray.Icon(f'{constants.LABEL_MAIN} 1',
                                    system_tray_image,
                                    constants.LABEL_MAIN,
                                    menu=pystray.Menu(
                                        pystray.MenuItem(constants.LABEL_DONE, tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem(constants.LABEL_PAUSE, tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem(constants.LABEL_ERROR, tray_icon_click, checked=lambda item: state),
                                        pystray.MenuItem(constants.LABEL_EXIT, tray_icon_click, checked=lambda item: state)))

    show.warning(f'Starting {constants.LABEL_MAIN} System...')
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

    show.warning(f'{constants.LABEL_MAIN} Initialized...')
    file_list.report_data()

    event_handler = MonitorFolder()
    observer = Observer()
    observer.schedule(event_handler, path=event_source_path, recursive=True)

    observer.start()

    if constants.UI == constants.GUI:
        show.warning(f'{constants.LABEL_MAIN} Tray Initialized...')
        system_tray_icon.run()

    if constants.UI == constants.CLI:
        show.warning(f'{section_line(constants.SYMBOL_EQ, 20)} Keyboard Initialized... {section_line(constants.SYMBOL_EQ, 20)}')
        keyboard_listening = True
        try:
            keyboard = KBHit()
            while keyboard_listening:
                time.sleep(1)
                if keyboard.check():
                    file_list.save_data()
                    show.warning(f'Terminating {constants.LABEL_MAIN} System...')
                    keyboard_listening = False

        except KeyboardInterrupt as keyboard_interrupt:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} KeyboardInterrupt: [{keyboard_interrupt}]')

    file_list.terminate()
    observer.stop()
    observer.join()

    show.warning(f'Bye...')
