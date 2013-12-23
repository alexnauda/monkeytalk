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
 * The system running the tests (aka the runner).
 */
public interface System extends MTObject {

	/**
	 * Execute the given command on the system. The system is the computer running the tests, not to
	 * be confused with the app under test that runs in the simulator/emulator or on the device.
	 * 
	 * @param command
	 *            the system command to execute
	 */
	public void exec(String command);

	/**
	 * Execute the given command on the system. The output from the command is set into the given
	 * variable name.
	 * 
	 * @param variable
	 *            the name of the variable to set
	 * @param command
	 *            the system command to execute the method to call
	 * @return the result of running the system command
	 */
	public String execAndReturn(String variable, String command);
}