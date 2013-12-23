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

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class JsonOutputTest extends BaseCommandTest {

	@Test
	public void testCommandJson() throws JSONException {
		Command cmd = new Command("Button OK Click");

		assertThat(cmd.getCommand(), is("Button OK Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("OK"));
		assertThat(json.getString("action"), is("Click"));

		assertThat(json.getJSONArray("args"), notNullValue());
		assertThat(json.getJSONArray("args").length(), is(0));

		JSONObject mods = json.getJSONObject("modifiers");
		assertThat(mods, notNullValue());
		assertThat(mods.length(), is(2));
		assertThat(mods.getInt("timeout"), is(Command.DEFAULT_TIMEOUT));
		assertThat(mods.getInt("thinktime"), is(Command.DEFAULT_THINKTIME));
	}

	@Test
	public void testCommandJsonWithArgs() throws JSONException {
		Command cmd = new Command("Button OK Click 17 33");

		assertThat(cmd.getCommand(), is("Button OK Click 17 33"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("OK"));
		assertThat(json.getString("action"), is("Click"));

		assertThat(json.getJSONArray("args"), notNullValue());
		assertThat(json.getJSONArray("args").length(), is(2));

		assertThat(json.getJSONArray("args").getString(0), is("17"));
		assertThat(json.getJSONArray("args").getInt(0), is(17));
		assertThat(json.getJSONArray("args").getString(1), is("33"));
		assertThat(json.getJSONArray("args").getInt(1), is(33));

		JSONObject mods = json.getJSONObject("modifiers");
		assertThat(mods, notNullValue());
		assertThat(mods.length(), is(2));
		assertThat(mods.getInt("timeout"), is(Command.DEFAULT_TIMEOUT));
		assertThat(mods.getInt("thinktime"), is(Command.DEFAULT_THINKTIME));
	}

	@Test
	public void testCommandJsonWithModifiers() throws JSONException {
		Command cmd = new Command("Button OK Click %foo=123 %bar=654");

		assertThat(cmd.getCommand(), is("Button OK Click %foo=123 %bar=654"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("OK"));
		assertThat(json.getString("action"), is("Click"));

		assertThat(json.getJSONArray("args"), notNullValue());
		assertThat(json.getJSONArray("args").length(), is(0));

		JSONObject mods = json.getJSONObject("modifiers");
		assertThat(mods, notNullValue());
		assertThat(mods.length(), is(4));
		assertThat(mods.getString("foo"), is("123"));
		assertThat(mods.getInt("foo"), is(123));
		assertThat(mods.getString("bar"), is("654"));
		assertThat(mods.getInt("bar"), is(654));
		assertThat(mods.getInt("timeout"), is(Command.DEFAULT_TIMEOUT));
		assertThat(mods.getInt("thinktime"), is(Command.DEFAULT_THINKTIME));
	}

	@Test
	public void testCommandJsonWithArgsAndModifiers() throws JSONException {
		Command cmd = new Command("Button OK Click 17 33 %foo=123 %bar=654");

		assertThat(cmd.getCommand(), is("Button OK Click 17 33 %foo=123 %bar=654"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), hasItems("17", "33"));
		assertThat(cmd.getArgsAsString(), is("17 33"));

		assertThat(cmd.getModifiers().values(), hasItems("123", "654"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=654"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("OK"));
		assertThat(json.getString("action"), is("Click"));

		assertThat(json.getJSONArray("args"), notNullValue());
		assertThat(json.getJSONArray("args").length(), is(2));

		assertThat(json.getJSONArray("args").getString(0), is("17"));
		assertThat(json.getJSONArray("args").getInt(0), is(17));
		assertThat(json.getJSONArray("args").getString(1), is("33"));
		assertThat(json.getJSONArray("args").getInt(1), is(33));

		JSONObject mods = json.getJSONObject("modifiers");
		assertThat(mods, notNullValue());
		assertThat(mods.length(), is(4));
		assertThat(mods.getString("foo"), is("123"));
		assertThat(mods.getInt("foo"), is(123));
		assertThat(mods.getString("bar"), is("654"));
		assertThat(mods.getInt("bar"), is(654));
		assertThat(mods.getInt("timeout"), is(Command.DEFAULT_TIMEOUT));
		assertThat(mods.getInt("thinktime"), is(Command.DEFAULT_THINKTIME));
	}

	@Test
	public void testCommandJsonWithQuotes() throws JSONException {
		Command cmd = new Command("Button \"some quoted id\" Click");

		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("some quoted id"));
		assertThat(cmd.getAction(), is("Click"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("some quoted id"));
		assertThat(json.getString("action"), is("Click"));
	}

	@Test
	public void testCommandJsonWithEscapedQuotes() throws JSONException {
		Command cmd = new Command("Button \"some \\\"escaped\\\" id\" Click");

		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("some \\\"escaped\\\" id"));
		assertThat(cmd.getAction(), is("Click"));

		JSONObject json = cmd.getCommandAsJSON();

		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("some \\\"escaped\\\" id"));
		assertThat(json.getString("action"), is("Click"));
	}

	@Test
	public void testCommandJsonWithSingleQuotes() throws JSONException {
		Command cmd = new Command("Button \"some 'single quotes' id\" Click");

		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("some 'single quotes' id"));
		assertThat(cmd.getAction(), is("Click"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("some 'single quotes' id"));
		assertThat(json.getString("action"), is("Click"));
	}

	@Test
	public void testNullCommand() throws JSONException {
		Command cmd = new Command();

		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.has("componentType"), is(false));
		assertThat(json.has("monkeyId"), is(false));
		assertThat(json.has("action"), is(false));
		assertThat(json.has("args"), is(true));
		assertThat(json.getJSONArray("args").length(), is(0));
		assertThat(json.has("modifiers"), is(true));

		JSONObject mods = json.getJSONObject("modifiers");
		assertThat(mods, notNullValue());
		assertThat(mods.length(), is(2));
		assertThat(mods.getInt("timeout"), is(Command.DEFAULT_TIMEOUT));
		assertThat(mods.getInt("thinktime"), is(Command.DEFAULT_THINKTIME));
	}

	@Test
	public void testCommandJsonWithExtendedLatin() throws JSONException {
		Command cmd = new Command("Button Héìíô Click");

		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("Héìíô"));
		assertThat(cmd.getAction(), is("Click"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("Héìíô"));
		assertThat(json.getString("action"), is("Click"));
	}

	@Test
	public void testCommandJsonWithUTF8() throws JSONException {
		Command cmd = new Command("Button \u21D0\u21D1\u21DD\u21DC Click");

		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("\u21D0\u21D1\u21DD\u21DC"));
		assertThat(cmd.getAction(), is("Click"));

		JSONObject json = cmd.getCommandAsJSON();
		assertThat(json.getString("componentType"), is("Button"));
		assertThat(json.getString("monkeyId"), is("\u21D0\u21D1\u21DD\u21DC"));
		assertThat(json.getString("action"), is("Click"));
	}
}