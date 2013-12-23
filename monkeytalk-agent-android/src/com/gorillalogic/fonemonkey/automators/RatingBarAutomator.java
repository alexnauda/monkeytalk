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

import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * @author sstern
 * 
 */
public class RatingBarAutomator extends ViewAutomator implements OnRatingBarChangeListener {

	private static Class<?> componentClass = RatingBar.class;
	static {
		Log.log("Initializing RatingBarAutomator");
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_RATING_BAR;
	}

	public RatingBar getRatingBar() {
		return (RatingBar) getComponent();
	}

	@Override
	public void record(String operation, String... args) {
		if (operation.equals(AutomatorConstants.ACTION_SELECT)) {
			super.record(operation, args);
		}
	}

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			if (args.length < 1) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_SELECT + " "
						+ componentType + " action requires one numeric argument.");
			}
			String value = args[0];
			float floatValue;
			try {
				floatValue = Float.valueOf(value);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_SELECT + " "
						+ componentType + " action requires a numeric value but found: " + value);

			}
			final float rating = floatValue;
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					getRatingBar().setRating(rating);
				}
			});
			return null;
		}
		return super.play(action, args);
	}

	// @Override
	// public void record(String action, Object[] args) {
	// if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
	// Float value = getRatingBar().getRating();
	// AutomationManager.record(Operation.Select.toString(), getRatingBar(), value.toString());
	// return;
	// }
	// super.record(action, args);
	// }

	@Override
	public String getValue() {
		return String.valueOf(getRatingBar().getRating());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.RatingBar.OnRatingBarChangeListener#onRatingChanged(android.widget.RatingBar,
	 * float, boolean)
	 */
	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		if (!fromUser) {
			return;
		}
		AutomationManager.record(AutomatorConstants.ACTION_SELECT, ratingBar,
				String.valueOf(rating));

	}
}