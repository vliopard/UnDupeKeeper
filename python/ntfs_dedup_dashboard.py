import re
import os
import constants
import subprocess

import logging
show = logging.getLogger(constants.DEBUG_DASH)


get_job = 'Get-DedupJob'
get_job_res = '''
Type               ScheduleType       StartTime              Progress   State                  Volume
----               ------------       ---------              --------   -----                  ------
Optimization       Scheduled                                 0 %        Queued                 E:
Optimization       Manual             01:25 a                10 %       Running                E:
'''

get_status = 'Get-DedupStatus'
get_status_res = '''
FreeSpace    SavedSpace   OptimizedFiles     InPolicyFiles      Volume
---------    ----------   --------------     -------------      ------
2.01 TB      3.58 TB      1226082            1310716            E:
'''

get_volume = 'Get-DedupVolume'
get_volume_res = '''
Enabled            UsageType          SavedSpace           SavingsRate          Volume
-------            ---------          ----------           -----------          ------
True               Default            66.76 GB             1 %                  E:
'''


def parse_volume(table):
    matching_pattern = re.compile(r'^(True|False)\s+(\w+)\s+([\d.]+\s\w+)\s+([\d.]+)\s%\s+(\w+):$')
    parsed_data = {"Enabled": None, "UsageType": None, "SavedSpace": None, "SavingsRate": None, "Volume": None}
    for line in table.strip().split(constants.NEW_LINE):
        match = matching_pattern.match(line.strip())
        if match:
            parsed_data["Enabled"] = match.group(1)
            parsed_data["UsageType"] = match.group(2)
            parsed_data["SavedSpace"] = match.group(3)
            parsed_data["SavingsRate"] = f"{match.group(4)} %"
            parsed_data["Volume"] = match.group(5) + ":"
    return parsed_data


def parse_status(table):
    matching_pattern = re.search(r'(\d+\.\d+ [GT]B)\s+(\d+\.\d+ [GT]B)\s+(\d+)\s+(\d+)\s+([A-Z]:)', table)
    return_value = None
    if matching_pattern:
        free_space_local = matching_pattern.group(1)
        saved_space_local = matching_pattern.group(2)
        optimized_files_local = matching_pattern.group(3)
        in_policy_files = matching_pattern.group(4)
        volume = matching_pattern.group(5)
        return_value = {'FreeSpace': free_space_local, 'SavedSpace': saved_space_local, 'OptimizedFiles': optimized_files_local, 'InPolicyFiles': in_policy_files, 'Volume': volume}
    return return_value


def parse_row(row):
    matching_pattern = re.compile(r'(\w+)\s+(\w+)\s+(\d{2}:\d{2}\s*[ap]?m?)?\s+(\d+\s*%)\s+(\w+)\s+(\w+:\s?)')
    matched_pattern = matching_pattern.match(row)
    if matched_pattern:
        return {
            'Type': matched_pattern.group(1),
            'ScheduleType': matched_pattern.group(2),
            'StartTime': matched_pattern.group(3) or 'N/A',
            'Progress': matched_pattern.group(4),
            'State': matched_pattern.group(5),
            'Volume': matched_pattern.group(6)
        }
    return None


def parse_job(jobs):
    lines = jobs.strip().split('\n')
    data_lines = lines[2:]
    parsed_job = [parse_row(line) for line in data_lines]
    parsed_job = [entry for entry in parsed_job if entry]
    return parsed_job


if __name__ == '__main__':
    DEBUG = False
    os.system('cls')
    print('Getting jobs...')
    if not DEBUG:
        get_job_res = subprocess.run(['powershell.exe', get_job], stdout=subprocess.PIPE, text=True)
        print('Done.')
        job_output = parse_job(get_job_res.stdout)
    else:
        job_output = parse_job(get_job_res)

    print('Getting status...')
    if not DEBUG:
        get_status_res = subprocess.run(['powershell.exe', get_status], stdout=subprocess.PIPE, text=True)
        print('Done.')
        status_output = parse_status(get_status_res.stdout)
    else:
        status_output = parse_status(get_status_res)

    print('Getting volume...')
    if not DEBUG:
        get_volume_res = subprocess.run(['powershell.exe', get_volume], stdout=subprocess.PIPE, text=True)
        print('Done.')
        volume_output = parse_volume(get_volume_res.stdout)
    else:
        volume_output = parse_volume(get_volume_res)

    right_justify = 12
    os.system('cls')
    print('_'*30)
    for job_element in job_output:
        if job_element['State'] == 'Running':
            print(f'Type:           [{job_element["Type"].rjust(right_justify)}]')
            print(f'ScheduleType:   [{job_element["ScheduleType"].rjust(right_justify)}]')
            print(f'StartTime:      [{job_element["StartTime"].rjust(right_justify)}]')
            print(f'State:          [{job_element["State"].rjust(right_justify)}]')

            progress = f'{int(job_element["Progress"].replace(" %", ""))}%'
            print(f'Progress:       [{progress.rjust(right_justify)}]')

    free_space_unit = ''
    saved_space_unit = ''
    if status_output and 'GB' in status_output["FreeSpace"]:
        free_space_unit = 'GB'
    if status_output and 'TB' in status_output["FreeSpace"]:
        free_space_unit = 'TB'
    if volume_output and 'GB' in volume_output["SavedSpace"]:
        saved_space_unit = 'GB'
    if volume_output and 'TB' in volume_output["SavedSpace"]:
        saved_space_unit = 'TB'

    free_space = f'{float(status_output["FreeSpace"].replace(" GB", "").replace(" TB", ""))} {free_space_unit}' if status_output else ''
    optimized_files = f'{int(status_output["OptimizedFiles"]):,}' if status_output else ''
    in_policy = f'{int(status_output["InPolicyFiles"]):,}' if status_output else ''

    saved_space = f'{float(volume_output["SavedSpace"].replace(" GB", "").replace(" TB", ""))} {saved_space_unit}' if 'SavedSpace' in volume_output else ''
    saving_rate = f'{int(volume_output["SavingsRate"].replace(" %", ""))}%' if 'SavingsRate' in volume_output else ''

    print(f'FreeSpace:      [{free_space.rjust(right_justify)}]')
    print(f'SavedSpace:     [{saved_space.rjust(right_justify)}]')
    print(f'OptimizedFiles: [{optimized_files.rjust(right_justify)}]')
    print(f'InPolicyFiles:  [{in_policy.rjust(right_justify)}]')
    print(f'SavingsRate:    [{saving_rate.rjust(right_justify)}]')
    print('_'*30)
    print('\n')
