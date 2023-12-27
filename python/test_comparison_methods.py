import os
import time
import UnDupeKeeper

from generate_binary_files import files

from datetime import timedelta
from operator import itemgetter

from filecmp import clear_cache

from openpyxl import Workbook


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
        excel_worksheet.append(tuple([trial[0], trial[1].split(':')[0], int(trial[1].split(':')[1].replace(',', '')), trial[3], trial[2]]))
    print('\n')


if __name__ == "__main__":
    print('Started...')
    excel_workbook = Workbook()
    excel_worksheet = excel_workbook.active
    excel_worksheet.title = 'File Comparison'

    trials = [UnDupeKeeper.BUFFER,
              UnDupeKeeper.COMPARE,
              UnDupeKeeper.ZIP_LONGEST,
              UnDupeKeeper.NATIVE,
              UnDupeKeeper.EXECUTABLE,
              UnDupeKeeper.HASH_MD5,
              UnDupeKeeper.HASH_SHA,
              UnDupeKeeper.HASH_SHA256,
              UnDupeKeeper.HASH_SHA512]

    print('Workbook...')
    excel_worksheet.append(tuple(['TIME', 'CATEGORY', 'SIZE', 'RESULT', 'METHOD']))

    for file_path in files:

        file = os.path.join(os.getcwd(), file_path.replace('/', '\\'))

        print(f'Working on [{file}]...')
        comparison_test(file, file.replace('.', '_dupe.'), f'EQUAL:{os.stat(file).st_size:0,.0f}')
        comparison_test(file, file.replace('.', '_start.'), f'NOT EQUAL/SAME SIZE/START:{os.stat(file).st_size:0,.0f}')
        comparison_test(file, file.replace('.', '_middle.'), f'NOT EQUAL/SAME SIZE/MIDDLE:{os.stat(file).st_size:0,.0f}')
        comparison_test(file, file.replace('.', '_end.'), f'NOT EQUAL/SAME SIZE/END : {os.stat(file).st_size:0,.0f}')

    # test(f1, f2, 'NOT EQUAL, SIZE DIFFERENT')

    excel_workbook.save('comparison_test.xlsx')
