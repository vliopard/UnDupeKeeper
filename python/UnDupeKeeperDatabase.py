import os
import json
import constants
from methods import timed
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError

import logging
show = logging.getLogger(constants.DEBUG_DB)


mongo_client = MongoClient('mongodb://localhost:27017/')
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]

DOC_ID = '_id'
FILE_LIST = 'file_list'
FILE_SIZE = 'file_size'
FILE_COUNT = 'file_list_count'


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
    documents = mongo_collection.aggregate([{"$project": {FILE_LIST: 1, FILE_SIZE: 1, FILE_COUNT: {"$size": "$file_list"}}}, {"$sort": {FILE_COUNT: -1 if desc else 1}}])
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
            result = mongo_collection.update_one({DOC_ID: document_id}, {'$set': {FILE_SIZE: os.path.getsize(file_data[document_id][0]), FILE_LIST: file_data[document_id]}}, upsert=True)
            upsert(result, document_id)


@timed
def import_database():
    with open(constants.STORAGE_FILE, constants.READ, encoding=constants.UTF8) as md5:
        file_data = json.load(md5)
        for document_id in file_data:
            try:
                mongo_collection.insert_one({DOC_ID: document_id, FILE_SIZE: os.path.getsize(file_data[document_id][0]), FILE_LIST: file_data[document_id]})
            except DuplicateKeyError as duplicate_key_error:
                print(f'[{duplicate_key_error}]')


def insert_item(document):
    for document_id in document:
        try:
            mongo_collection.insert_one({DOC_ID: document_id, FILE_SIZE: os.path.getsize(document[document_id][0]), FILE_LIST: document[document_id]})
        except DuplicateKeyError as duplicate_key_error:
            print(f'[{duplicate_key_error}]')
        break


def update_content_on_item(document_id, file_id):
    element = mongo_collection.find_one({DOC_ID: document_id})
    if file_id not in element[FILE_LIST]:
        element[FILE_LIST].append(file_id)
        result = mongo_collection.update_one({DOC_ID: document_id}, {'$set': {FILE_LIST: element[FILE_LIST]}}, upsert=True)
        upsert(result, document_id)
    else:
        print(f'[{document_id}][{file_id}] already exists.')


def delete_content_on_item(document_id, file_id):
    element = mongo_collection.find_one({DOC_ID: document_id})
    if file_id in element[FILE_LIST]:
        element[FILE_LIST].remove(file_id)
        result = mongo_collection.update_one({DOC_ID: document_id}, {'$set': {FILE_LIST: element[FILE_LIST]}}, upsert=True)
        upsert(result, document_id)
    else:
        print(f'[{document_id}][{file_id}] already exists.')


def get_item_by_id(document_id):
    return mongo_collection.find_one({DOC_ID: document_id})


def delete_item_by_id(item_id):
    result = mongo_collection.delete_one({DOC_ID: item_id})
    return result.deleted_count == 1


def get_item_by_content(file_id):
    return mongo_collection.find_one({FILE_LIST: {'$elemMatch': {'$eq': file_id}}})


if __name__ == "__main__":
    item = get_item_by_content(r'c:\teste')
    print(item)

    # update_item('8660dc65359955df67ebedc640fc3d82', r'c:\teste')

    # delete_item('8660dc65359955df67ebedc640fc3d82', r'c:\teste')

    # item = get_item_by_id('8660dc65359955df67ebedc640fc3d82')
    # print(item)
