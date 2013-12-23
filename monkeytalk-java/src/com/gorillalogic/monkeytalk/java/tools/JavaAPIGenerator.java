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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gorillalogic.monkeytalk.api.meta.API;
import com.gorillalogic.monkeytalk.api.meta.Action;
import com.gorillalogic.monkeytalk.api.meta.Arg;
import com.gorillalogic.monkeytalk.api.meta.Component;

/**
 * Generate Java API. Alas, the Java API can't directly use the interfaces as they are currently
 * written, so we just generate a new set of interfaces from the original interfaces.
 */
public class JavaAPIGenerator {

	/** exclude these component from the Java API */
	private static final List<String> EXCLUDE_COMPONENTS = Arrays.asList("Application",
			"Verifiable", "Vars", "Suite", "Test", "Setup", "Teardown", "MTObject", "Doc", "Debug",
			"System", "Globals");

	private static final String LICENSE = "/*  MonkeyTalk - a cross-platform functional testing tool\n"
			+ "    Copyright (C) 2013 Gorilla Logic, Inc.\n\n"
			+ "    This program is free software: you can redistribute it and/or modify\n"
			+ "    it under the terms of the GNU Affero General Public License as published by\n"
			+ "    the Free Software Foundation, either version 3 of the License, or\n"
			+ "    (at your option) any later version.\n\n"
			+ "    This program is distributed in the hope that it will be useful,\n"
			+ "    but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
			+ "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
			+ "    GNU Affero General Public License for more details.\n\n"
			+ "    You should have received a copy of the GNU Affero General Public License\n"
			+ "    along with this program.  If not, see <http://www.gnu.org/licenses/>. */\n";

	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			usage("you must specify the target dir");
		}

		File target = new File(args[0]);

		if (!target.exists()) {
			usage("target dir must exist");
		} else if (target.isFile()) {
			usage("target dir must be a directory");
		}

		try {
			generateAPI(target);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static void generateAPI(File dir) throws IOException {
		System.out.println("generating Java API:");
		generateApplication(dir);
		for (Component c : API.getComponents()) {
			if (EXCLUDE_COMPONENTS.contains(c.getName())) {
				continue;
			}

			generateComponent(c, dir);
		}
	}

	private static void generateApplication(File dir) throws IOException {
		System.out.println("  Application");
		List<Action> actions = new ArrayList<Action>();

		// add method for each component
		for (Component c : API.getComponents()) {
			if (EXCLUDE_COMPONENTS.contains(c.getName())) {
				continue;
			}

			String name = c.getName().substring(0, 1).toLowerCase() + c.getName().substring(1);
			Action a = new Action(name, c.getDescription(), Arrays.asList(new Arg("monkeyId",
					"the monkeyId", "String")), c.getName(), "the " + c.getName() + " component");
			actions.add(a);
		}

		// add 'raw' method
		Action a = new Action("raw", "Send a raw text MonkeyTalk command to the app under test.",
				Arrays.asList(new Arg("command", "the MonkeyTalk command", "String")), "String",
				"the return value (as from a Get action), or {@code null} if it doesn't exist");
		actions.add(a);

		String app = makeInterface("Application",
				"Helper for the MonkeyTalk application under test.", actions, false, false);
		writeFile(new File(dir, "Application.java"), app);
	}

	private static void generateComponent(Component c, File dir) throws IOException {
		System.out.println("  " + c.getName());
		String comp = makeInterface(c.getName(), c.getDescription(), c.getActions(), false, true);
		writeFile(new File(dir, c.getName() + ".java"), comp);
	}

	private static String makeInterface(String interfaceName, String description,
			List<Action> actions, boolean includeModsVarArgs, boolean includeModsMap) {
		List<String> methods = new ArrayList<String>();
		String m;
		StringBuilder sb = new StringBuilder();

		sb.append("/**\n * " + description + "\n */\n");
		sb.append("public interface " + interfaceName + " {\n");
		for (Action a : actions) {
			String name = a.getName().substring(0, 1).toLowerCase() + a.getName().substring(1);

			// first, the empty method: foo()
			m = makeMethod(name, a.getDescription(), a.getReturnType(),
					(a.getReturnType() == "void" ? "" : (a.getReturnDescription() != null
							&& a.getReturnDescription().length() > 0 ? a.getReturnDescription()
							: "the " + a.getReturnType() + " component")), null, null);
			if (!methods.contains(name + "()")) {
				methods.add(name + "()");
				sb.append(m);
			}

			// second, the mods methods...
			if (includeModsVarArgs) {
				m = makeMethod(name, a.getDescription(), a.getReturnType(),
						(a.getReturnType() == "void" ? "" : (a.getReturnDescription() != null
								&& a.getReturnDescription().length() > 0 ? a.getReturnDescription()
								: "the " + a.getReturnType() + " component")), "String... mods",
						Arrays.asList(new Arg("mods", "the MonkeyTalk modifiers", "String", true)));
				if (!methods.contains(name + "(String... mods)")) {
					methods.add(name + "(String... mods)");
					sb.append(m);
				}
			}
			if (includeModsMap) {
				m = makeMethod(name, a.getDescription(), a.getReturnType(),
						(a.getReturnType() == "void" ? "" : (a.getReturnDescription() != null
								&& a.getReturnDescription().length() > 0 ? a.getReturnDescription()
								: "the " + a.getReturnType() + " component")),
						"Map<String, String> mods", Arrays.asList(new Arg("mods",
								"the MonkeyTalk modifiers", "Map<String, String>")));
				if (!methods.contains(name + "(Map<String, String> mods)")) {
					methods.add(name + "(Map<String, String> mods)");
					sb.append(m);
				}
			}

			// third, all the methods appending any args one at a time
			StringBuilder args = new StringBuilder();
			List<Arg> argsList = new ArrayList<Arg>();

			for (int i = 0; i < a.getArgs().size(); i++) {
				// if action is Get or ExecAndReturn, then ignore the first arg (return var name)
				if (i == 0 && ("get".equals(name) || "execAndReturn".equals(name))) {
					continue;
				}

				Arg arg = a.getArgs().get(i);

				argsList.add(arg);
				args.append(args.length() > 0 ? ", " : "");

				if (arg.isVarArgs()) {
					StringBuilder args2 = new StringBuilder(args);
					args2.append(arg.getType() + "... " + arg.getName());
					m = makeMethod(name, a.getDescription(), a.getReturnType(),
							a.getReturnDescription(), args2.toString(), argsList);
					if (!methods.contains(name + "(" + args2.toString() + ")")) {
						methods.add(name + "(" + args2.toString() + ")");
						sb.append(m);
					}

					String type = ("int".equals(arg.getType()) ? "Integer" : arg.getType());
					args.append("List<" + type + "> " + arg.getName());
				} else {
					args.append(arg.toParam());
				}

				m = makeMethod(name, a.getDescription(), a.getReturnType(),
						a.getReturnDescription(), args.toString(), argsList);
				if (!methods.contains(name + "(" + args.toString() + ")")) {
					methods.add(name + "(" + args.toString() + ")");
					sb.append(m);
				}

				// and the mods methods...
				if (includeModsVarArgs) {
					List<Arg> argsListModsVarArgs = new ArrayList<Arg>(argsList);
					argsListModsVarArgs.add(new Arg("mods", "the MonkeyTalk modifiers", "String",
							true));

					m = makeMethod(name, a.getDescription(), a.getReturnType(),
							a.getReturnDescription(), args.toString() + ", String... mods",
							argsListModsVarArgs);
					if (!methods.contains(name + "(" + args.toString() + ", String... mods)")) {
						methods.add(name + "(" + args.toString() + ", String... mods)");
						sb.append(m);
					}
				}
				if (includeModsMap) {
					List<Arg> argsListModsMap = new ArrayList<Arg>(argsList);
					argsListModsMap.add(new Arg("mods", "the MonkeyTalk modifiers",
							"Map<String, String>"));

					m = makeMethod(name, a.getDescription(), a.getReturnType(),
							a.getReturnDescription(), args.toString()
									+ ", Map<String, String> mods", argsListModsMap);
					if (!methods.contains(name + "(" + args.toString()
							+ ", Map<String, String> mods)")) {
						methods.add(name + "(" + args.toString() + ", Map<String, String> mods)");
						sb.append(m);
					}
				}
			}
			sb.append("\n");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.append("}\n");

		if (sb.indexOf("Map<") != -1) {
			sb.insert(0, "import java.util.Map;\n\n");
		}
		if (sb.indexOf("List<") != -1) {
			sb.insert(0, "import java.util.List;\n");
		}
		sb.insert(0, "package com.gorillalogic.monkeytalk.java.api;\n\n");

		sb.insert(0, LICENSE);

		return sb.toString();
	}

	private static String makeMethod(String name, String description, String returnType,
			String returnTypeDescription, String args, List<Arg> argList) {
		System.out.println("    " + name + "(" + (args != null ? args : "") + ")");

		StringBuilder paramDoc = new StringBuilder();
		if (argList != null) {
			for (Arg a : argList) {
				paramDoc.append("\t * @param ").append(a.getName()).append(" ")
						.append(a.getDescription()).append("\n");
			}
		}

		String returnDoc = (returnTypeDescription != null && returnTypeDescription.length() > 0 ? "\t * @return "
				+ returnTypeDescription + "\n"
				: "");

		return "\t/**\n\t * " + description + "\n" + paramDoc + returnDoc + "\t */\n\tpublic "
				+ returnType + " " + name + "(" + (args != null ? args : "") + ");\n";
	}

	private static void writeFile(File f, String contents) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write(contents);
		out.close();
	}

	private static void usage(String err) {
		if (err != null) {
			System.err.println("ERROR: " + err);
		}
		System.out.println("Usage: java JavaAPIGenerator <target>");
		if (err != null) {
			System.exit(1);
		}
	}
}