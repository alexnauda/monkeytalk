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
package com.gorillalogic.fonemonkey.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.gorillalogic.fonemonkey.PropertyUtil;

public class PropertyUtilTest {

	@Test
	public void testWildcardMatcher() throws IOException {
		String src = "foobar bazbaz 123";

		List<String> goods = Arrays.asList(src, "*", "foo*", "*foo*", "*123", "*123*", "foo*123",
				"f????r b????z 1?3", "*baz*", "?????????????????", "f***3", "foobar bazbaz 123");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					PropertyUtil.wildcardMatch(good, src), is(true));
		}

		List<String> bads = Arrays.asList("", "foo", "?foo", "*foo", "123*", "foo*124", "F*",
				"foobar bazbaz 12");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					PropertyUtil.wildcardMatch(bad, src), is(false));
		}
	}

	@Test
	public void testWildcardMatcherWithMultiline() throws IOException {
		String src = "foo\nbar";

		List<String> goods = Arrays.asList(src, "*", "f*", "*r", "f*r", "f?????r", "???????",
				"f*?*r");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					PropertyUtil.wildcardMatch(good, src), is(true));
		}

		List<String> bads = Arrays.asList("", "foo", "bar", "*foo", "bar*", "???", "????????");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					PropertyUtil.wildcardMatch(bad, src), is(false));
		}
	}

	@Test
	public void testWildcardMatcherWithEscapedRegexChars() throws IOException {
		String src = "foo. (bar)* ?baz{}";

		List<String> goods = Arrays.asList(src, "*", "*?", "foo*", "*bar*", "*{}",
				"foo? (bar)? *{}", "f*}");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					PropertyUtil.wildcardMatch(good, src), is(true));
		}

		List<String> bads = Arrays.asList("", "foo", "bar", "*foo", "bar*", "???");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					PropertyUtil.wildcardMatch(bad, src), is(false));
		}
	}

	@Test
	public void testWildcardMatcherWithBlank() throws IOException {
		String src = "";

		List<String> goods = Arrays.asList(src, "*");
		for (String good : goods) {
			assertThat(PropertyUtil.wildcardMatch(good, src), is(true));
		}

		List<String> bads = Arrays.asList("?", "foo");
		for (String bad : bads) {
			assertThat(PropertyUtil.wildcardMatch(bad, src), is(false));
		}
	}
}