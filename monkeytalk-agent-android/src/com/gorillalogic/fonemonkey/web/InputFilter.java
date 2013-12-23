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

public abstract class InputFilter extends WebFilterBase {
	@Override
	public boolean isIncluded(HtmlElement elem, String monkeyId) {
		if (!super.isIncluded(elem, monkeyId)) {
			return false;
		}
		String type = elem.getType().toLowerCase();
		for (String t : types()) {
			if (t.equals(type)) {
				return true;
			}
		}
		return false;
	}

	static public String[] tagNames = new String[] { "input" };

	@Override
	protected String[] getTagNames() {
		return tagNames;
	}

	@Override
	public String getXpathNode() {
		String xpathNode = super.getXpathNode();
		String types = "";

		if (types().length > 0) {
			types = "[";

			for (int i = 0; i < types().length; i++) {
				String type = types()[i];
				types += "@type='" + type + "'";

				if (i + 1 < types().length) {
					types += " or ";
				}
			}

			types += "]";
		}

		return "(" + xpathNode + types + ")";
	}

	protected abstract String[] types();

}
