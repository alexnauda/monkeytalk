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
package com.gorillalogic.monkeytalk.ant.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.ant.RunTask;
import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.utils.TestHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer;

public class GlobalsRunTaskTest extends BaseAntTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18171;
	private CommandServer server;
	private RunTask task;
	private Project proj;

	@Before
	public void before() throws IOException {
		task = new RunTask();
		task.setHost(HOST);
		task.setPort(PORT);

		proj = new Project();
		task.setProject(proj);

		server = new PlaybackCommandServer(new TestHelper(), PORT);
		Globals.clear();
	}

	@After
	public void after() {
		server.stop();
	}

	@Test
	public void testGlobalsAttribute() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		task.setScript(foo);
		task.setAgent("iOS");
		task.setGlobals("foo=123 bar=\"Bo Bo\"");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap \"Bo Bo\""));

		assertThat(Globals.getGlobals().size(), is(2));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("Bo Bo"));
	}

	@Test
	public void testGlobalsAttributeWithDeepNestedScript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button 1FOO${foo} Tap\nScript bar.mt Run\nButton 2FOO${foo} Tap", dir);
		tempScript(
				"bar.mt",
				"Button 1BAR${foo} Tap\nGlobals * Set foo=234\nScript baz.mt Run\nButton 2BAR${foo} Tap",
				dir);
		tempScript("baz.mt", "Button 1BAZ${foo} Tap\nGlobals * Set foo=345\nButton 2BAZ${foo} Tap",
				dir);

		task.setScript(foo);
		task.setAgent("iOS");
		task.setGlobals("foo=123");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 1FOO123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button 1BAR123 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button 1BAZ234 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button 2BAZ345 Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button 2BAR345 Tap"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button 2FOO345 Tap"));

		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("345"));
	}

	@Test
	public void testGlobalsAttributeWithBadVarName() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap", dir);

		task.setScript(foo);
		task.setAgent("iOS");
		task.setGlobals("1foo=123");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(), is("illegal global variable '1foo' -- "
					+ Globals.ILLEGAL_MSG));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testGlobalsWithJavascript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		task.setScript(new File(dir, "foo.js"));
		task.setAgent("iOS");
		task.setGlobals("foo=123 bar=\"Bo Bo\"");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 tap \"Bo Bo\""));
	}

	@Test
	public void testPropertiesFile() throws IOException {
		File dir = tempDir();
		tempScript("globals.properties", "foo=123\nbar=Bo Bo", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap\nButton ${bar} Tap", dir);

		task.setScript(foo);
		task.setAgent("iOS");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(Globals.getGlobals().size(), is(2));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("Bo Bo"));
	}

	@Test
	public void testPropertiesFileWithBarVarName() throws IOException {
		File dir = tempDir();
		tempScript("globals.properties", "1foo=123", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap", dir);

		task.setScript(foo);
		task.setAgent("iOS");

		try {
			task.execute();
		} catch (BuildException ex) {
			assertThat(ex.getMessage(),
					is("globals file 'globals.properties' has illegal global variable '1foo' -- "
							+ Globals.ILLEGAL_MSG));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testBothGlobalsAttributeAndPropertiesFile() throws IOException {
		File dir = tempDir();
		tempScript("globals.properties", "foo=123\nbar=Bo Bo", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap\nButton ${bar} Tap", dir);

		task.setScript(foo);
		task.setAgent("iOS");
		task.setGlobals("foo=234");

		task.execute();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		// because the globals attribute overrides the properties file
		assertThat(server.getCommands().get(0).getCommand(), is("Button 234 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(Globals.getGlobals().size(), is(2));
		assertThat(Globals.getGlobal("foo"), is("234"));
		assertThat(Globals.getGlobal("bar"), is("Bo Bo"));
	}
}