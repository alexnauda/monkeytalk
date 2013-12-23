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
package com.gorillalogic.monkeytalk.agents;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.CommandSenderFactory;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.server.ServerConfig;

public class MTAgent implements IAgent {
	private Map<String, String> props;
	private CommandSender commandSender;
	private String host;
	private int port;

	public MTAgent() {
		this(ServerConfig.DEFAULT_PLAYBACK_HOST, -1);
	}

	public MTAgent(int port) {
		this(ServerConfig.DEFAULT_PLAYBACK_HOST, port);
	}

	public MTAgent(String host, int port) {
		super();
		props = new HashMap<String, String>();
		commandSender = null;
		setHost(host);
		setPort(port);
	}

	@Override
	public String getName() {
		return "MTAgent";
	}

	@Override
	public CommandSender getCommandSender() {
		if (getHost() == null || getPort() == -1) {
			throw new IllegalStateException("playback host and port not set");
		}
		if (commandSender == null) {
			commandSender = createCommandSender(getHost(), getPort());
		}

		return commandSender;
	}

	protected CommandSender createCommandSender(String host, int port) {
		return CommandSenderFactory.createCommandSender(host, port);
	}

	@Override
	public void setProperty(String key, String val) {
		if (props == null) {
			props = new HashMap<String, String>();
		}
		props.put(key, val);
	}

	@Override
	public String getProperty(String key) {
		return props.get(key);
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void start() {
		String errMsg = validate();
		if (errMsg != null) {
			throw new RuntimeException(errMsg);
		}
	}

	@Override
	public void stop() {
	}

	@Override
	public void close() {
	}

	@Override
	public String validate() {
		if (getHost() == null || getPort() == -1) {
			return getName() + " - playback host or port not set";
		}
		return null;
	}

	@Override
	public Command filterCommand(Command cmd) {
		return cmd;
	}

	@Override
	public boolean isReady() {
		boolean ready = false;
		try {
			start();
			Response response = getCommandSender().ping(false);
			if (response != null) {
				ready = ResponseStatus.OK.equals(response.getStatus());
			}
		} catch (Exception e) {
		}
		return ready;
	}

	@Override
	public String getAgentVersion() {
		String version = "";
		try {
			start();
			Response response = getCommandSender().ping(false);
			if (response != null) {
				JSONObject body=response.getBodyAsJSON();
				if (body!=null) {
					if (body.has("message")) {
						body=body.getJSONObject("message");
					}
					if (body.has("mtversion")) {
						version = body.getString("mtversion");
					}
				}
			}
		} catch (Exception e) {
		}
		return version;
	}

	@Override
	public boolean waitUntilReady(long timeout) {
		if (timeout < 1) {
			return true;
		}
		long start = System.currentTimeMillis();
		long pause = timeout / 100;
		if (pause < 50) {
			pause = 50;
		} else if (pause > 1000) {
			pause = 1000;
		}
		while (System.currentTimeMillis() - start < timeout) {
			if (isReady()) {
				return true;
			}
			try {
				Thread.sleep(pause);
			} catch (InterruptedException ex) {
				break;
			}
		}
		return false;
	}
}
