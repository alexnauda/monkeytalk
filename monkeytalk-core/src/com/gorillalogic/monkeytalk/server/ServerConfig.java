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
package com.gorillalogic.monkeytalk.server;

/**
 * Static helper for record/playback server configuration.
 */
public class ServerConfig {
	public static final String DEFAULT_RECORD_HOST = "10.0.2.2";
	public static final int DEFAULT_RECORD_PORT = 16861;
	public static final String DEFAULT_PLAYBACK_HOST = "localhost";
	public static final int DEFAULT_PLAYBACK_PORT_ANDROID = 16862;
	public static final int DEFAULT_PLAYBACK_PORT_IOS = 16863;
	public static final int DEFAULT_PLAYBACK_PORT_HTML5 = 16864;
	public static final int DEFAULT_PLAYBACK_PORT_FLEX = 16865;
	public static final int DEFAULT_PLAYBACK_PORT_WEB = 80;

	private ServerConfig() {
	}

	/**
	 * Get the playback port.
	 * 
	 * @param agent
	 *            the target agent (Android, iOS, etc.)
	 * @return the playback port
	 */
	public static int getPlaybackPort(String agent) {
		if ("ios".equalsIgnoreCase(agent)) {
			return DEFAULT_PLAYBACK_PORT_IOS;
		}
		if ("flex".equalsIgnoreCase(agent)) {
			return DEFAULT_PLAYBACK_PORT_FLEX;
		}
		return DEFAULT_PLAYBACK_PORT_ANDROID;
	}
}