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
package com.gorillalogic.monkeyconsole.editors.utils;

import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public enum ConnectionTypesEnum {
	EMULATOR("Android Emulator or Tethered Device"),
	SIMULATOR("iOS Simulator"),
	FLEX("FLEX Application"),
	WEB("Web Application (Firefox)"),
	CHROME("Web Application (Chrome)"),
	SAFARI("Web Application (Safari)"),
	IE("Web Application (IE)"),
	NETWORKED_IOS("device at [ip]"),
	NETWORKED_ANDROID("device at [ip]"),
	NO_DEVICE("no device"), CLOUD_ANDROID("CloudMonkey Android");
	
	public String humanReadableFormat = "";
	ConnectionTypesEnum(String humanReadableFormat){
		this.humanReadableFormat = humanReadableFormat;
	}
}