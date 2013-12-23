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
package com.gorillalogic.monkeytalk.parser.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.List;

import org.junit.Test;

import com.gorillalogic.monkeytalk.parser.CSVParser;

public class CSVParserTest {

	@Test
	public void testParsingNull() {
		List<String> tokens = CSVParser.parse(null);

		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(0));
	}
	
	@Test
	public void testParsingEmpty() {
		List<String> tokens = CSVParser.parse("");
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(0));
	}
	
	@Test
	public void testParsingSpaces() {
		List<String> tokens = CSVParser.parse("   ");
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(0));
	}
	
	@Test
	public void testParsing() {
		String line = "foo,bar,baz";
		List<String> tokens = CSVParser.parse(line);
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("foo", "bar", "baz"));
	}
	
	@Test
	public void testParsingNumbers() {
		String line = "123,456,987";
		List<String> tokens = CSVParser.parse(line);
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("123", "456", "987"));
	}

	@Test
	public void testParsingWithExtraSpace() {
		String line = " foo  , bar  ,       baz   ";
		List<String> tokens = CSVParser.parse(line);
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("foo", "bar", "baz"));
	}
	
	@Test
	public void testParsingWithQuotes() {
		String line = "\"foo\", \"bar\", \"baz\"";
		List<String> tokens = CSVParser.parse(line);

		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("foo", "bar", "baz"));
	}
	
	@Test
	public void testParsingWithQuotesAndExtraSpace() {
		String line = " \"foo\" , \" bar  \"   , \"baz\"  ";
		List<String> tokens = CSVParser.parse(line);
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("foo", " bar  ", "baz"));
	}
	
	@Test
	public void testParsingWithQuotedCommas() {
		String line = "\"foo\", \"bar, far, tar\", \"baz\"";
		List<String> tokens = CSVParser.parse(line);
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("foo", "bar, far, tar", "baz"));
	}
	
	@Test
	public void testParsingWithQuotedCommasAndEscapedQuotes() {
		String line = "\"foo\", \"bar, \\\"far\\\", tar\", \"baz\"";
		List<String> tokens = CSVParser.parse(line);
		
		assertThat(tokens, notNullValue());
		assertThat(tokens.size(), is(3));
		assertThat(tokens, hasItems("foo", "bar, \\\"far\\\", tar", "baz"));
	}
}