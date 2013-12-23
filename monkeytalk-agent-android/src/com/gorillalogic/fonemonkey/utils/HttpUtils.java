package com.gorillalogic.fonemonkey.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class HttpUtils {

	public HttpUtils() {
		// Nothing to initialize.
	}

	private static class SendPost extends AsyncTask<String, Void, String> {

		private static URI url;
		private static String json;

		/**
		 * Set server and message to send prior to running ASyncTask. This allows for using an
		 * existing, setup URI instead of passing a string.
		 * 
		 * @param url
		 *            URI of server.
		 * @param json
		 *            String json message to send.
		 */
		private void setBackgroundInfo(URI url, String json) {
			SendPost.url = url;
			SendPost.json = json;
		}

		@Override
		protected String doInBackground(String... arg0) {
			InputStream in = null;

			HttpClient client = new DefaultHttpClient();
			HttpResponse resp = null;
			HttpPost post = new HttpPost(url);
			post.addHeader("Content-type", "application/json;charset=utf-8");
			try {
				post.setEntity(new StringEntity(json));
			} catch (UnsupportedEncodingException e1) {
			}

			try {
				resp = client.execute(post);
			} catch (ClientProtocolException e) {
				// Should fail if server is not running
				return null;
			} catch (IOException e) {
				// Should fail if server is not running
				return null;
			}

			if (resp != null) {
				try {
					in = resp.getEntity().getContent();
					String body = FileUtils.readStream(in);
					return (resp.getStatusLine() != null
							&& resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK ? body
							: null);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return null;
		}
	}

	/**
	 * Sends a post request using an ASyncTask.
	 * 
	 * @param url
	 *            URI of server to send post to.
	 * @param json
	 *            String in json format.
	 * @return Response from server.
	 */
	public String post(URI url, String json) {
		SendPost sendPost = new SendPost();
		// Set information
		sendPost.setBackgroundInfo(url, json);
		try {
			// Return server response
			return new SendPost().execute().get();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
