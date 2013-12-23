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

import org.apache.tools.ant.BuildFileTest;

import com.gorillalogic.monkeytalk.utils.TestHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer;

public class RunAntIT extends BuildFileTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18172;
	private CommandServer server;

	public RunAntIT(String s) {
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

	public void testSimple() {
		// run the testSimple target in resources/test/build.xml
		executeTarget("testSimple");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("-run: simple.mt"));
		assertThat(getLog(), containsString("Button SIMPLE Tap"));
		assertThat(getLog(), containsString("-end: simple.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SIMPLE Tap"));
	}

	public void testScript() {
		// run the testScript target in resources/test/build.xml
		executeTarget("testScript");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("-run: myscript.mt"));
		assertThat(getLog(), containsString("Script login.mt Run MYUSER MYPASS"));
		assertThat(getLog(), containsString("---run: login.mt"));
		assertThat(getLog(), containsString("Vars * Define usr pwd"));
		assertThat(getLog(), containsString("Input username EnterText MYUSER"));
		assertThat(getLog(), containsString("Input password EnterText MYPASS"));
		assertThat(getLog(), containsString("Button LOGIN Tap"));
		assertThat(getLog(), containsString("Script logout.mt Run MYUSER"));
		assertThat(getLog(), containsString("-----run: logout.mt"));
		assertThat(getLog(), containsString("Vars * Define usr"));
		assertThat(getLog(), containsString("Button LOGOUT Verify %timeout=3000"));
		assertThat(getLog(), containsString("Label * Verify \"Welcome, MYUSER!\""));
		assertThat(getLog(), containsString("Button LOGOUT Tap"));
		assertThat(getLog(), containsString("-----end: logout.mt"));
		assertThat(getLog(), containsString("---end: login.mt"));
		assertThat(getLog(), containsString("-end: myscript.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(), is("Input username EnterText MYUSER"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input password EnterText MYPASS"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * Verify \"Welcome, MYUSER!\""));
		assertThat(server.getCommands().get(5).getCommand(), is("Button LOGOUT Tap"));
	}

	public void testDebugPrint() {
		// run the testDebugPrint target in resources/test/build.xml
		executeTarget("testDebugPrint");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("-run: debug.mt"));
		assertThat(getLog(), containsString("Button FOO Tap"));
		assertThat(getLog(), containsString("foo bar baz"));
		assertThat(getLog(), containsString("Button BAR Tap"));
		assertThat(getLog(), containsString("-end: debug.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
	}

	public void testVerbose() {
		// run the testVerbose target in resources/test/build.xml
		executeTarget("testVerbose");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("running script simple.mt..."));
		assertThat(getLog(), containsString("-run: simple.mt"));
		assertThat(getLog(), containsString("Button SIMPLE Tap"));
		assertThat(getLog(), containsString("-end: simple.mt"));
		assertThat(getOutput(), containsString("result: OK"));
		assertThat(getLog(), containsString("...done"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SIMPLE Tap"));
	}

	public void testSuite() {
		// run the testSuite target in resources/test/build.xml
		executeTarget("testSuite");

		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getOutput(), containsString("www.gorillalogic.com"));
		assertThat(getLog(), containsString("-start suite (3 tests)"));
		assertThat(getLog(), containsString("1 : login.mt[usr='justin' pwd='password']"));
		assertThat(getLog(), containsString("-> OK"));
		assertThat(getLog(), containsString("2 : login.mt[usr='bo bo' pwd='password']"));
		assertThat(getLog(), containsString("-> OK"));
		assertThat(getLog(), containsString("3 : login.mt[usr='charlie' pwd='pass WORD']"));
		assertThat(getLog(), containsString("-> OK"));
		assertThat(getLog(), containsString("-end suite"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(18));
		assertThat(server.getCommands().get(0).getCommand(), is("Input username EnterText justin"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * Verify \"Welcome, justin!\""));
		assertThat(server.getCommands().get(5).getCommand(), is("Button LOGOUT Tap"));

		assertThat(server.getCommands().get(6).getCommand(),
				is("Input username EnterText \"bo bo\""));
		assertThat(server.getCommands().get(7).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(9).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(10).getCommand(),
				is("Label * Verify \"Welcome, bo bo!\""));
		assertThat(server.getCommands().get(11).getCommand(), is("Button LOGOUT Tap"));

		assertThat(server.getCommands().get(12).getCommand(),
				is("Input username EnterText charlie"));
		assertThat(server.getCommands().get(13).getCommand(),
				is("Input password EnterText \"pass WORD\""));
		assertThat(server.getCommands().get(14).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(15).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(16).getCommand(),
				is("Label * Verify \"Welcome, charlie!\""));
		assertThat(server.getCommands().get(17).getCommand(), is("Button LOGOUT Tap"));
	}

	public void testSuiteVerbose() {
		// run the testSuiteVerbose target in resources/test/build.xml
		executeTarget("testSuiteVerbose");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getOutput(), containsString("www.gorillalogic.com"));
		assertThat(getLog(), containsString("running suite mysuite.mts..."));
		assertThat(getLog(), containsString("-start suite (3 tests)"));
		assertThat(getLog(), containsString("1 : login.mt[usr='justin' pwd='password']"));
		assertThat(getLog(), containsString("Vars * Define usr pwd"));
		assertThat(getLog(), containsString("Input username EnterText justin"));
		assertThat(getLog(), containsString("Input password EnterText password"));
		assertThat(getLog(), containsString("Button LOGIN Tap"));
		assertThat(getLog(), containsString("Script logout.mt Run justin"));
		assertThat(getLog(), containsString("Vars * Define usr"));
		assertThat(getLog(), containsString("Button LOGOUT Verify %timeout=3000"));
		assertThat(getLog(), containsString("Label * Verify \"Welcome, justin!\""));
		assertThat(getLog(), containsString("Button LOGOUT Tap"));
		assertThat(getLog(), containsString("-> OK"));
		assertThat(getLog(), containsString("2 : login.mt[usr='bo bo' pwd='password']"));
		assertThat(getLog(), containsString("Vars * Define usr pwd"));
		assertThat(getLog(), containsString("Input username EnterText \"bo bo\""));
		assertThat(getLog(), containsString("Input password EnterText password"));
		assertThat(getLog(), containsString("Button LOGIN Tap"));
		assertThat(getLog(), containsString("Script logout.mt Run \"bo bo\""));
		assertThat(getLog(), containsString("Vars * Define usr"));
		assertThat(getLog(), containsString("Button LOGOUT Verify %timeout=3000"));
		assertThat(getLog(), containsString("Label * Verify \"Welcome, bo bo!\""));
		assertThat(getLog(), containsString("Button LOGOUT Tap"));
		assertThat(getLog(), containsString("-> OK"));
		assertThat(getLog(), containsString("3 : login.mt[usr='charlie' pwd='pass WORD']"));
		assertThat(getLog(), containsString("Input username EnterText charlie"));
		assertThat(getLog(), containsString("Input password EnterText \"pass WORD\""));
		assertThat(getLog(), containsString("Button LOGIN Tap"));
		assertThat(getLog(), containsString("Script logout.mt Run charlie"));
		assertThat(getLog(), containsString("Vars * Define usr"));
		assertThat(getLog(), containsString("Button LOGOUT Verify %timeout=3000"));
		assertThat(getLog(), containsString("Label * Verify \"Welcome, charlie!\""));
		assertThat(getLog(), containsString("Button LOGOUT Tap"));
		assertThat(getLog(), containsString("-> OK"));
		assertThat(getLog(), containsString("-end suite"));
		assertThat(getOutput(), containsString("result: OK"));
		assertThat(getLog(), containsString("...done"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(18));
		assertThat(server.getCommands().get(0).getCommand(), is("Input username EnterText justin"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * Verify \"Welcome, justin!\""));
		assertThat(server.getCommands().get(5).getCommand(), is("Button LOGOUT Tap"));

		assertThat(server.getCommands().get(6).getCommand(),
				is("Input username EnterText \"bo bo\""));
		assertThat(server.getCommands().get(7).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(9).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(10).getCommand(),
				is("Label * Verify \"Welcome, bo bo!\""));
		assertThat(server.getCommands().get(11).getCommand(), is("Button LOGOUT Tap"));

		assertThat(server.getCommands().get(12).getCommand(),
				is("Input username EnterText charlie"));
		assertThat(server.getCommands().get(13).getCommand(),
				is("Input password EnterText \"pass WORD\""));
		assertThat(server.getCommands().get(14).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(15).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(16).getCommand(),
				is("Label * Verify \"Welcome, charlie!\""));
		assertThat(server.getCommands().get(17).getCommand(), is("Button LOGOUT Tap"));
	}

	public void testCommandText() {
		// run the testSimpleCommandText target in resources/test/build.xml
		executeTarget("testCommandText");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("Button SIMPCMDTEXT Tap"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SIMPCMDTEXT Tap"));
	}
}