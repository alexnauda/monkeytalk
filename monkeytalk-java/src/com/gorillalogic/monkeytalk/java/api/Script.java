/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.java.api;

import java.util.List;
import java.util.Map;

/**
 * A MonkeyTalk script. The monkeyId is the name of the script. If no extension is specified, then the script runner will first search for a .js file, and if one is not found, the runner will then search for an .mt file.
 */
public interface Script {
	/**
	 * Run the script with the given args.
	 */
	public void run();
	/**
	 * Run the script with the given args.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void run(Map<String, String> mods);
	/**
	 * Run the script with the given args.
	 * @param args the arguments
	 */
	public void run(String... args);
	/**
	 * Run the script with the given args.
	 * @param args the arguments
	 */
	public void run(List<String> args);
	/**
	 * Run the script with the given args.
	 * @param args the arguments
	 * @param mods the MonkeyTalk modifiers
	 */
	public void run(List<String> args, Map<String, String> mods);

	/**
	 * Data-drive the script with the given CSV data file.
	 */
	public void runWith();
	/**
	 * Data-drive the script with the given CSV data file.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void runWith(Map<String, String> mods);
	/**
	 * Data-drive the script with the given CSV data file.
	 * @param args the arguments (where the first arg is the datafile)
	 */
	public void runWith(String... args);
	/**
	 * Data-drive the script with the given CSV data file.
	 * @param args the arguments (where the first arg is the datafile)
	 */
	public void runWith(List<String> args);
	/**
	 * Data-drive the script with the given CSV data file.
	 * @param args the arguments (where the first arg is the datafile)
	 * @param mods the MonkeyTalk modifiers
	 */
	public void runWith(List<String> args, Map<String, String> mods);

	/**
	 * Run the script only if the given verify command (in the args) is true, otherwise do nothing.
	 */
	public void runIf();
	/**
	 * Run the script only if the given verify command (in the args) is true, otherwise do nothing.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void runIf(Map<String, String> mods);
	/**
	 * Run the script only if the given verify command (in the args) is true, otherwise do nothing.
	 * @param args the arguments (a MonkeyTalk verify command)
	 */
	public void runIf(String... args);
	/**
	 * Run the script only if the given verify command (in the args) is true, otherwise do nothing.
	 * @param args the arguments (a MonkeyTalk verify command)
	 */
	public void runIf(List<String> args);
	/**
	 * Run the script only if the given verify command (in the args) is true, otherwise do nothing.
	 * @param args the arguments (a MonkeyTalk verify command)
	 * @param mods the MonkeyTalk modifiers
	 */
	public void runIf(List<String> args, Map<String, String> mods);
}
