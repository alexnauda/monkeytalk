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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.parser.MonkeyTalkParser;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 */
public class JSMTGenerator {
	private static final Pattern SUBSTITUTION_VAR = Pattern.compile("([\\$|%]\\{\\w+)\\}");

	public static final List<String> JAVASCRIPT_RESERVED = Arrays.asList(new String[] { "alert",
			"frames", "outerHeight", "all", "frameRate", "outerWidth", "anchor", "function",
			"packages", "anchors", "getClass", "pageXOffset", "area", "hasOwnProperty",
			"pageYOffset", "Array", "hidden", "parent", "assign", "history", "parseFloat", "blur",
			"image", "parseInt", "button", "images", "password", "checkbox", "Infinity", "pkcs11",
			"clearInterval", "isFinite", "plugin", "clearTimeout", "isNaN", "prompt",
			"clientInformation", "isPrototypeOf", "propertyIsEnum", "close", "java", "prototype",
			"closed", "JavaArray", "radio", "confirm", "JavaClass", "reset", "constructor",
			"JavaObject", "screenX", "crypto", "JavaPackage", "screenY", "Date", "innerHeight",
			"scroll", "decodeURI", "innerWidth", "secure", "decodeURIComponent", "layer", "select",
			"defaultStatus", "layers", "self", "document", "length", "setInterval", "element",
			"link", "setTimeout", "elements", "location", "status", "embed", "Math", "String",
			"embeds", "mimeTypes", "submit", "encodeURI", "name", "taint", "encodeURIComponent",
			"NaN", "text", "escape", "navigate", "textarea", "eval", "navigator", "top", "event",
			"Number", "toString", "fileUpload", "Object", "undefined", "focus",
			"offscreenBuffering", "unescape", "form", "open", "untaint", "forms", "opener",
			"valueOf", "frame", "option", "window", "abstract", "else", "instanceof", "super",
			"boolean", "enum", "int", "switch", "break", "export", "interface", "synchronized",
			"byte", "extends", "let", "this", "case", "false", "long", "throw", "catch", "final",
			"native", "throws", "char", "finally", "new", "transient", "class", "float", "null",
			"true", "const", "for", "package", "try", "continue", "function", "private", "typeof",
			"debugger", "goto", "protected", "var", "default", "if", "public", "void", "delete",
			"implements", "return", "volatile", "do", "import", "short", "while", "double", "in",
			"static", "with" });

	public static String createScript(String projectName, File f) {
		List<Command> commands = MonkeyTalkParser.parseFile(f);
		return createScript(projectName, f.getName(), commands);
	}

	public static String createScript(String projectName, String filename, List<Command> commands) {
		Template script = new Template("templates/scriptgen/script.template.js");
		script.init();
		StringBuffer cmd_buf = new StringBuffer();
		StringBuffer param_buf = new StringBuffer();
		StringBuffer vars_buf = new StringBuffer();
		StringBuffer default_buf = new StringBuffer();
		String scriptName = FileUtils.removeExt(filename, CommandWorld.SCRIPT_EXT);

		for (Command cmd : commands) {
			if (cmd.isComment()) {
				cmd_buf.append("\t//" + cmd.toString().substring(1) + "\n");
				continue;
			}

			// Var: Generate default arg assignments for script
			if ("vars.define".equalsIgnoreCase(cmd.getCommandName())) {
				for (Entry<String, String> var : getVars(cmd).entrySet()) {
					param_buf.append(param_buf.length() > 0 ? ", " : "");
					vars_buf.append(vars_buf.length() > 0 ? " + " : "");

					String vname = var.getKey();
					param_buf.append(vname);

					// x = x != undefined && x != "*" ? x : "default value";
					default_buf.append("\t" + vname + " = (" + vname + " != undefined && " + vname
							+ " != \"*\" ? " + vname + " : ");
					if (var.getValue() == null) {
						default_buf.append("\"<" + vname + ">\"");
					} else if (var.getValue().startsWith("\"") && var.getValue().endsWith("\"")) {
						default_buf.append(var.getValue());
					} else {
						default_buf.append("\"" + var.getValue() + "\"");
					}
					default_buf.append(");\n");

					// we only need this for debug.vars
					vars_buf.append("\"" + vname + "=\" + " + vname + " + \"\\n\"");
				}
				continue;
			} else if (cmd.getCommandName().toLowerCase().startsWith("vars.verify")) {
				StringBuilder args = new StringBuilder();
				if (cmd.getArgs().size() > 0) {
					args.append("\"").append(cmd.getArgs().get(0)).append("\"");
				}
				if (cmd.getArgs().size() > 1) {
					args.append(", ").append(cmd.getArgs().get(1));
				}
				default_buf.append("\t// NOT SUPPORTED YET: app.vars().verify(").append(args)
						.append(");\n");
				continue;
			} else if ("debug.vars".equalsIgnoreCase(cmd.getCommandName())) {
				default_buf.append("\tapp.debug().print(").append(vars_buf).append(");\n");
				continue;
			} else if ("globals.define".equalsIgnoreCase(cmd.getCommandName())
					|| "globals.set".equalsIgnoreCase(cmd.getCommandName())) {
				for (Entry<String, String> var : getVars(cmd).entrySet()) {
					String key = var.getKey();
					String val = var.getValue();
					if (val == null) {
						val = "";
					}
					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					} else if (val.startsWith("'") && val.endsWith("'")) {
						val = val.substring(1, val.length() - 1);
					}
					if (val.contains("'")) {
						// escape single quotes
						val = val.replace("'", "\\\'");
					}
					cmd_buf.append("\tapp.globals().set('").append(key).append("=\"").append(val)
							.append("\"');\n");
					cmd_buf.append("\t").append(key).append(" = '").append(val).append("';\n");
				}
				continue;
			}

			cmd_buf.append("\t");
			List<String> args = cmd.getArgs();

			// Get: Generate Assignment statement
			if (cmd.getAction().equalsIgnoreCase("get")
					|| cmd.getAction().equalsIgnoreCase("execAndReturn")) { // get and ret
				if (args.size() > 0) {
					cmd_buf.append("var " + args.get(0) + " = ");
				}
			}

			// Generate argList for this command
			StringBuffer arg_buf = new StringBuffer();
			for (String arg : args) {
				String argExpr = getArgExpr(arg);
				arg_buf.append(arg_buf.length() > 0 ? ", " : "").append(argExpr);
			}

			if (cmd.getModifiers().size() > 0) {
				StringBuilder mods_buf = new StringBuilder();
				for (Entry<String, String> mod : cmd.getModifiers().entrySet()) {
					String val = mod.getValue();
					mods_buf.append(mods_buf.length() > 0 ? ", " : "");
					mods_buf.append(mod.getKey()).append(":\"").append(val).append("\"");
				}
				arg_buf.append(arg_buf.length() > 0 ? ", " : "").append("{").append(mods_buf)
						.append("}");
			}

			// Suppress "*" monkeyId
			String monkeyId = cmd.getMonkeyId().equals("*") ? "" : getArgExpr(cmd.getMonkeyId());

			String compName = cmd.getMonkeyId().split(".mt")[0].trim();

			String component;
			if ("script".equalsIgnoreCase(cmd.getComponentType())
					&& cmd.getMonkeyId().trim().length() > 0 && compName.matches("\\w+")) {
				// It's a script invocation but the monkeyId contains no substitution vars so we can
				// use it as the componentType (ie, app.scriptName() instead of
				// app.script("scriptName");
				component = Template.lowerCamel(compName) + "()";
			} else if ("*".equals(cmd.getComponentType())) {
				// Use View componentType for *
				component = "view(" + monkeyId + ")";
			} else {
				// Use the componentType for the scriptName and an expression for monkeyId (ie,
				// app.type("monkeyId"))
				component = Template.lowerCamel(cmd.getComponentType()) + "(" + monkeyId + ")";
			}

			String c = "app." + component + "." + Template.lowerCamel(cmd.getAction()) + "("
					+ arg_buf.toString() + ");\n";
			cmd_buf.append(c);
		}

		String[] parts = scriptName.split("\\.");
		String action;
		if (parts.length > 1) {
			action = parts[1];
			scriptName = parts[0];
		} else {
			action = "run";
		}
		script.replace("ACTION", action);
		script.replace("LIB_NAME", projectName);
		script.replace("SCRIPT_NAME", scriptName);
		script.replace("PARAMS", param_buf.toString());
		script.replace("DEFAULT_VALS", default_buf.toString());
		script.replace("COMMANDS", Template.removeReturn(cmd_buf.toString()));

		return script.toString();
	};

	public static Map<String, String> getVars(Command command) {
		LinkedHashMap<String, String> vars = new LinkedHashMap<String, String>();
		for (String arg : command.getArgs()) {
			if (arg.contains("=")) {
				String[] parts = arg.split("=");
				String var = parts[0];
				if (JAVASCRIPT_RESERVED.contains(var)) {
					var = "_" + var;
				}
				String val = parts[1];
				vars.put(var, val);
			} else {
				if (JAVASCRIPT_RESERVED.contains(arg)) {
					arg = "_" + arg;
				}
				vars.put(arg, null);
			}
		}
		return vars;
	}

	// Build a concatenation expression from text with embedded substitution args
	private static String getArgExpr(String arg) {
		StringBuffer expr = new StringBuffer();
		String[] literals = SUBSTITUTION_VAR.split(arg);
		if (literals.length > 0 && literals[0].length() > 0) {
			expr.append(escQuote(literals[0]));
		}
		Matcher matcher = SUBSTITUTION_VAR.matcher(arg);
		int i = 1;
		while (matcher.find()) {
			if (expr.length() > 0) {
				expr.append(" + ");
			}
			String token = matcher.group(1);
			String var = token.substring(2);
			if (JAVASCRIPT_RESERVED.contains(var)) {
				var = "_" + var;
			}
			if (token.startsWith("$")) {
				expr.append(var);
			} else {
				try {
					var = "arguments[" + (Integer.valueOf(var) - 1) + "]";
				} catch (NumberFormatException e) {
					// It's a named built-in var
					if (var.equals("monkeyId")) {
						var = "this.monkeyId";
					} else {
						// if we don't know it surround with quotes
						var = "\"" + var + "\"";
					}
				}
				expr.append(var);
			}
			if (literals.length > i && literals[i].length() > 0) {
				expr.append(" + " + escQuote(literals[i]));
			}
			i++;
		}
		return expr.toString();
	}

	private static String escQuote(String arg) {
		return "\"" + arg.replaceAll("\"", "\\\"") + "\"";
	}

	public static void main(String[] args) {
		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage: java JSMTGenerator <proj name> <input.mt> [output.js]");
			System.exit(1);
		}

		String projName = args[0];

		File input = new File(args[1]);
		if (!input.exists()) {
			System.err.println("ERROR: input '" + args[1] + "' not found");
			System.err.println("ERROR: workingDir='" + new File(".").getAbsolutePath() + "'");
			System.exit(1);
		}
		if (!input.isFile()) {
			System.err.println("ERROR: input '" + input.getAbsolutePath() + "' not a file!");
			System.exit(1);
		}
		if (!input.getName().toLowerCase().endsWith(CommandWorld.SCRIPT_EXT)) {
			System.err.println("ERROR: input '" + input.getAbsolutePath()
					+ "' must have .mt extension");
			System.exit(1);
		}

		File output = null;
		if (args.length == 2) {
			String outname = FileUtils.removeExt(input.getName(), CommandWorld.SCRIPT_EXT)
					+ CommandWorld.JS_EXT;
			output = new File(input.getParentFile(), outname);
		} else {
			output = new File(args[2]);
		}

		String js = JSMTGenerator.createScript(projName, input);

		try {
			FileUtils.writeFile(output, js);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}