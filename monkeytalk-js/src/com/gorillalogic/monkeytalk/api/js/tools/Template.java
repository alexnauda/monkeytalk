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
package com.gorillalogic.monkeytalk.api.js.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class Template {
	private String template;
	private String filename;
	private StringBuffer buffer;

	public Template(String filename) {
		this.filename = filename;
		try {
			template = fileToString(filename);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		init();
	}

	private String fileToString(String file) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
	      
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			System.err.println("Template: NULL");
			return "";
		}
	}

	public void init() {
		buffer = new StringBuffer(template);
	}

	public void replace(String token, String value) {
		if (value == null) {
			value = "";
		}

		boolean foundOne = false;
		while (true) {
			int start = buffer.indexOf("$" + token + "$");

			if (start == -1) {
				if (!foundOne) {
					throw new IllegalArgumentException("Unable to find token " + token
							+ " in file " + new File(filename).getAbsolutePath()
							+ " - contents of file: \n" + template + "\n - contents of buffer: \n" + buffer);
				}
				break;
			}
			buffer.replace(start, start + token.length() + 2, value);
			foundOne = true;
		}
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

	public static String lowerCamel(String s) {
		if (s == null) {
			return null;
		} else if (s.length() == 0) {
			return "";
		} else if (s.length() == 1) {
			return s.toLowerCase();
		} else {
			return s.substring(0, 1).toLowerCase() + s.substring(1);
		}
	}

	public static String removeReturn(String s) {
		if (s != null) {
			while (s.endsWith("\n")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}
}