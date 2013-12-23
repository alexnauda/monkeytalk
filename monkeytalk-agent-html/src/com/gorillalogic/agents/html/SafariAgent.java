package com.gorillalogic.agents.html;

import java.lang.reflect.Constructor;

import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.safari.SafariDriver;

import com.gorillalogic.agents.html.browser.BrowserType;
import com.gorillalogic.agents.html.browser.SafariAdapter;
import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;

public class SafariAgent extends WebDriverAgent {
	public SafariAgent() {
		super(16864);
	}
	public SafariAgent(int port) {
		super(port);
	}
	
	@Override
	public String getName() {
		return "Safari";
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		try {
			ping();
		} catch (Exception e) {
	    	// Safari Processor
//	    	scp = new SeleniumCommandProcessor(BrowserType.SAFARI, getHost(), new SafariDriver());
			CommandExecutor commandExecuter;
			
			try {
				Class<?> executer = Class.forName("org.openqa.selenium.safari.SafariDriverCommandExecutor");
				Class<?> partypes[] = new Class[1];
				partypes[0] = Integer.TYPE;
				Constructor<?> ct = executer.getConstructor(partypes);
				
				ct.setAccessible(true);
				
				Object arglist[] = new Object[1];
			    arglist[0] = new Integer(16864);
			    Object retobj = ct.newInstance(arglist);
			    commandExecuter = (CommandExecutor) retobj;
			    
			} catch (Throwable err) {
				throw new IllegalStateException("Unable to load WebDriverAgent: " + err);
			}
	    	
//			scp = new SeleniumCommandProcessor(BrowserType.SAFARI, getHost(), new SafariDriver());
			
	    	SafariAdapter safari = new SafariAdapter(commandExecuter);
//	    	scp = new SeleniumCommandProcessor(BrowserType.SAFARI, getHost(), safari.getSafariWebDriver());
	    	scp = new SeleniumCommandProcessor(BrowserType.SAFARI, getHost(), safari);
		}
	}
}
