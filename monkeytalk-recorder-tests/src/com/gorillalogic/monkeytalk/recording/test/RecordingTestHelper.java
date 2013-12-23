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

package com.gorillalogic.monkeytalk.recording.test;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class RecordingTestHelper extends TestHelper {

	private static Thread recordThread;
	private static AndroidEmulatorAgent agent;
	protected static CommandSender sender;

	@BeforeClass
	public static void beforeClass() {
		agent = new AndroidEmulatorAgent();
		agent.setProperty(AndroidEmulatorAgent.ADB_PROP,
				"/Applications/android-sdk-mac_x86/platform-tools/adb");
		agent.start();
		sender = agent.getCommandSender();
		recordThread = new Thread() {

			@Override
			public void run() {
				Recorder.start();
			}

		};

		recordThread.start();
		Recorder.waitUntilReady();
	}

	protected void assertRecorded(String cmd) {
		String expected = cmd;
		if (expected.contains("%thinktime")) {
			expected = expected.replaceAll(" %thinktime=\\d+", "");
		}
		assertRecorded(cmd, expected);
	}

	@Before
	public void before() throws IOException {
		Recorder.clearCommands();
	}

	protected void assertRecorded(String cmd, String expectedBack) {

		String xcmd = cmd;
		if (!xcmd.contains("%thinktime")) {
			xcmd += " %thinktime=0";
		}
		sender.play(new Command(xcmd + " %echo=true"));
		Command command = Recorder.popNextCommand(3000);

		Assert.assertNotNull("Never received expected command to be recorded: " + expectedBack,
				command);
		Assert.assertEquals(expectedBack, command.toString());
	}

	protected void play(String cmd) {
		play(cmd, 0);
	}
	
	protected void play(String cmd, long pauseAfter) {
		sender.play(new Command(cmd));
		try {
			Thread.sleep(pauseAfter);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
