package com.gorillalogic.agents.html.automators;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ImageAutomator extends WebElementAutomator {
	public static String componentType = "Image";
	
	@Override
	public String getComponentType() {
		return componentType;
	}
	
	@Override
	public String getElementExpr() { 
		return "//img"; 
	}
}
