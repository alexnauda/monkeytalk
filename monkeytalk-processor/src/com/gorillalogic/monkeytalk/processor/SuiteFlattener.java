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
package com.gorillalogic.monkeytalk.processor;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;

/**
 * Helper class to count the total number of tests in a suite.
 */
public class SuiteFlattener {
	public static final int BAD_FILENAME = -1;
	public static final int BAD_SUITE = -2;
	public static final int SUITE_NOT_FOUND = -3;

	private CommandWorld world;

	/**
	 * Instantiate a suite flattener with the given project root directory.
	 * 
	 * @param rootDir
	 *            the project root directory
	 */
	public SuiteFlattener(File rootDir) {
		this(new CommandWorld(rootDir));
	}

	/**
	 * Instantiate a suite flattener with the given command world.
	 * 
	 * @param world
	 *            the command world for the project
	 */
	public SuiteFlattener(CommandWorld world) {
		this.world = world;
	}

	/**
	 * Flatten the given suite (aka count the total number of tests). Data-driven tests are counted
	 * by counting the number of rows inside the driving datafile. Return a negative value upon
	 * error.
	 * 
	 * @param filename
	 *            the suite filename
	 * @return the total number of tests in a suite, negative values are returned for errors.
	 */
	public int flatten(String filename) {
		if (filename == null) {
			return BAD_FILENAME;
		}

		List<Command> commands = world.getSuite(filename);

		if (commands == null) {
			if (filename.toLowerCase().endsWith(CommandWorld.SCRIPT_EXT)) {
				return BAD_SUITE;
			}
			return SUITE_NOT_FOUND;
		} else if (commands.size() == 0) {
			return 0;
		}

		int i = 0;

		for (Command cmd : commands) {
			if ("test.run".equalsIgnoreCase(cmd.getCommandName())) {
				i++;
			} else if ("test.runwith".equalsIgnoreCase(cmd.getCommandName())) {
				if (cmd.getArgs().size() == 0) {
					i++;
				} else {
					String datafile = cmd.getArgs().get(0);
					List<Map<String, String>> data = world.getData(datafile);
					if (data == null) {
						i++;
					} else if (data.size() == 0) {
						i++;
					} else {
						i += data.size();
					}
				}
			} else if ("suite.run".equalsIgnoreCase(cmd.getCommandName())) {
				i += flatten(cmd.getMonkeyId());
			}
		}

		return i;
	}

	@Override
	public String toString() {
		return "SuiteFlattener: " + world;
	}
}