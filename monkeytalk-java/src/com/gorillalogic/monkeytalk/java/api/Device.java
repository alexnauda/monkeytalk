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
package com.gorillalogic.monkeytalk.java.api;

import java.util.Map;

/**
 * The device hosting the application under test.
 */
public interface Device {
	/**
	 * Shake the device. iOS: works great. Android: not yet implemented.
	 */
	public void shake();
	/**
	 * Shake the device. iOS: works great. Android: not yet implemented.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void shake(Map<String, String> mods);

	/**
	 * Change the device orientation.
	 */
	public void rotate();
	/**
	 * Change the device orientation.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void rotate(Map<String, String> mods);
	/**
	 * Change the device orientation.
	 * @param direction iOS: 'left' or 'right', Android: 'portrait' or 'landscape'
	 */
	public void rotate(String direction);
	/**
	 * Change the device orientation.
	 * @param direction iOS: 'left' or 'right', Android: 'portrait' or 'landscape'
	 * @param mods the MonkeyTalk modifiers
	 */
	public void rotate(String direction, Map<String, String> mods);

	/**
	 * Navigate back. iOS: Pops the current UINavigationItem (if there is one). Android: Presses the hardware device key.
	 */
	public void back();
	/**
	 * Navigate back. iOS: Pops the current UINavigationItem (if there is one). Android: Presses the hardware device key.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void back(Map<String, String> mods);

	/**
	 * Navigate forward. iOS: Pushes the next UINavigationItem, if there is one. Android: ignored.
	 */
	public void forward();
	/**
	 * Navigate forward. iOS: Pushes the next UINavigationItem, if there is one. Android: ignored.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void forward(Map<String, String> mods);

	/**
	 * Press the search key. iOS: ignored. Android: Presses the device search key.
	 */
	public void search();
	/**
	 * Press the search key. iOS: ignored. Android: Presses the device search key.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void search(Map<String, String> mods);

	/**
	 * Press the menu key. iOS: ignored. Android: Presses the device menu key.
	 */
	public void menu();
	/**
	 * Press the menu key. iOS: ignored. Android: Presses the device menu key.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void menu(Map<String, String> mods);

	/**
	 * Take a screenshot of the app under test.
	 */
	public void screenshot();
	/**
	 * Take a screenshot of the app under test.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void screenshot(Map<String, String> mods);

	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @return the value
	 */
	public String get();
	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @param mods the MonkeyTalk modifiers
	 * @return the value
	 */
	public String get(Map<String, String> mods);
	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @param propPath the property name or path expression (defaults to 'value')
	 * @return the value
	 */
	public String get(String propPath);
	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @param propPath the property name or path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 * @return the value
	 */
	public String get(String propPath, Map<String, String> mods);
}
