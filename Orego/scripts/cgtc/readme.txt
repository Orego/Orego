There are a number of python scripts in this directory for running experiments against GNU Go.

The files are as follows:

systemconfig.py		Defines some variables indicating the locations of files and machines on your system
experimentconfig.py	Defines parameters for the current experiment
tournament.py		Pits different Orego parameter sets against GNU Go
broadcast.py		Runs tournament on each of a number of machines
collate.py			Collates results of experiments
twogtp.py			(Modified version of the script supplied with GNU Go; see that file for license)

IMPORTANT NOTE: Running broadcast.py has two deliberate side effects which are intended to clean up past experiments:
it kills all of USER's processes on any of MACHINEs and it deletes all *.dump files in RESULT_DIR.

Because it kills all processes, the machine on which broadcast.py is run cannot be one of the machines running experiments.
If you are only using one machine, just run tournament.py on that machine. It is okay to run collate.py even if only one
machine is being used.

The general procedure for using these files is as follows:

1) Edit systemconfig.py for your machines
2) Edit experimentconfigy.py for the experiment at hand
3) Run broadcast.py (or tournament.py if only using one machine)
4) Run collate.py every once in a while; it will indicate which machines have completed their runs and report
   partial results. If one or more machines have unusually low win rates or have not finished when the others have,
   these machines may have some other load slowing them down. You can exclude them from the collation by listing
   the machines on the command line for collate.py. Note that results are only reported at the end of each condition,
   not the end of each game.
