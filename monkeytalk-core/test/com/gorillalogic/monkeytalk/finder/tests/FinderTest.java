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
package com.gorillalogic.monkeytalk.finder.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.finder.Finder;

public class FinderTest {
	private Command cmd;

	@Test
	public void testNullEverything() {
		cmd = Finder.findCommandByComponentType(null, null);
		assertThat(cmd, nullValue());

		cmd = Finder.findCommandByName(null, null);
		assertThat(cmd, nullValue());
	}

	@Test
	public void testNullCommands() {
		cmd = Finder.findCommandByComponentType(null, "Button");
		assertThat(cmd, nullValue());

		cmd = Finder.findCommandByName(null, "button.click");
		assertThat(cmd, nullValue());
	}

	@Test
	public void testFind() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Button OK Click"));

		cmd = Finder.findCommandByComponentType(commands, "Button");
		assertThat(cmd.getCommand(), is("Button OK Click"));

		cmd = Finder.findCommandByName(commands, "button.click");
		assertThat(cmd.getCommand(), is("Button OK Click"));
	}

	@Test
	public void testFindMissing() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Button OK Click"));

		cmd = Finder.findCommandByComponentType(commands, "Foo");
		assertThat(cmd, nullValue());

		cmd = Finder.findCommandByName(commands, "foo.bar");
		assertThat(cmd, nullValue());
	}

	@Test
	public void testFindWithNull() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("Button OK Click"));

		cmd = Finder.findCommandByComponentType(commands, null);
		assertThat(cmd, nullValue());

		cmd = Finder.findCommandByName(commands, null);
		assertThat(cmd, nullValue());
	}

	@Test
	public void testFindInMultiple() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo bar baz"));
		commands.add(new Command("Button OK Click"));
		commands.add(new Command("Button OK Click 17 33"));
		commands.add(new Command("foo bar baz"));

		cmd = Finder.findCommandByComponentType(commands, "Button");
		assertThat(cmd.getCommand(), is("Button OK Click"));

		cmd = Finder.findCommandByName(commands, "button.click");
		assertThat(cmd.getCommand(), is("Button OK Click"));
	}

	@Test
	public void testFindMissingInMultiple() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo bar baz"));
		commands.add(new Command("Button OK Click"));
		commands.add(new Command("Button OK Click 17 33"));
		commands.add(new Command("foo bar baz"));

		cmd = Finder.findCommandByComponentType(commands, "Input");
		assertThat(cmd, nullValue());

		cmd = Finder.findCommandByName(commands, "input.entertext");
		assertThat(cmd, nullValue());
	}

	@Test
	public void testFindCommandsInMultiple() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo bar baz"));
		commands.add(new Command("Button OK Click"));
		commands.add(new Command("Button OK Click 17 33"));
		commands.add(new Command("Button OK Tap"));
		commands.add(new Command("foo bar baz"));

		List<Command> found = Finder.findCommandsByComponentType(commands, "Button");
		assertThat(found, notNullValue());
		assertThat(found.size(), is(3));
		assertThat(found.get(0).getCommand(), is("Button OK Click"));
		assertThat(found.get(1).getCommand(), is("Button OK Click 17 33"));
		assertThat(found.get(2).getCommand(), is("Button OK Tap"));

		found = Finder.findCommandsByName(commands, "button.click");
		assertThat(found, notNullValue());
		assertThat(found.size(), is(2));
		assertThat(found.get(0).getCommand(), is("Button OK Click"));
		assertThat(found.get(1).getCommand(), is("Button OK Click 17 33"));
	}

	@Test
	public void testFindMissingCommandsInMultiple() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo bar baz"));
		commands.add(new Command("Button OK Click"));
		commands.add(new Command("Button OK Click 17 33"));
		commands.add(new Command("Button OK Tap"));
		commands.add(new Command("foo bar baz"));

		List<Command> found = Finder.findCommandsByComponentType(commands, "Input");
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));

		found = Finder.findCommandsByName(commands, "input.entertext");
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));
	}

	@Test
	public void testFindCommandsInMultipleWithNull() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo bar baz"));
		commands.add(new Command("Button OK Click"));
		commands.add(new Command("Button OK Click 17 33"));
		commands.add(new Command("Button OK Tap"));
		commands.add(new Command("foo bar baz"));

		List<Command> found = Finder.findCommandsByComponentType(commands, null);
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));

		found = Finder.findCommandsByName(commands, null);
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));
	}

	@Test
	public void testFindCommandsInMultipleWithNullCommands() {
		List<Command> found = Finder.findCommandsByComponentType(null, "Input");
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));

		found = Finder.findCommandsByName(null, "input.entertext");
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));
	}

	@Test
	public void testFindCommandsInMultipleWithNullEverything() {
		List<Command> found = Finder.findCommandsByComponentType(null, null);
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));

		found = Finder.findCommandsByName(null, null);
		assertThat(found, notNullValue());
		assertThat(found.size(), is(0));
	}
	
	@Test
	public void testFindByComponentTypeWithIgnoreModifier() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo b a"));
		commands.add(new Command("bar a b"));
		commands.add(new Command("baz a b %ignore=true"));
		commands.add(new Command("foo c a"));
		commands.add(new Command("foo x y"));

		// foo
		List<Command> found = Finder.findCommandsByComponentType(commands, "foo");
		assertThat(found.size(), is(3));
		assertThat(found.get(0).getCommand(), is("foo b a"));
		assertThat(found.get(1).getCommand(), is("foo c a"));
		assertThat(found.get(2).getCommand(), is("foo x y"));
		
		Command cmd = Finder.findCommandByComponentType(commands, "foo");
		assertThat(cmd.getCommand(), is("foo b a"));
		
		// bar
		found = Finder.findCommandsByComponentType(commands, "bar");
		assertThat(found.size(), is(1));
		assertThat(found.get(0).getCommand(), is("bar a b"));
		
		cmd = Finder.findCommandByComponentType(commands, "bar");
		assertThat(cmd.getCommand(), is("bar a b"));
		
		// baz
		found = Finder.findCommandsByComponentType(commands, "baz");
		assertThat(found.size(), is(0));
		
		cmd = Finder.findCommandByComponentType(commands, "baz");
		assertThat(cmd, nullValue());
	}
	
	@Test
	public void testFindByNameWithIgnoreModifier() {
		List<Command> commands = new ArrayList<Command>();
		commands.add(new Command("foo b a"));
		commands.add(new Command("bar a b"));
		commands.add(new Command("baz a b %ignore=true"));
		commands.add(new Command("foo c a"));
		commands.add(new Command("foo x y"));
		
		// foo
		List<Command> found = Finder.findCommandsByName(commands, "foo.a");
		assertThat(found.size(), is(2));
		assertThat(found.get(0).getCommand(), is("foo b a"));
		assertThat(found.get(1).getCommand(), is("foo c a"));
		
		Command cmd = Finder.findCommandByName(commands, "foo.a");
		assertThat(cmd.getCommand(), is("foo b a"));
		
		// bar
		found = Finder.findCommandsByName(commands, "bar.b");
		assertThat(found.size(), is(1));
		assertThat(found.get(0).getCommand(), is("bar a b"));
		
		cmd = Finder.findCommandByName(commands, "bar.b");
		assertThat(cmd.getCommand(), is("bar a b"));
		
		// baz
		found = Finder.findCommandsByName(commands, "baz.b");
		assertThat(found.size(), is(0));
		
		cmd = Finder.findCommandByName(commands, "baz.b");
		assertThat(cmd, nullValue());
	}
}