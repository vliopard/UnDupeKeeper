import constants
from methods import file_equals

import logging
show = logging.getLogger(constants.DEBUG_DIFF)

INPUT = 'input.txt'
OUTPUT = 'output.txt'

trials = [constants.BUFFER,
          constants.COMPARE,
          constants.ZIP_LONGEST,
          constants.NATIVE,
          constants.EXECUTABLE,
          constants.HASH_MD5,
          constants.HASH_SHA,
          constants.HASH_SHA256,
          constants.HASH_SHA512]

trial = trials[1]

if __name__ == "__main__":

    total_success = 0
    total_failure = 0
    final_result = []
    with open(INPUT, constants.READ, encoding=constants.UTF8) as input_file:
        folder_pairs = False
        header_line = False
        path_one = ''
        path_two = ''
        for line in input_file:
            if line.strip():
                content = line.strip()
                if content == 'Folder Pairs':
                    folder_pairs = True
                    continue
                if content == 'Relative path;Size;Difference;Action;Relative path;Size':
                    header_line = True
                    continue
                if folder_pairs:
                    folder_pairs = False
                    path_one = line.strip().split(";")[0]
                    path_two = line.strip().split(";")[1]
                if header_line:
                    file_one = f'{path_one}\\{line.strip().split(";")[0]}'
                    file_two = f'{path_two}\\{line.strip().split(";")[4]}'
                    result = f'{file_one}; {file_two}\n'
                    try:
                        print('_'*50)
                        print('COMPARING')
                        print(f'[{file_one}]')
                        print(f'[{file_two}]')
                        print('Phase 0...')
                        result_1 = file_equals(file_one, file_two, trials[0])
                        print('Phase 1...')
                        result_2 = file_equals(file_one, file_two, trials[1])
                        print('Phase 2...')
                        result_3 = file_equals(file_one, file_two, trials[2])
                        print('Phase 3...')
                        result_4 = file_equals(file_one, file_two, trials[3])
                        print('Done...')
                        results = result_1 and result_2 and result_3 and result_4
                        if not results:
                            print(f"FAIL [{result_1}] [{result_2}] [{result_3}] [{result_4}]")
                            final_result.append(result)
                            total_failure += 1
                        else:
                            print("OK")
                            total_success += 1
                    except PermissionError as permission_error:
                        final_result.append(result)
                        total_failure += 1
                        print(f'ERROR: [{permission_error}]')

    with open(OUTPUT, constants.WRITE, encoding=constants.UTF8) as output_file:
        output_file.writelines(final_result)

    print(f'[{total_failure}/{total_success + total_failure}]')
