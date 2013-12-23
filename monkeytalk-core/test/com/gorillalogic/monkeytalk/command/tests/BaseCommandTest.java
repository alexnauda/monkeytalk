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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import com.gorillalogic.monkeytalk.Command;

public class BaseCommandTest {

	protected void assertCommand(Command cmd) {
		assertThat(cmd.getCommand(), containsString("Button OK Click"));
		assertThat(cmd.getComponentType(), is("Button"));
		assertThat(cmd.getMonkeyId(), is("OK"));
		assertThat(cmd.getAction(), is("Click"));
		assertThat(cmd.getCommandName(), is("button.click"));
		assertThat(cmd.isComment(), is(false));
	}
	
	protected void assertNoArgsOrModifiers(Command cmd) {
		assertNoArgs(cmd);
		assertNoModifiers(cmd);
	}

	protected void assertNoArgs(Command cmd) {
		assertThat(cmd.getArgs(), notNullValue());
		assertThat(cmd.getArgs().size(), is(0));
		assertThat(cmd.getArgsAsString(), is(""));
	}

	protected void assertNoModifiers(Command cmd) {
		assertThat(cmd.getModifiers(), notNullValue());
		assertThat(cmd.getModifiers().size(), is(0));
		assertThat(cmd.getModifiersAsString(), is(""));
	}
}