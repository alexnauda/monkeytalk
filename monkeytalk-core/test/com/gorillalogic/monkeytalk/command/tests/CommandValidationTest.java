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

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandValidator;
import com.gorillalogic.monkeytalk.CommandValidator.CommandStatus;

public class CommandValidationTest extends BaseCommandTest {

	@Test
	public void testDefaultConstructor() {
		CommandValidator v = new CommandValidator();
		assertThat(v.getStatus(), is(CommandStatus.OK));
		assertThat(v.getMessage(), is(""));
		assertThat(v.toString(), is("Ok"));
	}

	@Test
	public void testConstructorWithType() {
		CommandValidator v = new CommandValidator(
				CommandStatus.BAD_COMPONENT_TYPE);
		assertThat(v.getStatus(), is(CommandStatus.BAD_COMPONENT_TYPE));
		assertThat(v.getMessage(), is(""));
		assertThat(v.toString(), is("Bad componentType"));
	}

	@Test
	public void testConstructorWithNullType() {
		CommandValidator v = new CommandValidator(null);
		assertThat(v.getStatus(), is(CommandStatus.OK));
		assertThat(v.getMessage(), is(""));
		assertThat(v.toString(), is("Ok"));
	}

	@Test
	public void testConstructorWithTypeAndMessage() {
		CommandValidator v = new CommandValidator(CommandStatus.BAD_MONKEY_ID,
				"some message");
		assertThat(v.getStatus(), is(CommandStatus.BAD_MONKEY_ID));
		assertThat(v.getMessage(), is("some message"));
		assertThat(v.toString(), is("Bad monkeyId : some message"));
	}

	@Test
	public void testConstructorWithTypeAndNullMessage() {
		CommandValidator v = new CommandValidator(CommandStatus.BAD_ACTION,
				null);
		assertThat(v.getStatus(), is(CommandStatus.BAD_ACTION));
		assertThat(v.getMessage(), is(""));
		assertThat(v.toString(), is("Bad action"));
	}

	@Test
	public void testConstructorWithNullTypeAndNullMessage() {
		CommandValidator v = new CommandValidator(null);
		assertThat(v.getStatus(), is(CommandStatus.OK));
		assertThat(v.getMessage(), is(""));
		assertThat(v.toString(), is("Ok"));
	}

	@Test
	public void testNullCommandIsInvalid() {
		Command cmd = new Command();
		CommandValidator v = CommandValidator.validate(cmd);
		assertThat(v, notNullValue());
		assertThat(v.getStatus(), is(CommandStatus.BAD_COMPONENT_TYPE));
		assertThat(cmd.isValid(), is(false));
	}

	@Test
	public void testCommandIsValid() {
		Command cmd = new Command("Button OK Click");
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testCommandByPartsIsValid() {
		Command cmd = new Command("Button", "OK", "Click", null, null);
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testCommentIsValid() {
		Command cmd = new Command("# some comment");
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testNullComponentTypeIsInvalid() {
		Command cmd = new Command(null, "OK", "Click", null, null);
		CommandValidator v = CommandValidator.validate(cmd);
		assertThat(cmd.isValid(), is(false));
		assertThat(v.getStatus(), is(CommandStatus.BAD_COMPONENT_TYPE));
		assertThat(v.getMessage(), is("componentType is empty"));
		assertThat(v.toString(),
				is("Bad componentType : componentType is empty"));
	}

	@Test
	public void testBlankComponentTypeIsInvalid() {
		Command cmd = new Command("", "OK", "Click", null, null);
		assertThat(cmd.getComponentType(), is("*"));
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testComponentTypeWithSpaceIsInvalid() {
		Command cmd = new Command("Button Foo", "OK", "Click", null, null);
		assertThat(cmd.getComponentType(), is("ButtonFoo"));
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testComponentTypeWithTabIsInvalid() {
		Command cmd = new Command("Button\tFoo", "OK", "Click", null, null);
		assertThat(cmd.getComponentType(), is("ButtonFoo"));
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testNullMonkeyIdIsInvalid() {
		Command cmd = new Command("Button", null, "Click", null, null);
		CommandValidator v = CommandValidator.validate(cmd);
		assertThat(cmd.isValid(), is(false));
		assertThat(v.getStatus(), is(CommandStatus.BAD_MONKEY_ID));
		assertThat(v.getMessage(), is("monkeyId is empty"));
		assertThat(v.toString(),
				is("Bad monkeyId : monkeyId is empty"));
	}

	@Test
	public void testNullActionIsInvalid() {
		Command cmd = new Command("Button", "OK", null, null, null);
		CommandValidator v = CommandValidator.validate(cmd);
		assertThat(cmd.isValid(), is(false));
		assertThat(v.getStatus(), is(CommandStatus.BAD_ACTION));
		assertThat(v.getMessage(), is("action is empty"));
		assertThat(v.toString(), is("Bad action : action is empty"));
	}

	@Test
	public void testBlankActionIsInvalid() {
		Command cmd = new Command("Button", "OK", "", null, null);
		assertThat(cmd.getAction(), is("*"));
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testActionWithSpaceIsInvalid() {
		Command cmd = new Command("Button", "OK", "Click Foo", null, null);
		assertThat(cmd.getAction(), is("ClickFoo"));
		assertThat(cmd.isValid(), is(true));
	}

	@Test
	public void testActionWithTabIsInvalid() {
		Command cmd = new Command("Button", "OK", "Click\tFoo", null, null);
		assertThat(cmd.getAction(), is("ClickFoo"));
		assertThat(cmd.isValid(), is(true));
	}
}