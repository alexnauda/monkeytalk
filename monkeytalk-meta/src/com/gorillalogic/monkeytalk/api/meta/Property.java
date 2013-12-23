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

/**
 * The meta API object for a property.
 */
public class Property extends BaseMeta {
	private String args;

	/**
	 * Instantiate a new {@code Property} with the given name, args, and description.
	 * 
	 * @param name
	 *            the name
	 * @param args
	 *            the args for a multi-valued property
	 * @param description
	 *            the description
	 */
	public Property(String name, String args, String description) {
		super(name, description);
	}

	/**
	 * Get the property args. NOTE: properties only need args if the are multi-valued.
	 * 
	 * @return the args
	 */
	public String getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean showDescription) {
		return name + (args != null && args.length() > 0 ? "(" + args + ")" : "")
				+ printDescription(showDescription);
	}
}