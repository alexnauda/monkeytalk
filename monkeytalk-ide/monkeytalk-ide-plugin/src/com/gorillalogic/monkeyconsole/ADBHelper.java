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
package com.gorillalogic.monkeyconsole;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

/**
 * Helper class to manage the Android Debug Bridge (aka ADB)
 */
public class ADBHelper {

	/**
	 * Return {@code null} if ADB path is valid, otherwise return error message.
	 * 
	 * @return null if valid, otherwise error message
	 */
	public static String validate() {
		String path = getAndroidSdkPref();
		if (path == null) {
			Action action = new Action() {
				public void run() {
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
							"com.gorillalogic.monkeyconsole.preferences.FonemonkeyPreferencePage",
							null, null);
					dialog.open();
				}
			};
			action.run();

			return null;
		}

		return validateAndroidSdkPath(path);
	}

	/**
	 * From the given path, return null if it leads to a valid ADB, otherwise return an error
	 * message.
	 * 
	 * @param path
	 *            the Android SDK path
	 * @return null if valid, otherwise error message.
	 */
	public static String validateAndroidSdkPath(String path) {
		if (path == null) {
			return "Android SDK path is null.";
		}

		File sdk = new File(path);
		if (!sdk.exists() || !sdk.isDirectory()) {
			return "Unable to find the Android SDK. Looked here: " + sdk.getAbsolutePath();
		}

		File platformTools = new File(sdk, "platform-tools");
		if (!platformTools.exists() || !platformTools.isDirectory()) {
			return "Unable to find the 'platform-tools' folder. Looked here: "
					+ platformTools.getAbsolutePath();
		}

		String os = System.getProperty("os.name").toLowerCase();
		File adb = new File(platformTools, "adb" + (os.contains("win") ? ".exe" : ""));
		if (!adb.exists() || !adb.isFile()) {
			return "Unable to find ADB. Looked here: " + adb.getAbsolutePath();
		}

		return null;
	}

	/**
	 * Get the location of the Android SDK folder from the Preferences.
	 */
	private static String getAndroidSdkPref() {
		String sdk = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.P_ANDROIDHOME);
		if (sdk != null) {
			File dir = new File(sdk);
			if (dir.exists() && dir.isDirectory()) {
				return sdk;
			}
		}

		return null;
	}

	/**
	 * Get the path to ADB from the Preferences, but return null if invalid.
	 * 
	 * @return the path to ADB if valid, otherwise null
	 */
	public static String getAdbPath() {
		String sdkPath = getAndroidSdkPref();
		if (sdkPath != null) {
			File sdk = new File(sdkPath);
			if (sdk.exists() && sdk.isDirectory()) {
				File platformTools = new File(sdk, "platform-tools");
				if (platformTools.exists() && platformTools.isDirectory()) {
					String os = System.getProperty("os.name").toLowerCase();
					File adb = new File(platformTools, "adb" + (os.contains("win") ? ".exe" : ""));
					if (adb.exists() && adb.isFile()) {
						return adb.getAbsolutePath();
					}
				}
			}
		}

		return null;
	}
}