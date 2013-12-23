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

public class ArgsTest extends TestHelper {
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

	@Test
	public void testIntArg() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.table().scrollToRow(123);
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(1));
		assertThat(cmds.get(0).getCommand(), is("Table * scrollToRow 123"));

		assertThat(output, containsString("Table * scrollToRow 123 -> OK\n"));
	}

	@Test
	public void testFloatArg() throws IOException {
		CommandServer server = new CommandServer(PORT);
		MonkeyTalkDriver mt = new MonkeyTalkDriver(tempDir(), "iOS", HOST, PORT);
		mt.setScriptListener(LISTENER_WITH_OUTPUT);

		Application app = mt.app();
		app.slider().select(123);
		app.slider().select(123.45f);
		app.slider().select(0.45f);
		app.slider().select(0.5600f);
		app.slider().select(0.0f);
		app.slider().select(0001.0f);
		server.stop();

		List<Command> cmds = server.getCommands();
		assertThat(cmds.size(), is(6));
		assertThat(cmds.get(0).getCommand(), is("Slider * select 123"));
		assertThat(cmds.get(1).getCommand(), is("Slider * select 123.45"));
		assertThat(cmds.get(2).getCommand(), is("Slider * select 0.45"));
		assertThat(cmds.get(3).getCommand(), is("Slider * select 0.56"));
		assertThat(cmds.get(4).getCommand(), is("Slider * select 0"));
		assertThat(cmds.get(5).getCommand(), is("Slider * select 1"));

		assertThat(output, containsString("Slider * select 123 -> OK\n"));
		assertThat(output, containsString("Slider * select 123.45 -> OK\n"));
		assertThat(output, containsString("Slider * select 0.45 -> OK\n"));
		assertThat(output, containsString("Slider * select 0.56 -> OK\n"));
		assertThat(output, containsString("Slider * select 0 -> OK\n"));
		assertThat(output, containsString("Slider * select 1 -> OK\n"));

	}
}
