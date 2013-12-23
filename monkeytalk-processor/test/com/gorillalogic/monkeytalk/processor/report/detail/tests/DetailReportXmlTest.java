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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class DetailReportXmlTest extends TestHelper {
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
	public void testRunScript() throws Exception {
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

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "idx="), is(4));
		assertThat(countOccurences(report, "idx=\"1\""), is(1));
		assertThat(countOccurences(report, "idx=\"2\""), is(1));
		assertThat(countOccurences(report, "idx=\"3\""), is(1));
		assertThat(countOccurences(report, "idx=\"4\""), is(1));
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(report, containsString("Button FOO Tap"));
		assertThat(report, containsString("Input name EnterText FRED"));
		assertThat(report, containsString("Button BAR Tap"));
	}

	@Test
	public void testRunScriptWithDatafile() throws Exception {
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

		assertThat(output, containsString("Vars * Define first=foo last=\"bar baz\""));
		assertThat(output, containsString("Input name EnterText \"Joe Smith\" -> OK"));
		assertThat(output, containsString("Input name EnterText \"Bo Bo Baker\" -> OK"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(4));
		assertThat(countOccurences(report, "dataIndex="), is(2));
		assertThat(countOccurences(report, "dataIndex=\"1\""), is(1));
		assertThat(countOccurences(report, "dataIndex=\"2\""), is(1));
		assertThat(countOccurences(report, "<cmd "), is(4));
		assertThat(countOccurences(report, "<suite "), is(0));
		assertThat(report, containsString("Input name EnterText &quot;Joe Smith&quot;"));
		assertThat(report, containsString("Input name EnterText &quot;Bo Bo Baker&quot;"));
	}
}