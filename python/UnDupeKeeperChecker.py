import os
import time
import shutil
import pystray
import threading
import constants

from methods import get_hash
from methods import line_number
from methods import file_equals
from methods import section_line

import argparse as arg_parse
from pymongo import MongoClient

from PIL import Image
from queue import Queue

from sys import stdin as sys_standard_in

from os import name as os_name
from os import path as os_path
from os import walk as os_walk

from os import unlink as delete_link
from os import remove as delete_file

from os.path import islink as is_link
from os.path import isfile as is_file
from os.path import exists as uri_exists

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

from logging.handlers import RotatingFileHandler

import logging
show = logging.getLogger(constants.DEBUG_CORE)

thread_started = time.time()
system_tray_icon = None


if os_name == constants.WINDOWS_NT:
    import msvcrt
else:
    import termios
    import atexit
    from select import select


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

    @staticmethod
    def file_operation(mode, source_file, target_file):
        _, drive_tail = os.path.splitdrive(source_file)
        drive_tail = drive_tail.lstrip(os.path.sep)
        target_file = os.path.join(target_file, drive_tail)
        os.makedirs(os.path.dirname(target_file), exist_ok=True)
        if mode == 'copy':
            shutil.copy2(source_file, target_file)
        elif mode == 'move':
            shutil.move(source_file, target_file)

    def add_file(self, add_uri):
        function_name = 'ADD FILE:'
        add_uri = add_uri.replace(constants.DOS_SLASH, constants.UNIX_SLASH)
        self.update_thread_started_time()
        show.info(f'{line_number()} {section_line(constants.SYMBOL_EQ, constants.LINE_LEN)}')
        new_file = FileHolder(add_uri)
        show.info(f'{line_number()} {function_name} [{add_uri}]')
        file_with_sha = self._file_database.database_get_item(new_file.file_sha)
        if file_with_sha and file_equals(add_uri, file_with_sha[constants.FILE][0], constants.COMPARISON_METHOD):
            show.info(f'{line_number()} {function_name} DELETE [{add_uri}]')
            # delete_file(add_uri)
        else:
            if is_link(add_uri):
                uri_file = os.readlink(add_uri)
                show.info(f'{line_number()} {function_name} COPY [{uri_file}] TO [{constants.TARGET_PATH}]')
                # self.file_operation('copy', uri_file, constants.TARGET_PATH)
                # delete_link(add_uri)
            elif is_file(add_uri):
                show.info(f'{line_number()} {function_name} COPY [{add_uri}] TO [{constants.TARGET_PATH}]')
                # self.file_operation('move', add_uri, constants.TARGET_PATH)
            else:
                show.info(f'{line_number()} {function_name} NO [{add_uri}] VALID ACTION')

        show.info(f'{line_number()} {constants.DEBUG_MARKER} {function_name} END')
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
            self._file_sha = get_hash(self._file_uri, constants.HASH_MD5)

    def __repr__(self):
        show.debug(f'{line_number()} {constants.DEBUG_MARKER} FILE HOLDER REPORT')
        return self._file_uri


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        show.info(f'{line_number()} ON_CREATED')
        file_set.add_file(event.src_path)
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_set}')

    def on_modified(self, event):
        show.info(f'{line_number()} ON_MODIFIED [{event.src_path}]')
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_set}')

    def on_moved(self, event):
        show.info(f'{line_number()} ON_MOVED [{event.src_path}], [{event.dest_path}]')
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_set}')

    def on_deleted(self, event):
        show.info(f'{line_number()} ON_DELETED [{event.src_path}]')
        show.debug(f'{line_number()} {constants.DEBUG_MARKER}  {file_set}')


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
        file_set.terminate()
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
    file_set = FileList()
    file_set.start_thread()

    if event_source_scan:
        for root, dirs, files in os_walk(event_source_path, topdown=True):
            for name in files:
                uri = str(os_path.join(root, name))
                if uri_exists(uri):
                    show.info(f'{line_number()} SCAN: FILE LIST ADD FILE: [{uri}]')
                    file_set.add_file(uri)
        file_set.pause_thread()

    show.warning(f'{constants.LABEL_MAIN} Initialized...')

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
                    show.warning(f'Terminating {constants.LABEL_MAIN} System...')
                    keyboard_listening = False

        except KeyboardInterrupt as keyboard_interrupt:
            show.debug(f'{line_number()} {constants.DEBUG_MARKER} KeyboardInterrupt: [{keyboard_interrupt}]')

    file_set.terminate()
    observer.stop()
    observer.join()

    show.warning(f'Bye...')
