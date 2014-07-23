package edu.lclark.orego.patterns;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class ShapeExtractorTest {

	public ShapeExtractor extractor;

	@Before
	public void setUp() throws Exception {
		extractor = new ShapeExtractor(false, 0.9f, 8);
	}

	@Test
	public void test() {
		String outputFile = "test-books/3x3PatternTest.data";
		extractor.buildPatternData("sgf-test-files/19/PatternTest.sgf", outputFile);
		ShapeTable table = new ShapeTable(outputFile);
		Board board = new Board(9);
		board.play("d5");
		board.play("a1");
		board.play("f5");
		assertTrue(table.getWinRate(PatternFinder.getHash(board,
				board.getCoordinateSystem().at("e4"), 8)) > 0.5);
		assertTrue(table.getWinRate(PatternFinder.getHash(board,
				board.getCoordinateSystem().at("e6"), 8)) > 0.5);
	}
}
