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
package com.gorillalogic.monkeytalk.ant.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gorillalogic.monkeytalk.ant.RunTask;
import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer;

public class RunTaskTest extends BaseAntTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18171;
	private CommandServer server;
	private RunTask task;
	private Project proj;

	@Before
	public void before() throws IOException {
		task = new RunTask();
		task.setHost(HOST);
		task.setPort(PORT);

		proj = new Project();
		task.setProject(proj);

		server = new PlaybackCommandServer(new TestHelper(), PORT);
	}

	@After
	public void after() {
		server.stop();
	}

	@Test
	public void testScript() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OK Click"));
	}

	@Test
	public void testEmptyScript() {
		RunTask task = new RunTask();

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), is("Nothing to run."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithBadAgent() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("BADAGENT");

		try {
			task.execute();
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), is("Unable to find agent BADAGENT"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptAndroidWithBadAdbProp() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("android");
		task.setAdb(new File(dir, "missing"));

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("adb not needed"));
			return;
		}
		fail("should have thrown exception");
	}

	@Ignore
	@Test
	public void testScriptAndroidEmulatorWithoutAdb() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("androidemulator");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("you must specify adb"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithAdbNotFound() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("androidemulator");
		task.setAdb(new File(dir, "missing"));

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(),
					containsString("you must specify a vaild path to adb. File not found:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithAdbAsDir() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("androidemulator");
		task.setAdb(dir);

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(),
					containsString("you must specify a vaild path to adb. Not a file:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithBadAdbFile() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);
		File adb = tempScript("adb", "", dir);

		task.setScript(script);
		task.setAgent("androidemulator");
		task.setAdb(adb);

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("Error starting adb:\nCannot run program"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithNothingToRunOniOS() {
		task.setAgent("ios");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), is("Nothing to run."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithNothingToRunOnAndroid() {
		task.setAgent("androidemulator");
		task.setHost("host");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), is("Nothing to run."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testMissingScript() {
		task.setScript(new File("missing.mt"));
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("Bad input script. File not found:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testMissingSuite() {
		task.setSuite(new File("missing.mts"));
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("Bad input script. File not found:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testScriptWithVars() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define x\nButton ${x} Click", dir);
		File script = tempScript("script.mt", "Script foo.mt Run AAA\nScript foo.mt Run BBB", dir);

		task.setScript(script);
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button AAA Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BBB Click"));
	}

	@Test
	public void testScriptWithData() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define x\nButton ${x} Click", dir);
		tempScript("data.csv", "x\nfoo\nbar\nbaz", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		task.setScript(script);
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button foo Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button bar Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button baz Click"));
	}

	@Test
	public void testScriptWithDataWithBadHeaderRow() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define x\nButton ${x} Click", dir);
		tempScript("data.csv", "bad\nfoo\nbar\nbaz", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		task.setScript(script);
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("datafile 'data.csv' is missing column 'x'"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		task.setSuite(suite);
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Click"));
	}

	@Test
	public void testSuiteWithSetupAndTeardown() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Click", dir);
		tempScript("bar.mt", "Button BAR Click", dir);
		tempScript("up.mt", "Button UP Click", dir);
		tempScript("down.mt", "Button DOWN Click", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nSetup up.mt Run\nTeardown down.mt Run", dir);

		task.setSuite(suite);
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(), is("Button UP Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button DOWN Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button UP Click"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button BAR Click"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button DOWN Click"));
	}

	@Test
	public void testCommandText() throws IOException {
		task.setAgent("iOS");
		task.addText("Button FOO Click\nButton BAR Click");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Click"));
	}

	@Test
	public void testCommandTextOnAndroid() throws IOException {
		task.setAgent("androidemulator");
		// using echo to sub for adb
		task.setAdb(new File("/bin/echo"));
		task.addText("Button FOO Click\nButton BAR Click");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Click"));
	}

	@Test
	public void testCommandTextWithPropertySubstitution() throws IOException {
		task.setAgent("iOS");
		task.addText("Input name EnterText \"${foo} ${bar} ${baz}\"");

		task.getProject().setProperty("foo", "FOO");
		task.getProject().setProperty("bar", "BAR");
		task.getProject().setProperty("baz", "BAZ");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input name EnterText \"FOO BAR BAZ\""));
	}

	@Test
	public void testBothScriptAndSuite() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Tap", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		task.setScript(script);
		task.setSuite(suite);
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(),
					containsString("You cannot specify both script and suite in the run task."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testBothScriptAndCommands() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Tap", dir);

		task.setScript(script);
		task.addText("Button CMD Tap");
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(
					ex.getMessage(),
					containsString("You cannot specify both script and inline commands in the run task."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testBothSuiteAndCommands() throws IOException {
		File dir = tempDir();
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		task.setSuite(suite);
		task.addText("Button CMD Tap");
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(
					ex.getMessage(),
					containsString("You cannot specify both suite and inline commands in the run task."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testReportdir() throws IOException {
		File dir = tempDir();
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);
		File reportdir = new File(dir, "/reports");

		task.setSuite(suite);
		task.setAgent("iOS");
		task.setReportdir(reportdir);

		task.execute();

		File report = new File(reportdir, "/TEST-suite.xml");
		assertTrue(report.exists());
	}

	@Test
	public void testReportdirNotDirectory() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		task.setSuite(suite);
		task.setAgent("iOS");
		task.setReportdir(suite);

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(),
					containsString("You must specify a valid reportdir. Not a directory."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testReportdirNotExist() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);
		File reportdir = new File("");

		task.setSuite(suite);
		task.setAgent("iOS");
		task.setReportdir(reportdir);

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), containsString("Failed to make reportdir:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testVerbose() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		task.setScript(script);
		task.setAgent("iOS");
		task.setVerbose(true);

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OK Click"));
	}

	@Test
	public void testScriptJavascript() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));

		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nInput name EnterText \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		task.setScript(new File(dir, "foo.js"));
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));

		assertThat(out.toString(), containsString("com.gorillalogic.monkeytalk.api.Button"));
	}

	@Test
	public void testScriptWithTimeoutAndThinktime() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap %timeout=2345", dir);

		task.setScript(foo);
		task.setAgent("iOS");
		task.setTimeout(1234);
		task.setThinktime(567);
		task.setStartup(-1);

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %thinktime=567 %timeout=1234"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Button BAR Tap %thinktime=567 %timeout=2345"));
	}

	@Test
	public void testBadStartup() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		task.setScript(foo);
		task.setAgent("iOS");
		task.setStartup(1);

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(),
					containsString("Unable to startup MonkeyTalk connection - timeout after 1s"));
			return;
		}
		fail("should have thrown exception");
	}
}