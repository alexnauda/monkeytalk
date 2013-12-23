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
package com.gorillalogic.monkeytalk.runner.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;

import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class BaseHelper extends TestHelper {
	protected static final int PORT = 18171;
	private PrintStream realOut = System.out;
	protected CommandServer server;
	protected final ByteArrayOutputStream output = new ByteArrayOutputStream();

	@Before
	public void before() throws IOException {
		server = new CommandServer(PORT);
		System.setOut(new PrintStream(output));
		AgentManager.removeAllAgents();
	}

	@After
	public void after() {
		server.stop();
		System.setOut(realOut);
	}
}