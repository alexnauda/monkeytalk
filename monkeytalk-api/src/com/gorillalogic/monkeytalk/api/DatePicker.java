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
 * A component that edits a date. iOS: UIDatePicker. Android: DatePicker.
 * 
 * @prop value - the date (as YYYY-MM-DD), or time (as HH:MM am/pm), or date and time (as YYYY-MM-DD
 *       HH:MM am/pm)
 * 
 * @mapiOS UIDatePicker
 * @mapAndroid DatePicker
 */
public interface DatePicker extends View {

	/**
	 * Enter the date value.
	 * 
	 * @param date
	 *            A date with the format YYYY-MM-DD where YYYY is the year, MM is the month (01-12),
	 *            and DD is the day (01-31).
	 */
	public void enterDate(String date);

	/**
	 * Enter the time value.
	 * 
	 * @param time
	 *            A time with the format hh:mm am/pm, where hh is the hour (01-12), mm is the minute
	 *            (00-59), and am/pm is the marker.
	 */
	public void enterTime(String time);

	/**
	 * Enter the date and time value.
	 * 
	 * @param dateAndTime
	 *            A date and time with the format YYYY-MM-DD hh:mm am/pm, where YYYY is the year, MM
	 *            is the month (01-12), DD is the day (01-31), hh is the hour (01-12), mm is the
	 *            minute (00-59), and am/pm is the marker.
	 */
	public void enterDateAndTime(String dateAndTime);

	/**
	 * Enter the count down timer value. (iOS only)
	 * 
	 * @param timer
	 *            A timer with the format hh:mm, where hh is the hour (00-23), and mm is the minute
	 *            (00-59).
	 */
	public void enterCountDownTimer(String timer);
}