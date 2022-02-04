import os
import shutil


files = ["c:\\vliopard\\workspace\\vliopard\\files\\file0000005.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0000050.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0000100.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0000500.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0001500.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0005000.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0010000.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0050000.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0150000.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file0300000.bin",
         "c:\\vliopard\\workspace\\vliopard\\files\\file1000000.bin"]


for x in files:
    position = int(os.stat(x).st_size / 6)
    data = bytes(56)

    new_name = x.replace('.', '_start.')
    shutil.copyfile(x, new_name)
    with open(new_name, 'r+b') as f:
        f.seek(position)
        f.write(data)

    new_name = x.replace('.', '_middle.')
    shutil.copyfile(x, new_name)
    with open(new_name, 'r+b') as f:
        f.seek(position * 2)
        f.write(data)

    new_name = x.replace('.', '_end.')
    shutil.copyfile(x, new_name)
    with open(new_name, 'r+b') as f:
        f.seek(position * 5)
        f.write(data)
