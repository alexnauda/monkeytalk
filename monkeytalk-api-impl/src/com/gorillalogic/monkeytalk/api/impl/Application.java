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
package com.gorillalogic.monkeytalk.api.impl;

/**
 * The application under test.
 */
public class Application implements com.gorillalogic.monkeytalk.api.Application {
	private String host;
	private int port;

	public Application(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/** {@inheritDoc} */
	@Override
	public int getPort() {
		return port;
	}

	/** {@inheritDoc} */
	@Override
	public String getHost() {
		return host;
	}
}