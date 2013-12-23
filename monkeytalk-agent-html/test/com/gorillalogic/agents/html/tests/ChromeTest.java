package com.gorillalogic.agents.html.tests;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;
import com.gorillalogic.monkeytalk.Command;


public class ChromeTest extends  HtmlTestHelper {
	
	@Test
	public void testChrome() {
		
		SeleniumCommandProcessor scp = new SeleniumCommandProcessor();
	    Command [] commands = new Command[] {
			new Command("Browser chrome setBrowser file:///" + getTestResource("html-content.html").getAbsolutePath() + "  %thinktime=500 %timeout=2000"),
    		new Command("input input-text-1 clear"), // Clear input first
    		new Command("input input-text-1 enterText chrome-test %thinktime=500 %timeout=2000"),
    		new Command("input xpath=//*[@id='input-text-1'] enterText -helloworld %thinktime=500 %timeout=2000"),
    		new Command("input xpath=//*[@id='input-text-2'] clear"), // Clear input first
    		new Command("input input-text-2 enterText hello %thinktime=500 %timeout=2000")	
	    };	    
	    
	    for(int i = 0; i < commands.length; i++){
	    	scp.processACommand(commands[i]);
	    }
	    
	    WebElement in1 = scp.driver.findElement(By.id("input-text-1"));	
	    assertEquals("chrome-test-helloworld", in1.getAttribute("value"));
	    WebElement in2 = scp.driver.findElement(By.id("input-text-2"));	
	    assertEquals( "hello", in2.getAttribute("value"));	    
	}

}
