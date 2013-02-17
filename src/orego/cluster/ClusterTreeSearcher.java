package orego.cluster;

import static orego.core.Coordinates.pointToString;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.mcts.StatisticalPlayer;
import orego.play.Player;
import orego.play.UnknownPropertyException;

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
	private SearchController controller;
	
	/** a reference to the player we are controller */
	private StatisticalPlayer player;
	
	/** the factory used for creating registries and for testing. We make it static for primitive dependency injection. */
	protected static RegistryFactory factory = new RegistryFactory();
	
	public static void main(String[] args) {
		// make sure that we were given a host to connect to
		String controllerHost;
		if(args.length == 0) {
			System.out.println("Usage: java orego.cluster.ClusterTreeSearcher host");
			return;
		}
		else {
			controllerHost = args[0];
			System.out.println("Trying to connect to host: " + controllerHost);
		}
		
		// we boot ourselves up and connect to the friendly neighborhood server
		// Do we need the permissions?
		// we do not need to publish any classes, so pass null for the first argument to configureRmi
		RMIStartup.configureRmi(null, RMIStartup.SECURITY_POLICY_FILE);
		
		try {
			Registry reg = factory.getRegistry(controllerHost);
			SearchController controller = (SearchController) reg.lookup(SearchController.SEARCH_CONTROLLER_NAME);
			
			ClusterTreeSearcher searcher = new ClusterTreeSearcher(controller);
			
			// now wait around until someone kills us
			Scanner scanner = new Scanner(System.in);
			
			// RMI calls will happen on the background thread
			while (!scanner.hasNextLine() || !scanner.nextLine().equals("die"));
			
			controller.removeSearcher(searcher);
			
			// forcibly unregister our callback so RMI will let us exit
			UnicastRemoteObject.unexportObject(searcher, true);
			
		} catch (RemoteException e) {
			System.err.println("Fatal error. Could not connect to ClusterPlayer.");
			e.printStackTrace();
			System.exit(1);
		} catch (NotBoundException e) {
			System.err.println("Fatal error. Could not find ClusterPlayer.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public ClusterTreeSearcher(SearchController controller) throws RemoteException {
		this.controller = controller;
		
		
		try {
			this.reset();
			controller.addSearcher(this);
			
		} catch (RemoteException e) {
			System.err.println("Fatal error. Could not add ourselves to ClusterPlayer.");
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	/** mostly used for unit testing */
	protected StatisticalPlayer getPlayer() {
		return this.player;
	}
	
	@Override
	public void reset() throws RemoteException {
		if (player == null) return;
		
		this.player.reset();
		
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
	public void acceptMove(int player, int location) throws RemoteException {
		if (this.player == null) return;
		
		System.out.println("Accepting move: " + pointToString(location));
		
		this.player.acceptMove(location);
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

}
