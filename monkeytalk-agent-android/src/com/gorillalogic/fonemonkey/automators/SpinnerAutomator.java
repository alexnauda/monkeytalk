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

import android.view.View.OnClickListener;
import android.widget.Spinner;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class SpinnerAutomator extends AdapterViewAutomator {

	private static Class<?> componentClass = Spinner.class;

	static {
		Log.log("Initializing SpinnerAutomator");
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_ITEM_SELECTOR;
	}

	private Spinner getSpinner() {
		return (Spinner) getComponent();
	}

	@Override
	protected void scrollToPosition(int position) {
		// do nothing - since dropdowns don't scroll
	}

	@Override
	protected void tapPosition(final int position, boolean isLong) {
		if (isLong) {
			throw new IllegalArgumentException(getComponentType() + " does not support Long Press");
		}

		AutomationManager.runOnUIThread(new Runnable() {
			public void run() {
				getSpinner().setSelection(position, true);
			}
		});
	}

	private static String[] aliases = { "Spinner" };

	@Override
	public String[] getAliases() {
		return aliases;
	}

	/**
	 * Setting an OnClickListener disables spinner popup and we don't need it to be recorded so we
	 * just ignore clicks on the spinner button. We do still record itemSelections of course.
	 */
	@Override
	protected boolean isExcludedFromChaining(Class<?> klass) {
		return OnClickListener.class.isAssignableFrom(klass);

	}
}