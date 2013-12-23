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
package com.gorillalogic.monkeytalk.sender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class to send HTTP messages via a HTTP POST.
 */
public class Sender {
	public static final String MIME_JSON = "application/json";

	private static final int TIMEOUT_CONNECT = 10000;
	private static final int TIMEOUT_READ = 30000;

	/**
	 * Send a JSON message to the given url.
	 * 
	 * @param url
	 *            the target url
	 * @param json
	 *            the JSON message body
	 * @return the response
	 */
	public Response sendJSON(URL url, String json) {
		return send(url, json, MIME_JSON);
	}

	/**
	 * Send a JSON message to the given url.
	 * 
	 * @param url
	 *            the target url
	 * @param json
	 *            the JSON message body as a {@link JSONObject} object
	 * @return the response
	 */
	public Response sendJSON(URL url, JSONObject json) {
		return send(url, json.toString(), MIME_JSON);
	}

	/**
	 * Send a message to the given url.
	 * 
	 * @param url
	 *            the target url
	 * @param message
	 *            the message body
	 * @param contentType
	 *            the HTTP content type (ex: {@code application/json})
	 * @return the response
	 */
	public Response send(URL url, String message, String contentType) {
		HttpURLConnection conn = null;

		if (message == null) {
			message = "";
		}

		int readTimeout = TIMEOUT_READ;

		try {
			JSONObject json = new JSONObject(message);
			JSONObject modifiers = json.getJSONObject("modifiers");
			String timeout = modifiers.optString("timeout");
			String thinktime = modifiers.optString("thinktime");
			if (timeout != null && timeout.length() > 0) {
				readTimeout += Integer.parseInt(timeout);
			}
			if (thinktime != null && thinktime.length() > 0) {
				readTimeout += Integer.parseInt(thinktime);
			}
		} catch (JSONException ex) {
			readTimeout = TIMEOUT_READ;
		}

		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(TIMEOUT_CONNECT);
			conn.setReadTimeout(readTimeout);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", contentType);
			conn.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			out.write(message);
			out.close();

			String resp = null;
			try {
				resp = readFromStream(conn.getInputStream());
			} catch (IOException ex) {
				resp = readFromStream(conn.getErrorStream());
			}
			return new Response(conn.getResponseCode(), resp);
		} catch (SocketTimeoutException ex) {
			return new Response(0, "{result: \"ERROR\", message: \"Timeout connecting to " + url
					+ ": " + ex.getMessage().replaceAll("\"", "'") + "\"}");
		} catch (Exception ex) {
			return new Response(0,
					"{result: \"ERROR\", message: \"Unable to send command to "
							+ url
							+ ": "
							+ (ex.getMessage() != null ? ex.getMessage().replaceAll("\"", "'")
									+ "\"}" : ""));
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private String readFromStream(InputStream in) throws UnsupportedEncodingException, IOException {
		if (in == null) {
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} finally {
			reader.close();
		}
		return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
	}
}