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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class ItemSelectorAutomator extends WebElementAutomator {
	public static String componentType = "ItemSelector";
	public static String[] aliases = { "Select" };

	private Select select = null;

	@Override
	public String getComponentType() {
		return componentType;
	}

	public String getElementExpr() {
		return "//select";
	}

	@Override
	public String play(Command command) {
		String action = command.getAction();
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_CLEAR)) {
			return clear();
		}
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			return select(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			return selectIndex(command);
		}

		return super.play(command);
	}
	
	protected String clear() {
		getSelect().deselectAll();
		
		return null;
	}

	protected String selectIndex(Command command) {
		int index = getIndexArg(command, 0) - 1;
		if (getSelect().isMultiple()) {
			getSelect().deselectAll();
		}
		getSelect().selectByIndex(index);
		return null;
	}

	protected String select(Command command) {
		List<String> selected = command.getArgs();
		int successCount = 0;
		
		// We may want to deselect all if we do not have deselect action
		if (getSelect().isMultiple()) {
			getSelect().deselectAll();
		}
		
		int i = 0;
		for (WebElement option : getSelect().getOptions()) {
			
			for (String selection : selected) {
				if (selection.equals(option.getText()) || selection.equals(option.getAttribute("value"))) {
					getSelect().selectByIndex(i);
					successCount++;
				}
			}
			i++;
		}
		
		if (successCount == command.getArgs().size())
			return null;
		
		throw new IllegalArgumentException("Invalid selection value '" + command.getArgsAsString() + "'");
	}

	private Select getSelect() {
		if (select != null) {
			return select;
		}

		select = new Select(getElement());
		return select;

	}

	@Override
	protected String getProperty(String prop) {

		String value = getSelect().getFirstSelectedOption().getAttribute(prop);
		return value == null ? super.getProperty(prop) : value;

	}
	
	@Override
	protected String getLocatorExpr() {
		String where = getWhereExpr(this.monkeyId);
		
		if (getOrdinal() != null) {
			where = getOrdinal();
			
			return "(" + getElementExpr() + ")" + "[" + where + "]";
		} else if (getMonkeyOrdinal() != null) {
			where = getWhereExpr(getMonkeyOrdinal().get(0));
		}
		
		return getElementExpr() + "[" + where + "]";
	}
}
