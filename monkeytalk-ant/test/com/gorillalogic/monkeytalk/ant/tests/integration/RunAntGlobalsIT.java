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
package com.gorillalogic.monkeytalk.ant.tests.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;

import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.utils.TestHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer;

public class RunAntGlobalsIT extends BuildFileTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18172;
	private CommandServer server;

	public RunAntGlobalsIT(String s) {
		super(s);
	}

	public void setUp() throws IOException {
		configureProject("resources/test/build.xml");
		getProject().setProperty("host", HOST);
		getProject().setProperty("port", Integer.toString(PORT));
		assertThat(getProjectDir().getAbsolutePath(),
				containsString("/monkeytalk-ant/resources/test"));

		server = new TestHelper().new CommandServer(PORT);
	}

	public void tearDown() {
		server.stop();
	}

	public void testGlobals() {
		// run the testGlobals target in resources/test/build.xml
		executeTarget("testGlobals");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getOutput(), containsString("www.gorillalogic.com"));
		assertThat(getLog(), containsString("-run: glob.mt"));
		assertThat(getLog(), containsString("Button 123 Tap \"Bo Bo\""));
		assertThat(getLog(), containsString("-end: glob.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap \"Bo Bo\""));
	}

	public void testGlobalsSingleQuote() {
		// run the testGlobalsSingleQuote target in resources/test/build.xml
		executeTarget("testGlobalsSingleQuote");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getOutput(), containsString("www.gorillalogic.com"));
		assertThat(getLog(), containsString("-run: glob.mt"));
		assertThat(getLog(), containsString("Button \"123 456\" Tap isn't"));
		assertThat(getLog(), containsString("-end: glob.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button \"123 456\" Tap isn't"));
	}

	public void testGlobalsWithBadArg() {
		// run the testGlobalsBadArg target in resources/test/build.xml
		executeTarget("testGlobalsBadArg");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getOutput(), containsString("www.gorillalogic.com"));
		assertThat(getLog(), containsString("-run: glob.mt"));
		assertThat(getLog(), containsString("Button 123 Tap ${bar}"));
		assertThat(getLog(), containsString("-end: glob.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap ${bar}"));
	}

	public void testGlobalsWithIllegalName() {
		// run the testGlobalsIllegalName target in resources/test/build.xml
		try {
			executeTarget("testGlobalsIllegalName");
		} catch (BuildException ex) {
			assertThat(getLog(), is("server=" + HOST + ":" + PORT));
			assertThat(getOutput(), is(""));
			assertThat(ex.getMessage(), is("illegal global variable '1foo' -- "
					+ Globals.ILLEGAL_MSG));
			assertThat(server.getCommands(), notNullValue());
			assertThat(server.getCommands().size(), is(0));
			return;
		}
		fail("should have thrown exception");
	}
}