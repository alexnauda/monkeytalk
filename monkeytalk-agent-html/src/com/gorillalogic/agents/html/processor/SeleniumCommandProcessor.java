package com.gorillalogic.agents.html.processor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.gorillalogic.agents.html.automators.AutomationManager;
import com.gorillalogic.agents.html.automators.IAutomator;
import com.gorillalogic.agents.html.browser.BrowserType;
import com.gorillalogic.agents.html.browser.ChromeAdapter;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.server.JsonServer.HttpStatus;

public class SeleniumCommandProcessor {

	public WebDriver driver = null; // TODO: Move

	public SeleniumCommandProcessor() {
	}

	public SeleniumCommandProcessor(BrowserType bt, String url, WebDriver driver) {
		this.setWebDriver(bt, url, driver);
	}

	public Response processACommand(Command command) {
		try {
			Thread.sleep(command.getThinktime());
		} catch (InterruptedException e1) {
			// OK
		}
		driver.manage().timeouts().implicitlyWait(command.getTimeout(), TimeUnit.MILLISECONDS);
		String result;
		long start = System.currentTimeMillis();
		boolean error = false;
		do {
			IAutomator auto = AutomationManager.getAutomator(driver, command.getComponentType(),
					command.getMonkeyId());
			try {
			result = auto.play(command);
			error = false;
			} catch (NoSuchElementException e) {
				error = true;
				result = e.getMessage();
			}
		} while (error && System.currentTimeMillis() < start + command.getTimeout());
		if (error) {
			return new Response(ResponseStatus.ERROR, result, null, null);
		}
		return new Response(ResponseStatus.OK, result, null, null);

		// if (command.getComponentType().equalsIgnoreCase("Browser")) {
		// setWebDriver(BrowserType.getFromString(command.getMonkeyId()), command.getArgs().get(0));
		// return cResponse;
		// }
		// if (driver == null) {
		// return cResponse;
		// }
		//
		// WebElement we = ElementLocator.findWebElement(command, driver);
		// // TODO: handle case of not finding WebElement.
		//
		// BrowserAction bAction = BrowserAction.getBrowserAction(command.getAction());
		// // TODO: Handle error with WebElement action (e.g. sendKeys, submit, etc.)
		// switch(bAction) {
		// case CLEAR: {
		// we.clear();
		// break;
		// }
		// case CLICK:
		// case TAP: {
		// we.click();
		// break;
		// }
		// case INPUTTEXT:
		// case TYPE:
		// case SENDKEYS:
		// case ENTERTEXT: {
		// we.clear();
		// we.sendKeys(command.getArgs().get(0));
		// break;
		// }
		// case SUBMIT: {
		// we.submit();
		// }
		// case SELECT: {
		// if(we instanceof Select) {
		// ((Select) we).selectByValue(command.getArgs().get(0));
		// }
		// }
		// case SELECTINDEX: {
		// if(we instanceof Select) {
		// ((Select) we).selectByIndex(Integer.parseInt(command.getArgs().get(0)));
		// }
		// }
		// case VERIFY: {
		// VerifyState v = new VerifyState(we, command);
		// cResponse.setVerify(v); // Other way to handle this...
		// v.verify();
		// }
		// }
		// return cResponse;
	}

	private void setWebDriver(BrowserType bt, String url, WebDriver driver) {
		this.driver = driver;

		// Create a new instance of the FireFox driver
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		switch (bt) {

		case FIREFOX: {
			



			break;
		}
		case CHROME: {
			ChromeAdapter cadp = new ChromeAdapter(); // Move to handle start/stop.
			this.driver = cadp.getChromeWebDriver();
			break;
		}
		case IOS: {
			try {
				driver = new RemoteWebDriver(new URL("http://localhost:3001/wd/hub"),
						DesiredCapabilities.iphone());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// or use the convenience class which uses localhost:3001 by default
			break;
		}

		}

		// driver.get(url);
	}

}
