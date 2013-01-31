package orego.decision;

import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import static orego.decision.DataCollection.*;
import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class NodeTest {

	DataCollection dataCollection;
	
	Node node;
	
	@Before
	public void setUp() throws Exception {
		dataCollection = new DataCollection();
		node = new Node(dataCollection, 0);
	}

	@Test
	public void testSplitTiming() {
		for (int i = 0; i < 1003; i++) {
			int[] previous = {i % 83};
			boolean category = (i % 3 == 0);
			Datum datum = new Datum(previous, category);
			node.addDatum(datum);
		}
		assertFalse(node.getPrevMove() == NO_SPLIT);
	}
	
	@Test
	public void testSplit(){
		int[] previous = {5};
		boolean category = true;
		Datum datum = new Datum(previous, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous, category);
		dataCollection.add(datum);
		category = true;
		datum = new Datum(previous, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous, category);
		dataCollection.add(datum);
		int[] previous2 = {3};
		category = true;
		datum = new Datum(previous2, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous2, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous2, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous2, category);
		dataCollection.add(datum);
		int[] previous3 = {2};
		category = true;
		datum = new Datum(previous3, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous3, category);
		dataCollection.add(datum);
		int[] previous4 = {at("c1")};
		category = false;
		datum = new Datum(previous4, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous4, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous4, category);
		dataCollection.add(datum);
		category = false;
		datum = new Datum(previous4, category);
		dataCollection.add(datum);
		dataCollection.getBestSplitMove(node);
		node.split();
		assertTrue(node.getNoChild() != null);
		assertTrue(node.getYesChild() != null);
		assertEquals(NO_SPLIT, node.getNoChild().getPrevMove());
		assertEquals(NO_SPLIT, node.getYesChild().getPrevMove());
		assertEquals(10 , node.getNoChild().getDataCollection().size());
		assertEquals(4 , node.getYesChild().getDataCollection().size());
	}

	@Test
	public void testSplitOnOlderMove() {
		Node root = new Node(new DataCollection(), 1);
		Node yes = new Node(new DataCollection(), 2);
		Node no = new Node(new DataCollection(), 1);
		root.setChildren(yes, no);
		root.setPrevMove(at("a1"));
		root.addDatum(new Datum(new int[] {at("b1"), at("a1")}, true));
		assertEquals(1, yes.getDataCollection().size());
	}

	@Test
	public void testSplitAtCorrectDepth() {
		dataCollection.add(new Datum(new int[] {at("a1"), at("d1")}, true));
		dataCollection.add(new Datum(new int[] {at("b1"), at("d1")}, true));
		dataCollection.add(new Datum(new int[] {at("c1"), at("d1")}, true));
		dataCollection.add(new Datum(new int[] {at("a1"), at("e1")}, false));
		dataCollection.add(new Datum(new int[] {at("a1"), at("e1")}, false));
		dataCollection.add(new Datum(new int[] {at("a1"), at("e1")}, false));
		Node root = new Node(dataCollection, 0);
		dataCollection.getBestSplitMove(root);
		assertEquals(0, root.getDepth());
		assertEquals(at("a1"), root.getPrevMove());
	}

	@Test
	public void testWinRate() {
		Board board = new Board();
		DataCollection dataCollection1 = new DataCollection();
		DataCollection dataCollection2 = new DataCollection();
		DataCollection dataCollection3 = new DataCollection();
		dataCollection1.add(new Datum(new int[] {at("b1"), at("a1")}, true));
		dataCollection1.add(new Datum(new int[] {at("b1"), at("a1")}, true));
		dataCollection1.add(new Datum(new int[] {at("b1"), at("a1")}, false));
		dataCollection2.add(new Datum(new int[] {at("c1"), at("a1")}, false));
		dataCollection2.add(new Datum(new int[] {at("c1"), at("a1")}, false));
		dataCollection3.add(new Datum(new int[] {at("c1"), at("d1")}, true));
		dataCollection3.add(new Datum(new int[] {at("c1"), at("d1")}, false));
		Node root = new Node(null,0);
		Node yes = new Node(null, 1);
		Node no = new Node(dataCollection3, 0);
		root.setChildren(yes, no);
		Node yes1 = new Node(dataCollection1, 2);
		Node no1 = new Node(dataCollection2, 1);
		yes.setChildren(yes1, no1);
		root.setPrevMove(at("a1"));
		yes.setPrevMove(at("b1"));
		board.play(at("b1"));
		board.play(at("a1"));
		assertEquals(0.66,root.getWinRate(board),0.01);
		board.play(at("c1"));
		board.play(at("d1"));
		assertEquals(0.50,root.getWinRate(board),0.01);
		board.clear();
		board.play(at("c1"));
		board.play(at("a1"));
		assertEquals(0.00,root.getWinRate(board),0.01);
	}
}
