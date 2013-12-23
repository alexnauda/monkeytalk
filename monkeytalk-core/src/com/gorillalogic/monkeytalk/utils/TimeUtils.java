package com.gorillalogic.monkeytalk.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
	public static final String UNDEFINED = "undefined";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATEHOUR_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final SimpleDateFormat DATESECOND_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat DATESTAMP_TIMEZONE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS z");
	public static final SimpleDateFormat DATESTAMP_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private TimeUtils() {
	}

	public static String printDuration(long start, long stop) {
		long t = (stop - start) / 1000;
		return (t < 0 ? "0:00:00" : formatTime(t / 3600, (t % 3600) / 60, (t % 60)));
	}

	public static String printDuration(Date start, Date stop) {
		if (start == null || stop == null) {
			return UNDEFINED;
		}
		return printDuration(start.getTime(), stop.getTime());
	}

	public static String formatTime(int h, int m, int s) {
		return String.format("%d:%02d:%02d", h, m, s);
	}

	private static String formatTime(long h, long m, long s) {
		return formatTime((int) h, (int) m, (int) s);
	}

	public static String formatDate(Date d) {
		return (d != null ? DATESTAMP_FORMAT.format(d) : "NULL");
	}

	public static String formatDateWithTimezone(Date d) {
		return (d != null ? DATESTAMP_TIMEZONE_FORMAT.format(d) : "NULL");
	}

	public static String formatDate(long t) {
		return formatDate(new Date(t));
	}
}
