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
package com.gorillalogic.monkeytalk.processor.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class GlobalsTest {

	@Before
	public void before() {
		Globals.clear();
	}

	@Test
	public void testClear() {
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.asString(), is("{ }"));

		Globals.setGlobal("foo", "123");
		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), nullValue());
		assertThat(Globals.asString(), is("{ foo:'123' }"));

		Globals.clear();
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.asString(), is("{ }"));
		assertThat(Globals.asJavascript(), is(""));
	}

	@Test
	public void testSetAndEdit() {
		Globals.setGlobal("foo", "123");
		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), nullValue());

		Globals.setGlobal("foo", "234");
		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("234"));
		assertThat(Globals.getGlobal("bar"), nullValue());
	}

	@Test
	public void testSetAndDelete() {
		Globals.setGlobal("foo", "123");
		assertThat(Globals.getGlobals().size(), is(1));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), nullValue());

		String val = Globals.deleteGlobal("foo");
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.getGlobal("foo"), nullValue());
		assertThat(val, is("123"));
	}

	@Test
	public void testSetWithIllegalVariable() {
		try {
			Globals.setGlobal("1foo", "123");
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), is("illegal global variable '1foo' -- "
					+ Globals.ILLEGAL_MSG));
			assertThat(Globals.getGlobals().size(), is(0));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testSetWithReservedWord() {
		try {
			Globals.setGlobal("alert", "123");
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("illegal global variable 'alert' -- "
					+ Globals.RESERVED_MSG));
			assertThat(Globals.getGlobals().size(), is(0));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testSetFromMap() {
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("foo", "123");
		map.put("bar", "Bo Bo");
		map.put("baz", "i'sn't");

		Globals.setGlobals(map);
		assertThat(Globals.getGlobals().size(), is(3));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("Bo Bo"));
		assertThat(Globals.getGlobal("baz"), is("i'sn't"));
		assertThat(Globals.asString(), is("{ foo:'123', bar:'Bo Bo', baz:'i'sn't' }"));
		assertThat(Globals.asJavascript(), is("var foo = '123';\n" + "var bar = 'Bo Bo';\n"
				+ "var baz = 'i\\'sn\\'t';\n"));
	}

	@Test
	public void testSetFromMapWithIllegalVariable() {
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));

		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("1foo", "123");
		map.put("bar", "Bo Bo");

		try {
			Globals.setGlobals(map);
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), containsString("illegal global variable '1foo' -- "
					+ Globals.ILLEGAL_MSG));
			assertThat(Globals.getGlobals().size(), is(0));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testSetFromFile() throws IOException {
		File dir = FileUtils.tempDir();
		File globals = new File(dir, "globals.properties");
		FileUtils.writeFile(globals, "foo=123\nbar=456");

		Globals.setGlobals(globals);
		assertThat(Globals.getGlobals().size(), is(2));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("456"));
		assertThat(Globals.getGlobal("baz"), nullValue());
		assertThat(Globals.asString(), is("{ bar:'456', foo:'123' }"));
	}

	@Test
	public void testSetFromNullFile() throws IOException {
		Globals.setGlobals((File) null);
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.asString(), is("{ }"));
	}

	@Test
	public void testSetFromMissingFile() throws IOException {
		Globals.setGlobals(new File("missing"));
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.asString(), is("{ }"));
	}

	@Test
	public void testSetFromDirectoryFile() throws IOException {
		File dir = FileUtils.tempDir();
		Globals.setGlobals(dir);
		assertThat(Globals.getGlobals(), notNullValue());
		assertThat(Globals.getGlobals().size(), is(0));
		assertThat(Globals.asString(), is("{ }"));
	}

	@Test
	public void testSetFromFileWithIllegalVariable() throws IOException {
		try {
			File dir = FileUtils.tempDir();
			File globals = new File(dir, "globals.properties");
			FileUtils.writeFile(globals, "1foo=123");
			Globals.setGlobals(globals);
		} catch (RuntimeException ex) {
			assertThat(
					ex.getMessage(),
					containsString("globals file 'globals.properties' has illegal global variable '1foo'"));
			assertThat(Globals.getGlobals().size(), is(0));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testParseFromStringWithDoubleQuotes() {
		Map<String, String> m = Globals.parse("foo=123 fred=\"bo bo\" joe=\"\" bob \"easy e\"");
		assertThat(m.size(), is(3));
		assertThat(m.get("foo"), is("123"));
		assertThat(m.get("fred"), is("bo bo"));
		assertThat(m.get("joe"), is(""));
		assertThat(m.get("bob"), nullValue());
	}

	@Test
	public void testParseFromStringWithDoubleQuotesAndEmbeddedDoubleQuote() {
		Map<String, String> m = Globals.parse("foo=\"123 456\" fred=joe\"bob\"smith");
		assertThat(m.size(), is(2));
		assertThat(m.get("foo"), is("123 456"));
		assertThat(m.get("fred"), is("joe\"bob\"smith"));
	}

	@Test
	public void testParseFromStringWithSingleQuotes() {
		Map<String, String> m = Globals.parse("foo=123 fred='bo bo' joe='' bob 'easy e'");
		assertThat(m.size(), is(3));
		assertThat(m.get("foo"), is("123"));
		assertThat(m.get("fred"), is("bo bo"));
		assertThat(m.get("joe"), is(""));
		assertThat(m.get("bob"), nullValue());
	}

	@Test
	public void testParseFromStringWithSingleQuotesAndEmbeddedSingleQuote() {
		Map<String, String> m = Globals.parse("foo='123 456' fred=joe'bob'smith");
		assertThat(m.size(), is(2));
		assertThat(m.get("foo"), is("123 456"));
		assertThat(m.get("fred"), is("joe'bob'smith"));
	}

	@Test
	public void testParseFromStringWithCrazyQuotes() {
		Map<String, String> m = Globals
				.parse("foo='1 23' bar=\"45 6\" isnot=isn't bad three=is'''not\"\"\"");
		assertThat(m.size(), is(4));
		assertThat(m.get("foo"), is("1 23"));
		assertThat(m.get("bar"), is("45 6"));
		assertThat(m.get("isnot"), is("isn't"));
		assertThat(m.get("three"), is("is'''not\"\"\""));
	}

	@Test
	public void testParseFromStringWithIllegalVariable() {
		try {
			Map<String, String> m = Globals.parse("1foo=123");
			assertThat(m.size(), is(0));
		} catch (RuntimeException ex) {
			assertThat(ex.getMessage(), is("illegal global variable '1foo' -- "
					+ Globals.ILLEGAL_MSG));
			assertThat(Globals.getGlobals().size(), is(0));
			return;
		}
		fail("should have thrown exception");
	}
}