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
 * Base class for all verifiable components.
 * 
 * @prop value
 */
public interface Verifiable extends MTObject {

	/**
	 * Verifies that a property of the component has some expected value.
	 * 
	 * @param expectedValue
	 *            the expected value of the property. If null, verifies the existence of the
	 *            component.
	 * @param propPath
	 *            the property name or property path expression (defaults to "value")
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verify(String expectedValue, String propPath, String failMessage);

	/**
	 * Verifies that a property of the component does not have some value.
	 * 
	 * @param expectedValue
	 *            the value the component shouldn't have. If null, verifies the non-existence of the
	 *            component.
	 * @param propPath
	 *            the property name or property path expression (defaults to "value")
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verifyNot(String expectedValue, String propPath, String failMessage);

	/**
	 * Verifies that a property of the component matches some regular expression.
	 * 
	 * @param regex
	 *            the regular expression to match
	 * @param propPath
	 *            the property name or property path expression (defaults to "value")
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verifyRegex(String regex, String propPath, String failMessage);

	/**
	 * Verifies that a property of the component does not have a value matching a regular
	 * expression.
	 * 
	 * @param regex
	 *            the regular expression that should not match.
	 * @param propPath
	 *            the property name or property path expression (defaults to "value")
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verifyNotRegex(String regex, String propPath, String failMessage);

	/**
	 * Verifies that a property of the component matches some wildcard expression.
	 * 
	 * @param wildcard
	 *            the wildcard expression to match
	 * @param propPath
	 *            the property name or property path expression (defaults to "value")
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verifyWildcard(String wildcard, String propPath, String failMessage);

	/**
	 * Verifies that a property of the component does not have a value matching some wildcard
	 * expression.
	 * 
	 * @param wildcard
	 *            the wildcard expression that should not match
	 * @param propPath
	 *            the property name or property path expression (defaults to "value")
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verifyNotWildcard(String wildcard, String propPath, String failMessage);
	
	/**
	 * Verifies that the screen image of a component matches the expected appearance.
	 *
	 * @param expectedImagePath
	 * 				the project-relative path to an image file, which contains the expected appearance.
	 * 				If the file does not exist, it will be created from the current appearance of the component
	 * @param tolerance
	 * 				the 'fuzziness' to apply to the match in terms of color and sharpness,
	 *  			where 0=perfect match and 10=maximum tolerance (defaults to '0')
	 * @param failMessage
	 *            the custom failure message
	 */
	public void verifyImage(String expectedImagePath, int tolerance, String failMessage);
	
	/**
	 * Waits for a component to be created and/or become visible.
	 *
	 * @param seconds
	 * 				how many seconds to wait before giving up and failing the command (defaults to 10).
	 */
	public void waitFor(int seconds);
	
	/**
	 * Waits for a component to no longer be found, or become hidden.
	 *
	 * @param seconds
	 * 				how many seconds to wait before giving up and failing the command (defaults to 10).
	 */
	public void waitForNot(int seconds);
	
}