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

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.web.HtmlElement;
import com.gorillalogic.fonemonkey.web.HtmlRadioButton;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class HtmlRadioButtonAutomator extends HtmlElementAutomator {

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT_INDEX, args, 1);
			int index = getIndexArg(AutomatorConstants.ACTION_SELECT_INDEX, args[0]);

			String radiogroup = getHtmlRadioButton().getName();
			List<HtmlElement> elems = getRadioGroup();
			if (elems.size() == 0) {
				throw new IllegalArgumentException("Unable to find radiobuttons: " + radiogroup);
			}
			if (elems.size() < index) {
				throw new IllegalArgumentException("Unable to select " + index
						+ ". RadioButtonGroup " + radiogroup + " has only " + elems.size()
						+ " buttons.");
			}

			HtmlElement radio = elems.get(index - 1);
			tap(radio);
			return null;

		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT, args, 1);
			String value = args[0];

			HtmlElement element = getRadioButton(value);

			if (element != null) {
				tap(element);
				return null;
			}

			// List<HtmlElement> elems = getRadioGroup();
			// for (HtmlElement elem : elems) {
			// if (elem.matches(value)) {
			// tap(elem);
			// return null;
			// }
			// }

			throw new IllegalArgumentException("Unable to find radio button with value: " + value);
		}

		return super.play(action, args);
	}

	private void tap(HtmlElement radio) {
		HtmlElementAutomator auto = (HtmlElementAutomator) AutomationManager
				.findAutomator(new HtmlRadioButton(radio));
		auto.tap();
	}

	private List<HtmlElement> getRadioGroup() {
		return findHtmlElements("function(radiobutton) {return document.getElementsByName(radiobutton.name)}");
	}

	private HtmlElement getRadioButton(String value) {
		WebViewAutomator auto = getWebViewAutomator();

		String jsElemsExpr = "document.getElementsByName(\'" + getHtmlElement().getName() + "')";
		if (auto.getWebView().getProgress() < 100)
			throw new IllegalArgumentException("Unable to find radio button with value: " + value);

		String json = auto.runJavaScript("return MonkeyTalk.getRadioButton(" + jsElemsExpr + ",'"
				+ value + "');");

		if (json.equalsIgnoreCase("null")) {
			Log.log("not found");
			return null;
		}

		return auto.decodeJsonElement(json);
	}

	@Override
	public String getComponentType() {
		return "RadioButtons";
	}

	@Override
	public String getValue() {
		return getHtmlElement().getAttr("selected");
	}

	@Override
	public Class<?> getComponentClass() {
		return HtmlRadioButton.class;
	}

	public HtmlRadioButton getHtmlRadioButton() {
		return (HtmlRadioButton) getComponent();
	}
}
