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
package com.gorillalogic.monkeytalk.processor.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.Scope;

public class ScopeTest {

	@Test
	public void testDefaultConstructor() {
		Scope scope = new Scope();

		assertThat(scope.getFilename(), nullValue());
		assertThat(scope.getParentScope(), nullValue());
		assertThat(scope.getComponentType(), nullValue());
		assertThat(scope.getMonkeyId(), nullValue());
		assertThat(scope.getAction(), nullValue());
		assertThat(scope.getArgs(), notNullValue());
		assertThat(scope.getArgs().size(), is(0));
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));
		assertThat(scope.getCurrentCommand(), nullValue());
		assertThat(scope.getCurrentIndex(), is(0));
	}

	@Test
	public void testNullConstructor() {
		Scope scope = new Scope(null);

		assertThat(scope.getFilename(), nullValue());
		assertThat(scope.getParentScope(), nullValue());
		assertThat(scope.getComponentType(), nullValue());
		assertThat(scope.getMonkeyId(), nullValue());
		assertThat(scope.getAction(), nullValue());
		assertThat(scope.getArgs(), notNullValue());
		assertThat(scope.getArgs().size(), is(0));
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));
		assertThat(scope.getCurrentCommand(), nullValue());
		assertThat(scope.getCurrentIndex(), is(0));
	}

	@Test
	public void testFilenameConstructor() {
		Scope scope = new Scope("foo.mt");

		assertThat(scope.getFilename(), is("foo.mt"));
		assertThat(scope.getParentScope(), nullValue());
		assertThat(scope.getComponentType(), nullValue());
		assertThat(scope.getMonkeyId(), nullValue());
		assertThat(scope.getAction(), nullValue());
		assertThat(scope.getArgs(), notNullValue());
		assertThat(scope.getArgs().size(), is(0));
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));
		assertThat(scope.getCurrentCommand(), nullValue());
		assertThat(scope.getCurrentIndex(), is(0));
	}

	@Test
	public void testFilenameAndParentScopeConstructor() {
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope("foo.mt", parent);

		assertThat(scope.getFilename(), is("foo.mt"));
		assertThat(scope.getParentScope().getFilename(), is("parent.mt"));
		assertThat(scope.getScopeHierarchy(scope, " > "), is("parent.mt > foo.mt"));
	}

	@Test
	public void testCommandConstructor() {
		Command cmd = new Command("Script foo.mt Run arg1 arg2");
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope(cmd, parent);

		assertThat(scope.getFilename(), is("foo.mt"));
		assertThat(scope.getParentScope().getFilename(), is("parent.mt"));
		assertThat(scope.getScopeHierarchy(), is("parent.mt > foo.mt"));
		assertThat(scope.getScopeHierarchy(scope, " > "), is("parent.mt > foo.mt"));
		assertThat(scope.toString(), containsString("hierarchy=parent.mt > foo.mt"));

		assertThat(scope.getComponentType(), is("Script"));
		assertThat(scope.getMonkeyId(), is("foo.mt"));
		assertThat(scope.getAction(), is("Run"));

		assertThat(scope.getArgs(), notNullValue());
		assertThat(scope.getArgs().size(), is(2));
		assertThat(scope.getArgs(), hasItems("arg1", "arg2"));

		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));

		assertThat(scope.getCurrentCommand(), nullValue());
		assertThat(scope.getCurrentIndex(), is(0));
	}

	@Test
	public void testCommandAndVarsConstructor() {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("foo", "123");
		vars.put("bar", "654");
		Command cmd = new Command("Script foo.mt Run arg1 arg2");
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope(cmd, parent, vars);

		assertThat(scope.getFilename(), is("foo.mt"));
		assertThat(scope.getParentScope().getFilename(), is("parent.mt"));
		assertThat(scope.getScopeHierarchy(scope, " > "), is("parent.mt > foo.mt"));

		assertThat(scope.getComponentType(), is("Script"));
		assertThat(scope.getMonkeyId(), is("foo.mt"));
		assertThat(scope.getAction(), is("Run"));

		assertThat(scope.getArgs(), notNullValue());
		assertThat(scope.getArgs().size(), is(2));
		assertThat(scope.getArgs(), hasItems("arg1", "arg2"));

		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(2));
		assertThat(scope.getVariables().values(), hasItems("123", "654"));
		assertThat(scope.getVariables().keySet(), hasItems("foo", "bar"));

		assertThat(scope.getCurrentCommand(), nullValue());
		assertThat(scope.getCurrentIndex(), is(0));
	}

	@Test
	public void testAddVariables() {
		Scope scope = new Scope("foo.mt");

		assertThat(scope.getFilename(), is("foo.mt"));
		assertThat(scope.getVariables(), notNullValue());
		assertThat(scope.getVariables().size(), is(0));

		scope.addVariable("foo", "123");
		assertThat(scope.getVariables().size(), is(1));
		assertThat(scope.getVariables().values(), hasItems("123"));
		assertThat(scope.getVariables().keySet(), hasItems("foo"));

		scope.addVariables(null);
		assertThat(scope.getVariables().size(), is(1));
		assertThat(scope.getVariables().values(), hasItems("123"));
		assertThat(scope.getVariables().keySet(), hasItems("foo"));

		Map<String, String> vars = new HashMap<String, String>();
		vars.put("bar", "643");
		vars.put("baz", "some val");

		scope.addVariables(vars);
		assertThat(scope.getVariables().size(), is(3));
		assertThat(scope.getVariables().values(), hasItems("123", "643", "some val"));
		assertThat(scope.getVariables().keySet(), hasItems("foo", "bar", "baz"));
	}

	@Test
	public void testSubstitute() {
		Command cmd = new Command("Script foo.mt Run arg1 arg2");
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope(cmd, parent);

		Command child = new Command("Button OK Click %{componentType}%{monkeyId}%{action}%{1}%{2}");
		assertThat(child.getCommand(),
				is("Button OK Click %{componentType}%{monkeyId}%{action}%{1}%{2}"));
		assertThat(child.getComponentType(), is("Button"));
		assertThat(child.getMonkeyId(), is("OK"));
		assertThat(child.getAction(), is("Click"));
		assertThat(child.isComment(), is(false));

		assertThat(child.getArgs().size(), is(1));
		assertThat(child.getArgs(), hasItems("%{componentType}%{monkeyId}%{action}%{1}%{2}"));
		assertThat(child.getArgsAsString(), is("%{componentType}%{monkeyId}%{action}%{1}%{2}"));

		Command full = scope.substituteCommand(child);
		assertThat(full.getCommand(), is("Button OK Click Scriptfoo.mtRunarg1arg2"));
		assertThat(full.getComponentType(), is("Button"));
		assertThat(full.getMonkeyId(), is("OK"));
		assertThat(full.getAction(), is("Click"));
		assertThat(full.isComment(), is(false));

		assertThat(full.getArgs().size(), is(1));
		assertThat(full.getArgs(), hasItems("Scriptfoo.mtRunarg1arg2"));
		assertThat(full.getArgsAsString(), is("Scriptfoo.mtRunarg1arg2"));
	}

	@Test
	public void testSubstituteComment() {
		Scope scope = new Scope("foo.mt");
		Command cmd = new Command("# some comment");

		Command full = scope.substituteCommand(cmd);
		assertThat(full.getCommand(), is("# some comment"));
		assertThat(full.getComponentType(), is(nullValue()));
		assertThat(full.getMonkeyId(), is(nullValue()));
		assertThat(full.getAction(), is(nullValue()));
		assertThat(full.isComment(), is(true));

		assertThat(full.getArgs().size(), is(0));
		assertThat(full.getModifiers().size(), is(0));
	}

	@Test
	public void testSetCommand() {
		Command cmd = new Command("Script foo.mt Run arg1 arg2");
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope(cmd, parent);

		Command child = new Command("Button OK Click");
		scope.setCurrentCommand(child);
		assertThat(scope.getCurrentCommand(), is(child));
		assertThat(scope.getCurrentIndex(), is(1));

		Command child2 = new Command("Button OK Click 17 33");
		scope.setCurrentCommand(child2);
		assertThat(scope.getCurrentCommand(), is(child2));
		assertThat(scope.getCurrentIndex(), is(2));

		Command child3 = new Command("Button OK Click arg1 arg2");
		scope.setCurrentCommand(child3, 37);
		assertThat(scope.getCurrentCommand(), is(child3));
		assertThat(scope.getCurrentIndex(), is(37));
	}

	@Test
	public void testScopeHierarchy() {
		Scope scope = new Scope();
		assertThat(scope.getScopeHierarchy(null, "|"), is(""));
		assertThat(scope.getScopeHierarchy(), is("<commands>"));
		assertThat(scope.getScopeHierarchy("|", false), is("<commands>"));
		assertThat(scope.getScopeHierarchy("|", true), is("<commands>:0"));

		Scope parent = new Scope("parent.mt");
		scope = new Scope("foo.mt", parent);
		assertThat(scope.getScopeHierarchy(), is("parent.mt > foo.mt"));
		assertThat(scope.getScopeHierarchy("|", false), is("parent.mt|foo.mt"));
		assertThat(scope.getScopeHierarchy("|", true), is("parent.mt:0|foo.mt:0"));

		Command cmd = new Command("Button OK Click");
		parent = new Scope("parent.mt");
		parent.setCurrentCommand(cmd, 3);
		scope = new Scope("foo.mt", parent);
		scope.setCurrentCommand(cmd, 2);
		Scope child = new Scope("bar.mt", scope);
		child.setCurrentCommand(cmd, 1);
		assertThat(child.getScopeHierarchy(), is("parent.mt > foo.mt > bar.mt"));
		assertThat(child.getScopeHierarchy("|", false), is("parent.mt|foo.mt|bar.mt"));
		assertThat(child.getScopeHierarchy("|", true), is("parent.mt:3|foo.mt:2|bar.mt:1"));
	}

	@Test
	public void testScopeTrace() {
		Scope scope = new Scope();
		assertThat(scope.getScopeTrace(), is("  at <unknown command> (<commands> : cmd #0)"));
	}

	@Test
	public void testScopeTraceWithParent() {
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope("foo.mt", parent);
		assertThat(scope.getScopeTrace(), is("  at <unknown command> (foo.mt : cmd #0)\n"
				+ "  at <unknown command> (parent.mt : cmd #0)"));
	}

	@Test
	public void testScopeTraceWithHierarchy() {
		Scope parent = new Scope("parent.mt");
		parent.setCurrentCommand(new Command("Script foo.mt Run"), 3);

		Scope scope = new Scope("foo.mt", parent);
		scope.setCurrentCommand(new Command("Script bar.mt Run"), 2);

		Scope child = new Scope("bar.mt", scope);
		child.setCurrentCommand(new Command("Button BAR Tap"), 1);

		assertThat(child.getScopeTrace(), is("  at Button BAR Tap (bar.mt : cmd #1)\n"
				+ "  at Script bar.mt Run (foo.mt : cmd #2)\n"
				+ "  at Script foo.mt Run (parent.mt : cmd #3)"));
	}
	
	@Test
	public void testScopeTraceWithHierarchyWithArgs() {
		Scope parent = new Scope("parent.mt");
		parent.setCurrentCommand(new Command("Script foo.mt Run arg1 arg2"), 3);
		
		Scope scope = new Scope("foo.mt", parent);
		scope.setCurrentCommand(new Command("Script bar.mt Run"), 2);
		
		Scope child = new Scope("bar.mt", scope);
		child.setCurrentCommand(new Command("Button BAR Tap"), 1);
		
		assertThat(child.getScopeTrace(), is("  at Button BAR Tap (bar.mt : cmd #1)\n"
				+ "  at Script bar.mt Run (foo.mt : cmd #2)\n"
				+ "  at Script foo.mt Run arg1 arg2 (parent.mt : cmd #3)"));
	}

	@Test
	public void testScopeTraceWithHierarchyWithDataDrive() {
		Scope parent = new Scope("parent.mt");
		parent.setCurrentCommand(new Command("Script foo.mt Run arg1 arg2"), 3);

		Scope scope = new Scope("foo.mt", parent);
		scope.setCurrentCommand(new Command("Script bar.mt Run data.csv"), 2);
		scope.addVariable("key1", "val1");
		scope.addVariable("key2", "val2");

		Scope child = new Scope("bar.mt", scope);
		child.setCurrentCommand(new Command("Button BAR Tap"), 1);

		assertThat(child.getScopeTrace(), is("  at Button BAR Tap (bar.mt : cmd #1)\n"
				+ "  at Script bar.mt Run data.csv [key1=val1 key2=val2] (foo.mt : cmd #2)\n"
				+ "  at Script foo.mt Run arg1 arg2 (parent.mt : cmd #3)"));
	}

	@Test
	public void testClone() {
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("var", "some val");
		vars.put("zar", "other val");

		Command cmd = new Command("Script foo.mt Run 17 33 %foo=123 %bar=654");
		Scope parent = new Scope("parent.mt");
		Scope scope = new Scope(cmd, parent, vars);

		assertThat(scope.getFilename(), is("foo.mt"));
		assertThat(scope.getComponentType(), is("Script"));
		assertThat(scope.getMonkeyId(), is("foo.mt"));
		assertThat(scope.getAction(), is("Run"));
		assertThat(scope.getArgs(), hasItems("17", "33"));
		assertThat(scope.getVariables().size(), is(2));
		assertThat(scope.getVariables().values(), hasItems("some val", "other val"));
		assertThat(scope.getVariables().keySet(), hasItems("var", "zar"));

		Scope clone = scope.clone();

		assertThat(clone.getFilename(), is("foo.mt"));
		assertThat(clone.getComponentType(), is("Script"));
		assertThat(clone.getMonkeyId(), is("foo.mt"));
		assertThat(clone.getAction(), is("Run"));
		assertThat(clone.getArgs(), hasItems("17", "33"));
		assertThat(clone.getVariables().size(), is(2));
		assertThat(clone.getVariables().values(), hasItems("some val", "other val"));
		assertThat(clone.getVariables().keySet(), hasItems("var", "zar"));
	}
}