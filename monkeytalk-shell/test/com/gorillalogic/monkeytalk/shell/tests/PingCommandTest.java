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
package com.gorillalogic.monkeytalk.shell.tests;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.shell.command.Ping;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class PingCommandTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18334;

	private File dir;
	private ScriptProcessor processor;
	private ByteArrayOutputStream out;

	@Before
	public void before() throws IOException {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));

		dir = FileUtils.tempDir();
		processor = new ScriptProcessor(HOST, PORT, dir);
	}

	@After
	public void after() {
		processor = null;
	}

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testBadPing() throws Exception {
		Ping ping = new Ping("/ping", processor);

		CommandServer server = new CommandServer(PORT);
		ping.run();
		server.stop();

		assertThat(out.toString(), containsString("bad ping"));
	}

	@Test
	public void testPing() throws Exception {
		Ping ping = new Ping("/ping", processor);

		PingServer server = new PingServer(PORT);
		ping.run();
		server.stop();

		assertThat(out.toString(), containsString("OS Agent"));
		assertThat(out.toString(), containsString("vVER"));
		assertThat(out.toString(), containsString("recording REC"));
	}

	public class PingServer extends JsonServer {
		public PingServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			return new Response(HttpStatus.OK,
					"{message:{os:\"OS\",mtversion:\"VER\",record:\"REC\"}}");
		}
	}
}