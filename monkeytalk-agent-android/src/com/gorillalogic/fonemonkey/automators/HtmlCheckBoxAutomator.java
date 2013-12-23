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
package com.gorillalogic.fonemonkey.automators;

import com.gorillalogic.fonemonkey.web.HtmlCheckBox;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class HtmlCheckBoxAutomator extends HtmlElementAutomator {
	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ON)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_OFF)) {
			boolean on = action.equalsIgnoreCase(AutomatorConstants.ACTION_ON);
			boolean checked = Boolean.valueOf(getHtmlElement().getAttr("checked"));
			if (checked != on) {
				tap();
			}
			return null;
		}

		return super.play(action, args);
	}

	@Override
	public String getComponentType() {
		return "CheckBox";
	}

	@Override
	public Class<?> getComponentClass() {
		return HtmlCheckBox.class;

	}

	@Override
	public String getValue() {
		boolean checked = Boolean.valueOf(getHtmlElement().getAttr("checked"));
		return checked ? "on" : "off";
	}
}
