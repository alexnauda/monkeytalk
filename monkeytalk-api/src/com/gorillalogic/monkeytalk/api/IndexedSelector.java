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
 * A component that provides for selection of an item from a list of items. Item selection is
 * recorded and played back with an index indicating the selected item.
 * 
 * @prop size(int section) - the total number of items in the list (per section on iOS)
 */
public interface IndexedSelector extends View {

	/**
	 * Selects an item by index.
	 * 
	 * @param itemNumber
	 *            the index of the item to select.
	 */
	public void selectIndex(int itemNumber);

	/**
	 * Long press an item by index.
	 * 
	 * @param itemNumber
	 *            the index of the item to long press.
	 */
	public void longSelectIndex(int itemNumber);
}