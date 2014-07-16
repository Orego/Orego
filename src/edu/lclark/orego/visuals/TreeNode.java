package edu.lclark.orego.visuals;

import java.util.LinkedList;

/** Easier to work with than the nodes in the TranspositionTable. */
final class TreeNode {

	private final LinkedList<TreeNode> children;

	private boolean isSelected;

	private final String move;

	private TreeNode next;

	private final TreeNode parent;

	private TreeNode previous;

	private final int runs;

	private final float winRate;

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

	public boolean isSelected() {
		return isSelected;
	}

	public void setNext(TreeNode next) {
		this.next = next;
	}

	public void setPrevious(TreeNode previous) {
		this.previous = previous;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

}
