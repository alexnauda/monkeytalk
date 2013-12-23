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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.junit.Test;

import com.gorillalogic.monkeytalk.server.JsonServer;

public class GetTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18006;
	private JsonServer server;
	private URL url;

	public void startServer(String host, int port) {
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

	@Test
	public void testSendGet() throws JSONException {
		startServer(HOST, PORT);
		String resp = sendGet(url);
		assertThat(resp, containsString("<title>MonkeyTalk</title>"));
		assertThat(resp, containsString("<h1>OK</h1>"));
		stopServer();
	}

	private String sendGet(URL url) {
		HttpURLConnection conn = null;
		StringBuilder sb = new StringBuilder();
		String line;

		try {
			conn = (HttpURLConnection) url.openConnection();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),
					"UTF-8"));
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return sb.toString();
	}
}