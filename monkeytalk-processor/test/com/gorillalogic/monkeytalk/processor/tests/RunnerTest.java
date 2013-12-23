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
package com.gorillalogic.monkeytalk.processor.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.agents.MTAgent;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Runner;
import com.gorillalogic.monkeytalk.server.ServerConfig;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class RunnerTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18029;
	private ByteArrayOutputStream out;

	@Before
	public void before() {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		AgentManager.removeAllAgents();
	}

	@After
	public void after() {
		out = null;
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testDefaultConstructor() {
		Runner runner = new Runner();
		assertThat(runner.getAgent().getClass().getName(), is(MTAgent.class.getName()));
		assertThat(runner.getHost(), is("localhost"));
		assertThat(runner.getPort(), is(ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID));
	}

	@Test
	public void testAgentConstructor() {
		Runner runner = new Runner("ios");
		assertThat(runner.getAgentName(), is("iOS"));
		assertThat(runner.getHost(), is("localhost"));
		assertThat(runner.getPort(), is(ServerConfig.DEFAULT_PLAYBACK_PORT_IOS));
	}

	@Test
	public void testAgentPortConstructor() {
		Runner runner = new Runner("android", 1234);
		assertThat(runner.getAgentName(), is("Android"));
		assertThat(runner.getHost(), is("localhost"));
		assertThat(runner.getPort(), is(1234));
	}

	@Test
	public void testAgentPortConstructorWithNegativePort() {
		Runner runner = new Runner("android", -32);
		assertThat(runner.getAgentName(), is("Android"));
		assertThat(runner.getHost(), is("localhost"));
		assertThat(runner.getPort(), is(ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID));
	}

	@Test
	public void testAgentHostPortConstructor() {
		Runner runner = new Runner("ANDROID", "myhost", 5432);
		runner.setScriptListener(null);
		runner.setSuiteListener(null);

		assertThat(runner.getAgentName(), is("Android"));
		assertThat(runner.getHost(), is("myhost"));
		assertThat(runner.getPort(), is(5432));
	}

	@Test
	public void testRunScript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
	}

	@Test
	public void testRunScriptHierarchy() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR1 Tap\nScript foo.mt Run\nButton BAR2 Tap", dir);
		File script = tempScript("baz.mt", "Button BAZ1 Tap\nScript bar.mt Run\nButton BAZ2 Tap",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(5));
		assertThat(cmds.get(0).getCommand(), is("Button BAZ1 Tap"));
		assertThat(cmds.get(1).getCommand(), is("Button BAR1 Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button BAR2 Tap"));
		assertThat(cmds.get(4).getCommand(), is("Button BAZ2 Tap"));

		File f = FileUtils.findFile("DETAIL-baz.mt.html", dir);
		assertThat(f, notNullValue());

		String html = FileUtils.readFile(f);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(html, not(containsString("class=\"offset4 span20\"")));
		assertThat(html, containsString("1</span><b>Script</b> baz.mt <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Button</b> BAZ1 <b>Tap</b>"));
		assertThat(html, containsString("3</span><b>Script</b> bar.mt <b>Run</b>"));
		assertThat(html, containsString("4</span><b>Button</b> BAR1 <b>Tap</b>"));
		assertThat(html, containsString("5</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("6</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, containsString("7</span><b>Button</b> BAR2 <b>Tap</b>"));
		assertThat(html, containsString("8</span><b>Button</b> BAZ2 <b>Tap</b>"));
	}

	@Test
	public void testRunScriptWithNullAdb() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);

		try {
			runner.run(foo, null);
		} catch (RuntimeException ex) {
			assertThat(
					ex.getMessage(),
					is("AndroidEmulator - you must specify adb to run on the Android Emulator or on a tethered Android device."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptWithMissingAdb() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);
		File missing = new File("missing");

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);
		runner.setAdb(missing);

		try {
			runner.run(foo, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(),
					is("AndroidEmulator - you must specify a vaild path to adb. File not found: "
							+ missing.getAbsolutePath()));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptWithAdbAsFolder() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);
		runner.setAdb(dir);

		try {
			runner.run(foo, null);
		} catch (RuntimeException ex) {
			assertThat(
					ex.getMessage(),
					is("AndroidEmulator - you must specify a vaild path to adb. Not a file: "
							+ dir.getAbsolutePath()));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptWithNullInput() throws IOException {
		Runner runner = new Runner("iOS", "myhost", PORT);

		try {
			runner.run(null, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), is("Bad input script."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptWithMissingInput() throws IOException {
		File missing = new File("missing");

		Runner runner = new Runner("iOS", HOST, PORT);

		try {
			runner.run(missing, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(),
					is("Bad input script. File not found: " + missing.getAbsolutePath()));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptWithInputAsFolder() throws IOException {
		File dir = tempDir();

		Runner runner = new Runner("iOS", HOST, PORT);

		try {
			runner.run(dir, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(),
					is("Bad input script. Not a file: " + dir.getAbsolutePath()));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptWithUnknownExtension() throws IOException {
		File dir = tempDir();
		File unknown = tempScript("unknown.ext", "", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		try {
			runner.run(unknown, null);
		} catch (RuntimeException ex) {
			assertThat(
					ex.getMessage(),
					is("Unrecognized input script file extension.  Allowed values are: .mt, .mts, .js"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(dir);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(suite, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-mysuite.xml", dir);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"2\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("running suite : 2 tests"));
		assertThat(log, containsString("1 : foo.mt"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("Button FOO2 Tap -> OK"));
		assertThat(log, containsString("Button FOO3 Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("2 : bar.mt"));
		assertThat(log, containsString("Button BAR Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("result: OK"));

	}

	@Test
	public void testRunSuiteWithNullReportDir() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(null);

		CommandServer server = new CommandServer(PORT);
		runner.run(suite, null);
		server.stop();

		File f = FileUtils.findFile("TEST-mysuite.xml", dir);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"2\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
	}

	@Test
	public void testRunSuiteThatMustCreateReportDir() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File reports = new File(dir, "reports");
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(reports);

		CommandServer server = new CommandServer(PORT);
		runner.run(suite, null);
		server.stop();

		File f = FileUtils.findFile("TEST-mysuite.xml", reports);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"2\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
	}

	@Test
	public void testRunSuiteWithUncreatableReportDir() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File reports = new File("");
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(reports);

		try {
			runner.run(suite, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("Failed to make reportdir:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunSuiteWithReportDirAsFile() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(foo);

		try {
			runner.run(suite, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), is("You must specify a valid reportdir. Not a directory."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunSuiteWithMultipleTests() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run\nTest baz.mt Run",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(dir);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(suite, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-mysuite.xml", dir);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"3\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt"));
		assertThat(xml, containsString("<testcase name=\"bar.mt"));
		assertThat(xml, containsString("<testcase name=\"baz.mt"));

		f = FileUtils.findFile("TEST-mysuite.html", dir);
		assertThat(f, notNullValue());

		String html = FileUtils.readFile(f);
		assertThat(html, containsString("www.gorillalogic.com"));
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"3\" data-err=\"0\" data-fail=\"0\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("bar.mt</a>"));
		assertThat(html, containsString("baz.mt</a>"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("running suite : 3 tests"));
		assertThat(log, containsString("1 : foo.mt"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("2 : bar.mt"));
		assertThat(log, containsString("Button BAR Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("3 : baz.mt"));
		assertThat(log, containsString("Button BAZ Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunSuiteWithFailingTest() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap\nButton FRED Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run\nTest baz.mt Run",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(dir);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		FailOnFredServer server = new FailOnFredServer(PORT);
		PlaybackResult result = runner.run(suite, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-mysuite.xml", dir);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"3\" suites=\"0\" errors=\"0\" failures=\"1\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt"));
		assertThat(xml, containsString("<testcase name=\"bar.mt"));
		assertThat(xml, containsString("<testcase name=\"baz.mt"));
		assertThat(xml, containsString("fail on Fred\n"
				+ "  at Button FRED Tap (bar.mt : cmd #2)\n"
				+ "  at Test bar.mt Run (mysuite.mts : cmd #2)"));

		f = FileUtils.findFile("TEST-mysuite.html", dir);
		assertThat(f, notNullValue());

		String html = FileUtils.readFile(f);
		assertThat(html, containsString("www.gorillalogic.com"));
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"3\" data-err=\"0\" data-fail=\"1\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("bar.mt</a>"));
		assertThat(html, containsString("baz.mt</a>"));
		assertThat(html, containsString("fail on Fred\n"
				+ "  at Button FRED Tap (bar.mt : cmd #2)\n"
				+ "  at Test bar.mt Run (mysuite.mts : cmd #2)"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("running suite : 3 tests"));
		assertThat(log, containsString("1 : foo.mt"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("2 : bar.mt"));
		assertThat(log, containsString("Button BAR Tap -> OK"));
		assertThat(log, containsString("Button FRED Tap -> FAILURE : fail on Fred"));
		assertThat(log, containsString("test result: FAILURE : fail on Fred"));
		assertThat(log, containsString("3 : baz.mt"));
		assertThat(log, containsString("Button BAZ Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunSuiteWithErroringTest() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap\nButton JOE Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run\nTest baz.mt Run",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(dir);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = runner.run(suite, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-mysuite.xml", dir);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"3\" suites=\"0\" errors=\"1\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt"));
		assertThat(xml, containsString("<testcase name=\"bar.mt"));
		assertThat(xml, containsString("<testcase name=\"baz.mt"));
		assertThat(xml, containsString("error on Joe\n" + "  at Button JOE Tap (bar.mt : cmd #2)\n"
				+ "  at Test bar.mt Run (mysuite.mts : cmd #2)"));

		f = FileUtils.findFile("TEST-mysuite.html", dir);
		assertThat(f, notNullValue());

		String html = FileUtils.readFile(f);
		assertThat(html, containsString("www.gorillalogic.com"));
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"3\" data-err=\"1\" data-fail=\"0\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("bar.mt</a>"));
		assertThat(html, containsString("baz.mt</a>"));
		assertThat(html, containsString("error on Joe\n"
				+ "  at Button JOE Tap (bar.mt : cmd #2)\n"
				+ "  at Test bar.mt Run (mysuite.mts : cmd #2)"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("running suite : 3 tests"));
		assertThat(log, containsString("1 : foo.mt"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("2 : bar.mt"));
		assertThat(log, containsString("Button BAR Tap -> OK"));
		assertThat(log, containsString("Button JOE Tap -> ERROR : error on Joe"));
		assertThat(log, containsString("test result: ERROR : error on Joe"));
		assertThat(log, containsString("3 : baz.mt"));
		assertThat(log, containsString("Button BAZ Tap -> OK"));
		assertThat(log, containsString("test result: OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunSuiteOfSuites() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("foo2.mt", "Button FOO2 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("bar2.mt", "Button BAR2 Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		tempScript("s2.mts", "Test bar.mt Run\nSuite s3.mts Run\nTest bar2.mt Run", dir);
		tempScript("s3.mts", "Test baz.mt Run", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nSuite s2.mts Run\nTest foo2.mt Run",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(dir);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = runner.run(suite, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(5));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR2 Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button FOO2 Tap"));

		File f = FileUtils.findFile("DETAIL-suite.mts.html", dir);
		assertThat(f, notNullValue());

		String html = FileUtils.readFile(f);

		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));

		assertThat(html, containsString("1</span><b>Suite</b> suite.mts <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Test</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("3</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, containsString("4</span><b>Suite</b> s2.mts <b>Run</b>"));
		assertThat(html, containsString("5</span><b>Test</b> bar.mt <b>Run</b>"));
		assertThat(html, containsString("6</span><b>Button</b> BAR <b>Tap</b>"));
		assertThat(html, containsString("7</span><b>Suite</b> s3.mts <b>Run</b>"));
		assertThat(html, containsString("8</span><b>Test</b> baz.mt <b>Run</b>"));
		assertThat(html, containsString("9</span><b>Button</b> BAZ <b>Tap</b>"));
		assertThat(html, containsString("10</span><b>Test</b> bar2.mt <b>Run</b>"));
		assertThat(html, containsString("11</span><b>Button</b> BAR2 <b>Tap</b>"));
		assertThat(html, containsString("12</span><b>Test</b> foo2.mt <b>Run</b>"));
		assertThat(html, containsString("13</span><b>Button</b> FOO2 <b>Tap</b>"));

	}

	@Test
	public void testRunScriptOnAndroid() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("Android", HOST, PORT);

		CommandServer server = new CommandServer(PORT);
		runner.run(foo, null);
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
	}

	@Test
	public void testRunScriptOnAndroidWithBadAdbProp() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("Android", HOST, PORT);
		runner.setAdb(new File("/bin/echo"));

		try {
			runner.run(foo, null);
		} catch (RuntimeException ex) {
			assertThat(
					ex.getMessage(),
					is("Android - adb not needed when running against a remote Android device. Use the 'AndroidEmulator' agent to run on the Emulator or on a tethered device."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptOnAndroidWithBadAdbSerialProp() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("Android", HOST, PORT);
		runner.setAgentProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP, "myserial");

		try {
			runner.run(foo, null);
		} catch (RuntimeException ex) {
			assertThat(
					ex.getMessage(),
					is("Android - adbSerial not needed when running against a remote Android device. Use the 'AndroidEmulator' agent to run on the Emulator or on a tethered device."));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunScriptOnAndroidEmulator() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);
		runner.setAdb(new File("/bin/echo"));

		CommandServer server = new CommandServer(PORT);
		runner.run(foo, null);
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
	}

	@Test
	public void testRunScriptOnAndroidWithSerial() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);
		runner.setAdb(new File("/bin/echo"));
		runner.setAgentProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP, "myhost:1234");

		CommandServer server = new CommandServer(PORT);
		runner.run(foo, null);
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
	}

	@Test
	public void testRunScriptOnAndroidWithNullSerial() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);
		runner.setAdb(new File("/bin/echo"));
		runner.setAgentProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP, null);

		CommandServer server = new CommandServer(PORT);
		runner.run(foo, null);
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
	}

	@Test
	public void testRunScriptOnAndroidWithBadAdb() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);
		File adb = tempScript("adb", "", dir);

		Runner runner = new Runner("AndroidEmulator", HOST, PORT);
		runner.setAdb(adb);

		try {
			runner.run(foo, null);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("Error starting adb:"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testRunCustomCommand() throws IOException {
		File dir = tempDir();
		tempScript("mycomp.myact.mt", "Button MY Tap", dir);
		File script = tempScript("myscript.mt", "MyComp * MyAct", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button MY Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("MyComp * MyAct"));
		assertThat(log, containsString("Button MY Tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunCustomCommandWithArgs() throws IOException {
		File dir = tempDir();
		tempScript("mycomp.myact.mt", "Vars * Define x\nButton ${x} Tap", dir);
		File script = tempScript("myscript.mt", "MyComp * MyAct MYFOO\nMyComp * MyAct MYBAR", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button MYFOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button MYBAR Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("MyComp * MyAct MYFOO"));
		assertThat(log, containsString("Button MYFOO Tap -> OK"));
		assertThat(log, containsString("MyComp * MyAct MYBAR"));
		assertThat(log, containsString("Button MYBAR Tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunScriptWithGet() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo\nInput name EnterText ${foo}", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		GetServer server = new GetServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get foo"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name EnterText FOO"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Get foo -> OK : FOO"));
		assertThat(log, containsString("Input name EnterText FOO -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithMultipleGets() throws IOException {
		File dir = tempDir();
		File foo = tempScript(
				"foo.mt",
				"Button FOO Get a\nButton BAR Get b\nButton BAZ Get c\nInput name EnterText ${a}${b}${c}",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		GetServer server = new GetServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get a"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Get b"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Get c"));
		assertThat(server.getCommands().get(3).getCommand(), is("Input name EnterText FOOBARBAZ"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Get a -> OK : FOO"));
		assertThat(log, containsString("Button BAR Get b -> OK : BAR"));
		assertThat(log, containsString("Button BAZ Get c -> OK : BAZ"));
		assertThat(log, containsString("Input name EnterText FOOBARBAZ -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithGetOverrides() throws IOException {
		File dir = tempDir();
		File foo = tempScript(
				"foo.mt",
				"Button FOO Get foo\nInput name EnterText ${foo}\nButton BAR Get foo\nInput name EnterText ${foo}",
				dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		GetServer server = new GetServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get foo"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name EnterText FOO"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAR Get foo"));
		assertThat(server.getCommands().get(3).getCommand(), is("Input name EnterText BAR"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Get foo -> OK : FOO"));
		assertThat(log, containsString("Input name EnterText FOO -> OK"));
		assertThat(log, containsString("Button BAR Get foo -> OK : BAR"));
		assertThat(log, containsString("Input name EnterText BAR -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testMTCallsMTCallsMTCallsMTCallsMT() throws IOException {
		File dir = tempDir();
		File sI = tempScript("s1.mt", "Button I Tap\nScript s2.mt Run I", dir);
		tempScript("s2.mt", "Vars * Define x\nButton ${x}A Tap\nScript s3.mt Run ${x}A", dir);
		tempScript("s3.mt", "Vars * Define x\nButton ${x}1 Tap\nScript s4.mt Run ${x}1", dir);
		tempScript("s4.mt", "Vars * Define x\nButton ${x}a Tap\nScript s5.mt Run ${x}a", dir);
		tempScript("s5.mt", "Vars * Define x\nButton ${x}i Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(sI, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(5));
		assertThat(server.getCommands().get(0).getCommand(), is("Button I Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button IA Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button IA1 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button IA1a Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button IA1ai Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button I Tap -> OK"));
		assertThat(log, containsString("Script s2.mt Run I"));
		assertThat(log, containsString("Button IA Tap -> OK"));
		assertThat(log, containsString("Script s3.mt Run I"));
		assertThat(log, containsString("Button IA1 Tap -> OK"));
		assertThat(log, containsString("Script s4.mt Run I"));
		assertThat(log, containsString("Button IA1a Tap -> OK"));
		assertThat(log, containsString("Script s5.mt Run I"));
		assertThat(log, containsString("Button IA1ai Tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testMTDataDrivesMTDataDrivesMTDataDrivesMTDataDrivesMT() throws IOException {
		File dir = tempDir();

		tempScript("data1.csv", "x\nI\nII", dir);
		tempScript("data2.csv", "x\nA\nB", dir);
		tempScript("data3.csv", "x\n1\n2", dir);
		tempScript("data4.csv", "x\na\nb", dir);
		tempScript("data5.csv", "x\ni\nii", dir);

		File script = tempScript("myscript.mt", "Script s1.mt RunWith data1.csv", dir);
		tempScript("s1.mt", "Vars * Define x\nButton ${x} Tap\nScript s2.mt RunWith data2.csv", dir);
		tempScript("s2.mt", "Vars * Define x\nButton ${x} Tap\nScript s3.mt RunWith data3.csv", dir);
		tempScript("s3.mt", "Vars * Define x\nButton ${x} Tap\nScript s4.mt RunWith data4.csv", dir);
		tempScript("s4.mt", "Vars * Define x\nButton ${x} Tap\nScript s5.mt RunWith data5.csv", dir);
		tempScript("s5.mt", "Vars * Define x\nButton ${x} Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(62));
		assertThat(server.getCommands().get(0).getCommand(), is("Button I Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button A Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button 1 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(6).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(7).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(9).getCommand(), is("Button 2 Tap"));
		assertThat(server.getCommands().get(10).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(11).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(12).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(13).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(14).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(15).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(16).getCommand(), is("Button B Tap"));
		assertThat(server.getCommands().get(17).getCommand(), is("Button 1 Tap"));
		assertThat(server.getCommands().get(18).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(19).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(20).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(21).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(22).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(23).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(24).getCommand(), is("Button 2 Tap"));
		assertThat(server.getCommands().get(25).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(26).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(27).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(28).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(29).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(30).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(31).getCommand(), is("Button II Tap"));
		assertThat(server.getCommands().get(32).getCommand(), is("Button A Tap"));
		assertThat(server.getCommands().get(33).getCommand(), is("Button 1 Tap"));
		assertThat(server.getCommands().get(34).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(35).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(36).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(37).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(38).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(39).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(40).getCommand(), is("Button 2 Tap"));
		assertThat(server.getCommands().get(41).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(42).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(43).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(44).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(45).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(46).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(47).getCommand(), is("Button B Tap"));
		assertThat(server.getCommands().get(48).getCommand(), is("Button 1 Tap"));
		assertThat(server.getCommands().get(49).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(50).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(51).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(52).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(53).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(54).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(55).getCommand(), is("Button 2 Tap"));
		assertThat(server.getCommands().get(56).getCommand(), is("Button a Tap"));
		assertThat(server.getCommands().get(57).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(58).getCommand(), is("Button ii Tap"));
		assertThat(server.getCommands().get(59).getCommand(), is("Button b Tap"));
		assertThat(server.getCommands().get(60).getCommand(), is("Button i Tap"));
		assertThat(server.getCommands().get(61).getCommand(), is("Button ii Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button I Tap -> OK"));
		assertThat(log, containsString("Button II Tap -> OK"));
		assertThat(log, containsString("Button A Tap -> OK"));
		assertThat(log, containsString("Button B Tap -> OK"));
		assertThat(log, containsString("Button 1 Tap -> OK"));
		assertThat(log, containsString("Button 2 Tap -> OK"));
		assertThat(log, containsString("Button a Tap -> OK"));
		assertThat(log, containsString("Button b Tap -> OK"));
		assertThat(log, containsString("Button i Tap -> OK"));
		assertThat(log, containsString("Button ii Tap -> OK"));
	}

	@Test
	public void testScriptWithUTF8() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Héìíô Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Héìíô Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button Héìíô Tap -> OK"));
	}

	@Test
	public void testRunScriptWithIgnore() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap %ignore=true\n"
				+ "Script bazz.mt Run %ignore=true\nScript bazz.mt RunWith data.csv %ignore=true\n"
				+ "Button BAZ Tap", dir);
		tempScript("bazz.mt", "Button BAZZ Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAZ Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("Button BAR Tap %ignore=true -> OK : ignored"));
		assertThat(log, containsString("Script bazz.mt Run %ignore=true\n -> OK : ignored"));
		assertThat(log,
				containsString("Script bazz.mt RunWith data.csv %ignore=true\n -> OK : ignored"));
		assertThat(log, containsString("Button BAZ Tap -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithTimeoutAndThinktime() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\n" + "Button BAR Tap %timeout=2345\n"
				+ "Button BAZ Tap %thinktime=888", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);
		runner.setGlobalTimeout(1234);
		runner.setGlobalThinktime(567);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %thinktime=567 %timeout=1234"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Button BAR Tap %thinktime=567 %timeout=2345"));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Button BAZ Tap %thinktime=888 %timeout=1234"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("Button BAR Tap %timeout=2345 -> OK"));
		assertThat(log, containsString("Button BAZ Tap %thinktime=888 -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithDebugPrint() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button FOO Tap\nDebug * Print foo bar baz\nButton BAR Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Tap -> OK\n"));
		assertThat(log, not(containsString("Debug")));
		assertThat(log, containsString("foo bar baz\n"));
		assertThat(log, containsString("Button BAR Tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithDebugVars() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo=123 bar=\"Bo Bo\"\nDebug * Vars\nButton FOO Tap", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Vars * Define foo=123 bar=\"Bo Bo\" -> OK"));
		assertThat(log, not(containsString("Debug")));
		assertThat(log, containsString("\nfoo=123\nbar=Bo Bo\n"));
		assertThat(log, containsString("Button FOO Tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithDebugVarsAndMultipleVarDefines() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo=123 bar=\"Bo Bo\"\nDebug * Vars\nButton FOO Tap\n"
						+ "Vars * Define baz=abc\nDebug * Vars", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Vars * Define foo=123 bar=\"Bo Bo\" -> OK"));
		assertThat(log, not(containsString("Debug")));
		assertThat(log, containsString("\nfoo=123\nbar=Bo Bo\n"));
		assertThat(log, containsString("Vars * Define baz=abc -> OK"));
		assertThat(log, containsString("\nfoo=123\nbar=Bo Bo\nbaz=abc\n"));
		assertThat(log, containsString("Button FOO Tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunScriptWithGlobalScreenshotOnErrorOff() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\n"
				+ "Button BAR Tap %screenshotonerror=false\n"
				+ "Button BAZ Tap %screenshotonerror=true", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setGlobalScreenshotOnError(false);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %screenshotonerror=false"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Button BAR Tap %screenshotonerror=false"));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Button BAZ Tap %screenshotonerror=true"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO Tap -> OK"));
		assertThat(log, containsString("Button BAR Tap %screenshotonerror=false -> OK"));
		assertThat(log, containsString("Button BAZ Tap %screenshotonerror=true -> OK"));
		assertThat(log, containsString("result: OK"));
	}
}