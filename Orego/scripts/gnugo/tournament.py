#!/usr/bin/python                                                                                                                               

# Pits different Orego parameter sets against GNU Go                                                                                                  

from systemconfig import *
from experimentconfig import *
from commands import *
from os import *

# Get name of this machine and find appropriate file in which to write output                                                                         

host = getoutput('echo `hostname`')
output = file(RESULT_DIR + '/' + host + '.dump', 'w')

output.write('%d games * 2 colors * %d conditions = %d total games per machine\n' %
             (GAMES_PER_COLOR, len(PARAMS), GAMES_PER_COLOR * 2 * len(PARAMS)))

gnu = "'" + GNU_GO + " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko'"

pnum = 0
for p in PARAMS:
    pnum = pnum + 1
    orego="'" + JAVA +" -Xmx1024M -ea -server -cp " + OREGO_ROOT + "/bin orego.ui.Orego " + p + "'"
    output.write('CONDITION: ' + p + '\n')
    output.write('Orego black\n')
    sgf = "%scondition%dblack" % (host, pnum)
    output.flush()
    
    pin, pout = popen2('nice -19 ' + OREGO_ROOT + '/scripts/gnugo/twogtp.py --black ' + orego 
                       + ' --white ' + gnu
                       + ' --verbose 0 --komi 7.5 --size 19 --games ' 
                       + str(GAMES_PER_COLOR) + ' --sgfbase ' + sgf)
    for line in pout.readlines():
        output.write(line)
    pout.close()
    pin.close()
    output.write('Orego white\n')
    sgf = "%scondition%dwhite" % (host, pnum)
    output.flush()
    pin, pout = popen2('nice -19 ' + OREGO_ROOT + '/scripts/gnugo/twogtp.py --black ' + gnu 
                       + ' --white ' + orego
                       + ' --verbose 0 --komi 7.5 --size 19 --games ' + str(GAMES_PER_COLOR)
                       + ' --sgfbase ' + sgf)
    for line in pout.readlines():
        output.write(line)
    pout.close()
    pin.close()

output.write('DONE\n')
output.close()
