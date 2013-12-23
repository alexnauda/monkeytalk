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

import android.view.ViewGroup;

import com.gorillalogic.fonemonkey.Log;

/**
 * This automator hides layout components, eg, LinearLayout, from automation, and prevents them from
 * being included in ordinal counting.
 */
public class ViewGroupAutomator extends ViewAutomator {
	static {
		Log.log("Initializing ViewGroupAutomator");
	}

	@Override
	public Class<?> getComponentClass() {
		return ViewGroup.class;
	}

	public ViewGroup getViewGroup() {
		return (ViewGroup) getView();
	}

	@Override
	protected boolean isAutomatable() {
		String id;
		return (getViewGroup().isClickable())
				|| ((id = this.getRawMonkeyId()) != null && !id.equals(""));
	}

}