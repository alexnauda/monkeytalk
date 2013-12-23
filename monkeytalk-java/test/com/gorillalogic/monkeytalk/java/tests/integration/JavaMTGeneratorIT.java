/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.java.tests.integration;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class JavaMTGeneratorIT extends TestHelper {
	private static String allInOneJar;

	@BeforeClass
	public static void beforeClass() {
		allInOneJar = null;

		// search the bin folder for the correct jar
		File dir = new File("bin");
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith("all-in-one.jar")) {
				allInOneJar = f.getAbsolutePath();
				break;
			}
		}
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testRunWithNoArgs() throws Exception {
		String run = run("");
		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("Usage: java JavaMTGenerator"));
	}

	@Test
	public void testRunWithMissingScript() throws Exception {
		String run = run("missing.mt Missing.java");
		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("ERROR: script"));
	}

	@Test
	public void testRun() throws Exception {
		File dir = FileUtils.tempDir();
		File mt = new File(dir, "foo.mt");
		File out = new File(dir, "Foo.java");
		FileUtils.writeFile(mt, "Input username EnterText \"Bo Bo\"\n"
				+ "Input password EnterText pass\nButton LOGIN Tap");

		String run = run(mt.getAbsolutePath() + " " + out.getAbsolutePath());
		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("generate Foo.java from foo.mt"));

		String java = FileUtils.readFile(out);
		assertThat(java, not(containsString("import com.gorillalogic.monkeytalk.java.utils.Mods;")));
		assertThat(java, containsString("public class Foo {"));
		assertThat(java, containsString("public void testFoo() {"));
		assertThat(java, containsString("app.input(\"username\").enterText(\"Bo Bo\");"));
		assertThat(java, containsString("app.input(\"password\").enterText(\"pass\");"));
		assertThat(java, containsString("app.button(\"LOGIN\").tap();"));
	}

	private String run(String args) throws IOException, InterruptedException {
		String cmd = "java -jar " + allInOneJar + " " + args;

		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();

		String err = "";
		try {
			err = new Scanner(p.getErrorStream(), "UTF-8").useDelimiter("\\A").next();
		} catch (NoSuchElementException ex) {
			err = "";
		}

		String out = "";
		try {
			out = new Scanner(p.getInputStream(), "UTF-8").useDelimiter("\\A").next();
		} catch (NoSuchElementException ex) {
			out = "";
		}
		return out + (err.length() > 0 ? "\n" + err : "");
	}
}