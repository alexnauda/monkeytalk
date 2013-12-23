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
package com.gorillalogic.monkeytalk.automators;

import java.util.HashMap;
import java.util.Map;

public class ActionFilter {
	private Map<String, Boolean> actionMap = new HashMap<String, Boolean>();

	public ActionFilter() {
		set(AutomatorConstants.ACTION_TAP, true);
		set(AutomatorConstants.ACTION_ENTER_TEXT, true);
		set(AutomatorConstants.ACTION_SELECT_INDEX, true);
		set(AutomatorConstants.ACTION_SELECT, true);
		set(AutomatorConstants.ACTION_LONG_SELECT, true);
		set(AutomatorConstants.ACTION_LONG_SELECT_INDEX, true);
		set(AutomatorConstants.ACTION_ENTER_DATE, true);
		set(AutomatorConstants.ACTION_BACK, true);
		set(AutomatorConstants.ACTION_LONG_PRESS, true);
		set(AutomatorConstants.ACTION_GET, true);
		set(AutomatorConstants.ACTION_VERIFY, true);
		set(AutomatorConstants.ACTION_VERIFY_NOT, true);
		set(AutomatorConstants.ACTION_SWIPE, true);
		set(AutomatorConstants.ACTION_DRAG, true);

		set(AutomatorConstants.TOUCH_UP, true);
		set(AutomatorConstants.TOUCH_DOWN, true);
		set(AutomatorConstants.TOUCH_MOVE, true);
	}

	public void set(String action, boolean value) {
		if (action != null && action.length() > 0) {
			actionMap.put(action, value);
		}
	}

	/**
	 * Get the filter setting for the given action, or true if none exists.
	 * 
	 * @param action
	 *            the action name
	 * @return the filter setting, or true if none exists
	 */
	public boolean get(String action) {
		return actionMap.containsKey(action) ? actionMap.get(action) : true;
	}
}