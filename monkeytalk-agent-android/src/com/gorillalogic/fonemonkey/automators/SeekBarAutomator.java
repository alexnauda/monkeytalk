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

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 */
public class SeekBarAutomator extends ViewAutomator implements OnSeekBarChangeListener {

	@Override
	public void record(String operation, String... args) {
		if (operation.equals(AutomatorConstants.ACTION_SELECT)) {
			super.record(operation, args);
		}
	}

	private static Class<?> componentClass = SeekBar.class;
	static {
		Log.log("Initializing SeekBarAutomator");
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_SLIDER;
	}

	public SeekBar getSeekBar() {
		return (SeekBar) getComponent();
	}

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			if (args.length < 1) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_SELECT + " "
						+ componentType + " action requires one integer argument.");
			}
			String value = args[0];
			int intValue;
			try {
				intValue = Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_SELECT + " "
						+ componentType + " action requires an integer value but found: " + value);

			}
			final int progress = intValue;
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					getSeekBar().setProgress(progress);
				}
			});
			return null;
		}

		return super.play(action, args);
	}

	//
	// @Override
	// public void record(String action, Object[] args) {
	// if (action.equals(Operation.Select.toString())) {
	// Integer value = getSeekBar().getProgress();
	// AutomationManager.record(Operation.Select.toString(), getSeekBar(), value.toString());
	// return;
	// }
	// super.record(action, args);
	// }

	@Override
	public String getValue() {
		return String.valueOf(getSeekBar().getProgress());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar,
	 * int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android.widget.SeekBar)
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android.widget.SeekBar)
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		AutomationManager.record(AutomatorConstants.ACTION_SELECT, seekBar,
				String.valueOf(seekBar.getProgress()));

	}

	@Override
	protected String getProperty(String propertyPath) {
		if (propertyPath.equals("min")) {
			return "0";
		}
		if (propertyPath.equals("max")) {
			return String.valueOf(getSeekBar().getMax());
		}

		return super.getProperty(propertyPath);
	}

}