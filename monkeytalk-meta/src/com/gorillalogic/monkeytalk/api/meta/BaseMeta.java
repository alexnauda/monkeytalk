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
 * Base class for the meta API objects.
 */
public class BaseMeta implements Comparable<BaseMeta> {
	protected String name;
	protected String description;

	/**
	 * Instantiate a new {@code BaseMeta} object given the name and description.
	 * 
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 */
	public BaseMeta(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Get the name of the meta API object (aka the name of the {@link Component}, the name of the
	 * {@link Action}, the name of the {@link Property}, or the name of the {@link Arg}).
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the meta object description.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Helper to get the description as a string. Returns an empty string if description is null.
	 * 
	 * @return the description as a string
	 */
	protected String printDescription() {
		return printDescription(true);
	}

	/**
	 * Helper to get the description as a string. If show is false, return the empty string,
	 * otherwise return an empty string if the description is null.
	 * 
	 * @param show
	 *            if false, return the empty string
	 * @return the description as a string
	 */
	protected String printDescription(boolean show) {
		return (show && description != null ? " - " + description : "");
	}

	@Override
	public String toString() {
		return name + printDescription();
	}

	@Override
	public int compareTo(BaseMeta that) {
		if (this == that)
			return 0;

		return getName().compareToIgnoreCase(that.getName());
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (getName() == null ? 0 : getName().toLowerCase().hashCode());
		result = 31 * result
				+ (getDescription() == null ? 0 : getDescription().toLowerCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof BaseMeta))
			return false;

		BaseMeta that = (BaseMeta) obj;

		boolean checkName = (getName() == null ? that.getName() == null : getName()
				.equalsIgnoreCase(that.getName()));

		boolean checkDescription = (getDescription() == null ? that.getDescription() == null
				: getDescription().equalsIgnoreCase(that.getDescription()));

		return checkName && checkDescription;
	}
}