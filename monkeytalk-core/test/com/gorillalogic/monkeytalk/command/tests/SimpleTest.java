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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class SimpleTest extends BaseCommandTest {

	@Test
	public void testDefaultConstructor() {
		Command cmd = new Command();

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), nullValue());
		assertThat(cmd.getCommandName(), is("null.null"));
		assertThat(cmd.toString(), nullValue());
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testConstructorWithNullCommand() {
		Command cmd = new Command((String) null);

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), nullValue());
		assertThat(cmd.getCommandName(), is("null.null"));
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testSimpleCommand() {
		Command cmd = new Command("Button OK Click");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.getCommandName(), is("button.click"));
		assertThat(cmd.toString(), is("Button OK Click"));
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testSimpleCommandByParts() {
		Command cmd = new Command("Button", "OK", "Click", null, null);

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.toString(), is("Button OK Click"));
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testComment() {
		Command cmd = new Command("# some comment");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("# some comment"));
		assertThat(cmd.toString(), is("# some comment"));
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(true));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testCommentByParts() {
		Command cmd = new Command("# some comment", "id", "action", null, null);

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("# some comment"));
		assertThat(cmd.toString(), is("# some comment"));
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(true));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testWhitespaceTrimmingOfCommand() {
		Command cmd = new Command("   Button OK Click       ");

		assertThat(cmd, notNullValue());
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testWhitespaceTrimmingOfComment() {
		Command cmd = new Command("  # some  whitespace   comment        ");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("# some  whitespace   comment"));
		assertThat(cmd.isComment(), is(true));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testBadCommand() {
		Command cmd = new Command("");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), nullValue());
		assertThat(cmd.toString(), nullValue());
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testOnlyComponentType() {
		Command cmd = new Command("Button");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button"));
		assertThat(cmd.toString(), is("Button"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testMissingAction() {
		Command cmd = new Command("Button OK");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("Button OK"));
		assertThat(cmd.toString(), is("Button OK"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testAllStars() {
		Command cmd = new Command("* * *");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("* * *"));
		assertThat(cmd.getCommandName(), is("*.*"));
		assertThat(cmd.toString(), is("* * *"));
		assertThat(cmd.getComponentType(), is("*"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("*"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testEmptyParts() {
		Command cmd = new Command("\"\" \"\" \"\"");

		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), is("* * *"));
		assertThat(cmd.getCommandName(), is("*.*"));
		assertThat(cmd.toString(), is("* * *"));
		assertThat(cmd.getComponentType(), is("*"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("*"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testIgnore() {
		assertThat(new Command("Button OK Tap %ignore").isIgnored(), is(false));
		assertThat(new Command("Button OK Tap %ignore=true").isIgnored(), is(true));
		assertThat(new Command("Button OK Tap %IGNORE=true").isIgnored(), is(true));
		assertThat(new Command("Button OK Tap %ignore=\"true\"").isIgnored(), is(true));
		assertThat(new Command("Button OK Tap %ignore=\" true \"").isIgnored(), is(false));
		assertThat(new Command("Button OK Tap %ignore=\" true \"").isIgnored("true"), is(true));
		assertThat(new Command("Button OK Tap %ignore=foo").isIgnored("foo"), is(true));
		assertThat(new Command("Button OK Tap %ignore=foo").isIgnored("bar"), is(false));
		assertThat(new Command("Button OK Tap %ignore=foo").isIgnored("oo"), is(true));
		assertThat(new Command("Button OK Tap %ignore=bar,baz,foo").isIgnored("foo"), is(true));
	}
}