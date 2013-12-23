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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.parser.CSVParser;

public class CSVFileParserTest {

	private static final String contents = "first,last,zip\n" + "\n\n\n\n"
			+ "Al,Smith,12345\n" + "\"Too Tall\",Baker,22335\n"
			+ "  Charlie  ,  \" The Magnificent  \"     ,   33311  \n"
			+ "Dale,\"The, Magic, Man\", 44445\n"
			+ "Everett,Hopkins,55110,extra,data,blah,blah,blah\n"
			+ "Farley,Williams\n";

	private static List<Map<String, String>> csv;

	@BeforeClass
	public static void beforeClass() {
		try {
			File tmp = File.createTempFile("data", ".csv");
			tmp.deleteOnExit();

			// write data
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write(contents);
			out.close();

			csv = CSVParser.parseFile(tmp);

			assertThat(csv, notNullValue());
			assertThat(csv.size(), is(6));
		} catch (IOException ex) {
			csv = null;
		}
	}
	
	@Test
	public void testNullFile() {
		List<Map<String,String>> data = CSVParser.parseFile(null);
		assertThat(data, nullValue());
	}
	
	@Test
	public void testMissingFile() {
		List<Map<String,String>> data = CSVParser.parseFile(new File("missing"));
		assertThat(data, nullValue());
	}

	@Test
	public void testEmpty() {
		try {
			File tmp = File.createTempFile("empty", ".csv");
			tmp.deleteOnExit();
			
			// write data
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write("");
			out.close();
			
			List<Map<String,String>> data = CSVParser.parseFile(tmp);
			
			//no header row, so we return null
			assertThat(data, nullValue());
		} catch (IOException ex) {
			csv = null;
		}
	}
	
	@Test
	public void testEmptyHeaderRow() {
		try {
			File tmp = File.createTempFile("empty-header", ".csv");
			tmp.deleteOnExit();
			
			// write data
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write("\n");
			out.close();
			
			List<Map<String,String>> data = CSVParser.parseFile(tmp);
			
			//header row contains no data, so we return null
			assertThat(data, nullValue());
		} catch (IOException ex) {
			csv = null;
		}
	}
	
	@Test
	public void testHeaderRowOnly() {
		try {
			File tmp = File.createTempFile("header-only", ".csv");
			tmp.deleteOnExit();
			
			// write data
			BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			out.write("first,last,zip");
			out.close();
			
			List<Map<String,String>> data = CSVParser.parseFile(tmp);
			
			assertThat(data, notNullValue());
			assertThat(data.size(), is(0));
		} catch (IOException ex) {
			csv = null;
		}
	}
	
	@Test
	public void testLine() {
		Map<String, String> vars = csv.get(0);

		assertThat(vars, notNullValue());
		assertThat(vars.size(), is(3));
		assertThat(vars.keySet(), hasItems("first", "last", "zip"));
		assertThat(vars.values(), hasItems("Al", "Smith", "12345"));
	}

	@Test
	public void testLineWithQuotes() {
		Map<String, String> vars = csv.get(1);

		assertThat(vars, notNullValue());
		assertThat(vars.size(), is(3));
		assertThat(vars.keySet(), hasItems("first", "last", "zip"));
		assertThat(vars.values(), hasItems("Too Tall", "Baker", "22335"));
	}

	@Test
	public void testLineWithExtraSpaces() {
		Map<String, String> vars = csv.get(2);

		assertThat(vars, notNullValue());
		assertThat(vars.size(), is(3));
		assertThat(vars.keySet(), hasItems("first", "last", "zip"));
		assertThat(vars.values(),
				hasItems("Charlie", " The Magnificent  ", "33311"));
	}

	@Test
	public void testLineWithExtraCommas() {
		Map<String, String> vars = csv.get(3);

		assertThat(vars, notNullValue());
		assertThat(vars.size(), is(3));
		assertThat(vars.keySet(), hasItems("first", "last", "zip"));
		assertThat(vars.values(), hasItems("Dale", "The, Magic, Man", "44445"));
	}
	
	@Test
	public void testLineWithExtraData() {
		Map<String, String> vars = csv.get(4);
		
		assertThat(vars, notNullValue());
		assertThat(vars.size(), is(3));
		assertThat(vars.keySet(), hasItems("first", "last", "zip"));
		assertThat(vars.values(), hasItems("Everett", "Hopkins", "55110"));
	}
	
	@Test
	public void testLineWithMissingData() {
		Map<String, String> vars = csv.get(5);
		
		assertThat(vars, notNullValue());
		assertThat(vars.size(), is(3));
		assertThat(vars.keySet(), hasItems("first", "last", "zip"));
		assertThat(vars.values(), hasItems("Farley", "Williams"));
		assertThat(vars.get("zip"), nullValue());
	}
}