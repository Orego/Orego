package edu.lclark.orego.visuals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.mcts.CopiableStructureFactory;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.mcts.SimpleSearchNode;
import edu.lclark.orego.mcts.SimpleSearchNodeBuilder;
import edu.lclark.orego.mcts.SimpleTreeUpdater;
import edu.lclark.orego.mcts.TranspositionTable;
import edu.lclark.orego.mcts.UctDescender;
import edu.lclark.orego.util.ListNode;

/** Interactive graphic representation of Monte Carlo tree search. */
@SuppressWarnings("serial")
public final class TreeVisualizer extends JFrame {

	/** Panel for drawing the nodes based on the navegable tree structure. */
	class DrawPanel extends JPanel {

		/** Recursively draws all the children of a node up to a max depth. */
		public void drawLevel(Graphics g, TreeNode parent, int x, int y,
				int width, int depth, int maxDepth) {
			if (maxDepth == depth) {
				return;
			}
			final LinkedList<TreeNode> children = parent.getChildren();
			if (children.size() == 0) {
				return;
			}
			x = x - width / 2;
			final int newWidth = width / children.size();
			int i = 0;
			for (final TreeNode child : children) {
				drawNode(g, x + newWidth / 2 + i * newWidth, y + 100, child);
				drawLevel(g, child, x + newWidth / 2 + i * newWidth,
						y + 100, newWidth, depth + 1, maxDepth);
				i++;
			}
		}

		/**
		 * Draws the circle for a given node in the proper position, highlighted
		 * if the node is currently selected.
		 */
		private void drawNode(Graphics g, int x, int y, TreeNode node) {
			final int diameter = 3 * (int) (Math.log(node.getRuns()) / Math.log(2));
			x = x - diameter / 2;
			y = y - diameter / 2;
			g.setColor(new Color(node.getWinRate(), node.getWinRate(), node
					.getWinRate()));
			g.fillOval(x, y, diameter, diameter);
			if (node.isSelected()) {
				g.setColor(Color.RED);
			} else {
				g.setColor(Color.BLACK);
			}
			g.drawOval(x, y, diameter, diameter);
		}

		@SuppressWarnings("synthetic-access")
		private void drawWithScaling(Graphics g, int x, int y) {
			int i = 1;
			TreeNode nodeToDraw = selectedNode.getNext();
			while (nodeToDraw != null) {
				drawNode(g, x + 30 * i, y, nodeToDraw);
				nodeToDraw = nodeToDraw.getNext();
				i++;
			}
			i = 1;
			nodeToDraw = selectedNode.getPrevious();
			while (nodeToDraw != null) {
				drawNode(g, x - 30 * i, y, nodeToDraw);
				nodeToDraw = nodeToDraw.getPrevious();
				i++;
			}
			draw.drawNode(g, x, y, selectedNode);
			draw.drawLevel(g, selectedNode, x, y, this.getWidth(), 0, 8);
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void paintComponent(Graphics g) {
			// Make sure the tree has been built
			if (root == null) {
				return;
			}
			super.paintComponent(g);
			final int x = this.getWidth() / 2;
			final int y = 20;
			if (scaling.isSelected()) {
				drawWithScaling(g, x, y);
			} else {
				draw.drawNode(g, x, y, root);
				draw.drawLevel(g, root, x, y, this.getWidth(), 0, 8);
			}
		}

	}

	public static void main(String[] args) {
		new TreeVisualizer().run();
	}

	private final Board board;

	private DrawPanel draw;

	private JPanel gui;

	private JLabel moveLabel;

	private final Player player;
	
	private TreeNode root;
	
	private JLabel runsLabel;
	
	private JCheckBox scaling;
	
	private TreeNode selectedNode;
	
	private final TranspositionTable table;

	private final SimpleTreeUpdater updater;

	private JLabel winRateLabel;

	public TreeVisualizer() {
		player = new Player(1, CopiableStructureFactory.feasible(7));
		board = player.getBoard();
		table = new TranspositionTable(10, new SimpleSearchNodeBuilder(
				board.getCoordinateSystem()), board.getCoordinateSystem());
		updater = new SimpleTreeUpdater(board, table, 0);
		player.setTreeUpdater(updater);
		player.setTreeDescender(new UctDescender(board, table, 75));
	}

	/** Recursive method for constructing nodes of the navigable tree. */
	private TreeNode buildNode(SearchNode source, SearchNode parentSearchNode,
			TreeNode parent, short p, Board tempBoard) {
		final int runs = parentSearchNode == null ? source.getTotalRuns()
				: parentSearchNode.getRuns(p);
		final float winRate = parentSearchNode == null ? 0.5f : parentSearchNode
				.getWinRate(p);
		final TreeNode nodeToAdd = new TreeNode(winRate, runs, parent, tempBoard
				.getCoordinateSystem().toString(p));

		if (parent != null && parent.getChildren().size() > 0) {
			nodeToAdd.setPrevious(parent.getChildren().getLast());
			nodeToAdd.getPrevious().setNext(nodeToAdd);
		}
		final ListNode<SearchNode> children = source.getChildren();
		if (children != null) {
			for (final short point : tempBoard.getCoordinateSystem()
					.getAllPointsOnBoard()) {
				if (source.hasChild(point)) {
					final Board childBoard = new Board(tempBoard
							.getCoordinateSystem().getWidth());
					childBoard.copyDataFrom(tempBoard);
					childBoard.play(point);
					final SimpleSearchNode child = (SimpleSearchNode) table
							.findIfPresent(childBoard.getFancyHash());
					if (child != null) {
						nodeToAdd.addChild(buildNode(child, source, nodeToAdd,
								point, childBoard));
					}
				}

			}
		}
		return nodeToAdd;
	}

	/** Recursively build navegable tree based on the transition table data. */
	private void buildTree() {
		final SearchNode node = updater.getRoot();
		root = buildNode(node, null, null, (short) 0, new Board(player
				.getBoard().getCoordinateSystem().getWidth()));
		root.setSelected(true);
		selectedNode = root;
	}

	/** Displays the interactive frame. */
	private void run() {
		buildTree();
		selectedNode = root;
		// Set up JFrame
		final Dimension dimension = java.awt.Toolkit.getDefaultToolkit()
				.getScreenSize();
		setSize((int) (dimension.getWidth() * .85),
				(int) (dimension.getHeight() * .85));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		// Set up DrawPanel
		draw = new DrawPanel();
		draw.setPreferredSize(new Dimension((int) (dimension.getWidth() * .85),
				(int) (dimension.getHeight() * .75)));
		add(draw, BorderLayout.NORTH);
		// Add GUI elements
		gui = new JPanel();
		gui.setLayout(new GridLayout(1, 3));
		add(gui, BorderLayout.SOUTH);
		final JPanel infoPanel = new JPanel(new GridLayout(3, 1));
		gui.add(infoPanel);
		infoPanel.setBackground(Color.WHITE);
		gui.add(infoPanel, BorderLayout.EAST);
		// Set up key bindings
		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "descend");
		infoPanel.getActionMap().put("descend", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getChildren().size() > 0) {
					selectedNode.setSelected(false);
					selectedNode = selectedNode.getChildren().get(0);
					selectedNode.setSelected(true);
					updateLabels();
					repaint();
				}
			}
		});
		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("UP"), "ascend");
		infoPanel.getActionMap().put("ascend", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getParent() != null) {
					selectedNode.setSelected(false);
					selectedNode = selectedNode.getParent();
					selectedNode.setSelected(true);
					updateLabels();
					repaint();
				}
			}
		});
		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "right");
		infoPanel.getActionMap().put("right", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getNext() != null) {
					selectedNode.setSelected(false);
					selectedNode = selectedNode.getNext();
					selectedNode.setSelected(true);
					updateLabels();
					repaint();
				}
			}
		});
		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "left");
		infoPanel.getActionMap().put("left", new AbstractAction() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getPrevious() != null) {
					selectedNode.setSelected(false);
					selectedNode = selectedNode.getPrevious();
					selectedNode.setSelected(true);
					updateLabels();
					repaint();
				}
			}
		});
		// JLabels
		moveLabel = new JLabel("Move: null");
		infoPanel.add(moveLabel);
		winRateLabel = new JLabel("Win Rate: null");
		infoPanel.add(winRateLabel);
		runsLabel = new JLabel("Runs: null");
		infoPanel.add(runsLabel);
		// JButtons
		final JButton performRun = new JButton("Perform Run");
		performRun.setFocusable(false);
		performRun.addActionListener(new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				player.getMcRunnable(0).performMcRun();
				buildTree();
				updateLabels();
				repaint();
			}
		});
		gui.add(performRun);
		final JButton perform100Runs = new JButton("Perform 100 Runs");
		perform100Runs.setFocusable(false);
		perform100Runs.addActionListener(new ActionListener() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < 100; i++) {
					player.getMcRunnable(0).performMcRun();
				}
				buildTree();
				updateLabels();
				repaint();
			}
		});
		gui.add(perform100Runs);
		// JCheckBox
		scaling = new JCheckBox("Dynamic Scaling");
		scaling.setSelected(false);
		scaling.setFocusable(false);
		scaling.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		gui.add(scaling);
		// Make it so!
		revalidate();
		repaint();
	}

	/** Update text of labels based on the selected node. */
	private void updateLabels() {
		moveLabel.setText("Move: " + selectedNode.getMove());
		winRateLabel.setText("Win Rate: " + selectedNode.getWinRate());
		runsLabel.setText("Runs: " + selectedNode.getRuns());
	}

}
