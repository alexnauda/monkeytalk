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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.SuiteListener;
import com.gorillalogic.monkeytalk.processor.SuiteProcessor;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class SuiteProcessorTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18026;
	private CommandServer server;
	private static int counter;

	private static final SuiteListener LISTENER_WITH_COUNTER = new SuiteListener() {

		@Override
		public void onRunStart(int total) {

		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {

		}

		@Override
		public void onTestStart(String name, int num, int total) {
			counter += 1;
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
			counter += 100;
			assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		}

		@Override
		public void onSuiteStart(int total) {
			counter += 1000000;

		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
			counter += 10000;

		}
	};

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testDefaultConstructor() {
		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, (File) null);
		assertThat(processor, notNullValue());
		assertThat(processor.toString(), containsString("SuiteProcessor:"));
		assertThat(processor.getWorld(), notNullValue());
	}

	@Test
	public void testRunSuite() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		CommandWorld world = processor.getWorld();
		assertThat(world, notNullValue());
		assertThat(world.toString(), containsString(suite.getName()));
		assertThat(world.toString(), containsString(foo.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<suite.*id=\\\"" + suite.getName() + "\\\".*result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*id=\\\"" + foo.getName() + "\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"OK\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
	}

	@Test
	public void testRunNullSuite() throws Exception {
		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, (File) null);
		PlaybackResult result = processor.runSuite(null);
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("suite filename is null"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		//assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<suite ") + countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		//assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<.*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[suite filename is null\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunMissingSuite() throws Exception {
		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, (File) null);
		PlaybackResult result = processor.runSuite("missing.mts");
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("suite 'missing.mts' not found"));

		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"missing.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"
				+ "tests=\\\"0\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[suite 'missing.mts' not found\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunScriptAsSuite() throws Exception {
		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, (File) null);
		PlaybackResult result = processor.runSuite("script.mt");
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("running script 'script.mt' as a suite is not allowed"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"script.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"
				+ "tests=\\\"0\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[running script 'script.mt' as a suite is not allowed\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunEmptySuite() throws Exception {
		File dir = tempDir();
		File suite = tempScript("suite.mts", "", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("suite '" + suite.getName() + "' is empty"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"
				+ "tests=\\\"0\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[suite '" + suite.getName() + "' is empty\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunSuiteWithIllegalCommand() throws Exception {
		File dir = tempDir();
		File suite = tempScript("suite.mts", "Button OK Click", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'button.click' is illegal -- only Test, Setup, Teardown, and Suite are allowed"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"
				+ "tests=\\\"0\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[command 'button.click' is illegal -- only Test, Setup, Teardown, and Suite are allowed\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunSuiteMustNotContainScriptCommand() throws Exception {
		File dir = tempDir();
		File suite = tempScript("suite.mts", "Script foo.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'script.run' is illegal -- only Test, Setup, Teardown, and Suite are allowed"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(1));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"
				+ "tests=\\\"0\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<script.*comp=\\\"Script\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[command 'script.run' is illegal -- only Test, Setup, Teardown, and Suite are allowed\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunSuiteWithComment() throws Exception {
		File dir = tempDir();
		File suite = tempScript("mysuite.mts", "# suite comment", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString("mysuite.mts"));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertThat(
				xml,
				is("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
						+ "<testsuite name=\"mysuite\" tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(0));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"mysuite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"0\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
	}

	@Test
	public void testRunSuiteWithMissingTest() throws Exception {
		File dir = tempDir();
		File suite = tempScript("suite.mts", "Test missing.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"missing.mt"));
		assertThat(xml, containsString("error message=\"script &apos;missing.mt&apos; not found\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"missing.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[script 'missing.mt' not found\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunSuiteWithEmptyTest() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("error message=\"script &apos;" + foo.getName() + "&apos; is empty\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(countOccurences(report, "<warning>"), is(0));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[script 'foo.mt' is empty\\]\\]>.*"), notNullValue());
	}

	@Test
	public void testRunSuiteWithError() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button JOE Tap", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("message=\"error on Joe\""));
		assertThat(xml, containsString("at Button JOE Tap (foo.mt : cmd #1)"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"JOE\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[error on Joe\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithFailure() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FRED Tap", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		FailOnFredServer server = new FailOnFredServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 1, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("message=\"fail on Fred\""));
		assertThat(xml, containsString("at Button FRED Tap (foo.mt : cmd #1)"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"1\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"failure\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(1));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FRED\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"failure\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[fail on Fred\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithData() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 3, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("name=&apos;Joe&apos;"));
		assertThat(xml, containsString("name=&apos;Bo Bo&apos;"));
		assertThat(xml, containsString("name=&apos;Charlie&apos;"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"3\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(4));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@1\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"1\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@2\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"2\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@3\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"3\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(3));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"OK\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"OK\""), is(3));
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithMissingDataArg() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt RunWith", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(
				xml,
				containsString("error message=\"datafile arg missing in command &apos;Test foo.mt RunWith&apos;\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"" + suite.getName() + "\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[datafile arg missing in command 'Test foo.mt RunWith'\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithMissingData() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt RunWith missing.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("error message=\"datafile &apos;missing.csv&apos; not found\""));
	}

	@Test
	public void testRunSuiteWithEmptyData() throws IOException {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\n", dir);
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("error message=\"datafile &apos;data.csv&apos; has no data\""));
	}

	@Test
	public void testRunSuiteWithErrorOnData() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\nJOE\n\"Bo Bo\"\nCharlie\n", dir);
		File foo = tempScript("foo.mt",
				"Vars * Define name=\"default name\"\nInput name EnterText ${name}", dir);
		File suite = tempScript("suite.mts", "Test foo.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 3, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("name=&apos;JOE&apos;"));
		assertThat(xml, containsString("error message=\"error on Joe\""));
		assertThat(xml, containsString("name=&apos;Bo Bo&apos;"));
		assertThat(xml, containsString("name=&apos;Charlie&apos;"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"3\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(4));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@1\\]\\\".*"
				+ "result=\\\"error\\\".*dataIndex=\\\"1\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@2\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"2\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@3\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"3\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(6));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Vars\\\".*id=\\\"[*]\\\".*action=\\\"Define\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Vars\" id=\"*\""), is(3));
		assertThat(countOccurences(report, "args=\"name=&quot;default name&quot;\""), is(3));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Input\\\".*id=\\\"name\\\".*action=\\\"EnterText\\\".*"
				+ "args=\\\"JOE\\\".*result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Input\\\".*id=\\\"name\\\".*action=\\\"EnterText\\\".*"
				+ "args=\\\"&quot;Bo Bo&quot;\\\".*result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Input\\\".*id=\\\"name\\\".*action=\\\"EnterText\\\".*"
				+ "args=\\\"Charlie\\\".*result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[error on Joe\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithSetup() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button Setup Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nSetup bar.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Setup Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button Test Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(1));
		assertThat(findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(2));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Setup\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenSetup() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nSetup bar.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Joe Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button Charlie Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button Test Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(4));
		assertThat(findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@1\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"1\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@2\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"2\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@3\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"3\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(4));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Joe\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Bo Bo\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Charlie\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenSetupWithMissingArg() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nSetup bar.mt RunWith", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(
				xml,
				containsString("error message=\"datafile arg missing in command &apos;Setup bar.mt RunWith&apos;\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(1));
		String line=findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(line.contains("args="), is(false));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[datafile arg missing in command 'Setup bar.mt RunWith'\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenSetupWithMissingData() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nSetup bar.mt RunWith missing.csv",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("error message=\"datafile &apos;missing.csv&apos; not found\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(1));
		String line=findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(line.contains("args=\"missing.csv\""), is(true));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(0));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[datafile 'missing.csv' not found\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenSetupWithEmptyData() throws IOException {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\n", dir);
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nSetup bar.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("error message=\"datafile &apos;data.csv&apos; has no data\""));
	}

	@Test
	public void testRunSuiteWithTeardown() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button Teardown Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTeardown bar.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Test Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button Teardown Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(1));
		assertThat(findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(2));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Teardown\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenTeardown() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTeardown bar.mt RunWith data.csv",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button Test Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button Joe Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button \"Bo Bo\" Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button Charlie Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(4));
		assertThat(findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@1\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"1\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@2\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"2\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "\\\"data.csv\\[@3\\]\\\".*"
				+ "result=\\\"ok\\\".*dataIndex=\\\"3\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(4));
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Joe\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Bo Bo\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Charlie\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenTeardownWithMissingArg() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTeardown bar.mt RunWith", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(
				xml,
				containsString("error message=\"datafile arg missing in command &apos;Teardown bar.mt RunWith&apos;"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(1));
		String line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(line.contains("args="), is(false));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(1));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[datafile arg missing in command 'Teardown bar.mt RunWith'\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenTeardownWithMissingData() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run\nTeardown bar.mt RunWith missing.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("error message=\"datafile &apos;missing.csv&apos; not found\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(1));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("args=\"missing.csv\""));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(1));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[datafile 'missing.csv' not found\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithDataDrivenTeardownWithEmptyData() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\n", dir);
		File foo = tempScript("foo.mt", "Button Test Click", dir);
		File bar = tempScript("bar.mt", "Button ${name} Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTeardown bar.mt RunWith data.csv",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("error message=\"datafile &apos;data.csv&apos; has no data\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(1));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("args=\"data.csv\""));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(1));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Test\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[datafile 'data.csv' has no data\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithSetupAndTeardown() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File baz = tempScript("baz.mt", "Button BAZ Click", dir);
		File up = tempScript("up.mt", "Button SETUP Click", dir);
		File down = tempScript("down.mt", "Button TEARDOWN Click", dir);
		File suite = tempScript(
				"suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nTest baz.mt Run\nSetup up.mt Run\nTeardown down.mt Run",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(baz.getName()));
		assertThat(processor.toString(), containsString(up.getName()));
		assertThat(processor.toString(), containsString(down.getName()));

		processor.setSuiteListener(LISTENER_WITH_COUNTER);

		counter = 0;
		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(counter, is(1010303));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 3, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

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
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"3\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(3));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"baz.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(3));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Setup\\\".*id=\\\"up.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"up.mt\""), is(3));
		assertThat(countOccurences(report, "<teardown "), is(3));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"down.mt\""), is(3));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(9));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAZ\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"SETUP\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"SETUP\" action=\"Click\""), is(3));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"TEARDOWN\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"TEARDOWN\" action=\"Click\""), is(3));
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithFailingTest() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File joe = tempScript("joe.mt", "Button JOE Click", dir);
		File up = tempScript("up.mt", "Button SETUP Click", dir);
		File down = tempScript("down.mt", "Button TEARDOWN Click", dir);
		File suite = tempScript(
				"suite.mts",
				"Test foo.mt Run\nTest joe.mt Run\nTest bar.mt Run\nSetup up.mt Run\nTeardown down.mt Run",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(joe.getName()));
		assertThat(processor.toString(), containsString(up.getName()));
		assertThat(processor.toString(), containsString(down.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 3, 1, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(8));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button TEARDOWN Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button TEARDOWN Click"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(6).getCommand(), is("Button BAR Click"));
		assertThat(server.getCommands().get(7).getCommand(), is("Button TEARDOWN Click"));
				
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"3\\\" errors=\\\"1\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(3));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"joe.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(3));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Setup\\\".*id=\\\"up.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"up.mt\""), is(3));
		assertThat(countOccurences(report, "<teardown "), is(3));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"down.mt\""), is(3));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(9));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"JOE\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"SETUP\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"SETUP\" action=\"Click\""), is(3));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"TEARDOWN\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"TEARDOWN\" action=\"Click\""), is(3));
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[error on Joe\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithFailingSetup() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File up = tempScript("up.mt", "Button SETUP Click\nButton JOE Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTest bar.mt Run\nSetup up.mt Run",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(up.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 2, 2, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button SETUP Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button SETUP Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"2\\\" errors=\\\"2\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(2));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(2));
		line=findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"up.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"up.mt\""), is(2));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(4));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"SETUP\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"SETUP\" action=\"Click\""), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"JOE\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"JOE\" action=\"Click\""), is(2));
		assertThat(countOccurences(report, "<msg>"), is(2));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[error on Joe\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "error on Joe"), is(2));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithFailingDataDrivenSetup() throws IOException {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\nJohn\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File up = tempScript("up.mt", "Vars * Define name\nButton ${name} Click", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nSetup up.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(up.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 2, 2, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button John Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button John Click"));
	}

	@Test
	public void testRunSuiteWithFailingTeardown() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File down = tempScript("down.mt", "Button TEARDOWN Click\nButton JOE Click", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nTeardown down.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(down.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 2, 2, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button TEARDOWN Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAR Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button TEARDOWN Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();		
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"2\\\" errors=\\\"2\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(2));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(2));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"down.mt\""), is(2));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(6));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"TEARDOWN\" action=\"Click\""), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"TEARDOWN\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"TEARDOWN\" action=\"Click\""), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"JOE\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"JOE\" action=\"Click\""), is(2));
		assertThat(countOccurences(report, "<msg>"), is(2));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[error on Joe\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "error on Joe"), is(2));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithFailingDataDrivenTeardown() throws Exception {
		File dir = tempDir();
		File data = tempScript("data.csv", "name\nJohn\nJoe\n\"Bo Bo\"\nCharlie\n", dir);
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File down = tempScript("down.mt", "Vars * Define name\nButton ${name} Click", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nTeardown down.mt RunWith data.csv", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(down.getName()));
		assertThat(processor.toString(), containsString(data.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 2, 2, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button John Click"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAR Click"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button John Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();		
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"2\\\" errors=\\\"2\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(2));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(6));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report,"args=\"data.csv\""), is(2));
		
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\[@1\\]\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(line,containsString("dataIndex=\"1\""));
		assertThat(countOccurences(report,"args=\"data.csv[@1]\""), is(2));
		
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\[@2\\]\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(line,containsString("dataIndex=\"2\""));
		assertThat(countOccurences(report,"args=\"data.csv[@2]\""), is(2));

		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(10));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"FOO\" action=\"Click\""), is(1));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BAR\" action=\"Click\""), is(1));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Vars\\\".*id=\\\"\\*\\\".*action=\\\"Define\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Vars\" id=\"*\" action=\"Define\""), is(4));
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BAR\" action=\"Click\""), is(1));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"Joe\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"Joe\" action=\"Click\""), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"John\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"John\" action=\"Click\""), is(2));
		assertThat(countOccurences(report, "<msg>"), is(2));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[error on Joe\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "error on Joe"), is(2));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithBadSetupAction() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File up = tempScript("up.mt", "Button SETUP Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run\nTest bar.mt Run\nSetup up.mt RunX",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(up.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 2, 2, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("command 'setup.runx' is illegal"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));
	}

	@Test
	public void testRunSuiteWithBadTeardownAction() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Click", dir);
		File bar = tempScript("bar.mt", "Button BAR Click", dir);
		File down = tempScript("down.mt", "Button TEARDOWN Click", dir);
		File suite = tempScript("suite.mts",
				"Test foo.mt Run\nTest bar.mt Run\nTeardown down.mt RunX", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.toString(), containsString(bar.getName()));
		assertThat(processor.toString(), containsString(down.getName()));

		counter = 0;
		server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 2, 2, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("command 'teardown.runx' is illegal"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Click"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Click"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();		
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"2\\\" errors=\\\"2\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(2));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"error\\\".*"), notNullValue());
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(2));
		line=findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"RunX\\\".*"
				+ "result=\\\"error\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report,"<teardown  comp=\"Teardown\" id=\"down.mt\" action=\"RunX\""), is(2));
		
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<msg>"), is(2));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[command 'teardown.runx' is illegal -- only Teardown.Run and Teardown.RunWith are allowed\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "command 'teardown.runx' is illegal -- only Teardown.Run and Teardown.RunWith are allowed"), is(2));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithReportDir() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		File reportDir = new File(dir, "reports");
		reportDir.mkdir();
		assertThat(reportDir.exists(), is(true));

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(reportDir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.getReportFile(), nullValue());

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-suite.xml", reportDir);
		assertThat(processor.getReportFile().getAbsolutePath(), is(f.getAbsolutePath()));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
	}

	@Test
	public void testRunSuiteWithUncreatedReportDir() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		File reportDir = new File(dir, "reports");

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(reportDir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.getReportFile(), nullValue());

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		File f = FileUtils.findFile("TEST-suite.xml", reportDir);
		assertThat(processor.getReportFile().getAbsolutePath(), is(f.getAbsolutePath()));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
	}

	@Test
	public void testRunSuiteWithReportDirAsFile() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Click", dir);
		File suite = tempScript("suite.mts", "Test foo.mt Run", dir);

		File reportDir = foo;

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(reportDir);
		assertThat(processor.toString(), containsString(suite.getName()));
		assertThat(processor.toString(), containsString(foo.getName()));
		assertThat(processor.getReportFile(), nullValue());

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite(suite.getName());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("failed to save XML report - reportDir not a folder: "
				+ foo.getAbsolutePath()));
	}

	@Test
	public void testRunSuiteWithCounter() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button OK Click", dir);
		tempScript("suite.mts", "Test foo.mt Run\nTest foo.mt RunWith data.csv", dir);
		tempScript("data.csv", "var\nabc\n123\n\"def 456\"", dir);

		File reportDir = new File(dir, "reports");
		reportDir.mkdir();
		assertThat(reportDir.exists(), is(true));

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setSuiteListener(new SuiteListener() {

			@Override
			public void onSuiteStart(int total) {
				assertThat(total, is(4));
			}

			@Override
			public void onSuiteComplete(PlaybackResult result, Report report) {
			}

			@Override
			public void onTestStart(String name, int num, int total) {
				assertThat(total, is(4));
			}

			@Override
			public void onTestComplete(PlaybackResult result, Report report) {
				assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
			}

			@Override
			public void onRunStart(int total) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRunComplete(PlaybackResult result, Report report) {
				// TODO Auto-generated method stub

			}
		});
		processor.setReportDir(reportDir);

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 4, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo.mt\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt[var=&apos;abc&apos;]\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt[var=&apos;123&apos;]\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt[var=&apos;def 456&apos;]\""));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"4\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(5));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		assertThat(findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\\".*"
				+ "result=\\\"ok\\\".*"), notNullValue());
		line= findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\[@1\\]\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "dataIndex=\"1\""), is(1));
		line= findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\[@2\\]\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "dataIndex=\"2\""), is(1));
		line= findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"RunWith\\\".*"
				+ "args=\\\"data.csv\\[@3\\]\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "dataIndex=\"3\""), is(1));
		
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(4));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"OK\\\".*action=\\\"Click\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"OK\" action=\"Click\""), is(4));
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithIgnoredTest() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap %ignore=true", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		tempScript("suite.mts", "Test foo.mt Run\nTest bar.mt Run %ignore=true\nTest baz.mt Run",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 3, 0, 0, 1);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("<testcase name=\"bar"));
		assertThat(xml, containsString("<testcase name=\"baz"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button BAZ Tap"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"3\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"1\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(3));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"skipped\\\".*");
		assertThat(line, notNullValue());
		assertThat(line, containsString("%ignore=true"));
		assertThat(countOccurences(line, "args="), is(0));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"baz.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Tap\\\".*"
				+ " %ignore=true\\\".*"
				+ "result=\\\"skipped\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAZ\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(line.contains("ignore"), is(false));
		
		assertThat(countOccurences(report, "<msg>"), is(2));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[ignored\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<![CDATA[ignored]]>"), is(2));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithIgnoredSetup() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("up.mt", "Button UP Tap", dir);
		tempScript("down.mt", "Button DOWN Tap", dir);
		tempScript("suite.mts",
				"Test foo.mt Run\nSetup up.mt Run %ignore=true\nTeardown down.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button DOWN Tap"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(1));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		
		// assertThat(countOccurences(report, "<setup "), is(1)); // ignored setup and teardown commands are "removed" by Finder
		assertThat(countOccurences(report, "<teardown "), is(1));
		line = findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"foo.mt\" action=\"Run\""), is(1));

		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(2));
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		line=findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"DOWN\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(line.contains("ignore"), is(false));
		
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testRunSuiteWithIgnoredTeardown() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("up.mt", "Button UP Tap", dir);
		tempScript("down.mt", "Button DOWN Tap", dir);
		tempScript("suite.mts",
				"Test foo.mt Run\nSetup up.mt Run\nTeardown down.mt Run %ignore=true", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 1, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button UP Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Tap"));
	}

	@Test
	public void testRunSuiteWithTestsIgnoringSetupAndTeardown() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("baz.mt", "Button BAZ Tap", dir);
		tempScript("both.mt", "Button BOTH Tap", dir);
		tempScript("up.mt", "Button UP Tap", dir);
		tempScript("down.mt", "Button DOWN Tap", dir);
		tempScript("suite.mts", "Test foo.mt Run\n" + "Test bar.mt Run %ignore=setup\n"
				+ "Test baz.mt Run %ignore=teardown\n"
				+ "Test both.mt Run %ignore=setup,teardown\n"
				+ "Setup up.mt Run\nTeardown down.mt Run", dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		assertReportCount(xml, 4, 0, 0, 0);
		assertThat(xml, containsString("<testcase name=\"foo"));
		assertThat(xml, containsString("<testcase name=\"bar"));
		assertThat(xml, containsString("<testcase name=\"baz"));
		assertThat(xml, containsString("<testcase name=\"both"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(8));
		assertThat(server.getCommands().get(0).getCommand(), is("Button UP Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button DOWN Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button DOWN Tap"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button UP Tap"));
		assertThat(server.getCommands().get(6).getCommand(), is("Button BAZ Tap"));
		assertThat(server.getCommands().get(7).getCommand(), is("Button BOTH Tap"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		String line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"4\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test "), is(4));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"foo.mt\" action=\"Run\""), is(1));
		
		assertThat(countOccurences(report, "<setup "), is(2));
		assertThat(countOccurences(report, "<teardown "), is(2));
	}

	@Test
	public void testRunSuiteOfSuites() throws Exception {
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

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		String line=findLineMatching(xml, ".*<testsuite name=\\\"suite\\\".*");
		assertThat(line, notNullValue());
		assertReportCount(line, 5, 2, 0, 0, 0);
		line=findLineMatching(xml, ".*<testsuite name=\\\"s2\\\".*");
		assertThat(line, notNullValue());
		assertReportCount(line, 3, 1, 0, 0, 0);
		line=findLineMatching(xml, ".*<testsuite name=\\\"s3\\\".*");
		assertThat(line, notNullValue());
		assertReportCount(line, 1, 0, 0, 0, 0);
		assertThat(xml, containsString("<testsuite name=\"suite\""));
		assertThat(xml, containsString("<testsuite name=\"s2\""));
		assertThat(xml, containsString("<testsuite name=\"s3\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt\""));
		assertThat(xml, containsString("<testcase name=\"foo2.mt\""));
		assertThat(xml, containsString("<testcase name=\"bar.mt\""));
		assertThat(xml, containsString("<testcase name=\"bar2.mt\""));
		assertThat(xml, containsString("<testcase name=\"baz.mt\""));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(5));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button BAR2 Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button FOO2 Tap"));

		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		
		assertThat(countOccurences(report, "<suite "), is(3));
		line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"5\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		line=(findLineMatching(report, ".*<suite .*id=\\\"s2.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"3\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		line=(findLineMatching(report, ".*<suite .*id=\\\"s3.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"1\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"0\\\".*"));
		assertThat(line, notNullValue());
		
		assertThat(countOccurences(report, "<test "), is(5));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar2.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo2.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		
		assertThat(countOccurences(report, "<cmd "), is(5));
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAZ\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR2\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO2\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		
		assertThat(countOccurences(report, "<setup "), is(0));
		assertThat(countOccurences(report, "<teardown "), is(0));
	}

	@Test
	public void testRunSuiteOfSuitesComplex() throws Exception {
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

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		String xml = FileUtils.readFile(processor.getReportFile());
		String line=findLineMatching(xml, ".*<testsuite name=\\\"suite\\\".*");
		assertThat(line, notNullValue());
		assertReportCount(line, 8, 2, 0, 0, 1);
		line=findLineMatching(xml, ".*<testsuite name=\\\"s2\\\".*");
		assertThat(line, notNullValue());
		assertReportCount(line, 6, 1, 0, 0, 1);
		line=findLineMatching(xml, ".*<testsuite name=\\\"s3\\\".*");
		assertThat(line, notNullValue());
		assertReportCount(line, 2, 0, 0, 0, 1);

		// FIXME: also what to do for ignored suite? JUnit turns it into a TestCase with ignore=true
		assertThat(xml, containsString("<testsuite name=\"suite\""));
		assertThat(xml, containsString("<testsuite name=\"s2\""));
		assertThat(xml, containsString("<testsuite name=\"s3\""));
		assertThat(xml, not(containsString("<testsuite name=\"s4\"")));
		assertThat(xml, containsString("<testcase name=\"foo.mt\""));
		assertThat(xml, containsString("<testcase name=\"foo2.mt\""));
		assertThat(xml, containsString("<testcase name=\"bar.mt[name=&apos;Alice&apos;]\""));
		assertThat(xml, containsString("<testcase name=\"bar.mt[name=&apos;Bo Bo&apos;]\""));
		assertThat(xml, containsString("<testcase name=\"bar.mt[name=&apos;Charlie&apos;]\""));
		assertThat(xml, containsString("<testcase name=\"bar2.mt\""));
		assertThat(xml, containsString("<testcase name=\"baz.mt\""));
		assertThat(xml, containsString("<testcase name=\"baz2.mt\""));

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
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report," idx=\""), is(35));
		for (int i=0; i<countOccurences(report," idx=\""); i++) {
			assertThat(countOccurences(report," idx=\"" + (i+1) + "\""), is(1));
		}
		assertThat(countOccurences(report, "<detail "), is(1));
		
		assertThat(countOccurences(report, "<suite "), is(3));
		line=(findLineMatching(report, ".*<suite .*id=\\\"suite.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"8\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"1\\\".*"));
		assertThat(line, notNullValue());
		line=(findLineMatching(report, ".*<suite .*id=\\\"s2.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"6\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"1\\\".*"));
		assertThat(line, notNullValue());
		line=(findLineMatching(report, ".*<suite .*id=\\\"s3.mts\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*"
				+ "tests=\\\"2\\\" errors=\\\"0\\\" failures=\\\"0\\\" skipped=\\\"1\\\".*"));
		assertThat(line, notNullValue());
		
		
		assertThat(countOccurences(report, "<test "), is(9));
		// foo.mt
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		
		// bar.mt
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar.mt\\\".*action=\\\"RunWith\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args=\"data.csv\""), is(1));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"bar.mt\" action=\"RunWith\" args=\"data.csv[@1]\""), is(1));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"bar.mt\" action=\"RunWith\" args=\"data.csv[@2]\""), is(1));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"bar.mt\" action=\"RunWith\" args=\"data.csv[@3]\""), is(1));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"bar.mt\" action=\"RunWith\""), is(4));
		
		// baz.mt
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"baz.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"baz.mt\""), is(1));
		
		// bar2.mt
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"bar2.mt\\\".*action=\\\"Run\\\".*"
				+ " %ignore=teardown.*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"bar2.mt\" action=\"Run\""), is(1));

		// baz2
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"baz2.mt\\\".*action=\\\"Run\\\".*"
				+ " %ignore=true.*"
				+ "result=\\\"skipped\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"baz2.mt\" action=\"Run\""), is(1));

		// foo2
		line = findLineMatching(report, ".*<test.*comp=\\\"Test\\\".*id=\\\"foo2.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");
		assertThat(line, notNullValue());
		assertThat(countOccurences(line, "args="), is(0));
		assertThat(countOccurences(report, "<test  comp=\"Test\" id=\"foo2.mt\" action=\"Run\""), is(1));
		
		
		assertThat(countOccurences(report, "<cmd "), is(15));
		// FOO
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"FOO\" action=\"Tap\""), is(1));

		// BAR
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BARAlice\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BARBo Bo\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BARCharlie\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BARAlice\" action=\"Tap\""), is(1));
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BARBo Bo\" action=\"Tap\""), is(1));
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BARCharlie\" action=\"Tap\""), is(1));
		
		// BAZ
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAZ\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BAZ\" action=\"Tap\""), is(1));

		// BAR2
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"BAR2\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BAR2\" action=\"Tap\""), is(1));
		
		// BAZ2
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"BAZ2\" action=\"Tap\""), is(0));
		
		// FOO2
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"FOO2\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"FOO2\" action=\"Tap\""), is(1));
		
		// UP
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"UP\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"UP\" action=\"Tap\""), is(1));
		
		// DOWN
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"UP\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"UP\" action=\"Tap\""), is(1));
		
		// UP2 (TEST Bar2 Run %ignore=teardown) 
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"UP2\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"UP2\" action=\"Tap\""), is(1));
		
		// DOWN2 (TEST Bar2 RunWith data.csv) 
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"DOWN2\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(line.contains("args="), is(false));
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"DOWN2\" action=\"Tap\""), is(3));
		
		// UP3  
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"UP3\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"UP3\" action=\"Tap\""), is(1));
		
		// DOWN3  
		line = findLineMatching(report, ".*<cmd.*comp=\\\"Button\\\".*id=\\\"DOWN3\\\".*action=\\\"Tap\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<cmd  comp=\"Button\" id=\"DOWN3\" action=\"Tap\""), is(1));
		
		
		assertThat(countOccurences(report, "<setup "), is(3));
		// up.mt 
		line = findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"up.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"up.mt\" action=\"Run\""), is(1));
		
		// u2.mt (Test Bar2 Run %ignore=teardown)
		line = findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"u2.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"u2.mt\" action=\"Run\""), is(1));
		// u3.mt 
		line = findLineMatching(report, ".*<setup.*comp=\\\"Setup\\\".*id=\\\"u3.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"u3.mt\" action=\"Run\""), is(1));

		
		assertThat(countOccurences(report, "<teardown "), is(5));
		// down.mt 
		line = findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"down.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"down.mt\" action=\"Run\""), is(1));
		
		// d2 (RunWith 3x)
		line = findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"d2.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"d2.mt\" action=\"Run\""), is(3));
		
		// d3.mt 
		line = findLineMatching(report, ".*<teardown.*comp=\\\"Teardown\\\".*id=\\\"d3.mt\\\".*action=\\\"Run\\\".*"
				+ "result=\\\"ok\\\".*");		
		assertThat(line, notNullValue());
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"d3.mt\" action=\"Run\""), is(1));
		
		assertThat(countOccurences(report, "<msg>"), is(1));
		assertThat(findLineMatching(report, ".*<msg><[!]\\[CDATA\\[ignored\\]\\]>.*"), notNullValue());
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	@Test
	public void testMultiSetupTeardownSuite() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("foo2.mt", "Button FOO2 Tap", dir);
		tempScript("foo3.mt", "Button FOO3 Tap", dir);
		tempScript("foo4.mt", "Button FOO4 Tap", dir);
		tempScript("u1.mt", "Button UP Tap", dir);
		tempScript("u2.mt", "Button UP2 Tap", dir);
		tempScript("d1.mt", "Button DOWN Tap", dir);
		tempScript("d2.mt", "Button DOWN2 Tap", dir);
		tempScript(
				"suite.mts",
				  "Test foo.mt Run\n"
				+ "Test foo2.mt Run %ignore=setup\n"
				+ "Test foo3.mt Run %ignore=teardown\n"
				+ "Test foo4.mt Run %ignore=setup,teardown\n"
				+ "Setup u1.mt Run\n"
				+ "Setup u2.mt Run\n"
				+ "Teardown d1.mt Run\n"
				+ "Teardown d2.mt Run",
				dir);

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setReportDir(dir);

		server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("suite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands().get(0).getCommand(), is("Button UP Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button UP2 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button FOO Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button DOWN Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button DOWN2 Tap"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button FOO2 Tap"));
		assertThat(server.getCommands().get(6).getCommand(), is("Button DOWN Tap"));
		assertThat(server.getCommands().get(7).getCommand(), is("Button DOWN2 Tap"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button UP Tap"));
		assertThat(server.getCommands().get(9).getCommand(), is("Button UP2 Tap"));
		assertThat(server.getCommands().get(10).getCommand(), is("Button FOO3 Tap"));
		assertThat(server.getCommands().get(11).getCommand(), is("Button FOO4 Tap"));
		
		String report = new ScriptReportHelper().createDetailReport(result).toXMLDocument();
		assertThat(countOccurences(report, "<detail "), is(1));
		assertThat(countOccurences(report, "<suite "), is(1));
		assertThat(countOccurences(report, "<test "), is(4));
		assertThat(countOccurences(report, "<setup "), is(4));
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"u1.mt\" action=\"Run\""), is(2));
		assertThat(countOccurences(report, "<setup  comp=\"Setup\" id=\"u2.mt\" action=\"Run\""), is(2));
		assertThat(countOccurences(report, "<teardown "), is(4));
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"d1.mt\" action=\"Run\""), is(2));
		assertThat(countOccurences(report, "<teardown  comp=\"Teardown\" id=\"d2.mt\" action=\"Run\""), is(2));
		assertThat(countOccurences(report, "<script "), is(0));
		assertThat(countOccurences(report, "<cmd "), is(12));
		assertThat(countOccurences(report, "<msg>"), is(0));
		assertThat(countOccurences(report, "<warning>"), is(0));
	}

	private void assertReportCount(String xml, int tests, int errors, int failures, int skipped) {
		this.assertReportCount(xml, tests, 0, errors, failures, skipped);
	}

	private void assertReportCount(String xml, int tests, int suites, int errors, int failures,
			int skipped) {
		assertThat(xml, containsString("tests=\"" + tests + "\" suites=\"" + suites
				+ "\" errors=\"" + errors + "\" failures=\"" + failures + "\" skipped=\"" + skipped
				+ "\""));
	}
}