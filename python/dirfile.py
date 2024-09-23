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
    results = mongo_collection.find({'file_list': {'$regex': search_pattern, '$options': 'i'}})

    ct = 0
    with open('dirfile.txt', 'w', encoding='utf-8') as df:
        for result in results:
            for file_path in result['file_list']:
                fp = file_path.split('\\')
                ct += 1
                saved = f'[{ct}]\t{fp[-1]}\t{"\\".join(fp[:-1])}\\'
                print(saved)
                df.write(f'{saved}\n')
    print('[Done.]')

if __name__ == "__main__":
    argument_parser = argparse.ArgumentParser()
    argument_parser.add_argument('--words', nargs='+', required=True, help='List of words')
    argument_parser.add_argument('--extension', required=False, help='Extension name')    
    arguments = argument_parser.parse_args()
    list_of_words = arguments.words
    file_extension = arguments.extension
    search_file(list_of_words, file_extension)
