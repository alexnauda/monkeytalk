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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Rect;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import com.gorillalogic.fonemonkey.automators.AutomationManager;

public class ActivityManager {

	static {
		AutomationManager.init();
	}
	static Map<String, Activity> activities = new HashMap<String, Activity>();
	private static Menu currentMenu;
	private static AlertDialog currentDialog;
	private static ArrayList<View> clippedViews;

	public static Menu getCurrentMenu() {
		// Log.log("Current Menu: " + getMenu() + " :" + getMenu().size());
		return getMenu();
	}

	/**
	 * Hack to open options menu (async) and return it once its open
	 * 
	 * @return
	 */
	private static Menu getMenu() {

		AutomationManager.runOnUIThread(new Runnable() {
			public void run() {
				AutomationManager.getTopActivity().openOptionsMenu();
			}
		});
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			//
		}
		return currentMenu;
	}

	public static void setCurrentMenu(Menu currentMenu) {
		ActivityManager.currentMenu = currentMenu;
	}

	static public void addActivity(Activity a) {
		activities.put(a.getComponentName().flattenToString(), a);
	}

	static public Activity getActivity(String name) {
		return activities.get(name);
	}

	/**
	 * @param d
	 */
	public static void setCurrentDialog(AlertDialog d) {
		ActivityManager.currentDialog = d;

	}

	public static AlertDialog getCurrentDialog() {
		return ActivityManager.currentDialog;

	}

	/**
	 * @param v
	 */
	public static void checkIsClipped(final View v) {
		new Thread(new Runnable() {
			public void run() {
				try {

					while (v.getDrawingTime() == 0)
						Thread.sleep(500);

					// Check if the view is clipped
					int[] location = new int[2];
					v.getLocationOnScreen(location);

					// Find top of the content
					Activity a=AutomationManager.getTopActivity();
					if (a!=null) {
						Window window = a.getWindow();
						if (window!=null) {
							View decorView = window.getDecorView();
							if (decorView!=null) {
								Rect rect = new Rect();
								decorView.getWindowVisibleDisplayFrame(rect);
								View contentView = window.findViewById(Window.ID_ANDROID_CONTENT);
								if (contentView != null) {
									int contentTop = contentView.getTop();
				
									// Get screen width and height
									Display display = a.getWindowManager().getDefaultDisplay();
									int screenWidth = display.getWidth(); // Deprecated
									int screenHeight = display.getHeight(); // Deprecated
				
									int viewXLeft = location[0];
									int viewXRight = viewXLeft + v.getWidth();
				
									int viewYTop = location[1];
									int viewYBottom = viewYTop + v.getHeight();
				
									boolean isClipped = viewXLeft < 0 || viewXRight > screenWidth
											|| viewYTop < contentTop || viewYBottom > screenHeight;
				
									if (isClipped) {
										ActivityManager.addClippedView(v);
									}
								}
							}
						}
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void addClippedView(View v) {
		if (ActivityManager.clippedViews == null)
			ActivityManager.clippedViews = new ArrayList<View>();

		ActivityManager.clippedViews.add(v);
	}

	public static List<View> getClippedViews() {
		return ActivityManager.clippedViews;
	}

	public static void clearClippedViews() {
		if (ActivityManager.clippedViews == null)
			return;

		ActivityManager.clippedViews.clear();
		ActivityManager.clippedViews = null;
	}
}
