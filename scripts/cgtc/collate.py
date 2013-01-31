#!/usr/bin/python

# Collates results of experiments

# Note that "wins" generally means "wins for Orego" here

from sys import *
from systemconfig import *
from experimentconfig import *

# Any command-line arguments to this program are treated as machines to exclude from the collation
excluded = argv[1:]

# Results are stored in these two dictionaries
winsPerCondition = [0 for c in PARAMS]
runsPerCondition = [0 for c in PARAMS]

# Go through each file, counting wins by Orego in each condition
for machine in MACHINES:
    if machine not in excluded:
        filename = RESULT_DIR + '/' + machine + '.dump'
        try:
            totalWins = 0
            totalRuns = 0
            condition = -1
            oregoBlack = True
            done = False
            for line in file(filename):
                if line == 'DONE\n':
                    done = True
                if 'CONDITION:' in line:
                    condition += 1
                if ',' in line: # Commas appear only in score lines
                    if oregoBlack:
                        wins = int(line.split()[1][:-1]) # Black score is word 1, less the ending comma
                        losses = int(line.split()[3]) # White score is word 3
                        totalWins += wins
                        totalRuns += wins + losses
                        winsPerCondition[condition] += wins
                        runsPerCondition[condition] += wins + losses
                    else:
                        wins = int(line.split()[3])
                        losses = int(line.split()[1][:-1])
                        totalWins += wins
                        totalRuns += wins + losses
                        winsPerCondition[condition] += wins
                        runsPerCondition[condition] += wins + losses
                    oregoBlack = not oregoBlack
            if done:
                print machine, 'finished, wins:',
            else:
                print machine, 'NOT finished, wins:',
            print '%1.3f = %d / %d' % (float(totalWins)/max(1, totalRuns), totalWins, totalRuns)
        except: # File not present
            print filename, 'could not be opened'

# Print the accumulated results
for c in range(len(winsPerCondition)):
    w = winsPerCondition[c]
    r = runsPerCondition[c]
    if r > 0:
        print '%s:\t%1.3f = %d / %d' % (PARAMS[c], float(w)/r, w, r)
