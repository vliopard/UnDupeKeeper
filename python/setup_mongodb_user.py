"""
netsh advfirewall firewall add rule name="_MongoDB - Open mongod config svr port 27019" dir=in action=allow protocol=TCP localport=27019
netsh advfirewall firewall add rule name="_MongoDB - Open mongod config svr inbound" dir=in action=allow protocol=TCP remoteip=<ip-address> localport=27019
netsh advfirewall firewall add rule name="_MongoDB - Open mongod config svr outbound" dir=out action=allow protocol=TCP remoteip=<ip-address> localport=27018

netsh advfirewall firewall add rule name="_MongoDB - Open mongod shard port 27018" dir=in action=allow protocol=TCP localport=27018
netsh advfirewall firewall add rule name="_MongoDB - Open mongod shardsvr inbound" dir=in action=allow protocol=TCP remoteip=<ip-address> localport=27018
netsh advfirewall firewall add rule name="_MongoDB - Open mongod shardsvr outbound" dir=out action=allow protocol=TCP remoteip=<ip-address> localport=27018

netsh advfirewall firewall delete rule name="Open mongod port 27017" protocol=tcp localport=27017
netsh advfirewall firewall delete rule name="Open mongod shard port 27018" protocol=tcp localport=27018

netsh advfirewall export "C:\vliopard\MongoDBfw.wfw"
netsh advfirewall reset
netsh advfirewall import "C:\vliopard\MongoDBfw.wfw"
"""

import os
import constants
import subprocess
from pymongo import MongoClient
from pymongo.errors import ServerSelectionTimeoutError

mongo_client = MongoClient(constants.DATABASE_URL)
mongo_database = mongo_client[constants.DATABASE_NAME]
mongo_collection = mongo_database[constants.DATABASE_COLLECTION]


def get_one_item():
    mongo_document = None
    try:
        print('Getting server info...')
        si = mongo_client.server_info()
        print(si)
        print('Looking for one document...')
        mongo_document = mongo_collection.find_one({})
        print('Document found...')
    except ServerSelectionTimeoutError as server_selection_timeout_error:
        print(f'SERVER SELECTION TIMEOUT ERROR: [{server_selection_timeout_error}]')
    except Exception as exception:
        print(f'EXCEPTION: [{exception}]')
    return mongo_document


if __name__ == constants.MAIN:
    # mongosh --port 27017
    # subprocess.run(['mongosh', '--port', '27017'], check=True)

    client = MongoClient('mongodb://localhost:27017/')

    # use admin
    db = client.admin
    # db.createUser({user:'username', pwd:'********',roles:['root']})
    username = input('Enter database username: ')
    password = input('Enter database password: ')
    db.command('createUser', username, pwd=password, roles=['root'])

    # db.grantRolesToUser('vliopard', [{role:'userAdminAnyDatabase', db:'admin'}, {role:'dbAdminAnyDatabase', db: 'admin'}, {role:'readWriteAnyDatabase', db:'admin'}])
    db.command('grantRolesToUser', username, roles=[
        {'role': 'userAdminAnyDatabase', 'db': 'admin'},
        {'role': 'dbAdminAnyDatabase', 'db': 'admin'},
        {'role': 'readWriteAnyDatabase', 'db': 'admin'}
    ])

    # db.getUsers()
    users = db.command('usersInfo')
    print(users)

    # exit
    client.close()

    # mongosh --port 27017 --authenticationDatabase "admin" -u "vliopard" -p
    subprocess.run(['mongosh', '--port', '27017', '--authenticationDatabase', 'admin', '-u', username, '-p'], check=True)

    # netsh advfirewall firewall show rule name=all

    mongo_directory = ' C:\\vliopard\\programs\\mongo_db\\bin\\'

    # netsh advfirewall firewall add rule name="_MongoDB - Open mongod port 27017" dir=in action=allow protocol=TCP localport=27017
    os.system('netsh advfirewall firewall add rule name="_MongoDB - Open mongod port 27017" dir=in action=allow protocol=TCP localport=27017')

    # netsh advfirewall firewall add rule name="_MongoDB - Allowing mongod" dir=in action=allow program=" C:\vliopard\programs\mongo_db\bin\mongod.exe"
    os.system(f'netsh advfirewall firewall add rule name="_MongoDB - Allowing mongod" dir=in action=allow program="{mongo_directory}mongod.exe"')

    # netsh advfirewall firewall add rule name="_MongoDB - Allowing mongos" dir=in action=allow program=" C:\vliopard\programs\mongo_db\bin\mongos.exe"
    os.system(f'netsh advfirewall firewall add rule name="_MongoDB - Allowing mongos" dir=in action=allow program="{mongo_directory}mongos.exe"')

    print('Initializing...')
    value = get_one_item()
    if value:
        print(f'Result:')
        print(f'[{value}]')
    else:
        print('Document not found...')
