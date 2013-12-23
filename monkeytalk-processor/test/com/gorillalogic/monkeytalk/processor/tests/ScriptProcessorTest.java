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
import static org.junit.matchers.JUnitMatchers.everyItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.utils.Base64;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class ScriptProcessorTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18027;
	private static int counter;
	private static String output;

	private static final PlaybackListener LISTENER_WITH_COUNTER = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			counter += 1;
		}

		@Override
		public void onScriptStart(Scope scope) {
			counter += 1000000;
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult r) {
			counter += 10000;
		}

		@Override
		public void onComplete(Scope scope, Response response) {
			counter += 100;
			assertThat(response, notNullValue());
			assertThat(response.getStatus(), is(ResponseStatus.OK));
		}

		@Override
		public void onPrint(String message) {
		}
	};

	private static final PlaybackListener LISTENER_WITH_OUTPUT = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			output += scope.getCurrentCommand();
		}

		@Override
		public void onScriptStart(Scope scope) {
			output = "START\n";
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult r) {
			output += "COMPLETE : " + r;
		}

		@Override
		public void onComplete(Scope scope, Response resp) {
			output += " -> " + resp + "\n";
		}

		@Override
		public void onPrint(String message) {
			output += message;
		}
	};

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testDefaultConstructor() {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		assertThat(processor, notNullValue());
		assertThat(processor.toString(), containsString("ScriptProcessor:"));
		assertThat(processor.getWorld(), notNullValue());
	}

	@Test
	public void testRunScript() throws IOException {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));

		CommandWorld world = processor.getWorld();
		assertThat(world, notNullValue());
		assertThat(world.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript(script.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
	}

	@Test
	public void testRunNullScript() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		PlaybackResult result = processor.runScript(null);
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("script filename is null"));
	}

	@Test
	public void testRunMissingScript() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		PlaybackResult result = processor.runScript("missing.mt");
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("script 'missing.mt' not found"));
	}

	@Test
	public void testRunSuiteAsScript() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		PlaybackResult result = processor.runScript("suite.mts");
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("running suite 'suite.mts' as a script is not allowed"));
	}

	@Test
	public void testRunEmptyScript() throws Exception {
		File dir = tempDir();
		File script = tempScript("script.mt", "", dir);
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript(script.getName());
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("script '" + script.getName() + "' is empty"));

		String report = new ScriptReportHelper().reportScriptSteps(result, new Scope("script.mt"),
				null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + script.getName()
				+ "\" action=\"Run\""));
	}

	@Test
	public void testRunScriptWithComment() throws Exception {
		File dir = tempDir();
		File script = tempScript("script.mt", "# script comment", dir);
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript(script.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String report = new ScriptReportHelper().reportScriptSteps(result, new Scope("script.mt"),
				null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + script.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(0));
	}

	@Test
	public void testRunScriptWithNullScope() throws Exception {
		File dir = tempDir();
		File script = tempScript("script.mt", "Button OK Click", dir);
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript(script.getName(), null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String report = new ScriptReportHelper().reportScriptSteps(result,
				new Scope(script.getName()), null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + script.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<cmd .*Button OK Click.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithNullCommands() throws Exception {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = null;

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript(commands, null);
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("command list is null"));

		String report = new ScriptReportHelper().reportScriptSteps(result, new Scope(), null)
				.toXMLDocument();
		assertThat(report, containsString("CDATA[command list is null]"));
	}

	@Test
	public void testRunScriptWithEmptyCommands() throws Exception {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = new ArrayList<Command>();

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(commands, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(counter, is(1010000));

		String report = new ScriptReportHelper().reportScriptSteps(result, new Scope(), null)
				.toXMLDocument();
		String line = findLineMatching(report, ".*<script .*action=\"Run\" .*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(line, containsString("result=\"ok\""));
		assertThat(report, containsString("<msg><![CDATA[empty command list]]>\n"));
	}

	@Test
	public void testSingleCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button OK Click");

		CommandServer server = new CommandServer(PORT);
		Response resp = processor.runCommand(cmd);
		server.stop();

		assertThat(resp.getStatus(), is(ResponseStatus.OK));
	}

	@Test
	public void testSingleCommentCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("# some comment");

		CommandServer server = new CommandServer(PORT);
		Response resp = processor.runCommand(cmd);
		server.stop();

		assertThat(resp.getCode(), is(200));
		assertThat(resp.getStatus(), is(ResponseStatus.OK));
		assertThat(resp.getMessage(), is("ignore comment"));
	}

	@Test
	public void testSingleDocVarsCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Doc * Vars first=\"contact's first name\"");

		CommandServer server = new CommandServer(PORT);
		Response resp = processor.runCommand(cmd);
		server.stop();

		assertThat(resp.getCode(), is(200));
		assertThat(resp.getStatus(), is(ResponseStatus.OK));
		assertThat(resp.getMessage(), is("ignore doc.vars"));
	}

	@Test
	public void testSingleDocScriptCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Doc * Script \"enter usename and password, then login\"");

		CommandServer server = new CommandServer(PORT);
		Response resp = processor.runCommand(cmd);
		server.stop();

		assertThat(resp.getCode(), is(200));
		assertThat(resp.getStatus(), is(ResponseStatus.OK));
		assertThat(resp.getMessage(), is("ignore doc.script"));
	}

	@Test
	public void testRunScriptWithSingleCommand() throws Exception {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button OK Click");

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(101));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button OK Click");

		assertThat(server.getRawJSONCommands(), notNullValue());
		assertThat(server.getRawJSONCommands().size(), is(1));

		String json = server.getRawJSONCommands().get(0);
		assertThat(json, containsString("\"mtcommand\":\"PLAY\""));
		assertThat(json, containsString("\"componentType\":\"Button\""));
		assertThat(json, containsString("\"monkeyId\":\"OK\""));
		assertThat(json, containsString("\"action\":\"Click\""));
		assertThat(json, containsString("\"args\":[]"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(
				findLineMatching(report,
						".*<script  comp=\"Script\" id=\"[^\"]*\" .*action=\"Run\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<cmd .*Button OK Click.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithSingleComment() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("# some comment");

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(0));
		assertThat(result, nullValue());

		server.assertCommands();
	}

	@Test
	public void testRunScriptWithSingleIgnoredCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button FOO Tap %ignore=true");

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(101));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("ignored"));

		server.assertCommands();
	}

	@Test
	public void testRunScriptWithSingleScript() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		Command cmd = new Command("Script foo.mt Run");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(1010202));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Click");

		String report = new ScriptReportHelper().reportScriptSteps(result,
				new Scope(foo.getName()), null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + foo.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<cmd .*Button FOO Click.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithSingleScriptWithDefaultTimings() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		Command cmd = new Command("Script foo.mt Run");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(1010202));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands(true, "Button FOO Click %thinktime=500 %timeout=2000");

		String report = new ScriptReportHelper().reportScriptSteps(result,
				new Scope(foo.getName()), null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + foo.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(
				findLineMatching(report,
						".*<cmd .*Button FOO Click %thinktime=500 %timeout=2000.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithSingleScriptWithCustomTimings() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		Command cmd = new Command("Script foo.mt Run");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setGlobalTimeout(6543);
		processor.setGlobalThinktime(1234);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(1010202));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands(true, "Button FOO Click %thinktime=1234 %timeout=6543");

		String report = new ScriptReportHelper().reportScriptSteps(result,
				new Scope(foo.getName()), null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + foo.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(
				findLineMatching(report,
						".*<cmd .*Button FOO Click %thinktime=1234 %timeout=6543.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithSingleScriptWithVars() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define x\nButton ${x} Click", dir);
		Command cmd = new Command("Script foo.mt Run \"Bo Bo\"");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(1010303));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button \"Bo Bo\" Click");

		String report = new ScriptReportHelper().reportScriptSteps(result,
				new Scope(foo.getName()), null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + foo.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(2));
		assertThat(
				findLineMatching(report, ".*<cmd .*Button &quot;Bo Bo&quot; Click.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithSingleScriptWithDatafile() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "first,last\nJoe,Smith\n\"Bo Bo\",Baker\n", dir);
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		Command cmd = new Command("Script foo.mt RunWith data.csv");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat(counter, is(2020808));
		assertThat(result, notNullValue());
		assertThat(result.toString(), is("OK : " + "2" + " data records processed"));

		server.assertCommands("Input firstName EnterText Joe", "Input lastName EnterText Smith",
				"Input firstName EnterText \"Bo Bo\"", "Input lastName EnterText Baker");

		String report = new ScriptReportHelper().reportScriptSteps(result, null, null)
				.toXMLDocument();
		String line;

		// dummy script holder due to command-interface to processor
		line = findLineMatching(report, ".*<script .*action=\"Run\" .*");
		assertThat(line, notNullValue());

		// RunWith invocations
		line = findLineMatching(report, ".*<script .*action=\"RunWith\" .*dataIndex=\"1\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("args=\"data.csv[@1]\""));
		assertThat(line, containsString("id=\"foo.mt\""));
		line = findLineMatching(report, ".*<script .*action=\"RunWith\" .*dataIndex=\"1\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("args=\"data.csv[@1]\""));
		assertThat(line, containsString("id=\"foo.mt\""));

		// commands
		assertThat(countOccurences(report, "<cmd "), is(6));
	}

	@Test
	public void testRunVarsDefineCommand() throws Exception {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Vars * Define foo=123 bar=654 baz=778"));

		Scope scope = new Scope();
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(commands, scope);
		server.stop();

		assertThat(counter, is(1010101));
		assertThat(scope.getVariables().size(), is(3));
		assertThat(scope.getVariables().keySet(), hasItems("foo", "bar", "baz"));
		assertThat(scope.getVariables().values(), hasItems("123", "654", "778"));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String report = new ScriptReportHelper().reportScriptSteps(result, null, null)
				.toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(
				findLineMatching(report,
						".*<cmd .*Vars \\* Define foo=123 bar=654 baz=778.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunVarsDefineCommandWithNoDefaults() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Vars * Define foo bar baz"));

		Scope scope = new Scope();
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(commands, scope);
		server.stop();

		assertThat(counter, is(1010101));
		assertThat(scope.getVariables().size(), is(3));
		assertThat(scope.getVariables().keySet(), hasItems("foo", "bar", "baz"));
		assertThat(scope.getVariables().values(), hasItems("<foo>", "<bar>", "<baz>"));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
	}

	@Test
	public void testRunVarsDefineCommandWithSpaces() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Vars * Define foo=\"some val\" bar=\"other val\""));

		Scope scope = new Scope();
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(commands, scope);
		server.stop();

		assertThat(counter, is(1010101));
		assertThat(scope.getVariables().size(), is(2));
		assertThat(scope.getVariables().keySet(), hasItems("foo", "bar"));
		assertThat(scope.getVariables().get("foo"), is("some val"));
		assertThat(scope.getVariables().get("bar"), is("other val"));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
	}

	@Test
	public void testRunBadVarsDefineCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Vars * Define foo=123 bar654 baz=778"));

		Scope scope = new Scope();
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(commands, scope);
		server.stop();

		assertThat(counter, is(1010101));
		assertThat(scope.getVariables().size(), is(3));
		assertThat(scope.getVariables().keySet(), hasItems("foo", "bar654", "baz"));
		assertThat(scope.getVariables().values(), hasItems("123", "<bar654>", "778"));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
	}

	@Test
	public void testRunMultipleCommands() throws Exception {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Button OK Click"));
		commands.add(new Command("Button OK Click 17 33"));
		commands.add(new Command("Input firstName EnterText \"Joe Bob\""));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript(commands, null);
		server.stop();

		assertThat(counter, is(1010303));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String report = new ScriptReportHelper().reportScriptSteps(result, null, null)
				.toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(findLineMatching(report, ".*<cmd .*Button OK Click [^1].*result=\"ok\".*"),
				notNullValue());
		assertThat(findLineMatching(report, ".*<cmd .*Button OK Click 17 33.*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*Input firstName EnterText &quot;Joe Bob&quot;.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithRecursion() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt Run", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(2020404));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*"),
				notNullValue());
		assertThat(findLineMatching(report, ".*<script .*id=\"" + foo.getName() + "\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(
				findLineMatching(report,
						".*<cmd .*Vars \\* Define first=foo last=&quot;bar baz&quot;.*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*Input firstName EnterText foo.*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*Input lastName EnterText &quot;bar baz&quot;.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithDeepRecursion() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File doo = tempScript("doo.mt", "Input firstName EnterText freedom\n"
				+ "Script foo.mt Run\n" + "Input lastName EnterText llaasstt", dir);
		File script = tempScript("script.mt", "Script doo.mt Run", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(doo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(3030707));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String report = new ScriptReportHelper().reportScriptSteps(result, new Scope("script.mt"),
				null).toXMLDocument();
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + foo.getName()
				+ "\" action=\"Run\""));
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + doo.getName()
				+ "\" action=\"Run\""));
		assertThat(report, containsString("<script  comp=\"Script\" id=\"" + script.getName()
				+ "\" action=\"Run\""));
		assertThat(countOccurences(report, "<cmd "), is(5));
	}

	@Test
	public void testRunScriptWithNoVarsDefineDefaults() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first last\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt Run", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(2020404));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Input firstName EnterText <first>",
				"Input lastName EnterText <last>");

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*"),
				notNullValue());
		assertThat(findLineMatching(report, ".*<script .*id=\"" + foo.getName() + "\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(findLineMatching(report, ".*<cmd .*Vars \\* Define first last .*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*Input firstName EnterText &lt;first&gt;.*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*Input lastName EnterText &lt;last&gt;.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithArgsAndNoVarsDefineDefaults() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first last\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt Run * \"Bo Bo\"", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(2020404));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Input firstName EnterText <first>",
				"Input lastName EnterText \"Bo Bo\"");
	}

	@Test
	public void testRunScriptWithDatafile() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "first,last\nJoe,Smith\n\"Bo Bo\",Baker\n", dir);
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(3030808));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Input firstName EnterText Joe", "Input lastName EnterText Smith",
				"Input firstName EnterText \"Bo Bo\"", "Input lastName EnterText Baker");

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(4));
		assertThat(findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<script .*id=\"" + foo.getName()
						+ "\".*action=\"RunWith\".*data.csv[^\\[].*"), notNullValue());
		assertThat(
				findLineMatching(report, ".*<script .*id=\"" + foo.getName()
						+ "\".*action=\"RunWith\".*data.csv\\[@1\\].*dataIndex=\"1\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<script .*id=\"" + foo.getName()
						+ "\".*action=\"RunWith\".*data.csv\\[@2\\].*dataIndex=\"2\".*"),
				notNullValue());
		assertThat(countOccurences(report, "dataIndex="), is(2));
		assertThat(countOccurences(report, "dataIndex=\"1\""), is(1));
		assertThat(countOccurences(report, "dataIndex=\"2\""), is(1));
		assertThat(countOccurences(report, "<cmd "), is(6));
		assertThat(
				findLineMatching(report,
						".*<cmd .*Vars \\* Define first=foo last=&quot;bar baz&quot;.*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*Input firstName EnterText Joe.*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*Input lastName EnterText Smith.*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*Input firstName EnterText &quot;Bo Bo&quot;.*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*Input lastName EnterText Baker.*result=\"ok\".*"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithNullDatafile() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(1010000));
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Script foo.mt RunWith' must have a datafile as its first arg"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		String line = findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("result=\"error\""));
		line = findLineMatching(report, ".*<script .*comp=\"Script\" .*id=\"" + foo.getName()
				+ "\".*action=\"RunWith\" .*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("result=\"error\""));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg"), is(1));
		assertThat(
				report,
				containsString("<msg><![CDATA[command 'Script foo.mt RunWith' must have a datafile as its first arg]]>"));
	}

	@Test
	public void testRunScriptWithMissingDatafile() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith missing.csv", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(1010000));
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("datafile 'missing.csv' not found"));
	}

	@Test
	public void testRunScriptWithEmptyDatafile() throws IOException {
		File dir = tempDir();
		File data = tempScript("data.csv", "first,last\n", dir);
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(1010000));
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("datafile 'data.csv' has no data"));
	}

	@Test
	public void testRunScriptWithBadDatafile() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "foo,bar\nJoe,Smith\n\"Bo Bo\",Baker\n", dir);
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(2020202));
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				containsString("datafile 'data.csv' is missing column 'first' from the header row"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(3));
		assertThat(findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<script .*id=\"" + foo.getName()
						+ "\".*args=\"data.csv\".*"), notNullValue());
		assertThat(
				findLineMatching(report, ".*<script .*id=\"" + foo.getName()
						+ "\".*args=\"data.csv\\[@1\\]\".*dataIndex=\"1\".*"), notNullValue());
		assertThat(countOccurences(report, "dataIndex="), is(1));
		assertThat(countOccurences(report, "<cmd "), is(1));
		String line = findLineMatching(report, ".*<cmd .*comp=\"Vars\" .*action=\"Define\" .*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("result=\"error\""));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(
				report,
				containsString("<msg><![CDATA[datafile 'data.csv' is missing column 'first' from the header row]]>\n"));
	}

	@Test
	public void testRunScriptWithUnicodeDatafile() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv",
				"first,last\nHé\u21D0\u21D1o,Smith\n\"é ì í \u21D0 \u21D1\",Baker\n", dir);
		File foo = tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input name EnterText \"${first} ${last}\"", dir);
		File script = tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(script.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(3030606));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input name EnterText \"Hé\u21D0\u21D1o Smith\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input name EnterText \"é ì í \u21D0 \u21D1 Baker\""));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(4));
		assertThat(findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*"),
				notNullValue());
		String line = findLineMatching(report, ".*<script .*action=\"RunWith\" .*dataIndex=\"1\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("args=\"data.csv[@1]\""));
		assertThat(line, containsString("id=\"foo.mt\""));
		line = findLineMatching(report, ".*<script .*action=\"RunWith\" .*dataIndex=\"2\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("args=\"data.csv[@2]\""));
		assertThat(line, containsString("id=\"foo.mt\""));
		assertThat(countOccurences(report, "dataIndex="), is(2));
		assertThat(countOccurences(report, "dataIndex=\"1\""), is(1));
		assertThat(countOccurences(report, "dataIndex=\"2\""), is(1));
		assertThat(countOccurences(report, "<cmd "), is(4));
		line = findLineMatching(
				report,
				".*<cmd .*action=\"EnterText\" .*raw=\"Input name EnterText &quot;&#233; &#236; &#237; &#8656; &#8657; Baker&quot;.*result=\"ok\".*");
		assertThat(line, notNullValue());
		line = findLineMatching(
				report,
				".*<cmd .*action=\"EnterText\" .*raw=\"Input name EnterText &quot;H&#233;&#8656;&#8657;o Smith&quot;.*result=\"ok\".*");
		assertThat(line, notNullValue());
	}

	@Test
	public void testRunScriptWithSuiteCommands() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File script = tempScript("script.mt", "Test foo.mt Run", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(1010000));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				containsString("command 'test.run' is only allowed in a suite"));
	}

	@Test
	public void testRunScriptWithWaitForCommands() throws Exception {
		File dir = tempDir();

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		CommandServer server;
		PlaybackResult result;

		// ///////////////////
		tempScript("foo.mt", "Button OK waitFor\nButton OK waitFor", dir);
		server = new CommandServer(PORT);
		counter = 0;
		result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010202));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		// ///////////////////
		tempScript("foo.mt", "Button OK waitFor\nButton OK waitFor 100", dir);
		server = new CommandServer(PORT);
		counter = 0;
		result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010202));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		// ///////////////////
		tempScript("foo.mt", "Button OK waitFor -1", dir);
		server = new CommandServer(PORT);
		counter = 0;
		result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010000));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				containsString("must have a number of seconds to wait greater than zero, found: -1"));

		// ///////////////////
		tempScript("foo.mt", "Button OK waitFor doodoo", dir);
		server = new CommandServer(PORT);
		counter = 0;
		result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010000));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				containsString("must have a number of seconds to wait as its first arg, found: doodoo"));
	}

	@Test
	public void testCustomCommand() throws Exception {
		File dir = tempDir();
		File custom = tempScript("comp.action.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Comp * Action Joe \"Bo Bo\"", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(custom.getName()));
		assertThat(processor.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(2020404));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Input firstName EnterText Joe", "Input lastName EnterText \"Bo Bo\"");

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(findLineMatching(report, ".*<script .*id=\"" + script.getName() + "\".*"),
				notNullValue());
		String line = findLineMatching(report,
				".*<script .*comp=\"Comp\".*action=\"Action\" .*args=\"Joe &quot;Bo Bo&quot;\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(3));
		line = findLineMatching(report,
				".*<cmd .*action=\"EnterText\" .*raw=\"Input firstName EnterText Joe .*result=\"ok\".*");
		assertThat(line, notNullValue());
		line = findLineMatching(
				report,
				".*<cmd .*action=\"EnterText\" .*raw=\"Input lastName EnterText &quot;Bo Bo&quot; .*result=\"ok\".*");
		assertThat(line, notNullValue());
	}

	@Test
	public void testCustomCommandAsScript() throws Exception {
		File dir = tempDir();
		File custom = tempScript("comp.action.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}", dir);
		File script = tempScript("script.mt", "Script comp.action Run Joe \"Bo Bo\"", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(custom.getName()));
		assertThat(processor.toString(), containsString(script.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat(counter, is(2020404));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Input firstName EnterText Joe", "Input lastName EnterText \"Bo Bo\"");
	}

	@Test
	public void testScriptWithUnicode() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Héìíô\u21D0\u21D1\u21DD\u21DC Click", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010101));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button Héìíô\u21D0\u21D1\u21DD\u21DC Click");
	}

	@Test
	public void testScriptWithEmptyVarsDefine() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define\nButton FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010101));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'vars.define' must define at least one variable"));
	}

	@Test
	public void testScriptWithIllegalVariableInVarsDefine() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define x x1 y_ 1y _z\nButton FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010101));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'vars.define' has illegal variable '1y' -- variables must begin with a letter and contain only letters, numbers, and underscores"));
	}

	@Test
	public void testRunScriptWithError() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton JOE Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("error on Joe"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(findLineMatching(report, ".*<script .*id=\"foo.mt\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(2));
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"JOE\".*result=\"error\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[error on Joe\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithFailure() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FRED Tap\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		FailOnFredServer server = new FailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), is("fail on Fred"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("Button FRED Tap -> FAILURE : fail on Fred"));
		assertThat(output, containsString("COMPLETE : FAILURE : fail on Fred"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(findLineMatching(report, ".*<script .*id=\"foo.mt\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(2));
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*comp=\"Button\".*id=\"FRED\".*result=\"failure\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[fail on Fred\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testFailureWithShouldFail() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FRED Tap %shouldfail=true\nButton BAR Tap",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		FailOnFredServer server = new FailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());

		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(
				output,
				containsString("Button FRED Tap %shouldfail=true -> OK : expected failure : fail on Fred"));
		assertThat(output, containsString("Button BAR Tap -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(findLineMatching(report, ".*<script .*id=\"foo.mt\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FRED\".*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"BAR\".*result=\"ok\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(
				findLineMatching(report,
						".*<msg><!\\[CDATA\\[expected failure : fail on Fred\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testErrorWithShouldFail() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton JOE Tap %shouldfail=true\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("error on Joe"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output,
				containsString("Button JOE Tap %shouldfail=true -> ERROR : error on Joe"));
		assertThat(output, containsString("COMPLETE : ERROR : error on Joe"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(findLineMatching(report, ".*<script .*id=\"foo.mt\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(2));
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"JOE\".*result=\"error\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[error on Joe\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testOkWithShouldFail() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap %shouldfail=true\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		FailOnFredServer server = new FailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), is("expected failure, but was OK"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %shouldfail=true %screenshotonerror=false"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("Button FOO Tap %shouldfail=true -> FAILURE : expected failure, but was OK"));
		assertThat(output, containsString("COMPLETE : FAILURE : expected failure, but was OK"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(findLineMatching(report, ".*<script .*id=\"foo.mt\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(
				findLineMatching(report,
						".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"failure\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(
				findLineMatching(report, ".*<msg><!\\[CDATA\\[expected failure, but was OK\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testRunScriptWithGlobalScreenshotOnErrorFalse() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setGlobalScreenshotOnError(false);

		FailOnFredServer server = new FailOnFredServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %screenshotonerror=false"));
	}

	@Test
	public void testRunScriptWithGlobalScreenshotOnErrorFalseOverriddenByCommand()
			throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap %screenshotonerror=true", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setGlobalScreenshotOnError(false);

		FailOnFredServer server = new FailOnFredServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO Tap %screenshotonerror=true"));
	}

	@Test
	public void testRunScriptWithIgnore() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap %ignore=true\nButton BAZ Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		counter = 0;
		processor.setPlaybackListener(LISTENER_WITH_COUNTER);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat(counter, is(1010303));
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap", "Button BAZ Tap");

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(findLineMatching(report, ".*<script .*id=\"foo.mt\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"ok\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report,
						".*<cmd .*comp=\"Button\".*id=\"BAR\".*%ignore=true\".*result=\"skipped\".*"),
				notNullValue());
		assertThat(
				findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*result=\"ok\".*"),
				notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[ignored\\]\\]>"), notNullValue());
	}

	@Test
	public void testRunScriptWithScreenshot() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nDevice * Screenshot\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		ScreenshotServer server = new ScreenshotServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap", "Device * Screenshot", "Button BAR Tap");

		File screenshotsDir = new File(dir, "screenshots");
		assertThat(screenshotsDir.exists(), is(true));
		assertThat(Arrays.asList(screenshotsDir.list()), everyItem(containsString("screenshot")));
	}

	@Test
	public void testRunScriptWithEscapes() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Label * VerifyRegex \"\\w+ \\w*\" prop");

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript(cmd, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Label * VerifyRegex \"\\w+ \\w*\" prop");

		assertThat(server.getRawJSONCommands(), notNullValue());
		assertThat(server.getRawJSONCommands().size(), is(1));

		String json = server.getRawJSONCommands().get(0);
		assertThat(json, containsString("\"mtcommand\":\"PLAY\""));
		assertThat(json, containsString("\"componentType\":\"Label\""));
		assertThat(json, containsString("\"monkeyId\":\"*\""));
		assertThat(json, containsString("\"action\":\"VerifyRegex\""));
		assertThat(json, containsString("\"args\":[\"\\\\w+ \\\\w*\",\"prop\"]"));
	}

	@Test
	public void testRunScriptWithVarsVerify() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nVars * Verify 123 foo\nButton FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap");
		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 -> OK"));
		assertThat(output, containsString("Vars * Verify 123 foo -> OK"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testDebugReporting() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nDebug * Print don't miss this\nDebug * Vars",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands(); // no server commands
		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 -> OK"));
		assertThat(output, containsString("Debug * Print don't miss this -> OK"));
		assertThat(output, containsString("\ndon't miss this\n"));
		assertThat(output, containsString("Debug * Vars -> OK"));
		assertThat(output, containsString("\nfoo=123\n"));
		assertThat(output, containsString("COMPLETE : OK"));

		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(0));
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(1));
		String line = findLineMatching(report, ".*<script.*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd "), is(3));
		line = findLineMatching(report,
				".*<cmd.*comp=\\\"Vars\\\".*id=\\\"*\\\".*action=\\\"Define\\\".*"
						+ "args=\\\"foo=123\\\".*" + "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line = findLineMatching(report,
				".*<cmd.*comp=\\\"Debug\\\".*id=\\\"*\\\".*action=\\\"Print\\\".*"
						+ "args=\\\"don&apos;t miss this\\\".*" + "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line = findLineMatching(report,
				".*<cmd.*comp=\\\"Debug\\\".*id=\\\"*\\\".*action=\\\"Vars\\\".*"
						+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(line.contains("args="), is(false));
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(countOccurences(report, "<debug>"), is(2));
		assertThat(report, containsString("<debug><![CDATA[don't miss this]]>\n"));
		assertThat(report, containsString("<debug><![CDATA[foo=123\n]]>\n"));

	}

	private class ScreenshotServer extends CommandServer {

		public ScreenshotServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {

			Response resp = super.serve(uri, method, headers, json);

			if ("device.screenshot".equals(new Command(json).getCommandName())) {
				try {
					resp = new Response(
							resp.getStatus(),
							resp.getBody(),
							resp.getHeaders(),
							Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAABdJREFUeNpi+s/AwPCfgYkRSDH+BwgwABcpAwRXSDQWAAAAAElFTkSuQmCC"));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			return resp;
		}
	}

	private class CommandServer extends com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer {
		public CommandServer(int port) throws IOException {
			super(port);
		}

		public void assertCommands(String... cmds) {
			assertCommands(false, cmds);
		}

		public void assertCommands(boolean showDefaultTimings, String... cmds) {
			assertThat(getCommands(), notNullValue());
			assertThat(getCommands().size(), is(cmds.length));
			for (int i = 0; i < cmds.length; i++) {
				assertThat(getCommands().get(i).getCommand(showDefaultTimings), is(cmds[i]));
			}
		}
	}
}