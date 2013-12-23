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
package com.gorillalogic.monkeytalk.parser.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.command.tests.BaseCommandTest;
import com.gorillalogic.monkeytalk.parser.MonkeyTalkParser;

public class FileParserTest extends BaseCommandTest {

	private static final String contents = "# this is a comment!\n"
			+ "\n\n\n\n\n"
			+ "Button OK Click\n"
			+ "Button OK Click 17 33\n"
			+ "Button OK Click arg \"some arg\" \"third arg\"\n"
			+ "Button OK Click %foo=123 %bar=654\n"
			+ "Button OK Click 17 33 %foo=123 %bar=654\n"
			+ "Button OK Click \"%foo=123\" %bar=654";

	private static List<Command> commands;

	@BeforeClass
	public static void beforeClass() {
		try {
			File tmp = File.createTempFile("script", ".mt");
			tmp.deleteOnExit();

			// write data
			BufferedWriter out = new BufferedWriter(
					new FileWriter(tmp));
			out.write(contents);
			out.close();
			
			commands = MonkeyTalkParser.parseFile(tmp, true, true);
			
			assertThat(commands, notNullValue());
			assertThat(commands.size(), is(6));
		} catch (IOException ex) {
			fail("failed to parse script.mt file");
		}
	}
	
	@Test
	public void testNullFile() {
		List<Command> commands = MonkeyTalkParser.parseFile(null);
		assertThat(commands, nullValue());
	}
	
	@Test
	public void testMissingFile() {
		List<Command> commands = MonkeyTalkParser.parseFile(new File("missing"));
		assertThat(commands, nullValue());
	}
	
	@Test
	public void testEmpty() {
		try {
			File tmp = File.createTempFile("empty", ".mt");
			tmp.deleteOnExit();
			
			// write data
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write("");
			out.close();
			
			List<Command> commands = MonkeyTalkParser.parseFile(tmp);
			
			assertThat(commands, notNullValue());
			assertThat(commands.size(), is(0));
		} catch (IOException ex) {
			fail("failed parsing empty file");
		}
	}
	
	@Test
	public void testCommand() {
		Command cmd = commands.get(0);
		
		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertButtonOkClick(cmd);
		
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testCommandWithArgs() {
		Command cmd = commands.get(1);

		assertThat(cmd.getCommand(), is("Button OK Click 17 33"));
		assertButtonOkClick(cmd);

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertNoModifiers(cmd);
	}
	
	@Test
	public void testCommandWithQuotedArgs() {
		Command cmd = commands.get(2);
		
		assertThat(cmd.getCommand(), is("Button OK Click arg \"some arg\" \"third arg\""));
		assertButtonOkClick(cmd);
		
		assertThat(cmd.getArgs(), hasItems("arg", "some arg", "third arg"));
		assertThat(cmd.getArgsAsString(), is("arg \"some arg\" \"third arg\""));
		
		assertNoModifiers(cmd);
	}

	@Test
	public void testCommandWithModifiers() {
		Command cmd = commands.get(3);

		assertThat(cmd.getCommand(), is("Button OK Click %foo=123 %bar=654"));
		assertButtonOkClick(cmd);

		assertNoArgs(cmd);

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}

	@Test
	public void testCommandWithArgsAndModifiers() {
		Command cmd = commands.get(4);

		assertThat(cmd.getCommand(),
				is("Button OK Click 17 33 %foo=123 %bar=654"));
		assertButtonOkClick(cmd);

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}

	@Test
	public void testCommandWithQuotedArgsThatLookLikeModifiers() {
		Command cmd = commands.get(5);

		assertThat(cmd.getCommand(),
				is("Button OK Click \"%foo=123\" %bar=654"));
		assertButtonOkClick(cmd);

		assertThat(cmd.getArgs(), hasItems("%foo=123"));
		assertThat(cmd.getArgsAsString(), is("\"%foo=123\""));

		assertThat(cmd.getModifiers().values(), hasItems("654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("bar"));
		assertThat(cmd.getModifiersAsString(), is("%bar=654"));
	}
	
	private void assertButtonOkClick(Command cmd) {
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));
	}
}