package com.gorillalogic.agents.html.automators;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ButtonAutomator extends WebElementAutomator {
	public static String componentType = "Button";

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public String getElementExpr() {
		// return
		// "//*[self::button or self::input][self::button or @type='submit' or @type='password' or @type='reset']";

		return "//*[translate(name(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'button' or "
				+ "(translate(name(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'input' and "
				+ "(translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'submit' or translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'password' or translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'button' or translate(@type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = 'reset'))]";
	}

	@Override
	protected String getProperty(String prop) {
		if (prop.equals("value") && getElement().getTagName().equalsIgnoreCase("button")) {
			prop = "textContent";
		}
		return super.getProperty(prop);
	}

	@Override
	public WebElement getElement() {
		// TODO Auto-generated method stub

		if (getMonkeyOrdinal() != null) {
			String monkeyOrdinal = getMonkeyOrdinal().get(1);
			List<WebElement> elements = driver.findElements(By.xpath(getLocatorExpr()));
			int monkeyIndex = Integer.parseInt(monkeyOrdinal) - 1;

			if (Integer.parseInt(monkeyOrdinal) > elements.size())
				throw new IllegalArgumentException("Unable to find button with monkeyID '"
						+ this.monkeyId + "'");

			return elements.get(monkeyIndex);
		} else if (getOrdinal() != null) {
			List<WebElement> elements = driver.findElements(By.xpath(getElementExpr()));
			int monkeyIndex = Integer.parseInt(getOrdinal()) - 1;

			if (Integer.parseInt(getOrdinal()) > elements.size())
				throw new IllegalArgumentException("Unable to find button with monkeyID '"
						+ this.monkeyId + "'");

			return elements.get(monkeyIndex);
		}

		return super.getElement();
	}

}
