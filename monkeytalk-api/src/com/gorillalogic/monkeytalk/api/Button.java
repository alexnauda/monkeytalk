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
 * A Button. iOS: UIButton. Android: Button. Web: Button tag, or Input tag with type="submit" or type="reset". If
 * the button has a label, it is used as the monkeyId.
 * 
 * @prop value - the button label text
 * 
 * @mapiOS UIButton
 * @mapAndroid Button
 * @mapHtml Button, Input type="button", Input type="submit", Input type="reset", Input type="image"
 */
public interface Button extends View {
}