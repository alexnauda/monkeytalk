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

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class JsonInputTest extends BaseCommandTest {

	@Test
	public void testJsonInput() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "OK");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertCommand(cmd);
		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithArgs() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "OK");
		json.put("action", "Click");
		json.put("args", new JSONArray(Arrays.asList("17", "33")));

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button OK Click 17 33"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertNoModifiers(cmd);
	}

	@Test
	public void testJsonInputWithMods() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "OK");
		json.put("action", "Click");
		json.put("modifiers", new JSONObject("{foo:\"123\",bar:\"654\"}"));

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button OK Click %foo=123 %bar=654"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgs(cmd);

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}

	@Test
	public void testJsonInputWithArgsAndMods() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "OK");
		json.put("action", "Click");
		json.put("args", new JSONArray(Arrays.asList("17", "33")));
		json.put("modifiers", new JSONObject("{foo:\"123\",bar:\"654\"}"));

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button OK Click 17 33 %foo=123 %bar=654"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));
	}

	@Test
	public void testJsonInputWithQuotedMonkeyId() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "this needs to be quoted");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button \"this needs to be quoted\" Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("this needs to be quoted"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithQuotedArgs() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "OK");
		json.put("action", "Click");
		json.put("args", new JSONArray(Arrays.asList("arg", "some arg", "third arg")));

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button OK Click arg \"some arg\" \"third arg\""));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertThat(cmd.getArgs(), hasItems("arg", "some arg", "third arg"));
		assertThat(cmd.getArgsAsString(), is("arg \"some arg\" \"third arg\""));

		assertNoModifiers(cmd);
	}

	@Test
	public void testJsonInputWithQuotedAndEscapedArgs() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "OK");
		json.put("action", "Click");
		json.put(
				"args",
				new JSONArray(Arrays.asList("some \\\"escaped\\\" arg", "\\\"beginning escape",
						"end escape\\\"")));

		Command cmd = new Command(json);
		assertThat(
				cmd.getCommand(),
				is("Button OK Click \"some \\\"escaped\\\" arg\" \"\\\"beginning escape\" \"end escape\\\"\""));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertThat(cmd.getArgs(),
				hasItems("some \\\"escaped\\\" arg", "\\\"beginning escape", "end escape\\\""));
		assertThat(cmd.getArgsAsString(),
				is("\"some \\\"escaped\\\" arg\" \"\\\"beginning escape\" \"end escape\\\"\""));

		assertNoModifiers(cmd);
	}

	@Test
	public void testJsonInputWithNullComponentType() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", (String) null);
		json.put("monkeyId", "OK");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("* OK Click"));
		assertThat(cmd.getComponentType(), is("*"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithMissingComponentType() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("monkeyId", "OK");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("* OK Click"));
		assertThat(cmd.getComponentType(), is("*"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithEmptyComponentType() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "");
		json.put("monkeyId", "OK");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("* OK Click"));
		assertThat(cmd.getComponentType(), is("*"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithCommentAsComponentType() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "# some comment");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("# some comment"));
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isComment(), is(true));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithNullMonkeyId() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", (String) null);
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button * Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithMissingMonkeyId() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button * Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithEmptyMonkeyId() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button * Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("*"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithExtendedLatin() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "Héìíô");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button Héìíô Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("Héìíô"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithExtendedLatinAsUnicode() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "H\u00e9\u00ec\u00ed\u00f4");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button Héìíô Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("Héìíô"));
		assertThat(cmd.getMonkeyId(), is("H\u00e9\u00ec\u00ed\u00f4"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}

	@Test
	public void testJsonInputWithUTF8() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("componentType", "Button");
		json.put("monkeyId", "\u21D0\u21D1\u21DD\u21DC");
		json.put("action", "Click");

		Command cmd = new Command(json);
		assertThat(cmd.getCommand(), is("Button \u21D0\u21D1\u21DD\u21DC Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("\u21D0\u21D1\u21DD\u21DC"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.isComment(), is(false));

		assertNoArgsOrModifiers(cmd);
	}
}