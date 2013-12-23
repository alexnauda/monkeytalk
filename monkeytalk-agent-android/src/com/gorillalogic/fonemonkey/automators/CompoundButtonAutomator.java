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

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class CompoundButtonAutomator extends ViewAutomator implements OnCheckedChangeListener {

	static {
		Log.log("Initializing CompoundButtonAutomator");
	}

	@Override
	public void record(String action, String... args) {
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_TAP)) {
			return;
		}
		super.record(action, args);
	}

	@Override
	public String play(String action, String... args) {
		boolean isChecked = getCompoundButton().isChecked();
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ON)) {
			if (!isChecked) {
				tap();
				return null;
			}
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_OFF)) {
			if (isChecked) {
				tap();
				return null;
			}
			return null;
		}

		return super.play(action, args);
	}

	public CompoundButton getCompoundButton() {
		return (CompoundButton) getComponent();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		String action = (isChecked ? AutomatorConstants.ACTION_ON : AutomatorConstants.ACTION_OFF);
		AutomationManager.record(action, buttonView, (String[]) null);
	}

	@Override
	public String getValue() {
		return String.valueOf(getCompoundButton().isChecked() ? "on" : "off");
	}
}