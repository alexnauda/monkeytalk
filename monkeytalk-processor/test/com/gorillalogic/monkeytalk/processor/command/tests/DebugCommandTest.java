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

public class DebugCommandTest extends BaseCommandHelper {

	@Test
	public void testRunScriptWithDebugPrint() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nDebug * Print foo bar baz\nButton BAR Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap", "Button BAR Tap");
		
		assertThat(output, containsString("START"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("Debug * Print foo bar baz -> OK"));
		assertThat(output, containsString("\nfoo bar baz\n"));
		assertThat(output, containsString("Button BAR Tap -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testRunScriptWithDebugVars() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123 bar=\"Bo Bo\"\n" + "Debug * Vars\n"
				+ "Button FOO Tap", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO Tap");
		
		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 bar=\"Bo Bo\" -> OK"));
		assertThat(output, containsString("Debug * Vars -> OK"));
		assertThat(output, containsString("\nfoo=123\n"));
		assertThat(output, containsString("\nbar=Bo Bo\n"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}
}