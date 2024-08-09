import os
import argparse
import constants
from tqdm import tqdm
from methods import timed
from methods import get_hash
from methods import get_level

from methods import section_line

from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError

import logging
show = logging.getLogger(constants.DEBUG_MAIN)


mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
# mongo_collection = mongo_database[constants.DATABASE_COLLECTION]
mongo_collection = mongo_database['sandbox']


def status():
    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    document_count = mongo_collection.count_documents({})
    print(f'DOCUMENT COUNT [{document_count:,}]')
    total_length = mongo_collection.aggregate([{"$project": {"file_list_length": {"$size": "$file_list"}}}, {"$group": {"_id": None, "total_length": {"$sum": "$file_list_length"}}}])
    result = list(total_length)
    if result:
        print(f"FILE COUNT     [{result[0]['total_length']:,}]")
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')


@timed
def count_files(target_directory):
    total_files = 0
    for root, dirs, files in tqdm(os.walk(target_directory), desc="SCANNING"):
        total_files += len(files)
    return total_files


@timed
def hash_directory_files(current_directory):
    print(f"SCANNING FILES: [{current_directory}]")
    status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
    total_files = count_files(current_directory)
    with tqdm(total=total_files, bar_format=status_bar_format) as tqdm_progress_bar:
        cdir = ''
        old_count = 0
        new_count = 0
        hash_count = 0
        database_file_count = 0
        for root, dirs, files in os.walk(current_directory):
            for file in files:
                database_file_count += 1
                level = get_level(root, 3)
                if cdir != level:
                    cdir = level
                    tqdm_progress_bar.set_postfix({'DIR': cdir})
                tqdm_progress_bar.update(1)
                file_name = os.path.join(root, file)
                file_name = os.path.normpath(file_name)
                file_hash = get_hash(file_name, constants.HASH_MD5)
                item_found = mongo_collection.find_one({constants.DOC_ID: file_hash})
                if item_found:
                    if file_hash not in item_found[constants.FILE_LIST]:
                        if file_name not in item_found[constants.FILE_LIST]:
                            item_found[constants.FILE_LIST].append(file_name)
                            mongo_collection.update_one({constants.DOC_ID: file_hash}, {'$set': {constants.FILE_LIST: item_found[constants.FILE_LIST]}}, upsert=True)
                            new_count += 1
                        else:
                            old_count += 1
                    else:
                        print(f'!!! [{file_hash}][{file_name}] already exists.')
                else:
                    try:
                        mongo_collection.insert_one({constants.DOC_ID: file_hash, constants.FILE_SIZE: os.path.getsize(file_name), constants.FILE_LIST: [file_name]})
                        hash_count += 1
                    except DuplicateKeyError as duplicate_key_error:
                        print(f'!!! [{duplicate_key_error}] already exists.')
    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    print(f'TOTAL FILES    [{total_files:,}]')
    print(f'TOTAL COUNT    [{database_file_count:,}]')
    print(f'INSERT COUNT   [{hash_count:,}]')
    print(f'UPDATE COUNT   [{new_count:,}]')
    print(f'NOFILE COUNT   [{old_count:,}]')
    print(f'INSERT UPDATE  [{new_count+hash_count+old_count:,}]')
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')
    status()

    # mongo_collection.delete_many({})


if __name__ == '__main__':
    argument_parser = argparse.ArgumentParser(description='Hash files in a directory and identify duplicates.')
    argument_parser.add_argument('-d', '--directory', type=str, default=None, help='Directory to scan for files')
    arguments = argument_parser.parse_args()
    hash_directory_files(arguments.directory)
