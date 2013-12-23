/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.monkeytalk.verify;

import java.io.File;
import java.util.regex.Pattern;

public class Verify {

	/**
	 * Compare expected with actual. Returns true if they are equal (case-sensitive), otherwise
	 * false.
	 * 
	 * @param expected
	 *            the expected value (as a String)
	 * @param actual
	 *            the actual value (as a String)
	 * @return True if they are non-null and equal (case-sensitive), otherwise false
	 */
	public static boolean verify(String expected, String actual) {
		return (expected != null && expected.equals(actual));
	}

	/**
	 * Compare expected with actual. Returns true if they are <i>not</i> equal (case-sensitive),
	 * otherwise false.
	 * 
	 * @param expected
	 *            the expected value (as a String)
	 * @param actual
	 *            the actual value (as a String)
	 * @return True if they are non-null and not equal (case-sensitive), otherwise false
	 */
	public static boolean verifyNot(String expected, String actual) {
		return (expected != null && !expected.equals(actual));
	}

	/**
	 * Compare actual with the given wildcard pattern. Returns true if actual matches the wildcard
	 * pattern, otherwise false.
	 * 
	 * @param wildcard
	 *            the wildcard pattern (containing * and ?)
	 * @param actual
	 *            the actual value
	 * @return True if actual matches the wildcard pattern, otherwise false
	 */
	public static boolean verifyWildcard(String wildcard, String actual) {
		if (wildcard == null || actual == null) {
			return false;
		}

		return _verifyWildcard(wildcard, actual);
	}

	/**
	 * Compare actual with the given wildcard pattern. Returns true if actual does <i>not</i> match
	 * the wildcard pattern, otherwise false.
	 * 
	 * @param wildcard
	 *            the wildcard pattern (containing * and ?)
	 * @param actual
	 *            the actual value
	 * @return True if actual does not match the wildcard pattern, otherwise false
	 */
	public static boolean verifyNotWildcard(String wildcard, String actual) {
		if (wildcard == null || actual == null) {
			return false;
		}

		return !_verifyWildcard(wildcard, actual);
	}

	/**
	 * Helper to do the comparison between actual and the given wildcard pattern. Returns true if
	 * actual matches the wildcard pattern, otherwise false. The wildcard pattern is converted to a
	 * regex with escaping.
	 * 
	 * @param wildcard
	 *            the wildcard pattern (containing * and ?)
	 * @param actual
	 *            the actual value
	 * @return True if actual matches the wildcard pattern, otherwise false
	 */
	private static boolean _verifyWildcard(String wildcard, String actual) {
		// first, escape everything in the pattern that's not a wildcard char (either * or ?)
		StringBuilder sb = new StringBuilder();
		for (char c : wildcard.toCharArray()) {
			if ("*?".indexOf(c) != -1) {
				sb.append(c);
			} else {
				// not wildcard char, so escape it
				sb.append("\\Q").append(c).append("\\E");
			}
		}

		// next, replace * (or repeated *'s) with .*
		wildcard = sb.toString().replaceAll("\\*+", ".*");

		// last, replace any ? with .
		wildcard = wildcard.replaceAll("\\?", ".");

		Pattern p = Pattern.compile(wildcard, Pattern.DOTALL | Pattern.MULTILINE);
		return p.matcher(actual).matches();
	}

	/**
	 * Compare actual with the given regex pattern. Returns true if actual matches the regex
	 * pattern, otherwise false.
	 * 
	 * @param regex
	 *            the regex pattern
	 * @param actual
	 *            the actual value
	 * @return True if actual matches the regex pattern, otherwise false
	 */
	public static boolean verifyRegex(String regex, String actual) {
		if (regex == null || actual == null) {
			return false;
		}

		return actual.matches(regex);
	}

	/**
	 * Compare actual with the given regex pattern. Returns true if actual does <i>not</i> match the
	 * regex pattern, otherwise false.
	 * 
	 * @param regex
	 *            the regex pattern
	 * @param actual
	 *            the actual value
	 * @return True if actual does not matches the regex pattern, otherwise false
	 */
	public static boolean verifyNotRegex(String regex, String actual) {
		if (regex == null || actual == null) {
			return false;
		}

		return !actual.matches(regex);
	}

	/**
	 * Compare the expected image with the actual image with the given fuzziness.
	 * 
	 * @param expected
	 *            the expected png image
	 * @param actual
	 *            the returned base64 encoded image
	 * @param fuzziness
	 *            the amount of allowed slop in red, green, blue, or alpha (0 is exactly equal, 128
	 *            matches anything)
	 * @return true if equal
	 */
	public static boolean verifyImage(File expected, String actual, int fuzziness) {
		// http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
		throw new RuntimeException("not yet impl");
	}
}