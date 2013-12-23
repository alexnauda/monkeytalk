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
package com.gorillalogic.monkeytalk.shell.tests.integration;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.TestHelper;

public class ShellIT extends TestHelper {
	//private static final int PORT = 18171;
	private static String shellJar;
	
	private ByteArrayOutputStream out;

	@Before
	public void before() throws IOException {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}

	@After
	public void after() {
		out = null;
	}

	@BeforeClass
	public static void beforeClass() {
		shellJar = null;

		// search the bin folder for the correct jar
		File dir = new File("bin");
		for (File f : dir.listFiles()) {
			if (f.getName().startsWith("monkeytalk-shell-")
					&& !f.getName().endsWith("-sources.jar")
					&& !f.getName().endsWith("-javadoc.jar")) {
				shellJar = f.getAbsolutePath();
			}
		}
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testShellVersion() throws Exception {
		String out = run("-version");
		assertThat(out, containsString("www.gorillalogic.com"));
	}
	
	@Test
	public void testShellHelp() throws Exception {
		String out = run("-help");
		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("print help"));
	}

	private String run(String args) throws IOException, InterruptedException {
		String cmd = "java -jar " + shellJar + " " + args;

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