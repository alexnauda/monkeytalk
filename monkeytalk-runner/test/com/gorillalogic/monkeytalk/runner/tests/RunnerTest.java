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
package com.gorillalogic.monkeytalk.runner.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.runner.Runner;

public class RunnerTest extends BaseHelper {

	@Test
	public void testScript() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT),
				script.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OK Click"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), suite.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("running suite : 2 tests"));
		assertThat(output.toString(), containsString("1 : foo.mt"));
		assertThat(output.toString(), containsString("test result: OK"));
		assertThat(output.toString(), containsString("2 : bar.mt"));
		assertThat(output.toString(), containsString("test result: OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testWithNullArgs() {
		Runner.main(null);
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("\n\nERROR: Bad commandline args\n\n"));
		assertThat(output.toString(), not(containsString("print version and exit")));
	}

	@Test
	public void testWithEmptyArgs() {
		Runner.main(new String[] {});
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("\n\nERROR: Bad commandline args\n\n"));
		assertThat(output.toString(), not(containsString("print version and exit")));
	}

	@Test
	public void testScriptWithBadAgent() {
		Runner.main(new String[] { "-agent", "BADAGENT" });
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(),
				containsString("\n\nERROR: Unable to find agent BADAGENT\n\n"));
		assertThat(output.toString(), containsString("print version and exit"));
	}

	@Test
	public void testWithNoScript() {
		Runner.main(new String[] { "-agent", "ios" });
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(),
				containsString("ERROR: You must specify a script or suite to run"));
		assertThat(output.toString(), containsString("print version and exit"));
	}

	@Test
	public void testWithMissingScript() {
		Runner.main(new String[] { "-agent", "ios", "missing.mt" });
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("ERROR: Bad input script. File not found"));
		assertThat(output.toString(), containsString("print version and exit"));
	}

	@Test
	public void testJavascript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nInput name EnterText \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		File fooJS = new File(dir, "foo.js");
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT),
				fooJS.getAbsolutePath(), "-verbose" };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button FOO tap -> OK"));
		assertThat(output.toString(), containsString("Input name enterText \"Bo Bo\" -> OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testQuietScript() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-quiet",
				script.getAbsolutePath() };

		Runner.main(args);

		assertThat(output.toString(), is(""));
	}

	@Test
	public void testVerboseScript() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				script.getAbsolutePath() };

		Runner.main(args);

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button OK Tap -> OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testQuietSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT),
				suite.getAbsolutePath(), "-quiet" };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		assertThat(output.toString(), is(""));
	}

	@Test
	public void testVerboseSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT),
				suite.getAbsolutePath(), "-verbose" };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("running suite : 2 tests"));
		assertThat(output.toString(), containsString("1 : foo.mt"));
		assertThat(output.toString(), containsString("Button FOO Tap -> OK"));
		assertThat(output.toString(), containsString("Button FOO2 Tap -> OK"));
		assertThat(output.toString(), containsString("Button FOO3 Tap -> OK"));
		assertThat(output.toString(), containsString("test result: OK"));
		assertThat(output.toString(), containsString("2 : bar.mt"));
		assertThat(output.toString(), containsString("Button BAR Tap -> OK"));
		assertThat(output.toString(), containsString("test result: OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testScriptAndroid() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);
		String[] args = { "-agent", "android", "-port", Integer.toString(PORT),
				script.getAbsolutePath(), "-verbose" };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OK Click"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button OK Click -> OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testScriptAndroidWithBadAdbProp() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		String[] args = { "-agent", "Android", "-adb", "/bin/echo", script.getAbsolutePath() };

		Runner.main(args);
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("ERROR: Android - adb not needed"));
		assertThat(output.toString(), containsString("print version and exit"));
	}

	@Test
	public void testScriptAndroidEmulator() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);
		String[] args = { "-agent", "AndroidEmulator", "-adb", "/bin/echo", "-port",
				Integer.toString(PORT), script.getAbsolutePath(), "-verbose" };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OK Click"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button OK Click -> OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Ignore
	@Test
	public void testScriptAndroidEmulatorWithoutAdb() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		String[] args = { "-agent", "AndroidEmulator", script.getAbsolutePath() };

		try {
			Runner.main(args);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("you must specify adb"));
			assertThat(output.toString(),
					containsString("Usage: <main class> [options] scripts..."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testMultipleScripts() throws IOException {
		File dir = tempDir();
		File script1 = tempScript("script1.mt", "Button OK Click", dir);
		File script2 = tempScript("script2.mt", "Button FOO Click", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT),
				script1.getAbsolutePath(), script2.getAbsolutePath(), "-verbose" };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OK Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Click"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button OK Click -> OK"));
		assertThat(output.toString(), containsString("result: OK\nMonkeyTalk"));
		assertThat(output.toString(), containsString("Button FOO Click -> OK"));
	}

	@Test
	public void testScriptWithTimeoutAndThinktime() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap %timeout=2345", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-timeout", "1234",
				"-thinktime", "567", "-startup", "0", "-verbose", foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %thinktime=567 %timeout=1234"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Button BAR Tap %thinktime=567 %timeout=2345"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button FOO Tap -> OK"));
		assertThat(output.toString(), containsString("Button BAR Tap %timeout=2345 -> OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testScriptWithBadStartup() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);
		String[] args = { "-agent", "iOS", "-startup", "1", foo.getAbsolutePath() };

		Runner.main(args);
		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(),
				containsString("ERROR: Unable to startup MonkeyTalk connection - timeout after 1s"));
		assertThat(output.toString(), containsString("print version and exit"));
	}

	@Test
	public void testScriptWithDebugPrint() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button FOO Tap\nDebug * Print foo bar baz\nButton BAR Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button FOO Tap -> OK\n"));
		assertThat(output.toString(), containsString("foo bar baz\n"));
		assertThat(output.toString(), containsString("Button BAR Tap -> OK\n"));
		assertThat(output.toString(), containsString("result: OK"));
	}
}