import argparse
import constants
from methods import timed
from pymongo import MongoClient
from methods import section_line

mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]


@timed
def duplicate_collection(collection_name):
    new_collection = mongo_database[f'{constants.DATABASE_COLLECTION}_{collection_name}']
    new_collection.insert_many(mongo_collection.find())


def list_collections():
    collection_list = mongo_database.list_collection_names()
    print('LIST OF COLLECTIONS:')
    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    for item in collection_list:
        print(f'{constants.DATABASE_NAME}:[ {item} ]')
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')

def delete_collection(collection_name):
    print(f'DELETING [{collection_name}]')
    mongo_database[collection_name].drop()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create a collection backup')
    parser.add_argument('-n', '--collection_name', type=str, help='New backup name')
    parser.add_argument('-l', '--collection_list', action='store_true', help='List collections')
    parser.add_argument('-d', '--collection_delete', type=str, help='Delete collection')
    args = parser.parse_args()
    if args.collection_list:
        list_collections()
    elif args.collection_name:
        duplicate_collection(args.collection_name)
    elif args.collection_delete:
        delete_collection(args.collection_delete)
    else:
        print('No valid option.')
