import os
import sys
import time
import json
import logging
import argparse

import UnDupeKeeper

from tqdm import tqdm
from functools import wraps
from datetime import datetime, timedelta
from PySide6 import QtGui, QtCore, QtWidgets

show = logging.getLogger('HDDHL')


def timed(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        time_start = time.time()
        result = func(*args, **kwargs)
        time_end = time.time()
        end_time = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        time_report = [f'Start time: {start_time}', f'End time:   {end_time}', f"Function {func.__name__} ran in {timedelta(seconds=(time_end - time_start))}"]
        print('=' * 100)
        for time_detail in time_report:
            print(time_detail)
        print('=' * 100)
        return result
    return wrapper


class GuidedUserInterface(QtWidgets.QDialog):
    def __init__(self, cli=False, parent=None):
        super(GuidedUserInterface, self).__init__(parent)
        self.resize(1024, 768)
        self.setWindowTitle('DedupeGUI')

        self.cli = cli
        self.json_file = 'UnDupeKeeper.json'

        self.hard_disk_drive_hash_list = {}

        self.database_file_count = 0
        self.file_count = 0
        self.hash_count = 0
        self.total_files = 0

        self.current_directory = UnDupeKeeper.config.get('PATHS', 'LOAD_PATH')
        print(f'LOADING [{self.current_directory}]')

        self.ui_items = QtWidgets.QTreeView()
        self.ui_items.sortByColumn(1, QtCore.Qt.SortOrder.AscendingOrder)
        self.ui_items.setEditTriggers(QtWidgets.QAbstractItemView.EditTrigger.NoEditTriggers)
        self.ui_items.setSelectionBehavior(QtWidgets.QAbstractItemView.SelectionBehavior.SelectRows)
        self.ui_items.setSelectionMode(QtWidgets.QAbstractItemView.SelectionMode.ExtendedSelection)
        self.ui_items.setModel(QtGui.QStandardItemModel())

        self.load_directory_button = QtWidgets.QPushButton('Load Directory')
        self.load_directory_button.clicked.connect(self.load_directory)

        self.checked_items_button = QtWidgets.QPushButton('Get Checked Items')
        self.checked_items_button.clicked.connect(self.print_checked_items)

        self.uncheck_all_button = QtWidgets.QPushButton('Uncheck All')
        self.uncheck_all_button.clicked.connect(self.uncheck_all)

        self.toggle_expand_collapse_button = QtWidgets.QPushButton('Collapse All')
        self.toggle_expand_collapse_button.clicked.connect(self.toggle_expand_collapse)

        self.toggle_hash_sort_button = QtWidgets.QPushButton('Toggle Hash Order')
        self.toggle_hash_sort_button.clicked.connect(self.toggle_hash_sort_order)

        self.toggle_file_sort_button = QtWidgets.QPushButton('Toggle File Order')
        self.toggle_file_sort_button.clicked.connect(self.toggle_file_sort_order)

        button_layout = QtWidgets.QHBoxLayout()
        button_layout.addWidget(self.load_directory_button)
        button_layout.addWidget(self.checked_items_button)
        button_layout.addWidget(self.uncheck_all_button)
        button_layout.addWidget(self.toggle_expand_collapse_button)
        button_layout.addWidget(self.toggle_hash_sort_button)
        button_layout.addWidget(self.toggle_file_sort_button)

        main_layout = QtWidgets.QVBoxLayout()
        main_layout.addWidget(self.ui_items)
        main_layout.addLayout(button_layout)

        self.gui_status_bar = QtWidgets.QStatusBar()

        self.gui_progress_bar = QtWidgets.QProgressBar(self)
        self.gui_status_bar.addPermanentWidget(self.gui_progress_bar)
        self.gui_progress_bar.setMinimum(0)
        self.gui_progress_bar.setMaximum(100)
        self.gui_progress_bar.hide()

        main_layout.addWidget(self.gui_status_bar)

        self.setLayout(main_layout)

        self.hash_sort_order = QtCore.Qt.SortOrder.AscendingOrder
        self.file_sort_order = QtCore.Qt.SortOrder.AscendingOrder
        self.expanded = True

        if not cli:
            self.create_model()

        self.gui_status_bar.showMessage("[SYSTEM FUNCTIONAL]")

    def load_directory(self):
        self.gui_status_bar.showMessage(f"[LOADING FILE LIST... WAIT]")
        self.ui_items.model().clear()

        selected = str(QtWidgets.QFileDialog.getExistingDirectory(self, "Select Directory...", self.current_directory))
        print(f'SELECTED [{selected}]')

        if selected:
            self.current_directory = selected

            UnDupeKeeper.config.set('PATHS', 'LOAD_PATH', self.current_directory)
            with open(UnDupeKeeper.SETTINGS_FILE, 'w') as configfile:  # save
                UnDupeKeeper.config.write(configfile)

            self.hash_directory_files()
            self.save_database()
            self.create_model()
        else:
            self.gui_status_bar.showMessage(f"[OPERATION ABORTED]")

    def create_model(self):
        model = self.ui_items.model()
        model.clear()
        model.setHorizontalHeaderLabels(['HASH/FILES'])
        self.ui_items.sortByColumn(0, QtCore.Qt.SortOrder.AscendingOrder)

        data = {}
        try:
            with open(self.json_file, 'r') as hdd_hl:
                data = json.load(hdd_hl)
        except FileNotFoundError:
            print('[NO DATA FOUND]')

        for k, v in data.items():
            if len(v) > 1:
                root_node = QtGui.QStandardItem(k.upper())
                for child in v:
                    child_node = QtGui.QStandardItem(child)
                    child_node.setCheckable(True)
                    root_node.appendRow(child_node)
                root_node.setData(len(v), QtCore.Qt.UserRole + 1)
                model.appendRow(root_node)

        self.ui_items.expandAll()
        self.ui_items.resizeColumnToContents(0)
        # self.status_bar.showMessage("Model created and list loaded.")

    def toggle_expand_collapse(self):
        if self.expanded:
            self.ui_items.collapseAll()
            self.toggle_expand_collapse_button.setText('Expand All')
            self.gui_status_bar.showMessage("[ALL ITEMS COLLAPSED]")
        else:
            self.ui_items.expandAll()
            self.toggle_expand_collapse_button.setText('Collapse All')
            self.gui_status_bar.showMessage("[ALL ITEMS EXPANDED]")
        self.expanded = not self.expanded

    def uncheck_all(self):
        model = self.ui_items.model()
        root_item = model.invisibleRootItem()
        self._uncheck_recursive(root_item)
        self.gui_status_bar.showMessage("[ALL ITEMS UNCHECKED]")

    def _uncheck_recursive(self, item):
        for i in range(item.rowCount()):
            child = item.child(i)
            if child.isCheckable():
                child.setCheckState(QtCore.Qt.CheckState.Unchecked)
            self._uncheck_recursive(child)

    def get_checked_items(self):
        model = self.ui_items.model()
        root_item = model.invisibleRootItem()
        checked_items = []
        self._get_checked_recursive(root_item, checked_items)
        return checked_items

    def _get_checked_recursive(self, item, checked_items):
        for i in range(item.rowCount()):
            child = item.child(i)
            if child.isCheckable() and child.checkState() == QtCore.Qt.CheckState.Checked:
                checked_items.append(child.text())
            self._get_checked_recursive(child, checked_items)

    def print_checked_items(self):
        checked_items = self.get_checked_items()
        print("CHECKED ITEMS:", checked_items)
        self.gui_status_bar.showMessage(f"[CHECKED ITEMS: {len(checked_items)}]")

    def handle_state_change(self, checked_item):
        state = checked_item.checkState()
        indices = self.ui_items.selectedIndexes()
        for idx in indices:
            item = idx.model().itemFromIndex(idx)
            item.setCheckState(state)

    def toggle_hash_sort_order(self):
        if self.hash_sort_order == QtCore.Qt.SortOrder.AscendingOrder:
            self.hash_sort_order = QtCore.Qt.SortOrder.DescendingOrder
        else:
            self.hash_sort_order = QtCore.Qt.SortOrder.AscendingOrder
        self.ui_items.sortByColumn(0, self.hash_sort_order)
        self.gui_status_bar.showMessage(f"[HASH ORDER TOGGLED TO {'ASCENDING' if self.hash_sort_order == QtCore.Qt.SortOrder.AscendingOrder else 'DESCENDING'}]")

    def toggle_file_sort_order(self):
        if self.file_sort_order == QtCore.Qt.SortOrder.AscendingOrder:
            self.file_sort_order = QtCore.Qt.SortOrder.DescendingOrder
        else:
            self.file_sort_order = QtCore.Qt.SortOrder.AscendingOrder
        self.sort_list_by_file_count()
        self.gui_status_bar.showMessage(f"[FILE ORDER TOGGLED TO {'ASCENDING' if self.file_sort_order == QtCore.Qt.SortOrder.AscendingOrder else 'DESCENDING'}]")

    def sort_list_by_file_count(self):
        model = self.ui_items.model()
        items = []
        for row in range(model.rowCount()):
            item = model.item(row)
            items.append((item.rowCount(), item))

        items.sort(reverse=self.file_sort_order == QtCore.Qt.SortOrder.DescendingOrder)

        model.clear()
        model.setHorizontalHeaderLabels(['HASH/FILES'])
        for _, item in items:
            model.appendRow(item)
        self.ui_items.expandAll()

    def count_files(self, target_directory):
        self.total_files = 0
        for root, dirs, files in os.walk(target_directory):
            self.total_files += len(files)

    @timed
    def hash_directory_files(self):
        self.gui_status_bar.showMessage(f"[SCANNING FILES: {self.current_directory}]")
        self.gui_progress_bar.setValue(0)
        self.gui_progress_bar.show()

        status_bar_format = "{desc}: {percentage:.2f}%|{bar}| {n:,}/{total:,} [{elapsed}<{remaining}, {rate_fmt}{postfix}]"

        self.count_files(self.current_directory)
        self.gui_progress_bar.setMaximum(self.total_files)
        with tqdm(total=self.total_files, bar_format=status_bar_format) as tqdm_progress_bar:
            self.database_file_count = 0
            self.hash_count = 0
            for root, dirs, files in os.walk(self.current_directory):
                for file in files:
                    self.database_file_count += 1
                    tqdm_progress_bar.update(1)
                    self.gui_progress_bar.setValue(self.database_file_count)
                    QtWidgets.QApplication.processEvents()
                    file_name = os.path.join(root, file)
                    file_name = os.path.normpath(file_name)
                    file_hash = UnDupeKeeper.get_hash(file_name, UnDupeKeeper.HASH_MD5)
                    if file_hash in self.hard_disk_drive_hash_list:
                        self.hard_disk_drive_hash_list[file_hash].append(file_name)
                    else:
                        self.hash_count += 1
                        self.hard_disk_drive_hash_list[file_hash] = [file_name]

        self.gui_progress_bar.hide()
        self.gui_status_bar.showMessage("[FILE SCANNING COMPLETE]")

    def save_database(self):
        print(f'SAVING DATABASE...')
        self.file_count = 0
        for hash_item in self.hard_disk_drive_hash_list:
            hash_total = 0
            for _ in self.hard_disk_drive_hash_list[hash_item]:
                hash_total += 1
                self.file_count += 1

        database_hash_count = len(self.hard_disk_drive_hash_list)

        count_difference = 0
        if database_hash_count != self.hash_count:
            count_difference += 1
            print(f'HASH [{database_hash_count:,}]')
        print(f'HASH [{self.hash_count:,}]')

        if self.database_file_count != self.file_count:
            count_difference += 1
            print(f'FILE [{self.database_file_count:,}]')
        print(f'FILE [{self.file_count:,}]')

        if count_difference > 0:
            print(f'DIFF [{(self.database_file_count - database_hash_count):,}]')
        print(f'DIFF [{(self.file_count - self.hash_count):,}]')

        print('SAVING...')
        with open(self.json_file, 'w') as save_file:
            json.dump(self.hard_disk_drive_hash_list, save_file)
        print('DONE.')

        self.gui_status_bar.showMessage(f'[LIST LOADED]             -=  HASH[{self.hash_count:,}]   FILE[{self.file_count:,}]   DIFF[{(self.file_count - self.hash_count):,}]  =-')


def main(search_directory):
    application = QtWidgets.QApplication(sys.argv)

    if search_directory:
        user_interface = GuidedUserInterface(False)
        user_interface.current_directory = search_directory
        user_interface.hash_directory_files()
        user_interface.save_database()

    else:
        user_interface = GuidedUserInterface()
        user_interface.show()
        sys.exit(application.exec())


if __name__ == '__main__':
    argument_parser = argparse.ArgumentParser(description='Hash files in a directory and identify duplicates.')
    argument_parser.add_argument('-d', '--directory', type=str, default=None, help='Directory to scan for files')
    arguments = argument_parser.parse_args()

    main(arguments.directory)
