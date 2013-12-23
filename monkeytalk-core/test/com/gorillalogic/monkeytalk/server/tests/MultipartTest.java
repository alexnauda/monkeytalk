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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.utils.Base64;

public class MultipartTest {
	private static final int PORT = 18005;

	@Test
	public void testMultipart() throws IOException, JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://localhost:" + PORT + "/");

		File image = new File("resources/test/base.png");
		FileBody imagePart = new FileBody(image);
		StringBody messagePart = new StringBody("some message");

		MultipartEntity req = new MultipartEntity();
		req.addPart("image", imagePart);
		req.addPart("message", messagePart);
		httppost.setEntity(req);

		ImageEchoServer server = new ImageEchoServer(PORT);
		HttpResponse response = httpclient.execute(httppost);
		server.stop();

		HttpEntity resp = response.getEntity();
		assertThat(resp.getContentType().getValue(), is("application/json"));

		// sweet one-liner to convert an inputstream to a string from stackoverflow:
		// http://stackoverflow.com/questions/309424/in-java-how-do-i-read-convert-an-inputstream-to-a-string
		String out = new Scanner(resp.getContent()).useDelimiter("\\A").next();

		JSONObject json = new JSONObject(out);

		String base64 = Base64.encodeFromFile("resources/test/base.png");

		assertThat(json.getString("screenshot"), is(base64));
		assertThat(json.getBoolean("imageEcho"), is(true));
	}

	private class ImageEchoServer extends JsonServer {
		public ImageEchoServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers, String body,
				Map<String, String> imageHeaders, byte[] image) {

			// take the POSTed image and echo it back in the response
			return new Response(HttpStatus.OK, "{imageEcho:\"true\"}", null, image);
		}
	}
}