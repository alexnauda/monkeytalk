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
package com.gorillalogic.monkeytalk.command.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.gorillalogic.monkeytalk.agents.AndroidAgent;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.agents.IOSAgent;
import com.gorillalogic.monkeytalk.agents.MTAgent;
import com.gorillalogic.monkeytalk.server.ServerConfig;

public class AgentTest {

	@Test
	public void testMTAgent() {
		IAgent agent = new MTAgent();
		assertThat(agent.getName(), is("MTAgent"));
		assertThat(agent.getHost(), is(ServerConfig.DEFAULT_PLAYBACK_HOST));
		assertThat(agent.getPort(), is(-1));
	}

	@Test
	public void testAndroidAgent() {
		IAgent agent = new AndroidAgent();
		assertThat(agent.getName(), is("Android"));
		assertThat(agent.getHost(), is(ServerConfig.DEFAULT_PLAYBACK_HOST));
		assertThat(agent.getPort(), is(ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID));
	}

	@Test
	public void testIOSAgent() {
		IAgent agent = new IOSAgent();
		assertThat(agent.getName(), is("iOS"));
		assertThat(agent.getHost(), is(ServerConfig.DEFAULT_PLAYBACK_HOST));
		assertThat(agent.getPort(), is(ServerConfig.DEFAULT_PLAYBACK_PORT_IOS));
	}

	@Test
	public void testCustomAgent() {
		IAgent agent = new MTAgent("host", 1234) {
			
			@Override
			public String getName() {
				return "custom";
			}
		};
		
		assertThat(agent.getName(), is("custom"));
		assertThat(agent.getHost(), is("host"));
		assertThat(agent.getPort(), is(1234));
	}
}