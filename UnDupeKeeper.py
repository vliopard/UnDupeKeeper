import sys
import time
import hashlib

import pandas as pd

from filecmp import cmp as is_equal
from os import remove as delete_file

from os.path import exists
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

SHA = 'SHA'
URI = 'URI'
KIND = 'KIND'

REMO = 'remo'
LINK = 'link'
LYNK = 'lynk'
FILE = 'file'
MOVE = 'move'


def get_platform():
    platforms = {'linux': 'Linux',
                 'linux1': 'Linux',
                 'linux2': 'Linux',
                 'darwin': 'OS X',
                 'win32': 'Windows'}
    if sys.platform not in platforms:
        return sys.platform
    return platforms[sys.platform]


def execute(target, source):
    ret_val = None

    source_is_file = is_file(source)
    target_exists = exists(target)
    if source_is_file and not target_exists:
        platform = {"Linux": f'ln -s {source} {target}',
                    "Windows": f'mklink {target} {source}'}
        command = platform[get_platform()]

        try:
            process = run_command(command,
                                  shell=True,
                                  check=True,
                                  stdout=PIPE,
                                  universal_newlines=True)
            ret_val = process.stdout
        except CalledProcessError as cp:
            logger.error(f'CalledProcessError {cp}')
            logger.error(f'Source is File? [{source_is_file}], Target Exists? [{target_exists}]')
    else:
        logger.error(f'ERROR: Source is File? [{source_is_file}], Target Exists? [{target_exists}]')

    return ret_val


class FileList:
    def __init__(self):
        self._save_count = 0
        self._file_list = pd.DataFrame(columns=[SHA, KIND, URI])
        self.load_data()

    def __repr__(self):
        return self.print_list()

    def print_list(self):
        return_string = ''
        for index, row in self._file_list.iterrows():
            return_string += f'DUPETABLE: {row[SHA]} {row[KIND]} {row[URI]}\n'
        return return_string

    def save_data(self):
        with open('file_table.txt', 'w') as ft:
            idx = self._file_list.loc[self._file_list[KIND] == FILE]
            if idx is not None and not idx.empty:
                for value in idx[URI].values:
                    ft.write(value + '\n')
                ft.flush()

        with open('file_links.txt', 'w') as fl:
            idx = self._file_list.loc[self._file_list[KIND] == LINK]
            if idx is not None and not idx.empty:
                for value in idx[URI].values:
                    fl.write(value + '\n')
                fl.flush()
            idx = self._file_list.loc[self._file_list[KIND] == LYNK]
            if idx is not None and not idx.empty:
                for value in idx[URI].values:
                    fl.write(value + '\n')
                fl.flush()

        self._save_count += 1
        if self._save_count > 10:
            self._file_list.to_pickle("database.pkl")
            self._save_count = 0

    def load_data(self):
        try:
            self._file_list = pd.read_pickle("database.pkl")
        except Exception as e:
            logger.debug(f'FILE NOT FOUND: {e}')

    def new_row(self, new_file, kind):
        new_row = {SHA: new_file.file_sha,
                   KIND: kind,
                   URI: new_file.file_uri}
        self._file_list = self._file_list.append(new_row, ignore_index=True)

    def get_file_index(self, file_uri, kind):
        if file_uri in self._file_list[URI].values:
            idx = self._file_list.loc[(self._file_list[URI] == file_uri) &
                                      (self._file_list[KIND] == kind)]
            if idx is not None and not idx.empty:
                return idx
        return None

    def get_file(self, file_uri, kind):
        idx = self.get_file_index(file_uri, kind)
        if idx is not None and not idx.empty:
            return str(idx[URI].values[0])
        return None

    def get_files(self, key_index, kind, idx):
        if key_index in self._file_list[idx].values:
            idx = self._file_list.loc[(self._file_list[idx] == key_index) &
                                      (self._file_list[KIND] == kind)]
            if idx is not None and not idx.empty:
                return idx[URI].values
        return None

    def add_file(self, uri):
        logger.debug('==============================================')
        logger.debug(f'def add_file(self, {uri}):')

        if is_link(uri):
            logger.debug(f'if is_link({uri}): TRUE - RETURNING FROM ADD')
            new_file = FileHolder(uri)
            line = self.get_files(new_file.file_sha, FILE, SHA)
            logger.debug(f'if {line} is None:')
            if line is not None:
                line = self.get_files(new_file.file_uri, LINK, URI)
                if line is None:
                    self.new_row(new_file, LINK)

            self._file_list.loc[(self._file_list[URI] == uri) &
                                (self._file_list[KIND] == MOVE), KIND] = LINK
        elif is_file(uri):
            logger.debug(f'if is_file({uri}):')
            new_file = FileHolder(uri)
            line = self.get_files(new_file.file_sha, FILE, SHA)
            logger.debug(f'if {line} is None:')
            if line is None:
                logger.debug(f'self.new_row(new_file)')
                self.new_row(new_file, FILE)

                line = self.get_files(new_file.file_sha, LYNK, SHA)
                if line is not None:
                    for row in line:
                        execute(row, new_file.file_uri)

                self._file_list.loc[(self._file_list[SHA] == new_file.file_sha) &
                                    (self._file_list[KIND] == LYNK), KIND] = LINK
            else:
                line = line[0]
                logger.debug(f'line = self.get_file({new_file.file_uri}, "link")')
                line_uri = self.get_file(new_file.file_uri, LINK)
                logger.debug(f'if line_uri is None:')
                if line_uri is None:
                    logger.debug(f'if line_uri is None:')
                    logger.debug(f'if {new_file.file_uri} != {line}')
                    logger.debug(f'and is_equal(new_file.file_uri, line_uri, False):')
                    if new_file.file_uri != line and is_equal(new_file.file_uri, line, False):
                        self.new_row(new_file, kind=LINK)
                        self.new_row(new_file, kind=REMO)
                        logger.debug(f'delete_file({new_file.file_uri})')
                        delete_file(new_file.file_uri)
                        execute(new_file.file_uri, line)
                else:
                    logger.debug(f'else:')
                    logger.debug(f'{line_uri}')
        self.save_data()

    def mod_file(self, uri):
        logger.debug('==============================================')
        logger.debug(f'def mod_file(self, {uri}):')
        if is_link(uri):
            logger.debug(f'if is_link({uri}): TRUE - RETURNING FROM MOD')
        elif is_file(uri):
            logger.debug(f'if is_file({uri}):')
            new_file = FileHolder(uri)

            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                logger.debug(f'if gotten_by_sha is not None:')
                logger.debug(f'if {new_file.file_uri} == {gotten_by_sha[0]}:')
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.debug(f'FILE MOVED - NO HANDLING HERE')
                    logger.debug(f'FILE UNCHANGED {new_file.file_uri}')
                    logger.debug(f'URI CHANGED {gotten_by_sha[0]}')

            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                logger.debug(f'if gotten_by_uri is not None:')
                logger.debug(f'if {new_file.file_sha} == {gotten_by_uri[SHA].values[0]}:')
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    logger.debug(f'CONTENT CHANGED - MUST UPDATE LINKS WHEN APPLICABLE')
                    logger.debug(f'OLD: {gotten_by_uri[SHA].values[0]}')
                    logger.debug(f'NEW: {new_file.file_sha}')
                    self._file_list.loc[(self._file_list[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha
        self.save_data()

    def move_file(self, source, target):
        logger.debug('==============================================')
        logger.debug(f'def move_file(self, {source}, {target}):')

        if is_link(target):
            logger.debug(f'if is_link({target}):')
            self._file_list.loc[(self._file_list[URI] == source), URI] = target

        elif is_file(target):
            logger.debug(f'elif is_file({target}):')
            new_file = FileHolder(target)
            gotten_by_uri = self.get_file_index(new_file.file_uri, FILE)
            if gotten_by_uri is not None:
                if new_file.file_sha != gotten_by_uri[SHA].values[0]:
                    # TODO: MUST CHECK IF NEW SHA ALREADY EXIST ON DATABASE
                    # IF NOT EXIST, GO AHEAD
                    # IF EXIST, MANAGE DUPE
                    logger.debug(f'SHA CHANGED')
                    logger.debug(f'SHA CHANGED FROM {gotten_by_uri[SHA].values[0]}')
                    logger.debug(f'SHA CHANGED TO   {new_file.file_sha}')
                    self._file_list.loc[(self._file_list[SHA] == gotten_by_uri[SHA].values[0]), SHA] = new_file.file_sha

            gotten_by_sha = self.get_files(new_file.file_sha, FILE, SHA)
            if gotten_by_sha is not None:
                if new_file.file_uri != gotten_by_sha[0]:
                    logger.debug(f'URI CHANGED FROM {gotten_by_sha[0]}')
                    logger.debug(f'URI CHANGED TO   {new_file.file_uri}')
                    logger.debug(f'CHANGING MAIN FILE ADDRESS BEFORE')
                    print(f'{self.print_list()}')
                    self._file_list.loc[(self._file_list[URI] == gotten_by_sha[0]) &
                                        (self._file_list[KIND] == FILE), URI] = new_file.file_uri
                    logger.debug(f'CHANGING MAIN FILE ADDRESS AFTER')
                    print(f'{self.print_list()}')
                    self._file_list.loc[(self._file_list[SHA] == new_file.file_sha) &
                                        (self._file_list[KIND] == LINK), KIND] = MOVE
                    logger.debug(f'CHANGING LINK FILE ADDRESS')
                    print(f'{self.print_list()}')
                    line = self.get_files(new_file.file_sha, MOVE, SHA)
                    if line is not None:
                        logger.debug(f'if line is not None:')
                        for row in line:
                            logger.debug(f'delete_file({row})')
                            delete_file(row)
                            logger.debug(f'CREATING LINK ({new_file.file_uri})')
                            execute(row, new_file.file_uri)
        self.save_data()

    def del_file(self, file_uri):
        logger.debug('==============================================')
        logger.debug(f'def del_file(self, {file_uri}):')
        idx = self.get_file_index(file_uri, REMO)
        logger.debug(f'if {idx} is not None:')
        if idx is not None:
            logger.debug(f'self._file_list.drop({idx.values}, inplace=True)')
            self._file_list.drop(idx.index, inplace=True)
            self.save_data()
            return

        idx = self.get_file_index(file_uri, LINK)
        logger.debug(f'if {idx} is not None:')
        if idx is not None:
            logger.debug(f'self._file_list.drop({idx.values}, inplace=True)')
            self._file_list.drop(idx.index, inplace=True)
            self.save_data()
            return

        idx = self.get_file_index(file_uri, FILE)
        if idx is not None:
            self._file_list.drop(idx.index, inplace=True)
            sha = idx[SHA].values[0]
            idx = self.get_files(sha, LINK, SHA)
            if idx is not None:
                for row in idx:
                    delete_file(row)
                self._file_list.loc[(self._file_list[SHA] == sha) &
                                    (self._file_list[KIND] == LINK), KIND] = LYNK
        self.save_data()


class FileHolder:
    def __init__(self, file_path):
        self._file_uri = file_path
        self._file_sha = None
        self.set_sha()

    @property
    def file_uri(self):
        return self._file_uri

    @file_uri.setter
    def file_uri(self, uri):
        self._file_uri = uri
        self.set_sha()

    @property
    def file_sha(self):
        return self._file_sha

    def set_sha(self):
        if self._file_uri:
            digest_method = hashlib.md5()
            # digest_method = hashlib.sha1()
            # digest_method = hashlib.sha256()
            # digest_method = hashlib.sha512()
            memory_view = memoryview(bytearray(128*1024))
            retry = True
            while retry:
                try:
                    with open(self._file_uri, 'rb', buffering=0) as uri:
                        for element in iter(lambda: uri.readinto(memory_view), 0):
                            digest_method.update(memory_view[:element])
                    self._file_sha = digest_method.hexdigest()
                    retry = False
                except PermissionError as pe:
                    time.sleep(1)

    def __repr__(self):
        return self._file_uri


file_list = FileList()


class MonitorFolder(FileSystemEventHandler):

    def on_created(self, event):
        file_list.add_file(event.src_path)
        print(f'{file_list}')

    def on_modified(self, event):
        file_list.mod_file(event.src_path)
        print(f'{file_list}')

    def on_moved(self, event):
        file_list.move_file(event.src_path, event.dest_path)
        print(f'{file_list}')

    def on_deleted(self, event):
        file_list.del_file(event.src_path)
        print(f'{file_list}')


if __name__ == "__main__":

    logging.basicConfig(handlers=[RotatingFileHandler("log.log",
                                                      maxBytes=10000000,
                                                      backupCount=20000)],
                        format='%(asctime)s,%(msecs)03d %(name)s\t %(levelname)s %(message)s',
                        datefmt='%H:%M:%S',
                        level="DEBUG")

    console = logging.StreamHandler()
    console.setLevel("DEBUG")
    formatter = logging.Formatter('%(name)-12s: %(levelname)-8s %(message)s')
    console.setFormatter(formatter)
    logging.getLogger().addHandler(console)

    src_path = sys.argv[1]

    event_handler = MonitorFolder()
    observer = Observer()
    observer.schedule(event_handler, path=src_path, recursive=True)

    observer.start()

    try:
        while True:
            time.sleep(0.25)

    except KeyboardInterrupt:
        observer.stop()
        observer.join()
