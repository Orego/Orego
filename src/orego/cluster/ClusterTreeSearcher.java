package orego.cluster;

import static orego.core.Coordinates.pointToString;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.mcts.MctsPlayer;
import orego.mcts.StatisticalPlayer;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

/**
 * Simple implementation of {@link TreeSearch} that enables the {@link SearchController}
 * to remotely control a player on a different machine. This class is essentially an
 * object adapter for the players. 
 * @author samstewart
 *
 */
public class ClusterTreeSearcher extends UnicastRemoteObject implements TreeSearcher {

	/** As a remote object, we must be serializable */
	private static final long serialVersionUID = 5944276914907386971L;

	/** our internal reference to our parent search controller. Serialized over RMI */
	protected SearchController controller;
	
	/** a reference to the player we are controller */
	private StatisticalPlayer player;
	
	/** the index of the specific controller we are connected to */
	protected int controllerIndex;
	
	/** the hostname of the main controller RMI server*/
	protected String controllerHost;
	
	/** maximum amount of time we are willing to wait for a connection*/
	protected static int MAX_WAIT = 1000 * 60 * 5; // 5 minutes

	/** an id set by the controller for internal use */
	private int searcherId = -1;
	
	/** the points that will be considered by this searcher */
	private IntSet consideredPoints;
	
	/** a flag that lets the application exit after an unsuccessful connection attempt */
	private boolean shouldExit = false;
	
	/** The command line command to die*/
	public static final String KILL_COMMAND = "quit";
	
	/** a counter that is used to ignore results that come in late */
	private AtomicInteger searchCount = new AtomicInteger(0);
	
	private Thread searchThread = null;
	
	/** the factory used for creating registries and for testing. We make it static for primitive dependency injection. */
	protected static RegistryFactory factory = new RegistryFactory();
	
	public static void main(String[] args) throws Exception {
		// make sure that we were given a host to connect to
		String controllerHost;
		int controllerIndex = -1;
		if(args.length < 2) {
			System.out.println("Usage: java orego.cluster.ClusterTreeSearcher remote_controller_index host");
			return;
		}
		else {
			controllerIndex = Integer.parseInt(args[0]);
			controllerHost = args[1];
			System.out.println("Trying to connect to host: " + controllerHost + " with machine index: " + controllerIndex);
		}
		
		// try to connect to RMI and get a reference to a controller
		ClusterTreeSearcher searcher = null;
		try {
			searcher = new ClusterTreeSearcher(controllerHost, controllerIndex);
		} catch (RemoteException e) {
			System.exit(1);
		}
		
		// now wait around until someone kills us (or the searcher says we can exit)
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		// RMI calls will happen on the background thread so we just sit here waiting.
		while ( ! searcher.shouldExit) {
			if (reader.ready() && reader.readLine().equals(KILL_COMMAND)) {
				break;
			}
			// we don't need to be checking all the time so let's wait awhile
			Thread.sleep(500);
		}
		
		// forcibly unregister ourselves so RMI will let us exit.
		// if we have been terminated by the main controller itself, this method
		// will do nothing.
		searcher.removeFromController();
	
	}

	/**
	 * Attempts to connect to RMI
	 * @param hostname the name of the remote RMI registry
	 * @throws RemoteException 
	 */
	protected void connectToRMI() throws RemoteException {
		
		// Clear out the player so RMI doesn't try to send it to the controller
		this.player = null;
		
		// we boot ourselves up and connect to the friendly neighborhood server
		// Do we need the permissions?
		// we do not need to publish any classes, so pass null for the first argument to configureRmi
		RMIStartup.configureRmi(null, RMIStartup.SECURITY_POLICY_FILE);
		
		// this call does not actually try to connect; RMI only connects
		// when we call .lookup()
		Registry reg = factory.getRegistry(controllerHost);
		
		if (reg == null)
			throw new RemoteException();
		
		
		SearchController controller = tryToConnectToController(reg, controllerIndex, MAX_WAIT);
				
		this.controller = controller;
		
		this.reset();
				
		if (controller == null) throw new RemoteException();
		
		controller.addSearcher(this);
	}
	/** 
	 * simple utility method to continually try to connect to the registry until
	 * the timeout limit is reached. Because RMI makes the actual connection only
	 * when you request an object, you must pass in the name you wish to request.
	 * @param registry the registry we wish to connect to.
	 * @param timeout the maximum wait time, in milliseconds
	 * @parma host the host we wish to connect to.
	 */
	protected SearchController tryToConnectToController(Registry registry, int playerIndex, int timeout) {
		if (playerIndex < 0) throw new UnsupportedOperationException("Tried to connect to search controller with invalid index " + playerIndex);
		
		int totalTime = 0;
		
		while (totalTime < timeout) {
			try {
				String name = SearchController.SEARCH_CONTROLLER_NAME + playerIndex;
				
				SearchController controller = (SearchController) registry.lookup(name);
				
				return controller;
				
			} catch (Exception e) { // we have to swallow any exception because java doesn't allow multiple
									// exception handling (we need Java 7)
				System.out.println("Failed to connect to controller " + playerIndex + ", waiting 1s then trying again...");
				try {
					Thread.sleep(1000); // wait a second; literally
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					return null;
				} 
				totalTime += 1000;
			}
		}
		System.out.println("Error: connection attempts to " + playerIndex + " timed out");
		return null;
	}
	public ClusterTreeSearcher(String hostname, int controllerIndex) throws RemoteException {
		this.consideredPoints = null;
		this.controllerIndex = controllerIndex;
		this.controllerHost = hostname;
	
		connectToRMI();
	}

	
	/** Removes ourselves from our controller and un-publishes ourselves from the RMI registry*/
	public void removeFromController() {
		// if we've already unregistered ourselves, skip it (happens when we call should terminate)
		if (this.controller == null) return;
		
		try {
			this.controller.removeSearcher(this);
			
			UnicastRemoteObject.unexportObject(this, true);
			
		} catch (RemoteException e) {
			System.err.println("Could not remove ClusterTreeSearcher from the controller.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void reset() throws RemoteException {
		if (player == null) return;
		
		this.player.reset();
		
	}

	@Override
	public void shouldTerminate() throws RemoteException {
		// clear out our controller
		this.controller = null;
		
		// clear settings specific to the controller
		this.consideredPoints = null;
		
		try {
			UnicastRemoteObject.unexportObject(ClusterTreeSearcher.this, true);
		} catch (NoSuchObjectException e) {
			System.err.println("Error cleaning searcher up");
			e.printStackTrace();
		}
		
		this.shouldExit = true; // tell ourselves we should terminate gracefully and the main loop will stop
	}
	
	@Override
	public void setKomi(double komi) throws RemoteException {
		if (player == null) return;
		
		this.player.setKomi(komi);
		
	}

	@Override
	public void setProperty(String key, String value) throws RemoteException {
		if (player == null) return;
		
		System.out.println("Set property: " + key + " = " + value);
		
		try {
			
			this.player.setProperty(key, value);
			
		} catch (UnknownPropertyException e) {
			// toss this thing upwards
			throw new RemoteException(e.getMessage());
		}
		
	}
	
	@Override
	public void setPointsToConsider(IntSet pts) throws RemoteException {
		System.out.println("Going to consider: " + pts.size() + " points.");
		
		consideredPoints = pts;
		excludeIgnoredPoints();
	}

	@Override
	public void acceptMove(int player, int location) throws RemoteException {
		if (this.player == null) return;
		
		System.out.println("Accepting move: " + pointToString(location));
		
		this.player.acceptMove(location);
		
		excludeIgnoredPoints();
	}
	
	private void excludeIgnoredPoints() {
		if(consideredPoints == null) return;
		
		for(int idx = 0; idx < FIRST_POINT_BEYOND_BOARD; idx++) {
			if(!consideredPoints.contains(idx)) {
				// TODO: This is a bit of a hack
				((MctsPlayer)this.player).getRoot().exclude(idx);
			}
		}
	}

	@Override
	public boolean undo() {
		if (this.player == null) return false;
		
		System.out.println("Undoing move.");
		
		return this.player.undo();
	}
	
	@Override
	public void beginSearch() throws RemoteException {
		if (player == null || controller == null) {
			throw new RemoteException("No player or controller set. Cannot return results.");
		}
		
		// increment the search count
		final int startingSearchCount = searchCount.incrementAndGet();

		// run the search on a separate thread so this doesn't block the server
		searchThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println(String.format("[%d: %d] Beginning to search.", 
						(System.currentTimeMillis() / 1000L), 
						startingSearchCount));
				
				// search for the best move to force the wins/runs tables to be filled
				ClusterTreeSearcher.this.player.runSearch();
				
				System.out.println(String.format("[%d: %d] Playouts completed: %d",
						(System.currentTimeMillis() / 1000L),
						startingSearchCount, 
						player.getTotalPlayoutCount()));

				System.out.println(String.format("[%d: %d] Done searching.", 
						(System.currentTimeMillis() / 1000L),
						startingSearchCount));
				
				// startingSearchCount is captured when the thread is created
				if(searchCount.get() != startingSearchCount) {
					System.out.println(String.format("[%d: %d] Finished late - not reporting results.", 
							(System.currentTimeMillis() / 1000L),
							startingSearchCount));
					return;
				}
				
				// ping right back to the server
				try {
					controller.acceptResults(ClusterTreeSearcher.this, player.getBoardPlayouts(), player.getBoardWins());
				} catch (RemoteException e) {
					System.err.println("Failed to report search results to controller.");
					e.printStackTrace();
				}
				
				searchThread = null;
			}
		});
		searchThread.start();
		
	}
	
	@Override
	public void terminateSearch() {
		int currentCount = searchCount.get();
		System.out.println(String.format("[%d: %d] Received terminateSearch.", (System.currentTimeMillis() / 1000L), currentCount));
		if(searchThread != null) {
			searchCount.incrementAndGet();
			searchThread.interrupt();
			player.terminateSearch();
		}
		System.out.println(String.format("[%d: %d] terminateSearch finished.", (System.currentTimeMillis() / 1000L),currentCount));
	}
	
	@Override
	public long getTotalPlayoutCount() {
		return player.getTotalPlayoutCount();
	}
	
	@Override
	public boolean shouldPassToWin() {
		return player.secondPassWouldWinGame();
	}
	
	@Override
	public String finalStatusList(String status) {
		System.out.println("Received finalStatusList.");
		return player.finalStatusList(status);
	}

	/** Mostly used for unit testing dependency injection*/
	protected void setPlayer(StatisticalPlayer player) {
		this.player = player;
	}
	
	protected StatisticalPlayer getPlayer() {
		return this.player;
	}
	
	@Override
	public boolean setPlayer(String player) {
		if (player == null) return false;
		
		System.out.println("Set player: " + player);
		
		// load player with java reflection
		try {
			Class<? extends Object> general_class = (Class<? extends Object>) Class.forName(player);
			
			if (StatisticalPlayer.class.isAssignableFrom(general_class)) {
				Class<? extends StatisticalPlayer> player_class = general_class.asSubclass(StatisticalPlayer.class);
				
				// Note: we assume the player constructors take no arguments
				this.player = player_class.newInstance();
				
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			
			e.printStackTrace();
			
			return false;
		} catch (IllegalAccessException e) {
			
			e.printStackTrace();
			
			return false;
		}
		
		return true;
		
	}

	@Override
	public void setSearcherId(int id) throws RemoteException {
		searcherId = id;
	}

	@Override
	public int getSearcherId() throws RemoteException {
		return searcherId;
	}

}
