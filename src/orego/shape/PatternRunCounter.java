package orego.shape;

import static orego.core.Board.NINE_PATTERN;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import orego.core.Colors;

public class PatternRunCounter {
	
	@SuppressWarnings("unchecked")
	private HashMap<Character, PatternInformation>[][] patterns = new HashMap[4][2];


	@SuppressWarnings("unchecked")
	private void loadPatternHashMaps() {
		for (int c = 0; c < 2; c++) {
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// load from files
				try {
					ObjectInputStream ir = new ObjectInputStream(
							new FileInputStream(new File(
									"./testFiles/patternPlayed" + (i * 2 + 3)
											+ Colors.colorToString(c) + ".dat")));
					patterns[i][c] = (HashMap<Character, PatternInformation>) (ir
							.readObject());
					ir.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public PatternRunCounter(){
		loadPatternHashMaps();
		
		for (int c = 0; c < 2; c++) {
			
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// load from files
				try {
					DataOutputStream out = 
							new DataOutputStream(new FileOutputStream(new File(
									"./testFiles/patternPlayed" + (i * 2 + 3)
											+ Colors.colorToString(c) + ".txt")));
					for (char key:patterns[i][c].keySet()){
						out.writeChars(patterns[i][c].get(key).getRuns()+"\n");
					}
					out.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new PatternRunCounter();
	}
}
