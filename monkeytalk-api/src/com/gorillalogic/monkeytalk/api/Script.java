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
package com.gorillalogic.monkeytalk.api;

/**
 * A MonkeyTalk script. The monkeyId is the name of the script. If no extension is specified, then
 * the script runner will first search for a .js file, and if one is not found, the runner will then
 * search for an .mt file.
 * 
 * @ignoreJS
 */
public interface Script extends MTObject {

	/**
	 * Run the script with the given args.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @ignoreJS
	 */
	public void run(String... args);

	/**
	 * Data-drive the script with the given CSV data file.
	 * 
	 * @param args
	 *            the arguments (where the first arg is the datafile)
	 * 
	 * @ignoreJS
	 */
	public void runWith(String... args);

	/**
	 * Run the script only if the given verify command (in the args) is true, otherwise do nothing.
	 * 
	 * @param args
	 *            the arguments (a MonkeyTalk verify command)
	 * 
	 * @ignoreJS
	 */
	public void runIf(String... args);
}