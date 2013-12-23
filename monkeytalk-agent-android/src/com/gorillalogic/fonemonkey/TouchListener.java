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
package com.gorillalogic.fonemonkey;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class TouchListener implements View.OnTouchListener {
	private static ArrayList<String> points = new ArrayList<String>();
	private static boolean pinching;

	public boolean onTouch(View v, MotionEvent event) {
		TouchListener.handleMotionEvent(v, event);
		return false;
	}

	public static boolean handleMotionEvent(View v, MotionEvent event) {
		if (!detectGestures) {
			Log.log("gesture detection suppressed...");
		} else {
			gestureTargetView = v;
			getGestureDetector().onTouchEvent(event);
			getScaleDetector().onTouchEvent(event);
		}

		boolean rez = false;
		int actionInt = event.getActionMasked();
		String action = null;
		switch (actionInt) {
		case MotionEvent.ACTION_DOWN:
			//Log.log("Down! " + event.getX() + "," + event.getY());
			action = AutomatorConstants.TOUCH_DOWN;
			points.clear();

			points.add(intVal(event.getX()));
			points.add(intVal(event.getY()));
			break;
		case MotionEvent.ACTION_UP:
			//Log.log("Up! " + event.getX() + "," + event.getY());
			if (points.size() == 0) {
				break;
			}
			if (pinching) {

				pinching = false;
				break;
			} else {

				// points array is array of strings representing alternating x y coords.
				if (points.size() == 2) {
					// It's a single point
					action = AutomatorConstants.ACTION_TAP;
					AutomationManager.record(action, gestureTargetView,
							(String[]) points.toArray(new String[] {}));
				} else if (points.size() > 2) {
					// It's multiple points
					action = AutomatorConstants.ACTION_DRAG;
					AutomationManager.record(action, gestureTargetView,
							(String[]) points.toArray(new String[] {}));
				}

			}
			action = AutomatorConstants.TOUCH_UP;
			break;
		case MotionEvent.ACTION_MOVE:
			//Log.log("Move!");
			action = AutomatorConstants.TOUCH_MOVE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			// Pinch
			//Log.log("Pointer Down! " + event.getX(1) + "," + event.getY(1));
			pinching = true;
			points.add(0, intVal(event.getX(1)));
			points.add(1, intVal(event.getY(1)));
			break;
		case MotionEvent.ACTION_POINTER_UP:
			// Pinch
			//Log.log("Pointer Up! " + event.getX(1) + "," + event.getY(1));
			points.add(intVal(event.getX(1)));
			points.add(intVal(event.getY(1)));
			break;
		}

		return rez;
	}

	private static String intVal(float x) {
		return String.valueOf(Math.round(x));
	}

	public static boolean detectGestures = true;
	private static GestureDetector gestureDetector = null;
	private static ScaleGestureDetector scaleDetector = null;

	public static GestureDetector getGestureDetector() {
		if (gestureDetector == null) {
			gestureDetector = new GestureDetector(getOnGestureListener());
		}
		return gestureDetector;
	}

	public static ScaleGestureDetector getScaleDetector() {
		if (scaleDetector == null) {
			scaleDetector = new ScaleGestureDetector(gestureTargetView.getContext(),
					getOnScaleListener());
		}
		return scaleDetector;
	}

	private static class OnGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (!pinching) {
				String endX = Integer.toString((int) (e2.getX()));
				String endY = Integer.toString((int) (e2.getY()));
				points.add(endX);
				points.add(endY);
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

	}

	private static DecimalFormat dec1 = new DecimalFormat("0.0");

	private static class OnScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			//Log.log("Scale ended");
			float scale = detector.getScaleFactor();
			AutomationManager.record(AutomatorConstants.ACTION_PINCH, gestureTargetView,
					dec1.format(scale));
			super.onScaleEnd(detector);
		}

	}

	private static OnGestureListener onGestureListener = null;
	private static OnScaleListener onScaleListener = null;

	private static View gestureTargetView = null;

	public static OnGestureListener getOnGestureListener() {
		if (onGestureListener == null) {
			onGestureListener = new OnGestureListener();
		}
		return onGestureListener;
	}

	public static OnScaleListener getOnScaleListener() {
		if (onScaleListener == null) {
			onScaleListener = new OnScaleListener();
		}
		return onScaleListener;
	}
}
