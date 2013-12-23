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
package com.gorillalogic.monkeytalk.utils.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class FileUtilsTest {

	@Test
	public void testRemoveExtension() {
		assertThat(FileUtils.removeExt("foo.mt", CommandWorld.SCRIPT_EXT), is("foo"));
		assertThat(FileUtils.removeExt("foo.MT", CommandWorld.SCRIPT_EXT), is("foo"));
		assertThat(FileUtils.removeExt("FOO.MT", CommandWorld.SCRIPT_EXT), is("FOO"));
		assertThat(FileUtils.removeExt("foo.mts", CommandWorld.SCRIPT_EXT), is("foo.mts"));
		assertThat(FileUtils.removeExt(".mt", CommandWorld.SCRIPT_EXT), is(""));
		assertThat(FileUtils.removeExt("", CommandWorld.SCRIPT_EXT), is(""));
		assertThat(FileUtils.removeExt(null, CommandWorld.SCRIPT_EXT), nullValue());
	}
}