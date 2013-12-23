package com.gorillalogic.agents.html.browser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

//public class SafariAdapter extends BrowserAdapter {
//
//	@Override
//	public BrowserType getBrowserType() {
//		return BrowserType.SAFARI;
//	}
//
//	@Override
//	public String getPath() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	public WebDriver getSafariWebDriver() {
//		try {
//			Class<?> executer = Class.forName("org.openqa.selenium.safari.SafariDriverCommandExecutor");
//			Class<?> partypes[] = new Class[1];
//			partypes[0] = Integer.TYPE;
//			Constructor<?> ct = executer.getConstructor(partypes);
//			
//			ct.setAccessible(true);
//			
//			Object arglist[] = new Object[1];
//	        arglist[0] = new Integer(0);
//	        Object retobj = ct.newInstance(arglist);
//			
//			return new RemoteWebDriver((CommandExecutor) retobj, DesiredCapabilities.safari());
//			
////			Class<?> writeoutClass = Class.forName("org.openqa.selenium.safari.SafariDriverCommandExecutor");
////
////			Method writeout = null;
////			for (Method mth : writeoutClass.getDeclaredMethods()) {
////				String methodString = mth.getName();
////				
////				methodString = "";
//////			    if (mth.getName().startsWith("writeout")) {
//////			        writeout = mth;
//////			        break;
//////			    }
////			}
////			
////			return null;
//		} catch (Throwable e) {
//			throw new IllegalStateException("Unable to load WebDriverAgent: " + e);
//		}
//	}
//
//}

public class SafariAdapter extends RemoteWebDriver
implements TakesScreenshot {

public SafariAdapter(CommandExecutor commandExecuter) {
	super(commandExecuter, DesiredCapabilities.safari());
}

@Override
public void setFileDetector(FileDetector detector) {
throw new WebDriverException(
    "Setting the file detector only works on remote webdriver instances obtained " +
    "via RemoteWebDriver");
}

@Override
protected void startClient() {
	CommandExecutor executor = (CommandExecutor) this.getCommandExecutor();
	
	Class<?> currentClass =  this.getClass();
	Method start = null;
	for (Method mth : executor.getClass().getDeclaredMethods()) {
		String methodString = mth.getName();
		
	    if (mth.getName().startsWith("start")) {
	    	start = mth;
	        break;
	    }
	}
	
	try {
		start.setAccessible(true);
		start.invoke(executor);
	} catch (IllegalArgumentException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (IllegalAccessException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (InvocationTargetException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
//try {
//  executor.start();
//} catch (IOException e) {
//  throw new WebDriverException(e);
//}
}

@Override
protected void stopClient() {
	CommandExecutor executor = (CommandExecutor) this.getCommandExecutor();
//executor.stop();
}

public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
// Get the screenshot as base64.
String base64 = (String) execute(DriverCommand.SCREENSHOT).getValue();
// ... and convert it.
return target.convertFromBase64Png(base64);
}
}
