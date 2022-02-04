import os
import time
import UnDupeKeeper

from datetime import timedelta
from operator import itemgetter

from filecmp import clear_cache

from openpyxl import Workbook

excel_workbook = Workbook()
excel_worksheet = excel_workbook.active
excel_worksheet.title = 'File Comparison'


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


trials = [UnDupeKeeper.BUFFER,
          UnDupeKeeper.COMPARE,
          UnDupeKeeper.ZIP_LONGEST,
          UnDupeKeeper.NATIVE,
          UnDupeKeeper.EXECUTABLE,
          UnDupeKeeper.HASH_MD5,
          UnDupeKeeper.HASH_SHA,
          UnDupeKeeper.HASH_SHA256,
          UnDupeKeeper.HASH_SHA512]

excel_worksheet.append(tuple(['TIME', 'CATEGORY', 'SIZE', 'RESULT', 'METHOD']))


def comparison_test(first_file, second_file, category):
    results = []
    for trial in trials:
        clear_cache()
        start_time = time.time()
        answer = UnDupeKeeper.file_equals(first_file, second_file, trial)
        results.append([str(timedelta(seconds=time.time() - start_time)), category, trial, answer])
    report = sorted(results, key=itemgetter(0))
    for trial in report:
        print(f"{trial[0]}  {trial[1].split(':')[0]}  {trial[1].split(':')[1]}  {trial[3]}  {trial[2]}")
        excel_worksheet.append(tuple([trial[0], trial[1].split(':')[0], trial[1].split(':')[1], trial[3], trial[2]]))
    print('\n')


for file in files:
    comparison_test(file, file.replace('.', '_dupe.'), f'EQUAL:{os.stat(file).st_size:0,.0f}')

for file in files:
    comparison_test(file, file.replace('.', '_start.'), f'NOT EQUAL/SAME SIZE/START:{os.stat(file).st_size:0,.0f}')

for file in files:
    comparison_test(file, file.replace('.', '_middle.'), f'NOT EQUAL/SAME SIZE/MIDDLE:{os.stat(file).st_size:0,.0f}')

for file in files:
    comparison_test(file, file.replace('.', '_end.'), f'NOT EQUAL/SAME SIZE/END : {os.stat(file).st_size:0,.0f}')

# test(f1, f2, 'NOT EQUAL, SIZE DIFFERENT')

excel_workbook.save('comparison_test.xlsx')
