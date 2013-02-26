package orego.cluster;

import static orego.core.Coordinates.pointToString;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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
	
	/** the factory used for creating registries and for testing. We make it static for primitive dependency injection. */
	protected static RegistryFactory factory = new RegistryFactory();
	
	public static void main(String[] args) throws Exception {
		// make sure that we were given a host to connect to
		String controllerHost;
		int controllerIndex = -1;
		if(args.length < 2) {
			System.out.println("Usage: java orego.cluster.ClusterTreeSearcher host machine_index");
			return;
		}
		else {
			controllerHost = args[0];
			controllerIndex = Integer.parseInt(args[1]);
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
		
		// RMI calls will happen on the background thread
		while(!searcher.shouldExit) {
			if(reader.ready() && reader.readLine().equals("die")) {
				break;
			}
			Thread.sleep(100);
		}
		
		// forcibly unregister ourselves so RMI will let us exit
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
		int totalTime = 0;
		
		while (totalTime < timeout) {
			try {
				// if player index < 0, go with default
				String name = (playerIndex >= 0 ? SearchController.SEARCH_CONTROLLER_NAME + playerIndex : SearchController.SEARCH_CONTROLLER_NAME);
				
				SearchController controller = (SearchController) registry.lookup(name);
				
				return controller;
			} catch (Exception e) { // we have to swallow any exception because java doesn't allow multiple
									// exception handling (we need Java 7)
				System.out.println("Failed to connect, waiting 1s then trying again...");
				try {
					Thread.sleep(1000); // wait a second; literally
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					return null;
				} 
				totalTime += 1000;
			}
		}
		System.out.println("Connection attempts timed out");
		return null;
	}
	public ClusterTreeSearcher(String hostname, int controllerIndex) throws RemoteException {
		this.consideredPoints = null;
		this.controllerIndex = controllerIndex;
	
		connectToRMI();
	}

	
	/** Removes ourselves from our controller and un-publishes ourselves from the RMI registry*/
	public void removeFromController() {
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
	public void kill() throws RemoteException {
		// clear out our controller
		this.controller = null;
		
		// we need to do this asynchronously to avoid blocking the server
		new Thread() {
			@Override
			public void run() {
				// try to reconnect, this will wait until a new server starts
				try {
					ClusterTreeSearcher.this.connectToRMI();
				} catch (Exception e) {
					try {
						UnicastRemoteObject.unexportObject(ClusterTreeSearcher.this, true);
						ClusterTreeSearcher.this.shouldExit = true;
					} catch (NoSuchObjectException ex) {
						ex.printStackTrace();
					}
				}
			}
		}.start();
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
		
		
		// run the search on a separate thread so this doesn't block the server
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("Beginning to search.");
				
				// search for the best move to force the wins/runs tables to be filled
				ClusterTreeSearcher.this.player.bestMove();
				
				System.out.println("Done searching.");
				// ping right back to the server
				try {
					controller.acceptResults(ClusterTreeSearcher.this, player.getBoardPlayouts(), player.getBoardWins());
				} catch (RemoteException e) {
					System.err.println("Failed to report search results to controller.");
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	@Override
	public long getTotalPlayoutCount() {
		return player.getTotalPlayoutCount();
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
