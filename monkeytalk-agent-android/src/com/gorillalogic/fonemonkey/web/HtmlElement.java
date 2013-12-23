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

import java.util.HashMap;

import org.json.JSONObject;

import android.webkit.WebView;

/**
 * Makes an HTML element look like a View for automation purposes.
 * 
 * @author sstern
 * 
 */
public class HtmlElement {
	private static String[] fields = { "monkeyId", "tagName", "id", "name", "className", "value",
			"textContent", "type", "x", "y", "width", "height", "title" };

	private WebView webview;

	public HtmlElement(HtmlElement elem) {
		this.webview = elem.webview;
		this.attrs = elem.attrs;
	}

	public HtmlElement(WebView webview, String monkeyId) {
		this.webview = webview;
		attrs.put("monkeyId", monkeyId);
	}

	public String[] getFields() {
		return fields;
	}

	public HtmlElement(WebView webview) {
		this(webview, null);
	}

	public String getTagName() {
		return getAttr("tagName");
	}

	public String getId() {
		return getAttr("id");
	}

	public String getName() {
		return getAttr("name");
	}

	public String getClassName() {
		return getAttr("className");
	}

	public String getValue() {
		return getAttr("value");
	}

	public String getTextContent() {
		return getAttr("textContent");
	}

	public int getX() {
		return Integer.valueOf(getAttr("x"));
	}

	public int getY() {
		return Integer.valueOf(getAttr("y"));
	}

	public int getWidth() {
		return Integer.valueOf(getAttr("width"));
	}

	public int getHeight() {
		return Integer.valueOf(getAttr("height"));
	}

	public String getType() {
		return getAttr("type");
	}

	public String getTitle() {
		return getAttr("title");
	}

	public boolean isShown() {
		return webview.isShown();
	}

	public String asJson() {
		return new JSONObject(attrs).toString();
	}

	protected HashMap<String, String> attrs = new HashMap<String, String>();

	public void putAttr(String field, String value) {
		attrs.put(field, value);

	}

	public String getMonkeyId() {
		return getAttr("monkeyId");
	}

	public String getAttr(String field) {
		return attrs.get(field);
	}

	/**
	 * 
	 * 
	 * @return true if this element has an identifying attribute with the supplied value
	 */
	public boolean matches(String monkeyId) {

		return monkeyId.equals("*") || monkeyId.equals(getId()) || monkeyId.equals(getName())
				|| monkeyId.equals(getValue()) || monkeyId.equals(getTitle())
				|| monkeyId.equals(getClassName()) || monkeyId.equals(getTextContent());
	}

	public WebView getWebView() {
		return this.webview;
	}

	public void setMonkeyId(String monkeyId) {
		attrs.put("monkeyId", monkeyId);

	}

	/**
	 * component type that was on the command that referenced this element
	 * 
	 * @param type
	 */
	// public void setUnderlyingType(String type) {
	// attrs.put("underlyingType", type);
	// }

	// public String getUnderlyingType() {
	// // TODO Auto-generated method stub
	// return attrs.get("underlyingType");
	// }

}