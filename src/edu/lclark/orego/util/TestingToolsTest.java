package edu.lclark.orego.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestingToolsTest {

	@SuppressWarnings("static-method")
	@Test
	public void testAsOneString() {
		assertEquals("foo\nbar\n", TestingTools.asOneString(new String[] {"foo", "bar"}));
	}

}
