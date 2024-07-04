import os
import json
import shutil
import argparse
from tqdm import tqdm


def copy_files(target_location):
    json_file = 'UnDupeKeeper.json'

    with open(json_file, 'r') as hdd_hl:
        data = json.load(hdd_hl)

        status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"

        with tqdm(total=len(data), bar_format=status_bar_format) as tqdm_progress_bar:
            for hash_file in data:
                source_file = sorted(data[hash_file])[0]
                try:
                    _, drive_tail = os.path.splitdrive(os.path.relpath(source_file, '/'))
                    target_file = os.path.join(target_location, drive_tail)
                    os.makedirs(os.path.dirname(target_file), exist_ok=True)
                    shutil.copyfile(source_file, target_file)
                    tqdm_progress_bar.update(1)
                except Exception as e:
                    print('_'*100)
                    print(f"Error copying:\n   [{source_file}]\nto [{target_file}]\n[{e}]")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Copy files to a target location.')
    parser.add_argument('target_location', type=str, help='The target location to copy files to')
    args = parser.parse_args()
    copy_files(args.target_location)
