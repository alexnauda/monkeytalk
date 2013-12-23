package com.gorillalogic.agents.html.browser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.gorillalogic.agents.utils.OS;

/**
 * TODO: Determine how driver will get loaded/found on testing system.
 * 
 * Selenium Doc @link http://code.google.com/p/selenium/wiki/ChromeDriver The ChromeDriver controls
 * the browser using Chrome's automation proxy framework.
 * 
 * The server expects you to have Chrome installed in the default location for each system: Linux ->
 * /usr/bin/google-chrome1 Mac -> /Applications/Google\Chrome.app/Contents/MacOS/Google\ Chrome
 * Windows XP -> %HOMEPATH%\Local Settings\Application Data\Google\Chrome\Application\chrome.exe
 * Windows Vista -> C:\Users\%USERNAME%\AppData\Local\Google\Chrome\Application\chrome.exe
 */
public class ChromeAdapter extends BrowserAdapter {

	private static OS getOs() {
		return OS.getOSType(System.getProperty("os.name"));
	}

	private static String getDriverFile() {
		String chromeDriver = "chromedriver";

		if (getOs().toString().toLowerCase().equalsIgnoreCase("windows"))
			chromeDriver = "chromedriver.exe";

		// throw new IllegalStateException("Path: " + chromeDriver);

		return chromeDriver;
	}

	public BrowserType getBrowserType() {
		return BrowserType.CHROME;
	}

	public String getPath() {

		URL url = getClass().getResource(getDriverPath() + "/" + getDriverFile()); // Handle slashes
																					// for windows

		// throw new IllegalStateException("Path: " + url.getPath());
		return url.getPath();
	}

	/**
	 * Starts service and returns the driver. Note: Might want to separate the start and driver
	 * create.
	 * 
	 * @return
	 */
	public WebDriver getChromeWebDriver() {

		// ChromeDriverService chromeSvc = new ChromeDriverService.Builder()
		// .usingChromeDriverExecutable(copiedDriver())
		// .usingAnyFreePort().build();

		// Discussed in this issue report to fix chromedriver errors in log:
		// http://code.google.com/p/selenium/issues/detail?id=3378
		// DesiredCapabilities chromeCapabilities = DesiredCapabilities.chrome();
		// System.setProperty("webdriver.chrome.driver", copiedDriver().getAbsolutePath());
		// WebDriver driverGC = new ChromeDriver(chromeCapabilities);
		//
		// return driverGC;

		File driverFile = copiedDriver();

		ChromeDriverService chromeSvc = new ChromeDriverService.Builder()
				.usingDriverExecutable(driverFile).usingAnyFreePort().build();

		try {
			chromeSvc.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new RemoteWebDriver(chromeSvc.getUrl(), DesiredCapabilities.chrome());
	}

	private File copiedDriver() {
		String path = getDriverPath() + "/";

		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		File temporaryFile = new File(tempDir, getDriverFile());
		InputStream templateStream = getClass().getResourceAsStream(path + getDriverFile());

		try {
			IOUtils.copy(templateStream, new FileOutputStream(temporaryFile));
			templateStream.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Set copied driver to be executable
		temporaryFile.setExecutable(true);

		return temporaryFile;
	}

	public static void stopService(ChromeDriverService service) {
		service.stop();
	}

	public RemoteWebDriver getWebDriver(ChromeDriverService service) {
		return new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
	}

	public void quitDriver(RemoteWebDriver driver) {
		driver.quit();
	}

}
