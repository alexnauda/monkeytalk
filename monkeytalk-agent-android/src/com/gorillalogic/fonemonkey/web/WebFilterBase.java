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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.webkit.WebView;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.fonemonkey.automators.WebViewAutomator;

public abstract class WebFilterBase implements IWebFilter {
	public static String[] webProperties = { "@id", "@name", "@value", "text()", "@title",
			"@class", "@alt", "@src", "@href" };
	private static Pattern ordPattern = Pattern.compile("#\\d+");

	@Override
	public HtmlElement findHtmlElement(WebView webview, String componentType, String monkeyId) {
		Pattern monkeyIndexPattern = Pattern.compile("\\([0-9]+(?:\\.[0-9]*)?\\)$");
		Matcher monkeyIndexMatcher = monkeyIndexPattern.matcher(monkeyId);
		Pattern xpathPattern = Pattern.compile("^xpath=");
		WebViewAutomator auto = (WebViewAutomator) AutomationManager.findAutomator(webview);
		String xpath = xpathPattern.matcher(monkeyId).find() ? monkeyId.replace("xpath=", "")
				: getXpathExpression(monkeyId);
		HtmlElement elem = null;
		boolean isOrdinalMonkeyId = ordPattern.matcher(monkeyId).matches()
				|| monkeyId.equalsIgnoreCase("*");
		boolean isIndexedMonkeyId = monkeyIndexMatcher.find();

		if (isOrdinalMonkeyId || isIndexedMonkeyId) {
			int n = 1;
			if (!monkeyId.equalsIgnoreCase("*") && !isIndexedMonkeyId) {
				n = Integer.valueOf(monkeyId.substring(1));
			} else if (isIndexedMonkeyId) {
				String monkeyIndex = monkeyIndexMatcher.group();
				String baseMonkeyId = monkeyId.replace(monkeyIndex, "");
				xpath = xpath.replace(monkeyId, baseMonkeyId);
				monkeyId = baseMonkeyId;
				monkeyIndex = monkeyIndex.replace("(", "");
				monkeyIndex = monkeyIndex.replace(")", "");
				n = Integer.parseInt(monkeyIndex);
			}
			elem = auto.findElementByXpath(n, xpath);
		} else {
			elem = auto.findElementByXpath(xpath);
		}

		return elem;
	}

	@Override
	public HtmlElement findNthHtmlElement(WebView webview, String componentType, int n) {
		String xpath = this.getXpathNode();
		WebViewAutomator auto = (WebViewAutomator) AutomationManager.findAutomator(webview);
		HtmlElement elem = auto.findElementByXpath(n, xpath);
		return elem;
	}

	public List<HtmlElement> findAll(WebView webview) {

		List<HtmlElement> results = new ArrayList<HtmlElement>();
		for (String tagName : getTagNames()) {

			List<HtmlElement> elems = WebAutomationManager.findHtmlElements(webview,
					"document.getElementsByTagName(\'" + tagName + "')");
			results.addAll(elems);
		}
		return results;

	}

	@Override
	public String getXpathExpression(String monkeyId) {
		String node = this.getXpathNode();
		String predicate;
		String xpath;

		if (ordPattern.matcher(monkeyId).matches() || monkeyId.equalsIgnoreCase("*")) {
			predicate = "";
		} else {
			predicate = this.getXpathPredicate(monkeyId);
		}

		xpath = node + predicate;

		return xpath;
	}

	@Override
	public String getXpathNode() {
		String node = "";
		boolean isCompoundNode = getTagNames().length > 1;

		if (isCompoundNode) {
			node += "(";
		}

		for (int i = 0; i < getTagNames().length; i++) {
			String tag = getTagNames()[i];
			node += "//" + tag;

			if (i + 1 < getTagNames().length) {
				node += "|";
			}
		}

		if (isCompoundNode) {
			node += ")";
		}
		return node;
	}

	@Override
	public String getXpathPredicate(String monkeyId) {
		String predicate = "[";

		for (int i = 0; i < webProperties.length; i++) {
			String property = webProperties[i];
			predicate += property + "='" + monkeyId + "'";

			if (i + 1 < webProperties.length) {
				predicate += " or ";
			}
		}

		predicate += "]";
		return predicate;
	}

	public boolean isIncluded(HtmlElement elem, String monkeyId) {
		boolean b = defaultIsIncluded(elem, monkeyId);
		return b;
	}

	protected boolean defaultIsIncluded(HtmlElement elem, String monkeyId) {
		return elem.matches(monkeyId);
	}

	protected abstract String[] getTagNames();
}
