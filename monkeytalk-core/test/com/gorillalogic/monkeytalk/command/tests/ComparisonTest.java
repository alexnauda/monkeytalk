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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class ComparisonTest extends BaseCommandTest {
	
	@Test
	public void testEqualCommands() {
		Command cmd  = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		
		assertAll(cmd);
		assertAll(cmd2);
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testEqualsSelf() {
		Command cmd  = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = cmd;
		
		assertAll(cmd);
		assertAll(cmd2);
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testEqualsIsCaseInsensitiveOnComponentType() {
		Command cmd  = new Command("buTTon OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("BUTTON OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testEqualsIsCaseInsensitiveOnAction() {
		Command cmd  = new Command("Button OK clICk 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("Button OK CLICK 17 \"some arg\" %foo=123 %bar=\"some val\"");
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testEqualsIsCaseSensitiveOnMonkeyId() {
		Command cmd  = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("Button ok Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		
		assertThat(cmd, is(not(cmd2)));
		assertThat(cmd.hashCode(), is(not(cmd2.hashCode())));
	}
	
	@Test
	public void testEqualsIsCaseSensitiveOnArgs() {
		Command cmd  = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("Button OK Click 17 \"SOME arg\" %foo=123 %bar=\"some val\"");
		
		assertThat(cmd, is(not(cmd2)));
		assertThat(cmd.hashCode(), is(not(cmd2.hashCode())));
	}
	
	@Test
	public void testEqualsIsCaseSensitiveOnKeyValueArgs() {
		Command cmd  = new Command("Button OK Click firstName=\"Joe Bob\"");
		Command cmd2 = new Command("Button OK Click firstname=\"Joe Bob\"");
		
		assertThat(cmd, is(not(cmd2)));
		assertThat(cmd.hashCode(), is(not(cmd2.hashCode())));
	}
	
	@Test
	public void testEqualsIsCaseInsensitiveOnModiferKeys() {
		Command cmd  = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("Button OK Click 17 \"some arg\" %FOO=123 %bar=\"some val\"");
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testEqualsIsCaseSensitiveOnModiferValues() {
		Command cmd  = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"SOME val\"");
		
		assertThat(cmd, is(not(cmd2)));
		assertThat(cmd.hashCode(), is(not(cmd2.hashCode())));
	}
	
	@Test
	public void testEqualsNull() {
		Command cmd  = new Command((String) null);
		
		assertThat(cmd.equals(null), is(false));
	}
	
	@Test
	public void testEqualNullCommands() {
		String s = null;
		Command cmd  = new Command(s);
		Command cmd2 = new Command(s);
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testNullCommandEqualsDefaultCommand() {
		Command cmd  = new Command((String) null);
		Command cmd2 = new Command();
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testEqualComments() {
		Command cmd  = new Command("# some comment");
		Command cmd2 = new Command("# some comment");
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testClone() {
		Command cmd = new Command("Button OK Click 17 \"some arg\" %foo=123 %bar=\"some val\"");
		Command cmd2 = cmd.clone();
		
		assertAll(cmd);
		assertAll(cmd2);
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	@Test
	public void testCloneComment() {
		Command cmd = new Command("# some comment");
		Command cmd2 = cmd.clone();
		
		assertThat(cmd.getCommand(), is("# some comment"));
		assertThat(cmd2.getCommand(), is("# some comment"));
		
		assertThat(cmd, is(cmd2));
		assertThat(cmd.hashCode(), is(cmd2.hashCode()));
	}
	
	private void assertAll(Command cmd) {
		assertCommand(cmd);
		
		assertThat(cmd.getArgs().size(), is(2));
		assertThat(cmd.getArgs(), hasItems("17", "some arg"));
		assertThat(cmd.getArgsAsString(), is("17 \"some arg\""));

		assertThat(cmd.getModifiers().size(), is(2));
		assertThat(cmd.getModifiers().values(), hasItems("123", "some val"));
		assertThat(cmd.getModifiers().keySet(), hasItems("foo", "bar"));
		assertThat(cmd.getModifiersAsString(), is("%foo=123 %bar=\"some val\""));
	}
}