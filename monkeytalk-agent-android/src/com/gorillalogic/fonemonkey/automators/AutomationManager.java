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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;

import com.gorillalogic.fonemonkey.ActivityManager;
import com.gorillalogic.fonemonkey.FunctionalityAdder;
import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.PropertyUtil;
import com.gorillalogic.fonemonkey.Recorder;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyScriptFailure;
import com.gorillalogic.fonemonkey.web.HtmlElement;
import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class AutomationManager {
	static boolean isInitialized = false;
	private static String ignoreNextAction = null;
	private static View ignoreNextView = null;
	@SuppressWarnings("unused")
	private static Recorder recorder = new Recorder();

	static void logVersion() {
		Log.log(BuildStamp.STAMP);
	}

	/**
	 * This method will be called by internal FoneMonkey code unless the other init() method has
	 * been called already.
	 */
	public static void init() {
		if (isInitialized)
			return;

		isInitialized = true;

		logVersion();

		try {
			loadAutomators(null);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	/**
	 * Client code should call this method upon app launch if you wish to specify an automators
	 * control file that is packaged with the app.
	 */
	public static void init(InputStream customAutomatorsSource) throws IOException {
		automators.clear();
		automatorsByType.clear();

		isInitialized = true;

		logVersion();

		try {
			loadAutomators(customAutomatorsSource);
		} catch (Exception e) {
			Log.log(e);
		}
	}

	static private void loadAutomators(InputStream customAutomatorsSource) throws Exception {
		InputStream is = AutomationManager.class.getResourceAsStream("/DefaultAutomators.txt");

		if (is == null) {
			Log.log("ERROR: Your APK does not seem to be linked with the MonkeyTalk library");
			return;
		}

		InputStream extensions = AutomationManager.class
				.getResourceAsStream("/monkeytalk.automators");
		if (extensions != null) {
			is = new SequenceInputStream(is, extensions);
		}

		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(is));

		boolean noOverrides = false;

		while ((line = in.readLine()) != null) {
			line = line.trim();

			if (line.startsWith("#") || line.length() == 0) {
				continue;
			}

			Class<?> klass;
			try {
				klass = (Class<?>) Class.forName(line);
			} catch (Exception e) {
				Log.log("Unable to load automator " + line, e);
				continue;
			}
			IAutomator ia = (IAutomator) klass.newInstance();
			registerClass(ia.getComponentType(), ia.getComponentClass(), klass, ia.getAliases(),
					ia.isHtmlAutomator());
		}

		in.close();

		// We give preference to loading from a source provided by the app
		if (customAutomatorsSource != null) {
			in = new BufferedReader(new InputStreamReader(customAutomatorsSource));
		}
		// Otherwise we'll see if there a file on the SD Card
		else {
			File sdCard = Environment.getExternalStorageDirectory();

			if (sdCard == null || !sdCard.exists())
				return;

			File customAutomators = new File(sdCard, "FoneMonkeyAutomators.txt");

			if (!customAutomators.exists())
				return;

			Log.log("Processing automators in: " + customAutomators);

			in = new BufferedReader(new FileReader(customAutomators));
		}

		while ((line = in.readLine()) != null) {
			line = line.trim();

			if (line.startsWith("#"))
				continue;

			if (line.startsWith("%")) {
				line = line.toUpperCase();

				if (line.equals("%CLEAR")) {
					automators.clear();
					automatorsByType.clear();
				} else if (line.equals("%NO_OVERRIDES"))
					noOverrides = true;

				continue;
			}

			Class<?> klass = Class.forName(line);
			IAutomator ia = (IAutomator) klass.newInstance();

			if (noOverrides && automatorsByType.get(ia.getComponentType()) != null) {
				Log.log("Warning: Overriding automator type \"" + ia.getComponentType()
						+ "\" not allowed due to %NO_OVERRIDES directive.");
				continue;
			}

			registerClass(ia.getComponentType(), ia.getComponentClass(), klass);
		}

		in.close();
	}

	private static HashMap<String, Class<?>> automators = new HashMap<String, Class<?>>();
	private static HashMap<String, Class<?>> automatorsByType = new HashMap<String, Class<?>>();
	private static HashMap<String, Class<?>> htmlAutomatorsByType = new HashMap<String, Class<?>>();
	private static HashMap<Integer, IAutomator> automatorCache = new HashMap<Integer, IAutomator>();

	public static void registerClass(String componentType, Class<?> componentClass,
			Class<?> automatorClass) {
		registerClass(componentType, componentClass, automatorClass, null);
	}

	public static void registerClass(String componentType, Class<?> componentClass,
			Class<?> automatorClass, String[] aliases) {
		registerClass(componentType, componentClass, automatorClass, aliases, false);
	}

	public static void registerClass(String componentType, Class<?> componentClass,
			Class<?> automatorClass, String[] aliases, boolean isHtmlAutomator) {
		if (componentClass != null) {
			automators.put(componentClass.getName(), automatorClass);
		}

		if (isHtmlAutomator) {
			htmlAutomatorsByType.put(componentType, automatorClass);
		} else {
			automatorsByType.put(componentType, automatorClass);
		}

		if (aliases == null) {
			return;
		}

		for (String alias : aliases) {
			if (isHtmlAutomator) {
				htmlAutomatorsByType.put(alias, automatorClass);
			} else {
				automatorsByType.put(alias, automatorClass);
			}
		}
	}

	private static int foundSoFar = 0;
	private static Pattern arrayPattern = Pattern.compile("^(.+?)\\((\\d+)\\)$");

	/**
	 * Return the view with the specified componentType and monkeyID. Matches on componentType only
	 * if monkeyID is blank or null.
	 * 
	 * @param componentType
	 *            Type type of the component to find
	 * @param monkeyID
	 *            If blank or null, the (indeterminate) first component found with a matching
	 *            componentType will be returned
	 * @return the found View or HtmlElement, or null if no View can be found
	 */
	public static Object findComponentByMonkeyID(String componentType, String monkeyID) {
		Class<?> type = getAutomator(componentType);
		if (type == null) {
			throw new IllegalArgumentException("Unrecognized component type: " + componentType);
		}

		String id = monkeyID;
		int index = 1;
		foundSoFar = 0;
		Matcher matcher = arrayPattern.matcher(monkeyID);
		if (matcher.matches()) {
			// matcher.find();
			id = matcher.group(1);
			index = Integer.valueOf(matcher.group(2));
		}

		HashSet<View> roots = new HashSet<View>(getRoots());
		Dialog box = ActivityManager.getCurrentDialog();
		if (box != null) {
			roots.add(box.getWindow().getDecorView());
		}
		for (View root : roots) {
			if (!root.isShown())
				continue;
			Object v = _findComponentByMonkeyID(root, componentType, id, index);
			if (v != null) {
				return v;
			}
		}

		return null;
	}

	public static Set<View> getRoots() {
		return FunctionalityAdder.getRoots();
	}

	private static Object _findComponentByMonkeyID(View root, String componentType,
			String monkeyID, int index) {

		IAutomator automator = AutomationManager.findAutomator(root);
		if (automator == null) {
			Log.log("Unable to find an automator for " + root.getClass().getName());
			return null;
		}

		if (automator instanceof WebViewAutomator && root.isShown()) {
			if (automator.canAutomate(componentType, monkeyID)) {
				// The command is being directed to the webview itself
				return root;
			}
			// Need to deal with ordinals on webview
			HtmlElement elem = ((WebViewAutomator) automator).findHtmlElement(componentType,
					monkeyID, index);
			if (elem != null) {
				return elem;
			} else {
				return null;
			}
		}

		if (automator.canAutomate(componentType, monkeyID)) {
			foundSoFar++;
			if (foundSoFar == index) {
				return root;
			}
		}

		if (!(root instanceof ViewGroup)) {
			return null;
		}

		ViewGroup vg = (ViewGroup) root;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			Object c = _findComponentByMonkeyID(vg.getChildAt(i), componentType, monkeyID, index);
			if (c != null) {
				return c;
			}
		}

		return null;
	}

	private static List<Object> filterComponentsForWildcardMonkeyId(String componentType,
			String monkeyId) {
		List<Object> views = new ArrayList<Object>();
		Set<View> roots = new HashSet<View>(getRoots());
		Dialog box = ActivityManager.getCurrentDialog();
		if (box != null) {
			roots.add(box.getWindow().getDecorView());
		}
		for (View root : roots) {
			if (!root.isShown()) {
				continue;
			}
			views.addAll(_findComponentsForWildcardMonkeyId(root, componentType, monkeyId));
		}
		return views;
	}

	private static List<Object> _findComponentsForWildcardMonkeyId(View root, String componentType,
			String monkeyId) {
		List<Object> views = new ArrayList<Object>();
		IAutomator automator = AutomationManager.findAutomator(root);
		if (automator == null) {
			Log.log("Unable to find an automator for " + root.getClass().getName());
			return views;
		}

		if (matchesWildcardMonkeyId(automator, componentType, monkeyId)) {
			Log.log("found " + automator.toString());
			views.add(root);
		}

		/*
		 * JUSTIN: stupid webview wildcard monkeyId hack... Just do the component lookup inside the
		 * webview for non-wildcard monkeyId and add it as a view
		 */
		if (monkeyId.equals("*") && automator instanceof WebViewAutomator && root.isShown()) {
			Object view = findComponentByMonkeyID(componentType, monkeyId);
			views.add(view);
		}

		if (root instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) root;
			for (int i = 0; i < vg.getChildCount(); i++) {
				View child = vg.getChildAt(i);
				views.addAll(_findComponentsForWildcardMonkeyId(child, componentType, monkeyId));
			}
		}
		return views;
	}

	private static boolean matchesWildcardMonkeyId(IAutomator automator, String componentType,
			String monkeyId) {
		if (automator instanceof ViewAutomator && ((ViewAutomator) automator).getView().isShown()
				&& automator.forSubtypeOf(componentType)) {
			return PropertyUtil.wildcardMatch(monkeyId, automator.getMonkeyID());
		}
		return false;
	}

	public static List<IAutomator> findAllWildcardMonkeyIdAutomators(String componentType,
			String monkeyId) {
		List<IAutomator> filteredAutomators = new ArrayList<IAutomator>();
		Class<?> c = getAutomator(componentType);
		if (c == null) {
			throw new IllegalArgumentException("Unrecognized component type: " + componentType);
		}

		List<Object> views = filterComponentsForWildcardMonkeyId(componentType, monkeyId);

		for (Object view : views) {
			IAutomator automator = null;
			try {
				automator = (IAutomator) c.newInstance();
			} catch (Exception e) {
				Log.log("Failure instancing automator for type " + componentType + ": "
						+ e.getMessage());
			}
			if (automator == null) {
				continue;
			}

			Class<?> k = automator.getComponentClass();

			if (k != null
					&& (View.class.isAssignableFrom(k) || HtmlElement.class.isAssignableFrom(k))) {
				// UI View or Html element. Find the corresponding component in the UI Tree.

				if (componentType.equals("")) {
					componentType = ViewAutomator.componentType;
				}

				if (view == null) {
					continue;
				}

				if (view.getClass() == HtmlElement.class) {
					Class<?> htmlAutomatorClass = getHtmlAutomator(componentType);
					if (htmlAutomatorClass == null) {
						htmlAutomatorClass = HtmlElementAutomator.class;
					}

					try {
						automator = (IAutomator) htmlAutomatorClass.newInstance();
					} catch (Exception ex) {
						// ignore
					}
				}

				if (view.getClass() != automator.getComponentClass()) {
					// ComponentType was for a supertype so there might be exist an
					// automator subclass
					automator = AutomationManager.findAutomator(view);
				}
				automator.setComponent(view);
			} else {
				// Non-UI component
				automator.setMonkeyID(monkeyId);
			}

			filteredAutomators.add(automator);
		}
		return filteredAutomators;
	}

	public static IAutomator find(String componentType, String monkeyID) {
		return find(componentType, monkeyID, false);
	}

	public static IAutomator find(String componentType, String monkeyID, boolean nullOnNotFound) {
		Class<?> c = getAutomator(componentType);
		if (c == null) {
			throw new IllegalArgumentException("Unrecognized component type: " + componentType);
		}

		IAutomator automator = null;
		try {
			automator = (IAutomator) c.newInstance();
		} catch (Exception e) {
			Log.log("Failure instancing automator for type " + componentType + ": "
					+ e.getMessage());
		}
		if (automator == null) {
			return null;
		}

		Class<?> k = automator.getComponentClass();

		if (k != null && (View.class.isAssignableFrom(k) || HtmlElement.class.isAssignableFrom(k))) {
			// UI View or Html element. Find the corresponding component in the UI Tree.

			if (componentType.equals("")) {
				componentType = ViewAutomator.componentType;
			}

			Object view = findComponentByMonkeyID(componentType, monkeyID);
			if (view == null) {
				if (nullOnNotFound) {
					return null;
				}
				throw new FoneMonkeyScriptFailure("Unable to find " + componentType
						+ " with monkeyID \"" + monkeyID + "\"");
			}

			if (view.getClass() == HtmlElement.class) {
				Class<?> htmlAutomatorClass = getHtmlAutomator(componentType);
				if (htmlAutomatorClass == null) {
					htmlAutomatorClass = HtmlElementAutomator.class;
				}

				try {
					automator = (IAutomator) htmlAutomatorClass.newInstance();
				} catch (Exception e) {
					Log.log("Failure instancing automator for type " + componentType + ": "
							+ e.getMessage());
				}
			}

			if (view.getClass() != automator.getComponentClass()) {
				// ComponentType was for a supertype so there might be exist an
				// automator subclass
				automator = AutomationManager.findAutomator(view);
			}
			automator.setComponent(view);
		} else {
			// Non-UI component
			automator.setMonkeyID(monkeyID);
		}

		return automator;
	}

	/**
	 * Find an automator that handles the class of this object. If none exists then return the
	 * automator that handles the nearest superclass. If o is null, return DeviceAutomator (ewww),
	 * otherwise return null.
	 */
	public static IAutomator findAutomator(Object o) {
		if (o == null) {
			return findAutomatorByType(AutomatorConstants.TYPE_DEVICE);
		}

		// JUSTIN: let's clear the cache before each search, so we can always start fresh
		automatorCache.clear();

		// get the automator for the given view's class
		return _findAutomator(o, o.getClass());
	}

	/**
	 * Find the automator for the given non-null object (typically a View). If none exists, then
	 * return the automator for the nearest superclass. If none can be found, return null.
	 */
	private static IAutomator _findAutomator(Object o, Class<?> c) {
		// first, check the cache
		IAutomator cached = automatorCache.get(o.hashCode());
		if (cached != null) {
			return cached;
		}

		// next, just get the automator for the given class
		Class<?> klass = automators.get(c.getName());

		// if that doesn't work, try the interfaces of the view
		if (klass == null) {
			for (Class<?> k : o.getClass().getInterfaces()) {
				klass = automators.get(k.getName());
				if (klass != null) {
					break;
				}
			}
		}

		// if we found it, then instantiate and cache
		IAutomator automator = null;
		if (klass != null) {
			try {
				automator = (IAutomator) klass.newInstance();
			} catch (Exception e) {
				Log.log("Error instancing automator " + o.getClass().getName());
			}

			if (automator != null) {
				automator.setComponent(o);
				automatorCache.put(o.hashCode(), automator);
				return automator;
			}
		}

		// nothing found yet, try the superclass
		Class<?> parent = c.getSuperclass();
		return (parent == null ? null : _findAutomator(o, parent));
	}

	public static IAutomator findAutomatorByType(String type) {
		Class<?> klass = automatorsByType.get(type);

		IAutomator automator = null;
		if (klass != null) {
			try {
				automator = (IAutomator) klass.newInstance();
			} catch (Exception e) {
				Log.log("Error instancing automator for type " + type);
			}
			return automator;
		}
		return null;
	}

	private static int index = 0, found = 0;

	public static String findIndexedMonkeyIdIfAny(IAutomator automator) {
		if (automator.getOrdinal() > 1) {
			index = 0;
			found = 0;
			findIndex(automator.getComponentType(), automator.getMonkeyID(), automator.getOrdinal());

			if (index > 1) {
				return automator.getMonkeyID() + "(" + index + ")";
			}
		}
		return automator.getMonkeyID();
	}

	private static Object findIndex(String componentType, String monkeyID, int ordinal) {
		Set<View> roots = new HashSet<View>(getRoots());
		Dialog box = ActivityManager.getCurrentDialog();
		if (box != null) {
			roots.add(box.getWindow().getDecorView());
		}

		for (View root : roots) {
			if (!root.isShown()) {
				continue;
			}
			Object c = _findIndex(root, componentType, monkeyID, ordinal);
			if (c != null) {
				return c;
			}
		}
		return null;
	}

	private static Object _findIndex(View root, String componentType, String monkeyID, int ordinal) {
		if (root instanceof View) {
			IAutomator automator = AutomationManager.findAutomatorByType(componentType);
			if (automator == null) {
				return null;
			}

			if (root.getClass() == automator.getComponentClass()) {
				automator.setComponent(root);
				found++;
				if (automator.getMonkeyID().equals(monkeyID)) {
					index++;
				}
				if (found == ordinal) {
					return root;
				}
			}
		}

		if (root instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) root;
			for (int i = 0; i < vg.getChildCount(); ++i) {
				Object c = _findIndex(vg.getChildAt(i), componentType, monkeyID, ordinal);
				if (c != null) {
					return index;
				}
			}
		}
		return null;
	}

	public static boolean record(String action, Object view, String... args) {
		IAutomator automator = AutomationManager.findAutomator(view);

		if (automator == null) {
			return false;
		}

		if (shouldIgnore(automator, action)) {
			cancelIgnore();
			return true;
		}

		automator.record(action, args);
		return true;
	}

	private static boolean shouldIgnore(IAutomator automator, String action) {
		Object view = automator.getComponent();
		if (view instanceof View) {
			View v = (View) view;
			if (isHiddenByParent(v)) {
				return true;
			}
			if (ignoreNextAction != null && ignoreNextAction.equalsIgnoreCase(action)
					&& ignoreNextView == v) {
				return true;
			}

			// ignore components in webviews (if view is null, it is a hardware button)
			if (v.getParent().getClass() == WebView.class || v.getClass() == WebView.class) {
				return true;
			}
		}
		return false;
	}

	// This needs to get folded into generic record
	public static boolean recordTab(String operation, String... args) {

		Class<?> klass = automatorsByType.get(AutomatorConstants.TYPE_TABBAR);
		if (klass == null)
			return false;

		IAutomator automator;

		try {
			automator = (IAutomator) klass.newInstance();
		} catch (Exception e) {
			Log.log("Error instancing automator " + klass.getName());
			return false;
		}

		automator.record(operation, args);
		return true;
	}

	/**
	 * @param runnable
	 */
	public static void runOnUIThread(Runnable runnable) {
		AutomationManager.getTopActivity().runOnUiThread(runnable);
	}

	/**
	 * Traverse ancestors to see if any are hiding this component from automation
	 * 
	 * @param view
	 *            the view to be checked
	 * @return true if any ancestor is hiding this view
	 */
	public static boolean isHiddenByParent(View view) {
		// Log.log("Checking if " + view + " is hidden");
		return _isHiddenByParent(view, view.getParent());
	}

	private static boolean _isHiddenByParent(View view, ViewParent parent) {
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

	/**
	 * 
	 * Use this method to register a Non-UI component (eg, Device).
	 * 
	 * @param componentType
	 * @param class1
	 * 
	 */
	public static void registerClass(String componentType, Class<?> automator) {
		registerClass(componentType, null, automator);
	}

	/**
	 * 
	 * the activity currently on top of the stack
	 */
	public static Activity getTopActivity() {
		ComponentName topDog = ((android.app.ActivityManager) Recorder.getSomeActivity()
				.getSystemService(Application.ACTIVITY_SERVICE)).getRunningTasks(1).get(0).topActivity;
		Activity topActivity = ActivityManager.getActivity(topDog.flattenToString());
		if (topActivity==null) {
			System.out.println("null top activity :-(");
			System.out.println(" top dog: " + topDog);
			System.out.println(" top dog.flattenToString(): " + topDog.flattenToString());
			System.out.println(" Recorder.getSomeActivity(): " + Recorder.getSomeActivity());
		}
		return topActivity;
	}

	public static String dumpViewTree() {
		JSONObject root = new JSONObject();
		try {
			root.put("ComponentType", "root");
			root.put("monkeyId", "");
			root.put("className", "");
			root.put("visible", "true");
			root.put("identifiers", new JSONArray());
			root.put("ordinal", 1);
			JSONArray kids = new JSONArray();
			Set<View> strippedRoots = FunctionalityAdder.getRootsStripped();
			for (View view : strippedRoots) {
				JSONObject comp = getJson(view);
				kids.put(comp);
			}
			root.put("children", kids);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return root.toString();
	}

	private static JSONObject getJson(View view) {
		IAutomator automator = findAutomator(view);

		if (automator == null) {
			IllegalStateException ex = new IllegalStateException("Unable to find automator for "
					+ view.getClass().getName());
			Log.log("Error dumping tree", ex);
			throw ex;
		}

		JSONObject comp = new JSONObject();
		try {
			comp.put("ComponentType", automator.getComponentType());
			comp.put("monkeyId", findIndexedMonkeyIdIfAny(automator));
			comp.put("className", view.getClass().getName());
			comp.put("visible", String.valueOf(view.isShown()));
			comp.put("identifiers", new JSONArray(automator.getIdentifyingValues()));
			comp.put("ordinal", automator.getOrdinal());
			JSONArray kids;
			if (automator instanceof WebViewAutomator) {
				kids = getJsonForWebView((WebViewAutomator) automator);
			} else {
				kids = getJsonForKids(view);
			}
			comp.put("children", kids);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return comp;
	}

	private static JSONArray getJsonForWebView(WebViewAutomator automator) throws JSONException {
		String componentTreeJson = automator.getComponentTreeJson();
		if (componentTreeJson != null && componentTreeJson.length() > 0) {
			return new JSONArray(componentTreeJson);
		}
		return new JSONArray();
	}

	private static JSONArray getJsonForKids(View view) {
		JSONArray kids = new JSONArray();

		if (!(view instanceof ViewGroup)) {
			return kids;
		}

		ViewGroup vg = (ViewGroup) view;
		for (int i = 0; i < vg.getChildCount(); ++i) {
			kids.put(getJson(vg.getChildAt(i)));
		}
		return kids;
	}

	private static Class<?> getAutomator(String type) {
		Class<?> c;
		if (type.equals("") || type.equals("*")) {
			c = automatorsByType.get(AutomatorConstants.TYPE_VIEW);
		} else {
			c = automatorsByType.get(type);
		}

		if (c == null) {
			c = getHtmlAutomator(type);
		}

		return c;
	}

	private static Class<?> getHtmlAutomator(String type) {
		Class<?> c;
		if (type.equals("") || type.equals("*")) {
			c = htmlAutomatorsByType.get(AutomatorConstants.TYPE_VIEW);
		} else {
			c = htmlAutomatorsByType.get(type);
		}
		return c;
	}

	/**
	 * Call to suppress recording an event resulting from a programmatic (rather than UI) action
	 * 
	 * @param view
	 * @param action
	 */
	public static void ignoreNext(View view, String action) {
		ignoreNextView = view;
		ignoreNextAction = action;
	}

	public static void cancelIgnore() {
		ignoreNextView = null;
		ignoreNextAction = null;
	}
}
