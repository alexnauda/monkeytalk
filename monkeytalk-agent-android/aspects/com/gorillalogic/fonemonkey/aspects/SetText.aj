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

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

aspect SetText {
	pointcut ignoreSetText() : (call(* setText(CharSequence)));

	before(): ignoreSetText()
	{
		Object target = thisJoinPoint.getTarget();

		if (target instanceof View) {
			// This is text being entered programmatically. Suppress from recording.
			AutomationManager.ignoreNext((View) target, AutomatorConstants.ACTION_ENTER_TEXT);
		}
	}
}