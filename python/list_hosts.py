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

network_ip = '192.168.0'
ONE = '192.168.0.1'

HOST_STATUS = 8
HOST_JUSTIFY = 15
COUNTER_JUSTIFY = 3
HORIZONTAL_LINE = 87
WORD_ALIGNMENT = 26

RED = '\033[0;91m'
GREEN = '\033[0;92m'
YELLOW = '\033[0;93m'
BLUE = '\033[0;94m'
RESET = '\033[0;0m'
DARK_RED = Fore.RED

ONLINE = f'{GREEN}Online*{RESET}'
INLINE = f'{YELLOW}Online?{RESET}'
ON_LINE = f'{BLUE}Online {RESET}'
OFFLINE = f'{RED}Offline{RESET}'

ARP = 'arp'
PING = 'png'
KNOWN_HOST = 'knh'
SSH2 = 'sh2'
SSH = f'{DARK_RED}SSH{RESET}'
SOCKET = 'sck'
SELF = 'slf'
IPC = 'ipc'
NO_DISCOVERY = '???'

HOST_IP = 'host_ip'
HOST_MAC = 'host_mac'
HOST_NAME = 'host_name'
HOST_DESCRIPTION = 'host_description'
HOST_DHCP = 'host_dhcp'
ONLINE_STATUS = 'host_status'
DISCOVERY_SOURCE = 'host_source'
DONE = 'done'
DICT_SOURCE = 'source'
COMPUTER_IP = 'computer_ip'
ARP_TYPE = 'arp_type'
ADAPTER = 'adapter_name'

ZERO = f'{RED}00-00-00-00-00-00{RESET}'
MASK = '---.---.-.0'
NO_NAME = '[={(?)}=] -'
header = '| [QTT] HOST IP         - HOST MAC ADDRESS  - SRC [STATUS ] HOST TYPE HOST NAME       |'


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
        ip_results[queue_dict[COMPUTER_IP]] = queue_dict[DICT_SOURCE]
    return ip_results


def get_host_ipv4s():
    addresses = socket.getaddrinfo(socket.gethostname(), None)
    ipv4_addresses = {addr[4][0] for addr in addresses if addr[0] == socket.AF_INET}
    return ipv4_addresses


def get_host_macs():
    adapters_info = psutil.net_if_addrs()
    local_list = {}
    for adapter_name, addresses in adapters_info.items():
        ip_address = None
        mac_address = None
        for address in addresses:
            if address.family == socket.AF_INET:
                ip_address = address.address
            elif address.family == psutil.AF_LINK:
                mac_address = address.address
        if ip_address and mac_address:
            local_list[ip_address] = {HOST_MAC: mac_address, ADAPTER: adapter_name}
    return local_list


def get_network_macs():
    arp_result = subprocess.check_output(['arp', '-a']).decode(constants.UTF8).splitlines()
    ip_list = {}
    arp_list = []
    for arp_element in arp_result:
        element = ' '.join(arp_element.split()).split(' ')
        if len(element) == 3:
            arp_list.append(element)
    
    for arp_line in arp_list:
        mac_address = arp_line[1].upper()
        arp_type = arp_line[2].upper()
        h_name = None
        if mac_address.upper() in known_hosts:
            h_name = known_hosts[mac_address]
        ip_list[arp_line[0]] = {HOST_MAC: mac_address, HOST_NAME: h_name, ARP_TYPE: arp_type}
    return ip_list


def scan_ip(ip_address):
    try:
        socket.gethostbyaddr(ip_address)
        return True
    except socket.herror:
        return False


def scan_ssh(computer_ip, port=22, timeout=5):
    try:
        test_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        test_socket.settimeout(timeout)
        test_socket.connect((computer_ip, port))
    except socket.error:
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
    except TimeoutError:
        return False
    finally:
        ssh_client.close()


def ping_ip(computer_ip):
    result = subprocess.run(['ping', '-n', '1', '-w', '250', computer_ip], stdout=subprocess.PIPE)
    return result.returncode


def ping_host(computer_ip):
    if ping_ip(computer_ip) == 0:
        if ping_ip(computer_ip) == 0:
            return True         
    return False


def deep_scan(computer_ip, result_queue):
    if scan_ssh(computer_ip):
        result_queue.put({COMPUTER_IP: computer_ip, DICT_SOURCE: SSH})
    elif ping_host(computer_ip):
        result_queue.put({COMPUTER_IP: computer_ip, DICT_SOURCE: PING})
    elif scan_ip(computer_ip):
        result_queue.put({COMPUTER_IP: computer_ip, DICT_SOURCE: SOCKET})
    elif scan_ssh_host(computer_ip):
        result_queue.put({COMPUTER_IP: computer_ip, DICT_SOURCE: SSH2})


def find_mac_by_ip(dictionary, mac_ip):
    for mac_value, details in dictionary.items():
        if details[HOST_IP] == mac_ip:
            return mac_value
    return None


def find_ip_by_mac(ip_mac_list, mac_address):
    for entry in ip_mac_list:
        mac_ip, info = entry
        if info[HOST_MAC] == mac_address:
            return mac_ip
    return None


def get_mac_in_table(table_data, mac_value):
    for element in table_data:
        if mac_value == element[HOST_MAC]:
            return element
    return None


def ipconfig_to_dict():
    raw_command = subprocess.check_output(['ipconfig', '/all'])
    dict_command = {}
    current_super_key = ''
    raw_command = raw_command.decode('utf-8')
    split_command = raw_command.split('\r\n')
    split_command = [ip_line for ip_line in split_command if ip_line]

    for command_item in split_command:
        split_item = command_item.split(' : ')
        if len(split_item) == 1 and not split_item[0].startswith(' '):
            current_super_key = split_item[0]
            if current_super_key.endswith(':'):
                current_super_key = current_super_key.replace(':', '')

            dict_command[current_super_key] = {}

        try:
            split_key = split_item[0].rstrip()
            split_value = split_item[1].rstrip()

            if ' .' in split_key:
                temp_key = split_key.replace('.', '')
                temp_key = temp_key.rstrip()
                temp_key = temp_key.lstrip()
                split_key = temp_key

            if '","' in split_value:
                temp_value = split_value.replace('"', '')
                temp_value = temp_value.rsplit()
                split_value = temp_value

            try:
                if dict_command[current_super_key][split_key] is not None:
                    if isinstance(dict_command[current_super_key][split_key], list):
                        dict_command[current_super_key][split_key].append(split_value)
                    else:
                        temp_value = dict_command[current_super_key][split_key]
                        dict_command[current_super_key][split_key] = [temp_value]
                        dict_command[current_super_key][split_key].append(split_value)
            except KeyError:
                dict_command[current_super_key][split_key] = split_value

        except IndexError:
            pass

    known = dict()
    for key, value in dict_command.items():
        if 'IPv4 Address' in value:
            physical_address = ''
            description = ''
            dhcp_server = ''
            if 'Physical Address' in value:
                physical_address = value['Physical Address']
            if 'Description' in value:
                description = value['Description']
            if 'DHCP Server' in value:
                dhcp_server = value['DHCP Server']
            known[value['IPv4 Address'].split('(')[0]] = {HOST_MAC: physical_address, HOST_DESCRIPTION: description, HOST_DHCP: dhcp_server}
    return known


if __name__ == '__main__':
    methods.clear_screen()
    colorama.init()
    print('Scanning:')
    print('* Host IP...')
    running_host_ip_addresses = get_host_ipv4s()
    print('* Host MAC...')
    running_host_mac_addresses = get_host_macs()

    host_ips_macs = {}
    for ip in running_host_ip_addresses:
        host_ips_macs[ip] = running_host_mac_addresses[ip][HOST_MAC]

    print('* Network MACs...')
    network_ips_and_macs = get_network_macs()
    network = ipconfig_to_dict()

    for mac_element in running_host_mac_addresses:
        if mac_element in network:
            running_host_mac_addresses[mac_element].update(network[mac_element])

    host_table = []
    for host_ip in network_ips_and_macs:
        host_mac = network_ips_and_macs[host_ip][HOST_MAC]
        discovery_source = ARP
        host_status = INLINE
        host_name = NO_NAME
        if host_mac in known_hosts:
            host_name = known_hosts[host_mac]
            discovery_source = KNOWN_HOST
            host_status = ONLINE
            if host_name.startswith('[Device'):
                host_status = ON_LINE
        else:
            for i in running_host_mac_addresses:
                if HOST_DHCP in running_host_mac_addresses[i] and host_ip == running_host_mac_addresses[i][HOST_DHCP]:
                    host_name = '[Device ] OTDS-VPN-Co.'
                    discovery_source = IPC
                    host_status = ON_LINE
                    
        host_table.append({HOST_IP: host_ip, HOST_MAC: host_mac, HOST_NAME: host_name, ONLINE_STATUS: host_status, DISCOVERY_SOURCE: discovery_source})

    for host_mac in known_hosts:
        item = get_mac_in_table(host_table, host_mac)
        if not item:
            host_table.append({HOST_IP: MASK, HOST_MAC: host_mac, HOST_NAME: known_hosts[host_mac], ONLINE_STATUS: OFFLINE, DISCOVERY_SOURCE: KNOWN_HOST})

    print('* Network IPs...')
    network_ips = scan_network(network_ip)

    for host_element in host_table:
        if host_element[HOST_IP] in network_ips:
            host_element[DISCOVERY_SOURCE] = network_ips[host_element[HOST_IP]]
            network_ips[host_element[HOST_IP]] = DONE

    for network_ip_item in network_ips:
        if network_ips[network_ip_item] != DONE:
            dis_source = network_ips[network_ip_item]
            hostname = NO_NAME
            mac_addr = ZERO
            online_status = INLINE
            if network_ip_item in network_ips_and_macs:
                mac_addr = network_ips_and_macs[network_ip_item][HOST_MAC]
            if network_ip_item in running_host_mac_addresses:
                dis_source = SELF
                online_status = ONLINE
                mac_addr = running_host_mac_addresses[network_ip_item][HOST_MAC]
                if mac_addr in known_hosts:
                    hostname = known_hosts[mac_addr]
                else:
                    hostname = 'This Host.'
            if network_ip_item == ONE:
                hostname = 'Router'
            host_table.append({HOST_IP: network_ip_item, HOST_MAC: mac_addr, HOST_NAME: hostname, ONLINE_STATUS: online_status, DISCOVERY_SOURCE: dis_source})

    sorted_list = sorted(host_table, key=lambda element: int(element[HOST_IP].split('.')[-1]))

    print('_' * HORIZONTAL_LINE)
    counter = 0
    count_online = 0
    print(header)
    for item in sorted_list:
        counter += 1
        counter_val = str(counter).rjust(COUNTER_JUSTIFY)
        if item[ONLINE_STATUS] == ONLINE:
            count_online += 1
        print(f'| [{counter_val}] {item[HOST_IP].ljust(HOST_JUSTIFY)} - {item[HOST_MAC]} - {item[DISCOVERY_SOURCE]} [{item[ONLINE_STATUS].ljust(HOST_STATUS)}] {item[HOST_NAME].ljust(WORD_ALIGNMENT)}|')
    print('‾' * HORIZONTAL_LINE)
    print(f'ONLINE [{count_online}]')
