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
 * A MonkeyTalk script run after each test script in the suite.
 * 
 * @ignoreJS
 */
public interface Teardown extends MTObject {

	/**
	 * Run the teardown script with the given args.
	 * 
	 * @param args
	 *            the arguments
	 * 
	 * @ignoreJS
	 */
	public void run(String... args);

	/**
	 * Data-drive the teardown script with the given CSV data file.
	 * 
	 * @param args
	 *            the arguments (where the first arg is the datafile)
	 * 
	 * @ignoreJS
	 */
	public void runWith(String... args);
}