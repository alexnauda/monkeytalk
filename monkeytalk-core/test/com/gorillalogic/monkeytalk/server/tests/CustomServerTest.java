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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.sender.Sender;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.server.JsonServer;

public class CustomServerTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18009;
	private static Sender sender;
	private static URL url;
	private FooServer server;

	@BeforeClass
	public static void beforeClass() {
		sender = new Sender();
		
		try {
			url = new URL("http://" + HOST + ":" + PORT);
		} catch (MalformedURLException ex) {
			fail("bad server url");
		}
	}

	@Before
	public void before() {
		try {
			server = new FooServer(PORT);
		} catch (IOException ex) {
			fail("FooServer failed to start");
		}

		assertThat(server, notNullValue());
		assertThat(server.isRunning(), is(true));
		assertThat(server.getPort(), is(PORT));
		System.out.println("server running on " + server.getPort() + "...");
	}

	@After
	public void after() {
		assertThat(server.isRunning(), is(true));
		server.stop();
		assertThat(server.isRunning(), is(false));
		System.out.println("server stopped on " + server.getPort() + "...");
	}

	@Test
	public void testSendingNullMessage() {
		Response resp = sender.sendJSON(url, (String) null);
		assertThat(resp,notNullValue());
		assertThat(resp.getBody(),is("{result:\"FOO\"}"));
	}
	
	@Test
	public void testSendingEmptyMessage() {
		Response resp = sender.sendJSON(url, "");
		assertThat(resp,notNullValue());
		assertThat(resp.getBody(),is("{result:\"FOO\"}"));
	}
	
	@Test
	public void testSendingFooMessage() {
		Response resp = sender.sendJSON(url, "{foo:123}");
		assertThat(resp,notNullValue());
		assertThat(resp.getBody(),is("{result:\"FOO\"}"));
	}
	
	@Test
	public void testSendingBarMessage() {
		Response resp = sender.sendJSON(url, "{bar:123}");
		assertThat(resp,notNullValue());
		assertThat(resp.getBody(),is("{result:\"FOOBAR\"}"));
	}

	/**
	 * Returns <code>{result:"FOO"}</code> for all messages, except if the
	 * message body contains {@code bar}, in which case it returns
	 * <code>{result:"FOOBAR"}</code>.
	 */
	private class FooServer extends JsonServer {

		public FooServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method,
				Map<String, String> headers, JSONObject json) {

			if (json.toString().contains("bar")) {
				return new Response(HttpStatus.OK, "{result:\"FOOBAR\"}");
			}
			return new Response(HttpStatus.OK, "{result:\"FOO\"}");
		}
	}
}