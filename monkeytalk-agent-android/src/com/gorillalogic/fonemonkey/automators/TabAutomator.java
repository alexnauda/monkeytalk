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

import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * Automation support for android.widget.TabHost
 */
public class TabAutomator extends ViewAutomator implements OnTabChangeListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gorillalogic.fonemonkey.automators.ViewAutomator#installDefaultListeners ()
	 */
	@Override
	public boolean installDefaultListeners() {
		// Need to override because of setter name is inconsistent with listener
		// name (change vs changed).
		chainListenerFor(OnTabChangeListener.class, "setOnTabChangedListener");
		return true;
	}

	private static Class<?> componentClass = TabHost.class;
	static {
		Log.log("Initializing TabAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_TABBAR;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	public TabHost getTabHost() {
		return (TabHost) getComponent();
	}

	/**
	 * hide all internal children (i.e., actual tabs)
	 */
	@Override
	public boolean hides(View view) {
		android.view.ViewParent parent = view.getParent();
		while (parent != null) {
			if (parent.getClass().getName().contains("TabWidget")) {
				return true;
			}
			parent = parent.getParent();
		}
		return super.hides(view);
	}

	@Override
	public String play(String action, String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)) {
			assertArgCount(action, args, 1);
			final TabHost tabs = this.getTabHost();
			final String title = args[0];
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					tabs.setCurrentTabByTag(title);
				}
			});
			return null;
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {
			assertArgCount(action, args, 1);
			final TabHost tabs = this.getTabHost();
			final int index = getIndexArg(action, args[0]);

			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					tabs.setCurrentTab(index - 1);
				}
			});
			return null;
		}
		return super.play(action, args);

	}

	public String getValue() {
		return getTabHost().getCurrentTabTag();
	}

	@Override
	protected String getProperty(String propertyPath) {
		if (propertyPath.equals("size")) {
			return String.valueOf(this.getTabHost().getTabWidget().getChildCount());
		}
		return super.getProperty(propertyPath);
	}

	@Override
	public void onTabChanged(String tabId) {
		AutomationManager.recordTab(AutomatorConstants.ACTION_SELECT, tabId);
	}
}