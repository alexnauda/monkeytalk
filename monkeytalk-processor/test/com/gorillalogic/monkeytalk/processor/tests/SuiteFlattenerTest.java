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

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.SuiteFlattener;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class SuiteFlattenerTest extends TestHelper {

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testDefaultConstructor() {
		SuiteFlattener flattener = new SuiteFlattener((File) null);
		assertThat(flattener, notNullValue());
		assertThat(flattener.toString(), containsString("SuiteFlattener:"));
	}

	@Test
	public void testSingleTest() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt Run", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(1));
		assertThat(flattener.flatten(null), is(SuiteFlattener.BAD_FILENAME));
		assertThat(flattener.flatten("missing.mts"), is(SuiteFlattener.SUITE_NOT_FOUND));
		assertThat(flattener.flatten("script.mt"), is(SuiteFlattener.BAD_SUITE));
	}

	@Test
	public void testMultipleTests() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt Run\nTest bar.mt Run\nTest baz.mt Run", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(3));
	}

	@Test
	public void testZeroTests() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(0));
	}

	@Test
	public void testDataDrivenTest() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt RunWith data.csv", dir);
		tempScript("data.csv", "header\nrow1\nrow2\nrow3", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(3));
	}

	@Test
	public void testDataDrivenTestWithMissingDatafile() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt RunWith", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(1));
	}

	@Test
	public void testDataDrivenTestWithDatafileNotFound() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt RunWith missing.csv", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(1));
	}

	@Test
	public void testDataDrivenTestWithEmptyDatafile() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt RunWith data.csv", dir);
		tempScript("data.csv", "header", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(1));
	}

	@Test
	public void testMultipleDataDrivenTests() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt Run arg1 arg2\n" + "Test foo.mt Run arg3 arg4\n"
				+ "Test bar.mt RunWith data1.csv\n" + "Test bar.mt RunWith data2.csv\n"
				+ "Test bar.mt RunWith data3.csv\n" + "Test baz.mt RunWith\n"
				+ "Test baz.mt RunWith missing.csv\n" + "Test baz.mt RunWith empty.csv\n"
				+ "Setup up.mt Run\n" + "Teardown down.mt Run", dir);
		tempScript("empty.csv", "", dir);
		tempScript("data1.csv", "header\nrow1", dir);
		tempScript("data2.csv", "header\nrow1\nrow2", dir);
		tempScript("data3.csv", "header\nrow1\nrow2\nrow3", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(11));
	}

	@Test
	public void testSuitesOfSuitesSimple() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt Run\nSuite s2.mts Run", dir);
		tempScript("s2.mts", "Test bar.mt Run\nSuite s3.mts Run", dir);
		tempScript("s3.mts", "Test baz.mt Run", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(3));
		assertThat(flattener.flatten("s2.mts"), is(2));
		assertThat(flattener.flatten("s3.mts"), is(1));
	}
	
	@Test
	public void testSuitesOfSuitesComplex() throws IOException {
		File dir = tempDir();
		tempScript("suite.mts", "Test foo.mt Run\nSuite s2.mts Run\nTest foo2.mt Run", dir);
		tempScript("s2.mts", "Test bar.mt Run\nSuite s3.mts Run\nTest bar2.mt Run", dir);
		tempScript("s3.mts", "Test baz.mt Run", dir);
		
		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(5));
		assertThat(flattener.flatten("s2.mts"), is(3));
		assertThat(flattener.flatten("s3.mts"), is(1));
	}

	@Test
	public void testSuitesOfSuitesWithDataDrive() throws IOException {
		File dir = tempDir();
		tempScript("data.csv", "header\nrow1\nrow2\nrow3", dir);
		tempScript("suite.mts", "Test foo.mt RunWith data.csv\nSuite s2.mts Run", dir);
		tempScript("s2.mts", "Test bar.mt RunWith data.csv\nSuite s3.mts Run", dir);
		tempScript("s3.mts", "Test baz.mt RunWith data.csv", dir);

		SuiteFlattener flattener = new SuiteFlattener(dir);
		assertThat(flattener.flatten("suite.mts"), is(9));
		assertThat(flattener.flatten("s2.mts"), is(6));
		assertThat(flattener.flatten("s3.mts"), is(3));
	}
}