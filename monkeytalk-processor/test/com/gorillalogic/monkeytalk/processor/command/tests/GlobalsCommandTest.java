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
package com.gorillalogic.monkeytalk.processor.command.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.SuiteProcessor;

public class GlobalsCommandTest extends BaseCommandHelper {

	@Test
	public void testDefine() throws IOException {
		assertThat(Globals.getGlobal("foo"), nullValue());

		File dir = tempDir();
		tempScript("foo.mt", "Globals * Define foo=123\nButton ${foo} Tap ${bar}", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), nullValue());

		server.assertCommands("Button 123 Tap ${bar}");
		assertThat(output, containsString("Globals * Define foo=123 -> OK\n"));
		assertThat(output, containsString("Button 123 Tap ${bar} -> OK\n"));
	}

	@Test
	public void testDefineWithBadArgument() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Define foobar", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'globals.define' has bad argument 'foobar' -- arguments must be in the form of name=value"));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.define' has bad argument 'foobar'"));
	}

	@Test
	public void testDefineWithIllegalName() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Define 1foo=123", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'globals.define' has illegal global variable '1foo' -- "
						+ Globals.ILLEGAL_MSG));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.define' has illegal global variable '1foo'"));
	}

	@Test
	public void testDefineWithMultipleVarsAndOneIllegalName() throws IOException {
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.getGlobal("foo"), nullValue());

		File dir = tempDir();
		tempScript("foo.mt", "Globals * Define foo=123 2bar=456", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'globals.define' has illegal global variable '2bar' -- "
						+ Globals.ILLEGAL_MSG));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.define' has illegal global variable '2bar'"));

		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("123"));
	}

	@Test
	public void testDefineWithReservedWord() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Define alert=123", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'globals.define' has illegal global variable 'alert' -- "
						+ Globals.RESERVED_MSG));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.define' has illegal global variable 'alert'"));
	}

	@Test
	public void testDefineWithMissingVar() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Define", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'globals.define' must define at least one global variable"));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.define' must define at least one global variable"));
	}

	@Test
	public void testSet() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Set foo=123\nButton ${foo} Tap\nButton ${bar} Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), nullValue());

		server.assertCommands("Button 123 Tap", "Button ${bar} Tap");
		assertThat(output, containsString("Globals * Set foo=123 -> OK\n"));
		assertThat(output, containsString("Button 123 Tap -> OK\n"));
		assertThat(output, containsString("Button ${bar} Tap -> OK\n"));
	}

	@Test
	public void testSetWithIllegalName() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Set 1foo=123", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				containsString("command 'globals.set' has illegal global variable '1foo'"));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.set' has illegal global variable '1foo'"));
	}

	@Test
	public void testSetWithReservedWord() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Set alert=123", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'globals.set' has illegal global variable 'alert' -- "
						+ Globals.RESERVED_MSG));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.set' has illegal global variable 'alert'"));
	}

	@Test
	public void testSetWithMissingVar() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Globals * Set", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'globals.set' must set at least one global variable"));

		server.assertCommands();
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'globals.set' must set at least one global variable"));
	}

	@Test
	public void testScriptSubstitution() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);

		Globals.setGlobal("foo", "123");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button 123 Tap ${bar}");
		assertThat(output, containsString("START\n"));
		assertThat(output, containsString("Button 123 Tap ${bar} -> OK\n"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testSuiteSubstitution() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		tempScript("mysuite.mts", "Test foo.mt Run ${foo}", dir);

		Globals.setGlobal("foo", "123");

		SuiteProcessor processor = new SuiteProcessor(HOST, PORT, dir);
		processor.setSuiteListener(SUITE_LISTENER);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button 123 Tap ${bar}");
		assertThat(output, containsString("RUN (1)\n"));
		assertThat(output, containsString("SUITE\nTEST foo.mt[123] (1 of 1)\nTEST_COMPLETE : OK\n"));
		assertThat(output, containsString("SUITE_COMPLETE : OK\n"));
		assertThat(output, containsString("RUN_COMPLETE : OK\n"));
	}
}