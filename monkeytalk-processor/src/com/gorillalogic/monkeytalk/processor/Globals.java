package com.gorillalogic.monkeytalk.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.processor.command.Vars;

/**
 * Static helper class to handle global MonkeyTalk variables.
 */
public class Globals {
	public static final String ILLEGAL_MSG = "global " + Vars.ILLEGAL_MSG;
	public static final String RESERVED_MSG = "is a reserved global variable name";

	private static final Pattern VARIABLES_IN_STRING = Pattern
			.compile("(\\S+=\".*?\"|\\S+='.*?'|\\S+=[^\\s]+)\\s*");

	private static Map<String, String> globals;

	static {
		globals = new LinkedHashMap<String, String>();
	}

	private Globals() {
	}

	/**
	 * Clear all globals.
	 */
	public static void clear() {
		globals = new LinkedHashMap<String, String>();
	}

	/**
	 * Set the global variable given the name and value. If name or value is {@code null}, then do
	 * nothing. Throws exception if the variable name is illegal.
	 * 
	 * @param name
	 *            the variable name
	 * @param value
	 *            the variable value
	 */
	public static void setGlobal(String name, String value) {
		if (name != null && value != null) {
			validateName(name, null);
			globals.put(name, value);
		}
	}

	/**
	 * Set the globals from the given map of variables. Throws exception if any variable name in
	 * given map is illegal.
	 * 
	 * @param globals
	 */
	public static void setGlobals(Map<String, String> globals) {
		if (globals != null && globals.size() > 0) {
			for (Map.Entry<String, String> entry : globals.entrySet()) {
				Globals.setGlobal(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Set the globals from the given properties file (typically the {@code globals.properties}
	 * file). Throws exception if file is null, file is not found, file is folder, or if variable
	 * name in file is illegal.
	 * 
	 * @param f
	 *            the properties file
	 */
	public static void setGlobals(File f) throws IOException {
		if (f == null || !f.exists() || !f.isFile()) {
			return;
		}

		InputStream in = null;
		try {
			Properties props = new Properties();
			in = new FileInputStream(f);
			props.load(in);

			for (String key : props.stringPropertyNames()) {
				validateName(key, "globals file '" + f.getName() + "' has");
				String val = props.getProperty(key);
				Globals.setGlobal(key, val);
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

	/**
	 * Parse the given string of global variables and return it as a map. Throws exception if
	 * variable name in given string is illegal. Used to parse the {@code globals} attribute in the
	 * {@code monkeytalk-ant} project.
	 * 
	 * @param s
	 *            the string of global variables to be parsed
	 * @return the map of variables
	 */
	public static Map<String, String> parse(String s) {
		Map<String, String> map = new LinkedHashMap<String, String>();
		if (s != null && s.length() > 0) {
			Matcher m = VARIABLES_IN_STRING.matcher(s);
			while (m.find()) {
				String tok = m.group(1);
				String[] parts = tok.split("=");
				if (parts.length > 1) {
					validateName(parts[0], null);

					String val = parts[1];
					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					} else if (val.startsWith("'") && val.endsWith("'")) {
						val = val.substring(1, val.length() - 1);
					}
					map.put(parts[0], val);
				}
			}
		}
		return map;
	}

	/**
	 * Get the map of global variables.
	 * 
	 * @return the globals
	 */
	public static Map<String, String> getGlobals() {
		return Collections.unmodifiableMap(globals);
	}

	/**
	 * Get a global variable by name. Returns {@code null} if name does not exist.
	 * 
	 * @param name
	 *            the variable name
	 * @return the variable value
	 */
	public static String getGlobal(String name) {
		return globals.get(name);
	}

	/**
	 * True if a global variable of the given name exists, otherwise false.
	 * 
	 * @param name
	 *            the variable name
	 * @return true if the global variable exists, otherwise false.
	 */
	public static boolean hasGlobal(String name) {
		return globals.containsKey(name);
	}

	/**
	 * Delete a global variable by name
	 * 
	 * @param name
	 *            the variable name
	 * @return the deleted variable value (or {@code null} if not found)
	 */
	public static String deleteGlobal(String name) {
		return globals.remove(name);
	}

	/**
	 * Validate the given variable name. Throws an exception if variable name is illegal, otherwise
	 * does nothing. The given extra message is prepended to the exception message. Variable name
	 * validation is done automatically by {@link Globals#setGlobal(String, String)}.
	 * 
	 * @param name
	 *            the variable name
	 * @param msg
	 *            the extra message
	 * @throws RuntimeException
	 *             if variable name is illegal.
	 */
	public static void validateName(String name, String msg) throws RuntimeException {
		if (!name.matches(Vars.VALID_VARIABLE_PATTERN)) {
			throw new RuntimeException((msg != null ? msg + " " : "") + "illegal global variable '"
					+ name + "' -- " + ILLEGAL_MSG);
		}
		if (JSHelper.RESERVED_WORDS.contains(name)) {
			throw new RuntimeException((msg != null ? msg + " " : "") + "illegal global variable '"
					+ name + "' -- " + RESERVED_MSG);
		}
	}

	/**
	 * Helper to output the entire global variables map as a String.
	 * 
	 * @return the global variables as a string
	 */
	public static String asString() {
		StringBuilder sb = new StringBuilder("{");
		for (Map.Entry<String, String> entry : globals.entrySet()) {
			sb.append(sb.length() > 1 ? ", " : " ").append(entry.getKey()).append(":'")
					.append(entry.getValue()).append("'");
		}
		return sb.append(" }").toString();
	}

	/**
	 * Helper to output the entire global variables map as a String.
	 * 
	 * @return the global variables as a string
	 */
	public static String asJavascript() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : globals.entrySet()) {
			String val = entry.getValue();
			if (val.contains("'")) {
				// escape single quotes...
				val = val.replace("'", "\\\'");
			}
			sb.append("var ").append(entry.getKey()).append(" = '").append(val).append("';\n");
		}
		return sb.toString();
	}
}
