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
package com.gorillalogic.fonemonkey.aspects;

import android.view.View;
import android.view.Window;

import com.gorillalogic.fonemonkey.automators.AutomationManager;
import com.gorillalogic.fonemonkey.automators.IAutomator;
import com.gorillalogic.fonemonkey.automators.WindowAutomator;

aspect SetListener {
	pointcut setListener(Object v) : target(v) && (call(void setOn*Listener(..)));

	after(Object v): setListener(v)
	{
		IAutomator auto = AutomationManager.findAutomator(v);
		if (auto == null) {
			return;
		}
		auto.installDefaultListeners();

	}

	pointcut setCallback(Window w) : target(w) && (call(void setCallback(Window.Callback)));

	after(Window w): setCallback(w)
	{
		WindowAutomator.automate(w);
	}
}