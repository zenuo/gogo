#!/usr/bin/python3
import os
import sys
import time

__PID_COMMAND = "ps -ef | grep gogo.jar | grep -v grep | awk '{print $2}'"
__START_COMMAND = 'nohup java -Xms64m -Xmx64m -Dport=5001 -jar gogo.jar > /dev/null 2>&1 &'
__STOP_COMMAND = 'kill %s'
__HELP = 'Arguments:\n    start\n    restart\n    stop\n'


def get_pid():
    return os.popen(__PID_COMMAND).read()


def start():
    pid = get_pid()
    if pid != '':
        print('Error, program is running on %s' % pid, file=sys.stderr)
        exit(1)
    else:
        os.popen(__START_COMMAND)
        pid = get_pid()
        if pid != '':
            print('Start, pid is %s' % pid)
            exit(0)
        else:
            print('Error, Unable to start', file=sys.stderr)
            exit(1)


def stop():
    pid = get_pid()
    if pid != '':
        os.popen(__STOP_COMMAND % pid)
        print('stop, pid is %s' % pid)
        exit(0)
    else:
        print('Error, program is not running', file=sys.stderr)
        exit(1)


def restart():
    pid = get_pid()
    if pid != '':
        os.popen(__STOP_COMMAND % pid)
        time.sleep(5)
        start()
    else:
        start()


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print(__HELP)
    else:
        arg = sys.argv[1]
        if arg == 'start':
            start()
        elif arg == 'stop':
            stop()
        elif arg == 'restart':
            restart()
        else:
            print('bad argument"%s"\n%s' % (arg, __HELP), file=sys.stderr)
