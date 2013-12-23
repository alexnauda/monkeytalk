/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.java.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.api.meta.API;
import com.gorillalogic.monkeytalk.parser.MonkeyTalkParser;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class JavaMTGenerator {
	private static final String TEST_TEMPLATE = "/templates/TestTemplate.txt";
	private static final List<String> EXCLUDED_COMPONENTS = Arrays.asList("vars", "doc", "debug",
			"suite", "test", "setup", "teardown", "script", "system");

	private static boolean mods = false;
	private static boolean verbose = true;
	private static int timeout = Command.DEFAULT_TIMEOUT;
	private static int thinktime = Command.DEFAULT_THINKTIME;

	public static void main(String[] args) {
		System.out.println(BuildStamp.STAMP);

		if (args.length != 2) {
			System.err.println("Usage: java JavaMTGenerator <input mt> <output java>");
			return;
		}

		File mt = new File(args[0]);
		if (!mt.exists()) {
			System.err.println("ERROR: script '" + mt.getAbsolutePath() + "' not found");
			return;
		}
		if (!mt.getName().endsWith(".mt")) {
			System.err.println("ERROR: script file must have .mt extension");
			return;
		}

		File java = new File(args[1]);
		if (java.getParentFile() == null || !java.getParentFile().exists()) {
			System.err.println("ERROR: output path must exist");
			return;
		}
		if (!java.getName().endsWith(".java")) {
			System.err.println("ERROR: output file must have .java extension");
			return;
		}
		String testClass = java.getName().substring(0, java.getName().length() - 5);

		if (verbose) {
			System.out.println("generate " + java.getName() + " from " + mt.getName());
		}

		try {
			String output = genJavaTest(mt, testClass);
			FileUtils.writeFile(java, output);
		} catch (IOException ex) {
			System.err.println("ERROR: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static String genJavaTest(File mt, String testClass) throws IOException {
		// load template
		String tmpl = FileUtils
				.readStream(JavaMTGenerator.class.getResourceAsStream(TEST_TEMPLATE));

		// init generator
		mods = false;

		// replace vars in template
		tmpl = tmpl.replace("${testClass}", testClass);
		tmpl = tmpl.replace("${package}", "com.example.test");
		tmpl = tmpl.replace("${driver}",
				"\t\tmt = new MonkeyTalkDriver(new File(\".\"), \"iOS\");\n");
		tmpl = tmpl
				.replace("${timeout}", (timeout != Command.DEFAULT_TIMEOUT ? "\t\tmt.setTimeout("
						+ timeout + ");\n" : ""));
		tmpl = tmpl.replace("${thinktime}",
				(thinktime != Command.DEFAULT_THINKTIME ? "\t\tmt.setThinktime(" + thinktime
						+ ");\n" : ""));
		String test = "test" + capitalize(mt.getName().substring(0, mt.getName().length() - 3));
		tmpl = tmpl.replace("${test}", test);

		List<String> cmds = genCommands(mt);
		tmpl = tmpl.replace("${commands}", join(cmds, "\n\t\t"));

		tmpl = tmpl.replace("${extra}", "");

		tmpl = tmpl.replace("${importMods}",
				mods ? "import com.gorillalogic.monkeytalk.java.utils.Mods;\n" : "");

		return tmpl;
	}

	private static List<String> genCommands(File mt) {
		List<Command> cmds = MonkeyTalkParser.parseFile(mt);
		List<String> out = new ArrayList<String>();

		for (Command cmd : cmds) {
			String java = genCommand(cmd);
			if (verbose) {
				System.out.println("  " + java);
			}
			out.add(java);
		}

		return out;
	}

	private static String genCommand(Command cmd) {
		if (cmd.isComment()) {
			return cmd.toString().replace("#", "//");
		}

		if (EXCLUDED_COMPONENTS.contains(cmd.getComponentType().toLowerCase())) {
			return "// " + cmd.toString();
		} else if (API.getComponentTypes().contains(cmd.getComponentType())) {
			return "app." + decapitalize(cmd.getComponentType()) + getMonkeyId(cmd)
					+ decapitalize(cmd.getAction()) + getArgsAndMods(cmd) + ";";
		} else {
			return "app.raw(\"" + cmd + "\");";
		}
	}

	private static String getMonkeyId(Command cmd) {
		if (cmd.getMonkeyId().equals("*")) {
			return "().";
		}
		return "(\"" + cmd.getMonkeyId().replaceAll("\"", "\\\"") + "\").";
	}

	private static String getArgsAndMods(Command cmd) {
		if (cmd.getArgs().size() == 0 && cmd.getModifiers().size() == 0) {
			return "()";
		} else if (cmd.getArgs().size() == 0) {
			// no args, only mods
			mods = true;
			return "(" + getMods(cmd) + ")";
		} else if (cmd.getModifiers().size() == 0) {
			// no mods, only args
			return "(" + getArgs(cmd) + ")";
		} else {
			// both args and mods
			mods = true;
			return "(" + getArgs(cmd) + ", " + getMods(cmd) + ")";
		}
	}

	private static String getArgs(Command cmd) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < cmd.getArgs().size(); i++) {
			String arg = cmd.getArgs().get(i);
			ArgType type = getArgType(cmd, i);
			if (type == ArgType.STRING) {
				// string args need to be quoted
				arg = "\"" + arg + "\"";
			} else {
				arg = arg.replaceAll("[^0-9\\.-]+", "");
			}
			sb.append(", ").append(arg);
		}

		return sb.toString().substring(2);
	}

	private static String getMods(Command cmd) {
		StringBuilder sb = new StringBuilder("new Mods.Builder().");

		List<String> keys = new ArrayList<String>(cmd.getModifiers().keySet());
		Collections.sort(keys);
		for (String key : keys) {
			sb.append(key).append("(").append(cmd.getModifiers().get(key)).append(").");
		}

		return sb.append("build()").toString();
	}

	private static String capitalize(String s) {
		return (s == null || s.length() == 0 ? s : s.substring(0, 1).toUpperCase() + s.substring(1));
	}

	private static String decapitalize(String s) {
		return (s == null || s.length() == 0 ? s : s.substring(0, 1).toLowerCase() + s.substring(1));
	}

	private static String join(List<String> list, String joiner) {
		StringBuilder sb = new StringBuilder();
		for (String item : list) {
			sb.append(joiner).append(item);
		}
		return sb.toString().substring(joiner.length());
	}

	private static enum ArgType {
		INT, FLOAT, STRING
	};

	private static ArgType getArgType(Command cmd, int idx) {
		try {
			String klassName = capitalize(cmd.getComponentType());
			Class<?> klass = Class.forName("com.gorillalogic.monkeytalk.java.api." + klassName);
			String methodName = decapitalize(cmd.getAction());
			for (Method m : klass.getDeclaredMethods()) {
				// ignore methods that don't match the action name
				if (!m.getName().equals(methodName)) {
					continue;
				}

				Class<?>[] params = m.getParameterTypes();

				// ignore methods with no params
				if (params.length == 0) {
					continue;
				}

				// ignore methods where last param is a list
				if (params[params.length - 1].equals(List.class)) {
					continue;
				}

				// ignore methods where last param is a map
				if (params[params.length - 1].equals(Map.class)) {
					continue;
				}

				Class<?> param = null;
				if (m.isVarArgs()) {
					// if necessary, count backward to find the vararg param
					while (idx >= params.length) {
						idx--;
					}
					param = params[idx];
				} else if (idx < params.length) {
					param = params[idx];
				}

				if (param != null) {
					if (param.getSimpleName().startsWith("int")) {
						return ArgType.INT;
					} else if (param.getSimpleName().startsWith("float")) {
						return ArgType.FLOAT;
					}
				}
			}
		} catch (Exception ex) {
			// if anything goes wrong, assume String
			return ArgType.STRING;
		}

		return ArgType.STRING;
	}
}
