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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class WebElementAutomator extends AutomatorBase {
	public static String componentType = "HtmlTag";
	public static String[] aliases = { "View" };

	private WebElement element = null;

	@Override
	public String getComponentType() { 
		return componentType;
	}

	@Override
	public void init(WebDriver driver, String monkeyId) {
		super.init(driver, monkeyId);
	}
	
	public WebElement getElement() {
		if (element != null) {
			return element;
		}
		
		String xpath = getLocatorExpr();
		try {
			List<WebElement> elements = driver.findElements(By.xpath(xpath));
			if (elements.size() == 0) {
				throw new IllegalArgumentException("Unable to find html element with monkeyID '" + this.monkeyId + "'");
			}
			int monkeyIndex = 0;
			
			if (getMonkeyOrdinal() != null) {
				String monkeyOrdinal = getMonkeyOrdinal().get(1);
				monkeyIndex = Integer.parseInt(monkeyOrdinal) - 1;
				
				if (Integer.parseInt(monkeyOrdinal) > elements.size())
					throw new IllegalArgumentException("Unable to find html element with monkeyID '" + this.monkeyId + "'");
			}
			
			driver.switchTo().defaultContent();
			element = elements.get(monkeyIndex);
		} catch (NoSuchElementException e1) {
			// OK
		}
		if (element != null) {
			return element;
		}
		
		List<WebElement> frameset = driver.findElements(By.tagName("iframe"));
		for (WebElement framename : frameset) {
			try {
				List<WebElement> elements = driver.findElements(By.xpath(xpath));
				int monkeyIndex = 0;
				
				if (getMonkeyOrdinal() != null) {
					String monkeyOrdinal = getMonkeyOrdinal().get(1);
					monkeyIndex = Integer.parseInt(monkeyOrdinal) - 1;
					
					if (Integer.parseInt(monkeyOrdinal) > elements.size())
						throw new IllegalArgumentException("Unable to find button with monkeyID '" + this.monkeyId + "'");
				}
				
				driver.switchTo().defaultContent();
				driver.switchTo().frame(framename);
				element = elements.get(monkeyIndex);
				break;
			} catch (NoSuchElementException e) {

			}
		}
		if (element == null) {
			throw new IllegalArgumentException("Unable to find " + getComponentType()
					+ " with monkeyId '" + monkeyId + "'");
		}
		return element;
	}

	public String getElementExpr() {
		return "//*";
	}
	
	public String getOrdinal() {
		String firstChar = this.monkeyId.substring(0, 1);
		
		if (firstChar.equals("#"))
			return this.monkeyId.substring(1);
		else if (this.monkeyId.equals("*"))
			return "1";
		
		return null;
	}
	
	public ArrayList<String> getMonkeyOrdinal() {
		Pattern pattern = Pattern.compile("\\(\\d+\\)$");
//		String firstChar = this.monkeyId.substring(0, 1);
		
		Matcher matcher = pattern.matcher(this.monkeyId);
		
		if (matcher.find()) {
			String ordinal = matcher.group().replace("(", "");
			ordinal = ordinal.replace(")", "");
			
//			this.monkeyId = this.monkeyId.substring(0, matcher.start());
			
			ArrayList<String> ordinalMID = new ArrayList<String>();
			
			ordinalMID.add(this.monkeyId.substring(0, matcher.start()));
			ordinalMID.add(ordinal);
			
			return ordinalMID;
		}
		
		return null;
	}

	/**
	 * "where" part of xpath expr (without []'s)
	 * 
	 * @param monkeyId
	 * @return
	 */
	public String getWhereExpr(String monkeyId) {
		String eqId = " = '" + monkeyId + "'";
		// The "." in the expession below matches the textContent of the node. 
		// For some (perhaps never TBD) reason, @textContent doesn't match 
		// when nodes contain tags, for example <a>this is <b>it</b></a>
		// . == 'this is it' matches but @textContent='this is it' doesn't
		// even though element.textContent will return 'this is it'.
		return "@id" + eqId + " or @name" + eqId + " or @value" + eqId + " or . " + eqId
				+ " or @title" + eqId + " or @styleClass" + eqId;
	}

	@Override
	public String play(Command command) {
		String action = command.getAction();
		if (isTapAction(action)) {
			tap();
			return null;
		}

		if (isEnterTextAction(action)) {
			enterText(command);
			return null;
		}


		return super.play(command);
	}
	
	protected void clear(Command command) {
		getElement().clear();
	}

	protected void enterText(Command command) {

		getElement().sendKeys(getArg(command, 0));
		if ("enter".equals(getOptArg(command, 1))) {
			getElement().sendKeys("\n");
		}

	}

	private boolean isEnterTextAction(String action) {
		return action.equalsIgnoreCase(AutomatorConstants.ACTION_ENTER_TEXT);
	}

	/**
	 * Override to customize tap
	 */
	protected void tap() {
		getElement().click();
		
		//new WebDriverBackedSelenium(driver, driver.getCurrentUrl()).clickAt(getLocatorExpr(),"0,0");

	}

	/**
	 * Override to customize what actions are taps
	 */
	private final static String[] taps = { AutomatorConstants.ACTION_TAP,
			AutomatorConstants.ACTION_CLICK };

	protected boolean isTapAction(String action) {
		for (String s : taps) {
			if (action.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}
	
	protected String getLocatorExpr() {
		String where = getWhereExpr(this.monkeyId);
		String base = getElementExpr();
		
		if (getMonkeyOrdinal() != null) {
			String strippedId = getMonkeyOrdinal().get(0);
			
			where = getWhereExpr(strippedId);
		} else if (getOrdinal() != null) {
			where = getOrdinal();
			base = "(" + base + ")";
		}
		
		return base + "[" + where + "]";
	}
	
	@Override
	protected void assertExists() {
		getElement();
	}

	@Override
	protected String getProperty(String prop) {
		String value = getElement().getAttribute(prop);
		return value == null ? super.getProperty(prop) : value;
	}

	@Override
	protected Rect getBoundingRectangle() {
		Dimension dim=getElement().getSize();
		Point p=getElement().getLocation();
		return new Rect(p.x, p.y, dim.height, dim.width);
	}
}
