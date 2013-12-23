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

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.sender.CommandSender;

/**
 * The client-side api for a MonkeyTalk agent. AgentBase provides a default implementation.
 */
public interface IAgent {
	/**
	 * Get the name of the agent.
	 * 
	 * @return the agent name
	 */
	public String getName();

	/**
	 * Get the command sender used to send playback commands to the agent. Throws an
	 * {@link IllegalStateException} if playback host and port are not set.
	 * 
	 * @return the command sender
	 */
	public CommandSender getCommandSender();

	/**
	 * Set an agent-specific configuration property with the given key and value. Ex: the location
	 * of adb for the {@link AndroidEmulatorAgent}.
	 * 
	 * @param key
	 *            the property key
	 * @param val
	 *            the property value
	 */
	public void setProperty(String key, String val);

	/**
	 * Get the value for the given property key.
	 * 
	 * @param key
	 *            the property key
	 * @return the property value
	 */
	public String getProperty(String key);

	/**
	 * Get the playback host.
	 * 
	 * @return the host
	 */
	public String getHost();

	/**
	 * The host where this (remote) agent should run.
	 * 
	 * @param host
	 *            the host
	 */
	public void setHost(String host);

	/**
	 * Get the playback port.
	 * 
	 * @return the post
	 */
	public int getPort();

	/**
	 * Set the playback port (the port the agent should listen on for incoming playback commands).
	 * 
	 * @param port
	 *            the port
	 */
	public void setPort(int port);

	/**
	 * Do any necessary initialization prior to starting playback of a script.
	 */
	public void start();

	/*
	 * Do any necessary processing after completing playback of a script.
	 */
	public void stop();

	/**
	 * Validate the agent and its properties
	 * 
	 * @return {@code null} if valid, otherwise return an error message.
	 */
	public String validate();

	/**
	 * Do agent-specific processing to a command prior to its being recorded.
	 * 
	 * @param cmd
	 *            the command about to be recorded
	 * @return the (possibly modified) command to be recording or null if this command should not be
	 *         recorded
	 */
	public Command filterCommand(Command cmd);

	/**
	 * check whether the agent can be used to send commands to an application
	 * 
	 * @return true if ready, else false
	 */
	public boolean isReady();
	
	/**
	 * wait until the agent is ready, or the supplied timeout expires
	 * 
	 * @param timeout
	 *            the timeout in ms
	 * @return true if ready, false if the wait timed out
	 */
	public boolean waitUntilReady(long timeout);
	
	/**
	 * get the version information from the remote agent, if available
	 * 
	 * @return the version information, or an empty string if it is not available
	 */
	public String getAgentVersion();
	
	/**
	 * Close the app
	 */
	void close();

}
