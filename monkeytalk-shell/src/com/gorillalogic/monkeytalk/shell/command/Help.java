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
package com.gorillalogic.monkeytalk.shell.command;

public class Help {
	public static final String HELP;
	private static final StringBuilder sb = new StringBuilder("HELP:\n");

	static {
		sb.append(" /help -- print this help\n");
		sb.append(" /history -- print command history\n");
		sb.append("   !! -- previous command\n");
		sb.append("   !n -- command n\n");
		sb.append("   !string -- most recent command starting with 'string'\n");
		sb.append("   ^string1^string2 -- last command, but replacing 'string1' with 'string2'\n");
		sb.append(" /ping -- ping the agent\n");
		sb.append(" /thinktime N -- set global thinktime to N ms\n");
		sb.append(" /timeout N -- set global timeout to N ms\n");
		sb.append(" /tree -- dump the component tree\n");
		sb.append(" /vars -- print all variables\n");
		sb.append(" /quit -- exit\n");
		HELP = sb.toString();
	}
}
