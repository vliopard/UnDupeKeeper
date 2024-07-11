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


def read_mongodb_data():
    # db.getCollection('UnDupeKeeperFiles').aggregate([ {$project: { "file_list": 1, "file_list_count": { $size: "$file_list" } }}, {$sort: {"file_list_count": -1}}])
    documents = mongo_collection.aggregate([{"$project": {"file_list": 1, "file_size": 1, "file_list_count": {"$size": "$file_list"}}}, {"$sort": {"file_list_count": -1}}])
    ct = 0
    for doc in documents:
        ct += 1
        print(doc)
        if ct > 10:
            break


@timed
def import_database():
    with open(constants.STORAGE_FILE, 'r', encoding=constants.UTF8) as md5:
        data = json.load(md5)
        for d in data:
            single = {'_id': d, 'file_size': os.path.getsize(data[d][0]), 'file_list': data[d]}
            try:
                mongo_collection.insert_one(single)
            except DuplicateKeyError as duplicate_key_error:
                print(f'[{duplicate_key_error}]')


if __name__ == "__main__":
    read_mongodb_data()
