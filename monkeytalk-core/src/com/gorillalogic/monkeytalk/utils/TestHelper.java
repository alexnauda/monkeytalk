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
package com.gorillalogic.monkeytalk.utils;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.server.JsonServer;

public class TestHelper {
	protected static final String DIR_PREFIX = "tmpMT";
	static {
		System.setProperty("user.timezone", "America/Denver");
	}

	/**
	 * Cleanup all child folders in the main temp dir.
	 * 
	 * @throws IOException
	 */
	public static void cleanup() throws IOException {
		File dummy = File.createTempFile("dummy", null);
		dummy.deleteOnExit();

		for (File f : dummy.getParentFile().listFiles()) {
			if (f.isDirectory() && f.getName().startsWith(DIR_PREFIX)) {
				FileUtils.deleteDir(f);
			}
		}
	}

	/**
	 * Create a child temp folder inside the main temp dir.
	 * 
	 * @return the folder
	 * @throws IOException
	 */
	protected File tempDir() throws IOException {
		final String now = Long.toString(System.nanoTime());
		final File dir = File.createTempFile(DIR_PREFIX, now);

		if (!dir.delete()) {
			throw new IOException("failed to delete file: " + dir.getAbsolutePath());
		}

		if (!dir.mkdir()) {
			throw new IOException("failed to create dir: " + dir.getAbsolutePath());
		}

		return dir;
	}

	/**
	 * Create a temp file with the given contents in the given folder.
	 * 
	 * @param filename
	 *            the temp file to be created
	 * @param contents
	 *            the contents
	 * @param dir
	 *            the folder
	 * @return the temp file
	 * @throws IOException
	 */
	protected File tempScript(String filename, String contents, File dir) throws IOException {
		File tmp = new File(dir, filename);
		FileUtils.writeFile(tmp, contents);
		return tmp;
	}

	/**
	 * Extend {@link JsonServer} to capture all commands.
	 */
	public class CommandServer extends JsonServer {
		private List<Command> commands;
		private List<String> jsons;

		public CommandServer(int port) throws IOException {
			super(port);
			commands = new ArrayList<Command>();
			jsons = new ArrayList<String>();
		}

		/**
		 * Get the list of all captured MonkeyTalk commands.
		 * 
		 * @return the commands
		 */
		public List<Command> getCommands() {
			return commands;
		}
		
		/**
		 * Get the list of all captured MonkeyTalk commands in their raw JSON format.
		 * 
		 * @return the commands
		 */
		public List<String> getRawJSONCommands() {
			return jsons;
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {

			jsons.add(json.toString());
			try {
				if (json.getString("mtcommand").equals("PLAY")) {
					commands.add(new Command(json));
				}
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			return new Response(HttpStatus.OK, "{result:\"OK\"}");
		}
	}

	/**
	 * Extend {@link CommandServer} to return an error response if the command contains the string
	 * <code>"joe"</code> anywhere.
	 */
	protected class ErrorOnJoeServer extends CommandServer {
		public ErrorOnJoeServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			if (json.toString().toLowerCase().contains("joe")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\",message:\"error on Joe\"}");
			} else {
				return super.serve(uri, method, headers, json);
			}
		}
	}

	/**
	 * Extend {@link CommandServer} to return a failure response if the command contains the string
	 * <code>"fred"</code> anywhere.
	 */
	protected class FailOnFredServer extends CommandServer {
		public FailOnFredServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			if (json.toString().toLowerCase().contains("fred")) {
				return new Response(HttpStatus.OK, "{result:\"FAILURE\",message:\"fail on Fred\"}");
			} else {
				return super.serve(uri, method, headers, json);
			}
		}
	}
	

	/**
	 * Extend {@link CommandServer} to return {@code monkeyId} as the value for any Get command.
	 */
	protected class GetServer extends CommandServer {

		public GetServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {

			Response r = super.serve(uri, method, headers, json);
			if (json.has("action") && "get".equalsIgnoreCase(json.optString("action"))) {
				return new Response(HttpStatus.OK, "{result:\"OK\",message:\"" + json.optString("monkeyId") + "\"}");
			}
			return r;
		}
	}
		
	protected String findLineMatching(String buffer, String matchThis) throws IOException {
		LineNumberReader rdr=new LineNumberReader(new StringReader(buffer));
		String line=null;
		while ((line=rdr.readLine())!=null) {
			if (line.matches(matchThis)) {
				return line;
			}
		}
		return null;
	}
	
	protected int countOccurences(String buffer, String matchThis) {
		int count=0;
		String rpt=buffer.substring(0);
		while (rpt.indexOf(matchThis)!=-1) {
			count++;
			rpt=rpt.substring(rpt.indexOf(matchThis) + matchThis.length());
		}
		return count;
	}
}