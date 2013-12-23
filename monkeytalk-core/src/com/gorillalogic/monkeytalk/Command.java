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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.CommandValidator.CommandStatus;
import com.gorillalogic.monkeytalk.parser.MonkeyTalkParser;

/**
 * This class represents a MonkeyTalk command, with componentType, monkeyId, action, args,
 * modifiers, etc. It can convert various inputs into a command, and provides various outputs (like
 * JSON).
 */
public class Command implements Cloneable {
	/**
	 * The comment prefix. MonkeyTalk command strings that begin with this are comments.
	 */
	public static final String COMMENT_PREFIX = "#";

	/**
	 * The command modifier prefix. Arguments that begin with this are MonkeyTalk command modifiers,
	 * like timeout, thinktime, etc.
	 */
	private static final String MODIFIER_PREFIX = "%";

	/**
	 * Default timeout (in ms) -- how long to retry before failing.
	 */
	public static final int DEFAULT_TIMEOUT = 2000;

	/**
	 * Default thinktime (in ms) -- how long to wait before playing the command.
	 */
	public static final int DEFAULT_THINKTIME = 500;

	/**
	 * Default retry delay (in ms) -- how long to wait before retrying.
	 */
	public static final int DEFAULT_RETRYDELAY = 100;

	public static final String SCREENSHOT_ON_ERROR = "screenshotonerror";
	public static final String ABORT_MODIFIER = "abort";
	public static final String IGNORE_MODIFIER = "ignore";
	public static final String SHOULD_FAIL_MODIFIER = "shouldfail";

	private String command;
	private String componentType;
	private String monkeyId;
	private String action;
	private List<String> args;
	private Map<String, String> modifiers;
	private boolean comment;
	private int defaultTimeout = -1;
	private int defaultThinktime = -1;

	/**
	 * Instantiate a null MonkeyTalk Command object.
	 */
	public Command() {
		initCommand();
	}

	/**
	 * Instantiate a MonkeyTalk Command object from string. If the incoming string begins with
	 * {@code #}, then it's a comment.
	 * 
	 * @param command
	 *            the MonkeyTalk command string
	 */
	public Command(String command) {
		initCommand();
		parseCommand(command);
	}

	/**
	 * Instantiate a MonkeyTalk Command object directly from its parts. If the {@code componentType}
	 * begins with {@code #}, then it's a comment and all the other parts are set to null.
	 * 
	 * @param componentType
	 *            the MonkeyTalk componentType
	 * @param monkeyId
	 *            the MonkeyTalk monkeyId
	 * @param action
	 *            the MonkeyTalk action
	 * @param args
	 *            the MonkeyTalk args
	 * @param modifiers
	 *            the MonkeyTalk command modifiers
	 */
	public Command(String componentType, String monkeyId, String action, List<String> args,
			Map<String, String> modifiers) {
		initCommand();
		setProperties(componentType, monkeyId, action, args, modifiers);
	}

	/**
	 * 
	 * Instantiate a MonkeyTalk Command object from JSON.
	 * 
	 * @param json
	 *            the JSON object
	 */
	public Command(JSONObject json) {
		String componentType = json.optString("componentType", "*");
		String monkeyId = json.optString("monkeyId", "*");
		String action = json.optString("action");

		List<String> args = new ArrayList<String>();
		JSONArray jsonArgs = json.optJSONArray("args");
		if (jsonArgs != null) {
			for (int i = 0; i < jsonArgs.length(); i++) {
				args.add(jsonArgs.optString(i));
			}
		}

		Map<String, String> mods = new HashMap<String, String>();
		JSONObject jsonModifiers = json.optJSONObject("modifiers");
		if (jsonModifiers != null) {

			@SuppressWarnings("unchecked")
			Iterator<String> keys = jsonModifiers.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				mods.put(key, jsonModifiers.optString(key));
			}
		}

		initCommand();
		setProperties(componentType, monkeyId, action, args, mods);
	}

	/**
	 * Get the MonkeyTalk command string.
	 * 
	 * @return the MonkeyTalk command string
	 */
	public String getCommand() {
		return getCommand(false);
	}

	/**
	 * Get the MonkeyTalk command string, optionally showing or hiding the timing modifiers.
	 * 
	 * @param showDefaultTimings
	 *            true to display the <code>%timeout</code> and <code>%thinktime</code>, otherwise
	 *            hide them
	 * 
	 * @return the MonkeyTalk command string
	 */
	public String getCommand(boolean showDefaultTimings) {
		if (command == null) {
			return null;
		} else if (showDefaultTimings) {
			return new Command(getCommandAsJSON()).getRawCommand();
		} else {
			return command.replaceAll(" %(timeout=" + getDefaultTimeout() + "|thinktime="
					+ getDefaultThinktime() + ")\\b", "");
		}
	}

	/**
	 * Get the raw command string.
	 * 
	 * @return the command string
	 */
	public String getRawCommand() {
		return command;
	}

	/**
	 * Helper to re-build the MonkeyTalk command string from its parts.
	 */
	private void setCommand() {
		setCommand(componentType, monkeyId, action, getArgsAsString(), getModifiersAsString());
	}

	/**
	 * Helper to re-build the MonkeyTalk command string from its parts.
	 * 
	 * @param componentType
	 *            the MonkeyTalk componentType
	 * @param monkeyId
	 *            the MonkeyTalk monkeyId
	 * @param action
	 *            the MonkeyTalk action
	 * @param args
	 *            the MonkeyTalk args string
	 * @param modifiers
	 *            the MonkeyTalk modifiers string
	 */
	private void setCommand(String componentType, String monkeyId, String action, String args,
			String modifiers) {
		if (comment) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		String part;

		do {
			if (componentType == null || componentType.length() == 0) {
				break;
			}
			sb.append(componentType);

			part = exportStr(monkeyId);
			if (part.length() == 0) {
				break;
			}
			sb.append(' ').append(part);

			if (action == null || action.length() == 0) {
				break;
			}
			sb.append(' ').append(action);

			if (args.length() > 0) {
				sb.append(' ').append(args);
			}

			if (modifiers.length() > 0) {
				sb.append(' ').append(modifiers);
			}
		} while (false);

		String cmd = sb.toString().trim();
		command = (cmd.length() == 0 ? null : cmd);

	}

	/**
	 * Helper to compute the command name, just {@code componentType.action} lowercased.
	 * 
	 * @return the command name
	 */
	public String getCommandName() {
		return (componentType + "." + action).toLowerCase();
	}

	/**
	 * Get the MonkeyTalk componentType.
	 * 
	 * @return the MonkeyTalk componentType
	 */
	public String getComponentType() {
		return componentType;
	}

	/**
	 * Set the MonkeyTalk componentType, stripping any quotes or spaces.
	 * 
	 * @param componentType
	 *            the MonkeyTalk componentType
	 */
	public void setComponentType(String componentType) {
		if (componentType != null) {
			componentType = componentType.trim();

			if (componentType.startsWith(COMMENT_PREFIX)) {
				// we are a comment, so reset & store comment unaltered
				initCommand();
				comment = true;
				command = componentType;
			} else {
				// not a comment, so clean it & store
				comment = false;

				componentType = importStr(componentType.replaceAll("\\s+", ""));

				// default componentType is *
				if (componentType.length() == 0) {
					componentType = "*";
				}

				// store component type & update the command string
				this.componentType = componentType;
				setCommand();
			}
		}
	}

	/**
	 * Get the MonkeyTalk monkeyId.
	 * 
	 * @return the MonkeyTalk monkeyId
	 */
	public String getMonkeyId() {
		return escape(monkeyId);
	}

	/**
	 * Set the MonkeyTalk monkeyId, first trimming any spaces, then trimming any quotes.
	 * 
	 * @param monkeyId
	 *            the MonkeyTalk monkeyId
	 */
	public void setMonkeyId(String monkeyId) {
		if (monkeyId != null) {
			monkeyId = importStr(monkeyId.trim());
			if (monkeyId.length() == 0) {
				monkeyId = "*";
			}
			this.monkeyId = monkeyId;
			setCommand();
		}
	}

	/**
	 * Get the MonkeyTalk action.
	 * 
	 * @return the MonkeyTalk action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Set the MonkeyTalk action, stripping any quotes or spaces.
	 * 
	 * @param action
	 *            the MonkeyTalk action
	 */
	public void setAction(String action) {
		if (action != null) {
			action = importStr(action.replaceAll("\\s+", ""));
			if (action.length() == 0) {
				action = "*";
			}
			this.action = action;
			setCommand();
		}
	}

	/**
	 * Get the list of arguments. If some of the args were originally quoted in the MonkeyTalk
	 * command string, they are returned in unquoted form. For example, the MonkeyTalk command
	 * string {@code Input password EnterText "i like cheese" "me too" bacon} with be stored as
	 * {@code i like cheese}, {@code me too}, {@code bacon} in the args list.
	 * 
	 * @return the list of MonkeyTalk Args
	 */
	public List<String> getArgs() {
		return Collections.unmodifiableList(args);
	}

	/**
	 * Get the list of MonkeyTalk command args as a javascript array eg. ['arg1','arg2','arg3'].
	 * 
	 * @return the MonkeyTalk args as a single string
	 */
	public String getArgsAsJsArray() {
		// if no args, return empty
		if (args.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			// if argument isn't a quoted key/value pair
			// NOTE: Assumes quoted pairs are escaped
			if (!arg.matches("[^\\s]+=\"[^\"]*\"")) {
				// format for export
				arg = exportStr(arg);

				// Surround arg with single quote
				arg = "'" + arg + "'";
			}
			sb.append(arg).append(',');
		}
		return "[" + sb.substring(0, sb.length() - 1) + "]";
	}

	/**
	 * Get the list of MonkeyTalk command args as a single string with quoted args as necessary (aka
	 * only when they contain spaces).
	 * 
	 * @return the MonkeyTalk args as a single string
	 */
	public String getArgsAsString() {
		// if no args, return empty
		if (args.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			// if argument isn't a quoted key/value pair
			// NOTE: Assumes quoted pairs are escaped
			if (!arg.matches("[^\\s]+=\"[^\"]*\"")) {
				// format for export
				arg = exportStr(arg);

				if (arg.startsWith(MODIFIER_PREFIX) && arg.contains("=")) {
					// arg looks like a modifier, so it must be quoted
					arg = "\"" + arg + "\"";
				}
			}
			sb.append(arg).append(' ');
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * Set the MonkeyTalk command args and MonkeyTalk command modifiers from the given string.
	 * 
	 * @param s
	 *            the MonkeyTalk args and modifiers as a string
	 */
	public void setArgsAndModifiers(String s) {
		if (s != null) {
			parseArgsAndModifiers(MonkeyTalkParser.parse(s));
			setCommand();
		}
	}

	/**
	 * Get the map of MonkeyTalk command modifiers. If some of the modifier values were originally
	 * quoted in the MonkeyTalk command string, they are returned in unquoted form. Additionally,
	 * the {@code %} prefix signifying a modifier key-value pair, is removed from the key name. For
	 * example, the modifier {@code %thinktime=123} would be stored with key {@code thinktime} and
	 * the value {@code 123} .
	 * 
	 * @return the map of MonkeyTalk command modifiers
	 */
	public Map<String, String> getModifiers() {
		return Collections.unmodifiableMap(modifiers);
	}

	/**
	 * Get the map of MonkeyTalk command modifiers as a single string with quoted values as
	 * necessary (aka only when they contain spaces).
	 * 
	 * @return the MonkeyTalk modifiers as a single string
	 */
	public String getModifiersAsString() {
		if (modifiers.size() == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (String key : modifiers.keySet()) {
			String val = exportStr(modifiers.get(key));
			sb.append(MODIFIER_PREFIX).append(key).append('=').append(val).append(' ');
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * Set a single MonkeyTalk command modifier with a named-value pair. A {@code null} key does
	 * nothing, but a valid key with a {@code null} value will cause the key to be deleted from the
	 * map.
	 * 
	 * @param key
	 *            the MonkeyTalk command modifier name
	 * @param value
	 *            the MonkeyTalk command modifier value
	 */
	public void setModifier(String key, String value) {
		if (key != null) {
			if (value != null) {
				// do NOT escaped
				modifiers.put(key, value);
				setCommand();
			} else if (modifiers.containsKey(key)) {
				modifiers.remove(key);
				setCommand();
			}
		}
	}

	/**
	 * Get the default timeout.
	 * 
	 * @return the timeout
	 */
	public int getDefaultTimeout() {
		return (defaultTimeout < 0 ? DEFAULT_TIMEOUT : defaultTimeout);
	}

	/**
	 * Set the default timeout. Typically by the processor to the global timeout.
	 * 
	 * @param defaultTimeout
	 *            the timeout
	 */
	public void setDefaultTimeout(int defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	/**
	 * Get the default thinktime.
	 * 
	 * @return the thinktime
	 */
	public int getDefaultThinktime() {
		return (defaultThinktime < 0 ? DEFAULT_THINKTIME : defaultThinktime);
	}

	/**
	 * Set the default thinktime. Typically set by the processor to the global thinktime.
	 * 
	 * @param defaultThinktime
	 */
	public void setDefaultThinktime(int defaultThinktime) {
		this.defaultThinktime = defaultThinktime;
	}

	/**
	 * Get the timeout MonkeyTalk command modifier, or {@link Command#DEFAULT_TIMEOUT} if not set.
	 * 
	 * @return the timeout
	 */
	public int getTimeout() {
		return getIntModiferValueWithDefault("timeout", getDefaultTimeout());
	}

	/**
	 * Get the timeout MonkeyTalk command modifier
	 * 
	 * @param noDefault
	 *            whether to return default if no value set
	 * @return timeout or -1 if unset
	 */
	public int getTimeoutRaw() {
		return getIntModiferValueWithDefault("timeout", -1);
	}

	/**
	 * Get the thinktime MonkeyTalk command modifier, or {@link Command#DEFAULT_THINKTIME} if not
	 * set.
	 * 
	 * @return the thinktime
	 */
	public int getThinktime() {
		return getIntModiferValueWithDefault("thinktime", getDefaultThinktime());
	}

	/**
	 * Get the thinktime MonkeyTalk command modifier
	 * 
	 * @return thinktime, or -1 if unset
	 */
	public int getThinktimeRaw() {
		return getIntModiferValueWithDefault("thinktime", -1);
	}

	/**
	 * Get the retry delay MonkeyTalk command modifier, or {@link Command#DEFAULT_RETRYDELAY} if not
	 * set.
	 * 
	 * @return the retry delay
	 */
	public int getRetryDelay() {
		return getIntModiferValueWithDefault("retrydelay", DEFAULT_RETRYDELAY);
	}

	/**
	 * Helper to get the value of a MonkeyTalk command modifier given its name (aka the key). If the
	 * modifier doesn't exist, return the given default value.
	 * 
	 * @param key
	 *            the MonkeyTalk command modifier
	 * @param defaultValue
	 *            the value to return if the modifier doesn't exist
	 * @return the value of the given key, or the default value if the modifier doesn't exist
	 */
	private int getIntModiferValueWithDefault(String key, int defaultValue) {
		int val = defaultValue;
		if (modifiers.containsKey(key)) {
			try {
				val = Integer.parseInt(modifiers.get(key).replaceAll("\\D+", "").toString());
			} catch (Exception ex) {
				val = defaultValue;
			}
		}
		return val;
	}

	/**
	 * If a MonkeyTalk command begins with {@code #}, the pound symbol, it is considered to be a
	 * single-line comment.
	 * 
	 * @return true if the MonkeyTalk command is a comment, otherwise false
	 */
	public boolean isComment() {
		return comment;
	}

	/**
	 * True if screenshot on error is on (aka if this command causes an error, then the Agent will
	 * return a screenshot along with the response), otherwise false. Defaults to {@code true}.
	 * 
	 * @return true if screenshot on error is on
	 */
	public boolean isScreenshotOnError() {
		String val = modifiers.get(SCREENSHOT_ON_ERROR);
		return (val == null ? true : !val.equalsIgnoreCase("false"));
	}

	/**
	 * True if ignore modifier is {@code true}, otherwise false. Defaults to {@code false}. If a
	 * command is ignored, it will be skipped by the processor during playback.
	 * 
	 * @return true if ignore is on
	 */
	public boolean isIgnored() {
		String val = modifiers.get(IGNORE_MODIFIER);
		return (val == null ? false : val.equalsIgnoreCase("true"));
	}

	/**
	 * True if ignore modifier contains the given value, otherwise false. Defaults to {@code false}.
	 * If a command is ignored, it will be skipped by the processor during playback.
	 * 
	 * @return true if ignore is on
	 */
	public boolean isIgnored(String value) {
		String val = modifiers.get(IGNORE_MODIFIER);
		return (val == null || value == null ? false : val.toLowerCase().indexOf(
				value.toLowerCase()) != -1);
	}

	/**
	 * True if should fail modifier is {@code true}, otherwise false. Defaults to {@code false}. If
	 * a command is set to should fail, the processor expects it to generate a failure result during
	 * playback (and then it doesn't fail and returns ok), otherwise an ok result generates a
	 * failure.
	 * 
	 * @return true if should fail is on
	 */
	public boolean shouldFail() {
		String val = modifiers.get(SHOULD_FAIL_MODIFIER);
		return (val == null ? false : val.equalsIgnoreCase("true"));
	}

	/**
	 * Set screenshot on error, true to turn on, false to turn off.
	 * 
	 * @param screenshotOnError
	 *            true to turn on screenshot on error
	 */
	public void setScreenshotOnError(boolean screenshotOnError) {
		setModifier(SCREENSHOT_ON_ERROR, Boolean.toString(screenshotOnError));
	}

	private void initCommand() {
		componentType = null;
		monkeyId = null;
		action = null;
		args = new ArrayList<String>();
		modifiers = new HashMap<String, String>();
		comment = false;
		command = null;
	}

	private void setProperties(String componentType, String monkeyId, String action,
			List<String> args, Map<String, String> modifiers) {

		// process component type first, and abort if we are a comment
		setComponentType(componentType);
		if (comment) {
			return;
		}

		setMonkeyId(monkeyId);
		setAction(action);

		if (args != null) {
			for (String arg : args) {
				// escape arg and append
				this.args.add(importStr(arg));
			}
		}

		if (modifiers != null) {
			for (Map.Entry<String, String> mod : modifiers.entrySet()) {
				// do NOT escape modifiers, just append
				this.modifiers.put(mod.getKey(), mod.getValue());
			}
		}

		// update command string
		setCommand();
	}

	/**
	 * Parse a MonkeyTalk command string, and update all the properties of this Command object.
	 * 
	 * @param command
	 *            the MonkeyTalk command string
	 */
	private void parseCommand(String command) {
		List<String> tokens = MonkeyTalkParser.parse(command);

		if (tokens.size() > 0) {
			setComponentType(tokens.get(0));
		}
		if (tokens.size() > 1) {
			setMonkeyId(tokens.get(1));
		}
		if (tokens.size() > 2) {
			setAction(tokens.get(2));
		}
		if (tokens.size() > 3) {
			parseArgsAndModifiers(tokens.subList(3, tokens.size()));
		}
	}

	/**
	 * Helper to parse the given tokens into MonkeyTalk command args and command modifiers.
	 * 
	 * @param tokens
	 *            the tokens
	 */
	private void parseArgsAndModifiers(List<String> tokens) {
		args = new ArrayList<String>();

		for (String token : tokens) {
			if (token.startsWith(MODIFIER_PREFIX) && token.contains("=")) {
				// token is a modifier
				token = token.substring(1, token.length());

				String[] m = token.split("=");
				String key = m[0].toLowerCase();
				String val = (m.length > 1 ? m[1] : null);

				modifiers.put(key, importStr(val));
			} else {
				// otherwise, token is an arg
				args.add(importStr(token));
			}
		}

		// update command string
		setCommand();
	}

	/**
	 * Given all substitution parameters, search the MonkeyTalk command string for all instances and
	 * replace them with the corresponding value. The built-in variables {@code componentType},
	 * {@code monkeyId}, {@code action} are substituted as <code>%{componentType}</code>,
	 * <code>%{monkeyId}</code>, <code>%{action}</code>. The argument list is substituted as
	 * <code>%{1}</code>, <code>%{2}</code>, etc. The variables map (of name-value pairs) is
	 * substituted as <code>${varname...}</code>. <b>NOTE:</b> all the built-in variables and args
	 * are percent-curly bracket, <code>%{...}</code>, and the named variables are dollar-curly
	 * bracket, <code>${...}</code>.
	 * 
	 * @param componentType
	 *            the value to be substituted for <code>%{componentType}</code>
	 * @param monkeyId
	 *            the value to be substituted for <code>%{monkeyId}</code>
	 * @param action
	 *            the value to be substituted for <code>%{action}</code>
	 * @param args
	 *            the list of values to be substituted for <code>%{1}</code>, <code>%{2}</code>,
	 *            etc.
	 * @param variables
	 *            the map of name-value pairs to be substituted for <code>${varname...}</code>
	 * @return a new, fully substituted MonkeyTalk command
	 */
	public Command substitute(String componentType, String monkeyId, String action,
			List<String> args, Map<String, String> variables) {

		String newComponentType = substituteString(this.componentType, componentType, monkeyId,
				action, args, variables);
		String newMonkeyId = substituteString(this.monkeyId, componentType, monkeyId, action, args,
				variables);
		String newAction = substituteString(this.action, componentType, monkeyId, action, args,
				variables);

		List<String> newArgs = new ArrayList<String>();
		for (String arg : this.args) {
			newArgs.add(substituteString(arg, componentType, monkeyId, action, args, variables));
		}

		Map<String, String> newModifiers = new HashMap<String, String>();
		for (Map.Entry<String, String> mod : modifiers.entrySet()) {
			// do NOT escape modifiers
			newModifiers.put(
					mod.getKey(),
					substituteString(mod.getValue(), componentType, monkeyId, action, args,
							variables));
		}

		return new Command(newComponentType, newMonkeyId, newAction, newArgs, newModifiers);
	}

	/**
	 * Helper to replace the given string with all built-in vars, built-in args, and named
	 * variables.
	 * 
	 * @param s
	 *            the target string
	 * @param componentType
	 *            the value to be substituted for <code>%{componentType}</code>
	 * @param monkeyId
	 *            the value to be substituted for <code>%{monkeyId}</code>
	 * @param action
	 *            the value to be substituted for <code>%{action}</code>
	 * @param args
	 *            the list of values to be substituted for <code>%{1}</code>, <code>%{2}</code>,
	 *            etc.
	 * @param variables
	 *            the map of name-value pairs to be substituted for <code>${varname...}</code>
	 * @return the fully-substituted string
	 */
	private String substituteString(String s, String componentType, String monkeyId, String action,
			List<String> args, Map<String, String> variables) {
		if (s == null) {
			return null;
		}

		if (componentType != null) {
			s = s.replaceAll("\\%\\{componentType\\}", Matcher.quoteReplacement(componentType));
		}
		if (monkeyId != null) {
			s = s.replaceAll("\\%\\{monkeyId\\}", Matcher.quoteReplacement(monkeyId));
		}
		if (action != null) {
			s = s.replaceAll("\\%\\{action\\}", Matcher.quoteReplacement(action));
		}

		if (args != null) {
			for (int i = 0; i < args.size(); i++) {
				String target = "\\%\\{" + (i + 1) + "\\}";
				s = s.replaceAll(target, Matcher.quoteReplacement(args.get(i)));
			}
		}

		if (variables != null) {
			for (Map.Entry<String, String> var : variables.entrySet()) {
				String target = "\\$\\{" + var.getKey() + "\\}";
				s = s.replaceAll(target, Matcher.quoteReplacement(var.getValue()));
			}
		}

		return s;
	}

	/**
	 * Get the JSON representation of the MonkeyTalk command.
	 * 
	 * @return the MonkeyTalk command as JSON
	 */
	public JSONObject getCommandAsJSON() {
		return getCommandAsJSON(true);
	}

	/**
	 * Get the JSON representation of the MonkeyTalk command, optionally without thinktime and
	 * timeout.
	 * 
	 * @return the MonkeyTalk command as JSON
	 */
	public JSONObject getCommandAsJSON(boolean withTimings) {
		JSONObject json = new JSONObject();
		try {
			json.put("componentType", componentType);
			json.put("monkeyId", monkeyId);
			json.put("action", action);
			json.put("args", new JSONArray(args));

			JSONObject mods = new JSONObject(modifiers);
			if (withTimings) {
				mods.put("timeout", getTimeout());
				mods.put("thinktime", getThinktime());
				if (!isScreenshotOnError()) {
					mods.put("screenshotonerror", "false");
				}
			}
			json.put("modifiers", mods);
		} catch (JSONException ex) {
			return new JSONObject();
		}
		return json;
	}

	/**
	 * Convert an internal value to export-able format. Token characters are escaped as needed. The
	 * string is quoted if it contains any spaces.
	 * 
	 * @param s
	 *            MonkeyTalk command token in internal format
	 * @return MonkeyTalk token in export-able format
	 */
	private String exportStr(Object s) {
		if (s == null) {
			return "";
		}

		String e = escape(s.toString());
		return e.matches("\\S+") ? e : "\"" + e + "\"";
	}

	/***
	 * Convert the specified string into escaped form. Instances of the following are escaped:
	 * <ul>
	 * <li><code>&lt;tab> => "\t"</code>
	 * <li><code>&lt;backspace> => "\b"</code>
	 * <li><code>&lt;newline> => "\n"</code>
	 * <li><code>&lt;carriage return> => "\r"</code>
	 * <li><code>&lt;formfeed> => "\f"</code>
	 * <li><code>&lt;double quote> => "\""</code>
	 * <li><code>&lt;backslash> => "\\"</code>
	 * </ul>
	 * 
	 * @param s
	 *            string to be escaped
	 * @return the specified string in escaped form
	 */
	private String escape(String s) {
		// FIXME: don't escape anything for now...
		if (s == null || s.matches(".*")) { // s.matches("[^\\t\\010\\n\\r\\f\'\"\\\\]+")) {
			return s;
		}

		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\t':
				sb.append("\\t");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			default:
				sb.append(c);
				break;
			}
		}

		return sb.toString();
	}

	/**
	 * Convert the specified string to internal format. All escape sequences are replaced with the
	 * characters they represent and any bounding quotes are removed.
	 * 
	 * @param s
	 *            string to convert to internal format
	 * @return the specified string in internal format
	 */
	private String importStr(String s) {
		if (s == null) {
			return null;
		}

		s = s.trim();
		return unescape(s.matches("\".*\"") ? s.substring(1, s.length() - 1) : s);
	}

	/**
	 * Replace escape sequenced in the specified string with the characters they represent. The
	 * following sequences are supported:
	 * <ul>
	 * <li><code>"\t" => &lt;tab></code>
	 * <li><code>"\b" => &lt;backspace></code>
	 * <li><code>"\n" => &lt;newline></code>
	 * <li><code>"\r" => &lt;carriage return></code>
	 * <li><code>"\f" => &lt;formfeed></code>
	 * <li><code>"\"" => &lt;double quote></code>
	 * <li><code>"\\" => &lt;backslash></code>
	 * </ul>
	 * 
	 * @param s
	 *            string to be un-escaped
	 * @return the specified string in un-escaped form
	 */
	private String unescape(String s) {
		// FIXME: don't unescape anything for now...
		if (s == null || s.matches(".*")) { // s.matches("[^\\\\]+")) {
			return s;
		}

		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c == '\\') {
				c = s.charAt(++i);
				switch (c) {
				case 't':
					c = '\t';
					break;
				case 'b':
					c = '\b';
					break;
				case 'n':
					c = '\n';
					break;
				case 'r':
					c = '\r';
					break;
				case 'f':
					c = '\f';
					break;
				case '\"':
				case '\\':
					break;
				default:
					break;
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Validate the command and return true if valid, otherwise false.
	 * 
	 * @return true if the command is valid, otherwise false.
	 */
	public boolean isValid() {
		return (CommandStatus.OK == CommandValidator.validate(this).getStatus());
	}

	@Override
	public Command clone() {
		if (comment) {
			return new Command(command);
		} else {
			return new Command(componentType, monkeyId, action, args, modifiers);
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result
				+ (getComponentType() == null ? 0 : getComponentType().toLowerCase().hashCode());
		result = 31 * result + (getMonkeyId() == null ? 0 : getMonkeyId().hashCode());
		result = 31 * result + (getAction() == null ? 0 : getAction().toLowerCase().hashCode());
		result = 31 * result + getArgsAsString().hashCode();
		result = 31 * result + getModifiersAsString().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Command))
			return false;

		Command that = (Command) obj;

		boolean checkComponentType = (getComponentType() == null ? that.getComponentType() == null
				: getComponentType().equalsIgnoreCase(that.getComponentType()));

		boolean checkMonkeyId = (getMonkeyId() == null ? that.getMonkeyId() == null : getMonkeyId()
				.equals(that.getMonkeyId()));

		boolean checkAction = (getAction() == null ? that.getAction() == null : getAction()
				.equalsIgnoreCase(that.getAction()));

		boolean checkArgs = getArgsAsString().equals(that.getArgsAsString());

		boolean checkModifiers = getModifiersAsString().equals(that.getModifiersAsString());

		return checkComponentType && checkMonkeyId && checkAction && checkArgs && checkModifiers;
	}

	@Override
	/**
	 * Get the MonkeyTalk command string.
	 * @return the MonkeyTalk command string
	 */
	public String toString() {
		return command;
	}
}