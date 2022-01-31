import sys
import time
import tools
import hashlib

import pandas as pd

from filecmp import cmp as is_equal
from os import remove as delete_file

from os import makedirs as make_dirs
from os.path import exists as uri_exists
from os.path import abspath as abs_path
from os.path import dirname as dir_name
from os.path import isfile as is_file
from os.path import islink as is_link

from subprocess import PIPE
from subprocess import CalledProcessError
from subprocess import run as run_command

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

from logging.handlers import RotatingFileHandler

import logging
logger = logging.getLogger('MAIN')

MAX_FILES = 10

SHA = 'SHA'
URI = 'URI'
KIND = 'KIND'

REMOVED = 'remo'
SYMLINK = 'link'
DELETED_PARENT = 'lynk'
FILE = 'file'
MOVED_FILE = 'move'

FILE_TABLE = 'file_table.txt'
LINK_TABLE = 'link_table.txt'
DATA_TABLE = 'UnDupyKeeper.pkl'


class FileList:
    def __init__(self):
        logger.info(f'{tools.lineno()} - def __init__(self):')
        self._save_count = 0
        self._file_allocation_table = pd.DataFrame(columns=[SHA, KIND, URI])
        self.load_data()

    def __repr__(self):
        logger.info(f'{tools.lineno()} - def __repr__(self):')
        return self.print_table()

    def print_table(self):
        logger.info(f'{tools.lineno()} - def print_table(self):')
        return_string = ''
        for index, row in self._file_allocation_table.iterrows():
            return_string += f'FILETABLE: {row[SHA]} {row[KIND]} {row[URI]}\n'
        logger.info(f'{tools.lineno()} - return return_string')
        return return_string

    def save_data(self):
        logger.info(f'{tools.lineno()} - def save_data(self):')
        logger.info(f'{tools.lineno()} - with open(FILE_TABLE, "w") as file_table_handler:')
        with open(FILE_TABLE, 'w') as file_table_handler:
            save_data_index = self._file_allocation_table.loc[self._file_allocation_table[KIND] == FILE]
            if save_data_index is not None and not save_data_index.empty:
                for value in save_data_index[URI].values:
                    file_table_handler.write(value + '\n')
                file_table_handler.flush()

        logger.info(f'{tools.lineno()} - with open(LINK_TABLE, "w") as link_table_handler:')
        with open(LINK_TABLE, 'w') as link_table_handler:
            save_data_index = self._file_allocation_table.loc[self._file_allocation_table[KIND] == SYMLINK]
            if save_data_index is not None and not save_data_index.empty:
                for value in save_data_index[URI].values:
                    link_table_handler.write(value + '\n')
                link_table_handler.flush()
            save_data_index = self._file_allocation_table.loc[self._file_allocation_table[KIND] == DELETED_PARENT]
            if save_data_index is not None and not save_data_index.empty:
                for value in save_data_index[URI].values:
                    link_table_handler.write(value + '\n')
                link_table_handler.flush()

        self._save_count += 1
        if self._save_count > MAX_FILES:
            logger.info(f'{tools.lineno()} - if self._save_count > {MAX_FILES}:')
            logger.info(f'{tools.lineno()} - self._file_allocation_table.to_pickle(DATA_TABLE)')
            self._file_allocation_table.to_pickle(DATA_TABLE)
            self._save_count = 0

    def load_data(self):
        logger.info(f'{tools.lineno()} - def load_data(self):')
        try:
            logger.info(f'{tools.lineno()} - self._file_allocation_table = pd.read_pickle(DATA_TABLE)')
            self._file_allocation_table = pd.read_pickle(DATA_TABLE)
        except Exception as exception:
            logger.info(f'{tools.lineno()} - FILE NOT FOUND: {exception}')

    def get_platform(self):
        logger.info(f'{tools.lineno()} - def get_platform(self):')
        platforms = {'linux' : 'Linux',
                     'linux1': 'Linux',
                     'linux2': 'Linux',
                     'darwin': 'OS X',
                     'win32' : 'Windows'}
        if sys.platform not in platforms:
            logger.info(f'{tools.lineno()} - if sys.platform not in platforms:')
            logger.info(f'{tools.lineno()} - return sys.platform')
            return sys.platform
        logger.info(f'{tools.lineno()} - platforms[sys.platform]')
        return platforms[sys.platform]

    def execute(self, target_link, source_file):
        logger.info(f'{tools.lineno()} - def execute(self, {target_link}, {source_file}):')
        return_value = None

        source_is_file = is_file(source_file)
        target_exists = uri_exists(target_link)
        if source_is_file and not target_exists:
            logger.info(f'{tools.lineno()} - if source_is_file and not target_exists:')
            dir_name_path = dir_name(abs_path(target_link))
            if not uri_exists(dir_name_path):
                logger.info(f'{tools.lineno()} - if not uri_exists({dir_name_path}):')
                logger.info(f'{tools.lineno()} - make_dirs({dir_name_path})')
                make_dirs(dir_name_path)

            platform = {"Linux"  : f'ln -s {source_file} {target_link}',
                        "Windows": f'mklink {target_link} {source_file}'}
            command = platform[self.get_platform()]

            try:
                logger.info(f'{tools.lineno()} - process = run_command({command},')
                process = run_command(command,
                                      shell=True,
                                      check=True,
                                      stdout=PIPE,
                                      universal_newlines=True)
                return_value = process.stdout
            except CalledProcessError as called_process_error:
                logger.error(f'{tools.lineno()} - CalledProcessError {called_process_error}')
                logger.error(f'{tools.lineno()} - Source is File? [{source_is_file}], Target Exists? [{target_exists}]')
        else:
            logger.error(f'{tools.lineno()} - ERROR: Source is File? [{source_is_file}], Target Exists? [{target_exists}]')

        logger.info(f'{tools.lineno()} - return {return_value}')
        return return_value

    def new_row(self, new_file, kind):
        logger.info(f'{tools.lineno()} - def new_row(self, {new_file}, {kind}):')
        new_row = {SHA: new_file.file_sha,
                   KIND: kind,
                   URI: new_file.file_uri}
        logger.info(f'{tools.lineno()} - self._file_allocation_table = self._file_allocation_table.append(new_row, ignore_index=True)')
        self._file_allocation_table = self._file_allocation_table.append(new_row, ignore_index=True)

    def delete_row(self, row):
        logger.info(f'{tools.lineno()} - def delete_row(self, {row}):')
        try:
            logger.info(f'{tools.lineno()} - delete_file({row})')
            delete_file(row)
        except FileNotFoundError as file_not_found_error:
            logger.error(f'{tools.lineno()} - FileNotFoundError: {row} {file_not_found_error}')

    def get_file_index(self, uri, kind):
        logger.info(f'{tools.lineno()} - def get_file_index(self, {uri}, {kind}):')
        if uri in self._file_allocation_table[URI].values:
            file_index = self._file_allocation_table.loc[(self._file_allocation_table[URI] == uri) &
                                                         (self._file_allocation_table[KIND] == kind)]
            if file_index is not None and not file_index.empty:
                logger.info(f'{tools.lineno()} - return file_index')
                return file_index
        logger.info(f'{tools.lineno()} - return None')
        return None

    def get_file(self, uri, kind):
        logger.info(f'{tools.lineno()} - def get_file(self, {uri}, {kind}):')
        file_index = self.get_file_index(uri, kind)
        if file_index is not None and not file_index.empty:
            logger.info(f'{tools.lineno()} - return file_index[URI].values[0]')
            return file_index[URI].values[0]
        logger.info(f'{tools.lineno()} - return None')
        return None

    def get_files(self, key_index, kind, file_index):
        logger.info(f'{tools.lineno()} - def get_files(self, {key_index}, {kind}, {file_index}):')
        if key_index in self._file_allocation_table[file_index].values:
            file_index = self._file_allocation_table.loc[(self._file_allocation_table[file_index] == key_index) &
                                                         (self._file_allocation_table[KIND] == kind)]
            if file_index is not None and not file_index.empty:
                logger.info(f'{tools.lineno()} - return file_index[URI].values')
                return file_index[URI].values
        logger.info(f'{tools.lineno()} - return None')
        return None

    def add_file(self, uri):
        logger.info(f'{tools.lineno()} - ==============================================')
        logger.info(f'{tools.lineno()} - def add_file(self, {uri}):')

        if is_link(uri):
            logger.info(f'{tools.lineno()} - if is_link({uri}):')
            new_file = FileHolder(uri)
            logger.info(f'{tools.lineno()} - line = self.get_files({new_file.file_sha}, FILE, SHA)')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            logger.info(f'if {line} is None:')
            if line is not None:
                logger.info(f'{tools.lineno()} - if line is not None:')
                logger.info(f'{tools.lineno()} - line = self.get_files({new_file.file_uri}, SYMLINK, URI)')
                line = self.get_files(new_file.file_uri, SYMLINK, URI)
                if line is None:
                    logger.info(f'{tools.lineno()} - if line is None:')
                    logger.info(f'{tools.lineno()} - self.new_row({new_file}, SYMLINK)')
                    self.new_row(new_file, SYMLINK)

            self._file_allocation_table.loc[(self._file_allocation_table[URI] == uri) &
                                            (self._file_allocation_table[KIND] == MOVED_FILE), KIND] = SYMLINK
        elif is_file(uri):
            logger.info(f'{tools.lineno()} - elif is_file({uri}):')
            new_file = FileHolder(uri)
            logger.info(f'{tools.lineno()} - line = self.get_files({new_file.file_sha}, FILE, SHA)')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is None:
                logger.info(f'{tools.lineno()} - if line is None:')
                logger.info(f'{tools.lineno()} - self.new_row({new_file}, FILE)')
                self.new_row(new_file, FILE)

                logger.info(f'{tools.lineno()} - line = self.get_files({new_file.file_sha}, DELETED_PARENT, SHA)')
                line = self.get_files(new_file.file_sha, DELETED_PARENT, SHA)
                if line is not None:
                    logger.info(f'{tools.lineno()} - if line is not None:')
                    for row in line:
                        logger.info(f'{tools.lineno()} - self.execute({row}, {new_file.file_uri})')
                        self.execute(row, new_file.file_uri)

                self._file_allocation_table.loc[(self._file_allocation_table[SHA] == new_file.file_sha) &
                                                (self._file_allocation_table[KIND] == DELETED_PARENT), KIND] = SYMLINK
            else:
                line = line[0]
                logger.info(f'{tools.lineno()} - line_uri = self.get_file({new_file.file_uri}, SYMLINK)')
                line_uri = self.get_file(new_file.file_uri, SYMLINK)
                if line_uri is None:
                    logger.info(f'{tools.lineno()} - if line_uri is None:')
                    logger.info(f'and is_equal(new_file.file_uri, line_uri, False):')
                    if new_file.file_uri != line and is_equal(new_file.file_uri, line, False):
                        logger.info(f'{tools.lineno()} - if {new_file.file_uri} != {line}')
                        logger.info(f'{tools.lineno()} - if is_equal({new_file.file_uri}, {line}, False):')

                        logger.info(f'{tools.lineno()} - self.new_row({new_file}, kind=SYMLINK)')
                        self.new_row(new_file, kind=SYMLINK)
                        logger.info(f'{tools.lineno()} - self.new_row({new_file}, kind=REMOVED)')
                        self.new_row(new_file, kind=REMOVED)

                        logger.info(f'{tools.lineno()} - self.delete_row({new_file.file_uri})')
                        self.delete_row(new_file.file_uri)
                        logger.info(f'{tools.lineno()} - self.execute({new_file.file_uri}, {line})')
                        self.execute(new_file.file_uri, line)
                else:
                    logger.error(f'{tools.lineno()} - else: {line_uri}')

        logger.info(f'{tools.lineno()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.lineno()} - return')

    def mod_file(self, uri):
        logger.info(f'{tools.lineno()} - ==============================================')
        logger.info(f'{tools.lineno()} - def mod_file(self, {uri}):')
        if is_link(uri):
            logger.warning(f'{tools.lineno()} - if is_link({uri}):')
            logger.info(f'{tools.lineno()} - return')
        elif is_file(uri):
            logger.info(f'{tools.lineno()} - elif is_file({uri}):')
            new_file = FileHolder(uri)

            logger.info(f'{tools.lineno()} - gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.info(f'{tools.lineno()} - if gotten_by_sha is not None:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.info(f'{tools.lineno()} - if {new_file.file_uri} == {gotten_by_sha[0]}:')
                    logger.info(f'{tools.lineno()} - FILE MOVED - NO HANDLING HERE')
                    logger.info(f'{tools.lineno()} - FILE UNCHANGED {new_file.file_uri}')
                    logger.info(f'{tools.lineno()} - URI    CHANGED {gotten_by_sha[0]}')

            logger.info(f'{tools.lineno()} - gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.info(f'{tools.lineno()} - if gotten_by_uri is not None:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.info(f'{tools.lineno()} - if {new_file.file_sha} != {gotten_by_uri[SHA].values[0]}:')
                    logger.info(f'{tools.lineno()} - CONTENT CHANGED - MUST UPDATE LINKS WHEN APPLICABLE')
                    logger.info(f'{tools.lineno()} - OLD: {gotten_by_uri[SHA].values[0]}')
                    logger.info(f'{tools.lineno()} - NEW: {new_file.file_sha}')
                    self._file_allocation_table.loc[(self._file_allocation_table[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha

            if gotten_by_sha is None and gotten_by_uri is None:
                logger.info(f'{tools.lineno()} - if gotten_by_sha is None and gotten_by_uri is None:')
                logger.info(f'{tools.lineno()} - self.add_file({uri})')
                self.add_file(uri)

        logger.info(f'{tools.lineno()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.lineno()} - return')

    def move_file(self, source_file, target_file):
        logger.info(f'{tools.lineno()} - ==============================================')
        logger.info(f'{tools.lineno()} - def move_file(self, {source_file}, {target_file}):')

        if is_link(target_file):
            logger.info(f'{tools.lineno()} - TRUE is_link({target_file}):')
            logger.info(f'{tools.lineno()} - self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file), URI] = target_file')
            self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file), URI] = target_file

        elif is_file(target_file):
            logger.info(f'{tools.lineno()} - TRUE is_file({target_file}):')
            new_file = FileHolder(target_file)
            logger.info(f'{tools.lineno()} - gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.info(f'{tools.lineno()} - if gotten_by_uri is not None:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.info(f'{tools.lineno()} - if new_file.file_sha != gotten_by_uri[SHA].values[0]:')
                    # TODO: MUST CHECK IF NEW SHA ALREADY EXIST ON DATABASE
                    # IF NOT EXIST, GO AHEAD
                    # IF EXIST, MANAGE DUPE
                    logger.info(f'{tools.lineno()} - SHA CHANGED FROM {gotten_by_uri[SHA].values[0]}')
                    logger.info(f'{tools.lineno()} - SHA CHANGED TO   {new_file.file_sha}')
                    logger.info(f'{tools.lineno()} - self._file_allocation_table.loc[(self._file_allocation_table[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha')
                    self._file_allocation_table.loc[(self._file_allocation_table[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha

            logger.info(f'{tools.lineno()} - gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.info(f'{tools.lineno()} - if gotten_by_sha is not None:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.info(f'{tools.lineno()} - if if new_file.file_uri != gotten_by_sha[0]:')
                    logger.info(f'{tools.lineno()} - URI CHANGED FROM {gotten_by_sha[0]}')
                    logger.info(f'{tools.lineno()} - URI CHANGED TO   {new_file.file_uri}')
                    logger.info(f'{tools.lineno()} - CHANGING MAIN FILE ADDRESS BEFORE')
                    logger.debug(f'{tools.lineno()} - \n{self.print_table()}')
                    self._file_allocation_table.loc[(self._file_allocation_table[URI] == gotten_by_sha[0]) &
                                                    (self._file_allocation_table[KIND] == FILE), URI] = new_file.file_uri
                    logger.info(f'{tools.lineno()} - CHANGING MAIN FILE ADDRESS AFTER')
                    logger.debug(f'{tools.lineno()} - \n{self.print_table()}')
                    self._file_allocation_table.loc[(self._file_allocation_table[SHA] == new_file.file_sha) &
                                                    (self._file_allocation_table[KIND] == SYMLINK), KIND] = MOVED_FILE
                    logger.info(f'{tools.lineno()} - CHANGING LINK FILE ADDRESS')
                    logger.debug(f'{tools.lineno()} - \n{self.print_table()}')
                    line = self.get_files(new_file.file_sha, MOVED_FILE, SHA)
                    if line is not None:
                        logger.info(f'{tools.lineno()} - if line is not None:')
                        for row in line:
                            logger.info(f'{tools.lineno()} - self.delete_row({row})')
                            self.delete_row(row)
                            logger.info(f'{tools.lineno()} - self.execute({row}, {new_file.file_uri})')
                            self.execute(row, new_file.file_uri)
        logger.info(f'{tools.lineno()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.lineno()} - return')

    def del_file(self, uri):
        logger.info(f'{tools.lineno()} - ==============================================')
        logger.info(f'{tools.lineno()} - def del_file(self, {uri}):')

        logger.info(f'{tools.lineno()} - delete_index = self.get_file_index(uri, REMOVED)')
        delete_index = self.get_file_index(uri, REMOVED)
        logger.info(f'{tools.lineno()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{tools.lineno()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_allocation_table.drop(delete_index.index, inplace=True)
            logger.info(f'{tools.lineno()} - self.save_data()')
            self.save_data()
            logger.info(f'{tools.lineno()} - return')
            return

        logger.info(f'{tools.lineno()} - delete_index = self.get_file_index(uri, SYMLINK)')
        delete_index = self.get_file_index(uri, SYMLINK)
        logger.info(f'{tools.lineno()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{tools.lineno()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_allocation_table.drop(delete_index.index, inplace=True)
            logger.info(f'{tools.lineno()} - self.save_data()')
            self.save_data()
            logger.info(f'{tools.lineno()} - return')
            return

        logger.info(f'{tools.lineno()} - delete_index = self.get_file_index(uri, FILE)')
        delete_index = self.get_file_index(uri, FILE)
        logger.info(f'{tools.lineno()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{tools.lineno()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_allocation_table.drop(delete_index.index, inplace=True)
            sha = delete_index[SHA].values[0]
            logger.info(f'{tools.lineno()} - delete_index = self.get_files(sha, SYMLINK, SHA)')
            delete_index = self.get_files(sha, SYMLINK, SHA)
            logger.info(f'{tools.lineno()} - if delete_index is not None: {delete_index is not None}')
            if delete_index is not None:
                for row in delete_index:
                    logger.info(f'{tools.lineno()} - self.delete_row({row})')
                    self.delete_row(row)
                self._file_allocation_table.loc[(self._file_allocation_table[SHA] == sha) &
                                                (self._file_allocation_table[KIND] == SYMLINK), KIND] = DELETED_PARENT
        logger.info(f'{tools.lineno()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.lineno()} - return')


class FileHolder:
    def __init__(self, file_path):
        logger.info(f'{tools.lineno()} - FileHolder {file_path}')
        self._file_uri = file_path
        self._file_sha = None
        self.set_sha()

    @property
    def file_uri(self):
        logger.info(f'{tools.lineno()} - file_uri {self._file_uri}')
        return self._file_uri

    @file_uri.setter
    def file_uri(self, uri):
        self._file_uri = uri
        self.set_sha()
        logger.info(f'{tools.lineno()} - file_uri {self._file_sha} {self._file_uri}')

    @property
    def file_sha(self):
        logger.info(f'{tools.lineno()} - file_sha {self._file_sha}')
        return self._file_sha

    def set_sha(self):
        logger.info(f'{tools.lineno()} - set_sha')
        if self._file_uri:
            # digest_method = hashlib.md5()
            digest_method = hashlib.sha1()
            # digest_method = hashlib.sha256()
            # digest_method = hashlib.sha512()
            memory_view = memoryview(bytearray(128*1024))
            retry = True
            while retry:
                try:
                    logger.info(f'{tools.lineno()} - Getting file HASH CODE...')
                    with open(self._file_uri, 'rb', buffering=0) as uri:
                        for element in iter(lambda: uri.readinto(memory_view), 0):
                            digest_method.update(memory_view[:element])
                    self._file_sha = digest_method.hexdigest()
                    retry = False
                except PermissionError as permission_error:
                    logger.error(f'{tools.lineno()} - PermissionError: {permission_error}')
                    time.sleep(1)

    def __repr__(self):
        logger.info(f'{tools.lineno()} - def __repr__(self):')
        logger.info(f'{tools.lineno()} - return self._file_uri')
        return self._file_uri


file_list = FileList()


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        file_list.add_file(event.src_path)
        logger.debug(f'{tools.lineno()} - ON_CREATED\n{file_list}')

    def on_modified(self, event):
        file_list.mod_file(event.src_path)
        logger.debug(f'{tools.lineno()} - ON_MODIFIED\n{file_list}')

    def on_moved(self, event):
        file_list.move_file(event.src_path, event.dest_path)
        logger.debug(f'{tools.lineno()} - ON_MOVED\n{file_list}')

    def on_deleted(self, event):
        file_list.del_file(event.src_path)
        logger.debug(f'{tools.lineno()} - ON_DELETED\n{file_list}')


if __name__ == "__main__":

    log_handler = [RotatingFileHandler("UnDupyKeeper.log", maxBytes=10000000, backupCount=20000)]
    log_format = '%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s'
    log_formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    date_format = '%H:%M:%S'
    debug_level = "INFO"

    logging.basicConfig(format=log_format,
                        datefmt=date_format,
                        level=debug_level,
                        handlers=log_handler)

    console = logging.StreamHandler()
    console.setLevel(debug_level)
    console.setFormatter(log_formatter)
    logging.getLogger().addHandler(console)

    src_path = sys.argv[1]

    event_handler = MonitorFolder()
    observer = Observer()
    observer.schedule(event_handler, path=src_path, recursive=True)

    observer.start()

    logger.info(f'{tools.lineno()} - Starting UnDupyKeeper System...')
    WORKING = True
    try:
        while WORKING:
            time.sleep(0.25)
            kb = tools.KBHit()
            if kb.check():
                logger.info(f'{tools.lineno()} - Terminating UnDupyKeeper System...')
                WORKING = False

        observer.stop()
        observer.join()
    except KeyboardInterrupt as keyboard_interrupt:
        logger.info(f'{tools.lineno()} -  KeyboardInterrupt: [{keyboard_interrupt}]')
        observer.stop()
        observer.join()
