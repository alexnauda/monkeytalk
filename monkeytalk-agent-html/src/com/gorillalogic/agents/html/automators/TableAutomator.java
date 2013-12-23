package com.gorillalogic.agents.html.automators;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class TableAutomator extends WebElementAutomator {
	public static String componentType = "Table";
	private Command tableCommand;
	private int row = -1;
	private int column = -1;
	
	protected WebElement element;

	@Override
	public String getComponentType() {
		return componentType;
	}
	
	public String getElementExpr() { 
		return "//table";   
	}
	
	@Override
	public String play(Command command) {
		tableCommand = command;
		String action = command.getAction();
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			return select(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			return selectIndex(command);
		}

		return super.play(command);
	}
	
	@Override
	protected String getLocatorExpr() {
		String action = tableCommand.getAction();
		String where = getWhereExpr(this.monkeyId);
		String monkeyOrdinal = null;
		
		if (getOrdinal() != null)
			where = getOrdinal();
		else if (getMonkeyOrdinal() != null) {
			where = getWhereExpr(getMonkeyOrdinal().get(0));
			monkeyOrdinal = getMonkeyOrdinal().get(1);
		}
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			String selection = getArg(tableCommand, 0);
			
			if (monkeyOrdinal != null)
				return "(//table[" + where + "])" + "[" + monkeyOrdinal + "]" + "//*[" + getWhereExpr(selection) + "]";
			
			return "//table[" + where + "]//*[" + getWhereExpr(selection) + "]";
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			String xpath = "//table[" + where + "]//tr[" + row + "]";
			
			if (monkeyOrdinal != null)
				xpath = "(//table[" + where + "])" + "[" + monkeyOrdinal + "]" + "//tr[" + row + "]";
			
			if (column != -1)
				xpath = xpath + "//*[" + column +"]";
			return xpath;
		}
		
		return super.getLocatorExpr();
	}

	protected String select(Command command) {
		String selection = getArg(command, 0);
		WebElement but;
		try {
			String xpath = getLocatorExpr();
			but = driver.findElement(By.xpath(xpath));
		} catch (NoSuchElementException e) {
			throw new IllegalArgumentException("Unable to find table '" + command.getMonkeyId() + "' with cell value '" + selection + "'");
		}
		but.click();
		return null;
	}
	
	protected String selectIndex(Command command) {
		int argCount = command.getArgs().size();
		
		if (argCount > 0)
			row = getIndexArg(command, 0);
		else
			throw new IllegalArgumentException("Action 'SelectIndex' requires 1 or more args");
		
		
		if (argCount == 2)
			column = getIndexArg(command, 1);
		
		WebElement but;
		try {
			String xpath = this.getLocatorExpr();
			
			but = driver.findElement(By.xpath(xpath));
		} catch (NoSuchElementException e) {
			String error = null;
			
			if (column != -1)
				error = "Unable to find cell for table '" + command.getMonkeyId() + "' at index '" + row + " " + column + "'";
			else
				error = "Unable to find row for table '" + command.getMonkeyId() + "' at index '" + row + "'";
			
			throw new IllegalArgumentException(error);
		}
		but.click();
		return null;
	}

}
