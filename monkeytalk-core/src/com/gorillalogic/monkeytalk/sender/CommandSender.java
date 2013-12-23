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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.server.JsonServer.HttpStatus;

/**
 * Helper class to send a MonkeyTalk {@link Command} object to the target url via a HTTP POST, where
 * the {@code Command} is encoded as JSON in the POST body.
 */
public class CommandSender extends Sender {
	/**
	 * MonkeyTalk wire protocol PLAY command.
	 */
	public static final String PLAY = "PLAY";

	/**
	 * MonkeyTalk wire protocol RECORD command.
	 */
	public static final String RECORD = "RECORD";

	/**
	 * MonkeyTalk wire protocol PING command.
	 */
	public static final String PING = "PING";

	/**
	 * MonkeyTalk wire protocol DUMPTREE command.
	 */
	public static final String DUMPTREE = "DUMPTREE";

	/**
	 * MonkeyTalk wire protocol version.
	 */
	private static final int VERSION = 1;

	/**
	 * Default target host
	 */
	private static final String DEFAULT_TARGET_HOST = "localhost";

	/**
	 * Default target path
	 */
	private static final String DEFAULT_CONTEXT_PATH = "fonemonkey";

	/**
	 * Playback ignores (aka doesn't put on the wire) any of these components.
	 */
	private static final Set<String> IGNORE_COMPONENTS_FOR_PLAY = new HashSet<String>(
			Arrays.asList("doc", "vars", "script", "test", "setup", "teardown"));

	private URL url;

	/**
	 * PACKAGE ACCESS ONLY --- use CommandSenderFactory
	 *
	 * Instantiate a new command sender from the given host and port, where path defaults to
	 * {@link CommandSender#DEFAULT_CONTEXT_PATH}.
	 * 
	 * @param host
	 *            the target host
	 * @param port
	 *            the target port
	 */
	protected CommandSender(String host, int port) {
		this(host, port, DEFAULT_CONTEXT_PATH);
	}
	public CommandSender() {}  // for factory ONLY do not call elsewhere
	public void init(CommandSenderFactory factory, String host, int port, String path) { // for factory ONLY do not call elsewhere
		if (host == null) {
			host = DEFAULT_TARGET_HOST;
		}
		if (path == null) {
			path = DEFAULT_CONTEXT_PATH;
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (port < 0) {
			port = 0;
		}

		try {
			url = new URL("http", host, port, path);
		} catch (MalformedURLException ex) {
			url = null;
		}
	}  

	/**
	 * Instantiate a new command sender from the given host, port, agent, and path that targets the
	 * url <code>http://&lt;host>:&lt;port>/&lt;path></code> for sending all commands.
	 * 
	 * @param host
	 *            the target host
	 * @param port
	 *            the target port
	 * @param path
	 *            the target context path
	 */
	protected CommandSender(String host, int port, String path) {
		init(null, host, port, path);
	}

	/**
	 * Send the dump tree command to the agent. Returns the UI component tree of the app (native
	 * components, plus web components).
	 */
	public Response dumpTree() {
		return sendCommand(DUMPTREE, new JSONObject());
	}

	/**
	 * Send the given MonkeyTalk command as a PLAY to the given url. If the command is a comment,
	 * sending is short-circuited and an OK response is returned immediately.
	 * 
	 * @param command
	 *            the MonkeyTalk command
	 * @return the response
	 */
	public Response play(Command command) {
		if (command == null || command.getCommand() == null) {
			return new Response(HttpStatus.OK.getCode(),
					"{result:\"OK\",message:\"ignore blank command\"}");
		} else if (command.isComment()) {
			return new Response(HttpStatus.OK.getCode(),
					"{result:\"OK\",message:\"ignore comment\"}");
		} else if (IGNORE_COMPONENTS_FOR_PLAY.contains(command.getComponentType().toLowerCase())) {
			return new Response(HttpStatus.OK.getCode(), "{result:\"OK\",message:\"ignore "
					+ command.getCommandName() + "\"}");
		} else {
			return sendCommand(PLAY, command.getCommandAsJSON());
		}
	}

	/**
	 * Build the MonkeyTalk command from its parts ({@code componentType}, {@code monkeyId},
	 * {@code action}, etc.), and play it.
	 * 
	 * @see CommandSender#play(Command)
	 * @see Command#Command(String, String, String, List, Map)
	 * 
	 * @param componentType
	 *            the MonkeyTalk componentType
	 * @param monkeyId
	 *            the MonkeyTalk monkeyId
	 * @param action
	 *            the MonkeyTalk action
	 * @param args
	 *            the MonkeyTalk command args
	 * @param modifiers
	 *            the MonkeyTalk command modifiers
	 * @return the response
	 */
	public Response play(String componentType, String monkeyId, String action, List<String> args,
			Map<String, String> modifiers) {
		return play(new Command(componentType, monkeyId, action, args, modifiers));
	}

	/**
	 * Send the given MonkeyTalk command as a RECORD to the given url.
	 * 
	 * @param command
	 *            the MonkeyTalk command
	 * @return the response
	 */
	public Response record(Command command) {
		return sendCommand(RECORD, command.getCommandAsJSON(false));
	}

	/**
	 * Send the given MonkeyTalk command as a PING to the given url. Ping is used to start and stop
	 * recording, and as a keep-alive to make sure agent exists.
	 * 
	 * @param record
	 *            true turns on agent-side recording (aka agent now sends all recorded commands to
	 *            the IDE), false turns off recording
	 * @return the response
	 */
	public Response ping(boolean record) {
		return ping(record, null, 0);
	}

	/**
	 * Send the given MonkeyTalk command as a PING to the given url. Ping is used to start and stop
	 * recording, and as a keep-alive to make sure agent exists.
	 * 
	 * @param record
	 *            true turns on agent-side recording (aka agent now sends all recorded commands to
	 *            the IDE), false turns off recording
	 * @param recordHost
	 *            the target host for recording (aka the ip addr of the MonkeyTalk IDE record
	 *            server)
	 * @param recordPort
	 *            the target port for recording (aka the port of the MonkeyTalk IDE record server)
	 * @return the response
	 */
	public Response ping(boolean record, String recordHost, int recordPort) {
		JSONObject json = new JSONObject();
		try {
			json.put("record", (record ? "ON" : "OFF"));
			if (record) {
				if (recordHost != null && recordHost.length() > 0) {
					json.put("recordhost", recordHost);
					json.put("recordport", recordPort);
				}
			}
		} catch (JSONException e) {
			return new Response(0, "failed to build outbound JSON message for PING");
		}
		return sendCommand(PING, json);
	}

	/**
	 * Send the given MonkeyTalk command as a JSON message via HTTP POST to the given url. Append to
	 * the given JSON object the {@code mtversion}, {@code mtcommand}, and {@code timestamp}.
	 * 
	 * @param url
	 *            the target url
	 * @param mtcommand
	 *            the MonkeyTalk wire protocol command (PLAY, PING, RECORD)
	 * @param command
	 *            the MonkeyTalk command
	 * @return the response
	 */
	private Response sendCommand(String mtcommand, JSONObject json) {
		try {
			json.put("mtversion", VERSION);
			json.put("mtcommand", mtcommand);
			json.put("timestamp", System.currentTimeMillis());
		} catch (JSONException ex) {
			return new Response(0, "failed to build outbound JSON message");
		}

		URL targetURL = getURLforCommand(mtcommand, json);

		return sendJSON(targetURL, json);
	}
	
	protected URL getURLforCommand(String mtcommand, JSONObject json) {
		return url;
	}

	@Override
	public String toString() {
		return "CommandSender: url=" + url;
	}
	
	protected URL getUrl() {
		return url;
	}
}