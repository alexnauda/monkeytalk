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
package com.gorillalogic.monkeytalk.processor.report;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;

/**
 * Enum of all possible test results.
 */
public enum TestResult {
	OK, ERROR, FAILURE, SKIPPED;

	/**
	 * Helper to get the {@code TestResult} from the {@link PlaybackStatus} set on the given
	 * {@link PlaybackResult}.
	 * 
	 * @param result
	 *            the playback result
	 * @return the test result
	 */
	public static TestResult getTestResultFromPlaybackStatus(PlaybackResult result) {
		if (result.getStatus() == PlaybackStatus.OK) {
			return (result.getMessage() != null && result.getMessage().equals("ignored") ? SKIPPED : OK);
		} else if (result.getStatus() == PlaybackStatus.FAILURE) {
			return FAILURE;
		}
		return ERROR;
	}

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}