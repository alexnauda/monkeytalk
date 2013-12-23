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

import android.gesture.GestureOverlayView;
import android.view.MotionEvent;
import android.view.View;

import com.gorillalogic.fonemonkey.GestureListener;
import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.Operation;

/**
 * @author spterry
 * 
 */
public class GestureAutomator extends ViewAutomator {
	private static String componentType = "GestureOverlay";
	private static Class<?> componentClass = GestureOverlayView.class;
	static {
		Log.log("Initializing Gesture automator");
	}

	public GestureOverlayView getGestureOverlay() {
		return (GestureOverlayView) getComponent();
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public String play(String action, String... args) {
		if (action.equals(Operation.GestureMotion.toString())) {

			super.play(Operation.MotionEvent.toString(), args);
			return null;
		}

		return super.play(action, args);
	}

	@Override
	public void record(String action, Object[] args) {
		if (action.equals(Operation.GestureMotion.toString())) {

			MotionEvent motion = MotionEvent.obtainNoHistory((MotionEvent) args[1]);

			record(Operation.GestureMotion.toString(), String.valueOf(motion.getX()),
					String.valueOf(motion.getY()), String.valueOf(motion.getAction()),
					String.valueOf(motion.getMetaState()), String.valueOf(motion.getDownTime()),
					String.valueOf(motion.getEventTime()));
			return;
		}

		super.record(action, args);
	}

	@Override
	public boolean installDefaultListeners() {
		View v = getView();
		boolean didInstall = super.installDefaultListeners();

		Log.log("adding default GestureListener to component: " + v);
		((GestureOverlayView) v).addOnGestureListener(new GestureListener());
		didInstall = true;

		return didInstall;
	}

}
