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
 * Base class for all MonkeyTalk objects.
 * 
 * @ignoreMeta
 * @ignoreJS
 */
public interface MTObject {
	/**
	 * Get the application under test.
	 * 
	 * @return the application
	 */
	public Application getApp();

	/**
	 * Get the MonkeyTalk componentType.
	 * 
	 * @return the componentType
	 */
	public String getComponentType();

	/**
	 * Get the MonkeyTalk monkeyId.
	 * 
	 * @return the monkeyId
	 */
	public String getMonkeyId();
}