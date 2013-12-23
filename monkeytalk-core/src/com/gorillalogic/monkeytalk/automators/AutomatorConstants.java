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
package com.gorillalogic.monkeytalk.automators;

/**
 * Static helper for Automator.
 */
public class AutomatorConstants {
	public static final String ACTION_TAP = "tap";
	public static final String ACTION_ENTER_TEXT = "enterText";
	public static final String ACTION_SELECT_INDEX = "selectIndex";
	public static final String ACTION_SELECT_ROW = "selectRow";
	public static final String ACTION_SELECT = "select";
	public static final String ACTION_LONG_SELECT = "longSelect";
	public static final String ACTION_LONG_SELECT_INDEX = "longSelectIndex";
	public static final String ACTION_ENTER_DATE = "enterDate";
	public static final String ACTION_BACK = "back";
	public static final String ACTION_FORWARD = "forward";
	public static final String ACTION_DUMP = "dumpTree";
	public static final String ACTION_LONG_PRESS = "longPress";
	public static final String ACTION_GET = "get";
	public static final String ACTION_VERIFY = "verify";
	public static final String ACTION_VERIFY_NOT = "verifyNot";
	public static final String ACTION_WAITFOR = "waitFor";
	public static final String ACTION_WAITFOR_NOT = "waitForNot";
	public static final String ACTION_SWIPE = "swipe";
	public static final String ACTION_MOVE = "move";
	public static final String ACTION_DRAG = "drag";
	public static final String ACTION_MENU = "menu";
	public static final String ACTION_ROTATE = "rotate";
	public static final String ACTION_ON = "on";
	public static final String ACTION_OFF = "off";
	public static final String ACTION_CLEAR = "clear";
	public static final String ACTION_SCREENSHOT = "screenshot";
	public static final String PROPERTY_VALUE = "value";
	public static final String ACTION_OPEN = "open";
	public static final String ACTION_CLICK = "click";
	public static final String ACTION_VERIFY_REGEX = "verifyRegex";
	public static final String ACTION_VERIFY_NOT_REGEX = "verifyNotRegex";
	public static final String ACTION_VERIFY_WILDCARD = "verifyWildcard";
	public static final String ACTION_VERIFY_NOT_WILDCARD = "verifyNotWildcard";
	public static final String ACTION_VERIFY_IMAGE = "verifyImage";
	public static final String ACTION_PINCH = "pinch";
	public static final String ACTION_SCROLL_TO_ROW = "scrollToRow";
	public static final String ACTION_SCROLL = "scroll";

	public static final String ACTION_EXEC = "Exec";
	public static final String ACTION_EXECANDRET = "ExecAndReturn";

	public static final String TOUCH_UP = "TouchUp";
	public static final String TOUCH_DOWN = "TouchDown";
	public static final String TOUCH_MOVE = "TouchMove";

	public static final String TYPE_BUTTON = "Button";
	public static final String TYPE_CHECK_BOX = "CheckBox";
	public static final String TYPE_DATE_PICKER = "DatePicker";
	public static final String TYPE_DEVICE = "Device";
	public static final String TYPE_APP = "App";
	public static final String TYPE_TABLE = "Table";
	public static final String TYPE_MENU = "Menu";
	public static final String TYPE_RATING_BAR = "RatingBar";
	public static final String TYPE_SLIDER = "Slider";
	public static final String TYPE_ITEM_SELECTOR = "ItemSelector";
	public static final String TYPE_TABBAR = "TabBar";
	public static final String TYPE_VIEW = "View";
	public static final String TYPE_SCROLLER = "Scroller";
	public static final String TYPE_INPUT = "Input";
	public static final String TYPE_TEXTAREA = "TextArea";
	public static final String TYPE_IMAGE = "Image";
	public static final String TYPE_GRID = "Grid";

	private AutomatorConstants() {
	}
}