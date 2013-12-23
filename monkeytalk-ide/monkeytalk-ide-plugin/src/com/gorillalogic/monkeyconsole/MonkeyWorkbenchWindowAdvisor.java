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

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class MonkeyWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public MonkeyWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		int x = PlatformUI.getPreferenceStore().getInt("posx");
		int y = PlatformUI.getPreferenceStore().getInt("posy");
		getWindowConfigurer().getWindow().getShell()
				.setLocation(x == 0 ? 1024 : x, y == 0 ? 768 : y);
	}

	@Override
	public boolean preWindowShellClose() {
		int x = getWindowConfigurer().getWindow().getShell().getLocation().x;
		int y = getWindowConfigurer().getWindow().getShell().getLocation().y;

		PlatformUI.getPreferenceStore().setValue("posx", x);
		PlatformUI.getPreferenceStore().setValue("posy", y);
		return super.preWindowShellClose();

	}

}