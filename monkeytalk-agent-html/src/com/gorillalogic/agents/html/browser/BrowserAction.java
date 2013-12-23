package com.gorillalogic.agents.html.browser;


public enum BrowserAction {

	BROWSER("browser"), 
	CLICK("click"), 
	TAP("tap"), 
	INPUTTEXT("inputtext"), 
	SELECT("select"), 
	SELECTINDEX("selectindex"), 
	SENDKEYS("sendkeys"), 
	TYPE("type"), 
	ENTERTEXT("entertext"),
	SUBMIT("submit"), 
	VERIFY("verify"),
	CLEAR("clear");
	
	private String actionName;
	
	private BrowserAction(String name) {
		actionName = name;
	}
	
	/**
	 * Thread Safe
	 * @param name
	 * @return BrowserAction
	 */
	public static BrowserAction getBrowserAction(String name) {
		synchronized (BrowserAction.class) {
			if(name != null) {
				for (BrowserAction ba : BrowserAction.values()) {
					if(ba.actionName.equalsIgnoreCase(name)) {
						return ba;
					}
				}
			}
			throw new IllegalArgumentException("No enum matches name: " + BrowserAction.class + "@name." + name);
		}
	}
	
}
