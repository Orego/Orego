package edu.lclark.orego.util;

/** Static methods for use in writing JUnit tests. */
public final class TestingTools {

	/**
	 * Returns a single String made by concatenating all the strings in diagram.
	 */
	public static String asOneString(String[] diagram) {
		String result = "";
		for (String s : diagram) {
			result += s + "\n";
		}
		return result;
	}

}
