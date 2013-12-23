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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;

public class ScriptProcessorJavascriptIT extends BaseIntegrationTest {
	private static final String HOST = "localhost";
	private static final int PORT = 18138;
	private ScriptProcessor processor;
	private ByteArrayOutputStream out;

	@Before
	public void before() throws IOException {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		processor = new ScriptProcessor(HOST, PORT, (File) null);
	}

	@After
	public void after() {
		out = null;
		processor = null;
	}

	@Test
	public void testRunJavascript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nInput name EnterText \"Bo Bo\"", dir);

		genAPIAndLib(dir);
		genJS(foo);

		processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("foo.js");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button FOO tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));
	}

	@Test
	public void testRunJavascriptWithArgs() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define name\nInput name EnterText ${name}", dir);
		tempScript("driver.mt", "Script foo.js Run \"Bo Bo\"\nScript foo.js Run Héìíô", dir);
		tempScript("data.csv", "name\nJohn\n\"Bo Bo\"\nHéìíô", dir);

		genAPIAndLib(dir);
		genJS(foo);

		processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("driver.mt");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Input name enterText \"Bo Bo\""));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText Héìíô"));
	}

	@Test
	public void testRunJavascriptDataDriven() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define name\nInput name EnterText ${name}", dir);
		tempScript("driver.mt", "Script foo.js RunWith data.csv", dir);
		tempScript("data.csv", "name\nJohn\n\"Bo Bo\"\nHéìíô", dir);

		genAPIAndLib(dir);
		genJS(foo);

		processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("driver.mt");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Input name enterText John"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(), is("Input name enterText Héìíô"));
	}

	@Test
	public void testRunJavascriptDataDrivenOverride() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define name\nInput name EnterText ${name}", dir);
		tempScript("driver.mt", "Script foo RunWith data.csv", dir);
		tempScript("data.csv", "name\nJohn\n\"Bo Bo\"\nHéìíô", dir);

		genAPIAndLib(dir);
		genJS(foo);

		processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new CommandServer(PORT);
		PlaybackResult result = processor.runScript("driver.mt");
		server.stop();
		
		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(3));
		assertThat(server.getCommands().get(0).getCommand(), is("Input name enterText John"));
		assertThat(server.getCommands().get(1).getCommand(), is("Input name enterText \"Bo Bo\""));
		assertThat(server.getCommands().get(2).getCommand(), is("Input name enterText Héìíô"));
	}
}