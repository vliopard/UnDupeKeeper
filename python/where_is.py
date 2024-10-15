import argparse
import constants
from methods import get_hash
from pymongo import MongoClient


def calculate_file_sha(filename):
    return get_hash(filename, constants.HASH_MD5)


def lookup_mongodb_by_sha(file_sha):
    mongo_client = MongoClient(constants.DATABASE_URL)
    mongo_database = mongo_client[constants.DATABASE_NAME]
    mongo_collection = mongo_database[constants.DATABASE_COLLECTION]
    return mongo_collection.find({constants.DOC_ID: file_sha})


def main(filename):
    file_results = lookup_mongodb_by_sha(calculate_file_sha(filename))
    if file_results:
        for file_result in file_results:
            print(f'File found:')
            print(f'MD5  [{file_result[constants.DOC_ID].upper()}]')
            print(f'SIZE [{file_result[constants.FILE_SIZE]:,} bytes]')
            print(f'Places:')
            for file_place in file_result[constants.FILE_LIST]:
                print(f'     {file_place}')
    else:
        print('File not found.')


if __name__ == constants.MAIN:
    parser = argparse.ArgumentParser(description='Search for a file.')
    parser.add_argument('filename', help='The path to the file')
    args = parser.parse_args()
    main(args.filename)
