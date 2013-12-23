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
package com.gorillalogic.fonemonkey.automators.tests;

public class Fred {
	public static String foo(String... args) {
		return "foo " + argsToString(args);
	}
	
	private static String argsToString(String... args) {
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			sb.append(arg).append(",");
		}
		return (sb.length() == 0 ? "" : sb.substring(0, sb.length()-1));
	}
	
	public String bar(String... args) {
		return "bar";
	}
	
	public static int baz(String... args) {
		return 1;
	}
	
	public static String buzz(int arg) {
		return "buzz";
	}
}
