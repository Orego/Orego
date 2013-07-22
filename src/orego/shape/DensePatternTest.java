package orego.shape;

import static org.junit.Assert.*;

import org.junit.Test;

public class DensePatternTest {

	@Test
	public void testToString() {
		String[] problem = { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"O#O................" // 1
		// ABCDEFGHJKLMNOPQRST
		};
		// Pretend pattern (all off board except center)
		DensePattern offBoard = new DensePattern(
				new int[] { 0xffff, 0xffff, 0xffff, 0xffff, 0xffff, 0xffff,
						0xffff, 0xffff, 0xffff, 0xffff });
		assertEquals(
				"****************************************.****************************************",
				offBoard.toString());
		// Empty pattern
		DensePattern emptyPat = new DensePattern(
				new int[] { 0xaaaa, 0xaaaa, 0xaaaa, 0xaaaa, 0xaaaa, 0xaaaa,
						0xaaaa, 0xaaaa, 0xaaaa, 0xaaaa });
		assertEquals(
				".................................................................................",
				emptyPat.toString());
		// Radius 1 pattern around d2
		DensePattern aroundD2 = new DensePattern(
				new int[] { 0xaa9a });
		assertEquals("......O..", aroundD2.toString());
		// Radius 2 pattern around b2
		DensePattern aroundB2 = new DensePattern(
				new int[] { 0xeaba, 0xaead, 0x1bff });
		assertEquals("*....*....*....*O#O.*****", aroundB2.toString());

		// Radius 2 pattern around t1
		DensePattern aroundT1 = new DensePattern(
				new int[] { 0xabea, 0xfaff, 0xffff });
		assertEquals("...**...**...************", aroundT1.toString());
	}

	@Test
	public void testEquals() {
		assertFalse((new DensePattern(new int[] {})).equals(""));
		assertFalse((new DensePattern(new int[] { 0xeaba, 0xaead,
				0x1bff })).equals(new DensePattern(new int[] {
				0xeaba, 0xaead, 0x1bff, 0x0 })));
		assertFalse((new DensePattern(new int[] { 0xeaba, 0xaead,
				0x1bff, 0x0 })).equals(new DensePattern(
				new int[] { 0xeaba, 0xaead, 0x1bff })));
		assertTrue((new DensePattern(new int[] { 0xeaba, 0xaead,
				0x1bff })).equals(new DensePattern(new int[] {
				0xeaba, 0xaead, 0x1bff })));
		assertTrue((new DensePattern(new int[] { 0xeaba }))
				.equals(new DensePattern(new int[] { 0xeaba })));
	}
	
	@Test
	public void testHashCode(){
		assertEquals(0xeaba,(new DensePattern(new int[] { 0xeaba})).hashCode());
		assertEquals((new DensePattern(new int[] { 0xeaba, 0xaead,
				0x1bff })).hashCode(),(new DensePattern(new int[] {
				0xeaba, 0xaead, 0x1bff })).hashCode());
		assertEquals((new DensePattern(new int[] { 0xeaba }))
				.hashCode(),(new DensePattern(new int[] { 0xeaba })).hashCode());
	}
}
