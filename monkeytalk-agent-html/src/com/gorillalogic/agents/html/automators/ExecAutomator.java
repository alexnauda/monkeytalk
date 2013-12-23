package com.gorillalogic.agents.html.automators;

import org.openqa.selenium.JavascriptExecutor;

import com.gorillalogic.monkeytalk.Command;

public class ExecAutomator extends AutomatorBase {
	public static String componentType = "ExecAutomator";

	@Override
	public String getComponentType() {
		return componentType;
	}
	
	@Override
	public String play(Command command) {
		return doExec(command);
	}
	
	/**
	 * JavaScript implementation:
	 * var MonkeyExec = {};
	 * MonkeyExec.getSomething = function(args) {
	 * 		var result;
	 *		// Do work...
	 *		return result;
	 * };
	 * 
	 * MonkeyTalk command:
	 * MonkeyExec * getSomething arg1 arg2 ...
	 * 
	 * @param command
	 * @return The results we received from the js function
	 */
	private String doExec(Command command) {
		String result = null;
		String component = command.getComponentType();
		String action = command.getAction();
		String args = command.getArgsAsJsArray();
		
		String jsString = "return " + component + "." + action + "(" + args + ");";
		JavascriptExecutor js = (JavascriptExecutor) driver;
		
		try {
			result = (String) js.executeScript(jsString);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unrecognized web component type: " + component + " with action: " + action);
		}
		
		return result;
	}
}
