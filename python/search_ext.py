import argparse
import constants
from pymongo import MongoClient

mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]


def search_file(extension):
    extension = [item.replace('.', r'\.') for item in extension]
    search_pattern = r'(?<=[\/\\])(?!.*[\/\\]).*(' + '|'.join(extension) + r')$'
    print(f'Searching for [ {search_pattern} ]')
    results = mongo_collection.find({constants.FILE_LIST: {'$regex': search_pattern, '$options': 'i'}})

    file_counter = 0
    with open('search_result.txt', constants.WRITE, encoding=constants.UTF8) as directory_file:
        for result in results:
            source_file = sorted(result[constants.FILE_LIST])[0]
            split_file_path = constants.DOS_SLASH.join(source_file.split(constants.DOS_SLASH))
            file_counter += 1
            print(split_file_path)
            directory_file.write(f'{split_file_path}\n')
    print(f'TOTAL: [{file_counter}]')
    print('[Done]')


if __name__ == constants.MAIN:
    argument_parser = argparse.ArgumentParser()
    argument_parser.add_argument('--extensions', nargs=constants.ALL_ARGUMENTS, required=True, help='List of extensions to search')
    arguments = argument_parser.parse_args()
    file_extensions = arguments.extensions
    search_file(file_extensions)
