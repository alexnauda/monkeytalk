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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.webkit.WebView;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.fonemonkey.automators.IAutomator;
import com.gorillalogic.fonemonkey.automators.ViewAutomator;
import com.gorillalogic.fonemonkey.automators.WebViewAutomator;

public class WebAutomationManager {

	private static HashMap<String, List<IWebFilter>> filters = new HashMap<String, List<IWebFilter>>();
	static {
		// registerFilter(new PopupFilter());
		registerFilter(new TextInputFilter());
		registerFilter(new ButtonFilter());
		registerFilter(new SelectorFilter());
		registerFilter(new LinkFilter());
		registerFilter(new TableFilter());
		registerFilter(new RadioButtonsFilter());
		registerFilter(new CheckBoxFilter());
		registerFilter(new TextAreaFilter());
		registerFilter(new LabelFilter());
		registerFilter(new ImageFilter());
		registerFilter(new TagFilter());
	}

	public static void registerFilter(IWebFilter filter) {
		for (String componentType : filter.getComponentTypes()) {
			componentType = componentType.toLowerCase();
			List<IWebFilter> list = filters.get(componentType);
			if (list == null) {
				list = new ArrayList<IWebFilter>();
				filters.put(componentType, list);
			}
			list.add(filter);
		}
	}

	public static HtmlElement findHtmlElement(WebView webview, String componentType,
			String monkeyId, int index) {
		List<IWebFilter> list = filters.get(componentType.toLowerCase());
		if (list == null) {
			return null;
		}

		for (IWebFilter filter : list) {
			if (index > 1)
				monkeyId += "(" + index + ")";
			HtmlElement elem = filter.findHtmlElement(webview, componentType, monkeyId);
			if (elem != null) {
				return elem;
			}
		}
		return null;
	}

	public static HtmlElement findNth(WebView webview, String componentType, int n) {
		List<IWebFilter> list = filters.get(componentType.toLowerCase());
		if (list == null) {
			return null;
		}

		int natives = getNativeCount(componentType, webview);
		if (n < natives) {
			return null;
		}

		int offset = n - natives;

		for (IWebFilter filter : list) {
			HtmlElement elem = filter.findNthHtmlElement(webview, componentType, offset);
			if (elem != null) {
				return elem;
			}
		}

		return null;
	}

	// Count how many native components preceed this webview
	private static int getNativeCount(String componentType, WebView webview) {
		IAutomator auto = AutomationManager.findAutomatorByType(componentType);
		int n = ViewAutomator.findOrdinalFor(webview, auto.getComponentClass());

		return n == -1 ? 0 : n;
	}

	/**
	 * 
	 * Run the JS returning a list of HtmlElements
	 * 
	 * @param webview
	 *            the webview in which to run the JS
	 * @param jsElemsExpr
	 *            a javascript expression that returns a single element, nodelist or array of
	 *            elements
	 * @return a list containing zero or more HtmlElements
	 */
	public static List<HtmlElement> findHtmlElements(WebView webview, String jsElemsExpr) {

		WebViewAutomator auto = (WebViewAutomator) AutomationManager.findAutomator(webview);

		return auto.findHtmlElements(jsElemsExpr);
	}

	public static HtmlElement findElement(WebView webview, String componentType, String monkeyId,
			String jsElemsExpr) {
		WebViewAutomator auto = (WebViewAutomator) AutomationManager.findAutomator(webview);

		return auto.findElement(jsElemsExpr, componentType, monkeyId);
	}

	public static HtmlElement findNthElement(WebView webview, String componentType, int n,
			String jsElemsExpr) {
		WebViewAutomator auto = (WebViewAutomator) AutomationManager.findAutomator(webview);

		return auto.findNthElement(jsElemsExpr, componentType, n);
	}

}
