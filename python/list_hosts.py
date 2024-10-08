import json
import socket
import psutil
import methods
import paramiko
import colorama
import threading
import constants
import subprocess
from queue import Queue
from colorama import Fore

HOST_STATUS = 8
HOST_JUSTIFY = 13
COUNTER_JUSTIFY = 3
HORIZONTAL_LINE = 85
WORD_ALIGNMENT = 26

RED = '\033[0;91m'
GREEN = '\033[0;92m'
YELLOW = '\033[0;93m'
BLUE = '\033[0;94m'
RESET = '\033[0;0m'
DARK_RED = Fore.RED

ONLINE = f'{GREEN}Online*{RESET}'
INLINE = f'{YELLOW}Online?{RESET}'
ATLINE = f'{BLUE}Online {RESET}'
OFFLINE = f'{RED}Offline{RESET}'


with open(constants.KNOWN_HOSTS, constants.READ, encoding=constants.UTF8) as known_hosts_file:
    known_hosts = json.load(known_hosts_file)


def scan_network(network_base_ip):
    threads = []
    result_queue = Queue()
    for number in range(1, 254):
        computer_ip = f'{network_base_ip}.{number}'
        running_thread = threading.Thread(target=deep_scan, args=(computer_ip, result_queue))
        threads.append(running_thread)
        running_thread.start()
    for running_thread in threads:
        running_thread.join()
    ip_results = {}
    while not result_queue.empty():
        queue_dict = result_queue.get()
        ip_results[queue_dict['computer_ip']] = queue_dict['source']
    return ip_results


def get_host_ip():
    return socket.gethostbyname(socket.gethostname())


def get_host_mac():
    for interface in psutil.net_if_addrs():
        if psutil.net_if_addrs()[interface][0].address:
            return psutil.net_if_addrs()[interface][0].address


def get_network_macs():
    arp_result = subprocess.check_output(['arp', '-a']).decode(constants.UTF8).splitlines()
    mac_list = {}
    for arp_line in arp_result[3:-1]:
        mac_address = arp_line.split()[1].upper()
        host_name = None
        if mac_address.upper() in known_hosts:
            host_name = known_hosts[mac_address]
        mac_list[mac_address] = {'host_ip': arp_line.split()[0], 'host_name': host_name}
    return mac_list


def scan_ip(ip_address):
    try:
        socket.gethostbyaddr(ip_address)
        return True
    except socket.herror:
        return False


def scan_ssh(computer_ip, port=22, timeout=1):
    try:
        test_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        test_socket.settimeout(timeout)
        test_socket.connect((computer_ip, port))
    except Exception as exception:
        return False
    else:
        test_socket.close()
    return True


def scan_ssh_host(computer_ip, port=22, timeout=1):
    ssh_client = paramiko.SSHClient()
    ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        ssh_client.connect(computer_ip, port=port, timeout=timeout)
        return True
    except Exception as exception:
        return False
    finally:
        ssh_client.close()


def ping_host(computer_ip):
    result = subprocess.run(['ping', '-n', '1', '-w', '250', computer_ip], stdout=subprocess.PIPE)
    if result.returncode == 0:
        result = subprocess.run(['ping', '-n', '1', '-w', '250', computer_ip], stdout=subprocess.PIPE)
        if result.returncode == 0:
            return True         
    return False


def deep_scan(computer_ip, result_queue):
    if scan_ssh(computer_ip):
        result_queue.put({'computer_ip': computer_ip, 'source': f'{DARK_RED}SSH{RESET}'})
    elif ping_host(computer_ip):
        result_queue.put({'computer_ip': computer_ip, 'source': 'png'})
    elif scan_ip(computer_ip):
        result_queue.put({'computer_ip': computer_ip, 'source': 'sck'})
    elif scan_ssh_host(computer_ip):
        result_queue.put({'computer_ip': computer_ip, 'source': 'sh2'})


def find_mac_by_ip(dictionary, mac_ip):
    for mac_value, details in dictionary.items():
        if details['host_ip'] == mac_ip:
            return mac_value
    return None


if __name__ == '__main__':
    methods.clear_screen()
    colorama.init()
    print('Scanning...')
    network_ip = '192.168.0'
    print('- Host IP...')
    running_host_ip = get_host_ip()
    print('- Host MAC...')
    running_host_mac = get_host_mac()
    print('- Network MACs...')
    network_macs = get_network_macs()

    host_table = []
    for mac in known_hosts:
        host_ip = '---.---.-.0'
        status = OFFLINE
        if mac in network_macs:
            host_ip = network_macs[mac]['host_ip']
            status = ATLINE
        elif mac == running_host_mac:
            host_ip = running_host_ip
            status = ATLINE
            
        if host_ip.startswith('192') and not (host_ip.endswith('252') or host_ip.endswith('255') or host_ip.endswith('.1')) and status == ATLINE:
            status = ONLINE
            
        host_table.append({'host_ip': host_ip, 'host_mac': mac, 'host_name': known_hosts[mac], 'host_status': status, 'host_source': 'arp' if status.startswith(ATLINE) else 'knh'})

    print('- Network IPs...')
    network_ips = scan_network(network_ip)

    unknown_ips = {}
    for host_element in host_table:
        if host_element['host_ip'] in network_ips:
            host_element['host_source'] = network_ips[host_element['host_ip']]
            network_ips[host_element['host_ip']] = 'done'

    for network_ip_item in network_ips:
        if network_ips[network_ip_item] != 'done':
            mac_addr = find_mac_by_ip(network_macs, network_ip_item)
            if not mac_addr:
                mac_addr = f'{RED}00-00-00-00-00-00{RESET}'
            hostname = '[={(?)}=] -'
            if network_ip_item == '192.168.0.1':
                hostname = 'Router'
            host_table.append({'host_ip': network_ip_item, 'host_mac': mac_addr, 'host_name': hostname, 'host_status': INLINE, 'host_source': network_ips[network_ip_item]})

    sorted_list = sorted(host_table, key=lambda element: int(element['host_ip'].split('.')[-1]))

    print('_' * HORIZONTAL_LINE)
    counter = 0
    count_online = 0
    print('| [QTT] HOST IP       - HOST MAC ADDRESS  - SRC [STATUS ] HOST TYPE HOST NAME       |')
    for item in sorted_list:
        counter += 1
        counter_val = str(counter).rjust(COUNTER_JUSTIFY)
        if item["host_status"] == ONLINE:
            count_online += 1
        print(f'| [{counter_val}] {item["host_ip"].ljust(HOST_JUSTIFY)} - {item["host_mac"]} - {item["host_source"]} [{item["host_status"].ljust(HOST_STATUS)}] {item["host_name"].ljust(WORD_ALIGNMENT)}|')
    print('‾' * HORIZONTAL_LINE)
    print(f'ONLINE [{count_online}]')

