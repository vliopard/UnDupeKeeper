import os
import shutil

magnitude = 1000

directory = 'files'

files = [f'{directory}/file0000005.bin',
         f'{directory}/file0000050.bin',
         f'{directory}/file0000100.bin',
         f'{directory}/file0000500.bin',
         f'{directory}/file0001500.bin',
         f'{directory}/file0005000.bin',
         f'{directory}/file0010000.bin',
         f'{directory}/file0050000.bin',
         f'{directory}/file0150000.bin',
         f'{directory}/file0300000.bin',
         f'{directory}/file1000000.bin']


def create_random_binary_file(file_path, size):
    with open(file_path, 'wb') as random_file:
        random_bytes = bytearray(os.urandom(size))
        random_file.write(random_bytes)


for file in files:
    file_size = file.replace('.bin', '').replace(f'{directory}/file', '')
    create_random_binary_file(file, int(file_size)*magnitude)


if not os.path.exists(directory):
    print(f'Creating [{directory}]...')
    os.mkdir(directory)


for file_name in files:
    position = int(os.stat(file_name).st_size / 6)
    data = bytes(56)

    new_name = file_name.replace('.', '_dupe.')
    shutil.copyfile(file_name, new_name)

    new_name = file_name.replace('.', '_start.')
    shutil.copyfile(file_name, new_name)
    with open(new_name, 'r+b') as file_writer:
        file_writer.seek(position)
        file_writer.write(data)

    new_name = file_name.replace('.', '_middle.')
    shutil.copyfile(file_name, new_name)
    with open(new_name, 'r+b') as file_writer:
        file_writer.seek(position * 2)
        file_writer.write(data)

    new_name = file_name.replace('.', '_end.')
    shutil.copyfile(file_name, new_name)
    with open(new_name, 'r+b') as file_writer:
        file_writer.seek(position * 5)
        file_writer.write(data)
