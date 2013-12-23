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

package com.gorillalogic.agents.html.automators;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;
import com.gorillalogic.monkeytalk.sender.Response;

public class BrowserAutomator extends AutomatorBase {

	public static String componentType = "Browser";

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public String play(Command command) {
		String action = command.getAction();
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_OPEN)) {
			String url = getArg(command, 0);
			
			if (!(url.substring(0, 7).equalsIgnoreCase("http://") ||
					url.substring(0, 7).equalsIgnoreCase("file://") ||
					url.substring(0, 8).equalsIgnoreCase("https://")))
				url = "http://" + url;
			
			driver.get(url);
			return null;
		}
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_GET)) {
			return driver.getTitle();
		}	
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_BACK)) {
			driver.navigate().back();
			return null;
		}	
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_FORWARD)) {
			driver.navigate().forward();
			return null;
		}	
		
		return super.play(command);
	}

	@Override
	protected String getProperty(String prop) {
		if (prop.equals("value")) {
			return driver.getTitle();
		}
		return super.getProperty(prop);
	}

}
