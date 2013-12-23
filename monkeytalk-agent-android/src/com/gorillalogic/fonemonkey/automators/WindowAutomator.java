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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.Window.Callback;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;

import com.gorillalogic.fonemonkey.ActivityManager;
import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * 
 * Provides recording for Window.Callback stuff, including Menu handling, and Back button handling.
 */
public class WindowAutomator extends AutomatorBase implements Callback {
	private static WeakHashMap<Window, Object> windows = new WeakHashMap<Window, Object>();

	static {
		Log.log("Initializing WindowAutomator");
	}

	private class MonkeyWindowHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Log.log("invoking " + method.getName());
			Object retVal = null;
			try {
				retVal = method.invoke(WindowAutomator.this, args);
			} catch (AbstractMethodError e) {
				// Additional methods were added to Window.Callback and these methods are sometimes
				// called by Android Monkey Exerciser.
				// It's not clear that in real life these methods would actually ever be called, so
				// for now we just eat the error.
				Log.log("Window.Callback method called that is only supported in later version of Android SDK than one used for building MonkeyTalk");
			}
			if (listener == null) {
				return retVal;
			}
			return method.invoke(listener, args);
		}
	}

	private Window.Callback listener = null;

	static OrientationEventListener oListener;

	public WindowAutomator(Window w) {
		w.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		w.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		w.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setComponent(w);
		try {
			Field f = Window.class.getDeclaredField("mCallback");
			f.setAccessible(true);
			listener = (Callback) f.get(getComponent());
			if (listener instanceof Activity) {
				Window.Callback proxy = (Callback) Proxy.newProxyInstance(this.getClass()
						.getClassLoader(), new Class<?>[] { Window.Callback.class },
						new MonkeyWindowHandler());
				w.setCallback(proxy);
			}

		} catch (Exception e) {
			throw new IllegalStateException("Unable to access mCallback field on window");
		}

		// if (isTopLevelWindow()) {
		// //Doesn't work on emulator! Might work on physical device.
		// oListener = new OrientationEventListener(w.getContext(),
		// SensorManager.SENSOR_DELAY_NORMAL) {
		//
		// @Override
		// public void onOrientationChanged(int orientation) {
		// Log.log("Orientation changed: " + orientation);
		//
		// }
		// };
		// oListener.enable();
		// }

	}

	public Window getWindow() {
		return (Window) getComponent();
	}

	private boolean isTopLevelWindow() {
		return getWindow().getContainer() == null;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			AutomationManager.record(AutomatorConstants.ACTION_BACK, null);
		} else if (event.getAction() == KeyEvent.ACTION_UP
				&& event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			if (isTopLevelWindow()) {
				AutomationManager.record(AutomatorConstants.ACTION_MENU, null);
			}
		}
		if (listener == null) {
			return getWindow().superDispatchKeyEvent(event);
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (listener == null) {
			return getWindow().superDispatchTouchEvent(event);
		}
		return false;
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		if (listener == null) {
			return getWindow().superDispatchTrackballEvent(event);
		}
		return false;
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View onCreatePanelView(int featureId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		ActivityManager.setCurrentMenu(menu);
		return false;
	}

	@Override
	public boolean onPreparePanel(int featureId, View view, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		ActivityManager.setCurrentMenu(menu);
		return false;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AutomationManager.record(AutomatorConstants.ACTION_SELECT, item, new String[0]);
		return false;
	}

	@Override
	public void onWindowAttributesChanged(LayoutParams attrs) {
	}

	@Override
	public void onContentChanged() {
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	}

	@Override
	public void onAttachedToWindow() {
	}

	@Override
	public void onDetachedFromWindow() {
	}

	@Override
	public void onPanelClosed(int featureId, Menu menu) {
	}

	// Presumably would be where we'd handle Search button recording.
	@Override
	public boolean onSearchRequested() {
		return false;
	}

	@Override
	public String getComponentType() {
		return null;
	}

	/**
	 * @param window
	 */
	public static void automate(Window window) {
		if (windows.containsKey(window)) {
			return;
		}
		windows.put(window, (Object) null);
		// Wrap with an event handler
		new WindowAutomator(window);
	}
}