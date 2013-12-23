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
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;

public class JsonRoundTripTest extends BaseCommandTest {

	private static final List<String> commands = new ArrayList<String>(
			Arrays.asList(
					"Button OK Click",
					"button OK click",
					"button Héìíô click",
					"button \u21D0\u21D1\u21DD\u21DC click",
					"Button OK Click 17 33",
					"button OK Click H é ì í ô",
					"Button OK Click %foo=123 %bar=654",
					"Button OK Click 17 33 %foo=123 %bar=654",
					"Button \"some quoted id\" Click",
					"Button \"some \\\"escaped\\\" id\" Click",
					"Button OK Click arg \"some arg\" \"third arg\"",
					"Button OK Click \"some \\\"escaped\\\" arg\" \"\\\"beginning escape\" \"end escape\\\"\""));

	@Test
	public void testJsonRoundTrip() {
		for (String command : commands) {
			Command cmd = new Command(command);
			JSONObject json = cmd.getCommandAsJSON();
			Command cmd2 = new Command(json);

			assertThat(cmd.getCommand(), is(command));
			assertThat(cmd2.getCommandAsJSON().toString(), is(json.toString()));
		}
	}
}