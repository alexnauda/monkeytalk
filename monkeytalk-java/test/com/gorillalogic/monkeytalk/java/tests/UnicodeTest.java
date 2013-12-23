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
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class UnicodeTest extends TestHelper {
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
	public void testUnicode() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.button("Héìíô\u21D0\u21D1\u21DD\u21DC").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(1));
		assertThat(cmds.get(0).getCommand(), is("Button Héìíô\u21D0\u21D1\u21DD\u21DC tap"));

		assertThat(output, containsString("Button Héìíô\u21D0\u21D1\u21DD\u21DC tap -> OK\n"));
	}

	@Test
	public void testRawUnicode() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.raw("Button Héìíô\u21D0\u21D1\u21DD\u21DC Tap");
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(1));
		assertThat(cmds.get(0).getCommand(), is("Button Héìíô\u21D0\u21D1\u21DD\u21DC Tap"));

		assertThat(output, containsString("Button Héìíô\u21D0\u21D1\u21DD\u21DC Tap -> OK\n"));
	}
}
