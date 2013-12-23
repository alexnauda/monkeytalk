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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class GetTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18025;

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testRunGetCommand() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button FOO Get foo");

		GetServer server = new GetServer(PORT);
		Response resp = processor.runCommand(cmd);
		server.stop();

		assertThat(resp.getStatus(), is(ResponseStatus.OK));
		assertThat(resp.getMessage(), is("FOO"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get foo"));
	}

	@Test
	public void testRunGetScript() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button FOO Get foo");

		GetServer server = new GetServer(PORT);
		PlaybackResult result = processor.runScript(cmd, new Scope());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("FOO"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get foo"));
	}

	@Test
	public void testRunMultiGetScript() throws IOException {
		File dir = tempDir();
		File foo = tempScript(
				"foo.mt",
				"Button FOO Get foo\nInput name EnterText ${foo}${bar}\nButton BAR Get bar\nInput name EnterText ${foo}${bar}",
				dir);
		Command cmd = new Command("Script foo.mt Run");

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		assertThat(processor.toString(), containsString(foo.getName()));

		GetServer server = new GetServer(PORT);
		PlaybackResult result = processor.runScript(cmd, new Scope());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get foo"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name EnterText FOO${bar}"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button BAR Get bar"));
		assertThat(server.getCommands().get(3).getCommand(), is("Input name EnterText FOOBAR"));
	}

	@Test
	public void testRunGetWithMissingVariable() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button FOO Get");

		GetServer server = new GetServer(PORT);
		PlaybackResult result = processor.runScript(cmd, new Scope());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Button FOO Get' must have a variable as its first arg"));

		assertThat(server.getCommands().size(), is(0));
	}

	@Test
	public void testRunGetWithBadVariable() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button FOO Get 123abc");

		GetServer server = new GetServer(PORT);
		PlaybackResult result = processor.runScript(cmd, new Scope());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'Button FOO Get 123abc' has illegal variable '123abc' as its first arg -- variables must begin with a letter and contain only letters, numbers, and underscores"));

		assertThat(server.getCommands().size(), is(0));
	}

	@Test
	public void testRunGetWithVariableNamedValue() throws IOException {
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, (File) null);
		Command cmd = new Command("Button FOO Get value");

		GetServer server = new GetServer(PORT);
		PlaybackResult result = processor.runScript(cmd, new Scope());
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("FOO"));
		assertThat(
				result.getWarning(),
				is("command 'Button FOO Get value' uses variable 'value' -- did you mean to use it as a property instead?"));

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO Get value"));
	}
}