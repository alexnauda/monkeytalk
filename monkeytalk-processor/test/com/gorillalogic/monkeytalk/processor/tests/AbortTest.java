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
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Runner;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.SuiteListener;
import com.gorillalogic.monkeytalk.processor.SuiteProcessor;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class AbortTest extends TestHelper {
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
			output += (output.length() > 0 ? "\n" : "") + "START\n";
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

	private static final SuiteListener SUITE_LISTENER = new SuiteListener() {

		@Override
		public void onRunStart(int total) {
			output = "RUN (" + total + ")\n";
		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {
			output += "RUN_COMPLETE : " + result + "\n";
		}

		@Override
		public void onTestStart(String name, int num, int total) {
			output += "TEST " + name + " (" + num + " of " + total + ")\n";
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
			output += "TEST_COMPLETE : " + result + "\n";
		}

		@Override
		public void onSuiteStart(int total) {
			output += "SUITE\n";
		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
			output += "SUITE_COMPLETE : " + result + "\n";
		}
	};

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Before
	public void before() {
		output = "";
	}

	@Test
	public void testScriptAbort() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		processor.abort();
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(output.toString(), containsString("Button FOO Tap -> OK"));
		assertThat(output.toString(), containsString("COMPLETE : ERROR : playback aborted"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[playback aborted\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testScriptAbortRunWith() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define name\nButton ${name} Tap", dir);
		tempScript("bar.mt", "Button BAR Tap\nScript foo.mt RunWith data.csv\nButton BAZ Tap", dir);
		tempScript("data.csv", "name\nFOO1\nSTOP2\nFOO3", dir);

		final ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					processor.abort();
				}
			}
		});

		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("bar.mt");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted: 2 data records processed"));

		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO1 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button STOP2 Tap"));

		assertThat(output.toString(), containsString("START\nButton BAR Tap -> OK\n"));
		assertThat(output.toString(), containsString("Script foo.mt RunWith data.csv\n"
				+ "START\nVars * Define name -> OK\nButton FOO1 Tap -> OK\nCOMPLETE : OK -> OK\n"));
		assertThat(output.toString(), containsString("Script foo.mt RunWith data.csv\n"
				+ "START\nVars * Define name -> OK\nButton STOP2 Tap -> OK\n"
				+ "COMPLETE : ERROR : playback aborted -> OK\n"));
		assertThat(output.toString(),
				containsString("COMPLETE : ERROR : playback aborted: 2 data records processed"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(4));
		assertThat(countOccurences(report, "<cmd "), is(5));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[playback aborted\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testScriptAbortInChildScript() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nButton STOP Tap\nButton BAR Tap", dir);
		tempScript("outer.mt", "Button OUT Tap\nScript foo.mt Run", dir);

		final ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					processor.abort();
				}
			}
		});

		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("outer.mt");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted"));

		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button OUT Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button STOP Tap"));

		assertThat(output.toString(), containsString("Button OUT Tap -> OK"));
		assertThat(output.toString(), containsString("Script foo.mt Run\nSTART"));
		assertThat(output.toString(), containsString("Button FOO Tap -> OK"));
		assertThat(output.toString(), containsString("Button STOP Tap -> OK"));
		assertThat(output.toString(), containsString("COMPLETE : ERROR : playback aborted"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"OUT\".*"),
				notNullValue());
		assertThat(findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"FOO\".*"),
				notNullValue());
		assertThat(findLineMatching(report, ".*<cmd .*comp=\"Button\".*id=\"STOP\".*"),
				notNullValue());
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[playback aborted\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testSuiteAbort() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("mysuite.mts", "Test foo.mt Run\nTest bar.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setSuiteListener(SUITE_LISTENER);
		processor.abort();
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));

		assertThat(output.toString(), containsString("RUN (2)\n"));
		assertThat(output.toString(), containsString("SUITE\n"));
		assertThat(output.toString(), containsString("TEST foo.mt (1 of 2)\n"));
		assertThat(output.toString(), containsString("TEST_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("SUITE_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("RUN_COMPLETE : ERROR : playback aborted\n"));
	}

	@Test
	public void testSuiteAbortRunWith() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define name\nButton ${name} Tap", dir);
		tempScript("bar.mt", "Button %{1} Tap", dir);
		tempScript("mysuite.mts",
				"Test bar.mt Run BAR1\nTest foo.mt RunWith data.csv\nTest bar.mt Run BAR2", dir);
		tempScript("data.csv", "name\nFOO1\nSTOP2\nFOO3", dir);

		final SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					processor.abort();
				}
			}
		});

		processor.setSuiteListener(SUITE_LISTENER);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted"));

		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button BAR1 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO1 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button STOP2 Tap"));

		assertThat(output.toString(), containsString("RUN (5)\n"));
		assertThat(output.toString(), containsString("SUITE\n"));
		assertThat(output.toString(),
				containsString("TEST bar.mt[BAR1] (1 of 5)\nTEST_COMPLETE : OK\n"));
		assertThat(output.toString(),
				containsString("TEST foo.mt[name='FOO1'] (1 of 5)\nTEST_COMPLETE : OK\n"));
		assertThat(
				output.toString(),
				containsString("TEST foo.mt[name='STOP2'] (2 of 5)\nTEST_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), not(containsString("FOO3")));
		assertThat(output.toString(), containsString("SUITE_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("RUN_COMPLETE : ERROR : playback aborted\n"));
	}

	@Test
	public void testSuiteOfSuitesAbort() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button STOP Tap\nButton BAR Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		tempScript("outer.mts", "Test foo.mt Run\nSuite inner.mts Run", dir);
		tempScript("inner.mts", "Test bar.mt Run\nTest baz.mt Run\nTest baz.mt Run", dir);

		final SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					processor.abort();
				}
			}
		});

		processor.setSuiteListener(SUITE_LISTENER);
		PlaybackResult result = processor.runSuite("outer.mts");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted"));

		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button STOP Tap"));

		assertThat(output.toString(), containsString("RUN (4)\n"));
		assertThat(output.toString(), containsString("SUITE\n"));
		assertThat(output.toString(), containsString("TEST foo.mt (1 of 4)\n"
				+ "TEST_COMPLETE : OK\n"));
		assertThat(output.toString(), containsString("TEST bar.mt (1 of 3)\n"
				+ "TEST_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("SUITE_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("RUN_COMPLETE : ERROR : playback aborted\n"));
	}

	@Test
	public void testSuiteCallingSuiteCallingTestCallingScriptAbort() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap\nScript fred.mt Run", dir);
		tempScript("fred.mt", "Button STOP Tap\nButton FRED Tap", dir);
		tempScript("outer.mts", "Test foo.mt Run\nSuite inner.mts Run", dir);
		tempScript("inner.mts", "Test bar.mt Run\nTest baz.mt Run", dir);

		final SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					processor.abort();
				}
			}
		});

		processor.setSuiteListener(SUITE_LISTENER);
		PlaybackResult result = processor.runSuite("outer.mts");
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("playback aborted"));

		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button STOP Tap"));

		assertThat(output.toString(), containsString("RUN (3)\n"));
		assertThat(output.toString(), containsString("SUITE\n"));
		assertThat(output.toString(), containsString("TEST foo.mt (1 of 3)\n"
				+ "TEST_COMPLETE : OK\n"));
		assertThat(output.toString(), containsString("TEST bar.mt (1 of 2)\n"
				+ "TEST_COMPLETE : OK\n"));
		assertThat(output.toString(), containsString("TEST baz.mt (2 of 2)\n"
				+ "TEST_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("SUITE_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("RUN_COMPLETE : ERROR : playback aborted\n"));
	}

	@Test
	public void testRunnerScriptAbort() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO1 Tap\nButton STOP2 Tap\nButton FOO3 Tap", dir);

		final Runner runner = new Runner("iOS", HOST, PORT);
		runner.setScriptListener(LISTENER_WITH_OUTPUT);
		runner.setVerbose(true);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					runner.abort();
				}
			}
		});

		PlaybackResult result = runner.run(foo, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO1 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button STOP2 Tap"));

		assertThat(output.toString(), containsString("START\n"));
		assertThat(output.toString(), containsString("Button FOO1 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button STOP2 Tap -> OK\n"));
		assertThat(output.toString(), not(containsString("FOO3")));
		assertThat(output.toString(), containsString("COMPLETE : ERROR : playback aborted"));
	}

	@Test
	public void testRunnerSuiteAbort() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO1 Tap\nButton STOP2 Tap\nButton FOO3 Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		File mysuite = tempScript("mysuite.mts",
				"Test bar.mt Run\nTest foo.mt Run\nTest baz.mt Run", dir);

		final Runner runner = new Runner("iOS", HOST, PORT);
		runner.setSuiteListener(SUITE_LISTENER);
		runner.setVerbose(true);

		CallbackServer server = new CallbackServer(PORT, new CallbackListener() {
			@Override
			public void onCommand(Command cmd) {
				// if we find 'STOP', then trigger an abort
				if (cmd.getCommand().contains("STOP")) {
					runner.abort();
				}
			}
		});

		PlaybackResult result = runner.run(mysuite, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO1 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button STOP2 Tap"));

		assertThat(output.toString(), containsString("RUN (3)\n"));
		assertThat(output.toString(), containsString("SUITE\n"));
		assertThat(output.toString(), containsString("TEST bar.mt (1 of 3)\nTEST_COMPLETE : OK\n"));
		assertThat(output.toString(),
				containsString("TEST foo.mt (2 of 3)\nTEST_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), not(containsString("baz.mt")));
		assertThat(output.toString(), containsString("SUITE_COMPLETE : ERROR : playback aborted\n"));
		assertThat(output.toString(), containsString("RUN_COMPLETE : ERROR : playback aborted\n"));
	}

	public interface CallbackListener {
		void onCommand(Command cmd);
	}

	private class CallbackServer extends CommandServer {
		private CallbackListener callbackListener;

		public CallbackServer(int port, CallbackListener callbackListener) throws IOException {
			super(port);
			this.callbackListener = callbackListener;
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {

			if (callbackListener != null) {
				callbackListener.onCommand(new Command(json));
			}
			return super.serve(uri, method, headers, json);
		}
	}
}