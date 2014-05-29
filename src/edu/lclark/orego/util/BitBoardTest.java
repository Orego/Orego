package edu.lclark.orego.util;

import static org.junit.Assert.*;
import orego.util.BitVector;

import org.junit.Before;
import org.junit.Test;

public class BitBoardTest {
	
	private BitBoard bitBoard;
	
	@Before
	public void setUp() throws Exception {
		bitBoard = new BitBoard(19);
	}

	@Test
	public void testSet() {
		bitBoard.set(0, 0);
		assertEquals(1, bitBoard.getRow(0));
		assertEquals(0, bitBoard.getRow(1));
		bitBoard.set(0,2);
		assertEquals(5, bitBoard.getRow(0));
		
	}
	
	@Test
	public void testClear(){
		bitBoard.set(0, 5);
		assertEquals(32, bitBoard.getRow(0));
		bitBoard.clear();
		assertEquals(0, bitBoard.getRow(0));
	}

}
