import os
import time
import shutil
import hashlib
import argparse


def get_file_groups(directory_name):
    content_map = {}
    for dirpath, _, filenames in os.walk(directory_name):
        for name in filenames:
            full_path = os.path.join(dirpath, name)
            file_size = os.path.getsize(full_path)
            if file_size in content_map:
                content_map[file_size]['file_list'].append(os.path.realpath(full_path))
                content_map[file_size]['count'] += 1
            else:
                content_map[file_size] = {'file_list': [os.path.realpath(full_path)], 'count': 1}
    return content_map


def group_files_by_content(filelist):
    content_map = {}
    final_cmap = {}
    for file_path in filelist:
        with open(file_path, 'rb') as f:
            content_hash = hashlib.sha256(f.read()).hexdigest()
        if content_hash in content_map:
            content_map[content_hash].append(file_path)
        else:
            content_map[content_hash] = [file_path]
    for fx in content_map:
        if len(content_map[fx]) > 1:
            final_cmap[fx] = {fn: '      ' for fn in content_map[fx]}
    return final_cmap


def move_file_with_rename(src_path, dst_path):
    if not os.path.isfile(src_path):
        return

    base_name = os.path.basename(src_path)
    name, ext = os.path.splitext(base_name)
    new_filename = os.path.join(dst_path, base_name)
    counter = 1

    while os.path.exists(new_filename):
        new_filename = f'{name} ({counter}){ext}'
        new_filename = os.path.join(dst_path, new_filename)
        counter += 1

    shutil.move(src_path, new_filename)


if __name__ == '__main__':
    start = time.time()
    print('Process started...')
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--source', required=True)
    parser.add_argument('-t', '--target', required=True)
    parser.add_argument('-m', '--move', action='store_true')
    args = parser.parse_args()
    current_path = args.source
    new_path = args.target
    move_files = args.move

    token_priority_to_remove = ['maskapuda', ' (1)', ' (2)', ' (3)', 'SmartSwitchBackup', 'Quick Share']
    grouped_files_dictionary = {}
    file_list_table = []
    grouped_files = get_file_groups(current_path)
    for file in grouped_files:
        if grouped_files[file]['count'] > 1:
            grouped_files_dictionary.update(group_files_by_content(grouped_files[file]['file_list']))

    for file_group in grouped_files_dictionary:
        file_count = len(grouped_files_dictionary[file_group].keys())
        for file_name in grouped_files_dictionary[file_group]:
            if any(word in file_name for word in token_priority_to_remove):
                if file_count > 1:
                    grouped_files_dictionary[file_group][file_name] = 'REMOVE'
                    file_count -= 1
        if file_count > 1:
            for file_name_remaining in grouped_files_dictionary[file_group]:
                if file_count > 1 and grouped_files_dictionary[file_group][file_name_remaining] != 'REMOVE':
                    grouped_files_dictionary[file_group][file_name_remaining] = 'REMOVE'
                    file_count -= 1

    for file_list in grouped_files_dictionary:
        file_list_table.append('=====================================')
        for filename in grouped_files_dictionary[file_list]:
            if move_files and grouped_files_dictionary[file_list][filename] == 'REMOVE':
                move_file_with_rename(filename, new_path)
            file_list_table.append([grouped_files_dictionary[file_list][filename], file_list[:6], filename])
    with open('remove_table.txt', 'w', encoding='utf-8') as file_indicator:
        for row in file_list_table:
            file_indicator.write(' '.join(row) + '\n')
    end = time.time()
    elapsed = int(end - start)
    hours = elapsed // 3600
    minutes = (elapsed % 3600) // 60
    seconds = elapsed % 60
    print(f'Execution time: {hours:02d}:{minutes:02d}:{seconds:02d}')
