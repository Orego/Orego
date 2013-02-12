package orego.cluster;

import java.rmi.RemoteException;

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
	
	public static void main(String[] args) {
		// Do we need anything?

	}

	@Override
	public void setController(SearchController c) throws RemoteException {
		this.controller = c;
		
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
		if (player == null) return;
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
