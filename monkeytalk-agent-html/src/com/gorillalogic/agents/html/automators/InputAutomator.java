package com.gorillalogic.agents.html.automators;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class InputAutomator extends WebElementAutomator {
	public static String componentType = "Input";
	@Override
	public String getElementExpr() { 
		return "//input[@type = 'text' or @type = 'password' or @name = 'text']";   
	}
	
	@Override
	public String getComponentType() {
		return componentType;
	}
	
	@Override
	public String play(Command command) {
		String action = command.getAction();
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_CLEAR)) {
			clear(command);
			return null;
		}

		return super.play(command);
	}
	     
}
 