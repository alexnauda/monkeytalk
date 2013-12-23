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
package com.gorillalogic.monkeytalk.server.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.server.JsonServer;

public class ServerTest {
	private static final int PORT = 18007;
	private static JsonServer server;

	@BeforeClass
	public static void beforeClass() {
		try {
			server = new JsonServer(PORT);
		} catch (IOException ex) {
			fail("server failed to start");
		}
		System.out.println("server running on " + server.getPort() + "...");
	}

	@AfterClass
	public static void afterClass() {
		assertThat(server.isRunning(), is(true));
		server.stop();
		assertThat(server.isRunning(), is(false));
		System.out.println("server stopped on " + server.getPort() + "...");
	}

	@Test
	public void testServer() {
		assertThat(server, notNullValue());
		assertThat(server.isRunning(), is(true));
		
		assertThat(server.toString(), containsString("alive and running"));
		assertThat(server.toString(), containsString("port " + PORT));
	}

	@Test
	public void testStopAndRestart() {
		// am I running?
		assertThat(server, notNullValue());
		assertThat(server.isRunning(), is(true));

		// stop it
		server.stop();
		assertThat(server.isRunning(), is(false));

		// restart
		try {
			server = new JsonServer(PORT);
		} catch (IOException ex) {
			fail("server failed to restart");
		}

		// am I running?
		assertThat(server, notNullValue());
		assertThat(server.isRunning(), is(true));
	}
}