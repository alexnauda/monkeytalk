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

import com.gorillalogic.fonemonkey.web.HtmlElement;
import com.gorillalogic.fonemonkey.web.HtmlTable;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class HtmlTableAutomator extends HtmlElementAutomator {

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT_INDEX, args, 1);
			int row = getIndexArg(AutomatorConstants.ACTION_SELECT_INDEX, args[0]);
			int col;
			if (args.length < 2) {
				col = 1;
			} else {
				col = getIndexArg(AutomatorConstants.ACTION_SELECT_INDEX, args[1]);
			}
			HtmlElement cell = getCell(row, col);
			HtmlElementAutomator auto = (HtmlElementAutomator) AutomationManager
					.findAutomator(cell);
			auto.tap();
			return null;

		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT, args, 1);
			String value = args[0];

			HtmlElement cell = getCell(value);
			HtmlElementAutomator auto = (HtmlElementAutomator) AutomationManager
					.findAutomator(cell);
			auto.tap();
			return null;

		}

		return super.play(action, args);
	}

	private HtmlElement getCell(String text) {
		String xpath = getHtmlElement().getAttr("xpath");
		xpath += "//*[text()='" + text + "']";
		HtmlElement element = getWebViewAutomator().findElementByXpath(xpath);

		return element;
	}

	private HtmlElement getCell(int row, int col) {
		String tableXpath = getHtmlElement().getAttr("xpath");
		String xpath = tableXpath;
		String cell = "/tr[" + row + "]";

		if (col > 0) {
			cell += "/td[" + col + "]";
		}

		xpath += cell;
		xpath += "|" + tableXpath + "/tbody" + cell;

		HtmlElement element = getWebViewAutomator().findElementByXpath(xpath);

		if (element == null) {
			throw new IllegalArgumentException("Unable to find item in \"" + getComponentType()
					+ "\"");
		}

		return element;
	}

	@Override
	public String getComponentType() {
		return "Table";
	}

	@Override
	public Class<?> getComponentClass() {
		return HtmlTable.class;
	}

	@Override
	protected String getProperty(String propertyPath) {
		if (propertyPath.equals("size")) {
			return runJavaScript("function(table) {var rows = table.rows.length; if(table.tHead != undefined)rows--; return rows}");
		}
		return super.getProperty(propertyPath);
	}

	@Override
	protected String getArrayItem(String name, List<Integer> indices) {
		if (name.equals("item")) {
			int row = indices.get(0);
			int col = (indices.size() > 1) ? indices.get(1) : -1;
			HtmlElement cell = getCell(row + 1, col + 1);
			return cell.getTextContent();

		}
		return super.getArrayItem(name, indices);
	}
}
