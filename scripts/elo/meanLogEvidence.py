#!/usr/bin/python

import os
from os.path import expanduser

MM_DIR = expanduser("~/Desktop/mm")

GAMMAS_FILE = "../../gammas.data"

RESULT_FILE = "results.dat"

TEMP_FILE = "data.tmp"

JAVA_BASE = "java -server -ea -cp ../../bin "

results = []

res_file = open(RESULT_FILE, "w")

for i in range(1, 10, 2):
    # 0 is i, 1 is MM_DIR, 2 is GAMMAS_FILE, 3 is TEMP_FILE
    generate_teams = JAVA_BASE + "orego.elo.generate.TeamGenerator -d SGFtestFiles/19/ -s {0} > {1}/input-s{0}.dat"
    compute_gammas = "{1}/mm < input-s{0}.dat > {2}"
    compute_mle = JAVA_BASE + "orego.elo.generate.EvidenceComputer -d SGFtestFiles/testSet > {3}"
    
    run = (generate_teams + " && " + compute_gammas + " && " + compute_mle).format(i, MM_DIR, GAMMAS_FILE, TEMP_FILE)
    os.system("echo '{0}'".format(run));
    
    f = open(TEMP_FILE)
    res = (i, f.readline().strip(),)
    f.close()
    res_file.write("{0}\t{1}\n".format(res[0], res[1]))
    results.append(res)
    
# clean up a bit
try:
    os.unlink(GAMMAS_FILE);
    os.unlink(TEMP_FILE);
finally:
    open(GAMMAS_FILE, 'w').close() # touch the gammas file