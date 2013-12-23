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
package com.gorillalogic.monkeytalk.verify.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.gorillalogic.monkeytalk.verify.Verify;

public class VerifyTest {

	@Test
	public void testVerify() {
		assertThat(Verify.verify("foo", "foo"), is(true));
		assertThat(Verify.verifyNot("foo", "foo"), is(false));

		assertThat(Verify.verify("foo", "bar"), is(false));
		assertThat(Verify.verifyNot("foo", "bar"), is(true));
	}

	@Test
	public void testVerifyWildcard() {
		String src = "foobar bazbaz 123";

		List<String> goods = Arrays.asList(src, "*", "foo*", "*foo*", "*123", "*123*", "foo*123",
				"f????r b????z 1?3", "*baz*", "?????????????????", "f***3", "foobar bazbaz 123");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					Verify.verifyWildcard(good, src), is(true));
			assertThat("should not match: src='" + src + "' pat='" + good + "'",
					Verify.verifyNotWildcard(good, src), is(false));
		}

		List<String> bads = Arrays.asList("", "foo", "?foo", "*foo", "123*", "foo*124", "F*",
				"foobar bazbaz 12");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyWildcard(bad, src), is(false));
			assertThat("should match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyNotWildcard(bad, src), is(true));
		}
	}

	@Test
	public void testVerifyWildcardWithMultiline() {
		String src = "foo\nbar";

		List<String> goods = Arrays.asList(src, "*", "f*", "*r", "f*r", "f?????r", "???????",
				"f*?*r");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					Verify.verifyWildcard(good, src), is(true));
			assertThat("should not match: src='" + src + "' pat='" + good + "'",
					Verify.verifyNotWildcard(good, src), is(false));
		}

		List<String> bads = Arrays.asList("", "foo", "bar", "*foo", "bar*", "???", "????????");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyWildcard(bad, src), is(false));
			assertThat("should match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyNotWildcard(bad, src), is(true));
		}
	}

	@Test
	public void testVerifyWildcardWithEscapedRegexChars() {
		String src = "foo. (bar)* ?baz{}";

		List<String> goods = Arrays.asList(src, "*", "*?", "foo*", "*bar*", "*{}",
				"foo? (bar)? *{}", "f*}");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					Verify.verifyWildcard(good, src), is(true));
			assertThat("should not match: src='" + src + "' pat='" + good + "'",
					Verify.verifyNotWildcard(good, src), is(false));
		}

		List<String> bads = Arrays.asList("", "foo", "bar", "*foo", "bar*", "???");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyWildcard(bad, src), is(false));
			assertThat("should match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyNotWildcard(bad, src), is(true));
		}
	}

	@Test
	public void testVerifyWildcardWithBlank() {
		String src = "";

		List<String> goods = Arrays.asList(src, "*");
		for (String good : goods) {
			assertThat(Verify.verifyWildcard(good, src), is(true));
			assertThat(Verify.verifyNotWildcard(good, src), is(false));
		}

		List<String> bads = Arrays.asList("?", "foo");
		for (String bad : bads) {
			assertThat(Verify.verifyWildcard(bad, src), is(false));
			assertThat(Verify.verifyNotWildcard(bad, src), is(true));
		}
	}

	@Test
	public void testVerifyRegex() {
		String src = "Welcome, Fred123!";

		List<String> goods = Arrays.asList(src, ".*", "Welcome, .*", "(?i)wElCoME, .*",
				"Welcome, [\\w\\d]+!", "Welcome, Fred123!");
		for (String good : goods) {
			assertThat("should match: src='" + src + "' pat='" + good + "'",
					Verify.verifyRegex(good, src), is(true));
			assertThat("should not match: src='" + src + "' pat='" + good + "'",
					Verify.verifyNotRegex(good, src), is(false));
		}

		List<String> bads = Arrays.asList("", "welcome, [\\w\\d]+!", "Welcome, Fred123",
				"elcome, Fred123!");
		for (String bad : bads) {
			assertThat("should not match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyRegex(bad, src), is(false));
			assertThat("should match: src='" + src + "' pat='" + bad + "'",
					Verify.verifyNotRegex(bad, src), is(true));
		}
	}
}