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
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class QuotesTest extends BaseCommandTest {

	@Test
	public void testQuotedCommand() {
		Command cmd = new Command("\"Button\" \"OK\" \"Click\"");

		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.getCommandName(), is("button.click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testQuotedArg() {
		Command cmd = new Command("Button OK Click \"quoted arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted arg"));
		assertThat(cmd.getArgsAsString(), is("\"quoted arg\""));
	}

	@Test
	public void testQuotedArgThatDoesntNeedQuotes() {
		Command cmd = new Command("Button OK Click \"quoted-arg-that-doesn't-need-quotes\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted-arg-that-doesn't-need-quotes"));
		assertThat(cmd.getArgsAsString(), is("quoted-arg-that-doesn't-need-quotes"));
	}

	@Test
	public void testQuotedArgs() {
		Command cmd = new Command("Button OK Click \"quoted arg\" \"another arg\" \"third arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted arg", "another arg", "third arg"));
		assertThat(cmd.getArgsAsString(), is("\"quoted arg\" \"another arg\" \"third arg\""));
	}

	@Test
	public void testQuotedArgWithEscapedQuotes() {
		Command cmd = new Command("Button OK Click \"quoted \\\"and escaped\\\" arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted \\\"and escaped\\\" arg"));
		assertThat(cmd.getArgsAsString(), is("\"quoted \\\"and escaped\\\" arg\""));
	}

	@Test
	public void testQuotedArgWithSingleEscapedQuote() {
		Command cmd = new Command("Button OK Click \"quoted escaped\\\" arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted escaped\\\" arg"));
		assertThat(cmd.getArgsAsString(), is("\"quoted escaped\\\" arg\""));
	}

	@Test
	public void testQuotedArgWithSingleEscapedQuoteAtTheBeginning() {
		Command cmd = new Command("Button OK Click \"\\\"quoted escaped arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("\\\"quoted escaped arg"));
		assertThat(cmd.getArgsAsString(), is("\"\\\"quoted escaped arg\""));
	}

	@Test
	public void testQuotedArgWithSingleEscapedQuoteAtTheEnd() {
		Command cmd = new Command("Button OK Click \"quoted escaped arg\\\"\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted escaped arg\\\""));
		assertThat(cmd.getArgsAsString(), is("\"quoted escaped arg\\\"\""));
	}

	@Test
	public void testQuotedArgWithOnlySingleEscapedQuote() {
		Command cmd = new Command("Button OK Click \"\\\"\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("\\\""));
		assertThat(cmd.getArgsAsString(), is("\\\""));
	}

	@Test
	public void testEscapedQuote() {
		Command cmd = new Command("Button OK Click in\\\"middle");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("in\\\"middle"));
		assertThat(cmd.getArgsAsString(), is("in\\\"middle"));
	}

	@Test
	public void testEscapedQuoteAtTheBeginning() {
		Command cmd = new Command("Button OK Click \\\"beginning");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("\\\"beginning"));
		assertThat(cmd.getArgsAsString(), is("\\\"beginning"));
	}

	@Test
	public void testEscapedQuoteAtTheEnd() {
		Command cmd = new Command("Button OK Click end\\\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("end\\\""));
		assertThat(cmd.getArgsAsString(), is("end\\\""));
	}

	@Test
	public void testSingleEscapedQuote() {
		Command cmd = new Command("Button OK Click \\\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("\\\""));
		assertThat(cmd.getArgsAsString(), is("\\\""));
	}

	@Test
	public void testQuotedArgWithSingleQuotes() {
		Command cmd = new Command("Button OK Click \"quoted 'single quotes' arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted 'single quotes' arg"));
		assertThat(cmd.getArgsAsString(), is("\"quoted 'single quotes' arg\""));
	}

	@Test
	public void testQuotedArgWithSingleQuote() {
		Command cmd = new Command("Button OK Click \"quoted single's arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted single's arg"));
		assertThat(cmd.getArgsAsString(), is("\"quoted single's arg\""));
	}

	@Test
	public void testQuotedArgWithSingleQuoteAtTheBeginning() {
		Command cmd = new Command("Button OK Click \"'quoted single arg\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("'quoted single arg"));
		assertThat(cmd.getArgsAsString(), is("\"'quoted single arg\""));
	}

	@Test
	public void testQuotedArgWithSingleQuoteAtTheEnd() {
		Command cmd = new Command("Button OK Click \"quoted single arg'\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("quoted single arg'"));
		assertThat(cmd.getArgsAsString(), is("\"quoted single arg'\""));
	}

	@Test
	public void testCrazyEscapedQuotesAndSingleQuotes() {
		Command cmd = new Command("Button OK Click \\\"''\\\"'\\\"''''\\\"\\\"");

		assertCommand(cmd);
		assertNoModifiers(cmd);

		assertThat(cmd.getArgs(), hasItems("\\\"''\\\"'\\\"''''\\\"\\\""));
		assertThat(cmd.getArgsAsString(), is("\\\"''\\\"'\\\"''''\\\"\\\""));
	}
}