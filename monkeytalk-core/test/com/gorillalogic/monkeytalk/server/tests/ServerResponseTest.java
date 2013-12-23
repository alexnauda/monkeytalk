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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.server.JsonServer.HttpStatus;
import com.gorillalogic.monkeytalk.server.JsonServer.Response;

public class ServerResponseTest {
	private static final int PORT = 18010;
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
	public void testDefaultConstructor() {
		Response r = server.new Response();
		assertThat(r.getStatus(), is(HttpStatus.OK));
		assertThat(r.getBody(), nullValue());
		assertThat(r.getHeaders(), nullValue());
		assertThat(r.toString(), containsString("status=" + HttpStatus.OK));
		assertThat(r.toString(), containsString("body=null"));
		assertThat(r.toString(), containsString("headers=null"));
	}

	@Test
	public void testConstructor() {
		Response r = server.new Response(HttpStatus.OK, "body");
		assertThat(r.getStatus(), is(HttpStatus.OK));
		assertThat(r.getBody(), is("body"));
		assertThat(r.getHeaders(), nullValue());
	}

	@Test
	public void testConstructorWithJSON() throws JSONException {
		Response r = server.new Response(HttpStatus.OK, new JSONObject("{result:\"OK\"}"));
		assertThat(r.getStatus(), is(HttpStatus.OK));
		assertThat(r.getBody(), is("{\"result\":\"OK\"}"));
		assertThat(r.getHeaders(), nullValue());
	}

	@Test
	public void testConstructorWithNullJSON() throws JSONException {
		Response r = server.new Response(HttpStatus.OK, (JSONObject) null);
		assertThat(r.getStatus(), is(HttpStatus.OK));
		assertThat(r.getBody(), nullValue());
		assertThat(r.getHeaders(), nullValue());
	}

	@Test
	public void testConstructorWithHeaders() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("SomeHeader","foo");
		
		Response r = server.new Response(HttpStatus.OK, "body", headers);
		assertThat(r.getStatus(), is(HttpStatus.OK));
		assertThat(r.getBody(), is("body"));
		assertThat(r.getHeaders().size(), is(1));
		assertThat(r.getHeaders().keySet(), hasItems("SomeHeader"));
		assertThat(r.getHeaders().values(), hasItems("foo"));
		assertThat(r.toString(), containsString("SomeHeader=foo"));
	}

	@Test
	public void testConstructorWithJSONAndHeaders() throws JSONException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("SomeHeader", "foo");
		headers.put("OtherHeader", "bar");

		Response r = server.new Response(HttpStatus.OK, new JSONObject("{result:\"OK\"}"), headers);
		assertThat(r.getStatus(), is(HttpStatus.OK));
		assertThat(r.getBody(), is("{\"result\":\"OK\"}"));
		assertThat(r.getHeaders().size(), is(2));
		assertThat(r.getHeaders().keySet(), hasItems("SomeHeader", "OtherHeader"));
		assertThat(r.getHeaders().values(), hasItems("foo", "bar"));
		assertThat(r.toString(), containsString("SomeHeader=foo"));
		assertThat(r.toString(), containsString("OtherHeader=bar"));
	}

	@Test
	public void testHttpStatus() {
		HttpStatus status = HttpStatus.NOT_FOUND;
		assertThat(status.getCode(), is(404));
		assertThat(status.getMessage(), is("Not Found"));
		assertThat(status.toString(), is("404 Not Found"));
	}
}