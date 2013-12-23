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

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class TimingsTest extends BaseCommandTest {
	private static final String DEFAULT_TIMINGS =  "%thinktime="
			+ Command.DEFAULT_THINKTIME + " %timeout=" + Command.DEFAULT_TIMEOUT;
	
	@Test
	public void testGetDefaultTimings() {
		Command cmd = new Command();
		assertThat(cmd.getDefaultTimeout(), is(Command.DEFAULT_TIMEOUT));
		assertThat(cmd.getDefaultThinktime(), is(Command.DEFAULT_THINKTIME));
	}

	@Test
	public void testShowDefaultTimings() {
		Command cmd = new Command("Button OK Click");
		assertThat(cmd.getCommand(true), is("Button OK Click " + DEFAULT_TIMINGS));
	}
	
	@Test
	public void testHideDefaultTimings() {
		Command cmd = new Command("Button OK Click");
		assertThat(cmd.getCommand(), is("Button OK Click"));
	}
	
	@Test
	public void testCommandTimings() {
		Command cmd = new Command("Button OK Click %timeout=123 %thinktime=654");
		assertThat(cmd.getCommand(), is("Button OK Click %thinktime=654 %timeout=123"));
		assertThat(cmd.getCommand(true), is("Button OK Click %thinktime=654 %timeout=123"));
	}
	
	@Test
	public void testCustomDefaultTimings() {
		Command cmd = new Command("Button OK Click");
		cmd.setDefaultTimeout(123);
		cmd.setDefaultThinktime(654);
		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.getCommand(true), is("Button OK Click %thinktime=654 %timeout=123"));
	}
	
	@Test
	public void testCommandTimingsAndCustomDefaultTimings() {
		Command cmd = new Command("Button OK Click %timeout=123 %thinktime=654");
		cmd.setDefaultTimeout(123);
		cmd.setDefaultThinktime(654);
		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.getCommand(true), is("Button OK Click %thinktime=654 %timeout=123"));
	}
}