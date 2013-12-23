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

import java.util.List;

import com.gorillalogic.fonemonkey.web.HtmlSelect;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class HtmlSelectAutomator extends HtmlElementAutomator {

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT_INDEX, args, 1);
			int i = getIndexArg(AutomatorConstants.ACTION_SELECT_INDEX, args[0]);
			runJavaScript("function(select) { select.selectedIndex=" + (i - 1) + " }");
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT, args, 1);
			runJavaScript("function(select) {window.monkeytalk.selectItem(select, '" + args[0]
					+ "')}");
			return null;
		}

		return super.play(action, args);
	}

	@Override
	public String getComponentType() {
		return "ItemSelector";
	}

	@Override
	public Class<?> getComponentClass() {
		return HtmlSelect.class;
	}

	@Override
	public String getValue() {
		return runJavaScript("function(select) {return select.options[select.selectedIndex].textContent}");
	}

	@Override
	public String getProperty(String propertyPath) {
		if (propertyPath.equals("size")) {
			return runJavaScript("function(select) {return select.options.length}");
		}
		return super.getProperty(propertyPath);

	}

	@Override
	public String getArrayItem(String name, List<Integer> indices) {
		if (name.equals("items")) {
			int index = indices.get(0) - 1;
			return runJavaScript("function(select) {return select.options[" + index
					+ "].textContent}");
		}
		return super.getArrayItem(name, indices);
	}
}
