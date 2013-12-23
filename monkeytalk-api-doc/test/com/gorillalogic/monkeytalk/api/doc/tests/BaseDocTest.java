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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

import org.junit.AfterClass;

public class BaseDocTest {
	protected static final String DIR_PREFIX = "tmpMT";

	@AfterClass
	public static void cleanup() throws IOException {
		File dummy = File.createTempFile("dummy", null);
		dummy.deleteOnExit();

		for (File f : dummy.getParentFile().listFiles()) {
			if (f.isDirectory() && f.getName().startsWith(DIR_PREFIX)) {
				deleteDir(f);
			}
		}
	}

	protected File tempDir() throws IOException {
		final String now = Long.toString(System.nanoTime());
		final File dir = File.createTempFile(DIR_PREFIX, now);

		if (!dir.delete()) {
			fail("failed to delete file: " + dir.getAbsolutePath());
		}

		if (!dir.mkdir()) {
			fail("failed to create dir: " + dir.getAbsolutePath());
		}

		return dir;
	}

	protected File tempScript(String filename, String contents, File dir) throws IOException {
		File tmp = new File(dir, filename);
		Writer out = new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8");
		try {
			out.write(contents);
		} finally {
			out.close();
		}
		return tmp;
	}

	private static void deleteDir(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	protected String readFile(File f) throws IOException {
		StringBuilder sb = new StringBuilder();
		Scanner scanner = new Scanner(new FileInputStream(f), "UTF-8");
		try {
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append('\n');
			}
		} finally {
			scanner.close();
		}
		return (sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "");
	}
}