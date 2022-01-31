import os
import ssl
import inspect
import smtplib

from email.mime.text import MIMEText

import parameters

import logging
logger = logging.getLogger('TOOLS')


if os.name == 'nt':
    import msvcrt
else:
    import sys
    import termios
    import atexit
    from select import select


class KBHit:

    def __init__(self):
        if os.name == 'nt':
            pass
        else:
            self.fd = sys.stdin.fileno()
            self.new_term = termios.tcgetattr(self.fd)
            self.old_term = termios.tcgetattr(self.fd)
            self.new_term[3] = (self.new_term[3] & ~termios.ICANON & ~termios.ECHO)
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.new_term)
            atexit.register(self.set_normal_term)

    def set_normal_term(self):
        if os.name == 'nt':
            pass
        else:
            termios.tcsetattr(self.fd, termios.TCSAFLUSH, self.old_term)

    def getch(self):
        s = ''
        # TODO: TRY EXCEPT UnicodeDecodeError:
        if os.name == 'nt':
            return msvcrt.getch().decode('utf-8')
        else:
            return sys.stdin.read(1)

    def getarrow(self):
        if os.name == 'nt':
            msvcrt.getch()  # skip 0xE0
            c = msvcrt.getch()
            vals = [72, 77, 80, 75]
        else:
            c = sys.stdin.read(3)[2]
            vals = [65, 67, 66, 68]
        return vals.index(ord(c.decode('utf-8')))

    def kbhit(self):
        if os.name == 'nt':
            return msvcrt.kbhit()
        else:
            dr, dw, de = select([sys.stdin], [], [], 0)
            return dr != []

    def check(self):
        if self.kbhit():
            c = self.getch()
            if ord(c) == parameters.SLASH:
                self.set_normal_term()
                return True
        return False


def lineno():
    return f"{inspect.currentframe().f_back.f_lineno:04d}"


def send_mail(title, body):
    with smtplib.SMTP_SSL(parameters.SMTP_SERV, parameters.SMTP_PORT, context=ssl.create_default_context()) as server:
        server.login(parameters.EMAIL_FROM, parameters.EMAIL_PASS)

        msg = body
        msg = MIMEText(msg)
        msg['Subject'] = title
        msg['To'] = f'{parameters.SEND_NAME} {parameters.SEND_TO}'
        msg['From'] = f'{parameters.EMAIL_NAME} {parameters.EMAIL_FROM}'

        server.sendmail(parameters.EMAIL_FROM, parameters.SEND_TO, msg.as_string())
