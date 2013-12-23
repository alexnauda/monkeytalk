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
 * The device hosting the application under test.
 * 
 * @prop value - the device OS
 * @prop os - the device OS (ex: iOS or Android)
 * @prop version - the device OS version (ex: 5.1.1 or 2.3.3)
 * @prop resolution - the device screen resolution in pixels (ex: 640x960)
 * @prop name - the device name (ex: iPhone 4S)
 * @prop orientation - the device orientation (either 'portrait' or 'landscape')
 * @prop battery - the percentage of the battery that is full
 * @prop memory - percentage of memory in use
 * @prop cpu - percentage of the cpu in use
 * @prop diskspace - percentage of the disk in use
 * @prop allinfo - memory, cpu, diskpace, and battery percentages returned comma separated
 * @prop totalMemory - total ram in the phone, in bytes
 * @prop totalDiskSpace - total space on disk, in bytes
 */
public interface Device extends MTObject {

	/**
	 * Shake the device. iOS: works great. Android: not yet implemented.
	 */
	public void shake();

	/**
	 * Change the device orientation.
	 * 
	 * @param direction
	 *            iOS: 'left' or 'right', Android: 'portrait' or 'landscape'
	 */
	public void rotate(String direction);

	/**
	 * Navigate back. iOS: Pops the current UINavigationItem (if there is one). Android: Presses the
	 * hardware device key.
	 */
	public void back();

	/**
	 * Navigate forward. iOS: Pushes the next UINavigationItem, if there is one. Android: ignored.
	 */
	public void forward();

	/**
	 * Press the search key. iOS: ignored. Android: Presses the device search key.
	 */
	public void search();

	/**
	 * Press the menu key. iOS: ignored. Android: Presses the device menu key.
	 */
	public void menu();

	/**
	 * Take a screenshot of the app under test.
	 */
	public void screenshot();

	/**
	 * Gets the value of the given property from the component, and set it into the given variable
	 * name.
	 * 
	 * @param variable
	 *            the name of the variable to set
	 * @param propPath
	 *            the property name or path expression (defaults to 'value')
	 * @return the value
	 */
	public String get(String variable, String propPath);
}