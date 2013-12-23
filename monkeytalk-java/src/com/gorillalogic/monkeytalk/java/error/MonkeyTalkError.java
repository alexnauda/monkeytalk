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
package com.gorillalogic.monkeytalk.java.error;

/**
 * Thrown to trigger a test error (as opposed to throwing {@link MonkeyTalkFailure} which triggers a
 * test failure).
 */
public class MonkeyTalkError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a {@code MonkeyTalkError} without an error message.
	 */
	public MonkeyTalkError() {
		super();
	}

	/**
	 * Construct a {@code MonkeyTalkError} with the given error message.
	 * 
	 * @param message
	 *            the error message
	 */
	public MonkeyTalkError(String message) {
		super(message);
	}

	/**
	 * Construct a {@code MonkeyTalkError} with the given error message and triggering exception.
	 * 
	 * @param message
	 *            the error message
	 * @param cause
	 *            the exception that trigger the error
	 */
	public MonkeyTalkError(String message, Throwable cause) {
		super(message, cause);
	}
}
