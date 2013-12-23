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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Runner;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class JavascriptTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18029;
	private ByteArrayOutputStream out;

	@Before
	public void before() {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		AgentManager.removeAllAgents();
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
	public void testRunJavascript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK"));
	}

	@Test
	public void testRunIllegalJavascript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("1foo.mt", "Button 1FOO Tap", dir);
		File fooJS = new File(dir, "1foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.1foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"1FOO\").tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("filename '1foo.js' is illegal -- filenames in JSProcessor must begin with a letter and contain only letters, numbers, and underscores"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("result: ERROR : filename '1foo.js' is illegal"));
	}

	@Test
	public void testRunJavascriptMultipleCommands() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\n" + "Input name EnterText \"Bo Bo\"\n"
				+ "Label * Verify \"Welcome, Bo Bo!\"", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Label * verify \"Welcome, Bo Bo!\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Input name enterText \"Bo Bo\" -> OK"));
		assertThat(log, containsString("Label * verify \"Welcome, Bo Bo!\" -> OK"));
	}

	@Test
	public void testRunJavascriptMultipleCommandsWithError() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nButton JOE Tap\nButton BAR Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));
		assertThat(js, containsString("app.button(\"JOE\").tap();"));
		assertThat(js, containsString("app.button(\"BAR\").tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), containsString("error on Joe"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Button JOE tap -> ERROR : error on Joe"));
	}

	@Test
	public void testRunJavascriptCommandsWithVars() throws IOException {
		File dir = tempDir();
		File bar = tempScript("bar.mt", "Vars * Define x\nButton BAR Tap\nButton ${x} Tap", dir);
		File script = tempScript("myscript.mt", "Script bar.js Run \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(bar);

		String js = FileUtils.readFile(new File(dir, "bar.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.bar.prototype.run = function(x)"));
		assertThat(js, containsString("x = (x != undefined && x != \"*\" ? x : \"<x>\");"));
		assertThat(js, containsString("app.button(\"BAR\").tap();"));
		assertThat(js, containsString("app.button(x).tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button BAR tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script bar.js Run \"Bo Bo\""));
		assertThat(log, containsString("Button BAR tap -> OK"));
		assertThat(log, containsString("Button \"Bo Bo\" tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunJavascriptCommandsWithVarsAndArgs() throws IOException {
		File dir = tempDir();
		File baz = tempScript("baz.mt",
				"Vars * Define name=Joe\nInput BAZ EnterText \"some ${name}\"", dir);
		File script = tempScript("myscript.mt", "Script baz.js Run \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(baz);

		String js = FileUtils.readFile(new File(dir, "baz.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.baz.prototype.run = function(_name)"));
		assertThat(js,
				containsString("_name = (_name != undefined && _name != \"*\" ? _name : \"Joe\");"));
		assertThat(js, containsString("app.input(\"BAZ\").enterText(\"some \" + _name);"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input BAZ enterText \"some Bo Bo\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script baz.js Run \"Bo Bo\""));
		assertThat(log, containsString("Input BAZ enterText \"some Bo Bo\" -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunJavascriptCommandsWithVarsAndDefaultArgs() throws IOException {
		File dir = tempDir();
		File baz = tempScript("baz.mt", "Vars * Define name=Joe\n"
				+ "Input BAZ EnterText \"some ${name}\"", dir);
		File script = tempScript("myscript.mt", "Script baz.js Run", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(baz);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Input BAZ enterText \"some Joe\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script baz.js Run"));
		assertThat(log, containsString("Input BAZ enterText \"some Joe\" -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunJavascriptCommandsWithVarsAndStarArgs() throws IOException {
		File dir = tempDir();
		File baz = tempScript("baz.mt", "Vars * Define name=Joe\n"
				+ "Input BAZ EnterText \"some ${name}\"", dir);
		File script = tempScript("myscript.mt", "Script baz.js Run *", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(baz);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Input BAZ enterText \"some Joe\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script baz.js Run"));
		assertThat(log, containsString("Input BAZ enterText \"some Joe\" -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunJavascriptCommandsWithDataDrive() throws IOException {
		File dir = tempDir();
		tempScript("data.csv", "x\nFOO\njoe\nBAR", dir);
		File foo = tempScript("foo.mt", "Vars * Define x\nButton ${x} Tap", dir);
		File script = tempScript("myscript.mt", "Script foo.js RunWith data.csv", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		ErrorOnJoeServer server = new ErrorOnJoeServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), containsString("error on Joe"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script foo.js RunWith data.csv"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Button joe tap -> ERROR : error on Joe"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunJavascriptCustomCommand() throws IOException {
		File dir = tempDir();
		File mycomp = tempScript("mycomp.myact.mt", "Button MY Tap", dir);
		File script = tempScript("myscript.mt", "MyComp * MyAct", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(mycomp);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button MY tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("MyComp * MyAct"));
		assertThat(log, containsString("Button MY tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunJavascriptWithMultipleCustomCommand() throws IOException {
		File dir = tempDir();
		File foo = tempScript("comp.foo.mt", "Button FOO Tap", dir);
		File bar = tempScript("comp.bar.mt", "Button BAR Tap", dir);
		File baz = tempScript("comp.baz.mt", "Button BAZ Tap", dir);
		File script = tempScript("myscript.mt", "Comp * Foo\nComp * Bar\nComp * Baz", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);
		JSHelper.genJS(bar);
		JSHelper.genJS(baz);

		String lib = FileUtils.readFile(new File(new File(dir, "libs"), "MyProj.js"));
		assertThat(lib, containsString("MyProj.comp.prototype.foo = function() {"));
		assertThat(lib, containsString("MT.CustomType.prototype.run.call(this, 'comp', 'foo');"));
		assertThat(lib, containsString("MyProj.comp.prototype.bar = function() {"));
		assertThat(lib, containsString("MT.CustomType.prototype.run.call(this, 'comp', 'bar');"));
		assertThat(lib, containsString("MyProj.comp.prototype.baz = function() {"));
		assertThat(lib, containsString("MT.CustomType.prototype.run.call(this, 'comp', 'baz');"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAZ tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Comp * Foo"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Comp * Bar"));
		assertThat(log, containsString("Button BAR tap -> OK"));
		assertThat(log, containsString("Comp * Baz"));
		assertThat(log, containsString("Button BAZ tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testRunIllegalComponentTypeInJavascriptCustomCommand() throws IOException {
		File dir = tempDir();
		File mycomp = tempScript("1mycomp.myact.mt", "Button MY Tap", dir);
		File script = tempScript("myscript.mt", "1MyComp * MyAct", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(mycomp);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("filename '1mycomp.myact.js' has illegal component type -- both parts of custom commands in JSProcessor must begin with a letter and contain only letters, numbers, and underscores"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("1MyComp * MyAct"));
		assertThat(
				log,
				containsString("result: ERROR : filename '1mycomp.myact.js' has illegal component type"));
	}

	@Test
	public void testRunIllegalActionInJavascriptCustomCommand() throws IOException {
		File dir = tempDir();
		File mycomp = tempScript("mycomp.1myact.mt", "Button MY Tap", dir);
		File script = tempScript("myscript.mt", "MyComp * 1MyAct", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(mycomp);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("filename 'mycomp.1myact.js' has illegal action -- both parts of custom commands in JSProcessor must begin with a letter and contain only letters, numbers, and underscores"));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("MyComp * 1MyAct"));
		assertThat(log,
				containsString("result: ERROR : filename 'mycomp.1myact.js' has illegal action"));
	}

	@Test
	public void testRunJavascriptCustomCommandWithArgs() throws IOException {
		File dir = tempDir();
		File mycomp = tempScript("mycomp.myact.mt", "Vars * Define x\nButton ${x} Tap", dir);
		File script = tempScript("myscript.mt", "MyComp * MyAct MYFOO\nMyComp * MyAct MYBAR", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(mycomp);

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button MYFOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button MYBAR tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("MyComp * MyAct MYFOO"));
		assertThat(log, containsString("Button MYFOO tap -> OK"));
		assertThat(log, containsString("MyComp * MyAct MYBAR"));
		assertThat(log, containsString("Button MYBAR tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptCustomCommandFromJS() throws IOException {
		File dir = tempDir();
		File mycomp = tempScript("mycomp.myact.mt", "Button MY Tap", dir);
		File foo = tempScript("foo.mt", "Button FOO Tap\nmycomp * myact\nButton BAR Tap", dir);
		File script = tempScript("myscript.mt", "Script foo.js Run", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(mycomp);
		JSHelper.genJS(foo);

		String lib = FileUtils.readFile(new File(new File(dir, "libs"), "MyProj.js"));
		assertThat(lib, containsString("MyProj.mycomp.prototype.myact = function() {"));
		assertThat(lib,
				containsString("MT.CustomType.prototype.run.call(this, 'mycomp', 'myact');"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button MY tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAR tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("mycomp * myact"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Button MY tap -> OK"));
		assertThat(log, containsString("Button BAR tap -> OK"));
		assertThat(log, containsString("\n -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithGet() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo\nInput name EnterText ${foo}", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("var foo = app.button(\"FOO\").get(\"foo\");"));
		assertThat(js, containsString("app.input(\"name\").enterText(foo);"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		GetServer server = new GetServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO get foo"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText FOO"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO get foo -> OK : FOO"));
		assertThat(log, containsString("Input name enterText FOO -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testMTCallsJSCallsMT() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define x\n" + "Input FOO EnterText \"some ${x}\"\n"
				+ "Script bar.mt Run FOO${x}", dir);
		tempScript("bar.mt", "Vars * Define y\n" + "Input BAR EnterText \"other ${y}\"", dir);
		File script = tempScript("myscript.mt", "Script foo.js Run \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(new File(dir, "foo.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function(x)"));
		assertThat(js, containsString("x = (x != undefined && x != \"*\" ? x : \"<x>\");"));
		assertThat(js, containsString("app.input(\"FOO\").enterText(\"some \" + x);"));
		assertThat(js, containsString("app.bar().run(\"FOO\" + x);"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input FOO enterText \"some Bo Bo\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input BAR EnterText \"other FOOBo Bo\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script foo.js Run"));
		assertThat(log, containsString("Input FOO enterText \"some Bo Bo\" -> OK"));
		assertThat(log, containsString("Script bar.mt Run \"FOOBo Bo\""));
		assertThat(log, containsString("Input BAR EnterText \"other FOOBo Bo\" -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testMTCallsJSCallsJS() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define x\n" + "Input FOO EnterText \"some ${x}\"\n"
				+ "Script bar.js Run FOO${x}", dir);
		File bar = tempScript("bar.mt", "Vars * Define y\n" + "Input BAR EnterText \"other ${y}\"",
				dir);
		File script = tempScript("myscript.mt", "Script foo.js Run \"Bo Bo\"", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);
		JSHelper.genJS(bar);

		String js = FileUtils.readFile(new File(dir, "foo.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function(x)"));
		assertThat(js, containsString("x = (x != undefined && x != \"*\" ? x : \"<x>\");"));
		assertThat(js, containsString("app.input(\"FOO\").enterText(\"some \" + x);"));
		assertThat(js, containsString("app.script(\"bar.js\").run(\"FOO\" + x);"));

		js = FileUtils.readFile(new File(dir, "bar.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.bar.prototype.run = function(y)"));
		assertThat(js, containsString("y = (y != undefined && y != \"*\" ? y : \"<y>\");"));
		assertThat(js, containsString("app.input(\"BAR\").enterText(\"other \" + y);"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input FOO enterText \"some Bo Bo\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input BAR enterText \"other FOOBo Bo\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script foo.js Run"));
		assertThat(log, containsString("Input FOO enterText \"some Bo Bo\" -> OK"));
		assertThat(log, containsString("Script bar.js Run \"FOOBo Bo\""));
		assertThat(log, containsString("Input BAR enterText \"other FOOBo Bo\" -> OK"));
		assertThat(log, containsString("\n -> OK"));
	}

	@Test
	public void testMTCallsJSDataDrivesMT() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Script bar.mt RunWith data.csv", dir);
		tempScript("bar.mt", "Vars * Define x y\n" + "Input BAR EnterText \"${x} ${y}\"", dir);
		tempScript("data.csv", "x,y\n1234,5678\nJohn,\"Bo Bo\"\n-4.135,-104.55513", dir);
		File script = tempScript("myscript.mt", "Script foo.js Run", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(new File(dir, "foo.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.bar().runWith(\"data.csv\");"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input BAR EnterText \"1234 5678\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input BAR EnterText \"John Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Input BAR EnterText \"-4.135 -104.55513\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script foo.js Run"));
		assertThat(log, containsString("Script bar.mt RunWith data.csv"));
		assertThat(log, containsString("Input BAR EnterText \"1234 5678\" -> OK"));
		assertThat(log, containsString("Input BAR EnterText \"John Bo Bo\" -> OK"));
		assertThat(log, containsString("Input BAR EnterText \"-4.135 -104.55513\" -> OK"));
	}

	@Test
	public void testMTCallsJSDataDrivesJS() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Script bar.js RunWith data.csv", dir);
		File bar = tempScript("bar.mt",
				"Vars * Define x y\n" + "Input BAR EnterText \"${x} ${y}\"", dir);
		tempScript("data.csv", "x,y\n1234,5678\nJohn,\"Bo Bo\"\n-4.135,-104.55513", dir);
		File script = tempScript("myscript.mt", "Script foo.js Run", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);
		JSHelper.genJS(bar);

		String js = FileUtils.readFile(new File(dir, "foo.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.script(\"bar.js\").runWith(\"data.csv\");"));

		js = FileUtils.readFile(new File(dir, "bar.js"));
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.bar.prototype.run = function(x, y)"));
		assertThat(js, containsString("x = (x != undefined && x != \"*\" ? x : \"<x>\");"));
		assertThat(js, containsString("y = (y != undefined && y != \"*\" ? y : \"<y>\");"));
		assertThat(js, containsString("app.input(\"BAR\").enterText(x + \" \" + y);"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(script, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input BAR enterText \"1234 5678\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input BAR enterText \"John Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Input BAR enterText \"-4.135 -104.55513\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Script foo.js Run"));
		assertThat(log, containsString("Script bar.js RunWith data.csv"));
		assertThat(log, containsString("Input BAR enterText \"1234 5678\" -> OK"));
		assertThat(log, containsString("Input BAR enterText \"John Bo Bo\" -> OK"));
		assertThat(log, containsString("Input BAR enterText \"-4.135 -104.55513\" -> OK"));
	}

	@Test
	public void testRunJavascriptWithIgnore() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button FOO Tap\nButton BAR Tap %ignore=true\nButton BAZ Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));
		assertThat(js, containsString("app.button(\"BAR\").tap({ignore:\"true\"});"));
		assertThat(js, containsString("app.button(\"BAZ\").tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAZ tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Button BAR tap %ignore=true -> OK : ignored"));
		assertThat(log, containsString("Button BAZ tap -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithTimeoutAndThinktime() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\n" + "Button BAR Tap %timeout=2345\n"
				+ "Button BAZ Tap %thinktime=888", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");\n"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));
		assertThat(js, containsString("app.button(\"BAR\").tap({timeout:\"2345\"});\n"));
		assertThat(js, containsString("app.button(\"BAZ\").tap({thinktime:\"888\"});\n"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);
		runner.setGlobalTimeout(1234);
		runner.setGlobalThinktime(567);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Button FOO tap %thinktime=567 %timeout=1234"));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Button BAR tap %thinktime=567 %timeout=2345"));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Button BAZ tap %thinktime=888 %timeout=1234"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK"));
		assertThat(log, containsString("Button BAR tap %timeout=2345 -> OK"));
		assertThat(log, containsString("Button BAZ tap %thinktime=888 -> OK"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithDebugPrint() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button FOO Tap\nDebug * Print foo bar baz\nButton BAR Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));
		assertThat(js, containsString("app.debug().print(\"foo\", \"bar\", \"baz\");"));
		assertThat(js, containsString("app.button(\"BAR\").tap();"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button BAR tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK\n"));
		assertThat(log, not(containsString("Debug")));
		assertThat(log, containsString("\nfoo bar baz\n"));
		assertThat(log, containsString("Button BAR tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithDebugVars() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo=123 bar=\"Bo Bo\"\nDebug * Vars\nButton FOO Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function(foo, bar) {"));
		assertThat(js,
				containsString("foo = (foo != undefined && foo != \"*\" ? foo : \"123\");\n"));
		assertThat(js,
				containsString("bar = (bar != undefined && bar != \"*\" ? bar : \"Bo Bo\");\n"));
		assertThat(
				js,
				containsString("app.debug().print(\"foo=\" + foo + \"\\n\" + \"bar=\" + bar + \"\\n\");\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, not(containsString("Debug")));
		assertThat(log, containsString("\nfoo=123\nbar=Bo Bo\n"));
		assertThat(log, containsString("Button FOO tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithDebugVarsAndMultipleVarDefines() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo=123 bar=\"Bo Bo\"\nDebug * Vars\nButton FOO Tap\n"
						+ "Vars * Define baz=abc\nDebug * Vars", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function(foo, bar, baz) {"));
		assertThat(js,
				containsString("foo = (foo != undefined && foo != \"*\" ? foo : \"123\");\n"));
		assertThat(js,
				containsString("bar = (bar != undefined && bar != \"*\" ? bar : \"Bo Bo\");\n"));
		assertThat(
				js,
				containsString("app.debug().print(\"foo=\" + foo + \"\\n\" + \"bar=\" + bar + \"\\n\");\n"));
		assertThat(js,
				containsString("baz = (baz != undefined && baz != \"*\" ? baz : \"abc\");\n"));
		assertThat(
				js,
				containsString("app.debug().print(\"foo=\" + foo + \"\\n\" + \"bar=\" + bar + \"\\n\" + \"baz=\" + baz + \"\\n\");\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, not(containsString("Debug")));
		assertThat(log, containsString("\nfoo=123\nbar=Bo Bo\n"));
		assertThat(log, containsString("Button FOO tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithVarsVerify() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define f=123\nVars * Verify 123 f\nButton FOO Tap",
				dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function(f) {"));
		assertThat(js, containsString("var app = this.app;\n"));
		assertThat(js, containsString("f = (f != undefined && f != \"*\" ? f : \"123\");\n"));
		assertThat(js, containsString("// NOT SUPPORTED YET: app.vars().verify(\"123\", f);\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button FOO tap -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithGlobals() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {"));
		assertThat(js, containsString("var app = this.app;\n"));
		assertThat(js, containsString("app.button(foo).tap(bar);\n"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		Map<String, String> globals = new LinkedHashMap<String, String>();
		globals.put("foo", "123");
		globals.put("bar", "Bo Bo");

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, globals);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 tap \"Bo Bo\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button 123 tap \"Bo Bo\" -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}

	@Test
	public void testRunJavascriptWithGlobalsSet() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Globals * Set foo=123 bar=\"Bo Bo\"\nButton ${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {"));
		assertThat(js, containsString("var app = this.app;\n"));
		assertThat(js, containsString("app.globals().set('foo=\"123\"');"));
		assertThat(js, containsString("foo = '123';"));
		assertThat(js, containsString("app.globals().set('bar=\"Bo Bo\"');"));
		assertThat(js, containsString("bar = 'Bo Bo';"));
		assertThat(js, containsString("app.button(foo).tap(bar);"));

		Runner runner = new Runner("iOS", HOST, PORT);
		runner.setVerbose(true);

		File logger = new File(dir, "log.txt");
		System.setOut(new PrintStream(new FileOutputStream(logger)));

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = runner.run(fooJS, null);
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 tap \"Bo Bo\""));

		String log = FileUtils.readFile(logger);
		assertThat(log, containsString("www.gorillalogic.com"));
		assertThat(log, containsString("Button 123 tap \"Bo Bo\" -> OK\n"));
		assertThat(log, containsString("result: OK"));
	}
}