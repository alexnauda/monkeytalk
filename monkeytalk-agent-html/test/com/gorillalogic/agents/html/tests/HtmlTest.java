package com.gorillalogic.agents.html.tests;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import com.gorillalogic.agents.utils.OS;
import com.gorillalogic.agents.html.browser.BrowserType;
import com.gorillalogic.agents.html.browser.ChromeAdapter;


public class HtmlTest extends HtmlTestHelper {

	
	@Test
	public void testGetDriverPath() {
		System.setProperty("os.name", "Mac OS X");
		ChromeAdapter cadp = new ChromeAdapter();
		assertThat(cadp.getDriverPath(), is("/browser-drivers/mac"));
	}
	
	@Test
	public void testGetOSType() {
		OS osMac = OS.getOSType("Mac");
		assertThat(osMac, is(OS.MAC));
		
		OS osWin = OS.getOSType("Windows");
		assertThat(osWin, is(OS.WINDOWS));
		
		OS osLinux = OS.getOSType("nix");
		assertThat(osLinux, is(OS.LINUX));		
	}
	
	@Test
	public void testBrowserType() {
		BrowserType bt = BrowserType.getFromString("firefox");
		assertThat(bt, is(BrowserType.FIREFOX));
	}

}
