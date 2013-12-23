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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class SubstitutionTest extends BaseCommandTest {

	@Test
	public void testBuiltinSubstitution() {
		Command cmd = new Command("%{componentType}%{monkeyId}%{action} monkeyId action");

		assertThat(cmd.getCommand(), is("%{componentType}%{monkeyId}%{action} monkeyId action"));
		assertThat(cmd.getComponentType(), is("%{componentType}%{monkeyId}%{action}"));
		assertThat(cmd.getMonkeyId(), is("monkeyId"));
		assertThat(cmd.getAction(), is("action"));

		Command newCmd = cmd.substitute("FOO", "BAR", "BAZ", null, null);

		assertThat(newCmd.getCommand(), is("FOOBARBAZ monkeyId action"));
		assertThat(newCmd.getComponentType(), is("FOOBARBAZ"));
		assertThat(newCmd.getMonkeyId(), is("monkeyId"));
		assertThat(newCmd.getAction(), is("action"));
	}

	@Test
	public void testArgsSubstitution() {
		Command cmd = new Command("comp%{1}%{3} monkeyId action");

		assertThat(cmd.getCommand(), is("comp%{1}%{3} monkeyId action"));
		assertThat(cmd.getComponentType(), is("comp%{1}%{3}"));
		assertThat(cmd.getMonkeyId(), is("monkeyId"));
		assertThat(cmd.getAction(), is("action"));

		List<String> args = new ArrayList<String>(Arrays.asList("FOO", "BAR", "BAZ"));

		Command newCmd = cmd.substitute(null, null, null, args, null);

		assertThat(newCmd.getCommand(), is("compFOOBAZ monkeyId action"));
		assertThat(newCmd.getComponentType(), is("compFOOBAZ"));
		assertThat(newCmd.getMonkeyId(), is("monkeyId"));
		assertThat(newCmd.getAction(), is("action"));
	}

	@Test
	public void testRepeatedArgsSubstitution() {
		Command cmd = new Command("comp%{1}%{3}%{3} monkeyId%{3} action%{3}");

		assertThat(cmd.getCommand(), is("comp%{1}%{3}%{3} monkeyId%{3} action%{3}"));
		assertThat(cmd.getComponentType(), is("comp%{1}%{3}%{3}"));
		assertThat(cmd.getMonkeyId(), is("monkeyId%{3}"));
		assertThat(cmd.getAction(), is("action%{3}"));

		List<String> args = new ArrayList<String>(Arrays.asList("FOO", "BAR", "BAZ"));

		Command newCmd = cmd.substitute(null, null, null, args, null);

		assertThat(newCmd.getCommand(), is("compFOOBAZBAZ monkeyIdBAZ actionBAZ"));
		assertThat(newCmd.getComponentType(), is("compFOOBAZBAZ"));
		assertThat(newCmd.getMonkeyId(), is("monkeyIdBAZ"));
		assertThat(newCmd.getAction(), is("actionBAZ"));
	}

	@Test
	public void testMissingArgsSubstitution() {
		Command cmd = new Command("comp%{1}%{5}%{3} monkeyId%{6} action%{7}");

		assertThat(cmd.getCommand(), is("comp%{1}%{5}%{3} monkeyId%{6} action%{7}"));
		assertThat(cmd.getComponentType(), is("comp%{1}%{5}%{3}"));
		assertThat(cmd.getMonkeyId(), is("monkeyId%{6}"));
		assertThat(cmd.getAction(), is("action%{7}"));

		List<String> args = new ArrayList<String>(Arrays.asList("FOO", "BAR", "BAZ"));

		Command newCmd = cmd.substitute(null, null, null, args, null);

		assertThat(newCmd.getCommand(), is("compFOO%{5}BAZ monkeyId%{6} action%{7}"));
		assertThat(newCmd.getComponentType(), is("compFOO%{5}BAZ"));
		assertThat(newCmd.getMonkeyId(), is("monkeyId%{6}"));
		assertThat(newCmd.getAction(), is("action%{7}"));
	}

	@Test
	public void testVariableSubstitution() {
		Command cmd = new Command("${foo} ${bar} ${baz}");

		assertThat(cmd.getCommand(), is("${foo} ${bar} ${baz}"));
		assertThat(cmd.getComponentType(), is("${foo}"));
		assertThat(cmd.getMonkeyId(), is("${bar}"));
		assertThat(cmd.getAction(), is("${baz}"));

		Map<String, String> vars = new LinkedHashMap<String, String>();
		vars.put("foo", "FOO");
		vars.put("bar", "BAR");
		vars.put("baz", "BAZ");

		Command newCmd = cmd.substitute(null, null, null, null, vars);

		assertThat(newCmd.getCommand(), is("FOO BAR BAZ"));
		assertThat(newCmd.getComponentType(), is("FOO"));
		assertThat(newCmd.getMonkeyId(), is("BAR"));
		assertThat(newCmd.getAction(), is("BAZ"));
	}

	@Test
	public void testEverythingSubstitution() {
		Command cmd = new Command(
				"Button OK Click %{componentType}%{monkeyId}%{action}%{1}%{2}${foo}${bar}${baz}");

		assertThat(
				cmd.getCommand(),
				is("Button OK Click %{componentType}%{monkeyId}%{action}%{1}%{2}${foo}${bar}${baz}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(),
				hasItems("%{componentType}%{monkeyId}%{action}%{1}%{2}${foo}${bar}${baz}"));
		assertThat(cmd.getArgsAsString(),
				is("%{componentType}%{monkeyId}%{action}%{1}%{2}${foo}${bar}${baz}"));

		List<String> args = new ArrayList<String>(Arrays.asList("1", "2", "3"));

		Map<String, String> vars = new LinkedHashMap<String, String>();
		vars.put("foo", "FOO");
		vars.put("bar", "BAR");
		vars.put("baz", "BAZ");

		Command newCmd = cmd.substitute("A", "B", "C", args, vars);

		assertThat(newCmd.getCommand(), is("Button OK Click ABC12FOOBARBAZ"));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs().size(), is(1));
		assertThat(newCmd.getArgs(), hasItems("ABC12FOOBARBAZ"));
		assertThat(newCmd.getArgsAsString(), is("ABC12FOOBARBAZ"));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testArgsWithSpacesSubstitution() {
		Command cmd = new Command("Button OK Click %{1}%{2}%{3}");

		assertThat(cmd.getCommand(), is("Button OK Click %{1}%{2}%{3}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), is(Arrays.asList("%{1}%{2}%{3}")));
		assertThat(cmd.getArgsAsString(), is("%{1}%{2}%{3}"));

		List<String> args = Arrays.asList("F O", "B R", "B Z");

		Command newCmd = cmd.substitute(null, null, null, args, null);

		assertThat(newCmd.getCommand(), is("Button OK Click \"F OB RB Z\""));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs(), is(Arrays.asList("F OB RB Z")));
		assertThat(newCmd.getArgsAsString(), is("\"F OB RB Z\""));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testArgsWithDollarsSubstitution() {
		Command cmd = new Command("Button OK Click %{1}");

		assertThat(cmd.getCommand(), is("Button OK Click %{1}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), is(Arrays.asList("%{1}")));
		assertThat(cmd.getArgsAsString(), is("%{1}"));

		List<String> args = Arrays.asList("$1,234.56");

		Command newCmd = cmd.substitute(null, null, null, args, null);

		assertThat(newCmd.getCommand(), is("Button OK Click $1,234.56"));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs(), is(Arrays.asList("$1,234.56")));
		assertThat(newCmd.getArgsAsString(), is("$1,234.56"));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testArgsWithBracketsSubstitution() {
		Command cmd = new Command("Button OK Click %{1}");

		assertThat(cmd.getCommand(), is("Button OK Click %{1}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), is(Arrays.asList("%{1}")));
		assertThat(cmd.getArgsAsString(), is("%{1}"));

		List<String> args = Arrays.asList("${bar}");

		Command newCmd = cmd.substitute(null, null, null, args, null);

		assertThat(newCmd.getCommand(), is("Button OK Click ${bar}"));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs(), is(Arrays.asList("${bar}")));
		assertThat(newCmd.getArgsAsString(), is("${bar}"));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testVarsWithSpacesSubstitution() {
		Command cmd = new Command("Button OK Click ${foo}${bar}${baz}");

		assertThat(cmd.getCommand(), is("Button OK Click ${foo}${bar}${baz}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs().size(), is(1));
		assertThat(cmd.getArgs(), hasItems("${foo}${bar}${baz}"));
		assertThat(cmd.getArgsAsString(), is("${foo}${bar}${baz}"));

		Map<String, String> vars = new LinkedHashMap<String, String>();
		vars.put("foo", "F O");
		vars.put("bar", "B R");
		vars.put("baz", "B Z");

		Command newCmd = cmd.substitute(null, null, null, null, vars);

		assertThat(newCmd.getCommand(), is("Button OK Click \"F OB RB Z\""));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs().size(), is(1));
		assertThat(newCmd.getArgs(), hasItems("F OB RB Z"));
		assertThat(newCmd.getArgsAsString(), is("\"F OB RB Z\""));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testVarsWithDollarsSubstitution() {
		Command cmd = new Command("Button OK Click ${foo}");

		assertThat(cmd.getCommand(), is("Button OK Click ${foo}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), is(Arrays.asList("${foo}")));
		assertThat(cmd.getArgsAsString(), is("${foo}"));

		Map<String, String> vars = new LinkedHashMap<String, String>();
		vars.put("foo", "$1,234.56");

		Command newCmd = cmd.substitute(null, null, null, null, vars);

		assertThat(newCmd.getCommand(), is("Button OK Click $1,234.56"));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs(), is(Arrays.asList("$1,234.56")));
		assertThat(newCmd.getArgsAsString(), is("$1,234.56"));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testVarsWithBracketsSubstitution() {
		Command cmd = new Command("Button OK Click ${foo}");

		assertThat(cmd.getCommand(), is("Button OK Click ${foo}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertThat(cmd.getArgs(), is(Arrays.asList("${foo}")));
		assertThat(cmd.getArgsAsString(), is("${foo}"));

		Map<String, String> vars = new LinkedHashMap<String, String>();
		vars.put("foo", "${bar}");

		Command newCmd = cmd.substitute(null, null, null, null, vars);

		assertThat(newCmd.getCommand(), is("Button OK Click ${bar}"));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertThat(newCmd.getArgs(), is(Arrays.asList("${bar}")));
		assertThat(newCmd.getArgsAsString(), is("${bar}"));

		assertNoModifiers(newCmd);
	}

	@Test
	public void testModifiersSubstitution() {
		Command cmd = new Command("Button OK Click %foo=${foo}");

		assertThat(cmd.getCommand(), is("Button OK Click %foo=${foo}"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));

		assertNoArgs(cmd);

		assertThat(cmd.getModifiers().values(), hasItems("${foo}"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo"));
		assertThat(cmd.getModifiersAsString(), is("%foo=${foo}"));

		Map<String, String> vars = new LinkedHashMap<String, String>();
		vars.put("foo", "F O");
		vars.put("bar", "B R");
		vars.put("baz", "B Z");

		Command newCmd = cmd.substitute(null, null, null, null, vars);

		assertThat(newCmd.getCommand(), is("Button OK Click %foo=\"F O\""));
		assertThat(newCmd.getComponentType(), is("Button"));
		assertThat(newCmd.getMonkeyId(), is("OK"));
		assertThat(newCmd.getAction(), is("Click"));

		assertNoArgs(newCmd);

		assertThat(newCmd.getModifiers().values(), hasItems("F O"));
		assertThat(newCmd.getModifiers().keySet(), hasItems("foo"));
		assertThat(newCmd.getModifiersAsString(), is("%foo=\"F O\""));
	}

	@Test
	public void testNullCommandSubstitution() {
		Command cmd = new Command();

		assertThat(cmd.getCommand(), nullValue());
		assertThat(cmd.getComponentType(), nullValue());
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());

		Command newCmd = cmd.substitute("FOO", "BAR", "BAZ", null, null);

		assertThat(newCmd.getCommand(), nullValue());
		assertThat(newCmd.getComponentType(), nullValue());
		assertThat(newCmd.getMonkeyId(), nullValue());
		assertThat(newCmd.getAction(), nullValue());
	}

	@Test
	public void testCommandSubstitutionOfInvalidCommand() {
		Command cmd = new Command("foo%{componentType}", null, null, null, null);

		assertThat(cmd.getCommand(), is("foo%{componentType}"));
		assertThat(cmd.getComponentType(), is("foo%{componentType}"));
		assertThat(cmd.getMonkeyId(), nullValue());
		assertThat(cmd.getAction(), nullValue());
		assertThat(cmd.isValid(), is(false));

		Command newCmd = cmd.substitute("FOO", "BAR", "BAZ", null, null);

		assertThat(newCmd.getCommand(), is("fooFOO"));
		assertThat(newCmd.getComponentType(), is("fooFOO"));
		assertThat(newCmd.getMonkeyId(), nullValue());
		assertThat(newCmd.getAction(), nullValue());
		assertThat(newCmd.isValid(), is(false));
		assertNoArgsOrModifiers(newCmd);
	}
}