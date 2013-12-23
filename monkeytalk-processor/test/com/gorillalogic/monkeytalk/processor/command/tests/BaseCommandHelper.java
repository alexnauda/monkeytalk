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
package com.gorillalogic.monkeytalk.processor.command.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;

import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.SuiteListener;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class BaseCommandHelper extends TestHelper {
	protected static final String HOST = "localhost";
	protected static final int PORT = 18027;
	protected static String output;

	protected static final PlaybackListener LISTENER_WITH_OUTPUT = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			output += scope.getCurrentCommand();
		}

		@Override
		public void onScriptStart(Scope scope) {
			output += (output.length() > 0 ? "\n" : "") + "START\n";
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult r) {
			output += "COMPLETE : " + r;
		}

		@Override
		public void onComplete(Scope scope, Response resp) {
			output += " -> " + resp + "\n";
		}

		@Override
		public void onPrint(String message) {
			output += message;
		}
	};

	protected static final SuiteListener SUITE_LISTENER = new SuiteListener() {

		@Override
		public void onRunStart(int total) {
			output = "RUN (" + total + ")\n";
		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {
			output += "RUN_COMPLETE : " + result + "\n";
		}

		@Override
		public void onTestStart(String name, int num, int total) {
			output += "TEST " + name + " (" + num + " of " + total + ")\n";
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
			output += "TEST_COMPLETE : " + result + "\n";
		}

		@Override
		public void onSuiteStart(int total) {
			output += "SUITE\n";
		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
			output += "SUITE_COMPLETE : " + result + "\n";
		}
	};

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Before
	public void before() {
		output = "";
		Globals.clear();
	}

	protected class CommandServer extends
			com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer {

		public CommandServer(int port) throws IOException {
			super(port);
		}

		public void assertCommands(String... cmds) {
			assertCommands(false, cmds);
		}

		public void assertCommands(boolean showDefaultTimings, String... cmds) {
			assertThat(getCommands(), notNullValue());
			assertThat(getCommands().size(), is(cmds.length));
			for (int i = 0; i < cmds.length; i++) {
				assertThat(getCommands().get(i).getCommand(showDefaultTimings), is(cmds[i]));
			}
		}
	}
}