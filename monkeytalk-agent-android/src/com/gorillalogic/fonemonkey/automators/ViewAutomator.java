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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.TouchListener;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * @author sstern
 * 
 */
public class ViewAutomator extends AutomatorBase implements OnTouchListener, /* OnClickListener, */
OnLongClickListener {

	private Map<Integer, String> idMap = null;

	protected Object component;
	protected String monkeyId;

	static String componentType = AutomatorConstants.TYPE_VIEW;
	private static Class<?> componentClass = View.class;
	static {
		Log.log("Initializing ViewAutomator");
		AutomationManager.registerClass(componentType, componentClass, ViewAutomator.class);
	}

	/**
	 * You must set the target component
	 * 
	 * @param v
	 */
	public void setComponent(Object o) {
		component = o;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getMonkeyID() {
		if (monkeyId == null) {
			monkeyId = getDefaultMonkeyID();
		}
		return monkeyId;
	}

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public Object getComponent() {
		return component;
	}

	public View getView() {
		return (View) getComponent();
	}

	public List<String> getRawMonkeyIdCandidates() {
		List<String> list = new ArrayList<String>();
		View v = getView();
		if (v == null) {
			return list;
		}
		CharSequence desc = v.getContentDescription();
		if (desc != null) {
			list.add(desc.toString());
		}
		String id = getId();
		if (id != null) {
			list.add(id);
		}
		Object tag = v.getTag();
		if (tag != null && tag instanceof String) {
			list.add(tag.toString());
		}

		return list;
	}

	public List<String> getIdentifyingValues() {
		List<String> list = super.getIdentifyingValues();
		list.addAll(this.getRawMonkeyIdCandidates());
		return list;
	}

	@Override
	public boolean canAutomate(String componentType, String monkeyId) {
		return getView().isShown() && super.canAutomate(componentType, monkeyId);
	}

	protected String getDefaultMonkeyID() {
		View v = getView();
		if (v == null) {
			return "";
		}
		String id = getRawMonkeyId();
		if (id != null) {
			return id;
		}
		String o = formatOrdinalFor(v);
		return o == null ? "" : o;
	}

	protected String getRawMonkeyId() {
		List<String> candidates = getRawMonkeyIdCandidates();
		for (String candidate : candidates) {
			if (candidate != null && candidate.trim().length() > 0) {
				return candidate;
			}
		}
		return null;
	}

	private boolean ordInit = false;
	private int ordinal;

	@Override
	public int getOrdinal() {
		if (!ordInit || ordinal == -1) {
			ordinal = findOrdinalFor(this.getView());
			ordInit = true;
		}
		return ordinal;
	}

	static String formatOrdinalFor(View v) {
		int index = findOrdinalFor(v);
		return index > 1 ? "#" + String.valueOf(index) : null;
	}

	static int findOrdinalFor(View v) {
		IAutomator ia =  AutomationManager.findAutomator(v);
		if (ia != null && !(ia instanceof ViewAutomator)) {
			return -1;
		}
		ViewAutomator auto = (ViewAutomator) ia;
		if (!auto.isAutomatable()) {
			return -1;
		}

		Class<?> targetComponentType = auto.getComponentClass();

		return findOrdinalFor(v, targetComponentType);
	}

	public static int findOrdinalFor(View target, Class<?> targetComponentType) {
		int[] ordinal = new int[] { 0 };
		for (View r : AutomationManager.getRoots()) {
			if (r instanceof ViewGroup) {
				ViewGroup root = (ViewGroup) r;
				if (_findOrdinalFor(root, target, targetComponentType, ordinal)) {
					return ordinal[0] + 1;
				}
			}
		}
		return -1;
	}

	public static boolean _findOrdinalFor(ViewGroup root, View target,
			Class<?> targetComponentType, int[] ordinal) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			View v = root.getChildAt(i);

			ViewAutomator auto = (ViewAutomator) AutomationManager.findAutomator(v);
			if (!v.isShown() || auto.isHiddenByParent()) {
				continue;
			}

			if (v.equals(target))
				return true;

			// if (target.getClass().isInstance(v))
			if (auto.getComponentClass().equals(targetComponentType)) {
				if (auto.isAutomatable()) {
					ordinal[0]++;
				}
			}

			if (v instanceof ViewGroup) {
				if (_findOrdinalFor((ViewGroup) v, target, targetComponentType, ordinal))
					return true;
			}
		}

		return false;
	}

	/**
	 * Return false if this component is hidden from automation
	 * 
	 * @return
	 */

	protected boolean isAutomatable() {
		return true;
	}

	@Override
	public String play(final String action, final String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_TAP)) {

			AutomationManager.runOnUIThread(new Runnable() {

				public void run() {

					if (args.length == 2) {
						int x = getIntegerArg(action, args[0], 0);
						int y = getIntegerArg(action, args[1], 1);
						tap(x, y);
					} else {
						tap();
					}
				}
			});

			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_LONG_PRESS)) {
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					getView().performLongClick();
				}
			});
			return null;
		}

		// if (action.equalsIgnoreCase(AutomatorConstants.ACTION_GET)) {
		// assertArgCount(AutomatorConstants.ACTION_GET, args, 1);
		// if (args.length == 1) {
		// return getValue();
		// } else {
		// return getValue(args[1]);
		// }
		// }

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_DRAG)) {
			if (args.length < 4) {
				throw new IllegalArgumentException(action
						+ " requires at least 4 arguments. Found " + args.length);
			}
			int[] points = new int[args.length];
			int i = 0;
			for (String arg : args) {
				try {
					points[i++] = Integer.valueOf(arg);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(AutomatorConstants.ACTION_DRAG
							+ " requires numeric arguments. Found " + arg);
				}
			}
			drag(points);
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_PINCH)) {

			if (args.length < 1 || args[0].length() < 1) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_PINCH
						+ " requires at least 1 argument numeric argument");
			}
			float arg;
			try {
				arg = Float.valueOf(args[0]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_PINCH
						+ " requires numeric arguments. Found " + args[0]);
			}
			pinch(arg);
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SWIPE)) {
			if (args.length < 1 || args[0].length() < 1) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_SWIPE
						+ " requires at least 1 argument: Down, Up, Left, or Right");
			}
			swipe(args[0]);
			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.TOUCH_DOWN)) {
			touch(0, args);
			return null;
		} else if (action.equalsIgnoreCase(AutomatorConstants.TOUCH_UP)) {
			touch(1, args);
			return null;
		} else if (action.equalsIgnoreCase(AutomatorConstants.TOUCH_MOVE)) {
			touch(2, args);
			return null;
		}

		return super.play(action, args);
	}

	protected void tap() {
		tap(getView().getWidth() / 2, getView().getHeight() / 2);
	}

	private static int PADDING = 50;

	// private static Object syncObject = new Object();
	protected void tap(int x, int y) {
		tap(x, y, 25);
	}

	protected void tap(int x, int y, int duration) {
		View v = getView();
		int dx = 0;
		int dy = 0;
		int top = v.getScrollY();
		int bottom = top + v.getHeight();
		int left = v.getScrollX();
		int right = left + v.getWidth();
		if (y < top) {
			dy = y - (top + PADDING);
		} else if (y > bottom) {
			dy = y - (bottom - PADDING);
		}
		if (x < left) {
			dx = x - (left + PADDING);
		} else if (x > right) {
			dx = x - (right - PADDING);
		}

		if (dx != 0 || dy != 0) {
			v.scrollTo(left + dx, top + dy);
			left = v.getScrollX();
			top = v.getScrollY();
		}

		long start = SystemClock.uptimeMillis();
		final MotionEvent down = MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN,
				x - left, y - top, 0);

		final MotionEvent up = MotionEvent.obtain(start, start + duration, MotionEvent.ACTION_UP, x
				- left, y - top, 0);

		// Can't sleep on UIThread to wait for ACTION_UP event, sandwich TouchEvents with a
		// Thread.sleep in between to allow for long presses.
		AutomationManager.runOnUIThread(new Runnable() {

			@Override
			public void run() {
				ViewAutomator.this.getView().dispatchTouchEvent(down);
			}

		});

		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			// do nothing
		}

		AutomationManager.runOnUIThread(new Runnable() {

			@Override
			public void run() {
				ViewAutomator.this.getView().dispatchTouchEvent(up);
			}

		});

	}

	protected void enterText(final String s) {
		enterText(s, false);
	}

	// KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
	final static KeyCharacterMap characterMap = KeyCharacterMap
			.load(KeyCharacterMap.BUILT_IN_KEYBOARD);

	protected void enterText(final String s, final boolean hitDone) {

		AutomationManager.runOnUIThread(new Runnable() {
			public void run() {
				KeyEvent[] keys = characterMap.getEvents(s.toCharArray());
				if (keys == null) {
					Log.log("Unable to find chars for \"" + s + "\" in builtin keyboard keymap");
					// special characters not found in charactermap, so
					// outputting via zapping rather than simulating keyboard
					if (getView() instanceof TextView) {
						((TextView) getView()).setText(s);
					}
				} else {
					for (KeyEvent key : keys) {
						getView().dispatchKeyEvent(key);
					}
				}
				if (hitDone) {
					KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
					getView().dispatchKeyEvent(down);
					KeyEvent up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
					getView().dispatchKeyEvent(up);
					InputMethodManager imm = (InputMethodManager) getView().getContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
				}

			}
		});
	}

	private void drag(final int... points) {

		AutomationManager.runOnUIThread(new Runnable() {

			@Override
			public void run() {
				long start = SystemClock.uptimeMillis();
				MotionEvent down = MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN,
						points[0], points[1], 0);
				ViewAutomator.this.getView().dispatchTouchEvent(down);
				for (int i = 0; i < points.length; i += 2) {
					MotionEvent move = MotionEvent.obtain(start, start + i * 100,
							MotionEvent.ACTION_MOVE, points[i], points[i + 1], 0);
					ViewAutomator.this.getView().dispatchTouchEvent(move);
				}
				MotionEvent up = MotionEvent.obtain(start, start + points.length * 100,
						MotionEvent.ACTION_UP, points[points.length - 2],
						points[points.length - 1], 0);

				ViewAutomator.this.getView().dispatchTouchEvent(up);
			}

		});
	}

	private void swipe(String direction) {
		int startX, startY, endX, endY;
		boolean isUp = direction.equalsIgnoreCase("up");
		boolean isVertical = isUp || direction.equalsIgnoreCase("down");
		if (isVertical) {
			startX = this.getView().getWidth() / 2;
			endX = startX;
			if (isUp) {
				startY = this.getView().getHeight() - 1;
				endY = 1;
			} else {
				startY = 1;
				endY = this.getView().getHeight() - 1;
			}
		} else {
			startY = this.getView().getHeight() / 2;
			endY = startY;
			if (direction.equalsIgnoreCase("left")) {
				startX = this.getView().getWidth() - 1;
				endX = 1;
			} else {
				startX = 1;
				endX = this.getView().getWidth() - 1;
				;

			}
		}

		long start = SystemClock.uptimeMillis();
		final MotionEvent down = MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN, startX,
				startY, 0);
		final MotionEvent move = MotionEvent.obtain(start, start + 500, MotionEvent.ACTION_MOVE,
				(startX + endX) / 2, (startY + endY) / 2, 0);
		// By making the time between the MOVE and UP events short (start+999 vs
		// start+1000), we simulate a "fling"
		final MotionEvent move1 = MotionEvent.obtain(start, start + 999, MotionEvent.ACTION_MOVE,
				endX, endY, 0);
		final MotionEvent up = MotionEvent.obtain(start, start + 1000, MotionEvent.ACTION_UP, endX,
				endY, 0);
		AutomationManager.runOnUIThread(new Runnable() {

			@Override
			public void run() {
				dispatchEvent(down);
				dispatchEvent(move);
				dispatchEvent(move1);
				dispatchEvent(up);
			}

		});
	}

	public void dispatchEvent(final MotionEvent event) {
		ViewAutomator.this.getView().dispatchTouchEvent(event);
		// ViewAutomator.this.getView().onTouchEvent(event);
	}

	// Used by WebViewAutomator and the ScrollerAutomator.
	protected void scroll(int x, int y) {
		View view = getView();
		view.scrollBy(x, y);
	}

	// Android doesn't recognize pinches starting to close to the edge (what sdk calls "edgeSlop")
	// We adjust accordingly. This value may need to be dynamic. Currently hardcoding to 50.
	private static int edgeSlop = 50;

	public void pinch(final float scale) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			throw new IllegalArgumentException(
					"Pinch playback not supported for Android "
							+ Build.VERSION.RELEASE
							+ ". MonkeyTalk only supports pinch playback for Android 2.3 Gingerbread and later.");
		}
		AutomationManager.runOnUIThread(new Runnable() {

			@Override
			public void run() {
				int width = getView().getWidth();
				int slopWidth = width - edgeSlop;
				int startDist = (int) (scale < 1 ? slopWidth : slopWidth / scale);
				int endDist = (int) (scale < 1 ? (slopWidth) * scale : slopWidth);
				int zoomDir = startDist - endDist > 0 ? 1 : -1;
				int y = getView().getHeight() / 2;
				int x = width / 2;
				int startX1 = x + startDist / 2;
				int startX2 = x - startDist / 2;
				int endX1 = x + endDist / 2;
				int endX2 = x - endDist / 2;
				long downTime = SystemClock.uptimeMillis();
				int pointers = 2;
				int[] pointerIds = { 1, 0 };
				int metaState = 0;
				int xPrecision = 1;
				int yPrecision = 1;
				int deviceId = 0;
				int edgeFlags = 0;
				int pressure = 1;
				int size = 1;
				int source = 0;
				int flags = 0;
				Log.log("pinch " + startX1 + " " + startX2 + " " + endX1 + " " + endX2);
				// try {
				// Method method;
				// method = MotionEvent.class.getMethod("obtainNano", long.class, long.class,
				// long.class,
				// int.class, int.class, int[].class, float[].class, int.class,
				// float.class, float.class, int.class, int.class);
				//
				// } catch (NoSuchMethodException e) {
				// Method method = MotionEvent.class.getMethod("obtain", long.class, long.class,
				// int.class, int.class, int[].class, PointerCoords[].class, int.class,
				// float.class, float.class, int.class, int.class, int.class, int.class);
				// }
				MotionEvent event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN,
						1, startX1, y, pressure, size, metaState, xPrecision, yPrecision, deviceId,
						edgeFlags);
				dispatchEvent(event);

				PointerCoords p1 = new PointerCoords();
				p1.x = startX1;
				p1.pressure = 1;
				p1.y = y;
				PointerCoords p2 = new PointerCoords();
				p2.x = startX2;
				p2.pressure = 1;
				p2.y = y;
				PointerCoords[] pointerCoords = { p2, p1 };
				event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_POINTER_1_DOWN,
						pointers, pointerIds, pointerCoords, metaState, 1, 1, deviceId, edgeFlags,
						source, flags);
				dispatchEvent(event);

				p1 = new PointerCoords();
				p1.pressure = 1;
				p1.x = startX1 - zoomDir * edgeSlop;
				p1.y = y;
				p2 = new PointerCoords();
				p2.x = startX2 + zoomDir * edgeSlop;
				p2.pressure = 1;
				p2.y = y;
				pointerCoords = new PointerCoords[] { p2, p1 };
				event = MotionEvent.obtain(downTime, downTime + 1000, MotionEvent.ACTION_MOVE,
						pointers, pointerIds, pointerCoords, metaState, 1, 1, deviceId, edgeFlags,
						source, flags);
				dispatchEvent(event);

				p1 = new PointerCoords();
				p1.pressure = 1;
				p1.x = endX1;
				p1.y = y;
				p2 = new PointerCoords();
				p2.x = endX2;
				p2.pressure = 1;
				p2.y = y;
				pointerCoords = new PointerCoords[] { p2, p1 };
				event = MotionEvent.obtain(downTime, downTime + 1000, MotionEvent.ACTION_MOVE,
						pointers, pointerIds, pointerCoords, metaState, 1, 1, deviceId, edgeFlags,
						source, flags);
				dispatchEvent(event);
				event = MotionEvent.obtain(downTime, downTime + 1000,
						MotionEvent.ACTION_POINTER_1_UP, pointers, pointerIds, pointerCoords,
						metaState, 1, 1, deviceId, edgeFlags, source, flags);
				dispatchEvent(event);

				event = MotionEvent.obtain(downTime, downTime + 1000, MotionEvent.ACTION_UP, 1,
						endX1, y, pressure, size, metaState, xPrecision, yPrecision, deviceId,
						edgeFlags);
				dispatchEvent(event);
			}
		});
	}

	private static long __downTime = SystemClock.uptimeMillis();

	private void touch(int actionInt, String... args) {
		float x = Float.parseFloat(args[0]);
		float y = Float.parseFloat(args[1]);
		touch(actionInt, x, y);
	}

	private void touch(int actionInt, float x, float y) {
		if (actionInt == 0) {
			ViewAutomator.__downTime = SystemClock.uptimeMillis();
		}
		long eventTime = System.currentTimeMillis();
		final MotionEvent event = MotionEvent.obtain(ViewAutomator.__downTime, eventTime,
				actionInt, x, y, 0);
		AutomationManager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				ViewAutomator.this.getView().dispatchTouchEvent(event);
			}
		});
	}

	@Override
	public boolean installDefaultListeners() {
		if (isHiddenByParent()) {
			return true;
		}
		boolean didInstall = super.installDefaultListeners();

		// // manage touch listeners
		// if (!hasOnTouchListener(v)) {
		// Log.log("ViewAutomator::installDefaultListeners - View "
		// + v
		// +
		// " does not seem to have our onTouchListener installed -- installing...");
		// v.setOnTouchListener(new TouchListener());
		// didInstall = true;
		// } else {
		// logWarn("OnTouchListener", v);
		// }
		return didInstall;
	}

	//
	// private static boolean hasOnTouchListener(View v) {
	// // We need to get a handle to the actual parent View
	// Class klass = v.getClass();
	// while (!klass.equals(View.class)) {
	// klass = klass.getSuperclass();
	// }
	//
	// try {
	// // Obviously this is not the best way to test if a listener
	// // exists. But it's all we've got ...
	// Field f = klass.getDeclaredField("mOnTouchListener");
	// f.setAccessible(true);
	// return f.get(v) != null;
	// } catch (Exception e) {
	// Log.log(e);
	// }
	// return false;
	// }

	private static String getDirectionFromFlick(Object[] args) {
		String direction = "left";
		try {
			int startX = Integer.parseInt((String) args[0]);
			int startY = Integer.parseInt((String) args[1]);
			int endX = Integer.parseInt((String) args[2]);
			int endY = Integer.parseInt((String) args[3]);

			int deltaX = endX - startX;
			int deltaY = endY - startY;
			if (deltaX < 0 && deltaY < 0) {
				// left or down
				if (deltaX < deltaY) {
					direction = "Left";
				} else {
					direction = "Up";
				}
			}
			if (deltaX < 0 && deltaY >= 0) {
				// left or up
				if (deltaX < (0 - deltaY)) {
					direction = "Left";
				} else {
					direction = "Down";
				}
			}
			if (deltaX >= 0 && deltaY < 0) {
				// right or down
				if (deltaX > (0 - deltaY)) {
					direction = "Right";
				} else {
					direction = "Up";
				}
			}
			if (deltaX >= 0 && deltaY >= 00) {
				// right or up
				if (deltaX > deltaY) {
					direction = "Right";
				} else {
					direction = "Down";
				}
			}

		} catch (NumberFormatException e) {
			Log.log("getDirectionFromFlick(): " + e.getMessage());
		}
		return direction;
	}

	/**
	 * @return true if hidden from recording by a parent (composite component).
	 */
	public boolean isHiddenByParent() {
		// Log.log("Checking if " + view + " is hidden");
		return _isHiddenByParent(getView(), getView().getParent());

	}

	private boolean _isHiddenByParent(View view, ViewParent parent) {
		// Log.log("Checking if " + view + " is hidden by " + parent);
		if (parent == null) {
			// Log.log(view + " is not hidden");
			return false;
		}
		IAutomator automator = AutomationManager.findAutomator(parent);
		if (automator == null) {
			return false;
		}
		if (automator.hides(view)) {
			return true;
		}
		// Log.log("Found automator " + automator);
		return (automator == null) ? false : _isHiddenByParent(view, parent.getParent());

	}

	@Override
	public void record(String operation, String... args) {
		if (!isAutomatable()) {
			return;
		}
		if (operation.equals(AutomatorConstants.ACTION_SWIPE)) {
			if (args.length < 1) {
				throw new IllegalArgumentException("SWIPE action must have at least one argument");
			}
			String direction = null;
			if (Character.isDigit(((String) args[0]).charAt(0))) {
				direction = ViewAutomator.getDirectionFromFlick(args);
			} else {
				direction = (String) args[0];
			}
			String[] sargs = { direction };
			super.record(operation, sargs);
		} else {
			super.record(operation, args);
		}
	}

	/**
	 * Chains listeners such that the automtor's implementation of the supplied listener will be
	 * called before the listener associated with the associted view. The automator must implement
	 * the supplied interface. Assumes listener name is of the form OnXxxListener. Also assumes
	 * there is a setter on the associated view called setOnXxxListener, and a private field called
	 * mOnXxxListener.
	 * 
	 * @param c
	 */
	// protected void chainListenerFor(Class<?> c) {
	//
	// String listenerClass = c.getName();
	// Matcher m = onListener.matcher(listenerClass);
	// m.find();
	// String listenerName = "On" + m.group(1) + "Listener";
	// String listenerField = "m" + listenerName;
	// chainListenerFor(c, listenerName);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		TouchListener.handleMotionEvent(v, event);
		return false;
	}

	protected String getId() {
		return getFieldMap().get(getView().getId());
	}

	public Map<Integer, String> getFieldMap() {
		if (idMap != null) {
			return idMap;
		}

		idMap = new HashMap<Integer, String>();
		Class<?> r;
		String rClass = this.getView().getContext().getApplicationContext().getPackageName()
				+ ".R$id";
		try {
			r = Class.forName(rClass);
		} catch (ClassNotFoundException e1) {
			Log.log("Unable to load " + rClass + ": " + e1.getMessage());
			return idMap;
		}
		for (Field f : r.getFields()) {
			int val;
			try {
				val = f.getInt(null);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to get value for " + f.getName() + ": "
						+ e.getMessage());
			}
			idMap.put(val, f.getName());

		}

		return idMap;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	// @Override
	// public void onClick(View v) {
	// AutomationManager.record(AutomatorConstants.ACTION_TAP, getView(), (String[]) null);
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
	 */
	@Override
	public boolean onLongClick(View v) {
		AutomationManager.record(AutomatorConstants.ACTION_LONG_PRESS, getView());
		return false;
	}

	@Override
	protected Rect getBoundingRectangle() {
		View v = getView();
		if (v == null) {
			return super.getBoundingRectangle();
		}
		// Log.log("getBoundingRectangle() for view type=" + v.getClass().getSimpleName()
		// + " left=" + v.getLeft()
		// + " top=" + v.getTop()
		// + " width=" + v.getWidth()
		// + " height=" + v.getHeight()
		// + " measuredWidth=" + v.getMeasuredWidth()
		// + " measuredHeight=" + v.getMeasuredHeight()
		// + " paddingTop=" + v.getPaddingTop()
		// + " paddingLeft=" + v.getPaddingLeft()
		// + " paddingRight=" + v.getPaddingRight()
		// + " paddingBottom=" + v.getPaddingBottom()
		// );
		int w = v.getWidth();
		int h = v.getHeight();

		int x = v.getLeft();
		int y = v.getTop();
		ViewParent parent = v.getParent();
		while (parent != null) {
			if (parent instanceof View) {
				View pv = (View) parent;
				// Log.log("getBoundingRectangle() for parent view type=" +
				// pv.getClass().getSimpleName()
				// + " left=" + pv.getLeft()
				// + " top=" + pv.getTop()
				// + " width=" + pv.getWidth()
				// + " height=" + pv.getHeight()
				// + " measuredWidth=" + pv.getMeasuredWidth()
				// + " measuredHeight=" + pv.getMeasuredHeight()
				// + " paddingTop=" + pv.getPaddingTop()
				// + " paddingLeft=" + pv.getPaddingLeft()
				// + " paddingRight=" + pv.getPaddingRight()
				// + " paddingBottom=" + pv.getPaddingBottom()
				// );
				x += pv.getLeft();
				y += pv.getTop();
			}
			parent = parent.getParent();
		}

		// x+=v.getPaddingLeft();
		// y+=v.getPaddingTop();
		// w-=(v.getPaddingLeft()+v.getPaddingRight());
		// h-=(v.getPaddingTop()+v.getPaddingBottom());

		return new Rect(x, y, x + w, y + h);
	}
}