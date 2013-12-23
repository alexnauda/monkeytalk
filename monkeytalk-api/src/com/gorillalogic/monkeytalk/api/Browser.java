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
 * The browser hosting the webapp under test.
 * 
 * @prop value - the current url
 */
public interface Browser extends MTObject {

	/**
	 * Open the given url.
	 * 
	 * @param url
	 *            the url to be opened
	 */
	public void open(String url);

	/**
	 * Navigate the browser back to the previous page. Ignored if this is the first page.
	 */
	public void back();

	/**
	 * Navigate the browser forward to the next page. Ignored if this is the last page.
	 */
	public void forward();
}