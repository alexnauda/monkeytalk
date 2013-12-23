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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class EscapingTest extends BaseCommandTest {

	@Test
	public void testArgWithSpace() {
		Command cmd = new Command("Button OK Click \"some arg\"");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click \"some arg\""));
		assertThat(cmd.toString(), is("Button OK Click \"some arg\""));
		assertCommand(cmd);

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("some arg"));
		assertThat(cmd.getArgsAsString(), is("\"some arg\""));

		assertNoModifiers(cmd);
	}

	@Test
	public void testArgWithRealTab() {
		Command cmd = new Command("Button OK Click \"some\targ\"");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click \"some\targ\""));
		assertThat(cmd.toString(), is("Button OK Click \"some\targ\""));
		assertCommand(cmd);

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("some\targ"));
		assertThat(cmd.getArgsAsString(), is("\"some\targ\""));

		assertNoModifiers(cmd);
	}

	@Test
	public void testArgWithEscapedTab() {
		Command cmd = new Command("Button OK Click some\\targ");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click some\\targ"));
		assertThat(cmd.toString(), is("Button OK Click some\\targ"));
		assertCommand(cmd);

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("some\\targ"));
		assertThat(cmd.getArgsAsString(), is("some\\targ"));

		assertNoModifiers(cmd);
	}
	
	@Test
	public void testArgWithQuotedEscapedTab() {
		Command cmd = new Command("Button OK Click \"some\\targ\"");
		
		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click some\\targ"));
		assertThat(cmd.toString(), is("Button OK Click some\\targ"));
		assertCommand(cmd);
		
		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("some\\targ"));
		assertThat(cmd.getArgsAsString(), is("some\\targ"));
		
		assertNoModifiers(cmd);
	}

	@Test
	public void testArgWithRealReturn() {
		Command cmd = new Command("Button OK Click \"some\narg\"");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click \\\"some\\narg\\\""));
		assertThat(cmd.toString(), is("Button OK Click \\\"some\\narg\\\""));
		assertCommand(cmd);

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("\"some\narg\""));
		assertThat(cmd.getArgsAsString(), is("\\\"some\\narg\\\""));

		assertNoModifiers(cmd);
	}

	@Test
	public void testArgWithEscapedReturn() {
		Command cmd = new Command("Button OK Click some\\narg");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click some\\narg"));
		assertThat(cmd.toString(), is("Button OK Click some\\narg"));
		assertCommand(cmd);

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("some\\narg"));
		assertThat(cmd.getArgsAsString(), is("some\\narg"));

		assertNoModifiers(cmd);
	}
	
	@Test
	public void testArgWithQuotedEscapedReturn() {
		Command cmd = new Command("Button OK Click \"some\\narg\"");
		
		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click some\\narg"));
		assertThat(cmd.toString(), is("Button OK Click some\\narg"));
		assertCommand(cmd);
		
		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("some\\narg"));
		assertThat(cmd.getArgsAsString(), is("some\\narg"));
		
		assertNoModifiers(cmd);
	}
	
	@Test
	public void testEscapingRegex() {
		Command cmd = new Command("Label * VerifyRegex \"\\w+ \\w*\" prop");
		
		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Label * VerifyRegex \"\\w+ \\w*\" prop"));
		assertThat(cmd.toString(), is("Label * VerifyRegex \"\\w+ \\w*\" prop"));
		
		assertThat(cmd.getArgs().size(), is(2));
		assertThat(cmd.getArgs(), hasItems("\\w+ \\w*", "prop"));
		assertThat(cmd.getArgsAsString(), is("\"\\w+ \\w*\" prop"));
		
		assertNoModifiers(cmd);
	}
}