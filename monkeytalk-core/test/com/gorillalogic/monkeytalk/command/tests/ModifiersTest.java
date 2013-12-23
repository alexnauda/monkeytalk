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
package com.gorillalogic.monkeytalk.command.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class ModifiersTest extends BaseCommandTest {

	@Test
	public void testGetTimingsWithModifiers() {
		Command cmd = new Command("Button OK Click %timeout=123 %thinktime=654 %retrydelay=777");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("123", "654", "777"));
		assertThat(cmd.getModifiers().keySet(), hasItems("timeout", "thinktime", "retrydelay"));

		assertThat(cmd.getTimeout(), is(123));
		assertThat(cmd.getThinktime(), is(654));
		assertThat(cmd.getRetryDelay(), is(777));
	}

	@Test
	public void testGetTimingsWithNoModifiers() {
		Command cmd = new Command("Button OK Click");

		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);

		assertThat(cmd.getTimeout(), is(Command.DEFAULT_TIMEOUT));
		assertThat(cmd.getThinktime(), is(Command.DEFAULT_THINKTIME));
		assertThat(cmd.getRetryDelay(), is(Command.DEFAULT_RETRYDELAY));
	}

	@Test
	public void testGetTimingsWithMissingModifiers() {
		Command cmd = new Command("Button OK Click %foo=123 %bar=654");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));

		assertThat(cmd.getTimeout(), is(Command.DEFAULT_TIMEOUT));
		assertThat(cmd.getThinktime(), is(Command.DEFAULT_THINKTIME));
		assertThat(cmd.getRetryDelay(), is(Command.DEFAULT_RETRYDELAY));
	}

	@Test
	public void testGetTimingsWithBadModifiers() {
		Command cmd = new Command(
				"Button OK Click %timeout=123.45 %thinktime=foo %retrydelay=\"some val\"");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("123.45", "foo", "some val"));
		assertThat(cmd.getModifiers().keySet(), hasItems("timeout", "thinktime", "retrydelay"));

		assertThat(cmd.getTimeout(), is(12345));
		assertThat(cmd.getThinktime(), is(Command.DEFAULT_THINKTIME));
		assertThat(cmd.getRetryDelay(), is(Command.DEFAULT_RETRYDELAY));
	}

	@Test
	public void testGetTimingsWithNumericStringModifiers() {
		Command cmd = new Command(
				"Button OK Click %timeout=123a %thinktime=654b %retrydelay=\"777 x\"");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("123a", "654b", "777 x"));
		assertThat(cmd.getModifiers().keySet(), hasItems("timeout", "thinktime", "retrydelay"));

		assertThat(cmd.getTimeout(), is(123));
		assertThat(cmd.getThinktime(), is(654));
		assertThat(cmd.getRetryDelay(), is(777));
	}

	@Test
	public void testGetTimingsWithRepeatedModifiers() {
		Command cmd = new Command("Button OK Click %timeout=123 %timeout=456 %timeout=789");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("789"));
		assertThat(cmd.getModifiers().keySet(), hasItems("timeout"));

		assertThat(cmd.getTimeout(), is(789));
	}

	@Test
	public void testGetScreenshotOnErrorTrue() {
		Command cmd = new Command("Button OK Click %screenshotonerror=true");

		assertThat(cmd.getCommand(),is("Button OK Click %screenshotonerror=true"));
		assertCommand(cmd);
		assertCommand(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("true"));
		assertThat(cmd.getModifiers().keySet(), hasItems(Command.SCREENSHOT_ON_ERROR));
		assertThat(cmd.isScreenshotOnError(), is(true));
	}

	@Test
	public void testGetScreenshotOnErrorFalse() {
		Command cmd = new Command("Button OK Click %screenshotonerror=false");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("false"));
		assertThat(cmd.getModifiers().keySet(), hasItems("screenshotonerror"));
		assertThat(cmd.isScreenshotOnError(), is(false));
	}
	
	@Test
	public void testGetScreenshotOnErrorNakedDefaultsToTrue() {
		// naked mod is NOT a mod, it is an arg
		Command cmd = new Command("Button OK Click %screenshotonerror");
		
		assertCommand(cmd);
		assertNoModifiers(cmd);
		assertThat(cmd.isScreenshotOnError(), is(true));
	}

	@Test
	public void testGetScreenshotOnErrorDefaultsToTrue() {
		Command cmd = new Command("Button OK Click %screenshotonerror=unknown");

		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("unknown"));
		assertThat(cmd.getModifiers().keySet(), hasItems("screenshotonerror"));
		assertThat(cmd.isScreenshotOnError(), is(true));
	}
	
	@Test
	public void testGetScreenshotOnErrorSetter() {
		Command cmd = new Command("Button OK Click");
		
		assertThat(cmd.getCommand(),is("Button OK Click"));
		assertThat(cmd.isScreenshotOnError(), is(true));
		
		cmd.setScreenshotOnError(false);
		assertThat(cmd.getCommand(),is("Button OK Click %screenshotonerror=false"));
		assertThat(cmd.isScreenshotOnError(), is(false));
		
		cmd.setScreenshotOnError(true);
		assertThat(cmd.getCommand(),is("Button OK Click %screenshotonerror=true"));
		assertThat(cmd.isScreenshotOnError(), is(true));
	}
	
	@Test
	public void testIgnoredTrue() {
		Command cmd = new Command("Button OK Click %ignore=true");
		
		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("true"));
		assertThat(cmd.getModifiers().keySet(), hasItems(Command.IGNORE_MODIFIER));
		assertThat(cmd.isIgnored(), is(true));
	}
	
	@Test
	public void testIgnoredFalse() {
		Command cmd = new Command("Button OK Click %ignore=false");
		
		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("false"));
		assertThat(cmd.getModifiers().keySet(), hasItems("ignore"));
		assertThat(cmd.isIgnored(), is(false));
	}
	
	@Test
	public void testIgnoredValue() {
		Command cmd = new Command("Button OK Click %ignore=value");
		
		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("value"));
		assertThat(cmd.getModifiers().keySet(), hasItems("ignore"));
		assertThat(cmd.isIgnored(), is(false));
		assertThat(cmd.isIgnored("value"), is(true));
		assertThat(cmd.isIgnored("val"), is(true));
		assertThat(cmd.isIgnored("ue"), is(true));
		assertThat(cmd.isIgnored("unknown"), is(false));
		assertThat(cmd.isIgnored(null), is(false));
	}
	
	@Test
	public void testShouldFailTrue() {
		Command cmd = new Command("Button OK Click %shouldfail=true");
		
		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("true"));
		assertThat(cmd.getModifiers().keySet(), hasItems(Command.SHOULD_FAIL_MODIFIER));
		assertThat(cmd.shouldFail(), is(true));
	}
	
	@Test
	public void testShouldFailFalse() {
		Command cmd = new Command("Button OK Click %shouldfail=false");
		
		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("false"));
		assertThat(cmd.getModifiers().keySet(), hasItems("shouldfail"));
		assertThat(cmd.shouldFail(), is(false));
	}
	
	@Test
	public void testShouldFailDefaultsToFalse() {
		Command cmd = new Command("Button OK Click %shouldfail=unknown");
		
		assertCommand(cmd);
		assertNoArgs(cmd);
		assertThat(cmd.getModifiers().values(), hasItems("unknown"));
		assertThat(cmd.getModifiers().keySet(), hasItems("shouldfail"));
		assertThat(cmd.shouldFail(), is(false));
	}
	
	@Test
	public void testModifierDefaults() {
		Command cmd = new Command("Button OK Click");
		
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
		assertThat(cmd.isScreenshotOnError(), is(true));
		assertThat(cmd.isIgnored(), is(false));
		assertThat(cmd.isIgnored(null), is(false));
		assertThat(cmd.shouldFail(), is(false));
	}
}