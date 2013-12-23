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
package com.gorillalogic.monkeytalk.processor.js;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * Use the embedded scripting engine capabilities in Java 6 to run Javascript scripts.
 * Unfortunately, the embedded engine doesn't have all the Rhino Shell stuff, so we must add our own
 * custom {@link MonkeyTalkJS#load(String)} impl at the top level of the Javascript execution scope.
 */
public class MonkeyTalkJS {
	private final ScriptEngine engine;
	private final ScriptProcessor processor;
	private final File rootDir;

	/**
	 * Instantiate a new {@code ScriptEngine} for running Javascript scripts with the given script
	 * processor. Since the embedded scripting engine doesn't have all the Rhino Shell stuff, we
	 * must add our own custom {@link MonkeyTalkJS#load(String)} impl at the top level scope. We
	 * also add the {@link ScriptProcessor} at the top level scope.
	 * 
	 * @param processor
	 *            the script processor
	 * @throws ScriptException
	 */
	public MonkeyTalkJS(ScriptProcessor processor) throws ScriptException {
		this.processor = processor;
		this.rootDir = processor.getWorld().getRootDir();

		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("JavaScript");
		engine.put("MonkeyTalkJS", this);
		engine.put("ProcessorObj", processor);
		engine.put(ScriptEngine.FILENAME, "MonkeyTalkJS.js");
		engine.eval("function load(filename) { MonkeyTalkJS.load(filename); }");
		engine.eval("function print(msg) { MonkeyTalkJS.print(msg); }");
	}

	/**
	 * Our custom {@code load()} impl that is injected into the top level scope of the Javascript
	 * engine.
	 * 
	 * @param filename
	 *            the path of the Javascript file to load and {@code eval()}.
	 * @throws ScriptException
	 */
	public void load(String filename) throws ScriptException {
		File f = new File(rootDir, filename);
		try {
			String js = FileUtils.readFile(f);
			engine.put(ScriptEngine.FILENAME, filename);
			engine.eval(js);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("file not found: " + f.getAbsolutePath() + "\n\t" + ex);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("bad encoding: " + f.getAbsolutePath() + "\n\t" + ex);
		} catch (IOException ex) {
			throw new RuntimeException("file load error: " + f.getAbsolutePath() + "\n\t" + ex);
		} catch (ScriptException ex) {
			throw new RuntimeException("script error: " + f.getAbsolutePath() + "\n\t" + ex);
		}
	}

	/**
	 * Our custom {@code print()} impl that is injected into the top-level scope of the Javascript
	 * engine.
	 * 
	 * @param msg
	 *            the message to print
	 */
	public void print(String msg) {
		processor.getPlaybackListener().onPrint(msg);
	}

	/**
	 * Get the Javascript script engine.
	 * 
	 * @return the script engine
	 */
	public ScriptEngine getEngine() {
		return engine;
	}

	/**
	 * Get the filename of the current script.
	 * 
	 * @return the script filename
	 */
	public String getFilename() {
		return engine.get(ScriptEngine.FILENAME).toString();
	}
}