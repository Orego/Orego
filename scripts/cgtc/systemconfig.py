#!/usr/local/bin/python

# Defines some variables indicating the locations of files and machines on your system

# Locations of files
# (For Olin Linux lab)
#GNU_GO = '/usr/local/bin/gnugo'
#JAVA = '/usr/java/jdk1.6.0/bin/java'
#OREGO_ROOT = '/home/cs/drake/workspace/Orego5'
#RESULT_DIR = '/home/cs/drake/results'
# (For Olin Mac lab)
JAVA = '/usr/bin/java'
OREGO_ROOT = '/Network/Servers/maccsserver.lclark.edu/Users/dtillis/Documents/workspace/Orego'
RESULT_DIR = '/Network/Servers/maccsserver.lclark.edu/Users/dtillis/Documents/orego/results'
# (For my home machine)
#GNU_GO = '/usr/local/bin/gnugo'
#JAVA = '/usr/bin/java'
#OREGO_ROOT = '/Users/drake/Documents/workspace/Orego'
#RESULT_DIR = '/Users/drake/Documents/go/orego/results'


# Machines and user
# (For Olin Linux lab)
#USER = 'drake'
#MACHINES = ['cs%d.lclark.edu' % n for n in range(4, 26)]
# (For Olin Mac Lab)
USER = 'drake'
MACHINES = ['maclabcs%d' % n for n in range(1, 14)]
# (For my home machine)
#USER = 'drake'
#MACHINES = ['peter-drakes-computer.local']
