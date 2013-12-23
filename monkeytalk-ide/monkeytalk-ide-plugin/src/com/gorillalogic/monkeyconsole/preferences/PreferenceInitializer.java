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
package com.gorillalogic.monkeyconsole.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.gorillalogic.cloud.ideversion.CloudConstants;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = FoneMonkeyPlugin.getDefault().getPreferenceStore();
		try {
			MonkeyTalkUtils.setDefaultUPFromClassPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		store.setDefault(PreferenceConstants.P_ANDROIDHOME, "~/androidsdk");
		store.setDefault(PreferenceConstants.P_DEFAULTTIMEOUT, "2000");
		store.setDefault(PreferenceConstants.P_INCLUDEANDROID, true);
		store.setDefault(PreferenceConstants.P_INCLUDEIOS, true);
		store.setDefault(PreferenceConstants.P_TAKEAFTERSCREENSHOTS, false);
		store.setDefault(PreferenceConstants.P_TAKEERRORSCREENSHOTS, true);
		store.setDefault(PreferenceConstants.P_THINKTIME, "500");
		store.setDefault(PreferenceConstants.P_EVENTSTOFILTER, "touchdown, touchmove, touchup");
		store.setDefault(PreferenceConstants.P_CLOUDUSR, "");
		store.setDefault(PreferenceConstants.P_CLOUDPASS, "");
		store.setDefault(PreferenceConstants.P_EVENTSTOCOMBINE, "EnterText, Drag");
		store.setDefault(PreferenceConstants.P_LOGEVENTCONSENT, true);
		store.setDefault(PreferenceConstants.P_CONTROLLER_HOST,
				CloudConstants.DEFAULT_CONTROLLER_HOST);
		store.setDefault(PreferenceConstants.P_CONTROLLER_PORT,
				CloudConstants.DEFAULT_CONTROLLER_PUBLIC_PORT);
		store.setDefault(PreferenceConstants.P_CONTROLLER_SSL_PORT,
				CloudConstants.DEFAULT_CONTROLLER_SSL_PORT);
		store.setDefault(PreferenceConstants.P_CONTROLLER_PROTOCOL,
				CloudConstants.DEFAULT_CONTROLLER_PROTOCOL);

	}

}