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

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.processor.command.Vars;
import com.gorillalogic.monkeytalk.processor.js.MonkeyTalkJS;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * Run a Javascript script and return the result.
 */
public class JSProcessor extends BaseProcessor {
	private MonkeyTalkJS mtjs;

	/**
	 * Instantiate a script processor with the given script processor.
	 * 
	 * @param processor
	 *            the script processor
	 */
	public JSProcessor(ScriptProcessor processor) {
		super(processor);

		try {
			mtjs = new MonkeyTalkJS(processor);
		} catch (ScriptException ex) {
			mtjs = null;
		}
	}

	/**
	 * Set the MonkeyTalk JS engine to the given engine.
	 * 
	 * @param mtjs
	 *            the MonkeyTalk JS engine
	 */
	public void setMonkeyTalkJS(MonkeyTalkJS mtjs) {
		this.mtjs = mtjs;
	}

	/**
	 * Run the JS script referenced by the supplied command. The command must not contain
	 * substitution vars, and cannot be a "runWith" (ex: {@code Script foo.js Run}).
	 * 
	 * @param cmd
	 *            the MonkeyTalk command
	 * @return the result of the script run
	 */
	public PlaybackResult runJavascript(Command cmd) {
		return runJavascript(cmd, null);
	}

	/**
	 * Run the JS script referenced by the supplied command in the given scope. The command must not
	 * contain substitution vars, and cannot be a "runWith". Furthermore, the monkeyId must be the
	 * full javascript filename including extension (ex: {@code Script foo.js Run}). Assumes
	 * MonkeyTalkAPI.js and the project's generated js wrapper lib are in a {@code libs}
	 * subdirectory.
	 * 
	 * @param cmd
	 *            the MonkeyTalk command
	 * @param scope
	 *            the script scope
	 * @return the result of the script run
	 */
	public PlaybackResult runJavascript(Command cmd, Scope scope) {
		long startTime = System.currentTimeMillis();

		if (scope == null) {
			scope = new Scope("anonymous");
			scope.setCurrentCommand(cmd);
		}

		if (cmd == null) {
			return errorResult("command is null", scope, startTime);
		} else if (!"script".equalsIgnoreCase(cmd.getComponentType())
				|| !"run".equalsIgnoreCase(cmd.getAction())) {
			return errorResult("command '" + cmd.getCommand()
					+ "' is illegal JSProcessor command - only 'script.run' allowed", scope,
					startTime);
		}

		String componentType = cmd.getComponentType();
		String monkeyId = cmd.getMonkeyId();
		String action = cmd.getAction();

		if (!world.fileExists(monkeyId)) {
			return errorResult("script '" + monkeyId + "' not found", scope, startTime);
		}

		if (monkeyId.matches("\\A[^\\.]+\\.[^\\.]+\\.js\\Z")) {
			String[] parts = FileUtils.removeExt(monkeyId, CommandWorld.JS_EXT).split("\\.");
			componentType = parts[0];
			if (!componentType.matches(Vars.VALID_VARIABLE_PATTERN)) {
				return errorResult(
						"filename '"
								+ monkeyId
								+ "' has illegal component type -- both parts of custom commands in JSProcessor must begin with a letter and contain only letters, numbers, and underscores",
						scope, startTime);
			}
			action = parts[1];
			if (!action.matches(Vars.VALID_VARIABLE_PATTERN)) {
				return errorResult(
						"filename '"
								+ monkeyId
								+ "' has illegal action -- both parts of custom commands in JSProcessor must begin with a letter and contain only letters, numbers, and underscores",
						scope, startTime);
			}
		} else {
			componentType = FileUtils.removeExt(monkeyId, CommandWorld.JS_EXT);
			if (!componentType.matches(Vars.VALID_VARIABLE_PATTERN)) {
				return errorResult(
						"filename '"
								+ monkeyId
								+ "' is illegal -- filenames in JSProcessor must begin with a letter and contain only letters, numbers, and underscores",
						scope, startTime);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("load(\"" + cmd.getMonkeyId() + "\");\n");
		sb.append("var app = new MT.Application(\"" + getHost() + "\", " + getPort() + ", \""
				+ world.getRootDir() + "\"" + ");\n");
		sb.append("app." + lowerFirst(componentType) + "()." + lowerFirst(action) + "("
				+ getArgsAsString(cmd.getArgs()) + ");");

		return runJavascript(sb.toString(), scope);
	}

	private PlaybackResult runJavascript(String js, Scope scope) {
		if (mtjs == null) {
			return new PlaybackResult(PlaybackStatus.ERROR, "bad js engine");
		}

		List<Step> steps = new ArrayList<Step>();
		PlaybackResult result = new PlaybackResult(PlaybackStatus.OK);
		result.setStartTime(System.currentTimeMillis());

		try {
			mtjs.getEngine().put("ScopeObj", scope);
			mtjs.getEngine().put("StepsObj", steps);
			if (Globals.getGlobals().size() > 0) {
				mtjs.getEngine().eval(Globals.asJavascript());
			}
			mtjs.getEngine().eval(js);
		} catch (ScriptException ex) {
			// see if last step was a non-OK MT Command Execution
			PlaybackResult lastResult = null;
			if (steps != null && steps.size() > 0) {
				lastResult = steps.get(steps.size() - 1).getResult();
			}
			if (lastResult != null && !lastResult.getStatus().equals(PlaybackStatus.OK)) {
				result = copyResult(lastResult, scope, result.getStartTime());
			} else {
				result.setStatus(PlaybackStatus.ERROR);
				result.setMessage(ex.getMessage());
			}
		}

		result.setStopTime(System.currentTimeMillis());
		result.setScope(scope);
		result.setSteps(steps);
		return result;
	}

	/** Helper to lowercase the first letter of the given string. */
	private String lowerFirst(String s) {
		if (s.length() < 2) {
			return s.toLowerCase();
		}
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	/**
	 * Helper to convert the list of MonkeyTalk command args into a single comma-separated String.
	 * 
	 * @param args
	 *            the MonkeyTalk args
	 * @return args as a single comma-separated String
	 */
	private String getArgsAsString(List<String> args) {
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append('"').append(arg).append('"');
		}
		return sb.toString();
	}

	private PlaybackResult errorResult(String message, Scope scope, long startTime) {
		if (startTime == 0) {
			startTime = System.currentTimeMillis();
		}
		PlaybackResult result = new PlaybackResult(PlaybackStatus.ERROR, message);
		result.setStartTime(startTime);
		result.setStopTime(System.currentTimeMillis());
		result.setScope(scope);
		return result;
	}

	@Override
	public String toString() {
		return "JSProcessor:\nurl: http://" + getHost() + ":" + getPort() + "/\n"
				+ super.toString();
	}
}