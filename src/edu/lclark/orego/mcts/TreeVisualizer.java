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
	
	final TreeIncorporator treeIncorporator;
	
	final Board board;
	
	public static void main(String[] args) {
		new TreeVisualizer().run();
	}
	
	public TreeVisualizer(){
		player = new Player(1, CopiableStructureFactory.feasible(5));
		board = player.getBoard();
		table = new TranspositionTable(new SimpleSearchNodeBuilder(board.getCoordinateSystem()), board.getCoordinateSystem());
		treeIncorporator = new TreeIncorporator(board, table);
		player.setRunIncorporator(treeIncorporator);
		Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		setSize((int)(dimension.getWidth()*.85), (int)(dimension.getHeight()*.85));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void run() {
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(player.getBoard().getCoordinateSystem().at("b1"));
		runnable.acceptMove(player.getBoard().getCoordinateSystem().at("c4"));
		runnable.acceptMove(player.getBoard().getCoordinateSystem().at("a2"));
		treeIncorporator.incorporateRun(BLACK, runnable);
		runnable.copyDataFrom(board);
		runnable.acceptMove(player.getBoard().getCoordinateSystem().at("b2"));
		runnable.acceptMove(player.getBoard().getCoordinateSystem().at("c5"));
		runnable.acceptMove(player.getBoard().getCoordinateSystem().at("a1"));
		treeIncorporator.incorporateRun(BLACK, runnable);
		
		
//		McRunnable mcRunnable = player.getMcRunnable(0);
//		player.getMcRunnable(0).acceptMove(player.getBoard().getCoordinateSystem().at("a3"));
//		player.incorporateRun(WHITE, player.getMcRunnable(0));
//		mcRunnable.copyDataFrom(board);
//		player.getMcRunnable(0).acceptMove(player.getBoard().getCoordinateSystem().at("d4"));
//		player.incorporateRun(BLACK, player.getMcRunnable(0));
//		mcRunnable.copyDataFrom(board);
//		player.getMcRunnable(0).acceptMove(player.getBoard().getCoordinateSystem().at("d3"));
//		player.getMcRunnable(0).acceptMove(player.getBoard().getCoordinateSystem().at("d5"));
//		player.incorporateRun(BLACK, player.getMcRunnable(0));
//		System.out.println(treeIncorporator.getRoot().deepToString(board, table, 5));
		repaint();
	}
	
	@Override
	public void paint(Graphics g){
		g.setColor(new Color(225, 225, 225));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		int x = this.getWidth()/2;
		int y = 20;
		SearchNode node = treeIncorporator.getRoot();
		float winRate = node.getWinRate((short)0);
		int diameter = node.getTotalRuns()/2;
		Color c = new Color(winRate, winRate, winRate);
		g.setColor(c);
		
		g.fillOval(x, y, diameter, diameter);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, diameter, diameter);
		drawLevel(g, node, x, y, this.getWidth());
	}
	
	public void drawLevel(Graphics g, SearchNode parent, int x, int y, int width){
		System.out.println("loop?");
		ListNode<SearchNode> children = parent.getChildren();
		if(children == null){
			return;
		}
		int size = 1;
		ListNode<SearchNode> loopChild = children.getNext();
		while(loopChild!=null){
			loopChild=children.getNext();
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
					drawLevel(g, child, x + (newWidth/2) + (i*newWidth), y + 100, newWidth);
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
				drawLevel(g, child, x + (newWidth/2) + (i*newWidth), y + 100, newWidth);
			}
		}
	}
	
	
}
