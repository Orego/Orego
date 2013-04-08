# Orego

Orego is an ongoing, multi-year project to research and develop programs for playing the game of Go. It is supervised by Dr. Peter Drake of Lewis & Clark College in Portland, OR. For more information, go to the [Orego research page](https://sites.google.com/a/lclark.edu/drake/research/orego "Orego").

## Running from the Command Line

After compiling Orego, you can run it with the default settings with the command

      java -ea -server -Xmx2048M -cp /path/to/Orego/binaries/ orego.ui.Orego [options]

where [options] are optional arguments described below. Orego requires about 2 GB of available memory to run.

### Command Line Options

<strong>book</strong>=<em>class</em>  
Name of opening book to use. Books must be located in the orego.Book package.
	
<strong>debug</strong>  
Print debugging info to stderr.

<strong>debugfile</strong>=<em>filename</em>  
Print debugging info to the specified file.

<strong>player=</strong><em>class</em>  
Name of player class to use. May be a fully-qualified class name, e.g., orego.mcts.RavePlayer. The "Player" suffix may be dropped. The package may also be dropped if it is orego.play or orego.mcts. Defaults to Lgrf2.

<strong>heuristics</strong>=<em>heuristic@weight:heuristic@weight:...</em>  
List of policy classes, most important first, with their associated integer weights. Each class may be a fully-qualified class name, e.g., orego.heuristic.EscapeHeuristic. The "Heuristic" suffix may be dropped. If no package is specified, it defaults to orego.Heuristic. Defaults to Escape@20:Pattern@20:Capture@20. To use no heuristics (uniform random moves beyond the tree), use heuristics= with nothing on the right side.

### Player-Specific Options

#### ThreadedPlayer

<strong>msec</strong>=<em>integer</em>  
Milliseconds per move. 1000 by default. A GTP time_left command will automatically set this to an appropriate value.
	
<strong>ponder</strong>  
Think during the opponent's turn.
	
<strong>threads</strong>=<em>integer</em>  
Number of threads to run. 2 by default.

#### McPlayer

In addition to the above,

<strong>playouts</strong>=<em>integer</em>  
Number of playouts per thread. This is mutually exclusive with msec; the most recently set value will be used, the other ignored.

#### MctsPlayer

In addition to the above,

<strong>grace</strong>  
If true, Orego enters "coup de grace" mode when it is far ahead. This mode concentrates on killing dead enemy stones so that Orego may safely pass, rather than playing the game out to the bitter end. This is a courtesy to human opponents.

<strong>pool</strong>=<em>integer</em>    
Number of search nodes allocated at startup. Defaults to 1024 * 1024 * 20 / BOARD_AREA.

<strong>priors</strong>=<em>integer</em>    
Weight to give heuristics when initializing each node. Defaults to 20.

#### RavePlayer

<strong>bias</strong>=<em>integer</em>    
A parameter in the RAVE formula. Higher values pay less attention to RAVE. Defaults to 0.0009.

### Heuristic-Specific Options
	
To set properties for individual heuristics, use:

heuristic.HeuristicName.property=value

For example:

heuristic.Pattern.numberOfGoodPatterns=400

## Running Experiments

To run an experiment:

1. Edit (and recompile) ExperimentConfiguration.java. Here you can set the path to GNU Go
(or whatever standard opponent you're using), the command-line options to Orego for each
condition, and other options.

2. Make and clear the results directory specified in ExperimentConfiguration.java.

3. If you are running on a cluster, run Broadcast using the command

        nohup java -cp /path/to/Orego/binaries/ orego.experiment.Broadcast &

    If you are only running on a single machine, instead run GameBatch with the command
   
        nohup java -cp /path/to/Orego/binaries/ orego.experiment.GameBatch <HOST> &
   		
    where &lt;HOST> is one of the hosts listed in orego.experiment.ExperimentConfiguration (note that if you are running an experiment locally, the host will probably have the form <em>hostname</em>.localhost, with <em>hostname</em> being the name of your computer.

4. Periodically run Collate to display the results.

If you want to kill an ongoing experiment, run KillExperiment. Note that this is running
`kill -9 -1` on all of the machines listed in ExperimentConfiguration.java, so it
will kill all of your processes and log you out.

To check for statistical significance (whether one condition is better than another), run Significance.
