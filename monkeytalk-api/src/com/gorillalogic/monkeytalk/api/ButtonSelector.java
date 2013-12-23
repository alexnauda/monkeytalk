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
 * A group of radio (mutually exclusive) buttons. iOS: UISegmentedControl. Android: RadioGroup. Web:
 * A set of Input tags with type="radio" and name="group".
 * 
 * @prop value - the selected button's label
 * 
 * @aliasAndroid RadioGroup
 * 
 * @mapiOS UISegmentedControl
 * @mapAndroid RadioGroup
 * @mapHtml Input type="radio" name="group"
 */
public interface ButtonSelector extends ItemSelector {
}