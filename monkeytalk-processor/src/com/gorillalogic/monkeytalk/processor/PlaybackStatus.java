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
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;

/**
 * Enum for the three possible command playback results: OK, ERROR, or FAILURE.
 */
public enum PlaybackStatus {
	/**
	 * Successfully played MonkeyTalk command.
	 */
	OK,
	/**
	 * Error playing MonkeyTalk command (halt playback).
	 */
	ERROR,
	/**
	 * Failed to verify MonkeyTalk command (fail the test).
	 */
	FAILURE;

	/**
	 * Helper to get the {@code PlaybackStatus} from the {@link ResponseStatus}
	 * set on the given {@link Response}.
	 * 
	 * @param resp
	 *            the response
	 * @return the playback status
	 */
	public static PlaybackStatus getStatusFromResponse(Response resp) {
		if (resp.getStatus() == ResponseStatus.OK) {
			return OK;
		} else if (resp.getStatus() == ResponseStatus.FAILURE) {
			return FAILURE;
		}
		return ERROR;
	}
}