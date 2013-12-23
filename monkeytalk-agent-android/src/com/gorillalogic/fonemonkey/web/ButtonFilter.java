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


public class ButtonFilter extends InputFilter {

	public static String[] componentTypes = {"input", "button"};
	
	@Override
	protected String[] getTagNames() {
		return new String[] { "button", "input" };
	}
	
	@Override
	public boolean isIncluded(HtmlElement elem, String monkeyId) {
		if (elem.getType() == null) {
			return defaultIsIncluded(elem, monkeyId);
		}
		String type = elem.getType().toLowerCase();
		if (type.equals("button")) {
			return defaultIsIncluded(elem, monkeyId);
		}

		return super.isIncluded(elem, monkeyId);
	}

	static private String[] types = new String[] { "button", "submit", "reset", "image" };

	@Override
	protected String[] types() {
		return types;
	}

	@Override
	public String[] getComponentTypes() {
		return componentTypes;
	}

}
