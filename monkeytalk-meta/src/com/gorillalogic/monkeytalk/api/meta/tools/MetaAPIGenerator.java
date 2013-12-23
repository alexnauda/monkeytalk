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
package com.gorillalogic.monkeytalk.api.meta.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;

/**
 * Generate the Meta API for Java and print it. This is used as input to be inserted into
 * com.gorillalogic.monkeytalk.api.meta.API.
 */
public class MetaAPIGenerator {
	/**
	 * When true, ignore all meta API sources in the {@code com.gorillalogic.monkeytalk.api.flex}
	 * package when generating the API.java class. NOTE: To fully ignore or un-ignore Flex, you must
	 * also see {@code JSAPIGenerator}.
	 */
	private static final boolean IGNORE_FLEX = true;

	public static String generateComponents(File srcDir) {
		JavaDocBuilder javadoc = new JavaDocBuilder();
		javadoc.addSourceTree(srcDir);

		Map<String, String> componentMap = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();

		for (JavaSource src : javadoc.getSources()) {
			if (IGNORE_FLEX && src.getPackageName().endsWith(".flex")) {
				// for (JavaClass clazz : src.getClasses()) {
				// System.out.println("IGNORE " + clazz.getFullyQualifiedName());
				// }
				continue;
			}

			for (JavaClass clazz : src.getClasses()) {
				if (clazz.getTagByName("ignoreMeta") != null) {
					continue;
				}
				System.out.println(clazz.getFullyQualifiedName());

				sb.append("components.add(new Component(\n  ");
				sb.append('"').append(clazz.getName()).append("\",\n  ");
				sb.append('"').append(cleanString(clazz.getComment())).append("\",\n  ");

				Type[] extendz = clazz.getImplements();
				sb.append(
						extendz == null || extendz.length == 0 || extendz[0].getJavaClass() == null
								|| extendz[0].getJavaClass().getName().equalsIgnoreCase("mtobject") ? "null"
								: '"' + extendz[0].getJavaClass().getName().toString() + '"')
						.append(",\n  ");

				StringBuilder actions = new StringBuilder();

				for (JavaMethod meth : clazz.getMethods()) {
					if (meth.getTagByName("ignoreMeta") != null) {
						continue;
					}
					if (actions.length() > 0) {
						actions.append(",\n");
					}

					actions.append("    new Action(\n      ");
					actions.append('"').append(upperCamel(meth.getName())).append("\",\n      ");
					actions.append('"').append(cleanString(meth.getComment()))
							.append("\",\n      ");

					StringBuilder args = new StringBuilder();
					for (DocletTag param : meth.getTagsByName("param")) {
						String value = param.getValue();

						int idx = value.indexOf("\n");
						if (idx == -1) {
							throw new RuntimeException("ERROR: " + clazz.getName() + "#"
									+ meth.getName() + "() - bad @param javadoc");
						}

						String pname = value.substring(0, idx);
						String pdesc = value.substring(idx + 1);

						JavaParameter p = meth.getParameterByName(pname);
						if (p == null) {
							throw new RuntimeException("ERROR: " + clazz.getName() + "#"
									+ meth.getName() + "() - @param " + pname
									+ " has no matching method argument");
						}
						String ptype = p.getType().getJavaClass().getName();

						if (args.length() > 0) {
							args.append(",\n");
						}

						args.append("        new Arg(");
						args.append('"').append(pname).append('"');
						args.append(",\"").append(cleanString(pdesc)).append('"');
						args.append(",\"").append(ptype).append('"');
						args.append(p.isVarArgs() ? ",true" : "");
						args.append(")");
					}

					if (args.length() > 0) {
						actions.append("new ArrayList<Arg>(Arrays.asList(\n")
								.append(args.toString()).append("\n      )),\n      ");
					} else {
						actions.append("null,\n      ");
					}

					actions.append('"')
							.append(meth.getReturnType() != null ? meth.getReturnType()
									.getJavaClass().getName() : "").append("\",\n      ");
					actions.append('"')
							.append(meth.getTagByName("return") != null ? cleanString(meth
									.getTagByName("return").getValue()) : "").append("\")");
				}

				if (actions.length() > 0) {
					sb.append("new ArrayList<Action>(Arrays.asList(\n").append(actions.toString())
							.append("\n    ))\n  ,\n  ");
				} else {
					sb.append("null,\n  ");
				}

				StringBuilder properties = new StringBuilder();

				for (DocletTag tag : clazz.getTagsByName("prop")) {
					String val = tag.getParameters()[0];
					String tagVal = tag.getValue();
					int sep = tagVal.indexOf("-");
					String desc = (sep != -1 ? tagVal.substring(sep + 1).trim() : null);
					String args = (sep != -1 ? tagVal.substring(0, sep).trim() : tagVal);
					args = (args.equals(val) ? null : args.substring(val.length()));

					if (properties.length() > 0) {
						properties.append(",\n");
					}

					properties.append("    new Property(");
					properties.append("\"").append(val).append("\", ");
					if (args == null) {
						properties.append("null, ");
					} else if (args.startsWith("(") && args.endsWith(")")) {
						properties.append("\"")
								.append(cleanString(args.substring(1, args.length() - 1)))
								.append("\", ");
					} else {
						properties.append("\"").append(cleanString(args)).append("\", ");
					}
					if (desc == null) {
						properties.append("null)");
					} else {
						properties.append("\"").append(cleanString(desc)).append("\")");
					}
				}

				if (properties.length() > 0) {
					sb.append("new ArrayList<Property>(Arrays.asList(\n")
							.append(properties.toString()).append("\n    ))\n  )");
				} else {
					sb.append("null)");
				}

				sb.append("\n);\n");

				componentMap.put(clazz.getName(), sb.toString());
				sb = new StringBuilder();
			}
		}

		// sort by component name (we do this now, during codegen, to avoid sorting later)
		sb = new StringBuilder();
		List<String> keys = new ArrayList<String>(componentMap.keySet());
		Collections.sort(keys);
		for (String k : keys) {
			sb.append(componentMap.get(k));
		}

		return "IGNORE_FLEX = " + IGNORE_FLEX + ";\n\n" + sb.toString();
	}

	private static String cleanString(String s) {
		if (s == null) {
			return "";
		}
		return s.trim().replaceAll("\"", "'").replaceAll("\\n", " ").replaceAll("\\s+", " ");
	}

	private static String upperCamel(String s) {
		if (s.length() < 2) {
			return s.toUpperCase();
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.err
					.println("Usage: java MetaAPIGenerator <src dir> <target> <start tag> <end tag>");
			System.exit(1);
		}

		File srcDir = new File(args[0]);
		if (!srcDir.exists()) {
			System.err.println("ERROR: srcDir '" + args[0] + "' not found");
			System.err.println("ERROR: workingDir='" + new File(".").getAbsolutePath() + "'");
			System.exit(1);
		}
		if (!srcDir.isDirectory()) {
			System.err.println("ERROR: srcDir '" + srcDir.getAbsolutePath() + "' not directory");
			System.exit(1);
		}

		File target = new File(args[1]);
		if (!target.exists()) {
			System.err.println("ERROR: target '" + args[1] + "' not found");
			System.exit(1);
		}
		if (!target.isFile()) {
			System.err.println("ERROR: target '" + args[1] + "' not file");
			System.exit(1);
		}

		if (args[2].length() == 0) {
			System.err.println("ERROR: start tag is blank");
			System.exit(1);
		}
		if (args[3].length() == 0) {
			System.err.println("ERROR: end tag is blank");
			System.exit(1);
		}

		String components = generateComponents(srcDir);

		try {
			String orig = readFile(target);

			int startTag = orig.indexOf(args[2]);
			int endTag = orig.lastIndexOf(args[3]);

			String out = orig.substring(0, startTag + args[2].length()) + "\n" + components
					+ orig.substring(endTag);

			writeFile(target, out);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static String readFile(File f) throws IOException {
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

	private static void writeFile(File f, String contents) throws IOException {
		Writer out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
		try {
			out.write(contents);
		} finally {
			out.close();
		}
	}
}