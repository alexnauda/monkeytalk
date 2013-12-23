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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class CommandTest extends BaseCommandTest {
	
	@Test
	public void testSimpleCommand() {
		Command cmd = new Command("Button OK Click");
		
		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertCommand(cmd);
		
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testCommandWithArgs() {
		Command cmd = new Command("Button OK Click 17 33");

		assertThat(cmd.getCommand(), is("Button OK Click 17 33"));
		assertCommand(cmd);

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertNoModifiers(cmd);
	}

	@Test
	public void testCommandWithModifiers() {
		Command cmd = new Command("Button OK Click %foo=123 %bar=654");

		assertThat(cmd.getCommand(), is("Button OK Click %foo=123 %bar=654"));
		assertCommand(cmd);

		assertNoArgs(cmd);

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}

	@Test
	public void testCommandWithArgsAndModifiers() {
		Command cmd = new Command("Button OK Click 17 33 %foo=123 %bar=654");

		assertThat(cmd.getCommand(),
				is("Button OK Click 17 33 %foo=123 %bar=654"));
		assertCommand(cmd);

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}

	@Test
	public void testCommandWithQuotedArgsThatLookLikeModifiers() {
		Command cmd = new Command("Button OK Click \"%foo=123\" %bar=654");

		assertThat(cmd.getCommand(),
				is("Button OK Click \"%foo=123\" %bar=654"));
		assertCommand(cmd);

		assertThat(cmd.getArgs(), hasItems("%foo=123"));
		assertThat(cmd.getArgsAsString(), is("\"%foo=123\""));

		assertThat(cmd.getModifiers().values(), hasItems("654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("bar"));
		assertThat(cmd.getModifiersAsString(), is("%bar=654"));
	}
	
	@Test
	public void testCommandWithModifiersWithoutEqaulsThatAreArgs() {
		Command cmd = new Command("Button OK Click %foo %bar %foo=123 %bar=654");
		
		assertThat(cmd.getCommand(),
				is("Button OK Click %foo %bar %foo=123 %bar=654"));
		assertCommand(cmd);

		assertThat(cmd.getArgs(), hasItems("%foo", "%bar"));
		assertThat(cmd.getArgsAsString(), is("%foo %bar"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}
	
	@Test
	public void testCommandWithQuotedModifierValues() {
		Command cmd = new Command("Button OK Click %foo=\"some val\" %bar=\"other val\"");
		
		assertThat(cmd.getCommand(),
				is("Button OK Click %foo=\"some val\" %bar=\"other val\""));
		assertCommand(cmd);
		
		assertNoArgs(cmd);
		
		assertThat(cmd.getModifiers().values(), hasItems("some val", "other val"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=\"some val\" %bar=\"other val\""));
	}
	
	@Test
	public void testCommandWithNakedModifier() {
		Command cmd = new Command("Button OK Click %foo=");
		
		assertThat(cmd.getCommand(),
				is("Button OK Click %foo="));
		assertCommand(cmd);
		
		assertNoArgs(cmd);
		
		assertThat(cmd.getModifiers().size(), is(1));
		assertThat(cmd.getModifiers().get("foo"), nullValue());
		assertThat(cmd.getModifiers().keySet(), hasItems("foo"));
		assertThat(cmd.getModifiersAsString(), is("%foo="));
	}
	
	@Test
	public void testCommandWithNakedQuotedModifier() {
		Command cmd = new Command("Button OK Click %foo=\"\"");
		
		assertThat(cmd.getCommand(),
				is("Button OK Click %foo=\"\""));
		assertCommand(cmd);
		
		assertNoArgs(cmd);
		
		assertThat(cmd.getModifiers().size(), is(1));
		assertThat(cmd.getModifiers().get("foo"), is(""));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo"));
		assertThat(cmd.getModifiersAsString(), is("%foo=\"\""));
	}
	
	@Test
	public void testBlankMonkeyIdIsConvertedToStar() {
		Command cmd = new Command("Button", "", "Click", null, null);
		
		assertThat(cmd.getCommand(), is("Button * Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("Click"));
		
		assertNoArgsOrModifiers(cmd);
	}
	
	@Test
	public void testVarsDefineCommand() {
		Command cmd = new Command("Vars * Define foo=123 firstName=\"first name\" lastName=\"last name\"");

		assertThat(cmd.getCommand(), is("Vars * Define foo=123 firstName=\"first name\" lastName=\"last name\""));
		assertThat(cmd.getComponentType(), is("Vars"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("Define"));

		assertThat(cmd.getArgs().size(), is(3));
		assertThat(cmd.getArgs(), hasItems("foo=123", "firstName=\"first name\"", "lastName=\"last name\""));
		assertThat(cmd.getArgsAsString(), is("foo=123 firstName=\"first name\" lastName=\"last name\""));

		assertNoModifiers(cmd);
	}
	
	@Test
	public void testGetCommand() {
		Command cmd = new Command("Button OK Get foo .font.fontName");
		
		assertThat(cmd.getCommand(), is("Button OK Get foo .font.fontName"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Get"));
		
		assertThat(cmd.getArgs().size(), is(2));
		assertThat(cmd.getArgs(), hasItems("foo", ".font.fontName"));
		assertThat(cmd.getArgsAsString(), is("foo .font.fontName"));
		
		assertNoModifiers(cmd);
	}
	
	@Test
	public void testVerifyCommand() {
		Command cmd = new Command("Button OK Verify expected .someProp.someOtherProp \"failure message\"");
		
		assertThat(cmd.getCommand(), is("Button OK Verify expected .someProp.someOtherProp \"failure message\""));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Verify"));
		
		assertThat(cmd.getArgs().size(), is(3));
		assertThat(cmd.getArgs(), hasItems("expected", ".someProp.someOtherProp", "failure message"));
		assertThat(cmd.getArgsAsString(), is("expected .someProp.someOtherProp \"failure message\""));
		
		assertNoModifiers(cmd);
	}
	
	@Test
	public void testCommandBecomesCommentViaComponentType() {
		Command cmd = new Command("Button OK Click arg1 arg2 %foo=123 %bar=654");
		
		assertThat(cmd.getCommand(), is("Button OK Click arg1 arg2 %foo=123 %bar=654"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));
		

		assertThat(cmd.getArgs(), hasItems("arg1", "arg2"));
		assertThat(cmd.getArgsAsString(), is("arg1 arg2"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
		
		cmd.setComponentType(" # some comment ");
		assertThat(cmd.getCommand(), is("# some comment"));
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(true));
		
		assertNoArgsOrModifiers(cmd);
	}
}