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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;

public class SysCommandTest extends BaseCommandHelper {

	@Test
	public void testExec() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nSystem * Exec ls\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap", "Button BAR Tap");

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("System * Exec ls -> OK :"));
		assertThat(output, containsString("bin\n"));
		assertThat(output, containsString("resources\n"));
		assertThat(output, containsString("pom.xml\n"));
		assertThat(output, containsString("Button BAR Tap -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testExecAndReturn() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Button FOO Tap\nSystem * ExecAndReturn v echo foobar\nButton ${v} Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap", "Button foobar Tap");

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("System * ExecAndReturn v echo foobar -> OK : foobar"));
		assertThat(output, containsString("Button foobar Tap -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testExecMissingCmd() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nSystem * Exec\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'System * Exec' must have a system command to execute as its first arg"));

		server.assertCommands("Button FOO Tap");

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("COMPLETE : ERROR : command 'System * Exec' must"));
	}

	@Test
	public void testExecAndReturnMissingVariable() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nSystem * ExecAndReturn\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'System * ExecAndReturn' must have a variable as its first arg"));

		server.assertCommands("Button FOO Tap");

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output,
				containsString("COMPLETE : ERROR : command 'System * ExecAndReturn' must"));
	}

	@Test
	public void testExecAndReturnMissingCmd() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nSystem * ExecAndReturn v\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'System * ExecAndReturn v' must have a system command to execute as its second arg"));

		server.assertCommands("Button FOO Tap");

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output,
				containsString("COMPLETE : ERROR : command 'System * ExecAndReturn v' must"));
	}

	@Test
	public void testExecWithError() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Button FOO Tap\nSystem * Exec sh -c \"echo foo; exit 123\"\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("foo\nerr 123"));

		server.assertCommands("Button FOO Tap");

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(
				output,
				containsString("System * Exec sh -c \"echo foo; exit 123\" -> ERROR : foo\nerr 123"));
		assertThat(output, containsString("COMPLETE : ERROR : foo\nerr 123"));
	}
}