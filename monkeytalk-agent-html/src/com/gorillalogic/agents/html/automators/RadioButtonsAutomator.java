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

package com.gorillalogic.agents.html.automators;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class RadioButtonsAutomator extends ItemSelectorAutomator {
	public static String componentType = "RadioButtons";
	public static String[] aliases = { "ButtonSelector" };
	private Command radioCommand;

	@Override
	public String getComponentType() {
		return componentType;
	}

	public String getElementExpr() {
		return "//input[@type = 'radio']";
	}
	
	@Override
	protected String getLocatorExpr() {
		String action = radioCommand.getAction();
		String base;
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			String selection = getArg(radioCommand, 0);
			base = getElementExpr() + "[" + getWhereExpr(this.monkeyId) + "]";
			
			if (getOrdinal() != null)
				base = "(" + getElementExpr() + ")" + "[" + getOrdinal() + "]";
			else if (getMonkeyOrdinal() != null)
				base = "(" + getElementExpr() + ")" + "[" + getMonkeyOrdinal().get(1) + "]";
			
			return base + "[@value = '" + selection + "']";
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			int index = getIndexArg(radioCommand, 0);
			base = getElementExpr() + "[" + getWhereExpr(this.monkeyId) + "]";
			
			if (getOrdinal() != null)
				base = getElementExpr() + "[" + getOrdinal() + "]";
			else if (getMonkeyOrdinal() != null)
				base = getElementExpr() + "[" + getMonkeyOrdinal().get(1) + "]";
			
			return "(" + base + ")[" + index + "]";
		}
		
		return super.getLocatorExpr();
	}
	
	@Override
	public String play(Command command) {
		String action = command.getAction();
		radioCommand = command;
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			return select(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			return selectIndex(command);
		}

		return super.play(command);
	}

	@Override
	protected String selectIndex(Command command) {
		int index = getIndexArg(command, 0);
		WebElement but;
		try {
			String xpath = getLocatorExpr();
			but = driver.findElement(By.xpath(xpath));
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Unable to find button radio button at index '" + index + "'");
		}
		but.click();
		return null;
	}

	@Override
	protected String select(Command command) {
		String selection = getArg(command, 0);
		WebElement but;
		try {
			String xpath = getLocatorExpr();
			but = driver.findElement(By.xpath(xpath));
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Unable to find button radio button with value '" + selection + "'");
		}
		but.click();
		return null;
	}
	
}
