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
package com.gorillalogic.monkeytalk.agents;

import com.gorillalogic.monkeytalk.server.ServerConfig;

public class AndroidAgent extends MTAgent {

	public AndroidAgent() {
		this(ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID);
	}

	public AndroidAgent(int port) {
		super(port);
	}

	@Override
	public String getName() {
		return "Android";
	}
	
	@Override
	public String validate() {
		if (getHost() == null || getPort() == -1) {
			return getName() + " - playback host or port not set";
		} else if (getProperty(AndroidEmulatorAgent.ADB_PROP) != null) {
			return getName() + " - adb not needed when running against a remote Android device. Use the 'AndroidEmulator' agent to run on the Emulator or on a tethered device.";
		} else if (getProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP) != null) {
			return getName() + " - adbSerial not needed when running against a remote Android device. Use the 'AndroidEmulator' agent to run on the Emulator or on a tethered device.";
		} else if (getProperty(AndroidEmulatorAgent.ADB_LOCAL_PORT_PROP) != null) {
			return getName() + " - adbLocalPort not needed when running against a remote Android device. Use the 'AndroidEmulator' agent to run on the Emulator or on a tethered device.";
		} else if (getProperty(AndroidEmulatorAgent.ADB_REMOTE_PORT_PROP) != null) {
			return getName() + " - adbRemotePort not needed when running against a remote Android device. Use the 'AndroidEmulator' agent to run on the Emulator or on a tethered device.";
		}
		return super.validate();
	}
}
