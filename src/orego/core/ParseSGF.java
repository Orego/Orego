package orego.core;

import java.io.*;
import java.util.StringTokenizer;

public class ParseSGF {

	private static String rules;
	private static double komi;
	private static int size;
	private static int[] moves;
	private static int[][] addBlack; //moves added by black and white
	private static int[][] addWhite;
	
	public ParseSGF(BufferedReader SGFBuffer){
		try{
			// BufferedReader SGFBuffer = new BufferedReader(new FileReader(dirFile));
			String wholeFile = "";
			String nextLine;
			while ((nextLine = SGFBuffer.readLine()) != null) {
				wholeFile += nextLine;
			}
			moves = new int[361]; // 19*19
			addBlack = new int[25][2]; // this is just an estimate. It's possible that this should be much larger.
			addWhite = new int[25][2];
			StringTokenizer separateInfo = new StringTokenizer(wholeFile, ";");
			int i = 0;
			int j = 0;
			int k = 0;
			while(separateInfo.hasMoreTokens()) {
				String move = separateInfo.nextToken();
				if( move.contains("RU") ||move.contains("SZ") ||move.contains("KM")){ // this is at the beginning of the file
					StringTokenizer initSetup = new StringTokenizer(move, "[]()");
					while(initSetup.hasMoreTokens()){
						String sizeAndSoOn = initSetup.nextToken();
						if(sizeAndSoOn.equalsIgnoreCase("RU")) { //rules
							rules = initSetup.nextToken();
						} else if(sizeAndSoOn.equalsIgnoreCase("SZ")) { //board size
							size = Integer.parseInt(initSetup.nextToken());
						} else if(sizeAndSoOn.equalsIgnoreCase("KM")) { //komi
							komi = Double.parseDouble(initSetup.nextToken());
						}
					} // I believe this is everything we could use from the first part of the .sgf files up to the handicaps and moves
				} else if(move.contains("AB") ||move.contains("AW")){ // handicaps
					StringTokenizer handiSetup = new StringTokenizer(move, "[]()");
					int state = 0;
					while(handiSetup.hasMoreTokens()){
						String setup = handiSetup.nextToken();
						if(setup.equals("AB")){
							state = 1;
						}else if(setup.equals("AW")){
							state = 2;
						}else if(state == 1){
							int foot = setup.charAt(1) - 'a';
							addBlack[j][0] = foot;
							int bart = setup.charAt(0) - 'a';
							addBlack[j++][1] = bart;
						}else if(state == 2){
							int foot = setup.charAt(1) - 'a';
							addWhite[k][0] = foot;
							int bart = setup.charAt(0) - 'a';
							addWhite[k++][1] = bart;
						} 
					} 
				} else if(move.contains("B[") || move.contains("W[")) { //if it's a move
					String filling = move.substring(move.indexOf('[')+1,move.indexOf(']'));
					if(filling.isEmpty()){
						moves[i++] = 0; // for pass
					} else {
						int c = filling.charAt(0) - 'a';
						int r = filling.charAt(1) - 'a';
						int BWMove = (r + 1) * Coordinates.SOUTH + (c + 1) * Coordinates.EAST;
						moves[i++] = BWMove;//filling.charAt(1) - 'a';						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public ParseSGF(File dirFile) throws FileNotFoundException{
		new ParseSGF(new BufferedReader(new FileReader(dirFile)));
//		try{
//			BufferedReader SGFBuffer = new BufferedReader(new FileReader(dirFile));
//			String wholeFile = "";
//			String nextLine;
//			while ((nextLine = SGFBuffer.readLine()) != null) {
//				wholeFile += nextLine;
//			}
//			moves = new int[361]; // 19*19
//			addBlack = new int[25][2]; // this is just an estimate. It's possible that this should be much larger.
//			addWhite = new int[25][2];
//			StringTokenizer separateInfo = new StringTokenizer(wholeFile, ";");
//			int i = 0;
//			int j = 0;
//			int k = 0;
//			while(separateInfo.hasMoreTokens()) {
//				String move = separateInfo.nextToken();
//				if( move.contains("RU") ||move.contains("SZ") ||move.contains("KM")){ // this is at the beginning of the file
//					StringTokenizer initSetup = new StringTokenizer(move, "[]()");
//					while(initSetup.hasMoreTokens()){
//						String sizeAndSoOn = initSetup.nextToken();
//						if(sizeAndSoOn.equalsIgnoreCase("RU")) { //rules
//							rules = initSetup.nextToken();
//						} else if(sizeAndSoOn.equalsIgnoreCase("SZ")) { //board size
//							size = Integer.parseInt(initSetup.nextToken());
//						} else if(sizeAndSoOn.equalsIgnoreCase("KM")) { //komi
//							komi = Double.parseDouble(initSetup.nextToken());
//						}
//					} // I believe this is everything we could use from the first part of the .sgf files up to the handicaps and moves
//				} else if(move.contains("AB") ||move.contains("AW")){ // handicaps
//					StringTokenizer handiSetup = new StringTokenizer(move, "[]()");
//					int state = 0;
//					while(handiSetup.hasMoreTokens()){
//						String setup = handiSetup.nextToken();
//						if(setup.equals("AB")){
//							state = 1;
//						}else if(setup.equals("AW")){
//							state = 2;
//						}else if(state == 1){
//							int foot = setup.charAt(1) - 'a';
//							addBlack[j][0] = foot;
//							int bart = setup.charAt(0) - 'a';
//							addBlack[j++][1] = bart;
//						}else if(state == 2){
//							int foot = setup.charAt(1) - 'a';
//							addWhite[k][0] = foot;
//							int bart = setup.charAt(0) - 'a';
//							addWhite[k++][1] = bart;
//						} 
//					} 
//				} else if(move.contains("B[") || move.contains("W[")) { //if it's a move
//					String filling = move.substring(move.indexOf('[')+1,move.indexOf(']'));
//					if(filling.isEmpty()){
//						moves[i++] = 0; // for pass
//					} else {
//						int c = filling.charAt(0) - 'a';
//						int r = filling.charAt(1) - 'a';
//						int BWMove = (r + 1) * Coordinates.SOUTH + (c + 1) * Coordinates.EAST;
//						moves[i++] = BWMove;//filling.charAt(1) - 'a';						
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
	}
	
	public ParseSGF(String filepath) throws FileNotFoundException{
		new ParseSGF(new File(filepath));
//		try{
//			BufferedReader SGFBuffer = new BufferedReader(new FileReader(new File(filepath)));
//			String wholeFile = "";
//			String nextLine;
//			while ((nextLine = SGFBuffer.readLine()) != null) {
//				wholeFile += nextLine;
//			}
//			moves = new int[361]; // 19*19
//			addBlack = new int[25][2]; // this is just an estimate. It's possible that this should be much larger.
//			addWhite = new int[25][2];
//			StringTokenizer separateInfo = new StringTokenizer(wholeFile, ";");
//			int i = 0;
//			int j = 0;
//			int k = 0;
//			while(separateInfo.hasMoreTokens()) {
//				String move = separateInfo.nextToken();
//				if( move.contains("RU") ||move.contains("SZ") ||move.contains("KM")){ // this is at the beginning of the file
//					StringTokenizer initSetup = new StringTokenizer(move, "[]()");
//					while(initSetup.hasMoreTokens()){
//						String sizeAndSoOn = initSetup.nextToken();
//						if(sizeAndSoOn.equalsIgnoreCase("RU")) { //rules
//							rules = initSetup.nextToken();
//						} else if(sizeAndSoOn.equalsIgnoreCase("SZ")) { //board size
//							size = Integer.parseInt(initSetup.nextToken());
//						} else if(sizeAndSoOn.equalsIgnoreCase("KM")) { //komi
//							komi = Double.parseDouble(initSetup.nextToken());
//						}
//					} // I believe this is everything we could use from the first part of the .sgf files up to the handicaps and moves
//				} else if(move.contains("AB") ||move.contains("AW")){ // handicaps
//					StringTokenizer handiSetup = new StringTokenizer(move, "[]()");
//					int state = 0;
//					while(handiSetup.hasMoreTokens()){
//						String setup = handiSetup.nextToken();
//						if(setup.equals("AB")){
//							state = 1;
//						}else if(setup.equals("AW")){
//							state = 2;
//						}else if(state == 1){
//							int foot = setup.charAt(1) - 'a';
//							addBlack[j][0] = foot;
//							int bart = setup.charAt(0) - 'a';
//							addBlack[j++][1] = bart;
//						}else if(state == 2){
//							int foot = setup.charAt(1) - 'a';
//							addWhite[k][0] = foot;
//							int bart = setup.charAt(0) - 'a';
//							addWhite[k++][1] = bart;
//						} 
//					} 
//				} else if(move.contains("B[") || move.contains("W[")) { //if it's a move
//					String filling = move.substring(move.indexOf('[')+1,move.indexOf(']'));
//					if(filling.isEmpty()){
//						moves[i++] = 0; // for pass
//					} else {
//						int c = filling.charAt(0) - 'a';
//						int r = filling.charAt(1) - 'a';
//						int BWMove = (r + 1) * Coordinates.SOUTH + (c + 1) * Coordinates.EAST;
//						moves[i++] = BWMove;//filling.charAt(1) - 'a';						
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
	}
	
	/*
	 * returns coordinate of nth move
	 */
	public int getMove(int n){
		return moves[n]; // right now this is set up so n = 0 would be the first move
	}

	public int[] getMoves(){
		return moves;
	}
	
	public String getRules(){
		return rules;
	}
	
	public double getKomi(){
		return komi;
	}
	
	public int getSize(){
		return size;
	}
	
	public int[][] getAddBlack(){
		return addBlack;
	}
	
	public int[][] getAddWhite(){
		return addWhite;
	}
	
}
