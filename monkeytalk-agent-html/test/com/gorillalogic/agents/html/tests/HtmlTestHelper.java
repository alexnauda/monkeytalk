package com.gorillalogic.agents.html.tests;

import java.io.File;
import java.net.URL;

public class HtmlTestHelper {
		
	protected File getTestResource(String fileName) {
		URL url = getClass().getResource("/test/" + fileName);
		System.out.println("Path to test resource: " + url.getPath());
		File rsc = new File(url.getPath());
		return rsc;
	}
	
	// EasyMock out Driver...	
}
