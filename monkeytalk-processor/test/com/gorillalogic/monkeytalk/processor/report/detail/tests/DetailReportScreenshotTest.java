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
import static org.hamcrest.CoreMatchers.not;
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
import com.gorillalogic.monkeytalk.processor.report.detail.DetailReportHtml;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.Base64;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class DetailReportScreenshotTest extends TestHelper {
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
	public void testAfterScreenshot() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setTakeAfterScreenshot(true);

		CommandServer server = new ScreenshotServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(3));
		assertThat(cmds.get(0).getCommand(), is("Device * Screenshot"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(2).getCommand(), is("Device * Screenshot"));

		assertThat(output, containsString("START\nButton FOO Tap -> OK\nCOMPLETE : OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(xml, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
		assertThat(xml, containsString("beforeScreenshot=\"screenshots/"));
		assertThat(xml, containsString("afterScreenshot=\"screenshots/"));

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("<title>DETAIL-foo.mt.html</title>"));
		assertThat(html, containsString("<h1>DETAIL-foo.mt.html</h1>"));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, not(containsString("<div id=\"extra1\"")));
		assertThat(html, containsString("<div id=\"extra2\" style=\"display:none;\">"));
		assertThat(
				html,
				containsString("<div class=\"offset2 span11\">\n<p class=\"extra\">before<br /><img src=\"screenshots/"));
		assertThat(
				html,
				containsString("<div class=\"span11\">\n<p class=\"extra\">after<br /><img src=\"screenshots/"));
		assertThat(html, not(containsString("battery")));
	}

	@Test
	public void testAfterMetrics() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setTakeAfterMetrics(true);

		CommandServer server = new ScreenshotServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(1).getCommand(), is("Device * Get dummy allinfo"));

		assertThat(output, containsString("START\nButton FOO Tap -> OK\nCOMPLETE : OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(xml, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
		assertThat(xml, containsString("memory=\"12%\""));
		assertThat(xml, containsString("cpu=\"34%\""));
		assertThat(xml, containsString("diskspace=\"56%\""));
		assertThat(xml, containsString("battery=\"78%\""));

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("<title>DETAIL-foo.mt.html</title>"));
		assertThat(html, containsString("<h1>DETAIL-foo.mt.html</h1>"));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, not(containsString("<div id=\"extra1\"")));
		assertThat(html, containsString("<div id=\"extra2\" style=\"display:none;\">"));
		assertThat(html, not(containsString("<p class=\"extra\">before")));
		assertThat(html, not(containsString("<p class=\"extra\">after")));
		assertThat(html, containsString("<div class=\"offset2 span22\">\n"
				+ "<p class=\"extra\"><b>memory</b> 12% &mdash; <b>cpu</b> 34% &mdash; "
				+ "<b>storage</b> 56% &mdash; <b>battery</b> 78%</p>\n</div>"));
	}

	@Test
	public void testAfterScreenshotAndAfterMetrics() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setTakeAfterScreenshot(true);
		processor.setTakeAfterMetrics(true);

		CommandServer server = new ScreenshotServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(4));
		assertThat(cmds.get(0).getCommand(), is("Device * Screenshot"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(2).getCommand(), is("Device * Get dummy allinfo"));
		assertThat(cmds.get(3).getCommand(), is("Device * Screenshot"));

		assertThat(output, containsString("START\nButton FOO Tap -> OK\nCOMPLETE : OK"));

		String xml = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(xml, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"));
		assertThat(xml, containsString("beforeScreenshot=\"screenshots/"));
		assertThat(xml, containsString("afterScreenshot=\"screenshots/"));

		String html = new DetailReportHtml().createDetailReportHtml(result, xml);
		assertThat(html, containsString("<title>DETAIL-foo.mt.html</title>"));
		assertThat(html, containsString("<h1>DETAIL-foo.mt.html</h1>"));
		assertThat(html, containsString("1</span><b>Script</b> foo.mt <b>Run</b>"));
		assertThat(html, containsString("2</span><b>Button</b> FOO <b>Tap</b>"));
		assertThat(html, not(containsString("<div id=\"extra1\"")));
		assertThat(html, containsString("<div id=\"extra2\" style=\"display:none;\">"));
		assertThat(
				html,
				containsString("<div class=\"offset2 span11\">\n<p class=\"extra\">before<br /><img src=\"screenshots/"));
		assertThat(
				html,
				containsString("<div class=\"span11\">\n<p class=\"extra\">after<br /><img src=\"screenshots/"));
		assertThat(html, containsString("<div class=\"offset2 span22\">\n"
				+ "<p class=\"extra\"><b>memory</b> 12% &mdash; <b>cpu</b> 34% &mdash; "
				+ "<b>storage</b> 56% &mdash; <b>battery</b> 78%</p>\n</div>"));
	}

	private class ScreenshotServer extends TestHelper.CommandServer {
		private String img;

		public ScreenshotServer(int port) throws IOException {
			super(port);
			img = Base64.encodeFromFile("resources/test/base.png");
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			Response resp = super.serve(uri, method, headers, json);
			if (json.toString().toLowerCase().contains("screenshot")) {
				return new Response(HttpStatus.OK, "{result:\"OK\", screenshot:\"" + img + "\"}");
			} else if (json.toString().toLowerCase().contains("allinfo")) {
				return new Response(HttpStatus.OK, "{result:\"OK\", message:\"12%,34%,56%,78%\"}");
			} else {
				return resp;
			}
		}
	}
}