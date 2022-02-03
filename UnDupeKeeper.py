import os
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
from os.path import isdir as is_dir

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

FILE_TABLE = 'tests/file_table.txt'
LINK_TABLE = 'tests/link_table.txt'
DATA_TABLE = 'UnDupyKeeper.pkl'


class FileList:
    def __init__(self):
        logger.info(f'{tools.line_number()} - def __init__(self):')
        self._save_count = 0
        self._file_allocation_table = pd.DataFrame(columns=[SHA, KIND, URI])
        self.load_data()

    def __repr__(self):
        logger.info(f'{tools.line_number()} - def __repr__(self):')
        return self.print_table()

    def print_table(self):
        logger.info(f'{tools.line_number()} - def print_table(self):')
        return_string = ''
        for index, row in self._file_allocation_table.iterrows():
            return_string += f'FILETABLE: {row[SHA]} {row[KIND]} {row[URI]}\n'
        logger.info(f'{tools.line_number()} - return return_string')
        return return_string

    def save_data(self, now=False):
        logger.info(f'{tools.line_number()} - def save_data(self):')
        logger.info(f'{tools.line_number()} - with open(FILE_TABLE, "w") as file_table_handler:')
        with open(FILE_TABLE, 'w') as file_table_handler:
            save_data_index = self._file_allocation_table.loc[self._file_allocation_table[KIND] == FILE]
            if save_data_index is not None and not save_data_index.empty:
                for value in save_data_index[URI].values:
                    file_table_handler.write(value + '\n')
                file_table_handler.flush()

        logger.info(f'{tools.line_number()} - with open(LINK_TABLE, "w") as link_table_handler:')
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
        logger.warning(f'{tools.line_number()} - SAVED DATA:')
        logger.warning(f'{tools.line_number()} - TOTAL        FILES: {len(self._file_allocation_table.index)}')
        logger.warning(f'{tools.line_number()} - TOTAL UNIQUE FILES: {self._file_allocation_table[SHA].nunique()}')

        self._save_count += 1
        if self._save_count > MAX_FILES or now:
            logger.info(f'{tools.line_number()} - if self._save_count > {MAX_FILES}:')
            logger.info(f'{tools.line_number()} - self._file_allocation_table.to_pickle(DATA_TABLE)')
            self._file_allocation_table.to_pickle(DATA_TABLE)
            self._save_count = 0

    def load_data(self):
        logger.info(f'{tools.line_number()} - def load_data(self):')
        try:
            logger.info(f'{tools.line_number()} - self._file_allocation_table = pd.read_pickle(DATA_TABLE)')
            self._file_allocation_table = pd.read_pickle(DATA_TABLE)

            logger.warning(f'{tools.line_number()} - LOADED DATA:')
            logger.warning(f'{tools.line_number()} - TOTAL        FILES: {len(self._file_allocation_table.index)}')
            logger.warning(f'{tools.line_number()} - TOTAL UNIQUE FILES: {self._file_allocation_table[SHA].nunique()}')

        except Exception as exception:
            logger.info(f'{tools.line_number()} - FILE NOT FOUND: {exception}')

    def get_platform(self):
        logger.info(f'{tools.line_number()} - def get_platform(self):')
        platforms = {'linux' : 'Linux',
                     'linux1': 'Linux',
                     'linux2': 'Linux',
                     'darwin': 'OS X',
                     'win32' : 'Windows'}
        if sys.platform not in platforms:
            logger.info(f'{tools.line_number()} - if sys.platform not in platforms:')
            logger.info(f'{tools.line_number()} - return sys.platform')
            return sys.platform
        logger.info(f'{tools.line_number()} - platforms[sys.platform]')
        return platforms[sys.platform]

    def execute(self, target_link, source_file):
        logger.info(f'{tools.line_number()} - def execute(self, {target_link}, {source_file}):')
        return_value = None

        source_is_file = is_file(source_file)
        target_exists = uri_exists(target_link)
        if source_is_file and not target_exists:
            logger.info(f'{tools.line_number()} - if source_is_file and not target_exists:')
            dir_name_path = dir_name(abs_path(target_link))
            if not uri_exists(dir_name_path):
                logger.info(f'{tools.line_number()} - if not uri_exists({dir_name_path}):')
                logger.info(f'{tools.line_number()} - make_dirs({dir_name_path})')
                make_dirs(dir_name_path)

            platform = {"Linux"  : f'ln -s "{source_file}" "{target_link}"',
                        "Windows": f'mklink "{target_link}" "{source_file}"'}
            command = platform[self.get_platform()]

            try:
                logger.warning(f'{tools.line_number()} - process = run_command({command},')
                process = run_command(command,
                                      shell=True,
                                      check=True,
                                      stdout=PIPE,
                                      universal_newlines=True)
                return_value = process.stdout
            except CalledProcessError as called_process_error:
                logger.error(f'{tools.line_number()} - CalledProcessError {called_process_error}')
                logger.error(f'{tools.line_number()} - Source is File? [{source_is_file}], Target Exists? [{target_exists}]')
        else:
            logger.error(f'{tools.line_number()} - ERROR: Source is File? [{source_is_file}], Target Exists? [{target_exists}]')

        logger.info(f'{tools.line_number()} - return {return_value}')
        return return_value

    def new_row(self, new_file, kind):
        logger.info(f'{tools.line_number()} - def new_row(self, {new_file}, {kind}):')
        new_row = {SHA: new_file.file_sha,
                   KIND: kind,
                   URI: new_file.file_uri}
        logger.info(f'{tools.line_number()} - self._file_allocation_table = self._file_allocation_table.append(new_row, ignore_index=True)')
        self._file_allocation_table = self._file_allocation_table.append(new_row, ignore_index=True)

    def delete_row(self, row):
        logger.info(f'{tools.line_number()} - def delete_row(self, {row}):')
        try:
            logger.info(f'{tools.line_number()} - delete_file({row})')
            if not is_dir(row):
                delete_file(row)
            else:
                logger.error(f'{tools.line_number()} - CRITICAL ERROR: TRYING TO DELETE DIRECTORY: {row}')
        except FileNotFoundError as file_not_found_error:
            logger.error(f'{tools.line_number()} - FileNotFoundError: {row} {file_not_found_error}')

    def get_file_index(self, index_uri, kind):
        logger.info(f'{tools.line_number()} - def get_file_index(self, {index_uri}, {kind}):')
        if index_uri in self._file_allocation_table[URI].values:
            index_file = self._file_allocation_table.loc[(self._file_allocation_table[URI] == index_uri) &
                                                         (self._file_allocation_table[KIND] == kind)]
            if index_file is not None and not index_file.empty:
                logger.info(f'{tools.line_number()} - return file_index')
                return index_file
        logger.info(f'{tools.line_number()} - return None')
        return None

    def get_file(self, get_uri, kind):
        logger.info(f'{tools.line_number()} - def get_file(self, {get_uri}, {kind}):')
        index_file = self.get_file_index(get_uri, kind)
        if index_file is not None and not index_file.empty:
            logger.info(f'{tools.line_number()} - return file_index[URI].values[0]')
            return index_file[URI].values[0]
        logger.info(f'{tools.line_number()} - return None')
        return None

    def get_files(self, key_index, kind, index_file):
        logger.info(f'{tools.line_number()} - def get_files(self, {key_index}, {kind}, {index_file}):')
        if key_index in self._file_allocation_table[index_file].values:
            index_file = self._file_allocation_table.loc[(self._file_allocation_table[index_file] == key_index) &
                                                         (self._file_allocation_table[KIND] == kind)]
            if index_file is not None and not index_file.empty:
                logger.info(f'{tools.line_number()} - return file_index[URI].values')
                return index_file[URI].values
        logger.info(f'{tools.line_number()} - return None')
        return None

    def update_junk(self):
        logger.info(f'{tools.line_number()} - def update_junk(self):')
        index_file = self._file_allocation_table.loc[(self._file_allocation_table[KIND] == REMOVED)]
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                delete_index = self.get_file_index(uri_index, REMOVED)
                if delete_index is not None:
                    logger.warning(f'{tools.line_number()} - DROP JUNK: {uri_index}')
                    self._file_allocation_table.drop(delete_index.index, inplace=True)
        logger.warning(f'{tools.line_number()} - TOTAL        FILES: {len(self._file_allocation_table.index)}')
        logger.warning(f'{tools.line_number()} - TOTAL UNIQUE FILES: {self._file_allocation_table[SHA].nunique()}')
        logger.info(f'{tools.line_number()} - def update_junk(self): RETURN')

    def update_files(self):
        logger.info(f'{tools.line_number()} - def update_files(self):')
        index_file = self._file_allocation_table.loc[(self._file_allocation_table[KIND] == FILE)]
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                if not uri_exists(uri_index):
                    delete_index = self.get_file_index(uri_index, FILE)
                    delete_sha = delete_index[SHA].values[0]
                    if delete_index is not None:
                        logger.warning(f'{tools.line_number()} - DROP FILE: {uri_index}')
                        self._file_allocation_table.drop(delete_index.index, inplace=True)
                        delete_index = self.get_files(delete_sha, SYMLINK, SHA)
                        if delete_index is not None:
                            for row in delete_index:
                                logger.warning(f'{tools.line_number()} - self.delete_row({row})')
                                self.delete_row(row)
                        self._file_allocation_table.loc[(self._file_allocation_table[SHA] == delete_sha) &
                                                        (self._file_allocation_table[KIND] == SYMLINK), KIND] = DELETED_PARENT
        logger.info(f'{tools.line_number()} - def update_files(self): RETURN')

    def update_links(self):
        logger.info(f'{tools.line_number()} - def update_links(self):')
        index_file = self._file_allocation_table.loc[(self._file_allocation_table[KIND] == SYMLINK)]
        if index_file is not None and not index_file.empty:
            for uri_index in index_file[URI].values:
                if not is_link(uri_index) and not uri_exists(uri_index):
                    delete_index = self.get_file_index(uri_index, SYMLINK)
                    if delete_index is not None:
                        logger.warning(f'{tools.line_number()} - DROP LINK: {uri_index}')
                        self._file_allocation_table.drop(delete_index.index, inplace=True)
        logger.info(f'{tools.line_number()} - def update_links(self): RETURN')

    def add_file(self, add_uri):
        logger.warning(f'{tools.line_number()} - ==============================================')
        logger.warning(f'{tools.line_number()} - def add_file(self, {add_uri}):')

        if is_link(add_uri):
            logger.info(f'{tools.line_number()} - if is_link({add_uri}):')
            new_file = FileHolder(add_uri)
            logger.info(f'{tools.line_number()} - line = self.get_files({new_file.file_sha}, FILE, SHA)')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is not None:
                logger.info(f'{tools.line_number()} - if line is not None:')
                logger.info(f'{tools.line_number()} - line = self.get_files({new_file.file_uri}, SYMLINK, URI)')
                line = self.get_files(new_file.file_uri, SYMLINK, URI)
                if line is None:
                    logger.info(f'{tools.line_number()} - if line is None:')
                    logger.info(f'{tools.line_number()} - self.new_row({new_file}, SYMLINK)')
                    self.new_row(new_file, SYMLINK)

            self._file_allocation_table.loc[(self._file_allocation_table[URI] == add_uri) &
                                            (self._file_allocation_table[KIND] == MOVED_FILE), KIND] = SYMLINK
        elif is_dir(add_uri):
            logger.warning(f'{tools.line_number()} - elif is_dir({add_uri}):')
            logger.warning(f'{tools.line_number()} - return')
        elif is_file(add_uri):
            logger.info(f'{tools.line_number()} - elif is_file({add_uri}):')
            new_file = FileHolder(add_uri)
            logger.info(f'{tools.line_number()} - line = self.get_files({new_file.file_sha}, FILE, SHA)')
            line = self.get_files(new_file.file_sha, FILE, SHA)
            if line is None:
                logger.info(f'{tools.line_number()} - if line is None:')
                logger.info(f'{tools.line_number()} - self.new_row({new_file}, FILE)')
                self.new_row(new_file, FILE)

                logger.info(f'{tools.line_number()} - line = self.get_files({new_file.file_sha}, DELETED_PARENT, SHA)')
                line = self.get_files(new_file.file_sha, DELETED_PARENT, SHA)
                if line is not None:
                    logger.info(f'{tools.line_number()} - if line is not None:')
                    for row in line:
                        logger.info(f'{tools.line_number()} - self.execute({row}, {new_file.file_uri})')
                        self.execute(row, new_file.file_uri)

                self._file_allocation_table.loc[(self._file_allocation_table[SHA] == new_file.file_sha) &
                                                (self._file_allocation_table[KIND] == DELETED_PARENT), KIND] = SYMLINK
            else:
                line = line[0]
                logger.info(f'{tools.line_number()} - line_uri = self.get_file({new_file.file_uri}, SYMLINK)')
                line_uri = self.get_file(new_file.file_uri, SYMLINK)
                if line_uri is None:
                    logger.info(f'{tools.line_number()} - if line_uri is None:')
                    logger.info(f'{tools.line_number()} - and is_equal(new_file.file_uri, line_uri, False):')
                    if new_file.file_uri != line and is_equal(new_file.file_uri, line, False):
                        logger.info(f'{tools.line_number()} - if {new_file.file_uri} != {line}')
                        logger.info(f'{tools.line_number()} - if is_equal({new_file.file_uri}, {line}, False):')

                        logger.info(f'{tools.line_number()} - self.new_row({new_file}, kind=SYMLINK)')
                        self.new_row(new_file, kind=SYMLINK)
                        logger.info(f'{tools.line_number()} - self.new_row({new_file}, kind=REMOVED)')
                        self.new_row(new_file, kind=REMOVED)

                        logger.info(f'{tools.line_number()} - self.delete_row({new_file.file_uri})')
                        self.delete_row(new_file.file_uri)
                        logger.info(f'{tools.line_number()} - self.execute({new_file.file_uri}, {line})')
                        self.execute(new_file.file_uri, line)
                else:
                    logger.error(f'{tools.line_number()} - else: {line_uri}')

        logger.info(f'{tools.line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.line_number()} - return')

    def mod_file(self, mod_uri):
        logger.warning(f'{tools.line_number()} - ==============================================')
        logger.warning(f'{tools.line_number()} - def mod_file(self, {mod_uri}):')
        if is_link(mod_uri):
            logger.warning(f'{tools.line_number()} - if is_link({mod_uri}):')
            logger.info(f'{tools.line_number()} - return')
        elif is_dir(mod_uri):
            logger.warning(f'{tools.line_number()} - if is_dir({mod_uri}):')
            logger.warning(f'{tools.line_number()} - return')
        elif is_file(mod_uri):
            logger.info(f'{tools.line_number()} - elif is_file({mod_uri}):')
            new_file = FileHolder(mod_uri)

            logger.info(f'{tools.line_number()} - gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.info(f'{tools.line_number()} - if gotten_by_sha is not None:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.info(f'{tools.line_number()} - if {new_file.file_uri} == {gotten_by_sha[0]}:')
                    logger.info(f'{tools.line_number()} - FILE MOVED - NO HANDLING HERE')
                    logger.info(f'{tools.line_number()} - FILE UNCHANGED {new_file.file_uri}')
                    logger.info(f'{tools.line_number()} - URI    CHANGED {gotten_by_sha[0]}')

            logger.info(f'{tools.line_number()} - gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.info(f'{tools.line_number()} - if gotten_by_uri is not None:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.info(f'{tools.line_number()} - if {new_file.file_sha} != {gotten_by_uri[SHA].values[0]}:')
                    logger.info(f'{tools.line_number()} - CONTENT CHANGED - MUST UPDATE LINKS WHEN APPLICABLE')
                    logger.info(f'{tools.line_number()} - OLD: {gotten_by_uri[SHA].values[0]}')
                    logger.info(f'{tools.line_number()} - NEW: {new_file.file_sha}')
                    self._file_allocation_table.loc[(self._file_allocation_table[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha

            if gotten_by_sha is None and gotten_by_uri is None:
                logger.info(f'{tools.line_number()} - if gotten_by_sha is None and gotten_by_uri is None:')
                logger.info(f'{tools.line_number()} - self.add_file({mod_uri})')
                self.add_file(mod_uri)

        logger.info(f'{tools.line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.line_number()} - return')

    def move_file(self, source_file, target_file):
        logger.warning(f'{tools.line_number()} - ==============================================')
        logger.warning(f'{tools.line_number()} - def move_file(self, {source_file}, {target_file}):')

        if is_link(target_file):
            logger.info(f'{tools.line_number()} - TRUE is_link({target_file}):')
            logger.info(f'{tools.line_number()} - self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file), URI] = target_file')
            self._file_allocation_table.loc[(self._file_allocation_table[URI] == source_file), URI] = target_file

        elif is_file(target_file):
            logger.info(f'{tools.line_number()} - TRUE is_file({target_file}):')
            new_file = FileHolder(target_file)
            logger.info(f'{tools.line_number()} - gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)')
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.info(f'{tools.line_number()} - if gotten_by_uri is not None:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.info(f'{tools.line_number()} - if new_file.file_sha != gotten_by_uri[SHA].values[0]:')
                    # TODO: MUST CHECK IF NEW SHA ALREADY EXIST ON DATABASE
                    # IF NOT EXIST, GO AHEAD
                    # IF EXIST, MANAGE DUPE
                    logger.info(f'{tools.line_number()} - SHA CHANGED FROM {gotten_by_uri[SHA].values[0]}')
                    logger.info(f'{tools.line_number()} - SHA CHANGED TO   {new_file.file_sha}')
                    logger.info(f'{tools.line_number()} - self._file_allocation_table.loc[(self._file_allocation_table[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha')
                    self._file_allocation_table.loc[(self._file_allocation_table[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha

            logger.info(f'{tools.line_number()} - gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)')
            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.info(f'{tools.line_number()} - if gotten_by_sha is not None:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.info(f'{tools.line_number()} - if if new_file.file_uri != gotten_by_sha[0]:')
                    logger.info(f'{tools.line_number()} - URI CHANGED FROM {gotten_by_sha[0]}')
                    logger.info(f'{tools.line_number()} - URI CHANGED TO   {new_file.file_uri}')
                    logger.info(f'{tools.line_number()} - CHANGING MAIN FILE ADDRESS BEFORE')
                    logger.debug(f'{tools.line_number()} - \n{self.print_table()}')
                    self._file_allocation_table.loc[(self._file_allocation_table[URI] == gotten_by_sha[0]) &
                                                    (self._file_allocation_table[KIND] == FILE), URI] = new_file.file_uri
                    logger.info(f'{tools.line_number()} - CHANGING MAIN FILE ADDRESS AFTER')
                    logger.debug(f'{tools.line_number()} - \n{self.print_table()}')
                    self._file_allocation_table.loc[(self._file_allocation_table[SHA] == new_file.file_sha) &
                                                    (self._file_allocation_table[KIND] == SYMLINK), KIND] = MOVED_FILE
                    logger.info(f'{tools.line_number()} - CHANGING LINK FILE ADDRESS')
                    logger.debug(f'{tools.line_number()} - \n{self.print_table()}')
                    line = self.get_files(new_file.file_sha, MOVED_FILE, SHA)
                    if line is not None:
                        logger.info(f'{tools.line_number()} - if line is not None:')
                        for row in line:
                            logger.info(f'{tools.line_number()} - self.delete_row({row})')
                            self.delete_row(row)
                            logger.info(f'{tools.line_number()} - self.execute({row}, {new_file.file_uri})')
                            self.execute(row, new_file.file_uri)
        logger.info(f'{tools.line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.line_number()} - return')

    def del_file(self, del_uri):
        logger.warning(f'{tools.line_number()} - ==============================================')
        logger.warning(f'{tools.line_number()} - def del_file(self, {del_uri}):')

        logger.info(f'{tools.line_number()} - delete_index = self.get_file_index(uri, REMOVED)')
        delete_index = self.get_file_index(del_uri, REMOVED)
        logger.info(f'{tools.line_number()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{tools.line_number()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_allocation_table.drop(delete_index.index, inplace=True)
            logger.info(f'{tools.line_number()} - self.save_data()')
            self.save_data()
            logger.info(f'{tools.line_number()} - return')
            return

        logger.info(f'{tools.line_number()} - delete_index = self.get_file_index(uri, SYMLINK)')
        delete_index = self.get_file_index(del_uri, SYMLINK)
        logger.info(f'{tools.line_number()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{tools.line_number()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_allocation_table.drop(delete_index.index, inplace=True)
            logger.info(f'{tools.line_number()} - self.save_data()')
            self.save_data()
            logger.info(f'{tools.line_number()} - return')
            return

        logger.info(f'{tools.line_number()} - delete_index = self.get_file_index(uri, FILE)')
        delete_index = self.get_file_index(del_uri, FILE)
        logger.info(f'{tools.line_number()} - if delete_index is not None: {delete_index is not None}')
        if delete_index is not None:
            logger.info(f'{tools.line_number()} - self._file_allocation_table.drop(delete_index.index, inplace=True)')
            self._file_allocation_table.drop(delete_index.index, inplace=True)
            sha = delete_index[SHA].values[0]
            logger.info(f'{tools.line_number()} - delete_index = self.get_files(sha, SYMLINK, SHA)')
            delete_index = self.get_files(sha, SYMLINK, SHA)
            logger.info(f'{tools.line_number()} - if delete_index is not None: {delete_index is not None}')
            if delete_index is not None:
                for row in delete_index:
                    logger.warning(f'{tools.line_number()} - self.delete_row({row})')
                    self.delete_row(row)
                self._file_allocation_table.loc[(self._file_allocation_table[SHA] == sha) &
                                                (self._file_allocation_table[KIND] == SYMLINK), KIND] = DELETED_PARENT
        logger.info(f'{tools.line_number()} - self.save_data()')
        self.save_data()
        logger.info(f'{tools.line_number()} - return')


class FileHolder:
    def __init__(self, file_path):
        logger.info(f'{tools.line_number()} - FileHolder {file_path}')
        self._file_uri = file_path
        self._file_sha = None
        self.set_sha()

    @property
    def file_uri(self):
        logger.info(f'{tools.line_number()} - file_uri {self._file_uri}')
        return self._file_uri

    @file_uri.setter
    def file_uri(self, universal_resource_indicator):
        self._file_uri = universal_resource_indicator
        self.set_sha()
        logger.info(f'{tools.line_number()} - file_uri {self._file_sha} {self._file_uri}')

    @property
    def file_sha(self):
        logger.info(f'{tools.line_number()} - file_sha {self._file_sha}')
        return self._file_sha

    def set_sha(self):
        logger.info(f'{tools.line_number()} - set_sha')
        if self._file_uri:
            # TODO: SAVE SHA TO FILETABLE (USE PREVIOUS IF EXIST)
            # digest_method = hashlib.md5()
            # digest_method = hashlib.sha1()
            # digest_method = hashlib.sha256()
            digest_method = hashlib.sha512()
            memory_view = memoryview(bytearray(128*1024))
            retry = True
            while retry:
                try:
                    logger.info(f'{tools.line_number()} - Getting file HASH CODE...')
                    with open(self._file_uri, 'rb', buffering=0) as uri_locator:
                        for element in iter(lambda: uri_locator.readinto(memory_view), 0):
                            digest_method.update(memory_view[:element])
                    self._file_sha = digest_method.hexdigest()
                    retry = False
                except PermissionError as permission_error:
                    logger.error(f'{tools.line_number()} - PermissionError: {permission_error}')
                    time.sleep(1)

    def __repr__(self):
        logger.info(f'{tools.line_number()} - def __repr__(self):')
        logger.info(f'{tools.line_number()} - return self._file_uri')
        return self._file_uri


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        file_list.add_file(event.src_path)
        logger.debug(f'{tools.line_number()} - ON_CREATED\n{file_list}')

    def on_modified(self, event):
        file_list.mod_file(event.src_path)
        logger.debug(f'{tools.line_number()} - ON_MODIFIED\n{file_list}')

    def on_moved(self, event):
        file_list.move_file(event.src_path, event.dest_path)
        logger.debug(f'{tools.line_number()} - ON_MOVED\n{file_list}')

    def on_deleted(self, event):
        file_list.del_file(event.src_path)
        logger.debug(f'{tools.line_number()} - ON_DELETED\n{file_list}')


if __name__ == "__main__":
    log_handler = [RotatingFileHandler("UnDupyKeeper.log", maxBytes=10000000, backupCount=20000)]
    log_format = '%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s'
    log_formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    date_format = '%H:%M:%S'
    debug_level = "WARNING"

    logging.basicConfig(format=log_format,
                        datefmt=date_format,
                        level=debug_level,
                        handlers=log_handler)

    console = logging.StreamHandler()
    console.setLevel(debug_level)
    console.setFormatter(log_formatter)
    logging.getLogger().addHandler(console)

    logging.getLogger("watchdog").setLevel(logging.CRITICAL)

    src_path = sys.argv[1]

    logger.warning(f'{tools.line_number()} - Starting UnDupyKeeper System...')
    file_list = FileList()

    file_list.update_links()
    file_list.update_files()
    for root, dirs, files in os.walk(src_path, topdown=True):
        for name in files:
            uri = str(os.path.join(root, name))
            if uri_exists(uri):
                file_list.add_file(uri)
    file_list.update_junk()

    event_handler = MonitorFolder()
    observer = Observer()
    observer.schedule(event_handler, path=src_path, recursive=True)

    observer.start()

    WORKING = True
    try:
        keyboard = tools.KBHit()
        while WORKING:
            time.sleep(1)
            if keyboard.check():
                file_list.save_data(True)
                logger.warning(f'{tools.line_number()} - Terminating UnDupyKeeper System...')
                WORKING = False

        observer.stop()
        observer.join()
    except KeyboardInterrupt as keyboard_interrupt:
        logger.info(f'{tools.line_number()} -  KeyboardInterrupt: [{keyboard_interrupt}]')
        observer.stop()
        observer.join()
