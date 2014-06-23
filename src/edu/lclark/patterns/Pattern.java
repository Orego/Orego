package edu.lclark.patterns;

public class Pattern implements Comparable<Pattern>{
	
	private final int hash;
	
	private final float winRate;
	
	Pattern(int hash, float winRate){
		this.hash = hash;
		this.winRate = winRate;
	}
	
	public int getHash(){
		return hash;
	}
	
	public float getWinRate(){
		return winRate;
	}
	

	@Override
	public int compareTo(Pattern pattern){
		if(winRate > pattern.winRate){
			return 1;
		}else if(winRate < pattern.winRate){
			return -1;
		}
		else if(winRate == pattern.winRate){
			if(hash > pattern.hash){
				return 1;
			} else if(hash < pattern.hash){
				return -1;
			}
		}
		return 0;
	}
	
	public boolean equals(Pattern pattern){
		return false;
	}
	
	public String toString(){
		
		String result = "Win Rate: " + winRate + " Hash = " + hash + "\n";
		result += toChar(((hash >>> 8) & 3));
		result += toChar((hash & 3));
		result += toChar(((hash >>> 10) & 3));
		result += "\n";
		result += toChar(((hash >>> 2) & 3));
		result += ".";
		result += toChar(((hash >>> 4) & 3));
		result += "\n";
		result += toChar(((hash >>> 12) & 3));
		result += toChar(((hash >>> 6) & 3));
		result += toChar(((hash >>> 14) & 3));
		result += "\n";
		return result;
	}
	
	public char toChar(int i){
		switch(i){
		case 0: return '#';
		case 1: return 'O';
		case 2: return '.';
		case 3: return '?';
		default : return ' ';
		}
		
	}

}
