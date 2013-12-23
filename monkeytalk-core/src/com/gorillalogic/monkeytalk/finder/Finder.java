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
package com.gorillalogic.monkeytalk.finder;

import java.util.ArrayList;
import java.util.List;

import com.gorillalogic.monkeytalk.Command;

/**
 * Helper class to find a specific command, or set of commands, given a matching criteria. Used by
 * the {@code ScriptProcessor} and {@code SuiteProcessor}.
 */
public class Finder {

	private Finder() {
	}

	/**
	 * Helper to find the first command in the given list of commands with the matching command
	 * name. If not found, return {@code null}.
	 * 
	 * @param commands
	 *            the list of MonkeyTalk commands to search
	 * @param name
	 *            the MonkeyTalk command name
	 * @return the first matching MonkeyTalk command, or null if no match is found
	 */
	public static Command findCommandByName(List<Command> commands, String name) {
		if (commands != null && name != null) {
			for (Command cmd : commands) {
				if (!cmd.isIgnored() && name.equalsIgnoreCase(cmd.getCommandName())) {
					return cmd;
				}
			}
		}
		return null;
	}

	/**
	 * Helper to find all commands in the given list of commands with the matching command name.
	 * 
	 * @param commands
	 *            the list of MonkeyTalk commands to search
	 * @param name
	 *            the MonkeyTalk command name
	 * @return the list of matching commands (could be empty, but never {@code null})
	 */
	public static List<Command> findCommandsByName(List<Command> commands, String name) {
		List<Command> found = new ArrayList<Command>();
		if (commands != null && name != null) {
			for (Command cmd : commands) {
				if (!cmd.isIgnored() && name.equalsIgnoreCase(cmd.getCommandName())) {
					found.add(cmd);
				}
			}
		}
		return found;
	}

	/**
	 * Helper to find the first non-ignored command in the given list of commands with the matching
	 * MonkeyTalk {@code componentType}. If not found, return {@code null}.
	 * 
	 * @param commands
	 *            the list of MonkeyTalk commands to search
	 * @param componentType
	 *            the MonkeyTalk componentType
	 * @return the first matching MonkeyTalk command, or null if no match is found
	 */
	public static Command findCommandByComponentType(List<Command> commands, String componentType) {
		if (commands != null && componentType != null) {
			for (Command cmd : commands) {
				if (!cmd.isIgnored() && componentType.equalsIgnoreCase(cmd.getComponentType())) {
					return cmd;
				}
			}
		}

		return null;
	}

	/**
	 * Helper to find all commands in the given list of commands with the matching MonkeyTalk
	 * {@code componentType}.
	 * 
	 * @param commands
	 *            the list of MonkeyTalk commands to search
	 * @param componentType
	 *            the MonkeyTalk componentType
	 * @return the list of matching commands (could be empty, but never {@code null})
	 */
	public static List<Command> findCommandsByComponentType(List<Command> commands,
			String componentType) {
		List<Command> found = new ArrayList<Command>();
		if (commands != null && componentType != null) {
			for (Command cmd : commands) {
				if (!cmd.isIgnored() && componentType.equalsIgnoreCase(cmd.getComponentType())) {
					found.add(cmd);
				}
			}
		}
		return found;
	}
}