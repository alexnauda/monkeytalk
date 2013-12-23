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
package com.gorillalogic.monkeytalk.sender.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.sender.Sender;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.server.JsonServer;

public class SenderTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18012;
	private static Sender sender;
	private static JsonServer server;
	private static URL url;

	@BeforeClass
	public static void beforeClass() {
		sender = new Sender();

		try {
			url = new URL("http://" + HOST + ":" + PORT);
		} catch (MalformedURLException ex) {
			fail("bad server url");
		}

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

	@Before
	public void before() {
		assertThat(server, notNullValue());
		assertThat(server.isRunning(), is(true));
		assertThat(server.getPort(), is(PORT));
	}

	@Test
	public void testSend() throws JSONException {
		Response resp = sender.send(url, "{foo:123}", Sender.MIME_JSON);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));
		assertThat(resp.getBody(), containsString("OK"));
		assertThat(resp.getBody(), containsString("foo"));
		assertThat(resp.getBody(), containsString("123"));
	}

	@Test
	public void testSendJSONString() throws JSONException {
		Response resp = sender.sendJSON(url, "{foo:123}");

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		assertSame(resp.getBodyAsJSON(), new JSONObject("{foo:123}"));
	}

	@Test
	public void testSendJSONObject() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("foo", 123);

		Response resp = sender.sendJSON(url, json);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		assertSame(resp.getBodyAsJSON(), json);
	}

	private void assertSame(JSONObject dis, JSONObject dat)
			throws JSONException {
		assertThat(dis, notNullValue());
		assertThat(dis.getString("result"), is("OK"));

		JSONObject message = dis.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.toString(), is(dat.toString()));
	}
}