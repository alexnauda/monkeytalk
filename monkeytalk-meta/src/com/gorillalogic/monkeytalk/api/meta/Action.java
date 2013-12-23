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
package com.gorillalogic.monkeytalk.api.meta;

import java.util.ArrayList;
import java.util.List;

/**
 * The meta API object for an action.
 */
public class Action extends BaseMeta {
	private List<Arg> args;
	private String returnType;
	private String returnDescription;

	/**
	 * Instantiate a new {@code Action} with the given name, description, {@link Arg}s, return type,
	 * and return description.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param args
	 *            the args
	 * @param returnType
	 *            the return type
	 * @param returnDescription
	 *            the return description
	 */
	public Action(String name, String description, List<Arg> args, String returnType,
			String returnDescription) {
		super(name, description);
		this.args = (args != null ? args : new ArrayList<Arg>());
		this.returnType = returnType;
		this.returnDescription = returnDescription;
	}

	/**
	 * Get the list of {@link Arg} objects.
	 * 
	 * @return the list of args
	 */
	public List<Arg> getArgs() {
		return args;
	}

	/**
	 * Get the list of arg names.
	 * 
	 * @return the list of arg names
	 */
	public List<String> getArgNames() {
		List<String> list = new ArrayList<String>();
		for (Arg a : getArgs()) {
			list.add(a.getName());
		}
		return list;
	}

	/**
	 * Get the list of arg names concatenated into a single space-separated string.
	 * 
	 * @return the list of args concatenated into a String
	 */
	public String getArgNamesAsString() {
		StringBuilder sb = new StringBuilder();
		for (String arg : getArgNames()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(arg);
		}
		return sb.toString();
	}

	/**
	 * Get an {@code Arg} object by name. Returns null if not found.
	 * 
	 * @param arg
	 *            the arg name
	 * @return the arg
	 */
	public Arg getArg(String arg) {
		if (arg != null) {
			for (Arg a : getArgs()) {
				if (a.getName().equalsIgnoreCase(arg)) {
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Add the given arg to the list of args.
	 * 
	 * @param arg
	 *            the arg
	 */
	public void addArg(Arg arg) {
		if (arg != null) {
			args.add(arg);
		}
	}

	/**
	 * Get the {@code Action}'s return type.
	 * 
	 * @return the return type
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * Get the {@code Action}'s return description.
	 * 
	 * @return the return description
	 */
	public String getReturnDescription() {
		return returnDescription;
	}

	/**
	 * True if the action has any varArg args in it, otherwise false.
	 * 
	 * @return true if the action has any varArg args, otherwise false.
	 */
	public boolean hasVarArgs() {
		for (Arg a : args) {
			if (a.isVarArgs()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the action's arguments as a method parameter list.
	 * 
	 * @return the parameter list
	 */
	public String toParams() {
		StringBuilder sb = new StringBuilder();
		for (Arg a : args) {
			sb.append(sb.length() > 0 ? ", " : "").append(a.toParam());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean showDescription) {
		StringBuilder sb = new StringBuilder();
		for (Arg arg : args) {
			sb.append(sb.length() > 0 ? ", " : "").append(arg.toString(false));
		}
		return name + "(" + sb.toString() + ")" + (returnType != null ? ":" + returnType : "")
				+ printDescription(showDescription);
	}
}