package com.gorillalogic.fonemonkey.automators;

public class HtmlInputAutomator extends HtmlElementAutomator {

	@Override
	public String getValue() {
		return getHtmlElement().getAttr("value");
	}

	@Override
	public String getComponentType() {
		return "Input";
	}

}
