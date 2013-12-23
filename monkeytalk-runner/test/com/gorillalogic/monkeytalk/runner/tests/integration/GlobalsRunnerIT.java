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
package com.gorillalogic.monkeytalk.runner.tests.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;

import org.junit.Test;

import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;

public class GlobalsRunnerIT extends BaseHelper {
	private static final int PORT = 18172;

	@Test
	public void testCommandlineGlobals() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		CommandServer server = new CommandServer(PORT);
		String run = run("-agent iOS -port " + PORT + " -Dfoo=123 -Dbar=BoBo -verbose "
				+ foo.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap BoBo"));

		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("Button 123 Tap BoBo -> OK\n"));
		assertThat(run, containsString("result: OK"));
	}

	@Test
	public void testCommandlineGlobalsInSuite() throws Exception {
		File dir = tempDir();
		tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		File suite = tempScript("mysuite.mts", "Test foo.mt Run", dir);

		CommandServer server = new CommandServer(PORT);
		String run = run("-agent iOS -port " + PORT + " -Dfoo=123 -Dbar=BoBo -verbose "
				+ suite.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap BoBo"));

		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("running suite : 1 test\n"));
		assertThat(run, containsString("1 : foo.mt\n"));
		assertThat(run, containsString("Button 123 Tap BoBo -> OK\n"));
		assertThat(run, containsString("test result: OK\n"));
		assertThat(run, containsString("result: OK\n"));
	}

	@Test
	public void testCommandlineGlobalsOverridePropertiesFile() throws Exception {
		File dir = tempDir();
		tempScript("globals.properties", "foo=123\nbar=\"Bo Bo\"", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		CommandServer server = new CommandServer(PORT);
		String run = run("-agent iOS -port " + PORT + " -Dfoo=234 -verbose "
				+ foo.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 234 Tap \"Bo Bo\""));

		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("Button 234 Tap \"Bo Bo\" -> OK\n"));
		assertThat(run, containsString("result: OK"));
	}

	@Test
	public void testCommandlineGlobalsWithJavascript() throws Exception {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		CommandServer server = new CommandServer(PORT);
		String run = run("-agent iOS -port " + PORT + " -Dfoo=123 -Dbar=BoBo -verbose "
				+ fooJS.getAbsolutePath());
		server.stop();

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 tap BoBo"));

		assertThat(run, containsString("www.gorillalogic.com"));
		assertThat(run, containsString("Button 123 tap BoBo -> OK\n"));
		assertThat(run, containsString("result: OK"));
	}
}