#!/usr/bin/python                                                                                                                               

# Runs computer go test collection.                                                                                                   

from systemconfig import *
from experimentconfig import *
from commands import *
from os import *
from glob import *

# Get name of this machine and find appropriate file in which to write output                                                                         

host = getoutput('echo `hostname`')


for p in PARAMS:
    orego="'" + JAVA +" -Xmx1024M -ea -server -cp " + OREGO_ROOT + "/bin orego.ui.Orego " + p + "'"
    outputParent = RESULT_DIR + '/' + host 
    if(not path.isdir(outputParent)):
       makedirs(outputParent)
    system("java -jar gogui-regress.jar -output '" + outputParent + "/params " + p + "'" + " " + orego + " " + " ".join(glob("cgtc-2.0.1/*.tst")))
    