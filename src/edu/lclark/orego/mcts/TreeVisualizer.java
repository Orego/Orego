package edu.lclark.orego.mcts;

import java.awt.*;
import java.util.LinkedList;

import javax.swing.JFrame;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ListNode;

@SuppressWarnings("serial")
public class TreeVisualizer extends JFrame {

	final Player player;

	final TranspositionTable table;

	final SimpleTreeUpdater updater;

	final Board board;

	private TreeNode root;

	public static void main(String[] args) {
		new TreeVisualizer().run();
	}

	public TreeVisualizer() {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		board = player.getBoard();
		table = new TranspositionTable(new SimpleSearchNodeBuilder(board.getCoordinateSystem()),
				board.getCoordinateSystem());
		updater = new SimpleTreeUpdater(board, table);
		player.setTreeUpdater(updater);
		player.setTreeDescender(new UctDescender(board, table));
	}

	private void run() {
		for (int i = 0; i < 100; i++) {
			player.getMcRunnable(0).performMcRun();
		}
		buildTree();
		Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (dimension.getWidth() * .85), (int) (dimension.getHeight() * .85));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private void buildTree() {
		SearchNode node = updater.getRoot();
		root = buildNode(node, null, null, (short) 0);
	}

	private TreeNode buildNode(SearchNode source, SearchNode parentSearchNode, TreeNode parent, short p) {
		int runs = parentSearchNode == null ? source.getTotalRuns() / 2 : parentSearchNode.getRuns(p);
		TreeNode nodeToAdd = new TreeNode(source.getWinRate((short) 0), runs,
				parent, board.getCoordinateSystem().toString(p));
		ListNode<SearchNode> children = source.getChildren();
		if (children != null) {
			ListNode<SearchNode> loopChild = children;
			for (short point : board.getCoordinateSystem().getAllPointsOnBoard()) {
				if (source.hasChild(point)) {
					nodeToAdd.addChild(buildNode(loopChild.getKey(), source, nodeToAdd, point));
					loopChild = loopChild.getNext();
					if(loopChild == null){
						break;
					}
				}

			}
		}
		return nodeToAdd;
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(new Color(225, 225, 225));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int x = this.getWidth() / 2;
		int y = 20;
		drawNode(g, x, y, root);
		drawLevel(g, root, x, y, this.getWidth(), 0, 5);
	}

	public void drawLevel(Graphics g, TreeNode parent, int x, int y, int width, int depth,
			int maxDepth) {
		if (maxDepth == depth) {
			return;
		}
		LinkedList<TreeNode> children = parent.getChildren();
		if (children.size() == 0) {
			return;
		}
		x = x - (width / 2);
		int newWidth = width / children.size();
		int i = 0;
		for (TreeNode child : children) {
			drawNode(g, x + (newWidth / 2) + (i * newWidth), y + 100, child);
			drawLevel(g, child, x + (newWidth / 2) + (i * newWidth), y + 100, newWidth,
					depth + 1, maxDepth);
			i++;
		}
	}

	private void drawNode(Graphics g, int x, int y, TreeNode node) {
		int diameter = node.getRuns();
		System.out.println(node.getRuns());
		g.setColor(new Color(node.getWinRate(), node.getWinRate(), node.getWinRate()));
		g.fillOval(x, y, diameter, diameter);
		if (node.isSelected) {
			g.setColor(Color.RED);
		} else {
			g.setColor(Color.BLACK);
		}
		g.drawOval(x, y, diameter, diameter);
	}

}
