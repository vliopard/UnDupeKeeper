import argparse
import constants
from pymongo import MongoClient

mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]


def search_file(words, extension):    
    search_pattern = r'(?<=[\/\\])(?!.*[\/\\]).*(' + '|'.join(words) + r')'
    if extension:
        search_pattern += r'.*' + extension + '$'

    print(f'Searching for [ {search_pattern} ]')
    results = mongo_collection.find({constants.FILE_LIST: {'$regex': search_pattern, '$options': 'i'}})

    file_counter = 0
    with open('search_result.txt', constants.WRITE, encoding=constants.UTF8) as directory_file:
        for result in results:
            for file_path in result[constants.FILE_LIST]:
                split_file_path = file_path.split(constants.DOS_SLASH)
                file_counter += 1
                joined_txt = constants.DOS_SLASH.join(split_file_path[:-1])
                search_result = f'[{file_counter}]\t{split_file_path[-1]}\t{joined_txt}\\'
                print(search_result)
                directory_file.write(f'{search_result}\n')
    print('[Done]')


if __name__ == constants.MAIN:
    argument_parser = argparse.ArgumentParser()
    argument_parser.add_argument('--words', nargs=constants.ALL_ARGUMENTS, required=True, help='List of words to search')
    argument_parser.add_argument('--extension', required=False, help='File extension')
    arguments = argument_parser.parse_args()
    list_of_words = arguments.words
    file_extension = arguments.extension
    search_file(list_of_words, file_extension)
