/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.shell;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;

/**
 * MonkeyTalk shell printer with optional color.
 * 
 * Colors:
 * <ul>
 * <li>0 = normal</li>
 * <li>2 = dark greay</li>
 * <li>4 = underline</li>
 * <li>5 = blink</li>
 * <li>7 = inverted</li>
 * <li>31 = red</li>
 * <li>32 = green</li>
 * <li>33 = yellow</li>
 * <li>34 = blue</li>
 * <li>35 = magenta</li>
 * <li>36 = cyan</li>
 * <li>37 = grey</li>
 * <li>41 = red bg, white text</li>
 * <li>42 = green bg, white text</li>
 * <li>43 = yellow bg, white text</li>
 * <li>44 = blue bg, white text</li>
 * </ul>
 */
public class Print {
	private enum Color {
		BLACK(30), RED(31), GREEN(32), YELLOW(33), BLUE(34), MAGENTA(35), CYAN(36), GREY(37);

		private final int c;

		private Color(int c) {
			this.c = c;
		}

		@Override
		public String toString() {
			return Integer.toString(c);
		}
	}

	private static boolean color = false;

	public static void setColor(boolean color) {
		Print.color = color;
	}

	public static void info(Object obj) {
		color(obj.toString(), Color.CYAN, true);
	}

	public static void print(Object obj) {
		color(obj.toString(), Color.GREY, false);
	}

	public static void println(Object obj) {
		color(obj.toString(), Color.GREY, true);
	}

	public static void println(PlaybackResult result) {
		if (result.getStatus() == PlaybackStatus.OK) {
			color(result.toString(), Color.GREEN, true);
			if (result.getWarning() != null && result.getWarning().length() > 0) {
				color("WARNING: " + result.getWarning(), Color.YELLOW, true);
			}
			if (result.getImageFile() != null && result.getImageFile().exists()) {
				color("IMAGE: " + result.getImageFile().getAbsolutePath(), Color.CYAN, true);
			}
		} else {
			err(result.toString());
		}
	}

	public static void err(Object obj) {
		color(obj.toString(), Color.RED, true);
	}

	private static void color(String msg, Color c, boolean println) {
		System.out.print(color ? "\u001B[" + c + "m" + msg + "\u001B[0m" : msg);
		System.out.print(println ? "\n" : "");
	}
}
