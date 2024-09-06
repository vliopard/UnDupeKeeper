import screen
import configparser
from getpass import getpass

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
SYMBOL_OVERLINE = '‾'
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

UTF8 = 'UTF-8'

DOC_ID = '_id'
FILE_LIST = 'file_list'
FILE_SIZE = 'file_size'
FILE_COUNT = 'file_list_count'
TOTAL_COUNT = 'total_count'

NEW_LINE = '\n'
DOS_SLASH = '\\'
UNIX_SLASH = '/'

DOT = '.'
EMPTY = ''
READ = 'r'
WRITE = 'w'
READ_BINARY = 'rb'
READ_WRITE_BINARY = 'r+b'

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
HASH_MD5_FAST = 'Hash_MD5_Fast'
HASH_MD5_CHUNK = 'Hash_MD5_Chunk'

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
LINE_LEN = screen.get_terminal_width() - 35
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

DATABASE_NAME = 'UnDupeKeeperDatabase'
DATABASE_COLLECTION = 'UnDupeKeeperFiles'
DATABASE_STATUS = 'UnDupeKeeperStatus'
DATABASE_UNDUPE = 'UnDupyKeeperFiles'

SETTINGS_FILE = 'UnDupeKeeper.ini'
STORAGE_FILE = 'UnDupeKeeper.json'
COUNTER_FILE = 'UnDupeKeeperCount.json'

COMPARISON_REPORT = 'comparison_test.xlsx'

config = configparser.ConfigParser()
config.read(SETTINGS_FILE)

UI = config.get('VALUES', 'UI')
MAX_FILES = config.getint('VALUES', 'MAX_FILES')
BUFFER_SIZE = config.getint('VALUES', 'BUFFER_SIZE')
COMPARISON_METHOD = config.get('VALUES', 'COMPARISON_METHOD')

MAIN_PATH = config.get('PATHS', 'MAIN_PATH')
TARGET_PATH = config.get('PATHS', 'TARGET_PATH')
LOG_FILE = config.get('PATHS', 'LOG_FILE')
LINK_TABLE = config.get('PATHS', 'LINK_TABLE')
FILE_TABLE = config.get('PATHS', 'FILE_TABLE')

DEBUG_LEVEL = config.get('DEBUG', 'DEBUG_LEVEL')
DEBUG_TEST = config.getboolean('DEBUG', 'DEBUG_TEST')

MONGO_USERNAME = config.get('SECURITY', 'USERNAME')
MONGO_PASSWORD = config.get('SECURITY', 'PASSWORD')
if not MONGO_PASSWORD:
    # MONGO_PASSWORD = getpass('Enter database password: ')
    MONGO_PASSWORD = input('Enter database password: ')
MONGO_HOST = config.get('DATABASE', 'HOST')
MONGO_PORT = config.get('DATABASE', 'PORT')
DATABASE_URL = f'mongodb://{MONGO_USERNAME}:{MONGO_PASSWORD}@{MONGO_HOST}:{MONGO_PORT}/'

DEBUG_BASE = 'UnDupyKeeper'
DEBUG_MAIN = f'{DEBUG_BASE}_MAIN'
DEBUG_DATA = f'{DEBUG_BASE}_DATA'
DEBUG_COPY = f'{DEBUG_BASE}_COPY'
DEBUG_CORE = f'{DEBUG_BASE}_CORE'
DEBUG_HASH = f'{DEBUG_BASE}_HASH'
DEBUG_COMP = f'{DEBUG_BASE}_DIR'
DEBUG_DASH = f'{DEBUG_BASE}_DASH'
DEBUG_UTIL = f'{DEBUG_BASE}_UTIL'
DEBUG_DIFF = f'{DEBUG_BASE}_DIFF'

CLEAR_CACHE = False

test_magnitude = 1000

test_directory = 'files'
test_files = [f'{test_directory}/file0000005.bin',
              f'{test_directory}/file0000050.bin',
              f'{test_directory}/file0000100.bin',
              f'{test_directory}/file0000500.bin',
              f'{test_directory}/file0001500.bin',
              f'{test_directory}/file0005000.bin',
              f'{test_directory}/file0010000.bin',
              f'{test_directory}/file0050000.bin',
              f'{test_directory}/file0150000.bin',
              f'{test_directory}/file0300000.bin',
              f'{test_directory}/file1000000.bin']

STATUS_BAR_FORMAT = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
