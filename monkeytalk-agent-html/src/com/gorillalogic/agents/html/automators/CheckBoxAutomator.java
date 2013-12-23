package com.gorillalogic.agents.html.automators;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class CheckBoxAutomator extends WebElementAutomator {
	public static String componentType = "CheckBox";
	public static String[] aliases = { "Toggle" };
	
	protected WebElement element;
	
	@Override
	public String getComponentType() {
		return componentType;
	}
	
	public String getElementExpr() { 
		return "//input[@type = 'checkbox']";   
	}
	
	@Override
	protected String getLocatorExpr() {
		if (getOrdinal() != null)
			return "(" + getElementExpr() + ")" + "[" + getOrdinal() + "]";
		else if (getMonkeyOrdinal() != null)
			return "(" + getElementExpr() + "[" + getWhereExpr(getMonkeyOrdinal().get(0)) + "])[" + getMonkeyOrdinal().get(1) + "]";
		
		return getElementExpr() + "[" + getWhereExpr(this.monkeyId) + "]";
	}
	
	@Override
	public String play(Command command) {
		String action = command.getAction();
		element = driver.findElement(By.xpath(getLocatorExpr()));
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ON) ||
				action.equalsIgnoreCase(AutomatorConstants.ACTION_OFF)) {
			return toggle(command);
		}

		return super.play(command);
	}

	protected String toggle(Command command) {
		String action = command.getAction();
		
		try {
			if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ON)) {
				if (!element.isSelected())
					element.click();
				
				return null;
			} else {
				if (element.isSelected())
					element.click();
				
				return null;
			}
			
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Unable to find checkbox with monkeyID '" + command.getMonkeyId() + "'");
		}
	}
	
	@Override
	protected String getProperty(String prop) {
		if (prop.equals("value")) {
			if (element.isSelected())
				return "on";
			else
				return "off";
		}
		return super.getProperty(prop);
	}

}
