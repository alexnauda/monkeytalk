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
import java.util.Set;

public class AgentManager {
	private static Map<String, Class<?>> agentClasses = new HashMap<String, Class<?>>();
	private static Map<String, IAgent> agents = new HashMap<String, IAgent>();

	static {
		registerAgent("MTAgent", MTAgent.class);
		registerAgent("iOS", IOSAgent.class);
		registerAgent("Android", AndroidAgent.class);
		registerAgent("AndroidEmulator", AndroidEmulatorAgent.class);
		registerAgent("CloudAndroid", AndroidCloudAgent.class);
		
		try {
			registerAgent("Flex", Class.forName("com.gorillalogic.agents.html.FlexAgent"));
			registerAgent("WebDriver", Class.forName("com.gorillalogic.agents.html.WebDriverAgent"));
			registerAgent("Firefox", Class.forName("com.gorillalogic.agents.html.WebDriverAgent"));
			registerAgent("Chrome", Class.forName("com.gorillalogic.agents.html.ChromeAgent"));	
			registerAgent("Safari", Class.forName("com.gorillalogic.agents.html.SafariAgent"));	
			registerAgent("IE", Class.forName("com.gorillalogic.agents.html.IEAgent"));	
		} catch (ClassNotFoundException ex) {
			// Circular build dependency. Agents depend on core, but here we want core to ref
			// agents. Need to revisit this soon.
			// throw new IllegalStateException("Unable to load WebDriverAgent");
		}
	}

	public static void registerAgent(String name, Class<?> agentClass) {
		agentClasses.put(name.toLowerCase(), agentClass);
	}
	
	public static void removeAllAgents() {
		agents.clear();
	}

	public static IAgent getAgent(String name) {
		return getAgent(name, null);
	}
	
	public static IAgent getAgent(String name, String host) {
		return getAgent(name, host, -1);
	}

	public static IAgent getAgent(String name, String host, int port) {
		if (name == null) {
			name = "MTAgent";
		}
		String key = name + ":" + host + ":" + port;
		IAgent agent = agents.get(key);
		if (agent != null) {
			return agent;
		}
		Class<?> agentClass = agentClasses.get(name.toLowerCase());
		if (agentClass == null) {
			throw new IllegalArgumentException("Unable to find agent " + name);
		}
		try {
			agent = (IAgent) agentClass.newInstance();
			if (host != null) {
				agent.setHost(host);
			} else {
				host = agent.getHost();
			}
			if (port > 0) {
				agent.setPort(port);
			} else {
				port = agent.getPort();
			}
			if (host!=null && host.length()>0 && port>0) {
				key = name + ":" + host + ":" + port;
				IAgent cachedAgent = agents.get(key);
				if (cachedAgent != null) {
					return cachedAgent;
				}
				agents.put(key, agent);
			}
			return agent;
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to start Agent '" + name
					+ "', allowed agents are: " + getAgentNames(), ex);
		}

	}

	public static IAgent getDefaultAgent() {
		return new MTAgent();
	}
	

	public static IAgent getDefaultAgent(String host, int port) {
		return new MTAgent(host, port);
	}	

	public static Set<String> getAgentNames() {
		return agentClasses.keySet();
	}
}
