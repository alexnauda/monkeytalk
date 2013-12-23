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
package com.gorillalogic.monkeytalk;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.api.meta.Action;
import com.gorillalogic.monkeytalk.api.meta.Arg;
import com.gorillalogic.monkeytalk.finder.Finder;
import com.gorillalogic.monkeytalk.parser.CSVParser;
import com.gorillalogic.monkeytalk.parser.MonkeyTalkParser;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * Scan the project folder to provide file system information and meta information to the processors
 * ({@link ScriptProcessor}, {@link SuiteProcessor}, etc.).
 */
public class CommandWorld {
	/**
	 * MonkeyTalk script file extension.
	 */
	public static final String SCRIPT_EXT = ".mt";
	/**
	 * MonkeyTalk suite file extension.
	 */
	public static final String SUITE_EXT = ".mts";
	/**
	 * MonkeyTalk Javascript file extension.
	 */
	public static final String JS_EXT = ".js";
	/**
	 * MonkeyTalk data file extension.
	 */
	public static final String DATA_EXT = ".csv";

	/**
	 * Pattern to match files named <code>&lt;componentType>.&lt;action>.mt</code>
	 */
	private static final String CUSTOM_PATTERN = "[^\\.]+\\.[^\\.]+\\" + SCRIPT_EXT;

	private File rootDir;

	/**
	 * Instantiate an empty world.
	 */
	public CommandWorld() {
		this(null);
	}

	/**
	 * Instantiate a command world from the given root directory.
	 * 
	 * @param rootDir
	 *            the project root directory (aka the directory where all your scripts, suites,
	 *            datafiles, etc. are located)
	 */
	public CommandWorld(File rootDir) {
		if (rootDir == null || !rootDir.exists() || !rootDir.isDirectory()) {
			this.rootDir = null;
		} else {
			this.rootDir = rootDir.getAbsoluteFile();
		}
	}

	/**
	 * Get the root directory (aka the <i>project</i> directory where all your scripts are).
	 * 
	 * @return the root directory
	 */
	public File getRootDir() {
		return rootDir;
	}

	/**
	 * Does the given script have a Javascript override? Returns true if yes {@code script.mt} is
	 * overridden by {@code script.js}, otherwise false.
	 * 
	 * @return true if a Javascript override exists, otherwise false
	 */
	public boolean hasJavascriptOverride(String filename) {
		File f = findFileWithExt(filename, JS_EXT);
		return (f != null);
	}

	/**
	 * Get the MonkeyTalk commands parsed from the given script.
	 * 
	 * @param filename
	 *            the suite filename
	 * @return the commands parsed from the suite
	 */
	public List<Command> getScript(String filename) {
		File f = findFileWithExt(filename, SCRIPT_EXT);
		return (f != null ? MonkeyTalkParser.parseFile(f) : null);
	}

	/**
	 * Get the MonkeyTalk commands parsed from the given suite.
	 * 
	 * @param filename
	 *            the suite filename
	 * @return the commands parsed from the suite
	 */
	public List<Command> getSuite(String filename) {
		File f = findFileWithExt(filename, SUITE_EXT);
		return (f != null ? MonkeyTalkParser.parseFile(f) : null);
	}

	/**
	 * Get the CSV data parsed from the given datafile.
	 * 
	 * @param filename
	 *            the datafile filename
	 * @return the CSV data parsed from the datafile
	 */
	public List<Map<String, String>> getData(String filename) {
		File f = findFileWithExt(filename, DATA_EXT);
		return (f != null ? CSVParser.parseFile(f) : null);
	}

	/**
	 * Get the list of script files in the project (both the scripts and the custom commands).
	 * 
	 * @return the list of script files
	 */
	public List<File> getScriptFiles() {
		return findFilesWithExt(SCRIPT_EXT);
	}

	/**
	 * Get the list of suite files in the project.
	 * 
	 * @return the list of suite files
	 */
	public List<File> getSuiteFiles() {
		return findFilesWithExt(SUITE_EXT);
	}

	/**
	 * Get the list of datafiles in the project.
	 * 
	 * @return the list of datafiles
	 */
	public List<File> getDataFiles() {
		return findFilesWithExt(DATA_EXT);
	}

	/**
	 * Get the list of custom command files in the project.
	 * 
	 * @return the list of custom command files
	 */
	public List<File> getCustomCommandFiles() {
		return findFilesWithRegex(CUSTOM_PATTERN);
	}

	/**
	 * Get the list of Javascript files in the project.
	 * 
	 * @return the list of Javascript files.
	 */
	public List<File> getJavascriptFiles() {
		return findFilesWithExt(JS_EXT);
	}

	/**
	 * Get the Meta API {@link com.gorillalogic.monkeytalk.api.meta.Action} for the given script
	 * filename.
	 * 
	 * @param filename
	 *            the script filename
	 * @return the action
	 */
	public Action getAPIAction(String filename) {
		Command cmd = null;
		List<Command> commands = getScript(filename);

		// get all arg docs
		Map<String, String> descArgs = new HashMap<String, String>();
		cmd = Finder.findCommandByName(commands, "doc.vars");
		if (cmd != null) {
			for (String arg : cmd.getArgs()) {
				int idx = arg.indexOf("=");
				if (idx != -1) {
					String key = arg.substring(0, idx);
					String val = arg.substring(idx + 1);

					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					}

					descArgs.put(key, val);
				}
			}
		}

		// get all args
		List<Arg> args = new ArrayList<Arg>();
		cmd = Finder.findCommandByName(commands, "vars.define");
		if (cmd != null) {
			for (String arg : cmd.getArgs()) {
				int idx = arg.indexOf("=");
				if (idx != -1) {
					String key = arg.substring(0, idx);
					String val = arg.substring(idx + 1);

					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					}

					args.add(new Arg(key, descArgs.get(key), "String", val));
				}
			}
		}

		// get the script doc (aka action description)
		String desc = null;
		cmd = Finder.findCommandByName(commands, "doc.script");
		if (cmd != null && cmd.getArgs().size() > 0) {
			String val = cmd.getArgs().get(0);

			if (val.startsWith("\"") && val.endsWith("\"")) {
				val = val.substring(1, val.length() - 1);
			}

			desc = val;
		}

		return new Action(filename, desc, (args.size() > 0 ? args : null), null, null);
	}

	/**
	 * Search the world for a file with the given filename.
	 * 
	 * @param filename
	 *            the filename to search for
	 * @return true if found, otherwise false
	 */
	public boolean fileExists(String filename) {
		if (filename != null) {
			return (FileUtils.findFile(filename, rootDir) != null);
		}
		return false;
	}

	/**
	 * Search for a file with the given filename and extension. This will append the extension to
	 * the filename if it's not already there. NOTE: this is <b>case-insensitive</b> on both
	 * filename and extension.
	 * 
	 * @param filename
	 *            the suite filename
	 * @return the file, or null if not found
	 */
	private File findFileWithExt(String filename, String ext) {
		if (filename != null) {
			if (!filename.toLowerCase().endsWith(ext.toLowerCase())) {
				filename += ext;
			}
			return FileUtils.findFile(filename, rootDir);
		}
		return null;
	}

	/**
	 * Find the list of files with the given extension. Could return an empty list, but never
	 * {@code null}. NOTE: match is <b>case-insensitive</b> on extension.
	 * 
	 * @param ext
	 *            the file extension
	 * @return the list of files (could be empty, but never null).
	 */
	private List<File> findFilesWithExt(String ext) {
		List<File> list = new ArrayList<File>();
		if (rootDir != null) {
			ext = ext.toLowerCase();
			for (File f : rootDir.listFiles()) {
				if (f.getName().toLowerCase().endsWith(ext)) {
					list.add(f);
				}
			}
			Collections.sort(list);
		}
		return list;
	}

	/**
	 * Find the list of files matching the given regex pattern. Could return an empty list, but
	 * never {@code null}. NOTE: match is <b>case-insensitive</b> on extension.
	 * 
	 * @param pattern
	 *            the regex pattern
	 * @return the list of files (could be empty, but never null).
	 */
	private List<File> findFilesWithRegex(String pattern) {
		List<File> list = new ArrayList<File>();
		if (rootDir != null) {
			for (File f : rootDir.listFiles()) {
				if (f.getName().toLowerCase().matches(pattern)) {
					list.add(f);
				}
			}
			Collections.sort(list);
		}
		return list;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("RootDir: ");
		sb.append(rootDir != null ? rootDir.getAbsolutePath() : "null");
		sb.append("\nScripts: ");
		sb.append(printFiles(getScriptFiles()));
		sb.append("\nSuites: ");
		sb.append(printFiles(getSuiteFiles()));
		sb.append("\nCustomCommands: ");
		sb.append(printFiles(getCustomCommandFiles()));
		sb.append("\nJavascripts: ");
		sb.append(printFiles(getJavascriptFiles()));
		sb.append("\nDatafiles: ");
		sb.append(printFiles(getDataFiles()));
		sb.append("\n");

		return sb.toString();
	}

	/**
	 * Helper to print the given list of strings.
	 * 
	 * @param list
	 *            the list of strings
	 * @return the strings joined by newlines
	 */
	private String printFiles(List<File> list) {
		if (list.size() == 0) {
			return "none";
		}
		StringBuilder sb = new StringBuilder();
		for (File f : list) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(f.getName());
		}
		return sb.toString();
	}
}