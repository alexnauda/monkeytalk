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
package com.gorillalogic.monkeytalk.api.impl.tests.integration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.SuiteProcessor;

public class SuiteProcessorJavascriptIT extends BaseIntegrationTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18139;
	private SuiteProcessor processor;

	@Before
	public void before() {
		processor = new SuiteProcessor(HOST, PORT, (File) null);
	}

	@After
	public void after() {
		processor = null;
	}

	@Test
	public void testJavascriptTest() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nInput name EnterText \"Bo Bo\"", dir);
		tempScript("mysuite.mts", "Test foo.js Run", dir);

		genAPIAndLib(dir);
		genJS(foo);

		processor = new SuiteProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));
	}
	
	@Test
	public void testJavascriptTestWithArgs() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define name\nInput name EnterText FOO${name}", dir);
		tempScript("mysuite.mts", "Test foo.js Run \"Bo Bo\"", dir);
		
		genAPIAndLib(dir);
		genJS(foo);
		
		processor = new SuiteProcessor(HOST, PORT, dir);
		
		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		
		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Input name enterText \"FOOBo Bo\""));
	}
	
	@Test
	public void testJavascriptTestWithDataDrive() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define x y\n" + "Input FOO EnterText \"${x} ${y}\"", dir);
		tempScript("mysuite.mts", "Test foo.js RunWith data.csv", dir);
		tempScript("data.csv", "x,y\n1234,5678\nJohn,\"Bo Bo\"\n-4.135,-104.55513", dir);
		
		genAPIAndLib(dir);
		genJS(foo);
		
		processor = new SuiteProcessor(HOST, PORT, dir);
		
		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		
		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input FOO enterText \"1234 5678\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input FOO enterText \"John Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Input FOO enterText \"-4.135 -104.55513\""));
	}
	
	@Test
	public void testJavascriptOverrideTestWithDataDrive() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define x y\n" + "Input FOO EnterText \"${x} ${y}\"", dir);
		tempScript("mysuite.mts", "Test foo RunWith data.csv", dir);
		tempScript("data.csv", "x,y\n1234,5678\nJohn,\"Bo Bo\"\n-4.135,-104.55513", dir);
		
		genAPIAndLib(dir);
		genJS(foo);
		
		processor = new SuiteProcessor(HOST, PORT, dir);
		
		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		
		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(),
				is("Input FOO enterText \"1234 5678\""));
		assertThat(server.getCommands().get(1).getCommand(),
				is("Input FOO enterText \"John Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(),
				is("Input FOO enterText \"-4.135 -104.55513\""));
	}
	
	@Test
	public void testJavascriptSetupWithArgs() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define name\nInput name EnterText FOO${name}", dir);
		File up = tempScript("up.mt", "Vars * Define name\nInput name EnterText UP${name}", dir);
		tempScript("mysuite.mts", "Test foo Run John\nTest foo Run \"Bo Bo\"\nSetup up.js Run \"some setup\"", dir);
		
		genAPIAndLib(dir);
		genJS(up);
		
		processor = new SuiteProcessor(HOST, PORT, dir);
		
		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		
		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Input name enterText \"UPsome setup\""));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name EnterText FOOJohn"));
		assertThat(server.getCommands().get(2).getCommand(), is("Input name enterText \"UPsome setup\""));
		assertThat(server.getCommands().get(3).getCommand(), is("Input name EnterText \"FOOBo Bo\""));
	}
	
	@Test
	public void testJavascriptTeardownWithArgs() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define name\nInput name EnterText FOO${name}", dir);
		File down = tempScript("down.mt", "Vars * Define name\nInput name EnterText DOWN${name}", dir);
		tempScript("mysuite.mts", "Test foo Run John\nTest foo Run \"Bo Bo\"\nTeardown down.js Run \"some teardown\"", dir);
		
		genAPIAndLib(dir);
		genJS(down);
		
		processor = new SuiteProcessor(HOST, PORT, dir);
		
		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runSuite("mysuite.mts");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));
		
		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(4));
		assertThat(server.getCommands().get(0).getCommand(), is("Input name EnterText FOOJohn"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"DOWNsome teardown\""));
		assertThat(server.getCommands().get(2).getCommand(), is("Input name EnterText \"FOOBo Bo\""));
		assertThat(server.getCommands().get(3).getCommand(), is("Input name enterText \"DOWNsome teardown\""));
	}
}