#!/usr/local/bin/python

# Runs tournament on each of a number of machines

# IMPORTANT NOTE: Running broadcast.py has two deliberate side effects which are intended to clean up past experiments:
# it kills all of USER's processes on any of MACHINEs and it deletes all *.dump files in RESULT_DIR.

from systemconfig import *
from commands import *
from os import *

# Kill all of user's existing processes on the test machines
for machine in MACHINES:
    print getoutput('ssh "' + USER + '@' + machine + '" "kill -9 -1"')

# Delete the existing results files
print getoutput('rm ' + RESULT_DIR + '/*dump')

# Run the experiments
for machine in MACHINES:
    # Python note: I would use getoutput() instead of system() here, as I did everywhere else,
    # but getoutput() mysteriously adds a semicolon after the &, which confuses the
    # remote machine.
    system('ssh "' + USER + '@' + machine + '" "' + OREGO_ROOT + '/orego/experiment/tournament.py" &')


