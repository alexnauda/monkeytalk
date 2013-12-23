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
package com.gorillalogic.monkeytalk.java.api;

import java.util.List;
import java.util.Map;

/**
 * An image. iOS: UIImage, Android: ImageView.
 */
public interface Image {
	/**
	 * Taps on the component. On Android, plays a 'click'. On iOS, plays a TouchDown/TouchMove/TouchUp sequence.
	 */
	public void tap();
	/**
	 * Taps on the component. On Android, plays a 'click'. On iOS, plays a TouchDown/TouchMove/TouchUp sequence.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void tap(Map<String, String> mods);

	/**
	 * Performs a long press on the component. On Android, plays a 'longClick'. On iOS, plays a longPush gesture.
	 */
	public void longPress();
	/**
	 * Performs a long press on the component. On Android, plays a 'longClick'. On iOS, plays a longPush gesture.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void longPress(Map<String, String> mods);

	/**
	 * Start touching the component.
	 */
	public void touchDown();
	/**
	 * Start touching the component.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchDown(Map<String, String> mods);
	/**
	 * Start touching the component.
	 * @param x x-coordinate of the touch
	 */
	public void touchDown(int x);
	/**
	 * Start touching the component.
	 * @param x x-coordinate of the touch
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchDown(int x, Map<String, String> mods);
	/**
	 * Start touching the component.
	 * @param x x-coordinate of the touch
	 * @param y y-coordinate of the touch
	 */
	public void touchDown(int x, int y);
	/**
	 * Start touching the component.
	 * @param x x-coordinate of the touch
	 * @param y y-coordinate of the touch
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchDown(int x, int y, Map<String, String> mods);

	/**
	 * Drag across the component
	 */
	public void touchMove();
	/**
	 * Drag across the component
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchMove(Map<String, String> mods);
	/**
	 * Drag across the component
	 * @param coords one or more (x,y) coordinate pairs specifying the path of the drag gesture
	 */
	public void touchMove(int... coords);
	/**
	 * Drag across the component
	 * @param coords one or more (x,y) coordinate pairs specifying the path of the drag gesture
	 */
	public void touchMove(List<Integer> coords);
	/**
	 * Drag across the component
	 * @param coords one or more (x,y) coordinate pairs specifying the path of the drag gesture
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchMove(List<Integer> coords, Map<String, String> mods);

	/**
	 * Stop touching the component.
	 */
	public void touchUp();
	/**
	 * Stop touching the component.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchUp(Map<String, String> mods);
	/**
	 * Stop touching the component.
	 * @param x x-coordinate of where touch is released
	 */
	public void touchUp(int x);
	/**
	 * Stop touching the component.
	 * @param x x-coordinate of where touch is released
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchUp(int x, Map<String, String> mods);
	/**
	 * Stop touching the component.
	 * @param x x-coordinate of where touch is released
	 * @param y y-coordinate of where touch is released
	 */
	public void touchUp(int x, int y);
	/**
	 * Stop touching the component.
	 * @param x x-coordinate of where touch is released
	 * @param y y-coordinate of where touch is released
	 * @param mods the MonkeyTalk modifiers
	 */
	public void touchUp(int x, int y, Map<String, String> mods);

	/**
	 * Pinch the component.
	 */
	public void pinch();
	/**
	 * Pinch the component.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void pinch(Map<String, String> mods);
	/**
	 * Pinch the component.
	 * @param scale The scale factor relative to the points of the two touches in screen coordinates
	 */
	public void pinch(float scale);
	/**
	 * Pinch the component.
	 * @param scale The scale factor relative to the points of the two touches in screen coordinates
	 * @param mods the MonkeyTalk modifiers
	 */
	public void pinch(float scale, Map<String, String> mods);
	/**
	 * Pinch the component.
	 * @param scale The scale factor relative to the points of the two touches in screen coordinates
	 * @param velocity The velocity of the pinch in scale factor per second (read-only)
	 */
	public void pinch(float scale, float velocity);
	/**
	 * Pinch the component.
	 * @param scale The scale factor relative to the points of the two touches in screen coordinates
	 * @param velocity The velocity of the pinch in scale factor per second (read-only)
	 * @param mods the MonkeyTalk modifiers
	 */
	public void pinch(float scale, float velocity, Map<String, String> mods);

	/**
	 * A simple directional swipe across the component.
	 */
	public void swipe();
	/**
	 * A simple directional swipe across the component.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void swipe(Map<String, String> mods);
	/**
	 * A simple directional swipe across the component.
	 * @param direction Left, Right, Up, or Down (case insensitive)
	 */
	public void swipe(String direction);
	/**
	 * A simple directional swipe across the component.
	 * @param direction Left, Right, Up, or Down (case insensitive)
	 * @param mods the MonkeyTalk modifiers
	 */
	public void swipe(String direction, Map<String, String> mods);

	/**
	 * Touch down at the first coordinate pair, move from pair to pair for all the given coordinates, and touch up at the last coordinate pair.
	 */
	public void drag();
	/**
	 * Touch down at the first coordinate pair, move from pair to pair for all the given coordinates, and touch up at the last coordinate pair.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void drag(Map<String, String> mods);
	/**
	 * Touch down at the first coordinate pair, move from pair to pair for all the given coordinates, and touch up at the last coordinate pair.
	 * @param coords one or more (x,y) coordinate pairs specifying the path of a drag gesture
	 */
	public void drag(int... coords);
	/**
	 * Touch down at the first coordinate pair, move from pair to pair for all the given coordinates, and touch up at the last coordinate pair.
	 * @param coords one or more (x,y) coordinate pairs specifying the path of a drag gesture
	 */
	public void drag(List<Integer> coords);
	/**
	 * Touch down at the first coordinate pair, move from pair to pair for all the given coordinates, and touch up at the last coordinate pair.
	 * @param coords one or more (x,y) coordinate pairs specifying the path of a drag gesture
	 * @param mods the MonkeyTalk modifiers
	 */
	public void drag(List<Integer> coords, Map<String, String> mods);

	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @return the value
	 */
	public String get();
	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @param mods the MonkeyTalk modifiers
	 * @return the value
	 */
	public String get(Map<String, String> mods);
	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @param propPath the property name or path expression (defaults to 'value')
	 * @return the value
	 */
	public String get(String propPath);
	/**
	 * Gets the value of the given property from the component, and set it into the given variable name.
	 * @param propPath the property name or path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 * @return the value
	 */
	public String get(String propPath, Map<String, String> mods);

	/**
	 * Verifies that a property of the component has some expected value.
	 */
	public void verify();
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verify(Map<String, String> mods);
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param expectedValue the expected value of the property. If null, verifies the existence of the component.
	 */
	public void verify(String expectedValue);
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param expectedValue the expected value of the property. If null, verifies the existence of the component.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verify(String expectedValue, Map<String, String> mods);
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param expectedValue the expected value of the property. If null, verifies the existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 */
	public void verify(String expectedValue, String propPath);
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param expectedValue the expected value of the property. If null, verifies the existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verify(String expectedValue, String propPath, Map<String, String> mods);
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param expectedValue the expected value of the property. If null, verifies the existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 */
	public void verify(String expectedValue, String propPath, String failMessage);
	/**
	 * Verifies that a property of the component has some expected value.
	 * @param expectedValue the expected value of the property. If null, verifies the existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verify(String expectedValue, String propPath, String failMessage, Map<String, String> mods);

	/**
	 * Verifies that a property of the component does not have some value.
	 */
	public void verifyNot();
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNot(Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param expectedValue the value the component shouldn't have. If null, verifies the non-existence of the component.
	 */
	public void verifyNot(String expectedValue);
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param expectedValue the value the component shouldn't have. If null, verifies the non-existence of the component.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNot(String expectedValue, Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param expectedValue the value the component shouldn't have. If null, verifies the non-existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 */
	public void verifyNot(String expectedValue, String propPath);
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param expectedValue the value the component shouldn't have. If null, verifies the non-existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNot(String expectedValue, String propPath, Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param expectedValue the value the component shouldn't have. If null, verifies the non-existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 */
	public void verifyNot(String expectedValue, String propPath, String failMessage);
	/**
	 * Verifies that a property of the component does not have some value.
	 * @param expectedValue the value the component shouldn't have. If null, verifies the non-existence of the component.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNot(String expectedValue, String propPath, String failMessage, Map<String, String> mods);

	/**
	 * Verifies that a property of the component matches some regular expression.
	 */
	public void verifyRegex();
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyRegex(Map<String, String> mods);
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param regex the regular expression to match
	 */
	public void verifyRegex(String regex);
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param regex the regular expression to match
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyRegex(String regex, Map<String, String> mods);
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param regex the regular expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 */
	public void verifyRegex(String regex, String propPath);
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param regex the regular expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyRegex(String regex, String propPath, Map<String, String> mods);
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param regex the regular expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 */
	public void verifyRegex(String regex, String propPath, String failMessage);
	/**
	 * Verifies that a property of the component matches some regular expression.
	 * @param regex the regular expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyRegex(String regex, String propPath, String failMessage, Map<String, String> mods);

	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 */
	public void verifyNotRegex();
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotRegex(Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param regex the regular expression that should not match.
	 */
	public void verifyNotRegex(String regex);
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param regex the regular expression that should not match.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotRegex(String regex, Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param regex the regular expression that should not match.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 */
	public void verifyNotRegex(String regex, String propPath);
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param regex the regular expression that should not match.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotRegex(String regex, String propPath, Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param regex the regular expression that should not match.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 */
	public void verifyNotRegex(String regex, String propPath, String failMessage);
	/**
	 * Verifies that a property of the component does not have a value matching a regular expression.
	 * @param regex the regular expression that should not match.
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotRegex(String regex, String propPath, String failMessage, Map<String, String> mods);

	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 */
	public void verifyWildcard();
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyWildcard(Map<String, String> mods);
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param wildcard the wildcard expression to match
	 */
	public void verifyWildcard(String wildcard);
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param wildcard the wildcard expression to match
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyWildcard(String wildcard, Map<String, String> mods);
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param wildcard the wildcard expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 */
	public void verifyWildcard(String wildcard, String propPath);
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param wildcard the wildcard expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyWildcard(String wildcard, String propPath, Map<String, String> mods);
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param wildcard the wildcard expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 */
	public void verifyWildcard(String wildcard, String propPath, String failMessage);
	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * @param wildcard the wildcard expression to match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyWildcard(String wildcard, String propPath, String failMessage, Map<String, String> mods);

	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 */
	public void verifyNotWildcard();
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotWildcard(Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param wildcard the wildcard expression that should not match
	 */
	public void verifyNotWildcard(String wildcard);
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param wildcard the wildcard expression that should not match
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotWildcard(String wildcard, Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param wildcard the wildcard expression that should not match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 */
	public void verifyNotWildcard(String wildcard, String propPath);
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param wildcard the wildcard expression that should not match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotWildcard(String wildcard, String propPath, Map<String, String> mods);
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param wildcard the wildcard expression that should not match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 */
	public void verifyNotWildcard(String wildcard, String propPath, String failMessage);
	/**
	 * Verifies that a property of the component does not have a value matching some wildcard expression.
	 * @param wildcard the wildcard expression that should not match
	 * @param propPath the property name or property path expression (defaults to 'value')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyNotWildcard(String wildcard, String propPath, String failMessage, Map<String, String> mods);

	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 */
	public void verifyImage();
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyImage(Map<String, String> mods);
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param expectedImagePath the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component
	 */
	public void verifyImage(String expectedImagePath);
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param expectedImagePath the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyImage(String expectedImagePath, Map<String, String> mods);
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param expectedImagePath the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component
	 * @param tolerance the 'fuzziness' to apply to the match in terms of color and sharpness, where 0=perfect match and 10=maximum tolerance (defaults to '0')
	 */
	public void verifyImage(String expectedImagePath, int tolerance);
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param expectedImagePath the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component
	 * @param tolerance the 'fuzziness' to apply to the match in terms of color and sharpness, where 0=perfect match and 10=maximum tolerance (defaults to '0')
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyImage(String expectedImagePath, int tolerance, Map<String, String> mods);
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param expectedImagePath the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component
	 * @param tolerance the 'fuzziness' to apply to the match in terms of color and sharpness, where 0=perfect match and 10=maximum tolerance (defaults to '0')
	 * @param failMessage the custom failure message
	 */
	public void verifyImage(String expectedImagePath, int tolerance, String failMessage);
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 * @param expectedImagePath the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component
	 * @param tolerance the 'fuzziness' to apply to the match in terms of color and sharpness, where 0=perfect match and 10=maximum tolerance (defaults to '0')
	 * @param failMessage the custom failure message
	 * @param mods the MonkeyTalk modifiers
	 */
	public void verifyImage(String expectedImagePath, int tolerance, String failMessage, Map<String, String> mods);

	/**
	 * Waits for a component to be created and/or become visible.
	 */
	public void waitFor();
	/**
	 * Waits for a component to be created and/or become visible.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void waitFor(Map<String, String> mods);
	/**
	 * Waits for a component to be created and/or become visible.
	 * @param seconds how many seconds to wait before giving up and failing the command (defaults to 10).
	 */
	public void waitFor(int seconds);
	/**
	 * Waits for a component to be created and/or become visible.
	 * @param seconds how many seconds to wait before giving up and failing the command (defaults to 10).
	 * @param mods the MonkeyTalk modifiers
	 */
	public void waitFor(int seconds, Map<String, String> mods);

	/**
	 * Waits for a component to no longer be found, or become hidden.
	 */
	public void waitForNot();
	/**
	 * Waits for a component to no longer be found, or become hidden.
	 * @param mods the MonkeyTalk modifiers
	 */
	public void waitForNot(Map<String, String> mods);
	/**
	 * Waits for a component to no longer be found, or become hidden.
	 * @param seconds how many seconds to wait before giving up and failing the command (defaults to 10).
	 */
	public void waitForNot(int seconds);
	/**
	 * Waits for a component to no longer be found, or become hidden.
	 * @param seconds how many seconds to wait before giving up and failing the command (defaults to 10).
	 * @param mods the MonkeyTalk modifiers
	 */
	public void waitForNot(int seconds, Map<String, String> mods);
}
