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
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class SetterTest extends BaseCommandTest {

	@Test
	public void testSetRequiredParts() {
		Command cmd = new Command();
		assertThat(cmd, notNullValue());

		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.isValid(), is(true));
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testSetNulls() {
		Command cmd = new Command();
		assertThat(cmd, notNullValue());
		assertThat(cmd.getCommand(), nullValue());
		assertThat(cmd.toString(), nullValue());
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));

		cmd.setComponentType(null);
		cmd.setMonkeyId(null);
		cmd.setAction(null);

		assertThat(cmd.getCommand(), nullValue());
		assertThat(cmd.toString(), nullValue());
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(false));
		assertThat(cmd.isValid(), is(false));
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testSetAndThenReset() {
		Command cmd = new Command();
		assertThat(cmd, notNullValue());

		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.isValid(), is(true));
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);

		cmd.setComponentType("Foo");
		assertThat(cmd.getComponentType(), is("Foo"));
		assertThat(cmd.getCommand(), is("Foo OK Click"));
	}

	@Test
	public void testSetMissingComponentType() {
		Command cmd = new Command();
		cmd.setComponentType("");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("* OK Click"));
	}

	@Test
	public void testSetStarComponentType() {
		Command cmd = new Command();
		cmd.setComponentType("*");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("* OK Click"));
	}

	@Test
	public void testSetQuotedComponentType() {
		Command cmd = new Command();
		cmd.setComponentType("\"Button\"");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button OK Click"));
	}

	@Test
	public void testSetQuotedComponentTypeWithSpaces() {
		Command cmd = new Command();
		cmd.setComponentType("\" This Has Spaces \"");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("ThisHasSpaces OK Click"));
	}

	@Test
	public void testSetMissingMonkeyId() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button * Click"));
	}

	@Test
	public void testSetStarMonkeyId() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("*");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button * Click"));
	}

	@Test
	public void testSetQuotedMonkeyId() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("\"OK\"");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button OK Click"));
	}

	@Test
	public void testSetQuotedMonkeyIdWithSpaces() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("\" This Has Spaces \"");
		cmd.setAction("Click");

		assertThat(cmd.getCommand(), is("Button \" This Has Spaces \" Click"));
	}

	@Test
	public void testSetMissingAction() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("");

		assertThat(cmd.getCommand(), is("Button OK *"));
	}

	@Test
	public void testSetStarAction() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("*");

		assertThat(cmd.getCommand(), is("Button OK *"));
	}

	@Test
	public void testSetQuotedAction() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("\"Click\"");

		assertThat(cmd.getCommand(), is("Button OK Click"));
	}

	@Test
	public void testSetQuotedActionWithSpaces() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("\" This Has Spaces \"");

		assertThat(cmd.getCommand(), is("Button OK ThisHasSpaces"));
	}

	@Test
	public void testSetArgs() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("17 33");

		assertThat(cmd.getCommand(), is("Button OK Click 17 33"));
		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));
		assertCommand(cmd);
		assertNoModifiers(cmd);
	}

	@Test
	public void testSetNullArgs() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers(null);

		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testSetModifiers() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("%foo=123 %bar=654");

		assertThat(cmd.getCommand(), is("Button OK Click %foo=123 %bar=654"));
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
		assertCommand(cmd);
		assertNoArgs(cmd);
	}

	@Test
	public void testSetArgsAndModifiers() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("17 33 %foo=123 %bar=654");

		assertThat(cmd.getCommand(), is("Button OK Click 17 33 %foo=123 %bar=654"));
		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
		assertCommand(cmd);
	}

	@Test
	public void testSetQuotedArgs() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("\"some arg\" \"other arg\"");

		assertThat(cmd.getCommand(), is("Button OK Click \"some arg\" \"other arg\""));
		assertThat(cmd.getArgs(), hasItems("some arg", "other arg"));
		assertThat(cmd.getArgsAsString(), is("\"some arg\" \"other arg\""));
		assertCommand(cmd);
		assertNoModifiers(cmd);
	}

	@Test
	public void testSetQuotedArgValues() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("arg=\"some val\" arg2=\"other val\"");

		assertThat(cmd.getCommand(), is("Button OK Click arg=\"some val\" arg2=\"other val\""));
		assertThat(cmd.getArgs(), hasItems("arg=\"some val\"", "arg2=\"other val\""));
		assertThat(cmd.getArgsAsString(), is("arg=\"some val\" arg2=\"other val\""));
		assertCommand(cmd);
		assertNoModifiers(cmd);
	}

	@Test
	public void testSetQuotedModifierValues() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("%foo=\"some val\" %bar=\"other val\"");

		assertThat(cmd.getCommand(), is("Button OK Click %foo=\"some val\" %bar=\"other val\""));
		assertThat(cmd.getModifiers().values(), hasItems("some val", "other val"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=\"some val\" %bar=\"other val\""));
		assertCommand(cmd);
		assertNoArgs(cmd);
	}

	@Test
	public void testSetQuotedArgWithEscapedQuotes() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("\"Joe \\\"The Magnificent\\\" Smith\"");

		assertThat(cmd.getCommand(), is("Button OK Click \"Joe \\\"The Magnificent\\\" Smith\""));
		assertThat(cmd.getArgs(), hasItems("Joe \\\"The Magnificent\\\" Smith"));
		assertThat(cmd.getArgsAsString(), is("\"Joe \\\"The Magnificent\\\" Smith\""));
		assertCommand(cmd);
		assertNoModifiers(cmd);
	}

	@Test
	public void testSetModifiersByKeyAndValue() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setModifier("foo", "some val");
		cmd.setModifier("bar", "other val");

		assertThat(cmd.getCommand(), is("Button OK Click %foo=\"some val\" %bar=\"other val\""));
		assertThat(cmd.getModifiers().values(), hasItems("some val", "other val"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=\"some val\" %bar=\"other val\""));
		assertCommand(cmd);
		assertNoArgs(cmd);
	}

	@Test
	public void testSetModifiersByStringAndKeyAndValue() {
		Command cmd = new Command();
		cmd.setComponentType("Button");
		cmd.setMonkeyId("OK");
		cmd.setAction("Click");
		cmd.setArgsAndModifiers("%foo=123 %bar=654");
		cmd.setModifier("foo", "some val");
		cmd.setModifier("baz", "other val");

		assertThat(cmd.getCommand(), containsString("Button OK Click"));
		assertThat(cmd.getCommand(), containsString("%foo=\"some val\""));
		assertThat(cmd.getCommand(), containsString("%bar=654"));
		assertThat(cmd.getCommand(), containsString("%baz=\"other val\""));
		assertThat(cmd.getModifiers().values(), hasItems("some val", "654", "other val"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar", "baz"));
		assertCommand(cmd);
		assertNoArgs(cmd);
	}

	@Test
	public void testDeleteModifiersByKey() {
		Command cmd = new Command("Button OK Click %foo=123 %bar=654");
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertCommand(cmd);
		assertNoArgs(cmd);

		cmd.setModifier("foo", null);
		assertThat(cmd.getModifiers().values(), hasItems("654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("bar"));
		assertCommand(cmd);
		assertNoArgs(cmd);

		cmd.setModifier("bar", null);
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testDeleteModifiersByNullKey() {
		Command cmd = new Command("Button OK Click %foo=123 %bar=654");
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));

		cmd.setModifier(null, null);
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
	}

	@Test
	public void testDeleteModifiersByMissingKey() {
		Command cmd = new Command("Button OK Click %foo=123 %bar=654");
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));

		cmd.setModifier("missing", null);
		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
	}
}