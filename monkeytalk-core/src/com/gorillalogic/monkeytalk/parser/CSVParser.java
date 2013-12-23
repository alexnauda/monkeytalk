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
package com.gorillalogic.monkeytalk.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gorillalogic.monkeytalk.Command;

/**
 * Static helper class to support converting CSV data into a hash.
 */
public class CSVParser {
	private static final String CRAZY = "\u21D0\u21D1\u21D2\u21D3\u21D5\u21D4\u21DD\u21DC";

	private static final Pattern regex = Pattern
			.compile("([^\\s,\"]+)|\"([^\"]*)\"|\\s*,\\s*");

	private CSVParser() {
	}

	/**
	 * Parse a CSV string into a list of tokens, correctly handling quoted
	 * strings and escaped quotes.
	 * 
	 * @param csv
	 *            the CSV string to be parsed
	 * @return a list of tokens
	 */
	public static List<String> parse(String csv) {
		List<String> tokens = new ArrayList<String>();

		if (csv == null) {
			return tokens;
		}

		// trim off any extra space
		csv = csv.trim();

		// this is a hack: replace all escaped quotes with crazy unicode
		csv = csv.replaceAll("\\\\\"", CRAZY);

		Matcher regexMatcher = regex.matcher(csv);
		while (regexMatcher.find()) {
			String token = regexMatcher.group().trim();
			if (regexMatcher.group(1) != null || regexMatcher.group(2) != null) {

				if (token.startsWith("\"") && token.endsWith("\"")) {
					token = token.substring(1, token.length() - 1);
				}

				// revert the hack, and put escaped quotes back into the token
				token = token.replaceAll(CRAZY, "\\\\\"");

				tokens.add(token);
			}
		}

		return tokens;
	}

	/**
	 * Parse a CSV file, ignoring any blank lines, just a list of MonkeyTalk
	 * {@link Command} objects.
	 * 
	 * @param f
	 *            the MonkeyTalk input file
	 * @return the list of Commands
	 */
	public static List<Map<String, String>> parseFile(File f) {
		return parseFile(f, true);
	}

	/**
	 * Parse a MonkeyTalk text file, with commands, blanks, and comments into a
	 * list of MonkeyTalk {@link Command} objects.
	 * 
	 * 
	 * @param f
	 *            the MonkeyTalk input file
	 * @param ignoreBlanks
	 *            if true, don't return any blank lines
	 * @return the list of Commands
	 */
	public static List<Map<String, String>> parseFile(File f,
			boolean ignoreBlanks) {

		if (f == null) {
			return null;
		}

		List<Map<String, String>> csv = new ArrayList<Map<String, String>>();
		Scanner scanner;

		try {
			scanner = new Scanner(f, "UTF-8");
		} catch (FileNotFoundException ex) {
			scanner = null;
		}

		if (scanner == null) {
			return null;
		}

		if (!scanner.hasNextLine()) {
			// no header row
			return null;
		}

		String headerLine = scanner.nextLine().trim();
		List<String> headers = parse(headerLine);

		if (headers.size() == 0) {
			// no headers
			return null;
		}

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			if (ignoreBlanks && line.length() == 0) {
				continue;
			}

			List<String> tokens = parse(line);

			Map<String, String> map = new LinkedHashMap<String, String>();

			for (int i = 0; i < headers.size(); i++) {
				String header = headers.get(i);
				map.put(header, (i < tokens.size() ? tokens.get(i) : null));
			}
			csv.add(map);
		}

		return csv;
	}
}