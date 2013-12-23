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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;
import com.gorillalogic.monkeytalk.java.error.MonkeyTalkError;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class ScriptTest extends TestHelper {
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
			output += (output.length() > 0 ? "\n" : "") + "START\n";
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
	public void testScriptRun() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.button("BAR").tap();
		app.script("foo.mt").run();
		app.button("BAZ").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(3));
		assertThat(cmds.get(0).getCommand(), is("Button BAR tap"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button BAZ tap"));

		assertThat(output, containsString("Button BAR tap -> OK\n"));
		assertThat(output, containsString("Script foo.mt run\n"));
		assertThat(output, containsString("START\nButton FOO Tap -> OK\nCOMPLETE"));
		assertThat(output, containsString("Button BAZ tap -> OK\n"));
	}

	@Test
	public void testScriptRunWith() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define name\nButton ${name} Tap", dir);
		tempScript("data.csv", "name\nFOO1\nFOO2\nFOO3", dir);

		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.button("BAR").tap();
		app.script("foo.mt").runWith("data.csv");
		app.button("BAZ").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		System.out.println(cmds);
		assertThat(cmds.size(), is(5));
		assertThat(cmds.get(0).getCommand(), is("Button BAR tap"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO1 Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button FOO2 Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button FOO3 Tap"));
		assertThat(cmds.get(4).getCommand(), is("Button BAZ tap"));

		assertThat(output, containsString("Button BAR tap -> OK\n"));
		assertThat(output, containsString("Script foo.mt runWith data.csv\n"));
		assertThat(output,
				containsString("START\nVars * Define name -> OK\nButton FOO1 Tap -> OK\nCOMPLETE"));
		assertThat(output,
				containsString("START\nVars * Define name -> OK\nButton FOO2 Tap -> OK\nCOMPLETE"));
		assertThat(output,
				containsString("START\nVars * Define name -> OK\nButton FOO3 Tap -> OK\nCOMPLETE"));
		assertThat(output, containsString("Button BAZ tap -> OK\n"));
	}

	@Test
	public void testScriptRunIf() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new EverythingServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.button("BAR").tap();
		app.script("foo.mt").runIf("Label", "foobar", "Verify", "some thing");
		app.button("BAZ").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(4));
		assertThat(cmds.get(0).getCommand(), is("Button BAR tap"));
		assertThat(cmds.get(1).getCommand(), is("Label foobar Verify \"some thing\""));
		assertThat(cmds.get(2).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button BAZ tap"));

		assertThat(output, containsString("Button BAR tap -> OK\n"));
		assertThat(
				output,
				containsString("Script foo.mt runIf Label foobar Verify \"some thing\" -> OK : running foo.mt...\n"));
		assertThat(output, containsString("Script foo.mt Run\n"));
		assertThat(output, containsString("START\nButton FOO Tap -> OK\nCOMPLETE"));
		assertThat(output, containsString("Button BAZ tap -> OK\n"));
	}

	@Test
	public void testScriptRunIfWithFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new EverythingServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.button("BAR").tap();
		app.script("foo.mt").runIf("Label", "foobar", "Verify", "some bob");
		app.button("BAZ").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(3));
		assertThat(cmds.get(0).getCommand(), is("Button BAR tap"));
		assertThat(cmds.get(1).getCommand(), is("Label foobar Verify \"some bob\""));
		assertThat(cmds.get(2).getCommand(), is("Button BAZ tap"));

		assertThat(output, containsString("Button BAR tap -> OK\n"));
		assertThat(
				output,
				containsString("Script foo.mt runIf Label foobar Verify \"some bob\" -> OK : not running foo.mt - fail verify on Bob\n"));
		assertThat(output, not(containsString("Script foo.mt Run\n")));
		assertThat(output, not(containsString("START")));
		assertThat(output, containsString("Button BAZ tap -> OK\n"));
	}

	@Test
	public void testScriptRunIfWithError() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new EverythingServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		try {
			app.button("BAR").tap();
			app.script("foo.mt").runIf("Label", "foobar", "Verify", "joe");
			app.button("BAZ").tap();
			fail("should have thrown exception");
		} catch (Exception ex) {
			assertThat(ex, instanceOf(MonkeyTalkError.class));
			assertThat(ex.getMessage(), is("verify error - error on Joe"));
		}
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(2));
		assertThat(cmds.get(0).getCommand(), is("Button BAR tap"));
		assertThat(cmds.get(1).getCommand(), is("Label foobar Verify joe"));

		assertThat(output, containsString("Button BAR tap -> OK\n"));
		assertThat(
				output,
				containsString("Script foo.mt runIf Label foobar Verify joe -> ERROR : verify error - error on Joe\n"));
		assertThat(output, not(containsString("BAZ")));
	}

	@Test
	public void testDeepScript() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);
		tempScript("bar.mt", "Button BAR1 Tap\nScript foo.mt Run\nButton BAR2 Tap", dir);
		tempScript("baz.mt", "Button BAZ1 Tap\nScript bar.mt Run\nButton BAZ2 Tap", dir);

		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.button("FRED1").tap();
		app.script("baz.mt").run();
		app.button("FRED2").tap();
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(7));
		assertThat(cmds.get(0).getCommand(), is("Button FRED1 tap"));
		assertThat(cmds.get(1).getCommand(), is("Button BAZ1 Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button BAR1 Tap"));
		assertThat(cmds.get(3).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(4).getCommand(), is("Button BAR2 Tap"));
		assertThat(cmds.get(5).getCommand(), is("Button BAZ2 Tap"));
		assertThat(cmds.get(6).getCommand(), is("Button FRED2 tap"));

		assertThat(output, containsString("Button FRED1 tap -> OK"));
		assertThat(output, containsString("Script baz.mt run\nSTART"));
		assertThat(output, containsString("Button BAZ1 Tap -> OK"));
		assertThat(output, containsString("Script bar.mt Run\nSTART"));
		assertThat(output, containsString("Button BAR1 Tap -> OK"));
		assertThat(output, containsString("Script foo.mt Run\nSTART"));
		assertThat(output, containsString("Button FOO Tap -> OK"));
		assertThat(output, containsString("Button BAR2 Tap -> OK"));
		assertThat(output, containsString("Button BAZ2 Tap -> OK"));
		assertThat(output, containsString("Button FRED2 tap -> OK"));
	}

	@Test
	public void testRawScript() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap", dir);

		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(dir, "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);
		Application app = mt.app();
		app.raw("Button BAR Tap");
		app.raw("Script foo.mt Run");
		app.raw("Button BAZ Tap");
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(3));
		assertThat(cmds.get(0).getCommand(), is("Button BAR Tap"));
		assertThat(cmds.get(1).getCommand(), is("Button FOO Tap"));
		assertThat(cmds.get(2).getCommand(), is("Button BAZ Tap"));

		assertThat(output, containsString("Button BAR Tap -> OK\n"));
		assertThat(output, containsString("START\nButton FOO Tap -> OK\nCOMPLETE"));
		assertThat(output, containsString("Button BAZ Tap -> OK\n"));
	}

	/**
	 * Test helper that errors on Joe, fails on Fred, echos the monkeyId on Get, returns OK on
	 * verify, and returns FAILURe on verify with Bob
	 */
	private class EverythingServer extends CommandServer {
		public EverythingServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			Response resp = super.serve(uri, method, headers, json);

			if (json.toString().toLowerCase().contains("joe")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\",message:\"error on Joe\"}");
			} else if (json.toString().toLowerCase().contains("fred")) {
				return new Response(HttpStatus.OK, "{result:\"FAILURE\",message:\"fail on Fred\"}");
			} else if (json.optString("action", "").equalsIgnoreCase("get")) {
				return new Response(HttpStatus.OK, "{result:\"OK\",message:\""
						+ json.optString("monkeyId", "") + "\"}");
			} else if (json.optString("action", "").equalsIgnoreCase("verify")
					&& json.toString().toLowerCase().contains("bob")) {
				return new Response(HttpStatus.OK,
						"{result:\"FAILURE\",message:\"fail verify on Bob\"}");
			} else if (json.optString("action", "").equalsIgnoreCase("verify")) {
				return new Response(HttpStatus.OK, "{result:\"OK\",message:\"msg\"}");
			}

			return resp;
		}
	}
}
