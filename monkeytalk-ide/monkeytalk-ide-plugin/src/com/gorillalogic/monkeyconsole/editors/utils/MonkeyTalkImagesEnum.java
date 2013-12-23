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

import org.eclipse.jface.resource.ImageDescriptor;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;

public enum MonkeyTalkImagesEnum {

    CONNECT("icons/coolbaricons/connect.gif"),
    FLEX("icons/coolbaricons/flex.png"),
    FIREFOX("icons/coolbaricons/firefox.png"),  
    CHROME("icons/coolbaricons/chrome.png"),
    SAFARI("icons/coolbaricons/safari.png"),
    IE("icons/coolbaricons/ie.png"),
    CONNECTANDROIDEMULATOR("icons/coolbaricons/android.gif"),
    CONNECTNETWORKEDANDROID("icons/coolbaricons/android_network.gif"),
    CONNECTIOSEMMULATOR("icons/coolbaricons/apple.png"),
    CONNECTNETWORKEDIOS("icons/coolbaricons/apple_network.png"),
    NOCONNECTION("icons/coolbaricons/noConnection.gif"),
    CLEAR("icons/coolbaricons/clear.gif"),
    CLEARROW("icons/coolbaricons/clear.gif"),
    PLAY("icons/coolbaricons/play.gif"),
    PLAYONCLOUD("icons/coolbaricons/runoncloud.png"),
    STOP("icons/coolbaricons/stop.gif"),
    PAUSE("icons/coolbaricons/pause.png"),
    RECORDING("icons/coolbaricons/recording.gif"),
    FILTER("icons/coolbaricons/filter.gif"),
    //added for treeview
    NEWBOOK("icons/newBook.gif"),
    BOOK("icons/book.gif"),
    GAMEBOARD("icons/gameboard.gif"),
    MEBOOK("icons/meBook.gif"),
    REMOVE("icons/remove.gif"),
    REFRESH("icons/coolbaricons/refresh.gif"),
    MOVINGBOX("icons/movingBox.gif"),
    EXPANDALL("icons/expandall.gif"),
    COLLAPSEALL("icons/collapseall.gif"),
    EYE("icons/eye.png"),
    EYENO("icons/eye_no.png"),
    TREEREFRESH("icons/treeRefresh.png"),
    CONNECTCLOUDMONKEY("icons/cloud-icon.gif"),
    TREE("icons/coolbaricons/tree.png"),
    TIME("icons/coolbaricons/time.png"),
    SCREENSHOT("icons/coolbaricons/screenshot.png"),
    SCREENSHOTERROR("icons/coolbaricons/screenshot_error.png");
    

    
	public ImageDescriptor image;
	public String path;
	
	MonkeyTalkImagesEnum(String path){
		image = FoneMonkeyPlugin
				.getImageDescriptor(path);
		this.path = path;
	}
	

}