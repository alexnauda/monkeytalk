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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.fonemonkey.automators.IAutomator;
import com.gorillalogic.fonemonkey.automators.WindowAutomator;

public class FunctionalityAdder {
	static Set<View> processed = new HashSet<View>();
	private static Set<View> roots = new LinkedHashSet<View>();
	public static Set<View> getRoots() {return Collections.unmodifiableSet(roots);}

	// Walk the View tree and add functionality as necessary
	public static void walkTree(View root) {
		if (!processed.contains(root)) {
			processView(root);
		}

		if (!(root instanceof ViewGroup)) {
			return;
		}

		ViewGroup vg = (ViewGroup) root;

		for (int i = 0; i < vg.getChildCount(); ++i) {
			walkTree(vg.getChildAt(i));
		}
	}

	private static void processView(View v) {
		if (v == null) {
			Log.log("null view passed, ignoring");
			return;
		}

		// add the default listeners for this guy
		IAutomator automator = AutomationManager.findAutomator(v);
		if (automator != null) {
			boolean didInstall = automator.installDefaultListeners();
			if (didInstall) {
				
				// Check if the view is clipped
				ActivityManager.checkIsClipped(v);
				
				processed.add(v);
			}
		} else {
			String id = Integer.toString(v.getId());
			try {
				id = v.getContext().getResources().getResourceName(v.getId());
			} catch (Throwable e) {
			}
			Log.log("no automator found for " + v + " (" + id + ")");
			return;
		}

		View root = v.getRootView();
		// if (roots.contains(root)) {
		if (isViewInRoots(root)) {
			return;
		}

		roots.add(root);
		Context c = root.getContext();
		if (c instanceof Activity) {
			Activity act = (Activity) c;
			WindowAutomator.automate(act.getWindow());
		}
		// Log.log("NEW ROOT " + root);
		root.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					public void onGlobalLayout() {
						Set<View> toRemove = new HashSet<View>();
						HashSet<View> rootsAndDialogs = new HashSet<View>(roots);
						Dialog box = ActivityManager.getCurrentDialog();
						if (box != null) {
							rootsAndDialogs.add(box.getWindow().getDecorView());
						}
						// Log.log("GLOBAL LAYOUT");
						for (View r : rootsAndDialogs) {
							/*
							 * Need better expunge logic. if (!r.isShown()) {
							 * toRemove.add(r); removeFromProcessed(r);
							 * continue; }
							 */
							walkTree(r);
						}

						roots.removeAll(toRemove);
					}
				});
		/*
		 * root.getViewTreeObserver().addOnGlobalFocusChangeListener(new
		 * ViewTreeObserver.OnGlobalFocusChangeListener() { public void
		 * onGlobalFocusChanged(View oldView, View newView) {
		 * Log.log("GLOBAL FOCUS " + newView); } });
		 */
	}
	
	private static boolean isViewInRoots(View v) {
		for (View root: getRoots()) {
			if (isViewInRoot(v, root)) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isViewInRoot(View v, View root) {
		if (root.equals(v)) {
			return true;
		}
		if (root instanceof ViewGroup) {
			ViewGroup group=(ViewGroup)root;
			for (int i=0; i<group.getChildCount(); i++) {
				if (isViewInRoot(v, group.getChildAt(i))) {
					return true;
				}
			}
		}
		return false;
	}

	static void logWarn(String klass, View v) {
		Log.log("WARNING: You have a "
				+ klass
				+ " set on "
				+ v
				+ ". FoneMonkey needs to set its own listener in order to learn about any views added after onCreate() has finished. See FoneMonkey's Multiplexed"
				+ klass + " class for a way to get around this problem.");
	}

	private static boolean hasHierarchyChangeListener(ViewGroup g) {
		// We need to get a handle to the actual parent ViewGroup
		Class klass = g.getClass();
		while (!klass.equals(ViewGroup.class)) {
			klass = klass.getSuperclass();
		}

		try {
			// Obviously this is not the best way to test if a listener
			// exists. But it's all we've got ...
			Field f = klass.getDeclaredField("mOnHierarchyChangeListener");
			f.setAccessible(true);
			return f.get(g) != null;
		} catch (Exception e) {
			Log.log(e);
		}
		return false;
	}

	private static void removeFromProcessed(View root) {
		processed.remove(root);
		if (!(root instanceof ViewGroup))
			return;

		ViewGroup vg = (ViewGroup) root;

		for (int i = 0; i < vg.getChildCount(); ++i) {
			removeFromProcessed(vg.getChildAt(i));
		}
	}

	static void prTree(View root, String indent) {
		Log.log("" + root);

		if (!(root instanceof ViewGroup))
			return;

		ViewGroup vg = (ViewGroup) root;

		for (int i = 0; i < vg.getChildCount(); ++i) {
			prTree(vg.getChildAt(i), indent + "    ");
		}
	}
	
	public static Set<View> getRootsStripped() {
		View[] views=roots.toArray(new View[roots.size()]);
		
		// find and eliminate any roots which are children of other roots
		for (int i=0; i<views.length; i++) {
			View root = views[i];
			if (root!=null) {
				for (int j=0; j<views.length; j++) {
					if (j!=i) { // don't compare to self
						View possibleParent = views[j];
						if (isDescendant(root, possibleParent)) {
							views[i]=null; // this view is a descendant of another view
							break;
						}
					}
				}
			}
		}
		
		// everyone has been checked. Re-constitute the original roots
		Set<View> stripped = new LinkedHashSet<View>();
		for (int i=0; i<views.length; i++) {
			View root = views[i];
			if (root!=null) {
				stripped.add(root);
			}
		}
		
		return stripped;
	}
	
	private static boolean isDescendant(View v, View root) {
		if (v==null || root==null) {
			return false;
		}
		if (v.equals(root)) {
			return true;
		}
		if (root instanceof ViewGroup) {
			ViewGroup group = (ViewGroup)root;
			for (int i=0; i<group.getChildCount(); i++) {
				if (isDescendant(v, group.getChildAt(i))) {
					return true;
				}
			}
		}
		return false;
	}
}