import re
import os
import subprocess

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
    pattern1 = re.compile(r'^(True|False)\s+(\w+)\s+([\d.]+\s\w+)\s+([\d.]+)\s%\s+(\w+):$')

    parsed_data1 = {
        "Enabled": None,
        "UsageType": None,
        "SavedSpace": None,
        "SavingsRate": None,
        "Volume": None
    }

    for line in table.strip().split('\n'):
        match = pattern1.match(line.strip())
        if match:
            parsed_data1["Enabled"] = match.group(1)
            parsed_data1["UsageType"] = match.group(2)
            parsed_data1["SavedSpace"] = match.group(3)
            parsed_data1["SavingsRate"] = f"{match.group(4)} %"
            parsed_data1["Volume"] = match.group(5) + ":"

    return parsed_data1


def parse_status(table):
    match = re.search(r'(\d+\.\d+ [GT]B)\s+(\d+\.\d+ [GT]B)\s+(\d+)\s+(\d+)\s+([A-Z]:)', table)
    retval = None
    if match:
        free_space = match.group(1)
        saved_space = match.group(2)
        optimized_files = match.group(3)
        in_policy_files = match.group(4)
        volume = match.group(5)
        retval = {'FreeSpace': free_space,
                  'SavedSpace': saved_space,
                  'OptimizedFiles': optimized_files,
                  'InPolicyFiles': in_policy_files,
                  'Volume': volume}
    return retval


def parse_row(row):
    pattern = re.compile(r'(\w+)\s+(\w+)\s+(\d{2}:\d{2}\s*[ap]?m?)?\s+(\d+\s*%)\s+(\w+)\s+(\w+:\s?)')
    match = pattern.match(row)
    if match:
        return {
            'Type': match.group(1),
            'ScheduleType': match.group(2),
            'StartTime': match.group(3) or 'N/A',
            'Progress': match.group(4),
            'State': match.group(5),
            'Volume': match.group(6)
        }
    return None


def parse_job(jobs):
    lines = jobs.strip().split('\n')
    data_lines = lines[2:]
    parsed_datax = [parse_row(line) for line in data_lines]
    parsed_datax = [entry for entry in parsed_datax if entry]
    return parsed_datax


if __name__ == '__main__':
    DEBUG = False
    os.system('cls')
    print('Getting jobs...')
    if not DEBUG:
        get_job_res = subprocess.run(['powershell.exe', get_job], stdout=subprocess.PIPE, text=True)
        print('Done.')
        j = parse_job(get_job_res.stdout)
    else:
        j = parse_job(get_job_res)

    print('Getting status...')
    if not DEBUG:
        get_status_res = subprocess.run(['powershell.exe', get_status], stdout=subprocess.PIPE, text=True)
        print('Done.')
        s = parse_status(get_status_res.stdout)
    else:
        s = parse_status(get_status_res)

    print('Getting volume...')
    if not DEBUG:
        get_volume_res = subprocess.run(['powershell.exe', get_volume], stdout=subprocess.PIPE, text=True)
        print('Done.')
        v = parse_volume(get_volume_res.stdout)
    else:
        v = parse_volume(get_volume_res)

    rj = 12
    os.system('cls')
    print('_'*30)
    for i in j:
        if i['State'] == 'Running':
            print(f'Type:           [{i["Type"].rjust(rj)}]')
            print(f'ScheduleType:   [{i["ScheduleType"].rjust(rj)}]')
            print(f'StartTime:      [{i["StartTime"].rjust(rj)}]')
            print(f'State:          [{i["State"].rjust(rj)}]')

            pg = f'{int(i["Progress"].replace(" %", ""))}%'
            print(f'Progress:       [{pg.rjust(rj)}]')

    sx = ''
    vx = ''
    if s and 'GB' in s["FreeSpace"]:
        sx = 'GB'
    if s and 'TB' in s["FreeSpace"]:
        sx = 'TB'
    if v and 'GB' in v["SavedSpace"]:
        vx = 'GB'
    if v and 'TB' in v["SavedSpace"]:
        vx = 'TB'

    fs = f'{float(s["FreeSpace"].replace(" GB", "").replace(" TB", ""))} {sx}' if s else ''
    of = f'{int(s["OptimizedFiles"]):,}' if s else ''
    ip = f'{int(s["InPolicyFiles"]):,}' if s else ''

    ss = f'{float(v["SavedSpace"].replace(" GB", "").replace(" TB", ""))} {vx}' if 'SavedSpace' in v else ''
    sr = f'{int(v["SavingsRate"].replace(" %", ""))}%' if 'SavingsRate' in v else ''

    print(f'FreeSpace:      [{fs.rjust(rj)}]')
    print(f'SavedSpace:     [{ss.rjust(rj)}]')
    print(f'OptimizedFiles: [{of.rjust(rj)}]')
    print(f'InPolicyFiles:  [{ip.rjust(rj)}]')
    print(f'SavingsRate:    [{sr.rjust(rj)}]')
    print('_'*30)
    print('\n')
