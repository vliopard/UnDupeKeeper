import os
import inspect

import logging
logger = logging.getLogger('TOOLS')

NT = 'nt'
SLASH = 92


if os.name == NT:
    import msvcrt
else:
    import sys
    import termios
    import atexit
    from select import select


class KBHit:

    def __init__(self):
        if os.name == NT:
            pass
        else:
            self.fd = sys.stdin.fileno()
            self.new_term = termios.tcgetattr(self.fd)
            self.old_term = termios.tcgetattr(self.fd)
            self.new_term[3] = (self.new_term[3] & ~termios.ICANON & ~termios.ECHO)
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.new_term)
            atexit.register(self.set_normal_term)

    def set_normal_term(self):
        if os.name == NT:
            pass
        else:
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.old_term)

    def getch(self):
        s = ''
        if os.name == NT:
            return msvcrt.getch().decode('utf-8')
        else:
            return sys.stdin.read(1)

    def getarrow(self):
        if os.name == NT:
            msvcrt.getch()
            c = msvcrt.getch()
            arrows = [72, 77, 80, 75]
        else:
            c = sys.stdin.read(3)[2]
            arrows = [65, 67, 66, 68]
        return arrows.index(ord(c.decode('utf-8')))

    def kbhit(self):
        if os.name == NT:
            return msvcrt.kbhit()
        else:
            dr, dw, de = select([sys.stdin], [], [], 0)
            return dr != []

    def check(self):
        if self.kbhit():
            c = self.getch()
            if ord(c) == SLASH:
                self.set_normal_term()
                return True
        return False


def line_number():
    return f"{inspect.currentframe().f_back.f_lineno:04d}"
