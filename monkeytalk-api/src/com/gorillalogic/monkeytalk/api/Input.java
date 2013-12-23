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
package com.gorillalogic.monkeytalk.api;

/**
 * A single-line input field. iOS: UITextField. Android: single-line editable TextView. Web: Input
 * tag with type="text". If the input as a hint/prompt, it is used as the monkeyId.
 * 
 * @prop value - the input field text
 */
public interface Input extends Label {

	/**
	 * Enter text into the input field.
	 * 
	 * @param text
	 *            the text to enter
	 * @param hitEnter
	 *            if "enter", hit the Enter/Return/Done/Next key after entering the text.
	 */
	public void enterText(String text, String hitEnter);

	/**
	 * Clear text from the input field.
	 */
	public void clear();
}