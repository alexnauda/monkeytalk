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
package com.gorillalogic.monkeytalk.processor.report.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.processor.report.Suite;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class ReportTest extends TestHelper {

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testTest() {
		com.gorillalogic.monkeytalk.processor.report.Test t = new com.gorillalogic.monkeytalk.processor.report.Test(
				"foo");
		assertThat(t, notNullValue());
		assertThat(t.toXML(""),
				is("<testcase name=\"foo\" starttime=\"0\" stoptime=\"0\" time=\"0.000\" />"));
		assertThat(t.toXML("\t"),
				is("\t<testcase name=\"foo\" starttime=\"0\" stoptime=\"0\" time=\"0.000\" />"));
		assertThat(t.toString(),
				is("\t<testcase name=\"foo\" starttime=\"0\" stoptime=\"0\" time=\"0.000\" />"));
	}

	@Test
	public void testSuite() {
		Suite s = new Suite("foo");
		assertThat(s, notNullValue());
		assertThat(
				s.toXML(""),
				is("<testsuite name=\"foo\" tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""
						+ " starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
		// assertThat(s.toXML(""), is(s.toXML("\t"))); I think this is a bogus assertion
	}

	@Test
	public void testSuiteWithNullName() {
		Suite s = new Suite(null);
		assertThat(s, notNullValue());
		assertThat(s.getName(), notNullValue());
		assertThat(s.getName(), is(""));
		assertThat(
				s.toXML(""),
				is("<testsuite tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" starttime=\"0\""
						+ " stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
	}

	@Test
	public void testSuiteWithBlankName() {
		Suite s = new Suite("");
		assertThat(s, notNullValue());
		assertThat(s.getName(), notNullValue());
		assertThat(s.getName(), is(""));
		assertThat(
				s.toXML(""),
				is("<testsuite name=\"\" tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
	}

	@Test
	public void testSuiteNameWithExtension() {
		Suite s = new Suite("mysuite.mts");
		assertThat(s, notNullValue());
		assertThat(
				s.toXML(""),
				is("<testsuite name=\"mysuite\" tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""
						+ " starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
		assertThat(s.toXML(""), is(s.toXML("\t")));
	}

	@Test
	public void testSuiteWithTest() {
		Suite s = new Suite("foo");
		com.gorillalogic.monkeytalk.processor.report.Test t = new com.gorillalogic.monkeytalk.processor.report.Test(
				"bar");

		s.addTest(t);

		assertThat(
				s.toXML("\t"),
				is("<testsuite name=\"foo\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n\t<testcase "
						+ "name=\"bar\" starttime=\"0\" stoptime=\"0\" time=\"0.000\" />\n</testsuite>"));
	}

	@Test
	public void testSuiteWithMultipleTests() {
		Suite s = new Suite("foo");
		com.gorillalogic.monkeytalk.processor.report.Test t = new com.gorillalogic.monkeytalk.processor.report.Test(
				"bar");
		com.gorillalogic.monkeytalk.processor.report.Test t2 = new com.gorillalogic.monkeytalk.processor.report.Test(
				"baz");

		s.addTest(t);
		s.addTest(t2);
		assertThat(
				s.toXML("\t"),
				is("<testsuite name=\"foo\" tests=\"2\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n\t<testcase "
						+ "name=\"bar\" starttime=\"0\" stoptime=\"0\" time=\"0.000\" />\n\t<testcase name=\"baz\""
						+ " starttime=\"0\" stoptime=\"0\" time=\"0.000\" />\n</testsuite>"));
	}

	@Test
	public void testSuiteWithMultipleSuites() {
		Suite s = new Suite("foo");
		Suite s1 = new Suite("boo");
		Suite s2 = new Suite("coo");
		s.addSuite(s1);
		s.addSuite(s2);
		com.gorillalogic.monkeytalk.processor.report.Test t = new com.gorillalogic.monkeytalk.processor.report.Test(
				"bar");
		com.gorillalogic.monkeytalk.processor.report.Test t2 = new com.gorillalogic.monkeytalk.processor.report.Test(
				"baz");

		s1.addTest(t);
		s1.addTest(t2);
		s2.addTest(t);
		s2.addTest(t2);
		assertThat(
				s.toXML("\t"),
				is("<testsuite name=\"foo\" tests=\"4\" suites=\"2\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n\t<testsuite "
						+ "name=\"boo\" tests=\"2\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" starttime=\"0\" "
						+ "stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n\t\t<testcase name=\"bar\" "
						+ "starttime=\"0\" stoptime=\"0\" time=\"0.000\" />\n\t\t<testcase name=\"baz\" starttime=\"0\" "
						+ "stoptime=\"0\" time=\"0.000\" />\n\t</testsuite>\n\t<testsuite name=\"coo\" tests=\"2\" suites=\"0\""
						+ " errors=\"0\" failures=\"0\" skipped=\"0\" starttime=\"0\" stoptime=\"0\" "
						+ "timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n\t\t<testcase name=\"bar\" starttime=\"0\" "
						+ "stoptime=\"0\" time=\"0.000\" />\n\t\t<testcase name=\"baz\" starttime=\"0\" stoptime=\"0\" "
						+ "time=\"0.000\" />\n\t</testsuite>\n</testsuite>"));
	}

	@Test
	public void testReportWithSuite() {
		Report r = new Report("suite");

		assertThat(
				r.toString(),
				is("<testsuite name=\"suite\" tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
	}

	@Test
	public void testReportWithNullSuite() {
		Report r = new Report((String) null);
		assertThat(
				r.toString(),
				is("<testsuite tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" starttime=\"0\""
						+ " stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
	}

	@Test
	public void testReportWithSuiteAndTest() {
		Report r = new Report("suite");
		r.startTest(new Command("Test foo.mt Run"));

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"2013-02-21T10:34:57\" time=\"FUZZ\">\n\t<testcase "
						+ "name=\"foo.mt\" starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testReportStartAndStop() throws InterruptedException {
		Report r = new Report("suite");
		r.startTest(new Command("Test foo.mt Run"));
		Thread.sleep(50);
		r.stopTest(new Command("Test foo.mt Run"), new PlaybackResult());

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"2013-02-21T10:34:57\" time=\"FUZZ\">\n\t<testcase "
						+ "name=\"foo.mt\" starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testReportWithNullTest() throws InterruptedException {
		Report r = new Report("suite");
		r.startTest(null);

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"2013-02-21T10:34:57\" time=\"FUZZ\">\n\t<testcase "
						+ "name=\"null\" starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testReportStopWithoutStart() throws InterruptedException {
		Report r = new Report("suite");
		r.stopTest(new Command("Test foo.mt Run"), new PlaybackResult());

		assertThat(
				r.toString(),
				is("<testsuite name=\"suite\" tests=\"0\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"0\" stoptime=\"0\" timestamp=\"1969-12-31T17:00:00\" time=\"0.000\">\n</testsuite>"));
	}

	@Test
	public void testReportGetName() {
		Report r = new Report("suite");
		r.startTest(new Command("Test foo.mt Run"));
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(), is("foo.mt"));

		assertThat(r.getMainSuite().getTest(null), nullValue());
		assertThat(r.getMainSuite().getTest("missing"), nullValue());
		assertThat(r.getMainSuite().getTest("foo.mt"), notNullValue());
	}

	@Test
	public void testReportGetNameWithArgs() {
		Report r = new Report("suite");
		r.startTest(new Command("Test foo.mt Run Joe \"Bo Bo\""));
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(), is("foo.mt[Joe 'Bo Bo']"));
	}

	@Test
	public void testReportGetNameWithNullCommand() {
		Report r = new Report("suite");
		r.startTest(null);
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(), nullValue());
	}

	@Test
	public void testReportGetNameWithData() {
		Report r = new Report("suite");
		Map<String, String> datum = new LinkedHashMap<String, String>();
		datum.put("first", "Joe");
		datum.put("last", "Bo Bo");
		r.startTest(new Command("Test foo.mt RunWith names.csv"), datum);
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(),
				is("foo.mt[first='Joe' last='Bo Bo']"));
	}

	@Test
	public void testReportGetNameWithNullCommandAndNullData() {
		Report r = new Report("suite");
		r.startTest(null, null);
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(), nullValue());
	}

	@Test
	public void testReportGetNameWithNullData() {
		Report r = new Report("suite");
		r.startTest(new Command("Test foo.mt RunWith names.csv"), null);
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(), is("foo.mt[null]"));
	}

	@Test
	public void testReportGetNameWithEmptyData() {
		Report r = new Report("suite");
		Map<String, String> datum = new LinkedHashMap<String, String>();
		r.startTest(new Command("Test foo.mt RunWith names.csv"), datum);
		assertThat(r.getMainSuite().getTests(), notNullValue());
		assertThat(r.getMainSuite().getTests().size(), is(1));
		assertThat(r.getMainSuite().getTests().get(0).getName(), is("foo.mt[empty]"));
	}

	@Test
	public void testSetTestResultOk() {
		Report r = new Report("suite");
		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		r.stopTest(cmd, new PlaybackResult());

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""
						+ " starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testSetTestResultError() {
		Report r = new Report("suite");
		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		r.stopTest(cmd, new PlaybackResult(PlaybackStatus.ERROR, "error msg"));

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"1\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\">\n\t\t<error message=\"error msg\" "
						+ "type=\"test.run\"><![CDATA[error msg\n  at Test foo.mt Run]]></error>\n\t</testcase>\n</testsuite>");

	}

	@Test
	public void testSetTestResultFailure() {
		Report r = new Report("suite");
		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		r.stopTest(cmd, new PlaybackResult(PlaybackStatus.FAILURE, "failure msg"));

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"1\" skipped=\"0\""
						+ " starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\">\n\t\t<failure message=\"failure msg\" "
						+ "type=\"test.run\"><![CDATA[failure msg\n  at Test foo.mt Run]]></failure>\n\t</testcase>\n</testsuite>");
	}

	@Test
	public void testSetTestResultWithNullCommand() {
		Report r = new Report("suite");
		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		r.stopTest(null, new PlaybackResult());

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""
						+ " starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testSetTestResultWithNullResult() {
		Report r = new Report("suite");
		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		r.stopTest(cmd, null);

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" "
						+ "skipped=\"0\" starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">"
						+ "\n\t<testcase name=\"foo.mt\" starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testSetTestResultWithNullCommandAndNullResult() {
		Report r = new Report("suite");
		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		r.stopTest(null, null);

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testCurrentTest() {
		Report r = new Report("suite");
		assertThat(r.getCurrentTest(), nullValue());

		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		assertThat(r.getCurrentTest().getName(), is("foo.mt"));
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test bar Run arg1 \"arg two\"");
		r.startTest(cmd);
		assertThat(r.getCurrentTest().getName(), is("bar[arg1 'arg two']"));
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test baz.js RunWith data.csv");
		Map<String, String> datum = new TreeMap<String, String>();
		datum.put("key1", "val1");
		datum.put("key2", "val two");
		r.startTest(cmd, datum);
		assertThat(r.getCurrentTest().getName(), is("baz.js[key1='val1' key2='val two']"));
		r.stopTest(cmd, new PlaybackResult());

		datum = new TreeMap<String, String>();
		datum.put("key1", "val3");
		datum.put("key2", "val four");
		r.startTest(cmd, datum);
		assertThat(r.getCurrentTest().getName(), is("baz.js[key1='val3' key2='val four']"));
		r.stopTest(cmd, new PlaybackResult());

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"suite\" tests=\"4\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n\t<testcase name=\"bar[arg1 &apos;arg two&apos;]\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n\t<testcase name=\"baz.js[key1=&apos;val1&apos; "
						+ "key2=&apos;val two&apos;]\" starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n\t<testcase name=\"baz.js"
						+ "[key1=&apos;val3&apos; key2=&apos;val four&apos;]\" starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testReportWithScope() throws InterruptedException {
		Report r = new Report("mysuite.mts");

		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		Thread.sleep(9);
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test bar.mt Run arg1 \"arg two\"");
		r.startTest(cmd);
		Thread.sleep(11);
		Scope parent = new Scope("parent.mt");
		parent.setCurrentCommand(cmd, 3);
		Scope scope = new Scope("bar.mt", parent);
		scope.setCurrentCommand(new Command("Script baz.mt Run"), 2);
		Scope child = new Scope("baz.mt", scope);
		child.setCurrentCommand(new Command("Button BAZ Tap"), 1);
		r.stopTest(cmd, new PlaybackResult(PlaybackStatus.FAILURE, "some fail msg", child));

		cmd = new Command("Test baz.js RunWith data.csv");
		Map<String, String> datum = new TreeMap<String, String>();
		datum.put("key1", "val1");
		datum.put("key2", "val two");
		r.startTest(cmd, datum);
		Thread.sleep(13);
		r.stopTest(cmd, new PlaybackResult());

		datum = new TreeMap<String, String>();
		datum.put("key1", "val3");
		datum.put("key2", "val four");
		r.startTest(cmd, datum);
		Thread.sleep(15);
		r.stopTest(cmd, new PlaybackResult());

		assertFuzzyTime(
				r.toString(),
				"<testsuite name=\"mysuite\" tests=\"4\" suites=\"0\" errors=\"0\" failures=\"1\" skipped=\"0\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" timestamp=\"FUZZ\"ime=\"FUZZ\">\n\t<testcase name=\"foo.mt\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n\t<testcase name=\"bar.mt[arg1 &apos;arg two&apos;]\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\">\n\t\t<failure message=\"some fail msg\" "
						+ "type=\"test.run\"><![CDATA[some fail msg\n  at Button BAZ Tap (baz.mt : cmd #1)\n  at Script baz.mt Run"
						+ " (bar.mt : cmd #2)\n  at Test bar.mt Run arg1 \"arg two\" (parent.mt : cmd #3)]]></failure>\n\t"
						+ "</testcase>\n\t<testcase name=\"baz.js[key1=&apos;val1&apos; key2=&apos;val two&apos;]\" starttime=\"FUZZ\" "
						+ "stoptime=\"FUZZ\" time=\"FUZZ\" />\n\t<testcase name=\"baz.js[key1=&apos;val3&apos; key2=&apos;val four&apos;]\" "
						+ "starttime=\"FUZZ\" stoptime=\"FUZZ\" time=\"FUZZ\" />\n</testsuite>");
	}

	@Test
	public void testHTMLReport() throws InterruptedException {
		Report r = new Report("mysuite.mts");

		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		Thread.sleep(11);
		r.stopTest(cmd, new PlaybackResult());

		String html = r.getMainSuite().toHTML();
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"1\" data-err=\"0\" data-fail=\"0\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("www.gorillalogic.com"));
	}

	@Test
	public void testHTMLReportWithMultiple() throws InterruptedException {
		Report r = new Report("mysuite.mts");

		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		Thread.sleep(10);
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test bar Run arg1 \"arg two\"");
		r.startTest(cmd);
		Thread.sleep(14);
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test baz.js RunWith data.csv");
		Map<String, String> datum = new TreeMap<String, String>();
		datum.put("key1", "val1");
		datum.put("key2", "val two");
		r.startTest(cmd, datum);
		Thread.sleep(18);
		r.stopTest(cmd, new PlaybackResult());

		datum = new TreeMap<String, String>();
		datum.put("key1", "val3");
		datum.put("key2", "val four");
		r.startTest(cmd, datum);
		Thread.sleep(22);
		r.stopTest(cmd, new PlaybackResult());

		String html = r.getMainSuite().toHTML();
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"4\" data-err=\"0\" data-fail=\"0\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("bar[arg1 'arg two']</a>"));
		assertThat(html, containsString("baz.js[key1='val1' key2='val two']</a>"));
		assertThat(html, containsString("baz.js[key1='val3' key2='val four']</a>"));
		assertThat(html, containsString("www.gorillalogic.com"));
	}

	@Test
	public void testHTMLReportWithSuitesOfSuites() throws InterruptedException {

		Suite s1 = new Suite("booSS");
		Suite s2 = new Suite("cooSS");
		Suite deepkneebends = new Suite("deepkneebends");
		com.gorillalogic.monkeytalk.processor.report.Test t = new com.gorillalogic.monkeytalk.processor.report.Test(
				"bar");
		com.gorillalogic.monkeytalk.processor.report.Test t2 = new com.gorillalogic.monkeytalk.processor.report.Test(
				"baz");
		com.gorillalogic.monkeytalk.processor.report.Test t3 = new com.gorillalogic.monkeytalk.processor.report.Test(
				"baz3");
		deepkneebends.addTest(t3);
		s1.addTest(t);
		s1.addSuite(deepkneebends);
		s1.addTest(t2);
		s2.addTest(t);
		s2.addTest(t2);
		Suite s = new Suite("foos");
		s.addSuite(s1);
		s.addSuite(s2);
		Report r = new Report(s);

		String html = r.getMainSuite().toHTML();
		assertThat(html, containsString("<!doctype html>"));
	}

	@Test
	public void testHTMLReportWithScreenshot() throws IOException, InterruptedException {
		Report r = new Report("mysuite.mts");

		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		Thread.sleep(16);
		r.stopTest(
				cmd,
				new PlaybackResult(
						PlaybackStatus.FAILURE,
						"some fail",
						null,
						"some warn",
						"iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAABdJREFUeNpi+s/AwPCfgYkRSDH+BwgwABcpAwRXSDQWAAAAAElFTkSuQmCC"));

		String html = r.getMainSuite().toHTML();
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"1\" data-err=\"0\" data-fail=\"1\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("<li id=\"message\">some fail\n  at Test foo.mt Run</li>"));
		assertThat(
				html,
				containsString("<li id=\"screenshot\">screenshot:<br /><img src=\"screenshots/screenshot_"));
		assertThat(html, containsString("www.gorillalogic.com"));
	}

	@Test
	public void testSaveReport() throws IOException, InterruptedException {
		Report r = new Report("mysuite.mts");

		Command cmd = new Command("Test foo.mt Run");
		r.startTest(cmd);
		Thread.sleep(10);
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test bar Run arg1 \"arg two\"");
		r.startTest(cmd);
		Thread.sleep(14);
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test baz.js RunWith data.csv");
		Map<String, String> datum = new TreeMap<String, String>();
		datum.put("key1", "val1");
		datum.put("key2", "val two");
		r.startTest(cmd, datum);
		Thread.sleep(18);
		r.stopTest(cmd, new PlaybackResult());

		datum = new TreeMap<String, String>();
		datum.put("key1", "val3");
		datum.put("key2", "val four");
		r.startTest(cmd, datum);
		Thread.sleep(22);
		r.stopTest(cmd, new PlaybackResult());

		cmd = new Command("Test scrn.mt Run");
		r.startTest(cmd);
		Thread.sleep(24);
		r.stopTest(
				cmd,
				new PlaybackResult(
						PlaybackStatus.FAILURE,
						"some fail",
						null,
						"some warn",
						"iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAABdJREFUeNpi+s/AwPCfgYkRSDH+BwgwABcpAwRXSDQWAAAAAElFTkSuQmCC"));

		String html = r.getMainSuite().toHTML();
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"5\" data-err=\"0\" data-fail=\"1\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("bar[arg1 'arg two']</a>"));
		assertThat(html, containsString("baz.js[key1='val1' key2='val two']</a>"));
		assertThat(html, containsString("baz.js[key1='val3' key2='val four']</a>"));
		assertThat(html, containsString("scrn.mt</a>"));
		assertThat(html, containsString("<li id=\"message\">some fail\n  at Test scrn.mt Run</li>"));
		assertThat(
				html,
				containsString("<li id=\"screenshot\">screenshot:<br /><img src=\"screenshots/screenshot_"));
		assertThat(html, containsString("www.gorillalogic.com"));

		File dir = tempDir();
		File reportDir = new File(dir, "myreport");
		r.saveReport(reportDir);

		File f = FileUtils.findFile("TEST-mysuite.xml", reportDir);
		assertThat(f, notNullValue());
		assertThat(f.getName(), is("TEST-mysuite.xml"));

		String xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("tests=\"5\" suites=\"0\" errors=\"0\" failures=\"1\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"scrn.mt"));

		f = FileUtils.findFile("TEST-mysuite.html", reportDir);
		assertThat(f, notNullValue());
		assertThat(f.getName(), is("TEST-mysuite.html"));

		html = FileUtils.readFile(f);
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"5\" data-err=\"0\" data-fail=\"1\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt</a>"));
		assertThat(html, containsString("bar[arg1 'arg two']</a>"));
		assertThat(html, containsString("baz.js[key1='val1' key2='val two']</a>"));
		assertThat(html, containsString("baz.js[key1='val3' key2='val four']</a>"));
		assertThat(html, containsString("scrn.mt</a>"));
		assertThat(html, containsString("<li id=\"message\">some fail\n  at Test scrn.mt Run</li>"));
		assertThat(
				html,
				containsString("<li id=\"screenshot\">screenshot:<br /><img src=\"screenshots/screenshot_"));
		assertThat(html, containsString("www.gorillalogic.com"));

		assertTrue(new File(reportDir, "screenshots").exists());
		assertTrue(new File(reportDir, "screenshots").isDirectory());

		String[] screenshots = new File(reportDir, "screenshots").list();
		assertThat(screenshots, notNullValue());
		assertThat(screenshots.length, is(1));
		assertThat(screenshots[0], is(r.getMainSuite().getTest("scrn.mt").getScreenshotFilename()));
	}

	@Test
	public void testSaveReportWithUTF8() throws IOException, InterruptedException {
		Report r = new Report("mysuite.mts");

		Command cmd = new Command("Test foo.mt Run Héìíô \u21D0\u21D1\u21DD\u21DC");
		r.startTest(cmd);
		Thread.sleep(10);
		r.stopTest(cmd, new PlaybackResult());

		String xml = r.getMainSuite().toXML();

		String html = r.getMainSuite().toHTML();
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"1\" data-err=\"0\" data-fail=\"0\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt[Héìíô \u21D0\u21D1\u21DD\u21DC]</a>"));
		assertThat(html, containsString("www.gorillalogic.com"));

		File dir = tempDir();
		File reportDir = new File(dir, "myreport");
		r.saveReport(reportDir);

		File f = FileUtils.findFile("TEST-mysuite.xml", reportDir);
		assertThat(f, notNullValue());
		assertThat(f.getName(), is("TEST-mysuite.xml"));

		xml = FileUtils.readFile(f);
		assertThat(
				xml,
				containsString("<testsuite name=\"mysuite\" tests=\"1\" suites=\"0\" errors=\"0\" failures=\"0\" skipped=\"0\""));
		assertThat(xml, containsString("<testcase name=\"foo.mt[H&#233;&#236;&#237;&#244; &#8656;&#8657;&#8669;&#8668;]\""));

		f = FileUtils.findFile("TEST-mysuite.html", reportDir);
		assertThat(f, notNullValue());
		assertThat(f.getName(), is("TEST-mysuite.html"));

		html = FileUtils.readFile(f);
		assertThat(html, containsString("<!doctype html>"));
		assertThat(html, containsString("<title>mysuite.mts</title>"));
		assertThat(html, containsString("<li id=\"suite-name\">mysuite.mts</li>"));
		assertThat(
				html,
				containsString("<li id=\"suite-tests\" class=\"suite-tests\" data-tests=\"1\" data-err=\"0\" data-fail=\"0\" data-skip=\"0\"></li>"));
		assertThat(html, containsString("foo.mt[Héìíô \u21D0\u21D1\u21DD\u21DC]</a>"));
		assertThat(html, containsString("www.gorillalogic.com"));
	}

	private static final String TIME_PATTERN = "time=\"\\d+\\.\\d+\"";
	private static final String TIME_REPLACE = "time=\"FUZZ\"";

	private static final String TIME_START_PATTERN = "starttime=\"\\d+\"";
	private static final String TIME_START_REPLACE = "starttime=\"FUZZ\"";

	private static final String TIME_STOP_PATTERN = "stoptime=\"\\d+\"";
	private static final String TIME_STOP_REPLACE = "stoptime=\"FUZZ\"";

	private static final String TIME_STAMP_PATTERN = "timestamp=.{23}";
	private static final String TIME_STAMP_REPLACE = "timestamp=\"FUZZ\"";

	private void assertFuzzyTime(String actual, String expected) {
		actual = actual.replaceAll(TIME_PATTERN, TIME_REPLACE);
		expected = expected.replaceAll(TIME_PATTERN, TIME_REPLACE);

		actual = actual.replaceAll(TIME_START_PATTERN, TIME_START_REPLACE);
		expected = expected.replaceAll(TIME_START_PATTERN, TIME_START_REPLACE);

		actual = actual.replaceAll(TIME_STOP_PATTERN, TIME_STOP_REPLACE);
		expected = expected.replaceAll(TIME_STOP_PATTERN, TIME_STOP_REPLACE);

		actual = actual.replaceAll(TIME_STAMP_PATTERN, TIME_STAMP_REPLACE);
		expected = expected.replaceAll(TIME_STAMP_PATTERN, TIME_STAMP_REPLACE);

		assertThat(actual, is(expected));
	}
}