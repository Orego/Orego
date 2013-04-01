package orego.mcts;

import orego.play.UnknownPropertyException;
import ec.util.MersenneTwisterFast;

public class SoftmaxPlayer extends Lgrf2Player {
	
	private SoftmaxPolicy defaultPolicy;
	
	public static void main(String[] args) {
		try {
			SoftmaxPlayer p = new SoftmaxPlayer();
			p.setProperty("heuristics", "Escape@20:Pattern@20:Capture@20");
			p.setProperty("heuristic.Pattern.numberOfGoodPatterns", "400");
			p.setProperty("threads", "1");
			double[] benchMarkInfo = p.benchmark();
			System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
					+ benchMarkInfo[1]);
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public SoftmaxPlayer() {
		super();
		double weights[] = new double[Character.MAX_VALUE];
		MersenneTwisterFast random = new MersenneTwisterFast();
		for(int idx = 0; idx < weights.length; idx++) {
			weights[idx] = random.nextDouble();
		}
		defaultPolicy = new SoftmaxPolicy(weights);
	}
	
	public void reset() {
		super.reset();
		// Replace McRunnables with SoftmaxMcRunnables
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new SoftmaxMcRunnable(this, getHeuristics().clone(), replies1, replies2, defaultPolicy));
		}
	}
}
