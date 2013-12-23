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

import android.gesture.GestureOverlayView;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class GestureListener implements GestureOverlayView.OnGestureListener
{
	
	private MotionEvent start;
	private MotionEvent current;
	private boolean isFling = false;
	private boolean isScroll = false;
	private SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

		/* (non-Javadoc)
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			//Log.log("scroll " + e1.getX() + "," + e1.getY() + " + " + distanceX + "," + distanceY + " + " + e2.getX() + "," + e2.getY());
			Log.log("scroll");
			isScroll = true;
			
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		/* (non-Javadoc)
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.log("fling " + velocityX + " " + velocityY);
			isFling = true;
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
	};
	
	private GestureDetector detector = new GestureDetector(listener);
	public void onGesture(GestureOverlayView overlay, MotionEvent event)
	{
//		Log.log("Gest: " + event.getAction() + " " + event.getX() + "," + event.getY());
//		add(overlay, event);
		detector.onTouchEvent(event);
	}

	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event)
	{
		//add(overlay, event);
	}

	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event)
	{
		detector.onTouchEvent(event);
		float startX = start.getX();
		float startY = start.getY();
		float endX = event.getX();
		float endY = event.getY();
		String direction;
		Log.log("end:" + startX + "," + startY + " " + endX + "," + endY);
	
		if (isFling) {
			if (Math.abs(startX - endX) > Math.abs(startY - endY)) {
				// Vert
				if (endX > startX) {
					direction = "Right";
				} else {
					direction = "Left";
				}
			} else {
				if (startY > endY) {
					direction = "Up";
				} else {
					direction = "Down";
				}
			}
			// now for all views in TouchListener
			// AutomationManager.record(IAutomator.ACTION_SWIPE, overlay, new String[] {direction});
		} else if (isScroll) {
			// now for all views in TouchListener
			// AutomationManager.record(IAutomator.ACTION_DRAG, overlay, new String[] {Integer.toString((int) startX), Integer.toString((int) startY), Integer.toString((int) endX), Integer.toString((int) endY)});
			
		}
		isScroll = false;
		isFling = false;
		//add(overlay, event);
	}

	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event)
	{
		isFling = false;
		isScroll = false;
		start = event.obtain(event);
		detector.onTouchEvent(event);
		//		detector.onTouchEvent(event);add(overlay, event);
	}

//	void add(GestureOverlayView overlay, MotionEvent event)
//	{
//		if (!AutomationManager.record(Operation.GestureMotion.toString(), new Object[] { overlay, event })) {
//			Log.log("Event not handled: " + getClass().getName());
//		}
//	}
}
