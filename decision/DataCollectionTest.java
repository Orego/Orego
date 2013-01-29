package orego.decision;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static orego.core.Coordinates.*;

public class DataCollectionTest {

	private DataCollection dataCollection;

	@Before
	public void setup() {
		dataCollection = new DataCollection();
		for (int i = 0; i < 100; i++) {
			int[] previous = {i % 83};
			boolean category = (i % 3 == 0);
			Datum datum = new Datum(previous, category);
			dataCollection.add(datum);
		}
	}

	@Test
	public void testCountCategory() {
		assertEquals(34, dataCollection.countWins());
	}

	@Test
	public void testCountPositiveWithAttribute() {
		assertEquals(1, dataCollection.countWinsAfter(81, 0, true));
		assertEquals(0, dataCollection.countWinsAfter(11, 0, true));
		assertEquals(0, dataCollection.countWinsAfter(32, 0, true));
		int[] previous = {54};
		Datum d = new Datum(previous, true);
		dataCollection.add(d);
		Datum e = new Datum(new int[] {32}, true);
		dataCollection.add(e);
		assertEquals(2, dataCollection.countWinsAfter(54, 0, true));
		assertEquals(1, dataCollection.countWinsAfter(32, 0, true));
	}

	@Test
	public void testCountWithPrevious() {
		assertEquals(1, dataCollection.countAfter(81, 0));
		assertEquals(2, dataCollection.countAfter(11, 0));
		assertEquals(1, dataCollection.countAfter(32, 0));
		int[] previous = {54};
		Datum d = new Datum(previous, true);
		dataCollection.add(d);
		Datum e = new Datum(new int[] {16}, true);
		dataCollection.add(e);
		assertEquals(2, dataCollection.countAfter(54, 0));
		assertEquals(3, dataCollection.countAfter(16, 0));
	}

	@Test
	public void testRemainder() {
		int[] previous = {11};
		Datum d = new Datum(previous, true);
		dataCollection.add(d);
		assertEquals(.930918, dataCollection.remainder(11, 0), .001);
	}

	@Test
	public void testGetBestSplitMove() {
		dataCollection = new DataCollection();
		dataCollection.add(new Datum(new int[] {at("a1")}, false));
		dataCollection.add(new Datum(new int[] {at("a1")}, false));
		dataCollection.add(new Datum(new int[] {at("a1")}, false));
		dataCollection.add(new Datum(new int[] {at("a1")}, true));
		dataCollection.add(new Datum(new int[] {at("b1")}, false));
		dataCollection.add(new Datum(new int[] {at("b1")}, false));
		dataCollection.add(new Datum(new int[] {at("b1")}, false));
		dataCollection.add(new Datum(new int[] {at("b1")}, false));
		dataCollection.add(new Datum(new int[] {at("c1")}, false));
		dataCollection.add(new Datum(new int[] {at("c1")}, false));
		dataCollection.add(new Datum(new int[] {at("c1")}, true));
		dataCollection.add(new Datum(new int[] {at("c1")}, true));
		Node node = new Node(dataCollection, 0);
		dataCollection.getBestSplitMove(node);
		assertEquals(at("b1"), node.getPrevMove());
		assertEquals(0, node.getDepth());
	}
}
