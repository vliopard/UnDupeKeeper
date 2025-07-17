import time
import constants
from platform import system
from functools import wraps
from screen import get_terminal_width
from datetime import datetime, timedelta


def print_debug(*arguments):
    if constants.DEBUG_MODE:
        print(constants.NEW_LINE)
        double_line()
        double_line(constants.TEXT_DEBUG_MESSAGE_START)
        double_line()
        print_display(*arguments)
        double_line()
        double_line(constants.TEXT_DEBUG_MESSAGE_END)
        double_line()
        print(constants.NEW_LINE)


def print_box(msg):
    print_underline()
    print_display(msg)
    print_overline()


def print_display(*arguments):
    if not constants.RUN_GUI:
        argument_count = len(arguments)
        if argument_count > 0:
            display_text = constants.SYMBOL_EMPTY
            for element in arguments:
                display_text = display_text + str(element) + constants.SYMBOL_BLANK
            display_text = display_text.rstrip()
            if display_text != constants.DEFAULT_L:
                print(display_text)
        else:
            return 0
    return None


def timed(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        start_time = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        time_start = time.time()
        result = func(*args, **kwargs)
        time_end = time.time()
        end_time = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
        time_report = [f'Start time: {start_time}', f'End time:   {end_time}', f"Function {func.__name__} ran in {timedelta(seconds=(time_end - time_start))}"]
        print(f'{section_line(constants.SYMBOL_EQ, get_terminal_width() - 35)}')
        for time_detail in time_report:
            print(time_detail)
        print(f'{section_line(constants.SYMBOL_EQ, get_terminal_width() - 35)}')
        return result
    return wrapper


def section_line(style, size):
    return style * size


def print_underline():
    print_display(constants.SYMBOL_UNDERLINE * (get_terminal_width()))


def print_overline():
    print_display(constants.SYMBOL_OVERLINE * (get_terminal_width()))


def double_line(message=None):
    if message:
        message = f'== {message} '
        print_display(message + constants.SYMBOL_EQ * (get_terminal_width() - len(message)))
    else:
        print_display(constants.SYMBOL_EQ * (get_terminal_width()))


def get_system():
    return system()