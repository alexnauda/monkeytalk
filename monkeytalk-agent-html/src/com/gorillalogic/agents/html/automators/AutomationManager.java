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

import java.util.HashMap;

import org.openqa.selenium.WebDriver;


public class AutomationManager {
	
	private static HashMap<String, Class<?>> automators = new HashMap<String,Class<?>>();
	
	static {
		registerAutomator(WebElementAutomator.componentType, WebElementAutomator.class, WebElementAutomator.aliases);
		registerAutomator(BrowserAutomator.componentType, BrowserAutomator.class);	
		registerAutomator(InputAutomator.componentType, InputAutomator.class);			
		registerAutomator(ButtonAutomator.componentType, ButtonAutomator.class);			
		registerAutomator(LinkAutomator.componentType, LinkAutomator.class);			
		registerAutomator(ItemSelectorAutomator.componentType, ItemSelectorAutomator.class);			
		registerAutomator(RadioButtonsAutomator.componentType, RadioButtonsAutomator.class, RadioButtonsAutomator.aliases);	
		registerAutomator(TableAutomator.componentType, TableAutomator.class);
		registerAutomator(CheckBoxAutomator.componentType, CheckBoxAutomator.class, CheckBoxAutomator.aliases);
		registerAutomator(TextAreaAutomator.componentType, TextAreaAutomator.class);
		registerAutomator(ImageAutomator.componentType, ImageAutomator.class);
		registerAutomator(LabelAutomator.componentType, LabelAutomator.class);
	}
	
	public static IAutomator getAutomator(WebDriver driver, String name, String monkeyId) {
		return find(driver, name, monkeyId);

	}
	
	private static IAutomator find(WebDriver driver, String componentType, String monkeyId) {
		Class<?> autoClass = automators.get(componentType);
		if (autoClass == null) {
			// Before failing with Unrecognized web component type, try executing js directly
			autoClass = ExecAutomator.class;
			
			//throw new IllegalArgumentException("Unrecognized web component type: " + componentType);
		}
		
		IAutomator auto;
		try {
			auto = (IAutomator) autoClass.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to instance automator " + autoClass.getName() + " for " + componentType + " : " + e.getMessage());
		}
		
		auto.init(driver, monkeyId);		
		return auto;
	}

	public static void registerAutomator(String componentType, Class<?> autoClass, String[] typeAliases) {
		registerAutomator(componentType, autoClass);
		for (String alias : typeAliases) {
			registerAutomator(alias, autoClass);
		}
	}
	
	public static void registerAutomator(String componentType, Class<?> autoClass) {
		automators.put(componentType, autoClass);
	}
}
