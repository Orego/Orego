package orego.cluster;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Scanner;

import orego.cluster.ClusterPlayer.RegistryFactory;
import orego.play.Player;
import orego.play.UnknownPropertyException;

/**
 * Simple implementation of {@link TreeSearch} that enables the {@link SearchController}
 * to remotely control a player on a different machine. This class is essentially an
 * object adapter for the players. 
 * @author samstewart
 *
 */
public class ClusterTreeSearcher implements TreeSearcher {

	/** our internal reference to our parent search controller. Serialized over RMI */
	private SearchController controller;
	
	/** a reference to the player we are controller */
	private Player player;
	
	/** the factory used for creating registries and for testing. We make it static for primitive dependency injection. */
	protected static RegistryFactory factory = new RegistryFactory();
	
	public static void main(String[] args) {
		// we boot ourselves up and connect to the friendly neighborhood server
		// Do we need the permissions?
		RMIStartup.configureRmi(ClusterTreeSearcher.class, ClusterPlayer.SECURITY_POLICY_MASTER);
		
		Registry reg;
		try {
			reg = factory.getRegistry();
			SearchController controller = (SearchController) reg.lookup(SearchController.SEARCH_CONTROLLER_NAME);
			
			ClusterTreeSearcher searcher = new ClusterTreeSearcher(controller);
			
			// now wait around until someone kills us
			Scanner scanner = new Scanner(System.in);
			
			// RMI calls will happen on the background thread
			while (!scanner.hasNextLine() || !scanner.nextLine().equals("die"));
			
			// clean ourselves up
			// TODO: notify the server 
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

	public ClusterTreeSearcher(SearchController controller) {
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
	protected Player getPlayer() {
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
		
		this.player.acceptMove(location);
	}

	@Override
	public void beginSearch() throws RemoteException {
		if (player == null || controller == null) return;
		
		/** 
		 * search for the best move to force the wins/runs tables
		 * to be filled.
		 */
		this.player.bestMove();
		
		
		// ping right back to the server
		controller.acceptResults(this, player.getPlayouts(), player.getWins());
	}

	@Override
	public boolean setPlayer(String player) {
		if (this.player == null) return false;
		
		// load player with java reflection
		try {
			Class<? extends Object> general_class = (Class<? extends Object>) Class.forName(player);
			
			if (Player.class.isAssignableFrom(general_class)) {
				Class<? extends Player> player_class = general_class.asSubclass(Player.class);
				
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
