Orego
=====

Source code for Dr. Peter Drake's computer Go engine.

## Running with Go GUI

`java -ea -server -Xmx8192M -cp [your local orego repo]/bin orego.ui.Orego threads=1 player=Mcts policy=Random playouts=1000`

## Command Line Options

All arguments take either the form

property=value

or (for boolean properties that default to false):

property

The following options are always available:

book=Class

	Name of opening book class to use, if any. It is assumed to be in the orego.book package.
	Example: FusekiBook.
debug
	Print debugging info to stderr.
debugfile = filename
	Print debugging info to the specified file.

player = Class
	Name of player class to use. May be a fully-qualified class name, e.g.,
	orego.mcts.RavePlayer. The "Player" suffix may be dropped. The package may also be
	dropped if it is orego.play or orego.mcts. Defaults to Lgrf2.

heuristics = Class@weight:Class@weight:...:Class@weight
	Name(s) of policy classes, most important first, with their associated integer weights.
	Each class may be a fully-qualified class name, e.g., orego.heuristic.EscapeHeuristic.
	The "Heuristic" suffix may be dropped. The package may also be dropped if it is orego.heuristic.
	Defaults to Escape@20:Pattern@20:Capture@20.

Additional options are available depending on players, policies, and other classes invoked.

ThreadedPlayer adds the following properties:

msec = #
	Milliseconds per move. 1000 by default. A GTP time_left command will automatically set
	this to an appropriate value.
ponder
	Think during the opponent's turn.
threads = #
	Number of threads to run. 2 by default.

McPlayer adds, in addition to the properties from ThreadedPlayer:

playouts=#
	Number of playouts per thread. This is mutually exclusive with msec; the most recently set
	value will be used, the other ignored.

MctsPlayer adds, in addition to the properties from McPlayer:

grace
	If true, Orego enters "coup de grace" mode when it is far ahead. This mode concentrates
	on killing dead enemy stones so that Orego may safely pass, rather than playing the game
	out to the bitter end. This is a courtesy to human opponents.
pool = #
	Number of search nodes allocated at startup. Defaults to 1024 * 1024 * 20 / BOARD_AREA.
priors = #
	Weight to give heuristics when initializing each node. Defaults to 20.

RavePlayer adds, in addition to the properties from MctsPlayer:

bias = #
	A parameter in the RAVE formula. Higher values pay less attention to RAVE. Defaults to 0.0009.

To set properties for individual heuristics, use:

heuristic.HeuristicName.property=#

For example:

heuristic.Pattern.numberOfGoodPatterns=400
