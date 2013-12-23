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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.JSProcessor;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class JSProcessorTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18028;
	private ScriptProcessor processor;
	private JSProcessor jsprocessor;
	private ByteArrayOutputStream out;
	private File dir;

	@Before
	public void before() throws IOException {
		dir = FileUtils.tempDir();
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		processor = new ScriptProcessor(HOST, PORT, dir);
		jsprocessor = new JSProcessor(processor);
	}

	@After
	public void after() {
		dir = null;
		out = null;
		processor = null;
		jsprocessor = null;
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testToString() {
		assertThat(jsprocessor, notNullValue());
		assertThat(jsprocessor.toString(), containsString("http://" + HOST + ":" + PORT + "/"));
	}

	@Test
	public void testRunNullJavascript() throws Exception {
		PlaybackResult result = jsprocessor.runJavascript(null);

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("command is null"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "result=\"error\""), is(1));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[command is null\\]\\]>"),
				notNullValue());
	}

	@Test
	public void testRunMissingJavascript() throws Exception {
		Command cmd = new Command("Script missing.js Run");
		PlaybackResult result = jsprocessor.runJavascript(cmd);

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("script 'missing.js' not found"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "result=\"error\""), is(2));
		assertThat(
				findLineMatching(report, ".*<msg><!\\[CDATA\\[script 'missing.js' not found\\]\\]>"),
				notNullValue());
	}

	@Ignore
	@Test
	public void testRunJavascript() throws Exception {
		tempScript("foo.js", "dummy js", dir);
		Command cmd = new Command("Script foo.js Run");

		PlaybackResult result = jsprocessor.runJavascript(cmd);

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("app.foo().run();"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "result=\"error\""), is(0));
		assertThat(countOccurences(report, "result=\"failure\""), is(0));
		assertThat(countOccurences(report, "result=\"ok\""), is(2));
		assertThat(findLineMatching(report, ".*<msg><!\\[CDATA\\[app\\.foo\\(\\)\\.run\\(\\);.*"),
				notNullValue());
	}

	@Ignore
	@Test
	public void testRunJavascriptWithArgs() throws Exception {
		tempScript("foo.js", "dummy js", dir);
		Command cmd = new Command("Script foo.js Run Joe \"Bo Bo\" 12345");

		PlaybackResult result = jsprocessor.runJavascript(cmd);

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("app.foo().run(\"Joe\", \"Bo Bo\", \"12345\");"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "result=\"error\""), is(0));
		assertThat(countOccurences(report, "result=\"failure\""), is(0));
		assertThat(countOccurences(report, "result=\"ok\""), is(2));
		assertThat(
				findLineMatching(report,
						".*<msg><!\\[CDATA\\[app\\.foo\\(\\)\\.run\\(\"Joe\", \"Bo Bo\", \"12345\"\\);.*"),
				notNullValue());
	}

	@Ignore
	@Test
	public void testRunJavascriptWithStarredArg() throws Exception {
		tempScript("foo.js", "dummy js", dir);
		Command cmd = new Command("Script foo.js Run * \"Bo Bo\" 12345");

		PlaybackResult result = jsprocessor.runJavascript(cmd);

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("app.foo().run(\"*\", \"Bo Bo\", \"12345\");"));

		String report = new ScriptReportHelper().reportScriptSteps(result).toXMLDocument();
		assertThat(countOccurences(report, "<script "), is(2));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "result=\"error\""), is(0));
		assertThat(countOccurences(report, "result=\"failure\""), is(0));
		assertThat(countOccurences(report, "result=\"ok\""), is(2));
		assertThat(
				findLineMatching(report,
						".*<msg><!\\[CDATA\\[app\\.foo\\(\\)\\.run\\(\"\\*\", \"Bo Bo\", \"12345\"\\);.*"),
				notNullValue());
	}
}