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
package com.gorillalogic.monkeytalk.processor.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.agents.MTAgent;
import com.gorillalogic.monkeytalk.processor.BaseProcessor;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class BaseProcessorTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18024;

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testConstructorWithNullFile() {
		BaseProcessor processor = new BaseProcessor((File) null, null);
		assertThat(processor, notNullValue());
		assertThat(processor.getWorld(), notNullValue());
		assertThat(processor.getWorld().getRootDir(), nullValue());
		assertThat(processor.toString(), is("RootDir: null\n" + "Scripts: none\n"
				+ "Suites: none\n" + "CustomCommands: none\n" + "Javascripts: none\n"
				+ "Datafiles: none\n"));
	}

	@Test
	public void testConstructorWithNullWorld() {
		BaseProcessor processor = new BaseProcessor(new CommandWorld(null), null);
		assertThat(processor, notNullValue());
		assertThat(processor.getWorld(), notNullValue());
		assertThat(processor.getWorld().getRootDir(), nullValue());
		assertThat(processor.toString(), is("RootDir: null\n" + "Scripts: none\n"
				+ "Suites: none\n" + "CustomCommands: none\n" + "Javascripts: none\n"
				+ "Datafiles: none\n"));
	}

	@Test
	public void testConstructorWithNullWorldAndNullAgent() {
		BaseProcessor processor = new BaseProcessor(new CommandWorld(null), null);
		assertThat(processor, notNullValue());
		assertThat(processor.getWorld(), notNullValue());
		assertThat(processor.getWorld().getRootDir(), nullValue());
		assertThat(processor.toString(), is("RootDir: null\n" + "Scripts: none\n"
				+ "Suites: none\n" + "CustomCommands: none\n" + "Javascripts: none\n"
				+ "Datafiles: none\n"));
	}

	@Test
	public void testConstructorWithProcessor() {
		BaseProcessor processor1 = new BaseProcessor((File) null, new MTAgent(HOST, PORT));
		BaseProcessor processor = new BaseProcessor(processor1);
		assertThat(processor, notNullValue());
		assertThat(processor.getWorld(), notNullValue());
		assertThat(processor.getWorld().getRootDir(), nullValue());
		assertThat(processor.toString(), is("RootDir: null\n" + "Scripts: none\n"
				+ "Suites: none\n" + "CustomCommands: none\n" + "Javascripts: none\n"
				+ "Datafiles: none\n"));
	}

	@Test
	public void testConstructorWithDir() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "", dir);
		tempScript("foo.js", "", dir);
		tempScript("bar.mt", "", dir);
		tempScript("mysuite.mts", "", dir);
		tempScript("data.csv", "", dir);

		BaseProcessor processor = new BaseProcessor(dir, new MTAgent(HOST, PORT));
		assertThat(processor, notNullValue());
		assertThat(processor.getWorld(), notNullValue());
		assertThat(processor.getWorld().getRootDir(), is(dir));
		assertThat(processor.toString(), containsString("RootDir: " + dir.getAbsolutePath()));
		assertThat(processor.toString(), containsString("Scripts: bar.mt, foo.mt"));
		assertThat(processor.toString(), containsString("Suites: mysuite.mts"));
		assertThat(processor.toString(), containsString("Javascripts: foo.js"));
		assertThat(processor.toString(), containsString("Datafiles: data.csv"));
	}
}