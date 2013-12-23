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

import android.view.View;

import com.gorillalogic.fonemonkey.web.HtmlElement;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class HtmlElementAutomator extends AutomatorBase {

	@Override
	public boolean isHtmlAutomator() {
		return true;
	}

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_TAP)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_CLICK)) {
			tap();
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ENTER_TEXT)) {
			clear();
			String text = args.length > 0 ? args[0] : "";
			text = text.replaceAll("\\\\n", "\n");
			enterText(text);
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_CLEAR)) {
			clear();
			return null;
		}

		// if (action.equalsIgnoreCase(AutomatorConstants.ACTION_GET)) {
		// return args.length == 0 ? getValue() : getHtmlElement().getAttr(
		// args[0]);
		//
		// }

		return super.play(action, args);
	}

	@Override
	public String getValue(String propertyPath) {
		String value = getHtmlElement().getAttr(propertyPath);

		if (value != null) {
			return value;
		}
		throw new RuntimeException("Unable to find property \"" + propertyPath + "\" for "
				+ getComponentType());
	}

	@Override
	public String getValue() {
		return getHtmlElement().getAttr("textContent");
	}

	public HtmlElement getHtmlElement() {
		return (HtmlElement) getComponent();
	}

	public void tap() {
		final HtmlElement elem = getHtmlElement();

		AutomationManager.runOnUIThread(new Runnable() {

			public void run() {
				float zoom = elem.getWebView().getScale();
				int x = (int) (zoom * elem.getX()) + elem.getWebView().getScrollX();
				int y = (int) (zoom * elem.getY()) + elem.getWebView().getScrollY();
				// int x = (int) (zoom * (elem.getX() + (elem.getWidth() / 2)));
				// int y = (int) (zoom * (elem.getY() + (elem.getHeight() / 2)));
				getWebViewAutomator().tap(x, y);
			}
		});
	}

	public void enterText(String s) {
		WebViewAutomator auto = (WebViewAutomator) AutomationManager.findAutomator(getHtmlElement()
				.getWebView());
		auto.enterText(s, getHtmlElement());
	}

	// public void enterText(String s) {
	// tap();
	// pause(750);
	// getWebViewAutomator().enterText(s);
	//
	// }
	//
	// private void pause(long millis) {
	// try {
	// Thread.sleep(millis);
	// } catch (InterruptedException e) {
	// Log.log("Sleep interrupted", e);
	// }
	// }

	protected WebViewAutomator getWebViewAutomator() {
		return (WebViewAutomator) AutomationManager.findAutomator(getHtmlElement().getWebView());
	}

	@Override
	public String getComponentType() {
		return "HtmlTag";
	}

	@Override
	public Class<?> getComponentClass() {
		return HtmlElement.class;
	}

	@Override
	public boolean hides(View view) {
		return true;
	}

	public void clear() {
		runJavaScript("function(elem) {elem.value=''}");
	}

	/**
	 * Runs the supplied JavaScript function with this Html element as an arg
	 */
	public String runJavaScript(String func) {
		return this.getWebViewAutomator().runJavaScript(
				"return window.monkeytalk.call(" + this.getHtmlElement().asJson() + ",(" + func
						+ "))");
	}

	/**
	 * Run the supplied JavaScript function that returns an html element or a list of htmlelements,
	 * passing this HtmlElement to the function as an argument
	 * 
	 * @param a
	 *            javascript function definition
	 * @return List of HtmlElement representations of the elements (array or nodelist) returned by
	 *         the function call. If the function returns a single (non-list) element, that element
	 *         will be returned wrapped in a one-element list.
	 */
	public List<HtmlElement> findHtmlElements(String func) {
		String encodedResult = runJavaScript("function(elem) {elems = (" + func
				+ ")(elem); return window.monkeytalk.encodeElements(elems)}");
		return getWebViewAutomator().decodeJsResult(encodedResult);
	}

	public HtmlElement findHtmlCell(HtmlElement table, String cellId) {
		return getWebViewAutomator().findCell(table, cellId);
	}
}
