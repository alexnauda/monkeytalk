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
package com.gorillalogic.monkeytalk.java.utils;

import java.util.HashMap;
import java.util.Map;

/** Helper to build a map of MonkeyTalk modifiers. */
public enum Mods {
	/**
	 * The {@code timeout} modifier. How long to search for the current component before failing (in
	 * ms).
	 */
	TIMEOUT("timeout"),
	/**
	 * The {@code thinktime} modifier. How long to wait before starting to play the current command
	 * (in ms).
	 */
	THINKTIME("thinktime"),
	/**
	 * The {@code ignore} modifier. If true, ignore the current command.
	 */
	IGNORE("ignore"),
	/**
	 * The {@code screenshotonerror} modifier. If true, then take a screenshot if the current
	 * command doesn't play successfully (defaults to true).
	 */
	SCREENSHOT_ON_ERROR("screenshotonerror"),
	/**
	 * The {@code shouldfail} modifier. If true, the current command is expected to fail.
	 */
	SHOULD_FAIL("shouldfail");

	private String mod;

	private Mods(String mod) {
		this.mod = mod;
	}

	@Override
	public String toString() {
		return mod;
	}

	public static class Builder {
		private Map<String, String> mods;

		/** Construct an empty modifier map. */
		public Builder() {
			mods = new HashMap<String, String>();
		}

		/** Construct a modifier map using the given non-{@code null} map. */
		public Builder(Map<String, String> mods) {
			if (mods == null) {
				mods = new HashMap<String, String>();
			}
			this.mods = mods;
		}

		/**
		 * The {@code timeout} modifier. How long to search for the current component before failing
		 * (in ms).
		 */
		public Builder timeout(int timeout) {
			mods.put(TIMEOUT.toString(), "" + timeout);
			return this;
		}

		/**
		 * The {@code thinktime} modifier. How long to wait before starting to play the current
		 * command (in ms).
		 */
		public Builder thinktime(int thinktime) {
			mods.put(THINKTIME.toString(), "" + thinktime);
			return this;
		}

		/** The {@code ignore} modifier. If true, ignore the current command. */
		public Builder ignore(boolean ignore) {
			mods.put(IGNORE.toString(), "" + ignore);
			return this;
		}

		/**
		 * The {@code screenshotonerror} modifier. If true, then take a screenshot if the current
		 * command doesn't play successfully (defaults to true).
		 */
		public Builder screenshotOnError(boolean screenshotOnError) {
			mods.put(SCREENSHOT_ON_ERROR.toString(), "" + screenshotOnError);
			return this;
		}

		/** The {@code shouldfail} modifier. If true, the current command is expected to fail. */
		public Builder shouldFail(boolean shouldFail) {
			mods.put(SHOULD_FAIL.toString(), "" + shouldFail);
			return this;
		}

		/** Build the modifiers map. */
		public Map<String, String> build() {
			return mods;
		}
	}

	/** Helper to build a 1-entry modifiers map. */
	public static Map<String, String> of(Mods m, String v) {
		Map<String, String> map = new HashMap<String, String>(1);
		map.put(m.toString(), v);
		return map;
	}

	/** Helper to build a 2-entry modifiers map. */
	public static Map<String, String> of(Mods m1, String v1, Mods m2, String v2) {
		Map<String, String> map = new HashMap<String, String>(2);
		map.put(m1.toString(), v1);
		map.put(m2.toString(), v2);
		return map;
	}

	/** Helper to build a 3-entry modifiers map. */
	public static Map<String, String> of(Mods m1, String v1, Mods m2, String v2, Mods m3, String v3) {
		Map<String, String> map = new HashMap<String, String>(4);
		map.put(m1.toString(), v1);
		map.put(m2.toString(), v2);
		map.put(m3.toString(), v3);
		return map;
	}

	/** Helper to build a 4-entry modifiers map. */
	public static Map<String, String> of(Mods m1, String v1, Mods m2, String v2, Mods m3,
			String v3, Mods m4, String v4) {
		Map<String, String> map = new HashMap<String, String>(4);
		map.put(m1.toString(), v1);
		map.put(m2.toString(), v2);
		map.put(m3.toString(), v3);
		map.put(m4.toString(), v4);
		return map;
	}
}