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
package com.gorillalogic.monkeytalk.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.utils.Base64;

/**
 * <p>
 * Nano-sized JSON-based HTTP server. All inbound POSTs have JSON-encoded bodies, all outbound
 * responses have JSON-encoded bodies. Some server code is borrowed from <a
 * href="http://elonen.iki.fi/code/nanohttpd/">NanoHTTPD</a>, which is BSD licensed.
 * </p>
 * 
 * <p>
 * By default the server comes up in echo mode (aka inbound messages are just echoed back). The
 * easiest way to customize the response is to extend {@link JsonServer} and override the
 * {@link JsonServer#serve(String, String, Map, JSONObject)} method.
 * </p>
 * 
 * <p>
 * Alternately, you can override the {@link JsonServer#serve(String, String, Map, String)} method if
 * you wish to handle the JSON parsing yourself.
 * </p>
 * 
 * @see <a href="http://elonen.iki.fi/code/nanohttpd/">NanoHTTPD</a>
 */
public class JsonServer {
	private static final String MIME_JSON = "application/json";
	private static final String MIME_MULTIPART = "multipart/form-data";
	private static final String MIME_HTML = "text/html";

	private ServerSocket serverSocket;
	private Thread serverThread;

	private static final SimpleDateFormat sdf;
	static {
		sdf = new java.text.SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Instantiate a new JSON server with the given port on a background daemon thread. The server
	 * immediately comes up listening for messages on the given port.
	 * 
	 * @param port
	 *            the server port
	 * @throws IOException
	 *             if an I/O error occurs while opening the socket
	 */
	public JsonServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						new HttpSession(serverSocket.accept());
					}
				} catch (IOException ex) {
					// do nothing
				}
			}
		});
		serverThread.setDaemon(true);
		serverThread.start();
	}

	/**
	 * Stop the server and close the socket. This blocks while the server thread is stopped. If an
	 * error occurs, just die silently.
	 */
	public void stop() {
		try {
			serverSocket.close();
			serverThread.join();
		} catch (InterruptedException ex) {
			throw new RuntimeException("ERROR: interrupted while stopping - ex=" + ex.getMessage());
		} catch (IOException ex) {
			throw new RuntimeException("ERROR: error while stopping - ex=" + ex.getMessage());
		}
	}

	/**
	 * <p>
	 * By default, just echo the incoming request as the response. Returns a {@link Response} object
	 * with a status code of 200 and a simple JSON message. For example, if the request is sent to
	 * <code>/foo</code> with a body of <code>{hello:123}</code>, then response would be
	 * <code>{result:OK, message:{method:"POST", uri:"/foo", body:{hello:123}}}</code> .
	 * </p>
	 * 
	 * <p>
	 * Extend {@link JsonServer} and override this method if you wish to get the body as a string
	 * and handle the JSON parsing yourself. A better choice, would be to override
	 * {@link JsonServer#serve(String, String, Map, JSONObject)} to customize the response.
	 * </p>
	 * 
	 * @see JsonServer#serve(String, String, Map, JSONObject)
	 * 
	 * @param uri
	 *            the URI
	 * @param method
	 *            the HTTP method (GET, POST, etc.)
	 * @param headers
	 *            the HTTP headers
	 * @param body
	 *            the POST body
	 * @return the response
	 */
	public Response serve(String uri, String method, Map<String, String> headers, String body) {
		if (body == null) {
			return serve(uri, method, headers, (JSONObject) null);
		}

		JSONObject json;
		try {
			json = new JSONObject(body);
		} catch (JSONException ex) {
			json = new JSONObject();
		}

		return serve(uri, method, headers, json);
	}

	/**
	 * <p>
	 * By default, just echo the incoming request as the response. Returns a {@link Response} object
	 * with a status code of 200 and a simple JSON message. For example, if the request is sent to
	 * <code>/foo</code> with a body of <code>{hello:123}</code>, then response would be
	 * <code>{result:OK, message:{method:"POST", uri:"/foo", body:{hello:123}}}</code> .
	 * </p>
	 * 
	 * <p>
	 * Extend {@link JsonServer} and override this method if you wish to get the body as a string
	 * and handle the JSON parsing yourself. A better choice, would be to override
	 * {@link JsonServer#serve(String, String, Map, JSONObject)} to customize the response.
	 * </p>
	 * 
	 * @see JsonServer#serve(String, String, Map, JSONObject)
	 * 
	 * @param uri
	 *            the URI
	 * @param method
	 *            the HTTP method (GET, POST, etc.)
	 * @param headers
	 *            the HTTP headers
	 * @param body
	 *            the POST body
	 * @param imageHeaders
	 *            the HTTP multipart headers from the image
	 * @param image
	 *            the raw image bytes
	 * @return the response
	 */
	public Response serve(String uri, String method, Map<String, String> headers, String body,
			Map<String, String> imageHeaders, byte[] image) {
		if (body == null) {
			return serve(uri, method, headers, (JSONObject) null);
		}

		JSONObject json;
		try {
			json = new JSONObject(body);
		} catch (JSONException ex) {
			json = new JSONObject();
		}

		return serve(uri, method, headers, json);
	}

	/**
	 * <p>
	 * By default, just echo the incoming request as the response. Returns a {@link Response} object
	 * with a status code of 200 and a simple JSON message. For example, if the request is sent to
	 * 
	 * <code>/foo</code> with a body of <code>{hello:123}</code>, then response would be
	 * <code>{result:OK, message:{method:"POST", uri:"/foo", body:{hello:123}}}</code>.
	 * </p>
	 * 
	 * <p>
	 * Extend {@link JsonServer} and override this method to customize the response. Alternately,
	 * you may wish to override {@link JsonServer#serve(String, String, Map, String)} if you wish to
	 * get the body as a string and handle the JSON parsing yourself.
	 * </p>
	 * 
	 * @see JsonServer#serve(String, String, Map, String)
	 * 
	 * @param uri
	 *            the URI
	 * @param method
	 *            the HTTP method (GET, POST, etc.)
	 * @param headers
	 *            the HTTP headers
	 * @param json
	 *            the POST body as a JSON object
	 * @return the response
	 */
	public Response serve(String uri, String method, Map<String, String> headers, JSONObject json) {

		if ("GET".equals(method)) {
			return new Response(HttpStatus.OK, "<!DOCTYPE html>\n<html>\n"
					+ "<head>\n<title>MonkeyTalk</title>\n</head>\n"
					+ "<body>\n<h1>OK</h1>\n<p>server running on port " + this.getPort()
					+ "</p>\n<p>" + BuildStamp.STAMP + "</p>\n</body>\n</html>");
		} else {
			JSONObject message = new JSONObject();
			try {
				message.put("method", method);
				message.put("uri", uri);
				message.put("body", json);
			} catch (JSONException ex) {
				message = new JSONObject();
			}

			JSONObject resp = new JSONObject();
			try {
				resp.put("result", "OK");
				resp.put("message", message);
			} catch (JSONException ex) {
				resp = new JSONObject();
			}
			return new Response(HttpStatus.OK, resp);
		}
	}

	/**
	 * Return true if the JSON server is up and running.
	 * 
	 * @return true if the server is running, otherwise false
	 */
	public boolean isRunning() {
		return serverThread.isAlive();
	}

	/**
	 * Return the port of the JSON server or {@code -1} if the server is not bound and listening
	 * yet.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return serverSocket.getLocalPort();
	}

	@Override
	public String toString() {
		return "JsonServer " + (serverThread.isAlive() ? "alive and running" : "dead")
				+ " on port " + getPort();
	}

	/**
	 * Handles one HTTP session -- parse the request and return the response.
	 */
	private class HttpSession implements Runnable {

		private Socket socket;

		public HttpSession(Socket socket) {
			this.socket = socket;
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public void run() {
			try {
				InputStream is = socket.getInputStream();

				// no stream, so we are done
				if (is == null) {
					return;
				}

				// read the first 8192 bytes, which should get the whole header
				byte[] buf = new byte[8192];
				int len = is.read(buf, 0, buf.length);

				// nothing to read, so we are done
				if (len == -1) {
					is.close();
					return;
				}

				int headerStart = search(buf, new byte[] { 13, 10 });
				int headerEnd = search(buf, new byte[] { 13, 10, 13, 10 });

				if (headerStart == -1 || headerEnd == -1) {
					sendError("failed to read headers");
					is.close();
					return;
				}

				// first line contains HTTP method and target URI
				String urlLine = getStringFromBytes(buf, headerStart);
				String[] parts = urlLine.split("\\s+");
				String method = parts[0];
				String uri = parts[1];

				if (uri.indexOf("?") != -1) {
					uri = uri.substring(0, uri.indexOf("?"));
				}

				// then comes all the headers
				Map<String, String> headers = getHeaders(buf, headerStart + 2, headerEnd);

				boolean isMultipart = false;
				String boundary = null;

				if ("GET".equals(method)) {
					Response r = serve(uri, method, headers, (String) null);
					send(r.getStatus(), MIME_HTML, r.getBody(), r.getHeaders());
					is.close();
					return;
				} else if ("POST".equals(method)) {
					if (!headers.containsKey("content-type")) {
						sendError("Content-Type header is missing");
						is.close();
						return;
					} else if (headers.get("content-type").toLowerCase().startsWith(MIME_JSON)) {
						// we are vanilla JSON
						isMultipart = false;
					} else if (headers.get("content-type").toLowerCase().startsWith(MIME_MULTIPART)) {
						// we are Multipart
						isMultipart = true;
						int boundaryIdx = headers.get("content-type").indexOf("boundary=");
						if (boundaryIdx > 0) {
							boundary = headers.get("content-type").substring(boundaryIdx + 9);
						}
					} else {
						sendError("post data must be " + MIME_JSON + " or " + MIME_MULTIPART);
						is.close();
						return;
					}
				}

				if (!headers.containsKey("content-length")) {
					sendError("Content-Length header is missing");
					is.close();
					return;
				}

				int L = Integer.parseInt(headers.get("content-length"));

				byte[] body = new byte[L];
				int bodylen = len - headerEnd - 4;

				// read in what we've got so far
				for (int i = 0; i < bodylen; ++i) {
					body[i] = buf[i + headerEnd + 4];
				}

				// we didn't get the entire body with first read, so read some more
				while (bodylen < L) {
					int sz = L - bodylen;
					int l = is.read(buf, 0, (sz > 1024 ? 1024 : sz));
					if (l != -1) {
						// append to body
						for (int i = 0; i < l; i++) {
							body[bodylen + i] = buf[i];
						}
						bodylen += l;
					}
				}

				Response r;

				// now we got the whole body, but if multipart we need to serve() the image
				if (isMultipart) {
					byte[] sep = ("--" + boundary).getBytes("UTF-8");

					int start1 = search(body, sep);
					int end1 = search(body, new byte[] { 13, 10, 13, 10 }, start1);

					int start2 = search(body, sep, end1);
					int end2 = search(body, new byte[] { 13, 10, 13, 10 }, start2);

					int last = search(body, sep, end2);

					Map<String, String> headers1 = getHeaders(body, start1 + sep.length + 2, end1);
					Map<String, String> headers2 = getHeaders(body, start2 + sep.length + 2, end2);

					if (headers1 == null) {
						sendError("failed to read " + MIME_MULTIPART + " headers1");
						is.close();
						return;
					}
					if (headers2 == null) {
						sendError("failed to read " + MIME_MULTIPART + " headers2");
						is.close();
						return;
					}

					String bodyStr = null;
					byte[] image = null;
					if (headers1.containsKey("content-disposition")) {
						if (headers1.get("content-disposition").toLowerCase()
								.contains("name=\"message\"")) {
							bodyStr = getStringFromBytes(body, start2 - end1 - 4, end1 + 4);
							image = new byte[last - end2 - 6];
							for (int i = 0; i < image.length; i++) {
								image[i] = body[end2 + 4 + i];
							}
						} else {
							bodyStr = getStringFromBytes(body, last - end2 - 4, end2 + 4);
							image = new byte[start2 - end1 - 6];
							for (int i = 0; i < image.length; i++) {
								image[i] = body[end1 + 4 + i];
							}
						}
					}

					if (bodyStr != null) {
						bodyStr = bodyStr.trim();
					}

					// now serve the image
					r = serve(uri, method, headers, bodyStr, headers1, image);
				} else {
					// now serve the JSON
					r = serve(uri, method, headers, new String(body, "UTF-8").trim());
				}

				if (r == null) {
					sendError("serve() returned null");
					is.close();
					return;
				} else {
					send(r.getStatus(), MIME_JSON, r.getBody(), r.getHeaders());
				}

				is.close();
			} catch (IOException ex) {
				sendError("exception - " + ex.getMessage());
			} finally {
				try {
					socket.close();
				} catch (IOException ex) {
					// do nothing
				}
			}
		}

		private Map<String, String> getHeaders(byte[] body, int start, int end) {
			if (start < 0 || end < 0) {
				return null;
			}

			String s = getStringFromBytes(body, end - start, start);

			Map<String, String> headers = new HashMap<String, String>();
			for (String line : s.split("\r\n")) {
				int i = line.indexOf(':');
				if (i > 0) {
					String key = line.substring(0, i).trim().toLowerCase();
					String val = line.substring(i + 1).trim();
					headers.put(key, val);
				}
			}
			return headers;
		}

		/**
		 * Helper to send an error JSON response.
		 * 
		 * @param err
		 */
		private void sendError(String err) {
			send(HttpStatus.INTERNAL_ERROR, MIME_JSON,
					"{result:\"ERROR\",message:\"" + err + "\"}", null);
		}

		/**
		 * Helper to send a full response: HTTP status, MIME type, body, and HTTP response headers.
		 * Always return response headers {@code Content-Type} and {@code Date} even if the
		 * {@code headers} is {@code null}.
		 * 
		 * @param status
		 *            the HTTP status
		 * @param mine
		 *            the MIME type
		 * @param body
		 *            the body
		 * @param headers
		 *            the HTTP headers
		 */
		private void send(HttpStatus status, String mime, String body, Map<String, String> headers) {
			try {
				PrintWriter pw = new PrintWriter(socket.getOutputStream());
				pw.print("HTTP/1.0 " + status + " \r\n");
				pw.print("Content-Type: " + mime + "\r\n");
				//pw.print("Access-Control-Allow-Origin: *\r\n");

				if (headers == null || headers.get("Date") == null) {
					pw.print("Date: " + sdf.format(new Date()) + "\r\n");
				}

				if (headers != null) {
					for (Map.Entry<String, String> header : headers.entrySet()) {
						pw.print(header.getKey() + ": " + header.getValue() + "\r\n");
					}
				}

				pw.print("\r\n");
				if (body != null) {
					pw.print(body);
				}
				pw.close();
			} catch (IOException ex) {
				try {
					socket.close();
				} catch (IOException ex2) {
				}
			}
		}

		/**
		 * Helper to convert a byte array into a UTF-8 string.
		 * 
		 * @param bytes
		 *            the byte array
		 * @param len
		 *            the number of bytes to convert
		 * @return the UTF-8 string
		 */
		private String getStringFromBytes(byte[] bytes, int len) {
			return getStringFromBytes(bytes, len, 0);
		}

		/**
		 * Helper to convert a byte array into a UTF-8 string.
		 * 
		 * @param bytes
		 *            the byte array
		 * @param len
		 *            the number of bytes to convert
		 * @param offset
		 *            the number of bytes to skip
		 * @return the UTF-8 string
		 */
		private String getStringFromBytes(byte[] bytes, int len, int offset) {
			if (bytes.length > len + offset) {
				if (len == 0) {
					return "";
				}

				byte[] b = new byte[len];
				for (int i = 0; i < len; i++) {
					b[i] = bytes[i + offset];
				}

				try {
					return new String(b, "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					return null;
				}
			}
			return null;
		}

		/**
		 * Helper to find a pattern of bytes (the needle) in the given byte array (the haystack).
		 * 
		 * @param haystack
		 *            the byte array to search
		 * @param needle
		 *            the byte pattern to find
		 * @return the starting index of the found pattern, or -1 if not found
		 */
		private int search(byte[] haystack, byte[] needle) {
			return search(haystack, needle, 0);
		}

		/**
		 * Helper to find a pattern of bytes (the needle) in the given byte array (the haystack)
		 * starting from the given offset.
		 * 
		 * @param haystack
		 *            the byte array to search
		 * @param needle
		 *            the byte pattern to find
		 * @param offset
		 *            the starting offset in bytes
		 * @return the starting index of the found pattern, or -1 if not found
		 */
		private int search(byte[] haystack, byte[] needle, int offset) {
			int i = offset;
			while (i <= haystack.length - needle.length) {
				if (haystack[i] == needle[0]) {
					boolean match = true;
					for (int j = 1; j < needle.length; j++) {
						if (haystack[i + j] != needle[j]) {
							match = false;
							break;
						}
					}
					if (match) {
						return i;
					}
				}
				i++;
			}
			return -1;
		}
	}

	/**
	 * The HTTP response as returned by the {@code serve()} method, contains the status code (and
	 * status message), JSON return body, HTTP response headers, and optionally an image.
	 */
	public class Response {
		private HttpStatus status;
		private String body;
		private Map<String, String> headers;
		private byte[] image;

		/**
		 * Instantiate a new {@code 200 OK} response, with no JSON body and no HTTP headers.
		 */
		public Response() {
			this(HttpStatus.OK, (String) null, null);
		}

		/**
		 * Instantiate a new response with the given HTTP status and JSON body, and no HTTP headers.
		 * 
		 * @param status
		 *            the HTTP status
		 * @param body
		 *            the JSON body
		 */
		public Response(HttpStatus status, String body) {
			this(status, body, null);
		}

		/**
		 * Instantiate a new response with the given HTTP status and JSON body, and no HTTP headers.
		 * 
		 * @param status
		 *            the HTTP status
		 * @param json
		 *            the JSON body
		 */
		public Response(HttpStatus status, JSONObject json) {
			this(status, json, null);
		}

		/**
		 * Instantiate a new response with the given HTTP status, JSON body, and HTTP headers.
		 * 
		 * @param status
		 *            the HTTP status
		 * @param json
		 *            the JSON body
		 * @param headers
		 *            the HTTP headers
		 */
		public Response(HttpStatus status, JSONObject json, Map<String, String> headers) {
			this(status, (json != null ? json.toString() : null), headers);
		}

		/**
		 * Instantiate a new response with the given HTTP status, JSON body, and HTTP headers.
		 * 
		 * @param status
		 *            the HTTP status
		 * @param body
		 *            the JSON body
		 * @param headers
		 *            the HTTP headers
		 */
		public Response(HttpStatus status, String body, Map<String, String> headers) {
			this(status, body, headers, null);
		}
		
		/**
		 * Instantiate a new response with the given HTTP status, JSON body, and HTTP headers.
		 * 
		 * @param status
		 *            the HTTP status
		 * @param body
		 *            the JSON body
		 * @param headers
		 *            the HTTP headers
		 * @param image
		 *            the raw image bytes
		 */
		public Response(HttpStatus status, String body, Map<String, String> headers, byte[] image) {
			this.status = status;
			this.body = body;
			this.headers = headers;
			this.image = image;
		}

		/**
		 * Get the HTTP status.
		 * 
		 * @return the HTTP status
		 */
		public HttpStatus getStatus() {
			return status;
		}

		/**
		 * Get the JSON body as a string.
		 * 
		 * @return the JSON body
		 */
		public String getBody() {
			if (image == null) {
				return body;
			}
			
			try {
				JSONObject json = new JSONObject(body);
				json.put("screenshot", Base64.encodeBytes(image));
				return json.toString();
			} catch (JSONException e) {
				return body;
			}
		}

		/**
		 * Get the HTTP headers.
		 * 
		 * @return the HTTP headers
		 */
		public Map<String, String> getHeaders() {
			return headers;
		}

		/**
		 * Get the raw image bytes.
		 * 
		 * @return the image
		 */
		public byte[] getImage() {
			return image;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("Response:\n");
			sb.append("  status=").append(status).append("\n");
			sb.append("  body=").append(body).append("\n");

			if (headers == null) {
				sb.append("  headers=null\n");
			} else {
				sb.append("  headers:\n");
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					sb.append("    ").append(entry.getKey()).append("=").append(entry.getValue())
							.append("\n");
				}
			}

			sb.append("  image=").append(image == null ? "NULL" : "YES").append('\n');

			return sb.substring(0, sb.length() - 1);
		}
	}

	/**
	 * Enum wrapper around the HTTP Status codes.
	 */
	public enum HttpStatus {
		/**
		 * 200 OK
		 */
		OK(200, "OK"),
		/**
		 * 400 Bad Request
		 */
		BAD_REQUEST(400, "Bad Request"),
		/**
		 * 404 Not Found
		 */
		NOT_FOUND(404, "Not Found"),
		/**
		 * 500 Internal Server Error
		 */
		INTERNAL_ERROR(500, "Internal Server Error");

		private int code;
		private String message;

		/**
		 * Instantiate a new HTTP Status with the given HTTP status code and HTTP status message.
		 * 
		 * @param code
		 *            the HTTP status code
		 * @param message
		 *            the HTTP status message
		 */
		private HttpStatus(int code, String message) {
			this.code = code;
			this.message = message;
		}

		/**
		 * Get the HTTP status code (ex: {@code 200} for a {@code 200 OK} status, {@code 404} for
		 * {@code 404 Not Found} status, etc.)
		 * 
		 * @return the HTTP status code
		 */
		public int getCode() {
			return code;
		}

		/**
		 * Get the HTTP status message (ex: {@code OK} for a {@code 200 OK} status,
		 * {@code Not Found} for {@code 404 Not Found} status, etc.)
		 * 
		 * @return the HTTP status message
		 */
		public String getMessage() {
			return message;
		}

		@Override
		/**
		 * Output the HTTP Status message exactly as it will appear in the response.
		 * @return the HTTP Status message (status code + space + status message)
		 */
		public String toString() {
			return code + " " + message;
		}
	}
}