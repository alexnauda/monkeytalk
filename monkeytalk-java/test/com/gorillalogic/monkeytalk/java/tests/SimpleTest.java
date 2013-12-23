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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;
import com.gorillalogic.monkeytalk.java.error.MonkeyTalkError;
import com.gorillalogic.monkeytalk.java.error.MonkeyTalkFailure;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class SimpleTest extends TestHelper {
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
	public void testSimple() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.input("username").enterText("fred");
		app.input("username").verify("fred");
		app.input("password").enterText("pass");
		app.button("LOGIN").tap();
		app.label().verify("Welcome, fred!");
		app.button("LOGOUT").tap();
		server.stop();

		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(), is("Input username enterText fred"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input username verify fred"));
		assertThat(server.getCommands().get(2).getCommand(), is("Input password enterText pass"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button LOGIN tap"));
		assertThat(server.getCommands().get(4).getCommand(),
				is("Label * verify \"Welcome, fred!\""));
		assertThat(server.getCommands().get(5).getCommand(), is("Button LOGOUT tap"));

		assertThat(output, containsString("Input username enterText fred -> OK\n"));
		assertThat(output, containsString("Input username verify fred -> OK\n"));
		assertThat(output, containsString("Input password enterText pass -> OK\n"));
		assertThat(output, containsString("Button LOGIN tap -> OK\n"));
		assertThat(output, containsString("Label * verify \"Welcome, fred!\" -> OK\n"));
		assertThat(output, containsString("Button LOGOUT tap -> OK\n"));
	}

	@Test
	public void testMonkeyId() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		Application app = mt.app();
		app.button().tap();
		app.button(null).tap();
		app.button("").tap();
		app.button("*").tap();
		app.button("**").tap();
		server.stop();

		assertThat(server.getCommands().size(), is(5));
		assertThat(server.getCommands().get(0).getCommand(), is("Button * tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button * tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button * tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button * tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button ** tap"));
	}

	@Test
	public void testCommandError() throws IOException {
		CommandServer server = new ErrorOnJoeServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		try {
			app.button("FOO").tap();
			app.button("JOE").tap();
			app.button("BAR").tap();
			fail("should have thrown exception");
		} catch (Exception ex) {
			assertThat(ex, instanceOf(MonkeyTalkError.class));
			assertThat(ex.getMessage(), is("error on Joe"));
		}
		server.stop();

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(output, containsString("Button FOO tap -> OK\n"));
		assertThat(output, containsString("Button JOE tap -> ERROR : error on Joe\n"));
		assertThat(output, not(containsString("Button BAR tap")));
	}

	@Test
	public void testCommandFailure() throws IOException {
		CommandServer server = new FailOnFredServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		try {
			app.button("FOO").tap();
			app.button("FRED").tap();
			app.button("BAR").tap();
			fail("should have thrown failure");
		} catch (AssertionError ex) {
			assertThat(ex, instanceOf(MonkeyTalkFailure.class));
			assertThat(ex.getMessage(), is("fail on Fred"));
		}
		server.stop();

		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(output, containsString("Button FOO tap -> OK\n"));
		assertThat(output, containsString("Button FRED tap -> FAILURE : fail on Fred\n"));
		assertThat(output, not(containsString("Button BAR tap")));
	}

	@Test
	public void testRaw() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.raw("Button FOO Tap");
		app.raw("Input BAR EnterText \"Bo Bo\"");
		app.raw("Label BAZ Verify");
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(3));
		assertThat(cmds.get(0).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(1).getCommand(), is("Input BAR EnterText \"Bo Bo\""));
		assertThat(cmds.get(2).getCommand(), is("Label BAZ Verify"));

		assertThat(output, containsString("Button FOO Tap -> OK\n"));
		assertThat(output, containsString("Input BAR EnterText \"Bo Bo\" -> OK\n"));
		assertThat(output, containsString("Label BAZ Verify -> OK\n"));
	}
}
