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
package com.gorillalogic.monkeyconsole.server;

import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.CommandSenderFactory;
import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.server.ServerConfig;

public class RecordServer extends JsonServer {
	private RecordListener recordListener;
	private boolean recording;
	private CommandSender commandSender;
	private IAgent agent;

	private static final RecordListener DEFAULT_RECORD_LISTENER = new RecordListener() {

		// @Override
		public void onRecord(Command command, JSONObject json) {
		}
	};

	public RecordServer() throws IOException {
		super(ServerConfig.DEFAULT_RECORD_PORT);
		recording = false;
	}

	public RecordListener getRecordListener() {
		if (recordListener == null) {
			recordListener = DEFAULT_RECORD_LISTENER;
		}
		return recordListener;
	}

	public void setRecordListener(RecordListener recordListener) {
		this.recordListener = recordListener;
	}

	public boolean isRecording() {
		return recording;
	}

	@Override
	public Response serve(String uri, String method, Map<String, String> headers, JSONObject json) {

		if ("GET".equals(method)) {
			return super.serve(uri, method, headers, json);
		}

		String mtcommand = json.optString("mtcommand");

		if ("RECORD".equals(mtcommand) && isRecording()) {
			Command cmd = new Command(json);
			cmd = agent.filterCommand(cmd);
			if (cmd != null) {
				// System.out.println("RECORD - " + cmd.toString());
				getRecordListener().onRecord(cmd, json);
			}
		} else {
			// System.out.println("UNKNOWN - " + json.toString());
		}

		return new Response();
	}

	/**
	 * Turn recording ON and OFF on the agent.
	 * 
	 * @param record
	 *            true for recoding ON, false for recoding OFF
	 * @param agentHost
	 *            the target playback host (on the agent)
	 * @param agentPort
	 *            the target playback host (on the agent)
	 * @param recordHost
	 *            the record host (on the IDE)
	 * @param recordPort
	 *            the record port (on the IDE)
	 * @return the response
	 */
	public com.gorillalogic.monkeytalk.sender.Response record(boolean record, String agentHost,
			int agentPort, String recordHost, int recordPort) {
		commandSender = CommandSenderFactory.createCommandSender(agentHost, agentPort);
		com.gorillalogic.monkeytalk.sender.Response resp = commandSender.ping(record, recordHost,
				recordPort);
		recording = record;
		return resp;
	}

	public void setCurrentAgent(IAgent agent) {
		this.agent = agent;

	}
	
	public IAgent getCurrentAgent() {
		return agent;
	}
}