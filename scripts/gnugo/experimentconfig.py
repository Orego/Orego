#!/usr/bin/python

# The experiment will run this many games in each condition for each of black and white
# Handy heuristic: with 1000 msec/move, each 9x9 game tends to take about one minute.
GAMES_PER_COLOR = 1

# Defines parameters for the current experiment
PARAMS = [\
'threads=2 playouts=8000 player=BestReply1Player gestate=1',
'threads=2 playouts=8000 player=BestReply1Player gestate=2',
'threads=2 playouts=8000 player=BestReply1Player gestate=3',
'threads=2 playouts=8000 player=BestReply1Player gestate=4',

]
