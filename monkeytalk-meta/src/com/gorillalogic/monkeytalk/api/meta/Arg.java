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
 * The meta API object for an arg.
 */
public class Arg extends BaseMeta {
	private String type;
	private boolean varArgs;
	private String defaultValue;

	/**
	 * Instantiate a new {@code Arg} with the given name, description, and variable type. Sets the
	 * default value to null.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param type
	 *            the arg variable type
	 */
	public Arg(String name, String description, String type) {
		this(name, description, type, false, null);
	}

	/**
	 * Instantiate a new {@code Arg} with the given name, description, and variable type. Sets the
	 * default value to null.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param type
	 *            the arg variable type
	 * @param varArgs
	 *            true if the arg is a varArgs, otherwise false
	 */
	public Arg(String name, String description, String type, boolean varArgs) {
		this(name, description, type, varArgs, null);
	}

	/**
	 * Instantiate a new {@code Arg} with the given name, description, variable type, and default
	 * value.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param type
	 *            the arg variable type
	 * @param defaultValue
	 *            the default value
	 */
	public Arg(String name, String description, String type, String defaultValue) {
		this(name, description, type, false, defaultValue);
	}

	/**
	 * Instantiate a new {@code Arg} with the given name, description, variable type, and default
	 * value.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 * @param type
	 *            the arg variable type
	 * @param varArgs
	 *            true if the arg is a varArgs, otherwise false
	 * @param defaultValue
	 *            the default value
	 */
	public Arg(String name, String description, String type, boolean varArgs, String defaultValue) {
		super(name, description);
		this.type = type;
		this.varArgs = varArgs;
		this.defaultValue = defaultValue;
	}

	/**
	 * Get the arg variable type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get if the arg is a varArg or not.
	 * 
	 * @return true if the arg is a varArg, otherwise false.
	 */
	public boolean isVarArgs() {
		return varArgs;
	}

	/**
	 * Get the default value of the arg.
	 * 
	 * @return the default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Get the arg as a method param.
	 * 
	 * @return the arg as a param.
	 */
	public String toParam() {
		return type + (varArgs ? "... " : " ") + name;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean showDescription) {
		return name + ":" + type + (isVarArgs() ? "[]" : "")
				+ (defaultValue != null ? "=" + defaultValue : "")
				+ printDescription(showDescription);
	}
}