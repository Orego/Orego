package edu.lclark.orego.patterns;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class PatternExtractorTest {

	private PatternExtractor extractor;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		extractor = new PatternExtractor();
		coords = CoordinateSystem.forWidth(19);
	}

	@Test
	public void testRotate90Degrees() {
		extractor.analyzeMove(coords.at("A5"));
		extractor.analyzeMove(coords.at("B6"));
		extractor.analyzeMove(coords.at("A7"));
		extractor.analyzeMove(coords.at("A6"));
		assertEquals(0.5, extractor.getWinRate(43425), 0.01);
		assertEquals(1.0, extractor.getWinRate(47900), 0.01);
		assertEquals(1.0, extractor.getWinRate(44867), 0.01);
		assertEquals(1.0, extractor.getWinRate(64193), 0.01);
	}

}
