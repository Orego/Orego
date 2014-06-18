package edu.lclark.orego.visuals;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.*;

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

@SuppressWarnings("serial")
public class TreeVisualizer extends JFrame {

	final Player player;

	final TranspositionTable table;

	final SimpleTreeUpdater updater;

	final Board board;

	private TreeNode root;

	private TreeNode selectedNode;

	private JPanel gui;
	private JLabel moveLabel;
	private JLabel winRateLabel;
	private JLabel runsLabel;
	private DrawPanel draw;
	private JCheckBox scaling;

	public static void main(String[] args) {
		new TreeVisualizer().run();
	}

	public TreeVisualizer() {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		board = player.getBoard();
		table = new TranspositionTable(1024 * 1024, new SimpleSearchNodeBuilder(
				board.getCoordinateSystem()),
				board.getCoordinateSystem());
		updater = new SimpleTreeUpdater(board, table);
		player.setTreeUpdater(updater);
		player.setTreeDescender(new UctDescender(board, table));
	}

	private void run() {
		// Run playouts to generate an initial tree
//		for (int i = 0; i < 100; i++) {
//			player.getMcRunnable(0).performMcRun();
//		}
		buildTree();
		selectedNode = root;

		// Set up JFrame
		Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int) (dimension.getWidth() * .85), (int) (dimension.getHeight() * .85));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		draw = new DrawPanel();
		draw.setPreferredSize(new Dimension((int) (dimension.getWidth() * .85), (int) (dimension
				.getHeight() * .75)));
		add(draw, BorderLayout.NORTH);

		// Add gui elements
		gui = new JPanel();
		gui.setLayout(new GridLayout(1, 3));
		add(gui, BorderLayout.SOUTH);

		JPanel infoPanel = new JPanel(new GridLayout(3, 1));
		gui.add(infoPanel);

		infoPanel.setBackground(Color.WHITE);
		gui.add(infoPanel, BorderLayout.EAST);

		// Set up key bindings
		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "descend");
		infoPanel.getActionMap().put("descend", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getChildren().size() > 0) {
					selectedNode.isSelected = false;
					selectedNode = selectedNode.getChildren().get(0);
					selectedNode.isSelected = true;
					updateLabels();
					repaint();
				}
			}
		});

		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("UP"), "ascend");
		infoPanel.getActionMap().put("ascend", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getParent() != null) {
					selectedNode.isSelected = false;
					selectedNode = selectedNode.getParent();
					selectedNode.isSelected = true;
					updateLabels();
					repaint();
				}
			}
		});

		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "right");
		infoPanel.getActionMap().put("right", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getNext() != null) {
					selectedNode.isSelected = false;
					selectedNode = selectedNode.getNext();
					selectedNode.isSelected = true;
					updateLabels();
					repaint();
				}
			}
		});

		infoPanel.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "left");
		infoPanel.getActionMap().put("left", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (selectedNode.getPrevious() != null) {
					selectedNode.isSelected = false;
					selectedNode = selectedNode.getPrevious();
					selectedNode.isSelected = true;
					updateLabels();
					repaint();
				}
			}
		});

		moveLabel = new JLabel("Move: null");
		infoPanel.add(moveLabel);
		winRateLabel = new JLabel("Win Rate: null");
		infoPanel.add(winRateLabel);
		runsLabel = new JLabel("Runs: null");
		infoPanel.add(runsLabel);

		JButton performRun = new JButton("Perform Run");
		performRun.setFocusable(false);
		performRun.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				player.getMcRunnable(0).performMcRun();
				buildTree();
				updateLabels();
				repaint();
			}
		});
		gui.add(performRun);

		JButton perform100Runs = new JButton("Perform 100 Runs");
		perform100Runs.setFocusable(false);
		perform100Runs.addActionListener(new ActionListener() {

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
		
		scaling = new JCheckBox("Draw with Scaling");
		scaling.isSelected();
		scaling.setFocusable(false);
		scaling.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
				
			}
			
		});
		gui.add(scaling);

		revalidate();
		repaint();
	}

	/** Update text of labels based on the selected node. */
	private void updateLabels() {
		moveLabel.setText("Move: " + selectedNode.getMove());
		winRateLabel.setText("Win Rate: " + selectedNode.getWinRate());
		runsLabel.setText("Runs: " + selectedNode.getRuns());
	}

	/** Recursively build navegable tree based on the transition table data. */
	private void buildTree() {
		SearchNode node = updater.getRoot();
		root = buildNode(node, null, null, (short) 0, new Board(player.getBoard()
				.getCoordinateSystem().getWidth()));
		root.isSelected = true;
		selectedNode = root;
	}

	/** Recursive method for constructing nodes of the navegable tree. */
	private TreeNode buildNode(SearchNode source, SearchNode parentSearchNode, TreeNode parent,
			short p, Board board) {
		int runs = parentSearchNode == null ? source.getTotalRuns() : parentSearchNode
				.getRuns(p);
		float winRate = parentSearchNode == null ? 0.5f : parentSearchNode
				.getWinRate(p);
		TreeNode nodeToAdd = new TreeNode(winRate, runs,
				parent, board.getCoordinateSystem().toString(p));

		if (parent != null && parent.getChildren().size() > 0) {
			nodeToAdd.setPrevious(parent.getChildren().getLast());
			nodeToAdd.getPrevious().setNext(nodeToAdd);
		}
		ListNode<SearchNode> children = source.getChildren();
		if (children != null) {
			for (short point : board.getCoordinateSystem().getAllPointsOnBoard()) {
				if (source.hasChild(point)) {
					Board childBoard = new Board(board.getCoordinateSystem().getWidth());
					childBoard.copyDataFrom(board);
					childBoard.play(point);
					SimpleSearchNode child = (SimpleSearchNode) table.findIfPresent(childBoard
							.getFancyHash());
					if (child != null) {
						nodeToAdd.addChild(buildNode(child, source, nodeToAdd, point, childBoard));
					}
				}

			}
		}
		return nodeToAdd;
	}

	/** Panel for drawing the nodes based on the navegable tree structure. */
	class DrawPanel extends JPanel {

		@Override
		public void paintComponent(Graphics g) {

			//Make sure the tree has been built
			if (root == null) {
				return;
			}
			super.paintComponent(g);
			int x = this.getWidth() / 2;
			int y = 20;
			
			if(scaling.isSelected()){
				drawWithScaling(g, x, y);
			} else {				
				draw.drawNode(g, x, y, root);
				draw.drawLevel(g, root, x, y, this.getWidth(), 0, 8);
			}
		}
		
		private void drawWithScaling(Graphics g, int x, int y){
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

		/** Recursively draws all the children of a node up to a max depth. */
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

		/**
		 * Draws the circle for a given node in the proper position, highlighted
		 * if the node is currently selected.
		 */
		private void drawNode(Graphics g, int x, int y, TreeNode node) {
			int diameter = 3 * (int) ((Math.log(node.getRuns())) / (Math.log(2)));
			x = x - (diameter / 2);
			y = y - (diameter / 2);
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

}
