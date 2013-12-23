package com.gorillalogic.agents.html.browser;

import com.gorillalogic.agents.utils.OS;

public abstract class BrowserAdapter {
	
	public abstract BrowserType getBrowserType();
	public abstract String getPath();
	
	public String getDriverPath() {
		OS os = OS.getOSType(System.getProperty("os.name"));
		String path = "/browser-drivers/" + os.toString().toLowerCase();
		System.out.println(getBrowserType() + " WebDriver Path: " + path);
		return path;
	}
	
}
