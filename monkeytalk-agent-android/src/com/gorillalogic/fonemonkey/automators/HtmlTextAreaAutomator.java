package com.gorillalogic.fonemonkey.automators;

public class HtmlTextAreaAutomator extends HtmlElementAutomator {
	@Override
	public String getValue() {
		return getHtmlElement().getAttr("value");
	}

	@Override
	public String getComponentType() {
		return "TextArea";
	}
}
