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
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.Docs;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class DocsTest extends TestHelper {

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testNullScript() {
		Docs docs = new Docs((File) null);
		assertThat(docs.getDocs(null), nullValue());
	}

	@Test
	public void testEmptyScriptFilename() {
		Docs docs = new Docs((File) null);
		assertThat(docs.getDocs(""), nullValue());
	}

	@Test
	public void testScriptWithNoDocs() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), nullValue());
	}

	@Test
	public void testVarsDefine() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "Vars * Define foo=123 bar=654", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(2));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("foo", "bar"));
		assertThat(docs.getDocs("script.mt").get("foo"), is("The default value is '123'."));
		assertThat(docs.getDocs("script.mt").get("bar"), is("The default value is '654'."));
	}

	@Test
	public void testEmptyVarsDefine() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "Vars * Define foo= bar=", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(2));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("foo", "bar"));
		assertThat(docs.getDocs("script.mt").get("foo"), is("The default value is 'null'."));
		assertThat(docs.getDocs("script.mt").get("bar"), is("The default value is 'null'."));
	}

	@Test
	public void testDocVars() throws IOException {
		File dir = tempDir();
		tempScript(
				"script.mt",
				"Vars * Define foo=123 bar=654\nDoc * Vars foo=\"some desc\" bar=\"Other description.\"",
				dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(2));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("foo", "bar"));
		assertThat(docs.getDocs("script.mt").get("foo"),
				is("some desc. The default value is '123'."));
		assertThat(docs.getDocs("script.mt").get("bar"),
				is("Other description. The default value is '654'."));
	}

	@Test
	public void testEmptyDocVars() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "Vars * Define foo=123 bar=654\nDoc * Vars foo= bar=", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(2));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("foo", "bar"));
		assertThat(docs.getDocs("script.mt").get("foo"), is("The default value is '123'."));
		assertThat(docs.getDocs("script.mt").get("bar"), is("The default value is '654'."));
	}

	@Test
	public void testEmptyVarsDefineWithValidDocVars() throws IOException {
		File dir = tempDir();
		tempScript("script.mt",
				"Vars * Define foo= bar=\nDoc * Vars foo=\"some desc\" bar=\"Other description.\"",
				dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(2));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("foo", "bar"));
		assertThat(docs.getDocs("script.mt").get("foo"),
				is("some desc. The default value is 'null'."));
		assertThat(docs.getDocs("script.mt").get("bar"),
				is("Other description. The default value is 'null'."));
	}

	@Test
	public void testEmptyVarsDefineAndEmptyDocVars() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "Vars * Define foo= bar=\nDoc * Vars foo= bar=", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(2));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("foo", "bar"));
		assertThat(docs.getDocs("script.mt").get("foo"), is("The default value is 'null'."));
		assertThat(docs.getDocs("script.mt").get("bar"), is("The default value is 'null'."));
	}

	@Test
	public void testDocScript() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "Doc * Script \"some script doc\"", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), notNullValue());
		assertThat(docs.getDocs("script.mt").size(), is(1));
		assertThat(docs.getDocs("script.mt").keySet(), hasItems("script.mt"));
		assertThat(docs.getDocs("script.mt").get("script.mt"), is("some script doc"));
	}

	@Test
	public void testDocScriptWithNoValue() throws IOException {
		File dir = tempDir();
		tempScript("script.mt", "Doc * Script", dir);

		Docs docs = new Docs(dir);

		assertThat(docs.getDocs("script.mt"), nullValue());
	}
}