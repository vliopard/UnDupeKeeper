import os
import json
import constants
from tqdm import tqdm
from methods import timed
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError, BulkWriteError

import logging
show = logging.getLogger(constants.DEBUG_DATA)


mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]


def upsert(result, document_id):
    if result.upserted_id is not None:
        print(f'[{document_id}] INSERTED.')
    elif result.modified_count > 0:
        print(f'[{document_id}] UPDATED.')
    else:
        print(f'[{document_id}] ERROR.')


def get_list_order_by_length(desc=True):
    # mongo_collection.count_documents({})
    # mongo_collection.aggregate([{"$project": {"count": {"$size": "$file_list"}}}, {"$group": {"_id": "null", "totalCount": {"$sum": "$count"}}}])
    # mongo_collection.aggregate([{"$project": {FILE_LIST: 1, FILE_SIZE: 1, FILE_COUNT: {"$size": "$file_list"}}}, {"$sort": {FILE_COUNT: -1 if desc else 1}}])
    # db.getCollection('UnDupeKeeperFiles').aggregate([ {$project: { "file_list": 1, "file_list_count": { $size: "$file_list" } }}, {$sort: {"file_list_count": -1}}])
    documents = mongo_collection.aggregate([{"$project": {constants.FILE_LIST: 1, constants.FILE_SIZE: 1, constants.FILE_COUNT: {"$size": "$file_list"}}}, {"$sort": {constants.FILE_COUNT: -1 if desc else 1}}])
    document_counter = 0
    for document in documents:
        document_counter += 1
        print(document)
        if document_counter > 10:
            break


@timed
def update_database():
    with open(constants.STORAGE_FILE, constants.READ, encoding=constants.UTF8) as md5:
        file_data = json.load(md5)
        for document_id in file_data:
            result = mongo_collection.update_one({constants.DOC_ID: document_id}, {'$set': {constants.FILE_SIZE: os.path.getsize(file_data[document_id][0]), constants.FILE_LIST: file_data[document_id]}}, upsert=True)
            upsert(result, document_id)


@timed
def load_json():
    print('Loading JSON...')
    with open(constants.STORAGE_FILE, constants.READ, encoding=constants.UTF8) as md5:
        file_data = json.load(md5)
    return file_data


@timed
def generate_list(file_data):
    print('Generating List...')
    bulk_insert_list = []
    total_files = len(file_data)
    with tqdm(total=total_files, bar_format=constants.STATUS_BAR_FORMAT) as tqdm_progress_bar:
        for document_id in file_data:
            try:
                document = {
                    constants.DOC_ID: document_id,
                    constants.FILE_SIZE: os.path.getsize(file_data[document_id][0]),
                    constants.FILE_LIST: file_data[document_id]
                }
                bulk_insert_list.append(document)
                tqdm_progress_bar.update(1)
            except Exception as e:
                print(f'Error processing {document_id}: {e}')
    return bulk_insert_list


@timed
def insert_bulk_list(bulk_insert_list):
    print('Inserting List...')
    if bulk_insert_list:
        try:
            mongo_collection.insert_many(bulk_insert_list, ordered=False)
        except BulkWriteError as bulk_write_error:
            print(f'Bulk write error: {bulk_write_error.details}')


@timed
def import_bulk_database():
    print('Bulk import started.')
    json_data = load_json()
    bulk_list = generate_list(json_data)
    insert_bulk_list(bulk_list)


@timed
def import_database():
    with open(constants.STORAGE_FILE, constants.READ, encoding=constants.UTF8) as md5:
        file_data = json.load(md5)
        total_files = len(file_data)
        status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"
        with tqdm(total=total_files, bar_format=status_bar_format) as tqdm_progress_bar:
            for document_id in file_data:
                try:
                    mongo_collection.insert_one({constants.DOC_ID: document_id, constants.FILE_SIZE: os.path.getsize(file_data[document_id][0]), constants.FILE_LIST: file_data[document_id]})
                    tqdm_progress_bar.update(1)
                except DuplicateKeyError as duplicate_key_error:
                    print(f'[{duplicate_key_error}]')


def insert_item(document):
    for document_id in document:
        try:
            mongo_collection.insert_one({constants.DOC_ID: document_id, constants.FILE_SIZE: os.path.getsize(document[document_id][0]), constants.FILE_LIST: document[document_id]})
        except DuplicateKeyError as duplicate_key_error:
            print(f'[{duplicate_key_error}]')
        break


def update_content_on_item(document_id, file_id):
    element = mongo_collection.find_one({constants.DOC_ID: document_id})
    if file_id not in element[constants.FILE_LIST]:
        element[constants.FILE_LIST].append(file_id)
        result = mongo_collection.update_one({constants.DOC_ID: document_id}, {'$set': {constants.FILE_LIST: element[constants.FILE_LIST]}}, upsert=True)
        upsert(result, document_id)
    else:
        print(f'[{document_id}][{file_id}] already exists.')


def delete_content_on_item(document_id, file_id):
    element = mongo_collection.find_one({constants.DOC_ID: document_id})
    if file_id in element[constants.FILE_LIST]:
        element[constants.FILE_LIST].remove(file_id)
        result = mongo_collection.update_one({constants.DOC_ID: document_id}, {'$set': {constants.FILE_LIST: element[constants.FILE_LIST]}}, upsert=True)
        upsert(result, document_id)
    else:
        print(f'[{document_id}][{file_id}] already exists.')


def get_item_by_id(document_id):
    return mongo_collection.find_one({constants.DOC_ID: document_id})


def delete_item_by_id(item_id):
    result = mongo_collection.delete_one({constants.DOC_ID: item_id})
    return result.deleted_count == 1


def get_item_by_content(file_id):
    return mongo_collection.find_one({constants.FILE_LIST: {'$elemMatch': {'$eq': file_id}}})


def get_item_by_file(file_piece):
    return mongo_collection.find({constants.FILE_LIST: {'$regex': file_piece}})


def count_files(file_piece):
    return mongo_collection.count_documents({constants.FILE_LIST: {'$regex': file_piece}})


if __name__ == constants.MAIN:
    # import_database()

    import_bulk_database()

    # item = get_item_by_content(r'c:\teste')
    # print(item)

    # update_item('8660dc65359955df67ebedc640fc3d82', r'c:\teste')

    # delete_item('8660dc65359955df67ebedc640fc3d82', r'c:\teste')

    # item = get_item_by_id('8660dc65359955df67ebedc640fc3d82')
    # print(item)
