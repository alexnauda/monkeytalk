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
package com.gorillalogic.monkeytalk.runner.tests.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;

public class RunnerIT extends BaseHelper {
	private static final int PORT = 18171;

	@Test
	public void testRunVersion() throws Exception {
		String out = run("-version");
		assertThat(out, containsString("MonkeyTalk v"));
		assertThat(out, containsString("www.gorillalogic.com"));
	}

	@Test
	public void testRunHelp() throws Exception {
		String out = run("-help");
		assertThat(out, containsString("MonkeyTalk v"));
		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("print help and exit"));
	}

	@Test
	public void testRunScript() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + foo.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("result: OK"));
	}

	@Test
	public void testRunSuite() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + suite.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("running suite : 2 tests"));
		assertThat(out, containsString("1 : foo.mt"));
		assertThat(out, containsString("test result: OK"));
		assertThat(out, containsString("2 : bar.mt"));
		assertThat(out, containsString("test result: OK"));
		assertThat(out, containsString("result: OK"));
	}

	@Test
	public void testRunWithEmptyArgs() throws Exception {
		String out = run("");

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("ERROR: Bad commandline args"));
		assertThat(out, not(containsString("print version and exit")));
	}

	@Test
	public void testRunWithBadAgent() throws Exception {
		String out = run("-agent BADAGENT");

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("ERROR: Unable to find agent BADAGENT"));
		assertThat(out, containsString("print version and exit"));
	}

	@Test
	public void testRunWithNoScript() throws Exception {
		String out = run("-agent ios");

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("ERROR: You must specify a script or suite to run"));
		assertThat(out, containsString("print version and exit"));
	}

	@Test
	public void testRunWithMissingScript() throws Exception {
		String out = run("-agent ios missing.mt");

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("ERROR: Bad input script. File not found"));
		assertThat(out, containsString("print version and exit"));
	}

	@Test
	public void testJavacript() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nInput name EnterText \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		File fooJS = new File(dir, "foo.js");
		assertThat(fooJS.exists() && fooJS.isFile(), is(true));

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + fooJS.getAbsolutePath() + " -verbose");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("Button FOO tap -> OK"));
		assertThat(out, containsString("Input name enterText \"Bo Bo\" -> OK"));
	}

	@Test
	public void testQuietScript() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " -quiet " + foo.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(out, is(""));
	}

	@Test
	public void testVerboseScript() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + foo.getAbsolutePath() + " -verbose");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("Button FOO Tap -> OK"));
		assertThat(out, containsString("result: OK"));
	}

	@Test
	public void testQuietSuite() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + suite.getAbsolutePath() + " -quiet");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		assertThat(out, is(""));
	}

	@Test
	public void testVerboseSuite() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + suite.getAbsolutePath() + " -verbose");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("running suite : 2 tests"));
		assertThat(out, containsString("1 : foo.mt"));
		assertThat(out, containsString("Button FOO Tap -> OK"));
		assertThat(out, containsString("Button FOO2 Tap -> OK"));
		assertThat(out, containsString("Button FOO3 Tap -> OK"));
		assertThat(out, containsString("test result: OK"));
		assertThat(out, containsString("2 : bar.mt"));
		assertThat(out, containsString("Button BAR Tap -> OK"));
		assertThat(out, containsString("test result: OK"));
		assertThat(out, containsString("result: OK"));
	}

	@Ignore
	@Test
	public void testScriptWithoutAdb() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent AndroidEmulator -port " + PORT + " " + foo.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		assertThat(
				out,
				containsString("AndroidEmulator - you must specify adb to run on the Android Emulator or on a tethered Android device"));
	}

	@Test
	public void testMultipleScripts() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);
		File bar = tempScript("bar.mt", "Button BAR Tap", dir);
		File baz = tempScript("baz.mt", "Button BAZ Tap", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + foo.getAbsolutePath() + " "
				+ bar.getAbsolutePath() + " " + baz.getAbsolutePath() + " -verbose");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("Button FOO Tap -> OK"));
		assertThat(out, containsString("Button BAR Tap -> OK"));
		assertThat(out, containsString("Button BAZ Tap -> OK"));
	}

	@Test
	public void testScriptWithUTF8() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Héìíô Tap", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " " + foo.getAbsolutePath() + " -verbose");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Héìíô Tap"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("Button Héìíô Tap -> OK"));
		assertThat(out, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithThinktimeAndTimeout() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap %timeout=2345", dir);

		CommandServer server = new CommandServer(PORT);
		String out = run("-agent iOS -port " + PORT + " -timeout 1234 -thinktime 567 "
				+ foo.getAbsolutePath() + " -verbose");
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %thinktime=567 %timeout=1234"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Button BAR Tap %thinktime=567 %timeout=2345"));

		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("Button FOO Tap -> OK"));
		assertThat(out, containsString("Button BAR Tap %timeout=2345 -> OK"));
		assertThat(out, containsString("result: OK"));
	}
}