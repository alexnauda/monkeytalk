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
package com.gorillalogic.monkeytalk.api.doc.tests;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.doc.APIDocGenerator;

public class APIDocGeneratorTest extends BaseDocTest {
	private ByteArrayOutputStream out;

	@Before
	public void before() throws IOException {
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
	}

	@After
	public void after() {
		out = null;
	}

	@Test
	public void testDoc() throws IOException {
		File dir = tempDir();
		File f = tempScript("doc.html", "", dir);
		APIDocGenerator.main(new String[] { f.getAbsolutePath() });

		String doc = readFile(f);
		assertThat(doc, containsString("<a name=\"Button\"></a>"));
		assertThat(doc, containsString("<a name=\"CheckBox\"></a>"));
		assertThat(doc, containsString("<a name=\"Device\"></a>"));
		assertThat(doc, containsString("<a name=\"View\"></a>"));
		assertThat(doc, containsString("<a name=\"Device.Shake\"></a>"));
		assertThat(
				doc,
				containsString("Verify(expectedValue:String, propPath:String, failMessage:String):void"));
		assertThat(doc, containsString("Tap():void"));
		assertThat(doc, containsString("TouchMove(coords:int[]):void"));
		assertThat(doc, containsString("<b>value</b> - the button label text"));

		assertThat(out.toString(), containsString("Button : View"));
		assertThat(out.toString(), containsString("Table : ItemSelector"));
	}
}