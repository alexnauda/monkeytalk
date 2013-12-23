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
package com.gorillalogic.monkeytalk.processor;

import com.gorillalogic.monkeytalk.sender.Response;

/**
 * Callback interface for monitoring script playback. Provides callback events on script start or
 * stop, plus events for individual command start and stop within each script.
 */
public interface PlaybackListener {
	/**
	 * Callback fires on script start.
	 * 
	 * @param scope
	 *            the current scope
	 */
	void onScriptStart(Scope scope);

	/**
	 * Callback fires on script complete.
	 * 
	 * @param scope
	 *            the current scope
	 * @param result
	 *            the playback result, including a status (OK, ERROR, or FAILURE) and a message
	 */
	void onScriptComplete(Scope scope, PlaybackResult result);

	/**
	 * Callback fires on command start.
	 * 
	 * @param scope
	 *            the current scope
	 */
	void onStart(Scope scope);

	/**
	 * Callback fires on command complete.
	 * 
	 * @param scope
	 *            the current scope
	 * @param response
	 *            the playback response returned by the agent, including the HTTP response code, raw
	 *            JSON body, a status (OK, ERROR, or FAILURE), and a message
	 */
	void onComplete(Scope scope, Response response);

	/**
	 * Callback fire on print output.
	 * 
	 * @param message
	 *            the message to be printed
	 */
	void onPrint(String message);
}