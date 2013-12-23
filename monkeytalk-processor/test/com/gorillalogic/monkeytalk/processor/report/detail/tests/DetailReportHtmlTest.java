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
package com.gorillalogic.monkeytalk.processor.report.detail.tests;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.SuiteProcessor;
import com.gorillalogic.monkeytalk.processor.report.detail.DetailReportHtml;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

@SuppressWarnings("unchecked")
public class DetailReportHtmlTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18027;
	private static String output;

	private static final PlaybackListener LISTENER_WITH_OUTPUT = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			output += scope.getCurrentCommand();
		}

		@Override
		public void onScriptStart(Scope scope) {
			output += "START\n";
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

	@Before
	public void before() {
		output = "";
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testSimpleScript() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nInput name EnterText FRED\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(3));

		assertThat(cmds.get(0).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(1).getCommand(), is("Input name EnterText FRED"));
		assertThat(cmds.get(2).getCommand(), is("Button BAR Tap"));

		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("Input name EnterText FRED -> OK"));
		assertThat(output, containsString("Button BAR Tap -> OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, not(containsString("class=\"offset2 span22\"")));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, containsString("3</span><b>Input</b> name <b>EnterText</b> FRED"));
		assertThat(html, containsString("4</span><b>Button</b> BAR <b>Tap</b>"));
	}

	@Test
	public void testQuotes() throws Exception {
		File dir = tempDir();

		// command 1: quoted monkeyId, quoted arg
		// command 2: monkeyId with escaped quote, arg with escaped quote
		tempScript("foo.mt", "Input \"FOO BAR\" EnterText \"FRED JOE\"\n"
				+ "Input Foo\\\"Bar EnterText Fred\\\"Joe", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Input \"FOO BAR\" EnterText \"FRED JOE\""));
		assertThat(cmds.get(1).getCommand(), is("Input Foo\\\"Bar EnterText Fred\\\"Joe"));

		assertThat(output, containsString("Input \"FOO BAR\" EnterText \"FRED JOE\" -> OK"));
		assertThat(output, containsString("Input Foo\\\"Bar EnterText Fred\\\"Joe -> OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, not(containsString("class=\"offset2 span22\"")));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(
				html,
				containsString("2</span><b>Input</b> &quot;FOO BAR&quot; <b>EnterText</b> &quot;FRED JOE&quot;"));
		assertThat(
				html,
				containsString("3</span><b>Input</b> Foo\\&quot;Bar <b>EnterText</b> Fred\\&quot;Joe"));
	}

	@Test
	public void testDeepScriptHierarchy() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR1 Tap\nScript foo.mt Run\nButton BAR2 Tap", dir);
		tempScript("baz.mt", "Button BAZ1 Tap\nScript bar.mt Run\nButton BAZ2 Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("baz.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(5));
		assertThat(cmds.get(0).getCommand(), is("Button BAZ1 Tap"));
		assertThat(cmds.get(1).getCommand(), is("Button BAR1 Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button BAR2 Tap"));
		assertThat(cmds.get(4).getCommand(), is("Button BAZ2 Tap"));

		assertThat(output, containsString("Button BAZ1 Tap -> OK"));
		assertThat(output, containsString("Button BAR1 Tap -> OK"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("Button BAR2 Tap -> OK"));
		assertThat(output, containsString("Button BAZ2 Tap -> OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
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
	public void testScriptWithDatafile() throws Exception {
		File dir = tempDir();
		tempScript("data.csv", "first,last\nJoe,Smith\n\"Bo Bo\",Baker\n", dir);
		tempScript("foo.mt", "Vars * Define first=foo last=\"bar baz\"\n"
				+ "Input name EnterText \"${first} ${last}\"", dir);
		tempScript("script.mt", "Script foo.mt RunWith data.csv", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("script.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Input name EnterText \"Joe Smith\""));
		assertThat(cmds.get(1).getCommand(), is("Input name EnterText \"Bo Bo Baker\""));

		assertThat(output, containsString("Vars * Define first=foo last=\"bar baz\" -> OK"));
		assertThat(output, containsString("Input name EnterText \"Joe Smith\" -> OK"));
		assertThat(output, containsString("Input name EnterText \"Bo Bo Baker\" -> OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(html, not(containsString("class=\"offset4 span20\"")));
		assertThat(
				html,
				allOf(containsString("1</span><b>Script</b> script.mt <b>Run</b>"),
						containsString("2</span><b>Script</b> foo.mt <b>RunWith</b> data.csv"),
						containsString("3</span><b>Script</b> foo.mt <b>RunWith</b> data.csv[@1]"),
						containsString("4</span><b>Vars</b> * <b>Define</b> first=foo last=&quot;bar baz&quot;"),
						containsString("5</span><b>Input</b> name <b>EnterText</b> &quot;Joe Smith&quot;"),
						containsString("6</span><b>Script</b> foo.mt <b>RunWith</b> data.csv[@2]"),
						containsString("7</span><b>Vars</b> * <b>Define</b> first=foo last=&quot;bar baz&quot;"),
						containsString("8</span><b>Input</b> name <b>EnterText</b> &quot;Bo Bo Baker&quot;")));
	}

	@Test
	public void testScriptWithError() throws Exception {
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

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("<b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, containsString("<span class=\"text-success\">ok</span>"));
		assertThat(html, containsString("<b>Button</b> JOE <b>Tap</b>"));
		assertThat(html, containsString("<span class=\"text-error\">error</span>"));
	}

	@Test
	public void testScriptWithFailure() throws Exception {
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

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("<b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, containsString("<span class=\"text-success\">ok</span>"));
		assertThat(html, containsString("<b>Button</b> FRED <b>Tap</b>"));
		assertThat(html, containsString("<span class=\"text-warning\">failure</span>"));
	}

	@Test
	public void testSimpleSuite() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton FOO2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(xml, containsString("tests=\"2\" errors=\"0\" failures=\"0\" skipped=\"0\""));

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, not(containsString("class=\"offset3 span21\"")));
		assertThat(
				html,
				allOf(containsString("1</span><b>Suite</b> mysuite.mts <b>Run</b>"),
						containsString("2</span><b>Test</b> foo.mt <b>Run</b>"),
						containsString("3</span><b>Button</b> FOO <b>Tap</b>"),
						containsString("4</span><b>Button</b> FOO2 <b>Tap</b>"),
						containsString("5</span><b>Button</b> FOO3 <b>Tap</b>"),
						containsString("6</span><b>Test</b> bar.mt <b>Run</b>"),
						containsString("7</span><b>Button</b> BAR <b>Tap</b")));
	}

	@Test
	public void testSuiteWithSetupAndTeardown() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Click", dir);
		tempScript("bar.mt", "Button BAR Click", dir);
		tempScript("baz.mt", "Button BAZ Click", dir);
		tempScript("up.mt", "Button SETUP Click", dir);
		tempScript("down.mt", "Button TEARDOWN Click", dir);
		File suite = tempScript(
				"suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nTest baz.mt Run\nSetup up.mt Run\nTeardown down.mt Run",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(9));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button TEARDOWN Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button BAR Click"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button TEARDOWN Click"));
		assertThat(server.getCommands().get(6).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(7).getCommand(), is("Button BAZ Click"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button TEARDOWN Click"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(
				html,
				allOf(containsString("1</span><b>Suite</b> suite.mts <b>Run</b>"),
						containsString("2</span><b>Test</b> foo.mt <b>Run</b>"),
						containsString("3</span><b>Setup</b> up.mt <b>Run</b>"),
						containsString("4</span><b>Button</b> SETUP <b>Click</b>"),
						containsString("5</span><b>Button</b> FOO <b>Click</b>"),
						containsString("6</span><b>Teardown</b> down.mt <b>Run</b>"),
						containsString("7</span><b>Button</b> TEARDOWN <b>Click</b>"),
						containsString("8</span><b>Test</b> bar.mt <b>Run</b>"),
						containsString("9</span><b>Setup</b> up.mt <b>Run</b>"),
						containsString("10</span><b>Button</b> SETUP <b>Click</b>"),
						containsString("11</span><b>Button</b> BAR <b>Click</b>"),
						containsString("12</span><b>Teardown</b> down.mt <b>Run</b>"),
						containsString("13</span><b>Button</b> TEARDOWN <b>Click</b>"),
						containsString("14</span><b>Test</b> baz.mt <b>Run</b>"),
						containsString("15</span><b>Setup</b> up.mt <b>Run</b>"),
						containsString("16</span><b>Button</b> SETUP <b>Click</b>"),
						containsString("17</span><b>Button</b> BAZ <b>Click</b>"),
						containsString("18</span><b>Teardown</b> down.mt <b>Run</b>"),
						containsString("19</span><b>Button</b> TEARDOWN <b>Click</b>")));
	}

	@Test
	public void testSuiteWithDataDrivenTest() throws Exception {
		File dir = tempDir();
		tempScript("data.csv", "name\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		tempScript("foo.mt", "Vars * Define name\nButton FOO-${name} Tap", dir);
		tempScript("up.mt", "Button UP Tap", dir);
		tempScript("down.mt", "Button DOWN Tap", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt RunWith data.csv\nSetup up.mt Run\nTeardown down.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(9));
		assertThat(cmds.get(0).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO-Joe Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(4).getCommand(), is("Button \"FOO-Bo Bo\" Tap"));
		assertThat(cmds.get(5).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(6).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(7).getCommand(), is("Button FOO-Charlie Tap"));
		assertThat(cmds.get(8).getCommand(), is("Button DOWN Tap"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(html, containsString("class=\"offset4 span20\""));
		assertThat(html, not(containsString("class=\"offset5 span19\"")));
		assertThat(html, containsString("1</span><b>Suite</b> suite.mts <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Test</b> foo.mt <b>RunWith</b> data.csv"));
		assertThat(html, containsString("3</span><b>Test</b> foo.mt <b>RunWith</b> data.csv[@1]"));
		assertThat(html, containsString("4</span><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("5</span><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("6</span><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("7</span><b>Button</b> FOO-Joe <b>Tap</b>"));
		assertThat(html, containsString("8</span><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("9</span><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("10</span><b>Test</b> foo.mt <b>RunWith</b> data.csv[@2]"));
		assertThat(html, containsString("11</span><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("12</span><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("13</span><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("14</span><b>Button</b> &quot;FOO-Bo Bo&quot; <b>Tap</b>"));
		assertThat(html, containsString("15</span><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("16</span><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("17</span><b>Test</b> foo.mt <b>RunWith</b> data.csv[@3]"));
		assertThat(html, containsString("18</span><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("19</span><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("20</span><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("21</span><b>Button</b> FOO-Charlie <b>Tap</b>"));
		assertThat(html, containsString("22</span><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("23</span><b>Button</b> DOWN <b>Tap</b>"));
	}

	@Test
	public void testSuiteWithDataDrivenTeardown() throws Exception {
		File dir = tempDir();
		tempScript("data.csv", "name\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		tempScript("foo.mt", "Button Test Click", dir);
		tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTeardown bar.mt RunWith data.csv",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Test Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button Joe Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button \"Bo Bo\" Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button Charlie Click"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(
				html,
				allOf(containsString("1</span><b>Suite</b> suite.mts <b>Run</b>"),
						containsString("2</span><b>Test</b> foo.mt <b>Run</b>"),
						containsString("3</span><b>Button</b> Test <b>Click</b>"),
						containsString("4</span><b>Teardown</b> bar.mt <b>RunWith</b> data.csv"),
						containsString("5</span><b>Teardown</b> bar.mt <b>RunWith</b> data.csv[@1]"),
						containsString("6</span><b>Button</b> Joe <b>Click</b>"),
						containsString("7</span><b>Teardown</b> bar.mt <b>RunWith</b> data.csv[@2]"),
						containsString("8</span><b>Button</b> &quot;Bo Bo&quot; <b>Click</b>"),
						containsString("9</span><b>Teardown</b> bar.mt <b>RunWith</b> data.csv[@3]"),
						containsString("10</span><b>Button</b> Charlie <b>Click</b>")));
	}

	@Test
	public void testSuiteWithErrorsFailuresSkips() throws Exception {
		File dir = tempDir();
		tempScript("err.csv", "name\nAlice\n\"Bo Bo\"\nJOE\nCharlie\n", dir);
		tempScript("fail.csv", "name\nDale\n\"Easy E\"\nFRED\nGeorge\n", dir);
		tempScript("foo.mt", "Vars * Define name\nButton FOO-${name} Tap", dir);
		tempScript("up.mt", "Button UP Tap", dir);
		tempScript("down.mt", "Button DOWN Tap", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run FOO\nTest foo.mt Run FOO2 %ignore=true\n"
						+ "Test foo.mt RunWith err.csv\nTest foo.mt RunWith fail.csv\n"
						+ "Setup up.mt Run\nTeardown down.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(25));
		assertThat(cmds.get(0).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO-FOO Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(4).getCommand(), is("Button FOO-Alice Tap"));
		assertThat(cmds.get(5).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(6).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(7).getCommand(), is("Button \"FOO-Bo Bo\" Tap"));
		assertThat(cmds.get(8).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(9).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(10).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(11).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(12).getCommand(), is("Button FOO-Charlie Tap"));
		assertThat(cmds.get(13).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(14).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(15).getCommand(), is("Button FOO-Dale Tap"));
		assertThat(cmds.get(16).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(17).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(18).getCommand(), is("Button \"FOO-Easy E\" Tap"));
		assertThat(cmds.get(19).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(20).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(21).getCommand(), is("Button DOWN Tap"));
		assertThat(cmds.get(22).getCommand(), is("Button UP Tap"));
		assertThat(cmds.get(23).getCommand(), is("Button FOO-George Tap"));
		assertThat(cmds.get(24).getCommand(), is("Button DOWN Tap"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		html = html.replaceAll("span", "s").replaceAll("\"", "'");
		assertThat(html, containsString("class='s24'"));
		assertThat(html, containsString("class='offset1 s23'"));
		assertThat(html, containsString("class='offset2 s22'"));
		assertThat(html, containsString("class='offset3 s21'"));
		assertThat(html, containsString("class='offset4 s20'"));
		assertThat(html, not(containsString("class='offset5 s19'")));

		assertThat(
				html,
				containsString("class='pbox pbox-error'><s class='idx'>1</s><b>Suite</b> suite.mts <b>Run</b>"));
		assertThat(html, containsString("<s>10 tests</s>"));
		assertThat(html, containsString("<s class='text-error'>1 error</s>"));
		assertThat(html, containsString("<s class='text-warning'>1 failure</s>"));
		assertThat(html, containsString("<s class='text-info'>1 skipped</s>"));

		assertThat(
				html,
				containsString("pbox pbox-success'><s class='idx'>10</s><b>Test</b> foo.mt <b>RunWith</b> err.csv"));
		assertThat(html, containsString("3</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("4</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("5</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("6</s><b>Button</b> FOO-FOO <b>Tap</b>"));
		assertThat(html, containsString("7</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("8</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("9</s><b>Test</b> foo.mt <b>Run</b> FOO2"));
		assertThat(html, containsString("10</s><b>Test</b> foo.mt <b>RunWith</b> err.csv"));
		assertThat(html, containsString("11</s><b>Test</b> foo.mt <b>RunWith</b> err.csv[@1]"));
		assertThat(html, containsString("12</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("13</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("14</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("15</s><b>Button</b> FOO-Alice <b>Tap</b>"));
		assertThat(html, containsString("16</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("17</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("18</s><b>Test</b> foo.mt <b>RunWith</b> err.csv[@2]"));
		assertThat(html, containsString("19</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("20</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("21</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("22</s><b>Button</b> &quot;FOO-Bo Bo&quot; <b>Tap</b>"));
		assertThat(html, containsString("23</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("24</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("25</s><b>Test</b> foo.mt <b>RunWith</b> err.csv[@3]"));
		assertThat(html, containsString("26</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("27</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("28</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("29</s><b>Button</b> FOO-JOE <b>Tap</b>"));
		assertThat(html, containsString("30</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("31</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("32</s><b>Test</b> foo.mt <b>RunWith</b> err.csv[@4]"));
		assertThat(html, containsString("33</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("34</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("35</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("36</s><b>Button</b> FOO-Charlie <b>Tap</b>"));
		assertThat(html, containsString("37</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("38</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("39</s><b>Test</b> foo.mt <b>RunWith</b> fail.csv"));
		assertThat(html, containsString("40</s><b>Test</b> foo.mt <b>RunWith</b> fail.csv[@1]"));
		assertThat(html, containsString("41</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("42</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("43</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("44</s><b>Button</b> FOO-Dale <b>Tap</b>"));
		assertThat(html, containsString("45</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("46</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("47</s><b>Test</b> foo.mt <b>RunWith</b> fail.csv[@2]"));
		assertThat(html, containsString("48</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("49</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("50</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("51</s><b>Button</b> &quot;FOO-Easy E&quot; <b>Tap</b>"));
		assertThat(html, containsString("52</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("53</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("54</s><b>Test</b> foo.mt <b>RunWith</b> fail.csv[@3]"));
		assertThat(html, containsString("55</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("56</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("57</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("58</s><b>Button</b> FOO-FRED <b>Tap</b>"));
		assertThat(html, containsString("59</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("60</s><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("61</s><b>Test</b> foo.mt <b>RunWith</b> fail.csv[@4]"));
		assertThat(html, containsString("62</s><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("63</s><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("64</s><b>Vars</b> * <b>Define</b> name"));
		assertThat(html, containsString("65</s><b>Button</b> FOO-George <b>Tap</b>"));
		assertThat(html, containsString("66</s><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("67</s><b>Button</b> DOWN <b>Tap</b>"));
	}

	@Test
	public void testSuiteOfSuites() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("foo2.mt", "Button FOO2 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("bar2.mt", "Button BAR2 Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		tempScript("suite.mts", "Test foo.mt Run\nSuite s2.mts Run\nTest foo2.mt Run", dir);
		tempScript("s2.mts", "Test bar.mt Run\nSuite s3.mts Run\nTest bar2.mt Run", dir);
		tempScript("s3.mts", "Test baz.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(5));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR2 Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button FOO2 Tap"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(html, containsString("class=\"offset4 span20\""));
		assertThat(html, not(containsString("class=\"offset5 span19\"")));
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
	public void testSuiteOfSuitesComplex() throws Exception {
		File dir = tempDir();
		tempScript("data.csv", "name\nAlice\n\"Bo Bo\"\nCharlie", dir);
		tempScript("up.mt", "Button UP Tap", dir);
		tempScript("down.mt", "Button DOWN Tap", dir);
		tempScript("u2.mt", "Button UP2 Tap", dir);
		tempScript("d2.mt", "Button DOWN2 Tap", dir);
		tempScript("u3.mt", "Button UP3 Tap", dir);
		tempScript("d3.mt", "Button DOWN3 Tap", dir);
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("foo2.mt", "Button FOO2 Tap", dir);
		tempScript("bar.mt", "Button BAR${name} Tap", dir);
		tempScript("bar2.mt", "Button BAR2 Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		tempScript("baz2.mt", "Button BAZ2 Tap", dir);
		tempScript("suite.mts",
				"Test foo.mt Run\nSuite s2.mts Run\nTest foo2.mt Run %ignore=setup,teardown\n"
						+ "Setup up.mt Run\nTeardown down.mt Run", dir);
		tempScript("s2.mts",
				"Test bar.mt RunWith data.csv %ignore=setup\nSuite s3.mts Run\nTest bar2.mt Run %ignore=teardown\n"
						+ "Setup u2.mt Run\nTeardown d2.mt Run", dir);
		tempScript("s3.mts", "Test baz.mt Run\nTest baz2.mt Run %ignore=true\n"
				+ "Setup u3.mt Run\nTeardown d3.mt Run\nSuite s4.mts Run %ignore=true", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(15));
		assertThat(server.getCommands().get(0).getCommand(), is("Button UP Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button DOWN Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BARAlice Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button DOWN2 Tap"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button \"BARBo Bo\" Tap"));
		assertThat(server.getCommands().get(6).getCommand(), is("Button DOWN2 Tap"));
		assertThat(server.getCommands().get(7).getCommand(), is("Button BARCharlie Tap"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button DOWN2 Tap"));
		assertThat(server.getCommands().get(9).getCommand(), is("Button UP3 Tap"));
		assertThat(server.getCommands().get(10).getCommand(), is("Button BAZ Tap"));
		assertThat(server.getCommands().get(11).getCommand(), is("Button DOWN3 Tap"));
		assertThat(server.getCommands().get(12).getCommand(), is("Button UP2 Tap"));
		assertThat(server.getCommands().get(13).getCommand(), is("Button BAR2 Tap"));
		assertThat(server.getCommands().get(14).getCommand(), is("Button FOO2 Tap"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("class=\"offset2 span22\""));
		assertThat(html, containsString("class=\"offset3 span21\""));
		assertThat(html, containsString("class=\"offset4 span20\""));
		assertThat(html, containsString("class=\"offset5 span19\""));
		assertThat(html, not(containsString("class=\"offset6 span18\"")));

		assertThat(html, containsString("1</span><b>Suite</b> suite.mts <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Test</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("3</span><b>Setup</b> up.mt <b>Run</b>"));
		assertThat(html, containsString("4</span><b>Button</b> UP <b>Tap</b>"));
		assertThat(html, containsString("5</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, containsString("6</span><b>Teardown</b> down.mt <b>Run</b>"));
		assertThat(html, containsString("7</span><b>Button</b> DOWN <b>Tap</b>"));
		assertThat(html, containsString("8</span><b>Suite</b> s2.mts <b>Run</b>"));
		assertThat(html, containsString("9</span><b>Test</b> bar.mt <b>RunWith</b>"));
		assertThat(html, containsString("10</span><b>Test</b> bar.mt <b>RunWith</b> data.csv[@1]"));
		assertThat(html, containsString("11</span><b>Button</b> BARAlice <b>Tap</b>"));
		assertThat(html, containsString("12</span><b>Teardown</b> d2.mt <b>Run</b>"));
		assertThat(html, containsString("13</span><b>Button</b> DOWN2 <b>Tap</b>"));
		assertThat(html, containsString("14</span><b>Test</b> bar.mt <b>RunWith</b> data.csv[@2]"));
		assertThat(html, containsString("15</span><b>Button</b> &quot;BARBo Bo&quot; <b>Tap</b>"));
		assertThat(html, containsString("16</span><b>Teardown</b> d2.mt <b>Run</b>"));
		assertThat(html, containsString("17</span><b>Button</b> DOWN2 <b>Tap</b>"));
		assertThat(html, containsString("18</span><b>Test</b> bar.mt <b>RunWith</b> data.csv[@3]"));
		assertThat(html, containsString("19</span><b>Button</b> BARCharlie <b>Tap</b>"));
		assertThat(html, containsString("20</span><b>Teardown</b> d2.mt <b>Run</b>"));
		assertThat(html, containsString("21</span><b>Button</b> DOWN2 <b>Tap</b>"));
		assertThat(html, containsString("22</span><b>Suite</b> s3.mts <b>Run</b>"));
		assertThat(html, containsString("23</span><b>Test</b> baz.mt <b>Run</b>"));
		assertThat(html, containsString("24</span><b>Setup</b> u3.mt <b>Run</b>"));
		assertThat(html, containsString("25</span><b>Button</b> UP3 <b>Tap</b>"));
	}

	@Test
	public void testScriptWithUnicode() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Héìíô\u21D0\u21D1\u21DD\u21DC Click", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("class=\"span24\""));
		assertThat(html, containsString("class=\"offset1 span23\""));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Button</b> Héìíô⇐⇑⇝⇜ <b>Click</b>"));
	}

	private class ErrorOnJoeAndFailOnFredServer extends TestHelper.CommandServer {
		public ErrorOnJoeAndFailOnFredServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			if (json.toString().toLowerCase().contains("joe")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\",message:\"error on Joe\"}");
			} else if (json.toString().toLowerCase().contains("fred")) {
				return new Response(HttpStatus.OK, "{result:\"FAILURE\",message:\"fail on Fred\"}");
			} else {
				return super.serve(uri, method, headers, json);
			}
		}
	}
}