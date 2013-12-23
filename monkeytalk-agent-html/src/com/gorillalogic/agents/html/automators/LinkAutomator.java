package com.gorillalogic.agents.html.automators;

import org.openqa.selenium.WebElement;

public class LinkAutomator extends WebElementAutomator {
	public static String componentType = "Link";
	public static String[] aliases = { "A", "SPAN" };

	protected WebElement element;

	@Override
	public String getComponentType() {
		return componentType;
	}
	
	public String getElementExpr() { 
		return "//*[self::a or self::span]";
		//return "//a";   
	}
	
	@Override
	protected String getProperty(String prop) {
		if (prop.equals("value")) {
			prop = "textContent";
		}
		return super.getProperty(prop);
	}
}
