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

import com.gorillalogic.monkeytalk.processor.report.Report;

/**
 * Callback interface for monitoring suite playback. Provides callback events on suite start and
 * stop, plus events for each individual test start and completion.
 */
public interface SuiteListener {
	/**
	 * Callback fires on suite start.
	 * 
	 * @param total
	 *            the total number of tests in the suite
	 */
	void onRunStart(int total);

	/**
	 * Callback fires on suite completion.
	 * 
	 * @param result
	 *            the playback result (OK, ERROR, or FAILURE)
	 * @param report
	 *            the JUnit-compatible XML report
	 */
	void onRunComplete(PlaybackResult result, Report report);

	/**
	 * Callback fires on test start.
	 * 
	 * @param name
	 *            the name of the running test
	 * @param num
	 *            the current test number
	 * @param total
	 *            the total number of tests in the suite
	 */
	void onTestStart(String name, int num, int total);

	/**
	 * Callback fires on test completion.
	 * 
	 * @param result
	 *            the playback result (OK, ERROR, or FAILURE)
	 * @param report
	 *            the <i>partial</i> report, including the result for the current test
	 */
	void onTestComplete(PlaybackResult result, Report report);
	
	/**
	 * Callback fires on suite run. This differs from onSuiteStart because suite start is run once, when the main suite starts
	 * 
	 * @param total
	 *            the total number of tests in the suite
	 */
	void onSuiteStart(int total);

	/**
	 * Callback fires on suite run completion. This differs from onSuiteComplete because suite start is run once, when the main suite starts
	 * 
	 * @param result
	 *            the playback result (OK, ERROR, or FAILURE)
	 * @param report
	 *            the JUnit-compatible XML report
	 */
	void onSuiteComplete(PlaybackResult result, Report report);


}