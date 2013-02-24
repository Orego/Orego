package orego.experiment.parallel;


public class GameBatch extends orego.experiment.GameBatch {

	public GameBatch(int batchNumber, String machine) {
		super(batchNumber, machine);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		assert args.length == 1;
		
		launchGameBatches(args[0]);
	}
	
	@Override
	protected String getOregoCommand() {
		return super.getOregoCommand() + " -Djava.rmi.server.hostname=" + this.hostname + " player=ClusterPlayer";
	}

}
