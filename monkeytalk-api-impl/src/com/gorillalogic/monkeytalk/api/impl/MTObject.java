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
 * Base class for all MonkeyTalk Objects
 */
public class MTObject implements com.gorillalogic.monkeytalk.api.MTObject {
	protected Application app;
	protected String componentType;
	protected String monkeyId;

	/**
	 * Instantiate an MTObject (aka a component) with the given app and monkeyId.
	 * 
	 * @param app
	 *            the app under test
	 * @param monkeyId
	 *            the MonkeyTalk monkeyId of the component
	 */
	public MTObject(Application app, String monkeyId) {
		this.app = app;
		this.monkeyId = monkeyId;
	}

	/** {@inheritDoc} */
	@Override
	public Application getApp() {
		return app;
	}

	/** {@inheritDoc} */
	@Override
	public String getComponentType() {
		return componentType;
	}

	/** {@inheritDoc} */
	@Override
	public String getMonkeyId() {
		return monkeyId;
	}
}