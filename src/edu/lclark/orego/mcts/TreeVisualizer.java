package edu.lclark.orego.mcts;

import java.awt.*;

import javax.swing.JFrame;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.StoneColor.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ListNode;

public class TreeVisualizer extends JFrame{
	
	final Player player;
	
	final TranspositionTable table;
	
	final SimpleTreeUpdater updater;
	
	final Board board;
	
	public static void main(String[] args) {
		new TreeVisualizer().run();
	}
	
	public TreeVisualizer(){
		player = new Player(1, CopiableStructureFactory.feasible(5));
		board = player.getBoard();
		table = new TranspositionTable(new SimpleSearchNodeBuilder(board.getCoordinateSystem()), board.getCoordinateSystem());
		updater = new SimpleTreeUpdater(board, table);
		player.setTreeUpdater(updater);
		player.setTreeDescender(new UctDescender(board, table));
		Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int)(dimension.getWidth()*.85), (int)(dimension.getHeight()*.85));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void run() {
		for(int i =0; i<100; i++){
			player.getMcRunnable(0).performMcRun();
		}
		repaint();
	}
	
	@Override
	public void paint(Graphics g){
		g.setColor(new Color(225, 225, 225));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int x = this.getWidth()/2;
		int y = 20;
		SearchNode node = updater.getRoot();
		float winRate = node.getWinRate((short)0);
		int diameter = node.getTotalRuns()/2;
		Color c = new Color(winRate, winRate, winRate);
		g.setColor(c);
		
		g.fillOval(x, y, diameter, diameter);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, diameter, diameter);
		drawLevel(g, node, x, y, this.getWidth(), 0, 5);
	}
	
	public void drawLevel(Graphics g, SearchNode parent, int x, int y, int width, int depth, int maxDepth){
		if(maxDepth==depth){
			return;
		}
		System.out.println("loop?");
		ListNode<SearchNode> children = parent.getChildren();
		System.out.println(children);
		if(children == null){
			return;
		}
		System.out.println("Children is not null");
		int size = 1;
		ListNode<SearchNode> loopChild = children.getNext();
		while(loopChild!=null){
			loopChild=loopChild.getNext();
			System.out.println("LoopChild = " + loopChild);
			size++;
		}
		x = x-(width/2);
		int newWidth = width/size;
		CoordinateSystem coords = board.getCoordinateSystem();
		Board childBoard = new Board(coords.getWidth());
		int i = 0;
		for (short p : coords.getAllPointsOnBoard()) {
			if (parent.hasChild(p)) {
				System.out.println("Child at " + coords.toString(p));
				int radius = parent.getRuns(p);
				float winRate = parent.getWinRate(p);
				g.setColor(new Color(winRate, winRate, winRate));
				g.fillOval(x + (newWidth/2) + (i*newWidth), y + 100, radius, radius);
				g.setColor(Color.BLACK);
				g.drawOval(x + (newWidth/2) + (i*newWidth), y + 100, radius, radius);
				childBoard.copyDataFrom(board);
				childBoard.play(p);
				// TODO Ugly cast
				SimpleSearchNode child = (SimpleSearchNode)table.findIfPresent(childBoard.getFancyHash());
				if (child != null) {
					drawLevel(g, child, x + (newWidth/2) + (i*newWidth), y + 100, newWidth, depth+1, maxDepth);
				}
				i++;
			}
		}
		short p = PASS;
		if (parent.hasChild(p)) {
			int radius = parent.getRuns(p);
			float winRate = parent.getWinRate(p);
			g.setColor(new Color(winRate, winRate, winRate));
			g.fillOval(x + (newWidth/2) + (i*newWidth), y + 100, radius, radius);
			g.setColor(Color.BLACK);
			g.drawOval(x + (newWidth/2) + (i*newWidth), y + 100, radius, radius);
			childBoard.copyDataFrom(board);
			childBoard.play(p);
			SimpleSearchNode child = (SimpleSearchNode)table.findIfPresent(childBoard.getFancyHash());
			if (child != null) {
				drawLevel(g, child, x + (newWidth/2) + (i*newWidth), y + 100, newWidth, depth+1, maxDepth);
			}
		}
	}
	
	
}
