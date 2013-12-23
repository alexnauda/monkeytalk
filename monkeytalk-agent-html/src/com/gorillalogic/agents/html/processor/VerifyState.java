package com.gorillalogic.agents.html.processor;


import org.junit.Assert;
import org.openqa.selenium.WebElement;
import static org.hamcrest.CoreMatchers.is;

import com.gorillalogic.monkeytalk.Command;

/**
 * TODO: Does it make sense to extend Assert...TestCase?
 * Add verify for different html elements.
 * Add Fields for assertion fail.
 * 
 */
public class VerifyState extends Assert {

	private boolean _success = false;
	private WebElement _we;
	private Command _cmd;
	
	public VerifyState(WebElement we, Command command) {
		_we = we;
		_cmd = command;
	}
	
	public boolean isSuccess() {
		return _success;
	}
	
	public void verify() {		
		// Text, Selected
		_success = false;
		try {
			String type = _we.getTagName();
			if(type.equalsIgnoreCase("input")) {
				String value = _we.getAttribute("value");
				String actual = _cmd.getArgsAsString();
				System.out.println("Verify Input -> Test Value: " + value + " Actual Value: " + actual);
				assertThat(value, is(actual));
				_success = true;
			}
		}
		catch(AssertionError e) {			
			_success = false;
			// Possible AssertionError 
			e.printStackTrace();			
		}		
	}
	
}
