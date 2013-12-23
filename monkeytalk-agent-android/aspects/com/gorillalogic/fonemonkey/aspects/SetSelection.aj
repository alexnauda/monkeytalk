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
package com.gorillalogic.fonemonkey.aspects;

import android.view.View;
import android.widget.AdapterView;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

aspect SetSelection {
	pointcut ignoreSetSelection() : (call(* setSelection(int)));

	before(): ignoreSetSelection()
	{
		// The selection is being set programmatically. Suppress from recording.
		Object ob = thisJoinPoint.getTarget();
		if (ob instanceof AdapterView<?>) {
			int position = (Integer) thisJoinPoint.getArgs()[0];
			AdapterView<?> view = (AdapterView<?>) ob;

			// If calling this setter will trigger a recording event, we must suppress it.
			if (view.getSelectedItemPosition() != position) {
				AutomationManager.ignoreNext((View) thisJoinPoint.getTarget(),
						AutomatorConstants.ACTION_SELECT);
			}
		}
	}
}