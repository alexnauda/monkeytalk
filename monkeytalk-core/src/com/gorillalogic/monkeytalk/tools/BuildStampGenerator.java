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
package com.gorillalogic.monkeytalk.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generate the build stamp and insert it into com.gorillalogic.monkeytalk.BuildStamp.
 */
public class BuildStampGenerator {
	public static final String COPYRIGHT = "Copyright 2012-2013 Gorilla Logic, Inc.";
	public static final String URL = "www.gorillalogic.com";

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public static void main(String[] args) {
		if (args.length != 5) {
			System.err
					.println("Usage: java BuildStampGenerator <file> <version> <build #> <start tag> <end tag>");
			System.exit(1);
		}

		File f = new File(args[0]);
		if (!f.exists()) {
			System.err.println("ERROR: file '" + args[0] + "' not found");
			System.exit(1);
		}
		if (!f.isFile()) {
			System.err.println("ERROR: file '" + args[0] + "' not file");
			System.exit(1);
		}

		String ver = args[1];
		if (ver.length() == 0) {
			System.err.println("ERROR: version is blank");
			System.exit(1);
		}

		String num = args[2];
		if (num.length() == 0) {
			System.err.println("ERROR: build number is blank");
			System.exit(1);
		}

		// if this is a local build, set build num to 'dev'
		if ("${env.build_number}".equalsIgnoreCase(num)) {
			num = "dev";
		}

		String startTag = args[3];
		if (startTag.length() == 0) {
			System.err.println("ERROR: start tag is blank");
			System.exit(1);
		}

		String endTag = args[4];
		if (endTag.length() == 0) {
			System.err.println("ERROR: end tag is blank");
			System.exit(1);
		}

		String time = sdf.format(new Date());

		String info = ver + "_" + num + " - " + time;

		String build = "MonkeyTalk v" + info + " - " + COPYRIGHT + " - " + URL;

		String codegen = "\tpublic static final String VERSION = \"" + ver + "\";\n"
				+ "\tpublic static final String BUILD_NUMBER = \"" + num + "\";\n"
				+ "\tpublic static final String TIMESTAMP = \"" + time + "\";\n"
				+ "\tpublic static final String VERSION_INFO = \"" + info + "\";\n"
				+ "\tpublic static final String STAMP = \"" + build + "\";\n";

		try {
			String orig = readFile(f);

			int startIdx = orig.indexOf(startTag);
			int endIdx = orig.lastIndexOf(endTag);

			String out = orig.substring(0, startIdx + startTag.length()) + "\n" + codegen + "\t"
					+ orig.substring(endIdx);

			writeFile(f, out);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static String readFile(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null) {
			sb.append(line).append("\n");
		}
		in.close();

		return sb.toString();
	}

	private static void writeFile(File f, String contents) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write(contents);
		out.close();
	}
}
