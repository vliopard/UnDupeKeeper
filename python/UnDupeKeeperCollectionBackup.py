import json
import argparse
import constants
from methods import timed
from pymongo import MongoClient
from methods import section_line
from pymongo.errors import DuplicateKeyError

mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]
mongo_stats = mongo_database[constants.DATABASE_STATUS]


@timed
def duplicate_collection(collection_name):
    new_collection = mongo_database[f'{constants.DATABASE_COLLECTION}_{collection_name}']
    new_collection.insert_many(mongo_collection.find())


@timed
def list_collections(size):
    collection_list = mongo_database.list_collection_names()
    print('LIST OF COLLECTIONS:')
    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    for item in sorted(collection_list):
        if item == constants.DATABASE_STATUS:
            continue
        collection = mongo_database[item]
        if size:
            stats_db = get_status(item)
            if not stats_db:
                stats = mongo_database.command("collstats", item)
                size_bytes = stats['size']
                count = collection.count_documents({})
                add_status({constants.DOC_ID: item, 'size': size_bytes, 'count': count})
            else:
                size_bytes = stats_db['size']
                count = stats_db['count']
            size_bytes_str = f'{size_bytes:,}'
            count_str = f'{count:,}'

            print(f'{constants.DATABASE_NAME}:[ {item.rjust(30)} ] [{count_str.rjust(9)}] [{size_bytes_str.rjust(13)}]')
        else:
            print(f'{constants.DATABASE_NAME}:[ {item.rjust(30)} ]')
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')


def add_status(document):
    try:
        mongo_stats.insert_one({constants.DOC_ID: document[constants.DOC_ID], 'size': document['size'], 'count': document['count']})
    except DuplicateKeyError as duplicate_key_error:
        print(f'[{duplicate_key_error}]')


def get_status(document_id):
    return mongo_stats.find_one({constants.DOC_ID: document_id})


def del_status(item_id):
    result = mongo_stats.delete_one({constants.DOC_ID: item_id})
    return result.deleted_count == 1


@timed
def delete_collection(collection_name):
    print(f'DELETING [{collection_name}]')
    mongo_database[collection_name].drop()
    del_status(collection_name)


@timed
def export_collection_to_json():
    with open('MongoDB_UnDupeKeeperCollection_backup.json', 'w') as file:
        json.dump(list(mongo_collection.find({})), file, default=str, indent=4)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create a collection backup')
    parser.add_argument('-n', '--collection_name', type=str, help='New backup name')
    parser.add_argument('-l', '--collection_list', action='store_true', help='List collections')
    parser.add_argument('-s', '--collection_size', action='store_true', help='List collections')
    parser.add_argument('-e', '--collection_export', action='store_true', help='List collections')
    parser.add_argument('-d', '--collection_delete', type=str, help='Delete collection')
    args = parser.parse_args()
    if args.collection_list:
        list_collections(args.collection_size)
    elif args.collection_name:
        duplicate_collection(args.collection_name)
    elif args.collection_delete:
        delete_collection(args.collection_delete)
    elif args.collection_export:
        export_collection_to_json()
    else:
        print('No valid option.')
