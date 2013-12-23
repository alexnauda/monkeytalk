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
package com.gorillalogic.monkeytalk.java.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;
import com.gorillalogic.monkeytalk.java.utils.Mods;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class ModsTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18317;
	private static String output;

	private static final PlaybackListener LISTENER_WITH_OUTPUT = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			output += scope.getCurrentCommand();
		}

		@Override
		public void onScriptStart(Scope scope) {
			output = "START\n";
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult r) {
			output += "COMPLETE : " + r;
		}

		@Override
		public void onComplete(Scope scope, Response resp) {
			output += " -> " + resp + "\n";
		}

		@Override
		public void onPrint(String message) {
			output += message;
		}
	};

	@Before
	public void before() throws IOException {
		output = "";
	}

	@After
	public void after() {
	}

	@Test
	public void testMods() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.button("FOO").tap(new Mods.Builder().timeout(1234).thinktime(5678).build());
		app.label("BAR").verify(Mods.of(Mods.SCREENSHOT_ON_ERROR, "false"));
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Button FOO tap %thinktime=5678 %timeout=1234"));
		assertThat(cmds.get(1).getCommand(), is("Label BAR verify %screenshotonerror=false"));

		assertThat(output, containsString("Button FOO tap %thinktime=5678 %timeout=1234 -> OK\n"));
		assertThat(output, containsString("Label BAR verify %screenshotonerror=false -> OK\n"));
	}

	@Test
	public void testIgnoreMod() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.button("FOO").tap();
		app.button("BAR").tap(new Mods.Builder().ignore(true).build());
		app.button("BAZ").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Button FOO tap"));
		assertThat(cmds.get(1).getCommand(), is("Button BAZ tap"));

		assertThat(output, containsString("Button FOO tap -> OK\n"));
		assertThat(output, containsString("Button BAR tap %ignore=true -> OK : ignored\n"));
		assertThat(output, containsString("Button BAZ tap -> OK\n"));
	}

	@Test
	public void testRawMods() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.raw("Button FOO Tap %thinktime=123 %timeout=456");
		app.raw("Button BAR Tap %ignore=true");
		app.raw("Label BAZ Verify %screenshotonerror=false");
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Button FOO Tap %thinktime=123 %timeout=456"));
		assertThat(cmds.get(1).getCommand(), is("Label BAZ Verify %screenshotonerror=false"));

		assertThat(output, containsString("Button FOO Tap %thinktime=123 %timeout=456 -> OK\n"));
		assertThat(output, containsString("Button BAR Tap %ignore=true -> OK : ignored\n"));
		assertThat(output, containsString("Label BAZ Verify %screenshotonerror=false -> OK\n"));
	}
}
