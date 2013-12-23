package com.gorillalogic.agents.html.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;


import com.gorillalogic.agents.html.processor.CommandResponse;
import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;

/**
 * TODO: Abstract commands at test setup
 *
 */
public class FirefoxTest extends HtmlTestHelper {

	ScriptProcessor scp = new ScriptProcessor(new File("."), AgentManager.getAgent("Firefox"));

	@Test
	public void testTestPage() {			
		/**
		 * The command has different properties that are not applicable.
		 * You have a setup phase to load the browser, page to his and times.
		 */
		PlaybackResult result = scp.runScript("testPage.mt");
	    Assert.assertEquals(result.getMessage(), PlaybackStatus.OK, result.getStatus());
	    
	}
	
//	@Test
//	public void testGoodVerify() {
//		Command [] commands = new Command[]{	
//	    		new Command("Browser firefox setBrowser file:///" + getTestResource("html-content.html").getAbsolutePath() + " %thinktime=500 %timeout=2000"),
//	    		new Command("input input-text-1 clear"), // Clear input first
//	    		new Command("input input-text-1 enterText hello %thinktime=500 %timeout=2000"),
//	    		new Command("input input-text-1 verify hello %thinktime=500 %timeout=2000")
//		};
//		for(int i = 0; i < commands.length; i++){	    	
//	    	CommandResponse cResp = scp.processACommand(commands[i]);
//	    	if(i == 3) {
//	    		assertThat(true, is(cResp.isVerify()));
//	    		assertThat(true, is(cResp.getVerify().isSuccess()));
//	    		assertThat(true, is(cResp.isSuccess()));
//	    	}
//	    }
//		
//	}
//	
//	@Test
//	public void testFailVerify() {
//		// Test verify FAIL
//		Command [] commands = new Command[]{	
//	    		new Command("Browser firefox setBrowser file:///" + getTestResource("html-content.html").getAbsolutePath() + " %thinktime=500 %timeout=2000"),
//	    		new Command("input input-text-1 clear"), // Clear input first
//	    		new Command("input input-text-1 enterText hello %thinktime=500 %timeout=2000"),
//	    		new Command("input input-text-1 verify hello %thinktime=500 %timeout=2000")
//		};
//		for(int j = 0; j < commands.length; j++){	    	
//	    	CommandResponse cResp = scp.processACommand(commands[j]);
//	    	if(j == 3) {
//	    		assertThat(true, is(cResp.isVerify()));
//	    		assertThat(false, is(cResp.getVerify().isSuccess()));
//	    		assertThat(false, is(cResp.isSuccess()));
//	    	}
//	    }
//	}
}
