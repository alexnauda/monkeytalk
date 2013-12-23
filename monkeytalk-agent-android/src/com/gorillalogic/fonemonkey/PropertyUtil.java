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
package com.gorillalogic.fonemonkey;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class PropertyUtil {
	/**
	 * Gets a property from a root object and a path. e.g. "font.name" would translate to
	 * getFont().getName()
	 */
	public static String getProperty(Object obj, String path) throws Throwable {
		int dot = path.indexOf('.');

		if (dot < 0) {
			return "" + getValue(obj, path);
		}

		Object ret = getValue(obj, path.substring(0, dot));

		if (ret == null) {
			throw new IllegalArgumentException("No such property or unable to retrieve value for "
					+ path.substring(0, dot) + " on object " + obj);
		}

		return getProperty(ret, path.substring(dot + 1));
	}

	private static Object getValue(Object obj, String name) throws Throwable {
		String method = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		String method2 = "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

		try {
			// Not supported in API 8
			// Method meth = obj.getClass().getMethod(method, null);

			Method meth = getMethod(obj.getClass(), method, method2);

			if (meth == null) {
				throw new IllegalArgumentException("No such property method " + method + " or "
						+ method2 + " on class " + obj.getClass().getName());
			}

			return meth.invoke(obj);
		} catch (Throwable e) {
			Log.log(e);
			throw e;
		}
	}

	private static Method getMethod(Class<?> klass, String name, String name2) throws Exception {
		Method[] methods = klass.getMethods();

		for (int i = 0; i < methods.length; ++i) {
			if (methods[i].getParameterTypes().length == 0) {
				if (methods[i].getName().equals(name) || methods[i].getName().equals(name2))
					return methods[i];
			}
		}

		return null;
	}

	public static boolean wildcardMatch(String wc, String s) {
		if (s == null || wc == null) {
			return false;
		}

		return matchWildcard(s, wc);
	}

	/**
	 * A better wildcard pattern matcher (written by Justin).
	 * 
	 * @param src
	 *            the string to search for the pattern
	 * @param pattern
	 *            the wildcard pattern (containing * and ?)
	 * @return true if matches, otherwise false
	 */
	private static boolean matchWildcard(String src, String pattern) {
		// first, escape everything in the pattern that's not a wildcard char (either * or ?)
		StringBuilder sb = new StringBuilder();
		for (char c : pattern.toCharArray()) {
			if ("*?".indexOf(c) != -1) {
				sb.append(c);
			} else {
				// not wildcard char, so escape it
				sb.append("\\Q").append(c).append("\\E");
			}
		}

		// 1. replace * (or repeated *'s) with .*
		// 2. replace ? with .
		pattern = sb.toString().replaceAll("\\*+", ".*").replaceAll("\\?", ".");

		Pattern p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE);

		return p.matcher(src).matches();
	}

	/**
	 * Adapted from org.apache.commons.digester.SimpleRegexMatcher
	 */
	// private static boolean wcmatch(String basePattern, String regexPattern, int baseAt, int
	// regexAt) {
	// if (regexAt >= regexPattern.length()) {
	// // maybe we've got a match
	// if (baseAt >= basePattern.length()) {
	// // ok!
	// return true;
	// }
	// // run out early
	// return false;
	// } else {
	// if (baseAt >= basePattern.length()) {
	// // run out early
	// return false;
	// }
	// }
	//
	// // ok both within bounds
	// char regexCurrent = regexPattern.charAt(regexAt);
	// switch (regexCurrent) {
	// case '*':
	// // this is the tricky case
	// // check for terminal
	// if (++regexAt >= regexPattern.length()) {
	// // this matches anything let - so return true
	// return true;
	// }
	// // go through every subsequent apperance of the next character
	// // and so if the rest of the regex matches
	// char nextRegex = regexPattern.charAt(regexAt);
	// int nextMatch = basePattern.indexOf(nextRegex, baseAt);
	// while (nextMatch != -1) {
	// if (wcmatch(basePattern, regexPattern, nextMatch, regexAt)) {
	// return true;
	// }
	// nextMatch = basePattern.indexOf(nextRegex, nextMatch + 1);
	// }
	// return false;
	// case '?':
	// // this matches anything
	// return wcmatch(basePattern, regexPattern, ++baseAt, ++regexAt);
	// default:
	// if (regexCurrent == basePattern.charAt(baseAt)) {
	// // still got more to go
	// return wcmatch(basePattern, regexPattern, ++baseAt, ++regexAt);
	// }
	// return false;
	// }
	// }
}
