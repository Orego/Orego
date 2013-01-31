#!/usr/bin/python

# Collates results of experiments

from sys import *
from systemconfig import *
from experimentconfig import *
import re

# Any command-line arguments to this program are treated as machines to exclude from the collation
excluded = argv[1:]

results = dict()

# regex for matching the outputs
pattern = re.compile('(CONDITION: .*?)?\nOrego (black|white).*?Black (\d+), white (\d+)', re.DOTALL)

# loop through all machines collecting results
for machine in MACHINES:
    if machine not in excluded:
        
        # open file
        filename = RESULT_DIR + '/' + machine + '.dump'
        try:  
            file = open(filename)
            text = file.read()
            
            # machine specific runs
            mwins = 0
            mruns = 0
            
            # loop through all occurrences of the pattern
            matches = pattern.findall(text)
            for match in matches:
                if len(match[0]) > 0: # change conditions if there is a new one
            	   condition = match[0]
                   
            	color = match[1]
            	bwins = match[2]
            	wwins = match[3]  
                runs = int(bwins) + int(wwins)
                
                if color == 'black':
                    wins = int(bwins)
                else:
                    wins = int(wwins)
                
                if condition not in results:
                    results[condition] = (0, 0)
                
                # results holds an ordered pair: runs first, then wins    
                results[condition] = (results[condition][0] + runs, results[condition][1] + wins)
                mwins = mwins + wins
                mruns = mruns + runs
                
            done = 0 < text.find('DONE') # find at least one occurrence of 'DONE'    
            if done:
                print machine, 'finished, wins:',
            else:
                print machine, 'NOT finished, wins:',
            print '%1.3f = %d / %d' % (float(mwins)/max(1, mruns), mwins, mruns)
            
        except IOError: # File not present
            print filename, 'could not be opened'


# Print the accumulated results
for r in results.keys():
    wins = results[r][1]
    runs = results[r][0]
    rate = float(wins) / runs
    print '%s\nRESULT: %1.3f = %d / %d' % (r, rate, wins, runs)
            
