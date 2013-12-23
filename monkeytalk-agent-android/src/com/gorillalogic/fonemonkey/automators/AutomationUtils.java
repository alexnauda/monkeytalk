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

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author sstern
 *
 */
public class AutomationUtils {
	/**
	 * Synthesize a Tap gesture on the supplied view.
	 * @param view The view to tap.
	 */
	public static void tap(final View view) {
		long start = SystemClock.uptimeMillis();
		final MotionEvent down = MotionEvent.obtain(start, start,
				MotionEvent.ACTION_DOWN, view.getWidth() / 2, view
						.getHeight() / 2, 0);
		final MotionEvent up = MotionEvent.obtain(start, start + 25,
				MotionEvent.ACTION_UP, view.getWidth() / 2, view
						.getHeight() / 2, 0);
		AutomationManager.runOnUIThread(new Runnable() {

			@Override
			public void run() {
				view.dispatchTouchEvent(down);
				view.dispatchTouchEvent(up);
			}

		});
	}
}