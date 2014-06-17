package edu.lclark.orego.mcts;

import java.util.LinkedList;

public class TreeNode {

	private float winRate;

	private int runs;
	
	private String move;

	private LinkedList<TreeNode> children;
	
	private TreeNode parent;
	
	boolean isSelected;
	
	public TreeNode(float winRate, int runs, TreeNode parent, String move){
		this.winRate = winRate;
		this.runs = runs;
		children = new LinkedList<>();
		this.parent = parent;
		this.move = move;
		isSelected = false;
	}

	public LinkedList<TreeNode> getChildren() {
		return children;
	}

	public float getWinRate() {
		return winRate;
	}

	public int getRuns() {
		return runs;
	}
	
	public TreeNode getParent(){
		return parent;
	}

	public void addChild(TreeNode child) {
		children.add(child);
	}

}
