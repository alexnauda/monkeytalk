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

import android.app.Dialog;
import android.view.View;
import android.widget.PopupWindow;

public class FoneMonkey
{

	public static void register(Dialog d)
	{
		View v = d.getWindow().getDecorView().getRootView();
		if (FunctionalityAdder.getRoots().contains(v)) {
			return;
		}
		FunctionalityAdder.walkTree(v);
	}

	public static void register(PopupWindow p)
	{
		View v = p.getContentView().getRootView();
		if (FunctionalityAdder.getRoots().contains(v)) {
			return;
		}
		if (v != null) {
			FunctionalityAdder.walkTree(v);
		}
	}

	public static void log(String msg)
	{
		Log.log(msg);
	}

	public static void log(Throwable t)
	{
		Log.log(t);
	}
}
