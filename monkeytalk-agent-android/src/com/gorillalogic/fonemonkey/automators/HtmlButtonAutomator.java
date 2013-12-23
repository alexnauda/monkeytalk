package com.gorillalogic.fonemonkey.automators;

public class HtmlButtonAutomator extends HtmlElementAutomator {
	@Override
	public String getValue() {
		String value = getHtmlElement().getAttr("value");
		if (value != null && value.length() > 0) {
			return value;
		}
		return super.getValue();
	}

	@Override
	public String getComponentType() {
		return "Button";
	}
}
