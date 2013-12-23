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
package com.gorillalogic.monkeytalk.java.utils.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

import com.gorillalogic.monkeytalk.java.utils.Mods;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class ModsTest extends TestHelper {

	@Test
	public void testModsBuilder() {
		Map<String, String> mods = new Mods.Builder().thinktime(123).timeout(456).ignore(true)
				.screenshotOnError(false).shouldFail(true).build();

		assertThat(mods.size(), is(5));
		assertThat(mods.get("thinktime"), is("123"));
		assertThat(mods.get("timeout"), is("456"));
		assertThat(mods.get("ignore"), is("true"));
		assertThat(mods.get("screenshotonerror"), is("false"));
		assertThat(mods.get("shouldfail"), is("true"));
		assertThat(mods.get("missing"), nullValue());
	}

	@Test
	public void testModsStaticHelpers() {
		Map<String, String> mods = Mods.of(Mods.THINKTIME, "123", Mods.TIMEOUT, "456",
				Mods.SCREENSHOT_ON_ERROR, "false");

		assertThat(mods.size(), is(3));
		assertThat(mods.get("thinktime"), is("123"));
		assertThat(mods.get("timeout"), is("456"));
		assertThat(mods.get("ignore"), nullValue());
		assertThat(mods.get("screenshotonerror"), is("false"));
		assertThat(mods.get("shouldfail"), nullValue());
	}
}
