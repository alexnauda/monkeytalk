
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

package com.gorillalogic.fonemonkey.web;

import android.webkit.WebView;

public class SelectorFilter extends WebFilterBase {
	public static String[] componentTypes = {"indexedselector", "itemselector"};	
	private static String[] tagNames = {"select"};
	@Override
	protected String[] getTagNames() {
		return tagNames;
	}
	@Override
	public String[] getComponentTypes() {
		return componentTypes;
	}
	
	@Override
	public HtmlElement findHtmlElement(WebView webview, 
			String componentType, String monkeyId) {
		HtmlElement elem = super.findHtmlElement(webview, componentType, monkeyId);
		if (elem != null) {
			return new HtmlSelect(elem);
		}
		return null;
	}

}
