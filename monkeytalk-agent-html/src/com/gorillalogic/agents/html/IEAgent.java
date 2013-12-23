package com.gorillalogic.agents.html;

import org.openqa.selenium.ie.InternetExplorerDriver;

import com.gorillalogic.agents.html.browser.BrowserType;
import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;

public class IEAgent extends WebDriverAgent {
	public IEAgent() {
		super();
	}
	public IEAgent(int port) {
		super(port);
	}
	
	@Override
	public String getName() {
		return "IE";
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		try {
			ping();
		} catch (Exception e) {
	    	// IE Processor
	    	scp = new SeleniumCommandProcessor(BrowserType.IE, getHost(), new InternetExplorerDriver());
		}
	}
}
