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
package com.gorillalogic.monkeytalk.api.impl.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.impl.ScriptErrorException;
import com.gorillalogic.monkeytalk.api.impl.ScriptImpl;

public class ScriptImplTest {

	@Test
	public void testConstructor() {
		ScriptImpl s = new ScriptImpl("host", 1234, "foo.mt");
		assertThat(s, notNullValue());
		assertThat(s.getApp().getHost(), is("host"));
		assertThat(s.getApp().getPort(), is(1234));
		assertThat(s.getMonkeyId(), is("foo.mt"));
		assertThat(s.toString(), containsString("Script: url=host:1234"));
	}

	@Test
	public void testRun() {
		try {
			ScriptImpl s = new ScriptImpl("host", 1234, "foo.mt");
			s.run("arg1", "arg2");
		} catch (ScriptErrorException ex) {
			assertThat(ex.getMessage(), is("ScriptError: run() not implemented"));
			return;
		}
		fail("exception should have been thrown");
	}

	@Test
	public void testRunWith() {
		try {
			ScriptImpl s = new ScriptImpl("host", 1234, "foo.mt");
			s.runWith("arg1", "arg2");
		} catch (ScriptErrorException ex) {
			assertThat(ex.getMessage(), is("ScriptError: runWith() not implemented"));
			return;
		}
		fail("exception should have been thrown");
	}

	@Ignore("monkeytalk-api-impl is deprecated")
	@Test
	public void testScriptRun() throws IOException {
		try {
			ScriptImpl s = new ScriptImpl("host", 1234, "foo.mt");
			s.run(Arrays.asList("arg1", "arg2", "arg3"), null);
		} catch (ScriptErrorException ex) {
			assertThat(ex.getMessage(), is("ScriptError: script 'foo.mt' not found"));
			return;
		}
		fail("exception should have been thrown");
	}

	@Ignore("monkeytalk-api-impl is deprecated")
	@Test
	public void testScriptRunParts() throws IOException {
		try {
			ScriptImpl s = new ScriptImpl("host", 1234, "foo.mt");
			s.run("Script", "Run", Arrays.asList("arg1", "arg2", "arg3"), null);
		} catch (ScriptErrorException ex) {
			assertThat(ex.getMessage(), is("ScriptError: script 'foo.mt' not found"));
			return;
		}
		fail("exception should have been thrown");
	}

	@Ignore("monkeytalk-api-impl is deprecated")
	@Test
	public void testScriptRunWith() throws IOException {
		try {
			ScriptImpl s = new ScriptImpl("host", 1234, "foo.mt");
			s.runWith("data.csv", null);
		} catch (ScriptErrorException ex) {
			assertThat(ex.getMessage(), is("ScriptError: datafile 'data.csv' not found"));
			return;
		}
		fail("exception should have been thrown");
	}
}