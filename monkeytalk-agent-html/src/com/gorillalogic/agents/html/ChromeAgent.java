package com.gorillalogic.agents.html;

import com.gorillalogic.agents.html.browser.BrowserType;
import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;

public class ChromeAgent extends WebDriverAgent {
	public ChromeAgent() {
		super();
	}
	public ChromeAgent(int port) {
		super(port);
	}
	
	@Override
	public String getName() {
		return "Chrome";
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		try {
			ping();
		} catch (Exception e) {
	    	// Chome Processor
	    	scp = new SeleniumCommandProcessor(BrowserType.CHROME, getHost(), null);
		}
	}

}
