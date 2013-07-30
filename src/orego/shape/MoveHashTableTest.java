package orego.shape;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;

import org.junit.Before;
import org.junit.Test;

public class MoveHashTableTest {

	MoveHashTable table;
	
	@Before
	public void setup() {
		table = new MoveHashTable();
	}
	
	@Test
	public void testCollisions() {
		table.add(10L, 15);
		table.add(10L+0x100000, 20);
		assertEquals(15, table.getMove(10L));
		assertEquals(20, table.getMove(10L+0x100000));
	}
	
	@Test
	public void testHashChanges(){
		assertEquals(NO_POINT, table.getMove(10L));
		table.add(10L, 15);
		assertEquals(15, table.getMove(10L));
		table.add(10L, 23);
		assertEquals(23, table.getMove(10L));
	}

}
