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
package com.gorillalogic.monkeytalk.ant.tests.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildFileTest;

import com.gorillalogic.monkeytalk.api.js.tools.JSAPIGenerator;
import com.gorillalogic.monkeytalk.api.js.tools.JSLibGenerator;
import com.gorillalogic.monkeytalk.api.js.tools.JSMTGenerator;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer;

public class RunAntJavascriptIT extends BuildFileTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18174;
	private CommandServer server;

	public RunAntJavascriptIT(String s) {
		super(s);
	}

	public void setUp() throws IOException {
		configureProject("resources/test2/build.xml");
		getProject().setProperty("host", HOST);
		getProject().setProperty("port", Integer.toString(PORT));
		assertThat(getProjectDir().getAbsolutePath(),
				containsString("/monkeytalk-ant/resources/test2"));

		// create the libs folder
		File libs = new File(getProjectDir(), "libs");
		if (libs.exists() && libs.isDirectory()) {
			FileUtils.deleteDir(libs);
		}
		libs.mkdir();

		// generate the api: MonkeyTalkAPI.js
		File apiJS = new File(libs, "MonkeyTalkAPI.js");
		FileUtils.writeFile(apiJS, "gen");
		JSAPIGenerator.main(new String[] { "../monkeytalk-api/src", apiJS.getAbsolutePath() });

		// generate the library: MyProj.js
		File libJS = new File(libs, "MyProj.js");
		FileUtils.writeFile(libJS, "gen");
		JSLibGenerator.main(new String[] { getProjectDir().getAbsolutePath(),
				libJS.getAbsolutePath() });

		// generate login.js
		File login = new File(getProjectDir(), "login.mt");
		JSMTGenerator.main(new String[] { "MyProj", login.getAbsolutePath() });

		server = new TestHelper().new CommandServer(PORT);
	}

	public void tearDown() {
		server.stop();
	}

	public void testLogin() {
		executeTarget("testLogin");
		assertThat(getLog(), containsString("-run: login.mt"));
		assertThat(getLog(), containsString("Vars * Define usr=Héìíô\u21D0\u21D1\u21DD\u21DC"));
		assertThat(getLog(), containsString(""));
		assertThat(getLog(), containsString("Input password EnterText password"));
		assertThat(getLog(), containsString("Button LOGIN Tap"));
		assertThat(getLog(), containsString("Button LOGOUT Verify %timeout=3000"));
		assertThat(getLog(),
				containsString("Label * Verify \"Welcome, Héìíô\u21D0\u21D1\u21DD\u21DC!\""));
		assertThat(getLog(), containsString("Button LOGOUT Tap %thinktime=1000"));
		assertThat(getLog(), containsString("-end: login.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input username EnterText Héìíô\u21D0\u21D1\u21DD\u21DC"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * Verify \"Welcome, Héìíô\u21D0\u21D1\u21DD\u21DC!\""));
		assertThat(server.getCommands().get(5).getCommand(),
				is("Button LOGOUT Tap %thinktime=1000"));
	}

	public void testLoginJS() {
		executeTarget("testLoginJS");
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input username enterText Héìíô\u21D0\u21D1\u21DD\u21DC"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input password enterText password"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * verify \"Welcome, Héìíô\u21D0\u21D1\u21DD\u21DC!\""));
		assertThat(server.getCommands().get(5).getCommand(),
				is("Button LOGOUT tap %thinktime=1000"));
	}

	public void testMyScriptMT() {
		executeTarget("testMyScriptMT");
		assertThat(getLog(), containsString("-run: myscriptmt.mt"));
		assertThat(getLog(), containsString("Input username EnterText Héìíô"));
		assertThat(getLog(), containsString("Input username EnterText \"Arrow \u21DD\u21DC\""));
		assertThat(getLog(), containsString("Input username EnterText \"Bo Bo\""));
		assertThat(getLog(), containsString("-end: myscriptmt.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(18));
		assertThat(server.getCommands().get(0).getCommand(), is("Input username EnterText Héìíô"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * Verify \"Welcome, Héìíô!\""));
		assertThat(server.getCommands().get(5).getCommand(),
				is("Button LOGOUT Tap %thinktime=1000"));
		assertThat(server.getCommands().get(6).getCommand(),
				is("Input username EnterText \"Arrow \u21DD\u21DC\""));
		assertThat(server.getCommands().get(7).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(9).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(10).getCommand(),
				is("Label * Verify \"Welcome, Arrow \u21DD\u21DC!\""));
		assertThat(server.getCommands().get(11).getCommand(),
				is("Button LOGOUT Tap %thinktime=1000"));
		assertThat(server.getCommands().get(12).getCommand(),
				is("Input username EnterText \"Bo Bo\""));
		assertThat(server.getCommands().get(13).getCommand(),
				is("Input password EnterText password"));
		assertThat(server.getCommands().get(14).getCommand(), is("Button LOGIN Tap"));
		assertThat(server.getCommands().get(15).getCommand(),
				is("Button LOGOUT Verify %timeout=3000"));
		assertThat(server.getCommands().get(16).getCommand(),
				is("Label * Verify \"Welcome, Bo Bo!\""));
		assertThat(server.getCommands().get(17).getCommand(),
				is("Button LOGOUT Tap %thinktime=1000"));
	}

	public void testMyScriptJS() {
		executeTarget("testMyScriptJS");
		assertThat(getLog(), containsString("-run: myscriptjs.mt"));
		assertThat(getLog(), containsString("Script login.js RunWith data.csv"));
		assertThat(getLog(), containsString("-end: myscriptjs.mt"));
		assertThat(getOutput(), containsString("result: OK"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(18));
		assertThat(server.getCommands().get(0).getCommand(), is("Input username enterText Héìíô"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input password enterText password"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button LOGIN tap"));
		assertThat(server.getCommands().get(3).getCommand(),
				is("Button LOGOUT verify %timeout=3000"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * verify \"Welcome, Héìíô!\""));
		assertThat(server.getCommands().get(5).getCommand(),
				is("Button LOGOUT tap %thinktime=1000"));
		assertThat(server.getCommands().get(6).getCommand(),
				is("Input username enterText \"Arrow \u21DD\u21DC\""));
		assertThat(server.getCommands().get(7).getCommand(),
				is("Input password enterText password"));
		assertThat(server.getCommands().get(8).getCommand(), is("Button LOGIN tap"));
		assertThat(server.getCommands().get(9).getCommand(),
				is("Button LOGOUT verify %timeout=3000"));
		assertThat(server.getCommands().get(10).getCommand(),
				is("Label * verify \"Welcome, Arrow \u21DD\u21DC!\""));
		assertThat(server.getCommands().get(11).getCommand(),
				is("Button LOGOUT tap %thinktime=1000"));
		assertThat(server.getCommands().get(12).getCommand(),
				is("Input username enterText \"Bo Bo\""));
		assertThat(server.getCommands().get(13).getCommand(),
				is("Input password enterText password"));
		assertThat(server.getCommands().get(14).getCommand(), is("Button LOGIN tap"));
		assertThat(server.getCommands().get(15).getCommand(),
				is("Button LOGOUT verify %timeout=3000"));
		assertThat(server.getCommands().get(16).getCommand(),
				is("Label * verify \"Welcome, Bo Bo!\""));
		assertThat(server.getCommands().get(17).getCommand(),
				is("Button LOGOUT tap %thinktime=1000"));
	}
}