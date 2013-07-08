package orego.mcts;

import static orego.core.Board.NINE_PATTERN;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import orego.patternanalyze.PatternInformation;

public class PatternPlayer extends MctsPlayer {
	
	HashMap<Character, PatternInformation> threePatterns;
	HashMap<Character, PatternInformation> fivePatterns;
	HashMap<Character, PatternInformation> sevenPatterns;
	HashMap<Character, PatternInformation> ninePatterns;
	@SuppressWarnings("unchecked")
	HashMap<Character, PatternInformation>[] patterns = new HashMap[4];
	
	@SuppressWarnings("unchecked")
	public PatternPlayer() {
		for (int i = 0; i < NINE_PATTERN + 1; i++) {
			//load from files
			try {
				ObjectInputStream ir = new ObjectInputStream(new FileInputStream(new File("./testFiles/patternPlayed"+(i*2+3)+".dat")));
				patterns[i] = (HashMap<Character, PatternInformation>)(ir.readObject());
				ir.close();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new PatternPlayer();
	}
	
	public PatternInformation getInformation(int patternType, char hash) {
		PatternInformation toReturn = patterns[patternType].get(hash);
		if (toReturn != null) {
			return toReturn;
		}
		else {
			return new PatternInformation();
		}
	}

}
