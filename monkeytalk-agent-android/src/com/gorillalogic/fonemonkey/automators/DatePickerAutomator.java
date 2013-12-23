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

import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * @author sstern
 * 
 */
public class DatePickerAutomator extends ViewAutomator implements OnDateChangedListener {

	private static Class<?> componentClass = DatePicker.class;
	static {
		Log.log("Initializing DatePickerAutomator");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gorillalogic.fonemonkey.automators.ViewAutomator#installDefaultListeners()
	 */
	@Override
	public boolean installDefaultListeners() {
		chainListenerFor(OnDateChangedListener.class, null);
		return true;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_DATE_PICKER;
	}

	public DatePicker getDatePicker() {
		return (DatePicker) getComponent();
	}

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ENTER_DATE)) {
			assertArgCount(action, args, 1);
			String[] date = args[0].split("-");
			if (date.length < 3) {
				throw new IllegalArgumentException("Expected date format YYYY-MM-DD but found: "
						+ args[0]);
			}

			try {
				final int year = Integer.valueOf(date[0]);
				final int month = Integer.valueOf(date[1]) - 1;
				final int day = Integer.valueOf(date[2]);
				AutomationManager.runOnUIThread(new Runnable() {
					public void run() {
						getDatePicker().updateDate(year, month, day);
					}

				});

			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid date format: " + args[0]);
			}

			return null;
		}

		return super.play(action, args);
	}

	@Override
	public boolean hides(View view) {
		// Hide all the children from recording
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.DatePicker.OnDateChangedListener#onDateChanged(android.widget.DatePicker,
	 * int, int, int)
	 */
	@Override
	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		String month = String.format("%02d", monthOfYear + 1);
		String day = String.format("%02d", dayOfMonth);
		AutomationManager.record(AutomatorConstants.ACTION_ENTER_DATE, view, year + "-" + month
				+ "-" + day);

	}

	public String getValue() {
		String month = String.format("%02d", getDatePicker().getMonth() + 1);
		String day = String.format("%02d", getDatePicker().getDayOfMonth());

		return getDatePicker().getYear() + "-" + month + "-" + day;
	}

}