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

package com.gorillalogic.agents.html;

import java.io.IOException;
import java.lang.reflect.Method;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ClasspathExtension;

import com.gorillalogic.agents.html.browser.BrowserType;
import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;
import com.gorillalogic.monkeytalk.agents.MTAgent;
import com.gorillalogic.monkeytalk.sender.CommandSender;

public class WebDriverAgent extends MTAgent {
	protected SeleniumCommandProcessor  scp;
	
	public WebDriverAgent() {
		super("localhost", 80);
	}
	public WebDriverAgent(int port) {
		super(port);
	}
	@Override
	public String getName() {
		return "WebDriver";
	}

	@Override
	protected CommandSender createCommandSender(String host, int port) {
		return null;
		/*
        return new CommandSender(host, port) {

        	@Override
			public Response play(Command command) {
				try {
					return scp.processACommand(command);
				} catch (IllegalArgumentException ex) {
					return new Response(HttpStatus.BAD_REQUEST.getCode(), ex.getMessage());
				} catch (Exception ex) {
					ex.printStackTrace();
					return new Response(HttpStatus.INTERNAL_ERROR.getCode(), ex.getClass().getName() + ": "  + ex.getMessage());
				}
			}
        	
			@Override
			public Response play(String componentType, String monkeyId,
					String action, List<String> args,
					Map<String, String> modifiers) {
        		return play(new Command(componentType, monkeyId, action, args, modifiers));
        	}
			
			@Override
			public Response ping(boolean recordOn, String recordHost, int recordPort) {
				try {
					WebDriverAgent.this.ping();
				} catch (Exception ex) {
					return new Response(HttpStatus.INTERNAL_ERROR.getCode(), "WebDriver not responding to ping: " + ex.getMessage());
				}
				return new Response();
			}
        };
        */
	}
	
	@Override
	public void start() {
		try {
			ping();
		} catch (Exception e) {
			FirefoxProfile profile = new FirefoxProfile();
			try {
				Method m = FirefoxProfile.class.getDeclaredMethod("addExtension", String.class, org.openqa.selenium.firefox.internal.Extension.class);
				m.setAccessible(true);				
//				ClasspathExtension extension = new ClasspathExtension(SeleniumCommandProcessor.class,
//				        "/" + SeleniumCommandProcessor.class.getPackage().getName().replace(".", "/") + "/selenium-ide.xpi");				
//				m.invoke(profile, "selenium-ide", extension);				
				ClasspathExtension extension = new ClasspathExtension(SeleniumCommandProcessor.class,
						"/" + SeleniumCommandProcessor.class.getPackage().getName().replace(".", "/") + "/monkeytalk-recorder.xpi");				
				m.invoke(profile, "monkeytalk-recorder", extension);				
			} catch (Exception ex) {
				throw new IllegalStateException("Unable to install monkeytalk-recorder firefox extension: " + ex.getMessage());
			}
	    	scp = new SeleniumCommandProcessor(BrowserType.FIREFOX, getHost(), new FirefoxDriver(profile));
		}
	}
	
	@Override
	public void close() {
		if (scp != null) {
			scp.driver.close();
		}
	}
	
	protected void ping() throws IOException {
		try {
			scp.driver.getCurrentUrl();
		} catch (Exception e) {
			throw new IOException("Unable to contact WebDriver");
		}
		
	}


}
