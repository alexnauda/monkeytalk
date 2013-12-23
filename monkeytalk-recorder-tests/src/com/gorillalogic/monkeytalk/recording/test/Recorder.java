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




package com.gorillalogic.monkeytalk.recording.test;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.server.ServerConfig;

/**
 * Simple commandline recorder that sends a single ping to turn on recording,
 * then just listens forever
 */
public class Recorder {
	private static final int PLAYBACK_PORT = ServerConfig.DEFAULT_PLAYBACK_PORT_WEB; 
	private static final int RECORD_PORT = 26862;
	private static final boolean IS_ANDROID = false;
	private static final String ADB = "/Applications/android-sdk-mac_x86/platform-tools/adb";
	private static RecordServer server;
	public static LinkedBlockingQueue<Command> buffer = new LinkedBlockingQueue<Command>(); 

	public static void main(String[] args) {
		
		start();
	}

	public static void start() {
		System.out.println("Recorder:");

		try {
			server = new RecordServer(RECORD_PORT);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		System.out.println("  " + server);

		String recordHost = null;
		try {
			recordHost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		System.out.println("  Local ip address is " + recordHost);
		
		if (IS_ANDROID) {
			androidPortForwarding(ADB, PLAYBACK_PORT);
		}

		IAgent agent = AgentManager.getAgent("AndroidEmulator");
		agent.setProperty(AndroidEmulatorAgent.ADB_PROP, ADB);
		
//		CommandSender sender = new CommandSender(PLAYBACK_HOST, PLAYBACK_PORT);
		agent.start();		
		CommandSender sender = agent.getCommandSender();

		Response resp = sender.ping(true, recordHost, RECORD_PORT);
		System.out.println(resp);
		synchronized(Recorder.class) {
			Recorder.class.notifyAll();
		}

		while (true) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private static void androidPortForwarding(String adb, int port) {
		String command = adb + " forward tcp:" + port + " tcp:" + port;
		Runtime runtime = Runtime.getRuntime();
		Process p;
		try {
			System.out.println("Exec: " + command);
			p = runtime.exec(command);

			if (p.waitFor() != 0) {
				InputStream out = p.getErrorStream();
				StringBuilder s = new StringBuilder();
				while (out.available() > 0) {
					s.append((char) out.read());
				}
				System.out.println("Failed exec command=" + command + " msg="
						+ s.toString());
			}
		} catch (Exception ex) {
			System.out.println("Error exec command=" + command + " ex=" + ex);
		}
	}

	public static class RecordServer extends JsonServer {

		public RecordServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method,
				Map<String, String> headers, JSONObject json) {

			String mtcommand = json.optString("mtcommand");

			if ("RECORD".equals(mtcommand)) {
				Command cmd = new Command(json);
				System.out.println("RECORD - " + cmd.toString());
					try {
						buffer.put(new Command(json));
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}


			} else {
				System.out.println("UNKNOWN - " + json.toString());
			}
			
			return new Response();
		}
	}

	public static void waitUntilReady() {
		synchronized(Recorder.class) {
			try {
				Recorder.class.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	public synchronized static Command popNextCommand(long timeout) {
		Command cmd;
		try {
			cmd = buffer.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return cmd;
		
	}

	public synchronized static void clearCommands() {
		buffer.clear();
	}
}
