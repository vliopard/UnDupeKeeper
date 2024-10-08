import os
import sys
import json
import argparse

import constants
from methods import timed
from methods import get_hash
from methods import get_level

from tqdm import tqdm
from PySide6 import QtGui, QtCore, QtWidgets

import logging
show = logging.getLogger(constants.DEBUG_MAIN)


class GuidedUserInterface(QtWidgets.QDialog):
    def __init__(self, cli=False, parent=None):
        super(GuidedUserInterface, self).__init__(parent)
        self.resize(1024, 768)
        self.setWindowTitle('DedupeGUI')

        self.cli = cli
        self.json_file = constants.STORAGE_FILE
        self.file_counter = constants.COUNTER_FILE

        self.hard_disk_drive_hash_list = {}

        self.database_file_count = 0
        self.file_count = 0
        self.hash_count = 0
        self.total_size = 0
        self.total_files = 0

        self.current_directory = constants.config.get('PATHS', 'LOAD_PATH')
        if not self.cli:
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

        if not self.cli:
            self.create_model()

        self.gui_status_bar.showMessage("[SYSTEM FUNCTIONAL]")

    def load_directory(self):
        self.gui_status_bar.showMessage(f"[LOADING FILE LIST... WAIT]")
        self.ui_items.model().clear()

        selected = str(QtWidgets.QFileDialog.getExistingDirectory(self, "Select Directory...", self.current_directory))
        print(f'SELECTED [{selected}]')

        if selected:
            self.current_directory = selected

            constants.config.set('PATHS', 'LOAD_PATH', self.current_directory)
            with open(constants.SETTINGS_FILE, constants.WRITE, encoding=constants.UTF8) as configfile:
                constants.config.write(configfile)

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
            with open(self.json_file, constants.READ, encoding=constants.UTF8) as hdd_hl:
                data = json.load(hdd_hl)
        except FileNotFoundError:
            print('[NO DATA FOUND]')
        except Exception as e:
            print(f'[EXCEPTION {e}]')

        for key, value in data.items():
            if len(value) > 1:
                root_node = QtGui.QStandardItem(key.upper())
                for child in value:
                    child_node = QtGui.QStandardItem(child)
                    child_node.setCheckable(True)
                    root_node.appendRow(child_node)
                root_node.setData(len(value), QtCore.Qt.UserRole + 1)
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
        for index in self.ui_items.selectedIndexes():
            item = index.model().itemFromIndex(index)
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

    @timed
    def count_files(self, target_directory):
        self.total_files = 0
        if os.path.isfile(self.file_counter):
            with open(self.file_counter, constants.READ, encoding=constants.UTF8) as file_count:
                count_data = json.load(file_count)
                if self.current_directory == count_data['current_dir']:
                    self.total_files = count_data['file_count']
                    return

        for root, dirs, files in tqdm(os.walk(target_directory), desc="SCANNING"):
            self.total_files += len(files)

        with open(self.file_counter, constants.WRITE, encoding=constants.UTF8) as file_count:
            count_data = {'current_dir': self.current_directory,
                          'file_count': self.total_files}
            json.dump(count_data, file_count)

    @timed
    def hash_directory_files(self):
        print(f"SCANNING FILES: [{self.current_directory}]")
        if not self.cli:
            self.gui_status_bar.showMessage(f"[SCANNING FILES: {self.current_directory}]")
            self.gui_progress_bar.setValue(0)
            self.gui_progress_bar.show()

        self.count_files(self.current_directory)
        if not self.cli:
            self.gui_progress_bar.setMaximum(self.total_files)
        with tqdm(total=self.total_files, bar_format=constants.STATUS_BAR_FORMAT) as tqdm_progress_bar:
            self.database_file_count = 0
            self.hash_count = 0
            cdir = ''
            for root, dirs, files in os.walk(self.current_directory):
                for file in files:
                    self.database_file_count += 1
                    level = get_level(root, 3)
                    if cdir != level:
                        cdir = level
                        tqdm_progress_bar.set_postfix({'DIR': cdir})
                    tqdm_progress_bar.update(1)
                    if not self.cli:
                        self.gui_progress_bar.setValue(self.database_file_count)
                        QtWidgets.QApplication.processEvents()
                    file_name = os.path.join(root, file)
                    file_name = os.path.normpath(file_name)
                    file_hash = get_hash(file_name, constants.HASH_MD5)
                    if file_hash in self.hard_disk_drive_hash_list:
                        self.hard_disk_drive_hash_list[file_hash].append(file_name)
                    else:
                        self.hash_count += 1
                        self.hard_disk_drive_hash_list[file_hash] = [file_name]

        if not self.cli:
            self.gui_progress_bar.hide()
            self.gui_status_bar.showMessage("[FILE SCANNING COMPLETE]")

    @timed
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
        with open(self.json_file, constants.WRITE, encoding=constants.UTF8) as save_file:
            json.dump(self.hard_disk_drive_hash_list, save_file)
        print('DONE.')

        self.gui_status_bar.showMessage(f'[LIST LOADED]             -=  HASH[{self.hash_count:,}]   FILE[{self.file_count:,}]   DIFF[{(self.file_count - self.hash_count):,}]  =-')


def main(search_directory):
    application = QtWidgets.QApplication(sys.argv)

    if search_directory:
        user_interface = GuidedUserInterface(True)
        user_interface.current_directory = search_directory
        user_interface.hash_directory_files()
        user_interface.save_database()

    else:
        user_interface = GuidedUserInterface()
        user_interface.show()
        sys.exit(application.exec())


if __name__ == constants.MAIN:
    argument_parser = argparse.ArgumentParser(description='Hash files in a directory and identify duplicates.')
    argument_parser.add_argument('-d', '--directory', type=str, default=None, help='Directory to scan for files')
    arguments = argument_parser.parse_args()

    main(arguments.directory)
