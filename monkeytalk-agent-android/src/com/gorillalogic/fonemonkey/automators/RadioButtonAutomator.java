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
package com.gorillalogic.fonemonkey.automators;

import android.widget.RadioButton;

import com.gorillalogic.fonemonkey.Log;

public class RadioButtonAutomator extends ButtonAutomator {
	private static String componentType = "RadioButton";
	private static Class<?> componentClass = RadioButton.class;
	static {
		Log.log("Initializing RadioButtonAutomator");
	}

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}
}