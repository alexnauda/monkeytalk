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
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.parser.MonkeyTalkParser;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * Generates JS LIB wrapper - individual JS wrappers for all scripts in the project dir.
 */
public class JSLibGenerator {
	public static String createLib(String libName, File projDir) {
		Template lib = new Template("templates/libgen/lib.template.js");
		Template scr = new Template("templates/libgen/scriptwrapper.template.js");
		Template cmd = new Template("templates/libgen/commandwrapper.template.js");
		Template act = new Template("templates/libgen/actionwrapper.template.js");
		lib.init();
		CommandWorld world = new CommandWorld(projDir);
		StringBuilder scripts = new StringBuilder();
		StringBuilder commands = new StringBuilder();

		for (File f : world.getScriptFiles()) {
			scr.init();
			String script = FileUtils.removeExt(f.getName(), CommandWorld.SCRIPT_EXT);
			if (script.indexOf('.') > 0) {
				// custom command, so skip it
				continue;
			}
			scr.replace("LIB_NAME", libName);
			scr.replace("SCRIPT_NAME", script);
			String lowerCamel = Template.lowerCamel(script);
			scr.replace("LOWER_SCRIPT_NAME", lowerCamel);
			String params = getParamString(f);
			scr.replace("PARAMS", params);
			scr.replace("THIS_AND_PARAMS", params.trim().length() == 0 ? "this" : "this, " + params);
			scripts.append(scr.toString());
		}

		String prevComponentType = null;
		for (File f : world.getCustomCommandFiles()) {

			String script = FileUtils.removeExt(f.getName(), CommandWorld.SCRIPT_EXT);
			String[] tokens = script.split("\\.");
			String componentType = tokens[0];
			String action = Template.lowerCamel(tokens[1]);
			String lowerComponentType = Template.lowerCamel(componentType);
			String params = getParamString(f);

			if (!componentType.equalsIgnoreCase(prevComponentType)) {
				cmd.init();
				cmd.replace("LIB_NAME", libName);
				cmd.replace("COMPONENT_TYPE", componentType);
				cmd.replace("LOWER_COMPONENT_TYPE", lowerComponentType);
				commands.append(cmd.toString());
			}

			act.init();
			act.replace("LIB_NAME", libName);
			act.replace("COMPONENT_TYPE", componentType);
			act.replace("ACTION", action);
			act.replace("PARAMS", params);
			act.replace("THIS_AND_COMP_AND_ACT_AND_PARAMS", "this, '" + componentType + "', '"
					+ action + "'" + (params.trim().length() == 0 ? "" : ", " + params));

			commands.append(act.toString());

			prevComponentType = componentType;
		}

		lib.replace("LIB_NAME", libName);
		lib.replace("SCRIPTS", scripts.toString());
		lib.replace("COMMANDS", commands.toString());
		return lib.toString();
	}

	private static String getParamString(File file) {
		StringBuilder params = new StringBuilder();
		List<Command> commands = MonkeyTalkParser.parseFile(file);

		for (Command command : commands) {
			if ("vars.define".equalsIgnoreCase(command.getCommandName())) {
				Map<String, String> vars = JSMTGenerator.getVars(command);
				for (String var : vars.keySet()) {
					if (params.length() > 0) {
						params.append(", ");
					}
					if (JSHelper.RESERVED_WORDS.contains(var)) {
						var = "_" + var;
					}
					params.append(var);
				}
			}
		}
		return params.toString();
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2 || args.length > 3) {
			System.err.println("Usage: java JSLibGenerator <project dir> <output> <copy API?>");
			System.exit(1);
		}

		File projDir = new File(args[0]);
		if (!projDir.exists()) {
			System.err.println("ERROR: projDir '" + args[0] + "' not found");
			System.err.println("ERROR: workingDir='" + new File(".").getAbsolutePath() + "'");
			System.exit(1);
		}
		if (!projDir.isDirectory()) {
			System.err.println("ERROR: projDir '" + projDir.getAbsolutePath() + "' not dir!");
			System.exit(1);
		}

		File target = new File(args[1]);
		String name = FileUtils.removeExt(target.getName(), CommandWorld.JS_EXT);

		String lib = JSLibGenerator.createLib(name, projDir);

		try {
			FileUtils.writeFile(target, lib);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		if (args.length == 3) {
			String s = args[2].toLowerCase().trim();
			if (s.startsWith("y") || s.startsWith("t") || s.startsWith("1")) {
				// copy MonkeyTalkAPI.js to dest
				File api = new File(target.getAbsoluteFile().getParentFile(), "MonkeyTalkAPI.js");
				InputStream in = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream("templates/MonkeyTalkAPI.js");
				FileUtils.writeFile(api, in);
			}
		}
	}
}