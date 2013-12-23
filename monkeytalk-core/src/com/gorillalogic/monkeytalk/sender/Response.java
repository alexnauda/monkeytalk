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

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.server.JsonServer.HttpStatus;
import com.gorillalogic.monkeytalk.utils.Base64;

/**
 * The response from an HTTP POST, returned by the {@link CommandSender} during playback or
 * recording.
 */
public class Response {
	private int code;
	private String body;
	private ResponseStatus status;
	private String message;
	private String warning;
	private String image;
	private File imageFile = null;

	/**
	 * Instantiate a {@code 200 OK} response with an empty body.
	 */
	public Response() {
		this(HttpStatus.OK.getCode(), null);
	}

	/**
	 * Instantiate a response with a code of {@code 200 OK} and the given body.
	 * 
	 * @param body
	 *            the JSON body
	 */
	public Response(String body) {
		this(HttpStatus.OK.getCode(), body);
	}

	/**
	 * <p>
	 * Instantiate a response with the given HTTP status code and JSON body as returned by an HTTP
	 * POST. The JSON body is parsed into a {@link ResponseStatus} plus a message.
	 * </p>
	 * 
	 * <p>
	 * For example, given a code of {@code 200 OK} and a body of
	 * <code>{result:"OK",message:"foo"}</code>, the status is {@link ResponseStatus#OK} and the
	 * message is {@code "foo"}.
	 * </p>
	 * 
	 * @see ResponseStatus
	 * 
	 * @param code
	 *            the HTTP status code
	 * @param body
	 *            the JSON body
	 */
	public Response(int code, String body) {
		this.code = code;
		this.body = body;

		status = ResponseStatus.ERROR;

		if (code != HttpStatus.OK.getCode()) {
			JSONObject json = getBodyAsJSON();
			if (json != null && json.has("message")) {
				try {
					JSONObject msg = json.getJSONObject("message");
					message = msg.optString("message", null);
					warning = msg.optString("warning", null);
					image = msg.optString(msg.has("screenshot") ? "screenshot" : "image", null);
				} catch (JSONException ex) {
					message = json.optString("message", null);
				}
			} else {
				message = body;
			}
		} else {
			if (body == null) {
				status = ResponseStatus.OK;
			} else {
				JSONObject json = getBodyAsJSON();
				if (json != null) {
					String result = json.optString("result");
					if (result != null) {
						if (result.equalsIgnoreCase("ok")) {
							status = ResponseStatus.OK;
						} else if (result.equalsIgnoreCase("failure")) {
							status = ResponseStatus.FAILURE;
						}
					}

					try {
						JSONObject msg = json.getJSONObject("message");
						message = msg.optString("message", null);
						warning = msg.optString("warning", null);
						image = msg.optString(msg.has("screenshot") ? "screenshot" : "image", null);
					} catch (JSONException ex) {
						message = json.optString("message", null);
						warning = json.optString("warning", null);
						image = json.optString(json.has("screenshot") ? "screenshot" : "image",
								null);
						
					}
				} else {
					message = body;
				}
			}
		}
	}
	
	public Response(ResponseStatus status, String message, String warning, String image) {
		this.status = status;
		this.message = message;
		this.warning = warning;
		this.image = image;

		code = HttpStatus.OK.getCode();

		JSONObject msg = new JSONObject();
		try {
			msg.put("result", status.toString());
			if (warning == null && image == null) {
				msg.putOpt("message", message);
			} else {
				JSONObject msgObj = new JSONObject();
				msgObj.putOpt("message", message);
				msgObj.putOpt("warning", warning);
				msgObj.putOpt("screenshot", image);
				msg.put("message", msgObj);
			}
			body = msg.toString();
		} catch (JSONException ex) {
			body = null;
		}
	}

	/**
	 * Get the HTTP status code.
	 * 
	 * @return the HTTP status code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Get the JSON body as a string.
	 * 
	 * @return the JSON body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Get the JSON body as a {@code JSONObject} object.
	 * 
	 * @return the JSON body
	 */
	public JSONObject getBodyAsJSON() {
		if (body == null) {
			return null;
		}

		try {
			return new JSONObject(body);
		} catch (JSONException ex) {
			return null;
		}
	}

	/**
	 * Get the response status, which is one of OK, ERROR, or FAILURE.
	 * 
	 * @return the response status
	 */
	public ResponseStatus getStatus() {
		return status;
	}

	/**
	 * Get the response message, typically everything under the {@code message} key in the JSON
	 * body.
	 * 
	 * @return the response message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the response warning, typically everything under the {@code warning} key in the JSON
	 * body.
	 * 
	 * @return the response message
	 */
	public String getWarning() {
		return warning;
	}

	/**
	 * Get the response image, typically everything under the {@code image} key in the JSON body.
	 * 
	 * @return the response message
	 */
	public String getImage() {
		return image;
	}

	public File getImageFile() {
		if (imageFile != null) {
			return imageFile;
		}
		
		if (image == null) {
			return null;
		}
		
		try {
			imageFile = File.createTempFile("screenshot_", ".png");
			Base64.decodeToFile(image, imageFile.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("Error getting image: " + e.getMessage());
		}
		
		return imageFile;
	}

	@Override
	public String toString() {
		return status + (message != null && message.length() > 0 ? " : " + message : "");
	}

	/**
	 * Enum for the three possible Response statuses: OK, ERROR, or FAILURE.
	 */
	public enum ResponseStatus {
		/**
		 * Successfully played MonkeyTalk command.
		 */
		OK,
		/**
		 * Error playing MonkeyTalk command (halt playback).
		 */
		ERROR,
		/**
		 * Failed to verify MonkeyTalk command (fail the test).
		 */
		FAILURE;
	}

	public static class Builder {
		private ResponseStatus status;
		private String message;
		private String warning;
		private String image;

		public Builder() {
			this(null);
		}

		public Builder(String message) {
			this.status = ResponseStatus.OK;
			this.message = message;
		}

		public Builder ok() {
			this.status = ResponseStatus.OK;
			return this;
		}

		public Builder failure() {
			this.status = ResponseStatus.FAILURE;
			return this;
		}

		public Builder error() {
			this.status = ResponseStatus.ERROR;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder warning(String warning) {
			this.warning = warning;
			return this;
		}

		public Builder image(String image) {
			this.image = image;
			return this;
		}

		public Response build() {
			return new Response(status, message, warning, image);
		}
	}

	public void setImageFile(File image) {
		imageFile = image;
		
	}
}