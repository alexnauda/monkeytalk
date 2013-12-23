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

import android.widget.ListView;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class ListViewAutomator extends AdapterViewAutomator {

	private static Class<?> componentClass = ListView.class;

	static {
		Log.log("Initializing ListViewAutomator");
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_TABLE;
	}

	private ListView getListView() {
		return (ListView) getComponent();
	}

	@Override
	public String play(String action, String... args) {
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SCROLL_TO_ROW)) {
			assertArgCount(action, args, 1);
			int row = getIndexArg(action, args[0]);

			int max = getListView().getCount();
			assertMaxArg(row, max);

			scrollToPosition(row - 1);
			return null;
		}
		return super.play(action, args);
	}

	/** Use smooth scrolling to make things look cool. */
	@Override
	protected void scrollToPosition(final int position) {
		AutomationManager.runOnUIThread(new Runnable() {
			public void run() {
				getListView().setSmoothScrollbarEnabled(true);
				getListView().smoothScrollToPosition(position);
			}
		});

		// loop while we wait for the scroll to complete
		for (int j = 0; j < 100; j++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// ignore
			}

			if (position >= getListView().getFirstVisiblePosition()
					&& position <= getListView().getLastVisiblePosition()) {

				// sleep some extra time to guarantee we are fully scrolled
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					// ignore
				}
				break;
			}
		}
	}
}
