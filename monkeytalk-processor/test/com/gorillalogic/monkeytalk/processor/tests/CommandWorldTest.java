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
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.File;

import org.junit.Test;

import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.api.meta.Action;

public class CommandWorldTest {

	@Test
	public void testDefaultConstructor() {
		CommandWorld world = new CommandWorld();
		assertThat(world.getRootDir(), nullValue());
		assertThat(world.toString(), containsString("Scripts: none"));
		assertThat(world.toString(), containsString("Suites: none"));
		assertThat(world.toString(), containsString("CustomCommands: none"));
		assertThat(world.toString(), containsString("Javascripts: none"));
		assertThat(world.toString(), containsString("Datafiles: none"));
	}

	@Test
	public void testConstructorWithNull() {
		CommandWorld world = new CommandWorld(null);
		assertThat(world.getRootDir(), nullValue());
	}

	@Test
	public void testConstructorWithMissing() {
		CommandWorld world = new CommandWorld(new File("missing"));
		assertThat(world.getRootDir(), nullValue());
	}

	@Test
	public void testConstructorWithFile() {
		File f = new File("resources/test/foo.mt");
		assertThat(f.exists(), is(true));

		CommandWorld world = new CommandWorld(f);
		assertThat(world.getRootDir(), nullValue());
	}

	@Test
	public void testConstructorWithDir() {
		CommandWorld world = new CommandWorld(new File("resources/test"));

		assertThat(
				world.toString(),
				containsString("Scripts: bar.mt, comp.action.mt, comp.foobar.mt, foo.mt"));
		assertThat(world.toString(), containsString("Suites: suite.mts"));
		assertThat(
				world.toString(),
				containsString("CustomCommands: comp.action.mt, comp.foobar.mt"));
		assertThat(world.toString(), containsString("Javascripts: foo.js"));
		assertThat(world.toString(), containsString("Datafiles: names.csv"));
	}

	@Test
	public void testFileExists() {
		CommandWorld world = new CommandWorld(new File("resources/test"));

		assertThat(world.fileExists("foo.mt"), is(true));
		assertThat(world.fileExists("missing.mt"), is(false));
		assertThat(world.fileExists(""), is(false));
		assertThat(world.fileExists(null), is(false));
	}
	
	@Test
	public void testHasJavascriptOverride() {
		CommandWorld world = new CommandWorld(new File("resources/test"));
		assertThat(world.hasJavascriptOverride("foo"), is(true));
		assertThat(world.hasJavascriptOverride("foo.mt"), is(false));
		assertThat(world.hasJavascriptOverride("foo.js"), is(true));
		assertThat(world.hasJavascriptOverride("missing.mt"), is(false));
		assertThat(world.hasJavascriptOverride("bar.mt"), is(false));
		assertThat(world.hasJavascriptOverride("bar.js"), is(false));
		assertThat(world.hasJavascriptOverride(null), is(false));
	}

	@Test
	public void testGetNullScript() {
		CommandWorld world = new CommandWorld(new File("resources/test"));
		assertThat(world.getScript(null), nullValue());
	}

	@Test
	public void testGetMissingScript() {
		CommandWorld world = new CommandWorld(new File("resources/test"));
		assertThat(world.getScript("missing.mt"), nullValue());
	}

	@Test
	public void testGetCustomCommand() {
		CommandWorld world = new CommandWorld(new File("resources/test"));
		assertThat(world.getScript("comp.action.mt"), notNullValue());
		assertThat(world.getScript("comp.action"), notNullValue());
		assertThat(world.getScript("Comp.Action.mt"), notNullValue());
		assertThat(world.getScript("Comp.Action"), notNullValue());
	}
	
	@Test
	public void testGetSuite() {
		CommandWorld world = new CommandWorld(new File("resources/test"));
		assertThat(world.getScript("suite.mts"), nullValue());
		assertThat(world.getSuite("suite.mts"), notNullValue());
	}

	@Test
	public void testAPIAction() {
		CommandWorld world = new CommandWorld(new File("resources/test"));

		Action foo = world.getAPIAction("foo.mt");
		assertThat(foo, notNullValue());
		assertThat(foo.getName(), is("foo.mt"));
		assertThat(foo.getArgs(), notNullValue());
		assertThat(foo.getArgs().size(), is(0));

		Action bar = world.getAPIAction("bar.mt");
		assertThat(bar, notNullValue());
		assertThat(bar.getName(), is("bar.mt"));
		assertThat(bar.getArgs(), notNullValue());
		assertThat(bar.getArgs().size(), is(1));
		assertThat(bar.getArgNames(), hasItems("baz"));

		Action compAction = world.getAPIAction("comp.action.mt");
		assertThat(compAction, notNullValue());
		assertThat(compAction.getArgs(), notNullValue());
		assertThat(compAction.getArgs().size(), is(2));
		assertThat(compAction.getArgNames(), hasItems("first", "last"));
		assertThat(compAction.getArgNamesAsString(), is("first last"));

		Action compFoobar = world.getAPIAction("comp.foobar.mt");
		assertThat(compFoobar, notNullValue());
		assertThat(compFoobar.getArgs(), notNullValue());
		assertThat(compFoobar.getArgs().size(), is(2));
		assertThat(compFoobar.getArgNames(), hasItems("foo", "bar"));
		assertThat(compFoobar.getArgNamesAsString(), is("foo bar"));
	}
}