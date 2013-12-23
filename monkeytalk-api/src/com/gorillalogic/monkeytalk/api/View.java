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
 * Base class for all UI components. On iOS, monkeyId defaults to the accessibilityLabel. On
 * Android, monkeyId defaults to contentDescription if one exists, otherwise the component's tag
 * value if it has a string value.
 * 
 * @prop value
 */
public interface View extends Verifiable {

	/**
	 * Taps on the component. On Android, plays a "click". On iOS, plays a
	 * TouchDown/TouchMove/TouchUp sequence.
	 */
	public void tap();

	/**
	 * Performs a long press on the component. On Android, plays a "longClick". On iOS, plays a
	 * longPush gesture.
	 */
	public void longPress();

	/**
	 * Start touching the component.
	 * 
	 * @param x
	 *            x-coordinate of the touch
	 * @param y
	 *            y-coordinate of the touch
	 */
	public void touchDown(int x, int y);

	/**
	 * Drag across the component
	 * 
	 * @param coords
	 *            one or more (x,y) coordinate pairs specifying the path of the drag gesture
	 */
	public void touchMove(int... coords);

	/**
	 * Stop touching the component.
	 * 
	 * @param x
	 *            x-coordinate of where touch is released
	 * @param y
	 *            y-coordinate of where touch is released
	 */
	public void touchUp(int x, int y);

	/**
	 * Pinch the component.
	 * 
	 * @param scale
	 *            The scale factor relative to the points of the two touches in screen coordinates
	 * @param velocity
	 *            The velocity of the pinch in scale factor per second (read-only)
	 */
	public void pinch(float scale, float velocity);

	/**
	 * A simple directional swipe across the component.
	 * 
	 * @param direction
	 *            Left, Right, Up, or Down (case insensitive)
	 */
	public void swipe(String direction);

	/**
	 * Touch down at the first coordinate pair, move from pair to pair for all the given
	 * coordinates, and touch up at the last coordinate pair.
	 * 
	 * @param coords
	 *            one or more (x,y) coordinate pairs specifying the path of a drag gesture
	 */
	public void drag(int... coords);

	/**
	 * Gets the value of the given property from the component, and set it into the given variable
	 * name.
	 * 
	 * @param variable
	 *            the name of the variable to set
	 * @param propPath
	 *            the property name or path expression (defaults to "value")
	 * @return the value
	 */
	public String get(String variable, String propPath);
}