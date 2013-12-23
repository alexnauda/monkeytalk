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
package com.gorillalogic.monkeytalk.api.impl;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.gorillalogic.monkeytalk.api.Script;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.sender.Response;

/**
 * Script component where monkeyId is the filename. If no file extension is given, then the script
 * runner will first search for a Javascript override (and run it if found), otherwise the runner
 * will search for a MonkeyTalk script.
 */
public class ScriptImpl extends MTObject implements Script {
	private ScriptProcessor processor;

	/**
	 * Instantiate a Script object with the given agent host, agent port, and the monkeyId. Script
	 * is assumed to be stored in current dir.
	 * 
	 * @param host
	 *            the agent host
	 * @param port
	 *            the agent port
	 * @param monkeyId
	 *            the monkeyId of the script
	 */
	public ScriptImpl(String host, int port, String monkeyId) {
		this(host, port, monkeyId, ".");
	}

	/**
	 * Instantiate a Script object with the given agent host, agent port, and the monkeyId.
	 * 
	 * @param host
	 *            the agent host
	 * @param port
	 *            the agent port
	 * @param monkeyId
	 *            the monkeyId of the script
	 * @param scriptDir
	 *            the directory in which to search for the script, or null for the current
	 *            directory.
	 */
	public ScriptImpl(String host, int port, String monkeyId, String scriptDir) {
		super(new Application(host, port), monkeyId);

		if (scriptDir == null) {
			scriptDir = ".";
		}

		File currDir = new File(scriptDir).getAbsoluteFile();

		processor = new ScriptProcessor(host, port, currDir);
		processor.setPlaybackListener(new PlaybackListener() {
			private String indent = "";

			@Override
			public void onScriptStart(Scope scope) {
				System.out.println();
				indent += "  ";
			}

			@Override
			public void onScriptComplete(Scope scope, PlaybackResult result) {
				indent = indent.substring(2);
			}

			@Override
			public void onStart(Scope scope) {
				System.out.println(indent + scope.getCurrentCommand());
			}

			@Override
			public void onComplete(Scope scope, Response response) {
			}
			
			@Override
			public void onPrint(String message) {
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public String run(String... args) {
		throw new ScriptErrorException("run() not implemented");
	}

	/** {@inheritDoc} */
	@Override
	public void runWith(String... args) {
		throw new ScriptErrorException("runWith() not implemented");
	}

	/** {@inheritDoc} */
	@Override
	public String run(List<String> args, HashMap<String, String> modifiers) {
		return "DEPRECATED!";
	}

	/** {@inheritDoc} */
	@Override
	public String run(String componentType, String action, List<String> args,
			HashMap<String, String> modifiers) {
		return "DEPRECATED!";
	}

	/** {@inheritDoc} */
	@Override
	public void runWith(String datafile, HashMap<String, String> modifiers) {
		// deprecated!
	}

	@Override
	public String toString() {
		return "Script: url=" + getApp().getHost() + ":" + getApp().getPort() + " dir="
				+ processor.getWorld().getRootDir().getAbsolutePath();
	}
}