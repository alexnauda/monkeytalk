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
 * The application under test.
 */
public interface App extends MTObject {

	/**
	 * Execute a method on a native class. The method must take zero or more String arguments. The
	 * class is given in the monkeyId by its fully qualified class name.
	 * 
	 * @param method
	 *            the method to call
	 * @param args
	 *            the String args to be supplied to the method
	 */
	public void exec(String method, String... args);

	/**
	 * Execute a method on a native class. The method must take zero or more String arguments and
	 * return a String result. The returned value is set into the given variable name. The class is
	 * given in the monkeyId by its fully qualified class name.
	 * 
	 * @param variable
	 *            the name of the variable to set
	 * @param method
	 *            the method to call
	 * @param args
	 *            the String args to be supplied to the method
	 * @return the return value
	 */
	public String execAndReturn(String variable, String method, String... args);
}