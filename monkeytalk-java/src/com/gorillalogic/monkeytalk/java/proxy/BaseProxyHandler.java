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
package com.gorillalogic.monkeytalk.java.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.java.Logger;
import com.gorillalogic.monkeytalk.java.error.MonkeyTalkError;
import com.gorillalogic.monkeytalk.java.error.MonkeyTalkFailure;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.Step;

/**
 * Base class for the proxy handlers. Does the actually playing of MonkeyTalk commands.
 */
public class BaseProxyHandler {
	protected ScriptProcessor processor;
	protected Scope scope;
	protected List<Step> steps;
	private boolean verbose = true;
	private Logger logger = new Logger();

	public BaseProxyHandler(ScriptProcessor processor, Scope scope) {
		this.processor = processor;
		this.scope = scope;
		steps = new ArrayList<Step>();
	}

	/** True for verbose output. */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/** Play a single command (including scripts). */
	protected PlaybackResult playCommand(Command cmd) {
		if (verbose) {
			logger.print(cmd);
		}

		scope.setCurrentCommand(cmd);

		Step step = new Step(cmd, scope, 0);
		steps.add(step);
		PlaybackResult result = processor.runScript(cmd, scope);
		step.setResult(result);

		if (verbose) {
			logger.println(" -> " + result);
		}
		return result;
	}

	/** Play a command (from a raw string). */
	protected String play(String cmd) {
		return play(new Command(cmd));
	}

	/** Play a command (from it's parts) */
	protected String play(String componentType, String monkeyId, String action, List<String> args,
			Map<String, String> mods) {
		return play(new Command(componentType, monkeyId, action, args, mods));
	}

	/**
	 * Play a command. In the case of Get or ExecAndReturn, return the String result, otherwise
	 * return {@code null}. Throws {@link MonkeyTalkError} for errors or {@link MonkeyTalkFailure}
	 * for failures.
	 * 
	 * @param cmd
	 *            the MonkeyTalk command to play
	 * @return the value (for Get or ExecAndReturn), otherwise returns {@code null}.
	 */
	protected String play(Command cmd) {
		PlaybackResult result = null;
		try {
			result = playCommand(cmd);
		} catch (Exception ex) {
			throw new MonkeyTalkError("unknown error", ex);
		}

		// throw exceptions for error or failures
		if (result == null) {
			throw new MonkeyTalkError("bad result");
		} else if (PlaybackStatus.ERROR == result.getStatus()) {
			throw new MonkeyTalkError(result.getMessage());
		} else if (PlaybackStatus.FAILURE == result.getStatus()) {
			throw new MonkeyTalkFailure(result.getMessage());
		}

		// for Get, return the result message
		if (isGet(cmd.getAction())) {
			return result.getMessage();
		}
		return null;
	}

	/**
	 * True if action is Get or ExecAndReturn (aka actions that need to have return values),
	 * otherwise false.
	 */
	protected boolean isGet(String action) {
		return ("get".equalsIgnoreCase(action) || "execAndReturn".equalsIgnoreCase(action));
	}
}
