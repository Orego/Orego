package edu.lclark.orego.visuals;

import java.util.LinkedList;

import edu.lclark.orego.core.Board;

public class TreeNode {

	private LinkedList<TreeNode> children;

	boolean isSelected;

	private String move;

	private TreeNode next;

	private TreeNode parent;

	private TreeNode previous;

	private int runs;

	private float winRate;

	public TreeNode(float winRate, int runs, TreeNode parent, String move) {
		this.winRate = winRate;
		this.runs = runs;
		children = new LinkedList<>();
		this.parent = parent;
		this.move = move;
		isSelected = false;
	}

	public void addChild(TreeNode child) {
		children.add(child);
	}

	public LinkedList<TreeNode> getChildren() {
		return children;
	}

	public String getMove() {
		return move;
	}

	public TreeNode getNext() {
		return next;
	}

	public TreeNode getParent() {
		return parent;
	}

	public TreeNode getPrevious() {
		return previous;
	}

	public int getRuns() {
		return runs;
	}

	public float getWinRate() {
		return winRate;
	}

	public void setNext(TreeNode next) {
		this.next = next;
	}

	public void setPrevious(TreeNode previous) {
		this.previous = previous;
	}

}
