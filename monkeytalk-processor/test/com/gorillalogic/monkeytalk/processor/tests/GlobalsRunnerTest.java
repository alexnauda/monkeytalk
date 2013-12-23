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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Runner;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.SuiteListener;
import com.gorillalogic.monkeytalk.processor.SuiteProcessor;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class GlobalsRunnerTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18029;
	private ByteArrayOutputStream out;

	private static final PlaybackListener SCRIPT_LISTENER = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			System.out.print(scope.getCurrentCommand());
		}

		@Override
		public void onScriptStart(Scope scope) {
			System.out.println("START");
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult r) {
			System.out.println("COMPLETE : " + r);
		}

		@Override
		public void onComplete(Scope scope, Response resp) {
			System.out.println(" -> " + resp);
		}

		@Override
		public void onPrint(String message) {
			System.out.println(message);
		}
	};

	private static final SuiteListener SUITE_LISTENER = new SuiteListener() {

		@Override
		public void onRunStart(int total) {
			System.out.println("RUN (" + total + ")");
		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {
			System.out.println("RUN_COMPLETE : " + result);
		}

		@Override
		public void onTestStart(String name, int num, int total) {
			System.out.println("TEST " + name + " (" + num + " of " + total + ")");
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
			System.out.println("TEST_COMPLETE : " + result);
		}

		@Override
		public void onSuiteStart(int total) {
			System.out.println("SUITE");
		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
			System.out.println("SUITE_COMPLETE : " + result);
		}
	};

	@Before
	public void before() {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		AgentManager.removeAllAgents();
	}

	@After
	public void after() {
		out = null;
		Globals.clear();
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testScript() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		Globals.setGlobal("foo", "123");
		Globals.setGlobal("bar", "Bo Bo");

		CommandServer server = new CommandServer(PORT);
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(SCRIPT_LISTENER);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap \"Bo Bo\""));

		assertThat(out.toString(), containsString("START"));
		assertThat(out.toString(), containsString("Button 123 Tap \"Bo Bo\" -> OK\n"));
		assertThat(out.toString(), containsString("COMPLETE : OK\n"));
	}

	@Test
	public void testSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button ${foo} Tap", dir);
		tempScript("bar.mt", "Button ${bar} Tap", dir);
		tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Globals.setGlobal("foo", "123");
		Globals.setGlobal("bar", "Bo Bo");

		CommandServer server = new CommandServer(PORT);
		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setSuiteListener(SUITE_LISTENER);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(out.toString(), containsString("RUN (2)\n"));
		assertThat(out.toString(), containsString("SUITE\n"));
		assertThat(out.toString(), containsString("TEST foo.mt (1 of 2)\n"));
		assertThat(out.toString(), containsString("TEST bar.mt (2 of 2)\n"));
		assertThat(out.toString(), containsString("TEST_COMPLETE : OK\n"));
		assertThat(out.toString(), containsString("SUITE_COMPLETE : OK\n"));
		assertThat(out.toString(), containsString("RUN_COMPLETE : OK\n"));
	}

	@Test
	public void testRunner() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");
		map.put("bar", "Bo Bo");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap \"Bo Bo\""));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Button 123 Tap \"Bo Bo\" -> OK\n"));
	}

	@Test
	public void testRunnerGetIntoGlobal() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Label L1 Get foo\nLabel L2 Get bar", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new GetServer(PORT);
		PlaybackResult result = runner.run(foo, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Label L1 Get foo"));
		assertThat(server.getCommands().get(1).getCommand(), is("Label L2 Get bar"));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Label L1 Get foo -> OK : L1\n"));
		assertThat(out.toString(), containsString("Label L2 Get bar -> OK : L2\n"));
		assertThat(out.toString(), containsString("result: OK\n"));

		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("L1"));
		assertThat(Globals.getGlobal("bar"), nullValue());
	}

	@Test
	public void testRunnerGetIntoLocalOverrideOfGlobal() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo\nLabel L1 Get foo\nLabel L2 Get bar\nButton ${foo} Tap", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");
		map.put("bar", "Bo Bo");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new GetServer(PORT);
		PlaybackResult result = runner.run(foo, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Label L1 Get foo"));
		assertThat(server.getCommands().get(1).getCommand(), is("Label L2 Get bar"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button L1 Tap"));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Vars * Define foo -> OK\n"));
		assertThat(out.toString(), containsString("Label L1 Get foo -> OK : L1\n"));
		assertThat(out.toString(), containsString("Label L2 Get bar -> OK : L2\n"));
		assertThat(out.toString(), containsString("Button L1 Tap -> OK\n"));
		assertThat(out.toString(), containsString("result: OK\n"));

		assertThat(Globals.getGlobals().size(), is(2));

		// since local foo overrides global foo, global foo is unchanged
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("L2"));
		assertThat(Globals.getGlobal("baz"), nullValue());
	}

	@Test
	public void testRunnerVarsVerifyWithGlobal() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Verify 123 foo", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Vars * Verify 123 foo -> OK\n"));
		assertThat(out.toString(), containsString("result: OK\n"));

		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("123"));
	}

	@Test
	public void testRunnerVarsVerifyWithLocalAndGlobal() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define foo=234\nVars * Verify 234 foo", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Vars * Define foo=234 -> OK\n"));
		assertThat(out.toString(), containsString("Vars * Verify 234 foo -> OK\n"));
		assertThat(out.toString(), containsString("result: OK\n"));

		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), nullValue());
	}

	@Test
	public void testRunnerJSCallsMTCallsJS() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO${foo} Tap ${bar}\n"
				+ "Globals * Set foo=234 bar=\"Bo Bo 2\"\n"
				+ "Script bar Run\nButton FOO${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		tempScript("bar.mt", "Button BAR${foo} Tap ${bar}\n"
				+ "Globals * Set foo=345 bar=\"Bo Bo 3\"\n"
				+ "Script baz Run\nButton BAR${foo} Tap ${bar}", dir);

		File baz = tempScript("baz.mt", "Button BAZ${foo} Tap ${bar}\n"
				+ "Globals * Set foo=456 bar=\"Bo Bo 4\"\n" + "Button BAZ${foo} Tap ${bar}", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);
		JSHelper.genJS(baz);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");
		map.put("bar", "Bo Bo");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds, notNullValue());
		assertThat(cmds.size(), is(6));
		assertThat(cmds.get(0).getCommand(), is("Button FOO123 tap \"Bo Bo\""));
		assertThat(cmds.get(1).getCommand(), is("Button BAR234 Tap \"Bo Bo 2\""));
		assertThat(cmds.get(2).getCommand(), is("Button BAZ345 tap \"Bo Bo 3\""));
		assertThat(cmds.get(3).getCommand(), is("Button BAZ456 tap \"Bo Bo 4\""));
		assertThat(cmds.get(4).getCommand(), is("Button BAR456 Tap \"Bo Bo 4\""));
		assertThat(cmds.get(5).getCommand(), is("Button FOO456 tap \"Bo Bo 4\""));

		String o = out.toString();
		assertThat(o, containsString("www.gorillalogic.com"));
		assertThat(o, containsString("Button FOO123 tap \"Bo Bo\" -> OK\n"));
		assertThat(o, containsString("Globals * set foo=\"234\" -> OK\n"));
		assertThat(o, containsString("Globals * set bar=\"Bo Bo 2\" -> OK\n"));
		assertThat(o, containsString("Script bar.mt Run\n"));
		assertThat(o, containsString("Button BAR234 Tap \"Bo Bo 2\" -> OK\n"));
		assertThat(o, containsString("Globals * Set foo=345 bar=\"Bo Bo 3\" -> OK\n"));
		assertThat(o, containsString("Script baz Run\n"));
		assertThat(o, containsString("Button BAZ345 tap \"Bo Bo 3\" -> OK\n"));
		assertThat(o, containsString("Globals * set foo=\"456\" -> OK\n"));
		assertThat(o, containsString("Globals * set bar=\"Bo Bo 4\" -> OK\n"));
		assertThat(o, containsString("Button BAZ456 tap \"Bo Bo 4\" -> OK\n"));
		assertThat(o, containsString("Button BAR456 Tap \"Bo Bo 4\" -> OK\n"));
		assertThat(o, containsString("Button FOO456 tap \"Bo Bo 4\" -> OK\n"));
		assertThat(o, containsString("result: OK"));
	}

	@Test
	public void testRunnerWithPropertiesFile() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		tempScript("globals.properties", "foo=123\nbar=Bo Bo\n", dir);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap \"Bo Bo\""));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Button 123 Tap \"Bo Bo\" -> OK\n"));
	}

	@Test
	public void testRunnerWithPropertiesFileOverriddenByCommandline() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		tempScript("globals.properties", "foo=123\nbar=Bo Bo\n", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "234");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(foo, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 234 Tap \"Bo Bo\""));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("Button 234 Tap \"Bo Bo\" -> OK\n"));
	}

	@Test
	public void testRunnerWithSuite() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button ${foo} Tap", dir);
		tempScript("bar.mt", "Button ${bar} Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");
		map.put("bar", "Bo Bo");

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setReportdir(dir);
		runner.setVerbose(true);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(suite, map);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-mysuite.xml", dir);
		assertThat(f, notNullValue());

		String xml = FileUtils.readFile(f);
		assertThat(xml, containsString("tests=\"2\" suites=\"0\""));
		assertThat(xml, containsString("errors=\"0\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("<testcase name=\"bar"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(out.toString(), containsString("www.gorillalogic.com"));
		assertThat(out.toString(), containsString("running suite : 2 tests\n"));
		assertThat(out.toString(), containsString("1 : foo.mt\n"));
		assertThat(out.toString(), containsString("Button 123 Tap -> OK\n"));
		assertThat(out.toString(), containsString("2 : bar.mt\n"));
		assertThat(out.toString(), containsString("Button \"Bo Bo\" Tap -> OK\n"));
		assertThat(out.toString(), containsString("test result: OK\n"));
		assertThat(out.toString(), containsString("result: OK\n"));
	}
}