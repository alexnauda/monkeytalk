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
 * A component that provides for selecting a text item from a list of items. iOS: UIPickerView.
 * Android: UISpinner. Web: Select tag.
 * 
 * @prop item(int row, int section) - (array) the text of the item per row (and per section on iOS)
 */
public interface ItemSelector extends IndexedSelector {

	/**
	 * Select an item by value.
	 * 
	 * @param value
	 *            the value of the item to select.
	 */
	public void select(String value);

	/**
	 * Long select an item by value.
	 * 
	 * @param value
	 *            the value of the item to select.
	 */
	public void longSelect(String value);

}