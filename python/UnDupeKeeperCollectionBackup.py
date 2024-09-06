import json
import argparse
import constants
from tqdm import tqdm
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


def get_db_size(collection_name):
    collection_cursor = mongo_database[collection_name]
    stats = mongo_database.command("collstats", collection_name)
    return {constants.DOC_ID: collection_name, 'size': stats['size'], 'count': collection_cursor.count_documents({})}


@timed
def list_collections(size):
    collection_list = mongo_database.list_collection_names()

    si = mongo_client.server_info()
    operating_system = si['targetMinOS']

    print(f'LIST OF COLLECTIONS: [{operating_system}]')
    print(f'{section_line(constants.SYMBOL_UNDERLINE, constants.LINE_LEN)}')
    for collection_name in sorted(collection_list):
        if collection_name == constants.DATABASE_STATUS:
            continue
        if size:
            stats_db = get_status(collection_name)
            if not stats_db:
                add_status_item = get_db_size(collection_name)
                size_bytes = add_status_item['size']
                count = add_status_item['count']
                add_status(add_status_item)
            else:
                size_bytes = stats_db['size']
                count = stats_db['count']
            size_bytes_str = f'{size_bytes:,}'
            count_str = f'{count:,}'

            print(f'{constants.DATABASE_NAME}:[ {collection_name.rjust(30)} ] [{count_str.rjust(9)}] [{size_bytes_str.rjust(13)}]')
        else:
            print(f'{constants.DATABASE_NAME}:[ {collection_name.rjust(30)} ]')
    print(f'{section_line(constants.SYMBOL_OVERLINE, constants.LINE_LEN)}')


def get_status(document_id):
    return mongo_stats.find_one({constants.DOC_ID: document_id})


def add_status(add_doc):
    try:
        mongo_stats.insert_one({constants.DOC_ID: add_doc[constants.DOC_ID], 'size': add_doc['size'], 'count': add_doc['count']})
    except DuplicateKeyError as duplicate_key_error:
        print(f'[{duplicate_key_error}]')


def del_status(item_id):
    result = mongo_stats.delete_one({constants.DOC_ID: item_id})
    return result.deleted_count == 1


def update_status(collection_id):
    collection_info = get_db_size(collection_id)
    mongo_stats.update_one({'_id': collection_id}, {'$set': {'count': collection_info['count'], 'size': collection_info['size']}})


@timed
def delete_collection(collection_name):
    print(f'DELETING [{collection_name}]')
    mongo_database[collection_name].drop()
    del_status(collection_name)


@timed
def export_collection_to_json():
    with open('MongoDB_UnDupeKeeperCollection_backup.json', 'w') as file:
        json.dump(list(mongo_collection.find({})), file, default=str, indent=4)


@timed
def export_collection_to_json_progress():
    print(f'GENERATING JSON BACKUP... [{constants.DATABASE_COLLECTION}]')
    with open('MongoDB_UnDupeKeeperCollection_backup.json', 'w') as file:
        stats_db = get_status(constants.DATABASE_COLLECTION)
        if not stats_db:
            count = mongo_collection.count_documents({})
        else:
            count = stats_db['count']
        with tqdm(total=int(count), bar_format=constants.STATUS_BAR_FORMAT) as tqdm_progress_bar:
            for item in mongo_collection.find({}):
                json.dump(item, file, default=str, indent=4)
                tqdm_progress_bar.update(1)
    print('DONE.')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Create a collection backup')
    parser.add_argument('-n', '--collection_name', type=str, help='New backup name')
    parser.add_argument('-l', '--collection_list', action='store_true', help='List collections')
    parser.add_argument('-s', '--collection_size', action='store_true', help='List collections with size')
    parser.add_argument('-e', '--collection_export', action='store_true', help='Export collection')
    parser.add_argument('-p', '--collection_progress', action='store_true', help='Export collection with progress')
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
    elif args.collection_progress:
        export_collection_to_json_progress()
    else:
        print('No valid option.')
