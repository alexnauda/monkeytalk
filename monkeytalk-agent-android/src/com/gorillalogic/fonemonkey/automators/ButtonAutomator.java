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

import android.widget.Button;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * Automation support for android.view.Button classes
 * 
 * @author sstern
 * 
 */
public class ButtonAutomator extends TextViewAutomator {

	private static Class<?> componentClass = Button.class;
	static {
		Log.log("Initializing ButtonAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_BUTTON;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	public Button getButton() {
		return (Button) getComponent();
	}

	@Override
	public String getMonkeyID() {
		String s = getButton().getText().toString();
		return s != null && s.trim().length() > 0 ? s : getDefaultMonkeyID();
	}
}