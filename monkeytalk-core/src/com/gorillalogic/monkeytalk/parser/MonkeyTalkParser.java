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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * Static helper class to support converting MonkeyTalk command strings into MonkeyTalk
 * {@link Command} objects.
 * 
 * @see Command
 */
public class MonkeyTalkParser {
	private static final String CRAZY = "\u21D0\u21D1\u21D2\u21D3\u21D5\u21D4\u21DD\u21DC";

	private static final Pattern TOKEN = Pattern
			.compile("[^\\s\"=]+=\"[^\"]*\"|[^\\s\"]+|\"[^\"]*\"");

	private MonkeyTalkParser() {
	}

	/**
	 * Parse a MonkeyTalk command string into a list of tokens, correctly handling quoted strings
	 * and escaped quotes.
	 * 
	 * @param command
	 *            the MonkeyTalk command string
	 * @return a list of tokens
	 */
	public static List<String> parse(String command) {
		List<String> tokens = new ArrayList<String>();

		if (command == null) {
			return tokens;
		}

		// trim off any extra space
		command = command.trim();

		if (command.startsWith(Command.COMMENT_PREFIX)) {
			// command is a comment, so just return a single token
			tokens.add(command);
		} else {
			// this is a hack: replace all escaped quotes with crazy unicode
			command = command.replaceAll("\\\\\"", CRAZY);

			Matcher tokenMatcher = TOKEN.matcher(command);
			while (tokenMatcher.find()) {
				String token = tokenMatcher.group();

				// revert the hack, and put escaped quotes back into the token
				token = token.replaceAll(CRAZY, "\\\\\"");

				tokens.add(token);
			}
		}

		return tokens;
	}

	/**
	 * Parse a MonkeyTalk text file, ignoring blanks, and return just a list of MonkeyTalk
	 * {@link Command} objects.
	 * 
	 * @param f
	 *            the MonkeyTalk input file
	 * @return the list of Commands
	 */
	public static List<Command> parseFile(File f) {
		return parseFile(f, false, true);
	}

	/**
	 * Parse a MonkeyTalk text file, with commands, blanks, and comments into a list of MonkeyTalk
	 * {@link Command} objects.
	 * 
	 * @param f
	 *            the MonkeyTalk input file
	 * @param ignoreComments
	 *            if true, don't return any MonkeyTalk comments (aka lines beginning with
	 *            <code>#</code>).
	 * @param ignoreBlanks
	 *            if true, don't return any blank lines
	 * @return the list of Commands
	 */
	public static List<Command> parseFile(File f, boolean ignoreComments, boolean ignoreBlanks) {

		if (f == null) {
			return null;
		}
		
		try {
			String text = FileUtils.readFile(f);
			return parseText(text, ignoreComments, ignoreBlanks);
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			return new ArrayList<Command>();
		}
	}

	/**
	 * Parse the MonkeyTalk command text, ignoring blanks, and return just a list of MonkeyTalk
	 * {@link Command} objects.
	 * 
	 * @param text
	 *            the MonkeyTalk command text
	 * @return the list of Commands
	 */
	public static List<Command> parseText(String text) {
		return parseText(text, false, true);
	}

	/**
	 * Parse the MonkeyTalk command text, with commands, blanks, and comments into a list of
	 * MonkeyTalk {@link Command} objects.
	 * 
	 * @param text
	 *            the MonkeyTalk command text
	 * @param ignoreComments
	 *            if true, don't return any MonkeyTalk comment objects (aka if true lines beginning
	 *            with <code>#</code> will be ignored).
	 * @param ignoreBlanks
	 *            if true, don't return any blank MonkeyTalk {@link Command} objects (aka if true
	 *            blank lines will be ignored).
	 * @return the list of Commands
	 */
	public static List<Command> parseText(String text, boolean ignoreComments, boolean ignoreBlanks) {
		List<Command> commands = new ArrayList<Command>();
		Scanner scanner = new Scanner(text);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			if (ignoreBlanks && line.length() == 0) {
				continue;
			}

			if (ignoreComments && line.startsWith(Command.COMMENT_PREFIX)) {
				continue;
			}

			Command cmd = new Command(line);
			commands.add(cmd);
		}

		return commands;
	}
}