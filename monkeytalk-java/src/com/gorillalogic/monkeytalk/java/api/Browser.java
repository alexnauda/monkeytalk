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
 * The browser hosting the webapp under test.
 */
public interface Browser {
	/**
	 * Open the given url.
	 */
	public void open();
	/**
	 * Open the given url.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void open(Map<String, String> mods);
	/**
	 * Open the given url.
	 * @param url the url to be opened
	 */
	public void open(String url);
	/**
	 * Open the given url.
	 * @param url the url to be opened
	 * @param mods the MonkeyTalk modifiers
	 */
	public void open(String url, Map<String, String> mods);

	/**
	 * Navigate the browser back to the previous page. Ignored if this is the first page.
	 */
	public void back();
	/**
	 * Navigate the browser back to the previous page. Ignored if this is the first page.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void back(Map<String, String> mods);

	/**
	 * Navigate the browser forward to the next page. Ignored if this is the last page.
	 */
	public void forward();
	/**
	 * Navigate the browser forward to the next page. Ignored if this is the last page.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void forward(Map<String, String> mods);
}
