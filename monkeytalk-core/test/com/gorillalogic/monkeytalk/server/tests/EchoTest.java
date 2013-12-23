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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Sender;
import com.gorillalogic.monkeytalk.server.JsonServer;

public class EchoTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18008;
	private Sender sender;
	private JsonServer server;
	private URL url;

	public void startServer(String host, int port) {
		sender = new Sender();

		try {
			url = new URL("http", host, port, "/");
		} catch (MalformedURLException ex) {
			fail("bad server url");
		}

		try {
			server = new JsonServer(port);
		} catch (IOException ex) {
			fail("server failed to start");
		}
		System.out.println("server running on " + server.getPort() + "...");
	}

	public void stopServer() {
		assertThat(server.isRunning(), is(true));
		server.stop();
		assertThat(server.isRunning(), is(false));
		System.out.println("server stopped on " + server.getPort() + "...");
	}

	@Ignore("sometimes fails in CI for reasons unknown")
	@Test
	public void testSendText() {
		startServer(HOST, PORT + 800);
		Response resp = sender.send(url, "text", "text/plain");

		assertThat(resp, notNullValue());

		if (resp.getCode() == 0) {
			System.out.println("FATAL: " + resp);
		}

		assertThat(resp.getCode(), is(500));
		assertThat(resp.getBody(), containsString("ERROR"));
		assertThat(resp.getBody(), containsString("you must send application/json data"));
		stopServer();
	}

	@Ignore("sometimes fails in CI for reasons unknown")
	@Test
	public void testSendHtml() {
		startServer(HOST, PORT + 801);
		Response resp = sender.send(url, "<html><body>body</body></html>", "text/html");

		assertThat(resp, notNullValue());

		if (resp.getCode() == 0) {
			System.out.println("FATAL: " + resp);
		}

		assertThat(resp.getCode(), is(500));
		assertThat(resp.getBody(), containsString("ERROR"));
		assertThat(resp.getBody(), containsString("you must send application/json data"));
		stopServer();
	}

	@Test
	public void testSendJSON() {
		startServer(HOST, PORT + 802);
		Response resp = sender.sendJSON(url, "{foo:123}");

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));
		assertThat(resp.getBody(), containsString("OK"));
		assertThat(resp.getBody(), containsString("foo"));
		assertThat(resp.getBody(), containsString("123"));
		stopServer();
	}

	@Test
	public void testSendToUrlWithQuery() {
		startServer(HOST, PORT + 803);
		URL url = null;
		try {
			url = new URL("http", HOST, PORT + 803, "/path?foo=bar");
		} catch (MalformedURLException ex) {
			fail("bad url with query");
		}

		Response resp = sender.sendJSON(url, "{foo:123}");

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));
		assertThat(resp.getBody(), containsString("OK"));
		assertThat(resp.getBody(), containsString("foo"));
		assertThat(resp.getBody(), containsString("123"));
		stopServer();
	}

	@Test
	public void testSendJSONObject() throws JSONException {
		startServer(HOST, PORT + 804);
		JSONObject json = new JSONObject();
		json.put("str", "foobar");
		json.put("int", 123);
		json.put("bool", true);

		Response resp = sender.sendJSON(url, json);
		JSONObject body = resp.getBodyAsJSON();

		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");

		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.toString(), is(json.toString()));
		stopServer();
	}

	@Test
	public void testEcho() throws JSONException {
		startServer(HOST, PORT + 805);
		List<String> jsonList = new ArrayList<String>(
				Arrays.asList("{}", "{\"foo\":123}", "{\"foo\":123,\"bar\":654}",
						"{\"str\":\"foobar\",\"int\":123,\"bool\":true}",
						"{\"arr\":[1,2,3],\"hash\":{\"foo\":123}}",
						"{\"arr\":[[[[1,2,3],2,3],2,3],2,3],\"hash\":{\"hash2\":{\"hash3\":{\"hash4\":{\"foo\":123}}}}}"));

		for (String jsonStr : jsonList) {
			JSONObject json = new JSONObject(jsonStr);
			Response resp = sender.sendJSON(url, json.toString());

			JSONObject body = resp.getBodyAsJSON();
			assertThat(body, notNullValue());

			JSONObject message = body.getJSONObject("message");
			assertThat(message, notNullValue());

			JSONObject echo = message.getJSONObject("body");
			assertThat(echo, notNullValue());

			@SuppressWarnings("unchecked")
			Iterator<String> keys = json.keys();

			while (keys.hasNext()) {
				String k = keys.next();

				assertThat(echo.has(k), is(true));
				assertThat(echo.get(k).toString(), is(json.get(k).toString()));
			}
		}
		stopServer();
	}
}