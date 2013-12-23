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

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;

import com.gorillalogic.fonemonkey.ActivityManager;
import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyScriptFailure;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * 
 * Works in conjunction with WindowAutomator to provide menu recording.
 * 
 * @author sstern
 * 
 */
public class MenuAutomator extends AutomatorBase implements OnMenuItemClickListener {

	@Override
	public int getOrdinal() {
		return 1;
	}

	@Override
	public void record(String operation, String... args) {
		super.record(operation, getMenuItem() == null ? super.getMonkeyID() : getMenuItem()
				.getTitle().toString());
	}

	private static Class<?> componentClass = MenuItem.class;
	static {
		Log.log("Initializing MenuAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_MENU;
	}

	public MenuItem getMenuItem() {
		return (MenuItem) getComponent();
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String play(String action, String... args) {
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)) {

			assertArgCount(action, args, 1);
			final String title = args[0];

			final Activity activity = AutomationManager.getTopActivity();
			if (activity == null) {
				throw new IllegalStateException("Unable to find top activity");
			}

			Menu menu = ActivityManager.getCurrentMenu();

			if (menu == null) {
				throw new FoneMonkeyScriptFailure("Unable to find menu.");
			}

			int size = menu.size();

			int itemId = -1;

			if (action.equals(AutomatorConstants.ACTION_SELECT)) {
				outer: for (int i = 0; i < size; i++) {
					MenuItem item = menu.getItem(i);
					if (item.getTitle().toString().equals(title)) {
						itemId = item.getItemId();
						break;
					}

					// HACK: just brute force down some more (so: menus, sub-menus, sub-sub-menus)
					if (item.hasSubMenu()) {
						SubMenu sub = item.getSubMenu();
						for (int j = 0; j < sub.size(); j++) {
							MenuItem subitem = sub.getItem(j);
							if (subitem.getTitle().toString().equals(title)) {
								itemId = subitem.getItemId();
								sub.close();
								break outer;
							}
							if (subitem.hasSubMenu()) {
								SubMenu subsub = subitem.getSubMenu();
								for (int k = 0; k < subsub.size(); k++) {
									MenuItem subsubitem = subsub.getItem(k);
									if (subsubitem.getTitle().toString().equals(title)) {
										itemId = subsubitem.getItemId();
										subsub.close();
										sub.close();
										break outer;
									}
								}
							}
						}
					}
				}

				if (itemId == -1) {
					String msg = getComponentType()
							+ " selection failed. Unable to find item with title: " + title;
					throw new FoneMonkeyScriptFailure(msg);
				}
			} else {
				if (args.length == 0) {
					throw new FoneMonkeyScriptFailure(AutomatorConstants.ACTION_SELECT_INDEX
							+ " requires an argument specifying the index to select");
				}

				String index = args[0];
				int i;
				try {
					i = Integer.valueOf(index);
				} catch (NumberFormatException e) {
					throw new FoneMonkeyScriptFailure(AutomatorConstants.ACTION_SELECT_INDEX
							+ ": index argument must be an integer. Found \"" + index + "\"");
				}
				if (i > size || i < 1) {
					throw new FoneMonkeyScriptFailure("Invalid index " + index
							+ " specified. Menu has " + size + " items");
				}

				itemId = menu.getItem(i - 1).getItemId();
			}

			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					activity.openOptionsMenu();
				}
			});

			// HACK: wait a little for menu to open
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// ignore
			}

			final int id = itemId;
			final Menu m = menu;
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					activity.closeOptionsMenu();
					m.performIdentifierAction(id, Menu.NONE);

				}
			});

			return null;
		}
		return super.play(action, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.MenuItem.OnMenuItemClickListener#onMenuItemClick(android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		record(AutomatorConstants.ACTION_LONG_SELECT, getMenuItem() == null ? super.getMonkeyID()
				: getMenuItem().getTitle().toString());
		return false;
	}

	@Override
	public String getValue() {
		final Activity activity = AutomationManager.getTopActivity();
		if (activity == null) {
			throw new IllegalStateException("Unable to find top activity");
		}

		String value = "";
		Menu menu = ActivityManager.getCurrentMenu();

		// get titles for menu items to return
		for (int i = 0; i < menu.size(); i++) {
			value += menu.getItem(i);

			if (i < menu.size() - 1)
				value += ", ";
		}

		return value;
	}

}