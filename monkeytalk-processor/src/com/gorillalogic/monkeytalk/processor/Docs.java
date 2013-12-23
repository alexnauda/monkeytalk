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
package com.gorillalogic.monkeytalk.processor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.finder.Finder;

/**
 * Class for getting script docs.
 */
public class Docs {
	private CommandWorld world;

	/**
	 * Instantiate a docs processor with the given project root directory.
	 * 
	 * @param rootDir
	 *            the project dir
	 */
	public Docs(File rootDir) {
		this(new CommandWorld(rootDir));
	}

	/**
	 * Instantiate a docs processor with the given command world.
	 * 
	 * @param world
	 *            the command world for the project
	 */
	public Docs(CommandWorld world) {
		this.world = world;
	}

	/**
	 * Get the docs for the given script. Docs are returned as a map of variables as keys and their
	 * description as values.
	 * 
	 * @param filename
	 *            the script
	 * @return the docs map
	 */
	public Map<String, String> getDocs(String filename) {
		if (filename == null) {
			return null;
		}

		Map<String, String> docs = new HashMap<String, String>();
		List<Command> commands = world.getScript(filename);

		if (commands == null) {
			return null;
		}

		Command docScript = Finder.findCommandByName(commands, "doc.script");
		if (docScript != null && docScript.getArgs().size() >= 1) {
			docs.put(filename, docScript.getArgs().get(0));
		}

		Command varsDefine = Finder.findCommandByName(commands, "vars.define");
		if (varsDefine != null) {
			Command docVars = Finder.findCommandByName(commands, "doc.vars");
			Map<String, String> docVarsMap = new HashMap<String, String>();
			if (docVars != null) {
				for (String arg : docVars.getArgs()) {
					String[] m = arg.split("=");
					String key = m[0].toLowerCase();
					String val = (m.length > 1 ? m[1] : null);

					if (val != null) {
						if (val.startsWith("\"") && val.endsWith("\"")) {
							val = val.substring(1, val.length() - 1);
						}
						docVarsMap.put(key, val);
					}
				}
			}

			for (String arg : varsDefine.getArgs()) {
				String[] m = arg.split("=");
				String key = m[0].toLowerCase();
				String val = (m.length > 1 ? m[1] : null);

				if (val != null) {
					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					}
				}

				String docDefault = "The default value is '" + val + "'.";

				if (docVarsMap.containsKey(key)) {
					String doc = docVarsMap.get(key);
					docs.put(key, (doc.endsWith(".") ? doc : doc + ".") + " " + docDefault);
				} else {
					docs.put(key, docDefault);
				}
			}
		}

		return (docs.size() == 0 ? null : docs);
	}

	@Override
	public String toString() {
		return "Docs: rootDir=" + world.getRootDir();
	}
}