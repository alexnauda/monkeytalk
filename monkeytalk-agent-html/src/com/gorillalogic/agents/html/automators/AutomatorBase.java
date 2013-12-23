
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

package com.gorillalogic.agents.html.automators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.WebDriver;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * Base automator for desktop web elements
 * 
 * @author sstern
 * 
 */
public abstract class AutomatorBase implements IAutomator {

	protected WebDriver driver;
	protected String monkeyId;

	@Override
	public String play(Command command) {
		String action = command.getAction();

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT)) {
			return verifyNot(command);
		}
		
		assertExists();
		
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_GET)) {
			return get(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY)) {
			return verify(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_REGEX)) {
			return verifyRegex(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT_REGEX)) {
			return verifyNotRegex(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_WILDCARD)) {
			return verifyWildcard(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_NOT_WILDCARD)) {
			return verifyNotWildcard(command);
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_VERIFY_IMAGE)) {
			return verifyImage(command);
		}

		throw new IllegalArgumentException("Action '" + command.getAction()
				+ "' unrecognized for component type " + command.getComponentType());
	}

	protected String get(Command command) {

		return doGet(command);
	}
	
	protected String verify(Command command) {

		return doVerify(command, false);
	}

	protected String verifyNot(Command command) {
		return doVerify(command, true);
	}

	protected String verifyRegex(Command command) {

		return doVerifyRegex(command, false);
	}

	protected String verifyNotRegex(Command command) {
		return doVerifyRegex(command, true);
	}

	protected String verifyWildcard(Command command) {

		return doVerifyWildcard(command, false);
	}

	protected String verifyNotWildcard(Command command) {
		return doVerifyWildcard(command, true);
	}
	
	protected String verifyImage(Command command) {
		return doVerifyImage(command);
	}
	
	private String doGet(Command command) {

		String propName = getOptArg(command, 1);
		if (propName == null) {
			propName = "value";
		}
		String result = getProperty(propName);
		
		return result;
	}

	private String doVerify(Command command, boolean not) {
		String expected = getOptArg(command, 0);

		if (expected == null) {
			if (not) {
				boolean exists = true;
				try { 
					assertExists();
				} catch (Exception e) {
					exists = false;
				}
				if (exists) {
					throw new NoSuchElementException(getComponentType() + " '" + monkeyId + "' should not be found");
				}
				return "true";
			}
		}

		String propName = getOptArg(command, 1);
		if (propName == null) {
			propName = "value";
		}
		String actual = getProperty(propName);

		boolean result = actual.equals(expected);
		if (not) {
			if (result) {
				throw new java.util.NoSuchElementException("Actual value for property '" + propName
						+ "' should not equal '" + actual + "'");
			}
			return "true";
		} else {
			if (!result) {
				throw new java.util.NoSuchElementException("Expected '" + expected
						+ "' but found '" + actual + "' for property '" + propName + "'");
			}
			return "true";
		}
	}

	// Throw an exception if the associated component exists
	protected void assertExists() {
		
	}

	private String doVerifyRegex(Command command, boolean not) {
		String expected = getArg(command, 0);

		String propName = getOptArg(command, 1);
		if (propName == null) {
			propName = "value";
		}
		String actual = getProperty(propName);

		boolean result = actual.matches(expected);
		if (not) {
			if (result) {
				throw new java.util.NoSuchElementException("Actual value '" + actual + "'for property '" + propName
						+ "' should not match '" + expected + "'");
			}
			return "true";
		} else {
			if (!result) {
				throw new java.util.NoSuchElementException("Expected to match '" + expected
						+ "' but found '" + actual + "' for property '" + propName + "'");
			}
			return "true";
		}
	}

	private String doVerifyWildcard(Command command, boolean not) {
		String expected = getArg(command, 0);
		String regex = wildcardToRegex(expected);
		String propName = getOptArg(command, 1);
		if (propName == null) {
			propName = "value";
		}
		String actual = getProperty(propName);

		boolean result = actual.matches(regex);
		if (not) {
			if (result) {
				throw new java.util.NoSuchElementException("Actual value '" + actual + "'for property '" + propName
						+ "' should not match '" + expected + "'");
			}
			return "true";
		} else {
			if (!result) {
				throw new java.util.NoSuchElementException("Expected to match '" + expected
						+ "' but found '" + actual + "' for property '" + propName + "'");
			}
			return "true";
		}
	}

	/**
	 * performs the agent-side part of the verifyImage command
	 * take a screenshot and return it, along with the 
	 * position and size of the target component
	 * @return
	 */
	private String doVerifyImage(Command command) {
		Rect br=getBoundingRectangle();
		return takeScreenshot(br.x+" "+ br.y + " " + br.w + " " + br.h);
	}
	
	private String takeScreenshot(String msg) {
		if (msg == null) {
			msg = "no message";
		}
		System.out.println("VerifyImage SCREENSHOT - " + msg + " - taking screenshot...");
		
		//DeviceAutomator device = (DeviceAutomator) AutomationManager.findAutomatorByType("Device");
		IAutomator device = this;
		Command screenshotCommand 
			= new Command(this.getComponentType(),
							this.monkeyId,
							AutomatorConstants.ACTION_SCREENSHOT, 
							new ArrayList<String>(),
							new HashMap<String, String>());

		if (device != null) {
			try {
				String screenshot = device.play(screenshotCommand);
				if (screenshot != null && screenshot.startsWith("{screenshot")) {
					System.out.println("SCREENSHOT - done!");
					return "{message:\"" + msg.replaceAll("\"", "'") + "\","
							+ screenshot.substring(1);
				}
			} catch (Exception ex) {
				String exMsg = ex.getMessage();
				if (exMsg != null) {
					exMsg = exMsg.replaceAll("\"", "'");
				} else {
					exMsg = ex.getClass().getName();
				}
				return msg + " -- " + exMsg;
			}
		}
		return msg;
	}

	protected static class Rect {
		int x,y,w,h;
		public Rect(int x, int y, int w, int h) {this.x=x; this.y=y; this.w=w; this.h=h;}
	}
	protected Rect getBoundingRectangle() {
		return new Rect(0,0,1,1);
	}
	
	protected String getArg(Command command, int index) {
		try {
			return command.getArgs().get(index);
		} catch (Exception e) {
			throw new IllegalArgumentException("Expected " + index + " arguments for action '"
					+ command.getAction() + "' but found " + command.getArgs().size());
		}
	}
	
	protected int getIndexArg(Command command, int index) {
		String arg = getOptArg(command, index);
		if (arg == null) {
			return 1;
		}
		int i = 0;
		try {
			i = Integer.valueOf(arg);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Expecting numeric value for argument " + index + 1 + " but found '" + arg + "'");
		}
		if (i < 1) {
			throw new IllegalArgumentException("Expecting positive (> 0) value for argument " + index + 1 + " but found '" + i + "'");
		}
		return i;
		
	}

	protected String getOptArg(Command command, int index) {
		try {
			return command.getArgs().get(index);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void init(WebDriver driver, String monkeyId) {
		this.driver = driver;
		this.monkeyId = monkeyId;

	}

	/**
	 * The actual value of some property
	 * 
	 * @param prop
	 *            the property name
	 * @return the property value
	 */
	protected String getProperty(String prop) {
		throw new IllegalArgumentException("No such property '" + prop + "' for "
				+ getComponentType());
	}

	// Thanks - http://www.rgagnon.com/javadetails/java-0515.html
	private static String wildcardToRegex(String wildcard) {
		StringBuilder s = new StringBuilder(wildcard.length());
		s.append('^');
		for (int i = 0, is = wildcard.length(); i < is; i++) {
			char c = wildcard.charAt(i);
			switch (c) {
			case '*':
				s.append(".*");
				break;
			case '?':
				s.append(".");
				break;
			// escape special regexp-characters
			case '(':
			case ')':
			case '[':
			case ']':
			case '$':
			case '^':
			case '.':
			case '{':
			case '}':
			case '|':
			case '\\':
				s.append("\\");
				s.append(c);
				break;
			default:
				s.append(c);
				break;
			}
		}
		s.append('$');
		return (s.toString());
	}
}
