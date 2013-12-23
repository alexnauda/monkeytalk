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

import java.lang.reflect.Method;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class RadioButtonsAutomator extends ViewAutomator implements OnCheckedChangeListener {
	private static String componentType = "ButtonSelector";
	private static Class<?> componentClass = RadioGroup.class;
	static {
		Log.log("Initializing RadioButtonsAutomator");
	}

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String play(String action, String... args) {
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			assertArgCount(AutomatorConstants.ACTION_SELECT, args, 1);
			String label = args[0];
			for (int i = 0; i < getRadioGroup().getChildCount(); i++) {
				RadioButton but = (RadioButton) getRadioGroup().getChildAt(i);
				if (but.getText().toString().equals(label)) {
					AutomationUtils.tap(but);
					return null;
				}
			}
			throw new IllegalArgumentException("Unable to find Radio Button \"" + label + "\"");
		}
		return super.play(action, args);
	}

	public RadioGroup getRadioGroup() {
		return (RadioGroup) getComponent();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		View v=group.findViewById(checkedId);
		if (v instanceof RadioButton) {
			RadioButton button = (RadioButton) v;
			String label = button.getText().toString();
			AutomationManager.record(AutomatorConstants.ACTION_SELECT, group, label);
		} else {
			// someone has created some kind of custom RadioGroup that does not contain RadioButtons
			// see if we can get any kind of getText() method on this thing
			String label = "unknown_not_a_RadioButton";
			try {
				Method m = v.getClass().getMethod("getText", (Class<?>[])null);
				if (m!=null) {
					Object o = m.invoke(v);
					if (o != null && o instanceof String) {
						label=(String)o;
					}				}
			} catch (Exception e) {
				//ok, go with "unknown" 
			}
			AutomationManager.record(AutomatorConstants.ACTION_SELECT, group, label);	
		}
	}

	@Override
	public boolean hides(View view) {
		return true;
	}

	@Override
	public String getValue() {
		for (int i = 0; i < getRadioGroup().getChildCount(); i++) {
			RadioButton but = (RadioButton) getRadioGroup().getChildAt(i);
			if (but.isChecked()) {
				return but.getText().toString();
			}
		}
		return null;
	}

	@Override
	public String getProperty(String propertyPath) {
		if (propertyPath.equals("size")) {
			return String.valueOf(getRadioGroup().getChildCount());
		}
		return super.getProperty(propertyPath);
	}

	private static String[] aliases = { "RadioGroup" };

	@Override
	public String[] getAliases() {
		return aliases;
	}
}