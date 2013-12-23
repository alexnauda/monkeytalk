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
package com.gorillalogic.monkeytalk.api.js.tools.tests;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.js.tools.JSAPIGenerator;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class JSAPIGeneratorTest extends TestHelper {
	private ByteArrayOutputStream out;
	
	@Before
	public void before() {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}
	
	@After
	public void after() {
		out = null;
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testClassLoader() throws IOException {
		InputStream is = this.getClass().getClassLoader()
				.getResourceAsStream("templates/class.template.js");

		String js = readInputStream(is);

		assertThat(js, containsString("// CLASS: $CLASS_NAME$"));
	}

	@Test
	public void testEmptyDir() throws IOException {
		File dir = tempDir();
		String js = JSAPIGenerator.createAPI(dir);
		assertThat(js, containsString("// MonkeyTalkAPI.js"));
		assertThat(js, containsString("MT.Application.prototype.script = function(monkeyId)"));

		assertThat(js, not(containsString("$")));
	}

	@Test
	public void testDirWithMTObject() throws IOException {
		File dir = tempDir();
		tempScript("MTObject.java", "package com.gorillalogic.monkeytalk.api;\n\n"
				+ "/** MTObject class.\n* @ignoreJS */\n" + "public interface MTObject {\n"
				+ "public Application getApp();\n" + "public String getComponentType();\n"
				+ "public String getMonkeyId();\n" + "}", dir);
		String js = JSAPIGenerator.createAPI(dir);

		assertThat(js, containsString("// MonkeyTalkAPI.js"));
		assertThat(js, containsString("MT.Application.prototype.script = function(monkeyId)"));
		assertThat(js, not(containsString("$")));
	}

	@Test
	public void testDirWithView() throws IOException {
		File dir = tempDir();
		tempScript("MTObject.java", "package com.gorillalogic.monkeytalk.api;\n\n"
				+ "/** MTObject class.\n* @ignoreJS */\n" + "public interface MTObject {\n"
				+ "public Application getApp();\n" + "public String getComponentType();\n"
				+ "public String getMonkeyId();\n" + "}", dir);
		tempScript("View.java", "package com.gorillalogic.monkeytalk.api;\n\n"
				+ "/** View class. */\n" + "public interface View extends MTObject {\n"
				+ "/** Do tap. */\n" + "public void tap();\n" + "}\n", dir);
		String js = JSAPIGenerator.createAPI(dir);

		assertThat(js, containsString("// MonkeyTalkAPI.js"));
		assertThat(js, containsString("MT.Application.prototype.script = function(monkeyId)"));
		assertThat(js, containsString("MT.Application.prototype.view = function(monkeyId)"));
		assertThat(js, containsString("MT.View.prototype.tap = function()"));
		assertThat(js, not(containsString("$")));
	}

	@Test
	public void testDirWithViewAndButton() throws IOException {
		File dir = tempDir();
		tempScript("View.java", "package com.gorillalogic.monkeytalk.api;\n\n"
				+ "/** View class */\n" + "public interface View extends MTObject {\n"
				+ "/** Do tap. */\n" + "public void tap();\n" + "}\n", dir);
		tempScript("Button.java", "package com.gorillalogic.monkeytalk.api;\n\n"
				+ "/** Button class. */\n" + "public interface Button extends View { }\n", dir);
		String js = JSAPIGenerator.createAPI(dir);

		assertThat(js, containsString("// MonkeyTalkAPI.js"));
		assertThat(js, containsString("MT.Application.prototype.script = function(monkeyId)"));
		assertThat(js, containsString("MT.Application.prototype.view = function(monkeyId)"));
		assertThat(js, containsString("MT.View.prototype.tap = function()"));
		assertThat(js, containsString("MT.Application.prototype.button = function(monkeyId)"));
		assertThat(js, containsString("MT.Button.prototype = new MT.View;"));
		assertThat(js, not(containsString("$")));
	}

	private String readInputStream(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is, "UTF-8");
		final char[] buf = new char[1024];
		StringBuilder sb = new StringBuilder();
		int len;

		while ((len = reader.read(buf, 0, buf.length)) != -1) {
			sb.append(buf, 0, len);
		}

		is.close();

		return sb.toString();
	}
}