package com.gorillalogic.agents.html;

import com.gorillalogic.agents.html.processor.SeleniumCommandProcessor;
import com.gorillalogic.monkeytalk.Command;

public class WebTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SeleniumCommandProcessor scp = new SeleniumCommandProcessor();
	    Command [] commands = new Command[]{
	    		new Command("Browser firefox * file:///Users/markarroyo/projects/trunk/monkeytalk-agent-html/test.html %thinktime=500 %timeout=2000"),
	    		new Command("input afield1 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input xpath=//*[@id='afield1'] enterText hello333 %thinktime=500 %timeout=2000"),
	    		new Command("input afield2 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield3 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield4 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield5 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield6 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield7 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield8 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield9 enterText hello %thinktime=500 %timeout=2000"),
	    		new Command("input afield10 enterText hello %thinktime=500 %timeout=2000")
	    };
	    for(int i = 0; i < commands.length; i++){
	    	scp.processACommand(commands[i]);
	    }

	}

}
