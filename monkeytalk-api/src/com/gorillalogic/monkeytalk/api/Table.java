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
 * A component that provides a tabular view of data. iOS: UITableView. Android: Table. Web: Table tag. For web tables, the section specifies the column.
 * 
 * @prop detail(int row, int section) - the detail text of the item per row (and per section on iOS)
 */
public interface Table extends ItemSelector {

	/**
	 * Select a row.
	 * 
	 * @param row
	 *            the row to select
	 * @param section
	 *            the section containing the row, defaults to section #1. (Ignored on Android)
	 */
	public void selectRow(int row, int section);

	/**
	 * Select the indicator (the icon on the right). Android: Ignored.
	 * 
	 * @param row
	 *            the row to select
	 * @param section
	 *            the section containing the row, defaults to section #1.
	 */
	public void selectIndicator(int row, int section);

	/**
	 * Scroll to a row by row number.
	 * 
	 * @param row
	 *            the row to scroll to
	 * @param section
	 *            the section containing the row, defaults to section #1. (Ignored on Android)
	 */
	public void scrollToRow(int row, int section);

	/**
	 * Scroll to a row by value.
	 * 
	 * @param value
	 *            the value of the row to scroll to.
	 */
	public void scrollToRow(String value);

	/**
	 * Enable/disable table editing. iOS: Enabled editing mode for table. Android: ignored.
	 * 
	 * @param enabled
	 *            if true, enable editing, else disable editing.
	 */
	public void setEditing(boolean enabled);

	/**
	 * Insert a row into the table. iOS: Inserts a row. Android: Ignored.
	 * 
	 * @param row
	 *            the index of the row after which to insert a new row.
	 * @param section
	 *            the section containing the row, defaults to section #1.
	 */
	public void insert(int row, int section);

	/**
	 * Remove a row from the table. iOS: Deletes the row. Android: Ignored.
	 * 
	 * @param row
	 *            the index of the row to be removed.
	 * @param section
	 *            the section containing the row, defaults to section #1.
	 */
	public void remove(int row, int section);

	/**
	 * Move a row. iOS: Moves a row. Android: Ignored.
	 * 
	 * @param from
	 *            the index of the row to be moved.
	 * @param to
	 *            the destination row for the move.
	 */
	public void move(int from, int to);
}