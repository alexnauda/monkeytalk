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

public class Log {
	private static final String TAG = "MonkeyTalk";
	private static boolean shouldPrint = false;

	public static void log(String msg) {
		logIt(msg, null);
	}

	public static void log(String msg, Throwable t) {
		logIt(msg, t);
	}

	public static void log(Throwable t) {
		logIt(t.getMessage() != null ? t.getMessage() : "Error", t);
	}
	
	public static void setShouldPrint(boolean shouldPrint) {
		Log.shouldPrint = shouldPrint;
	}

	private static void logIt(String msg, Throwable t) {
		if (shouldPrint || !isAndroid()) {
			System.out.println(msg);
			if (t != null) {
				t.printStackTrace();
			}
		} else {
			if (t != null) {
				android.util.Log.e(TAG, msg, t);
			} else {
				android.util.Log.d(TAG, msg);
			}
		}
	}

	private static boolean isAndroid() {
		try {
			Class.forName("android.util.Log");
			return true;
		} catch (Exception ex) {
		}

		return false;
	}

}