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

public class RunAntAndroidIT extends BuildFileTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18173;
	private static final String ADB = "/bin/echo";
	private CommandServer server;

	public RunAntAndroidIT(String s) {
		super(s);
	}

	public void setUp() throws IOException {
		configureProject("resources/test3/build.xml");
		getProject().setProperty("host", HOST);
		getProject().setProperty("port", Integer.toString(PORT));
		getProject().setProperty("adb", ADB);
		assertThat(getProjectDir().getAbsolutePath(),
				containsString("/monkeytalk-ant/resources/test3"));

		server = new TestHelper().new CommandServer(PORT);
	}

	public void tearDown() {
		server.stop();
	}

	/**
	 * run the testSimpleAndroid target in resources/test3/build.xml
	 */
	public void testSimpleAndroid() {
		executeTarget("testSimpleAndroid");
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

	/**
	 * run the testSimpleAndroidEmulator target in resources/test3/build.xml
	 */
	public void testSimpleAndroidEmulator() {
		executeTarget("testSimpleAndroidEmulator");
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

	/**
	 * run the testSimpleAndroidSerial target in resources/test3/build.xml
	 */
	public void testSimpleAndroidSerial() {
		executeTarget("testSimpleAndroidSerial");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("myserial: -run: simple.mt"));
		assertThat(getLog(), containsString("myserial:   Button SIMPLE Tap"));
		assertThat(getLog(), containsString("myserial: -end: simple.mt"));
		assertThat(getOutput(), containsString("result: OK"));
		assertThat(getLog(), containsString("...done"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SIMPLE Tap"));
	}

	/**
	 * run the testSimpleAndroidSerialRemote target in resources/test3/build.xml
	 */
	public void testSimpleAndroidSerialRemote() {
		executeTarget("testSimpleAndroidSerialRemote");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("foohost:4321: -run: simple.mt"));
		assertThat(getLog(), containsString("foohost:4321:   Button SIMPLE Tap"));
		assertThat(getLog(), containsString("foohost:4321: -end: simple.mt"));
		assertThat(getOutput(), containsString("result: OK"));
		assertThat(getLog(), containsString("...done"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SIMPLE Tap"));
	}

	/**
	 * run the testParallel target in resources/test3/build.xml
	 */
	public void testParallel() {
		executeTarget("testParallel");
		assertThat(getLog(), containsString("server=" + HOST + ":" + PORT));
		assertThat(getLog(), containsString("running suite mysuite.mts..."));

		assertThat(getLog(), containsString("myserial1: -start suite (29 tests)"));
		assertThat(getLog(), containsString("myserial1:   1 : login.mt[usr='Héìíô']"));
		assertThat(getLog(), containsString("myserial1:   2 : login.mt[usr='Arrow \u21DD\u21DC']"));
		assertThat(getLog(), containsString("myserial1:   3 : login.mt[usr='Bo Bo']"));
		assertThat(getLog(), containsString("myserial1:   4 : login.mt[usr='Able']"));
		assertThat(getLog(), containsString("myserial1:   5 : login.mt[usr='Baker']"));
		assertThat(getLog(), containsString("myserial1:   6 : login.mt[usr='Charile']"));
		assertThat(getLog(), containsString("myserial1:   7 : login.mt[usr='Dogg']"));
		assertThat(getLog(), containsString("myserial1:   8 : login.mt[usr='Edgar']"));
		assertThat(getLog(), containsString("myserial1:   9 : login.mt[usr='Fred']"));
		assertThat(getLog(), containsString("myserial1:   10 : login.mt[usr='Gorilla']"));
		assertThat(getLog(), containsString("myserial1:   11 : login.mt[usr='Hburg']"));
		assertThat(getLog(), containsString("myserial1:   12 : login.mt[usr='Igor']"));
		assertThat(getLog(), containsString("myserial1:   13 : login.mt[usr='Joe']"));
		assertThat(getLog(), containsString("myserial1:   14 : login.mt[usr='Kappa']"));
		assertThat(getLog(), containsString("myserial1:   15 : login.mt[usr='Lemon']"));
		assertThat(getLog(), containsString("myserial1:   16 : login.mt[usr='Manny']"));
		assertThat(getLog(), containsString("myserial1:   17 : login.mt[usr='Number']"));
		assertThat(getLog(), containsString("myserial1:   18 : login.mt[usr='Octopus']"));
		assertThat(getLog(), containsString("myserial1:   19 : login.mt[usr='Pop']"));
		assertThat(getLog(), containsString("myserial1:   20 : login.mt[usr='Quinn']"));
		assertThat(getLog(), containsString("myserial1:   21 : login.mt[usr='Roger']"));
		assertThat(getLog(), containsString("myserial1:   22 : login.mt[usr='Sammy']"));
		assertThat(getLog(), containsString("myserial1:   23 : login.mt[usr='Test']"));
		assertThat(getLog(), containsString("myserial1:   24 : login.mt[usr='Umbra']"));
		assertThat(getLog(), containsString("myserial1:   25 : login.mt[usr='Vulture']"));
		assertThat(getLog(), containsString("myserial1:   26 : login.mt[usr='Water']"));
		assertThat(getLog(), containsString("myserial1:   27 : login.mt[usr='Xray']"));
		assertThat(getLog(), containsString("myserial1:   28 : login.mt[usr='Yellow']"));
		assertThat(getLog(), containsString("myserial1:   29 : login.mt[usr='Zapp']"));
		assertThat(getLog(), containsString("myserial1: -end suite"));

		assertThat(getLog(), containsString("myserial2: -start suite (29 tests)"));
		assertThat(getLog(), containsString("myserial2:   1 : login.mt[usr='Héìíô']"));
		assertThat(getLog(), containsString("myserial2:   2 : login.mt[usr='Arrow \u21DD\u21DC']"));
		assertThat(getLog(), containsString("myserial2:   3 : login.mt[usr='Bo Bo']"));
		assertThat(getLog(), containsString("myserial2:   4 : login.mt[usr='Able']"));
		assertThat(getLog(), containsString("myserial2:   5 : login.mt[usr='Baker']"));
		assertThat(getLog(), containsString("myserial2:   6 : login.mt[usr='Charile']"));
		assertThat(getLog(), containsString("myserial2:   7 : login.mt[usr='Dogg']"));
		assertThat(getLog(), containsString("myserial2:   8 : login.mt[usr='Edgar']"));
		assertThat(getLog(), containsString("myserial2:   9 : login.mt[usr='Fred']"));
		assertThat(getLog(), containsString("myserial2:   10 : login.mt[usr='Gorilla']"));
		assertThat(getLog(), containsString("myserial2:   11 : login.mt[usr='Hburg']"));
		assertThat(getLog(), containsString("myserial2:   12 : login.mt[usr='Igor']"));
		assertThat(getLog(), containsString("myserial2:   13 : login.mt[usr='Joe']"));
		assertThat(getLog(), containsString("myserial2:   14 : login.mt[usr='Kappa']"));
		assertThat(getLog(), containsString("myserial2:   15 : login.mt[usr='Lemon']"));
		assertThat(getLog(), containsString("myserial2:   16 : login.mt[usr='Manny']"));
		assertThat(getLog(), containsString("myserial2:   17 : login.mt[usr='Number']"));
		assertThat(getLog(), containsString("myserial2:   18 : login.mt[usr='Octopus']"));
		assertThat(getLog(), containsString("myserial2:   19 : login.mt[usr='Pop']"));
		assertThat(getLog(), containsString("myserial2:   20 : login.mt[usr='Quinn']"));
		assertThat(getLog(), containsString("myserial2:   21 : login.mt[usr='Roger']"));
		assertThat(getLog(), containsString("myserial2:   22 : login.mt[usr='Sammy']"));
		assertThat(getLog(), containsString("myserial2:   23 : login.mt[usr='Test']"));
		assertThat(getLog(), containsString("myserial2:   24 : login.mt[usr='Umbra']"));
		assertThat(getLog(), containsString("myserial2:   25 : login.mt[usr='Vulture']"));
		assertThat(getLog(), containsString("myserial2:   26 : login.mt[usr='Water']"));
		assertThat(getLog(), containsString("myserial2:   27 : login.mt[usr='Xray']"));
		assertThat(getLog(), containsString("myserial2:   28 : login.mt[usr='Yellow']"));
		assertThat(getLog(), containsString("myserial2:   29 : login.mt[usr='Zapp']"));
		assertThat(getLog(), containsString("myserial2: -end suite"));

		assertThat(getOutput(), containsString("result: OK"));
		assertThat(getLog(), containsString("...done"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().get(0).getCommand(), is("Input username EnterText Héìíô"));
		assertThat(server.getCommands().get(server.getCommands().size() - 1).getCommand(),
				is("Button LOGOUT Tap %thinktime=1000"));
	}
}