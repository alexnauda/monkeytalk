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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.processor.command.Debug;
import com.gorillalogic.monkeytalk.processor.command.Globals;
import com.gorillalogic.monkeytalk.processor.command.Sys;
import com.gorillalogic.monkeytalk.processor.command.Vars;
import com.gorillalogic.monkeytalk.processor.command.VerifyImage;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * Class for running scripts and returning a result. Provides a callback interface via
 * {@link PlaybackListener} to monitor a running script.
 */
public class ScriptProcessor extends BaseProcessor {
	private static final String SCREENSHOTS_DIR = "screenshots";
	private static final Command SCREENSHOT_COMMAND = new Command("Device * Screenshot");
	private static final Command METRICS_COMMAND = new Command("Device * Get dummy allinfo");
	private static final SimpleDateFormat screenshotFmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

	private PlaybackListener listener;
	private JSProcessor jsprocessor;
	private boolean abortOnError = true;
	private boolean abortOnFailure = true;
	private boolean abortByRequest = false;
	private boolean saveScreenshots = true;

	private boolean firstCommand = false;
	private ArrayList<String> screenshots;

	// used to save the very first before screenshot (when only taking after screenshots)
	private File beforeScreenshot = null;

	/**
	 * Default playback listener -- all callbacks do nothing.
	 */
	private static final PlaybackListener DEFAULT_LISTENER = new PlaybackListener() {
		@Override
		public void onStart(Scope scope) {
		}

		@Override
		public void onScriptStart(Scope scope) {
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult result) {
		}

		@Override
		public void onComplete(Scope scope, Response response) {
		}

		@Override
		public void onPrint(String message) {
		}
	};

	/**
	 * Default playback result.
	 */
	private static final PlaybackResult PLAYBACK_OK = new PlaybackResult();

	/**
	 * Suite commands - which are illegal in a script.
	 */
	private static final Set<String> SUITE_COMPONENTS = new HashSet<String>(Arrays.asList("test",
			"setup", "teardown", "suite"));

	/**
	 * Instantiate a script processor with the given host, port, and project root directory.
	 * 
	 * @param host
	 *            the target host
	 * @param port
	 *            the target port
	 * @param rootDir
	 *            the project root directory
	 */
	public ScriptProcessor(String host, int port, File rootDir) {
		this(rootDir, AgentManager.getDefaultAgent(host, port));
	}

	/**
	 * Instantiate a script processor with the given projectDir and agent.
	 * 
	 * @param rootDir
	 *            the project location
	 * @param agent
	 *            the agent to use for sending commands
	 */
	public ScriptProcessor(File rootDir, IAgent agent) {
		super(rootDir, agent);
	}

	/**
	 * Get the playback listener callbacks. If not set, return the default playback listener. This
	 * is never {@code null}.
	 * 
	 * @see ScriptProcessor#DEFAULT_LISTENER
	 * 
	 * @return the playback listener
	 */
	public PlaybackListener getPlaybackListener() {
		if (listener == null) {
			listener = DEFAULT_LISTENER;
		}
		return listener;
	}

	/**
	 * Set the playback listener.
	 * 
	 * @param listener
	 *            the playback listener
	 */
	public void setPlaybackListener(PlaybackListener listener) {
		this.listener = listener;
	}

	private JSProcessor getJSProcessor() {
		if (jsprocessor == null) {
			jsprocessor = new JSProcessor(this);
		}
		return jsprocessor;
	}

	/**
	 * Play the given script or suite with a new top-level scope.
	 * 
	 * @param filename
	 *            the script (or suite) filename
	 * @return the playback result (OK, ERROR, or FAILURE)
	 */
	public PlaybackResult runScript(String filename) {
		return runScript(filename, new Scope(filename));
	}

	/**
	 * Play the given script or suite with the given scope.
	 * 
	 * @param filename
	 *            the script (or suite) filename
	 * @param scope
	 *            the scope
	 * @return the playback result (OK, ERROR, or FAILURE)
	 */
	public PlaybackResult runScript(String filename, Scope scope) {
		if (filename == null) {
			return new PlaybackResult(PlaybackStatus.ERROR, "script filename is null", scope);
		}

		if (scope == null) {
			scope = new Scope(filename);
		}

		if (world.hasJavascriptOverride(filename)) {
			String jsFilename = filename
					+ (filename.toLowerCase().endsWith(CommandWorld.JS_EXT) ? ""
							: CommandWorld.JS_EXT);

			Command cmd;
			if (scope.getVariables().size() > 0) {
				cmd = new Command("Script", jsFilename, "Run", new ArrayList<String>(scope
						.getVariables().values()), null);
			} else {
				cmd = new Command("Script", jsFilename, "Run", scope.getArgs(), null);
			}

			return getJSProcessor().runJavascript(cmd, scope);
		} else {
			List<Command> commands = world.getScript(filename);

			if (commands == null) {
				if (filename.toLowerCase().endsWith(CommandWorld.SUITE_EXT)) {
					return new PlaybackResult(PlaybackStatus.ERROR, "running suite '" + filename
							+ "' as a script is not allowed", scope);
				}
				return new PlaybackResult(PlaybackStatus.ERROR, "script '" + filename
						+ "' not found", scope);
			} else if (commands.size() == 0) {
				return new PlaybackResult(PlaybackStatus.ERROR, "script '" + filename
						+ "' is empty", scope);
			}

			return runScript(commands, scope);
		}
	}

	/**
	 * Play the given list of commands with the given scope and return the result. Simply loop over
	 * the list of commands and play them one at a time with
	 * {@link ScriptProcessor#runScript(Command, Scope)}.
	 * 
	 * @param commands
	 *            the list of commands
	 * @param scope
	 *            the scope
	 * @return the playback result (OK, ERROR, or FAILURE)
	 */
	public PlaybackResult runScript(List<Command> commands, Scope scope) {
		return runScript(commands, scope, null);
	}

	/**
	 * Play the given list of commands with the given scope and return the result. Simply loop over
	 * the list of commands and play them one at a time with
	 * {@link ScriptProcessor#runScript(Command, Scope)}.
	 * 
	 * @param commands
	 *            the list of commands
	 * @param scope
	 *            the scope
	 * @return the playback result (OK, ERROR, or FAILURE)
	 */
	protected PlaybackResult runScript(List<Command> commands, Scope scope, List<Step> steps) {
		long startTime = System.currentTimeMillis();

		agent.start();

		PlaybackResult result = null;

		if (scope == null) {
			scope = new Scope();
		}

		if (commands == null) {
			result = new PlaybackResult(PlaybackStatus.ERROR, "command list is null", scope);
			result.setStartTime(startTime);
			result.setStopTime(System.currentTimeMillis());
			return result;
		} else if (commands.size() == 0) {
			getPlaybackListener().onScriptStart(scope);
			result = new PlaybackResult(PlaybackStatus.OK, "empty command list", scope);
			result.setStartTime(startTime);
			result.setStopTime(System.currentTimeMillis());
			getPlaybackListener().onScriptComplete(scope, result);
			return result;
		}

		getPlaybackListener().onScriptStart(scope);
		if (steps == null) {
			steps = new ArrayList<Step>();
		}

		scope.setCurrentIndex(0);
		for (Command cmd : commands) {
			Command full = scope.substituteCommand(cmd);
			Step step = new Step(full, scope, scope.getCurrentIndex());
			steps.add(step);
			firstCommand = cmd.equals(commands.get(0)) ? true : false;
			result = runScript(full, scope);
			step.setResult(result);

			if (shouldAbort(result)) {

				// Save image filename to link to in report
				for (String image : getScreenshots()) {
					result.addImage(image);
				}

				break;
			}
		}

		// Report report = new Report(screenshotFmt.format(new Date()));
		Report report = new Report("last_script_run");
		File dir = new File(world.getRootDir(), SCREENSHOTS_DIR);

		try {
			report.saveScreenshotsToHTML(getScreenshots(), dir);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		PlaybackResult resultWithImages = new PlaybackResult(PlaybackStatus.OK);
		if (result != null) {
			getPlaybackListener().onScriptComplete(scope, result);
			resultWithImages.setStatus(result.getStatus());
			if (!result.getStatus().equals(PlaybackStatus.OK)) {
				resultWithImages = copyResult(result, scope, startTime);
			}
		} else {
			getPlaybackListener().onScriptComplete(scope, PLAYBACK_OK);
		}
		resultWithImages.setSteps(steps);
		resultWithImages.setStartTime(startTime);
		resultWithImages.setStopTime(System.currentTimeMillis());
		resultWithImages.setScope(scope);
		return resultWithImages;
		// return PLAYBACK_OK;
	}

	/**
	 * Play the given command with the given scope and return the result.
	 * 
	 * @param cmd
	 *            the command to be played
	 * @param scope
	 *            the scope
	 * @return the playback result (OK, ERROR, or FAILURE)
	 */
	public PlaybackResult runScript(Command cmd, Scope scope) {

		// capture start time
		long startTime = System.currentTimeMillis();

		// update timings
		cmd.setDefaultTimeout(getGlobalTimeout());
		cmd.setDefaultThinktime(getGlobalThinktime());

		// save curr cmd into scope
		if (scope == null) {
			scope = new Scope();
			scope.setCurrentIndex(0);
		}
		scope.setCurrentCommand(cmd);

		// init result
		PlaybackResult result = null;

		if (cmd.isIgnored()) {
			getPlaybackListener().onStart(scope);
			result = new PlaybackResult(PlaybackStatus.OK, "ignored");
			getPlaybackListener().onComplete(scope, new Response.Builder("ignored").build());
		} else if ("script.run".equals(cmd.getCommandName())) {
			getPlaybackListener().onStart(scope);
			result = runScript(cmd.getMonkeyId(), new Scope(cmd, scope));
			getPlaybackListener().onComplete(scope, new Response());
		} else if ("script.runif".equals(cmd.getCommandName())) {
			if (cmd.getArgs().size() == 0) {
				getPlaybackListener().onStart(scope);
				Response resp = new Response.Builder()
						.error()
						.message(
								"command '" + cmd.getCommand()
										+ "' must have a valid verify command as its arguments")
						.build();
				getPlaybackListener().onComplete(scope, resp);
				result = new PlaybackResult(resp, scope);
			} else {
				Command verify = new Command(cmd.getArgsAsString() + " "
						+ cmd.getModifiersAsString());

				if (verify.getAction() == null
						|| !verify.getAction().toLowerCase().startsWith("verify")) {
					String msg = "command '" + cmd.getCommand() + "' has invalid verify command '"
							+ verify.getCommand() + "'";
					getPlaybackListener().onStart(scope);
					Response resp = new Response.Builder().error().message(msg).build();
					getPlaybackListener().onComplete(scope, resp);
					result = new PlaybackResult(resp, scope);
				} else {
					Response verifyResp = runCommand(verify);
					PlaybackResult verifyResult = new PlaybackResult(verifyResp, scope);

					if (verifyResult.getStatus().equals(PlaybackStatus.OK)) {
						String msg = "running " + cmd.getMonkeyId() + "...";
						Response resp = new Response.Builder().ok().message(msg).build();
						getPlaybackListener().onStart(scope);
						getPlaybackListener().onComplete(scope, resp);

						cmd.setArgsAndModifiers("");
						cmd.setAction("Run");
						result = runScript(cmd, scope);
					} else if (verifyResult.getStatus().equals(PlaybackStatus.FAILURE)) {
						String msg = "not running " + cmd.getMonkeyId() + " - "
								+ verifyResp.getMessage();
						Response resp = new Response.Builder().ok().message(msg).build();
						getPlaybackListener().onStart(scope);
						getPlaybackListener().onComplete(scope, resp);
						result = new PlaybackResult(PlaybackStatus.OK, msg, scope);
					} else {
						String msg = "verify error - " + verifyResp.getMessage();
						Response resp = new Response.Builder().error().message(msg).build();
						getPlaybackListener().onStart(scope);
						getPlaybackListener().onComplete(scope, resp);
						result = new PlaybackResult(PlaybackStatus.ERROR, msg, scope);
					}
				}
			}
		} else if ("script.runwith".equals(cmd.getCommandName())) {
			if (cmd.getArgs().size() == 0) {
				result = new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
						+ "' must have a datafile as its first arg", scope);
			} else {
				String datafile = cmd.getArgs().get(0);
				List<Map<String, String>> data = world.getData(datafile);
				if (data == null) {
					result = new PlaybackResult(PlaybackStatus.ERROR, "datafile '" + datafile
							+ "' not found", scope);
				} else if (data.size() == 0) {
					result = new PlaybackResult(PlaybackStatus.ERROR, "datafile '" + datafile
							+ "' has no data", scope);
				} else {
					List<Step> steps = new ArrayList<Step>();
					int stepNumber = 1;
					for (Map<String, String> datum : data) {

						getPlaybackListener().onStart(scope);
						Command stepCommand = new Command(cmd.getCommand().replaceAll(datafile,
								datafile + "\\[\\@" + stepNumber + "\\]"));
						Step step = new Step(stepCommand, scope, stepNumber++);
						steps.add(step);
						PlaybackResult r = runScript(cmd.getMonkeyId(),
								new Scope(cmd, scope, datum));
						step.setResult(r);
						getPlaybackListener().onComplete(scope, new Response());

						if (shouldAbort(r)) {
							String message = r.getMessage();
							if (message == null) {
								message = "";
							} else {
								if (message.length() > 0) {
									message += ": ";
								}
							}
							result = new PlaybackResult(r.getStatus(), message + (stepNumber - 1)
									+ " data records processed", scope);
							break;
						}
					}
					if (result == null) {
						result = new PlaybackResult(PlaybackStatus.OK, (stepNumber - 1)
								+ " data records processed", scope);
					}
					result.setSteps(steps);
				}
			}
		} else if ("globals.define".equals(cmd.getCommandName())
				|| "globals.set".equals(cmd.getCommandName())) {
			result = new Globals(cmd, scope, getPlaybackListener()).define();
		} else if ("vars.define".equals(cmd.getCommandName())) {
			result = new Vars(cmd, scope, getPlaybackListener()).define();
		} else if (cmd.getCommandName().toLowerCase().startsWith("vars.verify")) {
			result = new Vars(cmd, scope, getPlaybackListener()).verify();
		} else if (world.fileExists(getCustomCommandFilename(cmd))) {
			// custom command, so run it
			String filename = getCustomCommandFilename(cmd);

			getPlaybackListener().onStart(scope);
			result = runScript(
					filename,
					new Scope(filename, scope, cmd.getComponentType(), cmd.getMonkeyId(), cmd
							.getAction(), cmd.getArgs(), null));
			getPlaybackListener().onComplete(scope, new Response());
		} else if ("debug.print".equals(cmd.getCommandName())) {
			result = new Debug(cmd, scope, getPlaybackListener()).print();
		} else if ("debug.vars".equals(cmd.getCommandName())) {
			result = new Debug(cmd, scope, getPlaybackListener()).vars();
		} else if ("system.exec".equals(cmd.getCommandName())) {
			result = new Sys(cmd, scope, getPlaybackListener()).exec();
		} else if ("system.execandreturn".equals(cmd.getCommandName())) {
			result = new Sys(cmd, scope, getPlaybackListener()).execAndReturn();
		} else if ("verifyImage".equalsIgnoreCase(cmd.getAction())) {
			result = new VerifyImage(cmd, scope, getPlaybackListener(), this, this.getWorld()
					.getRootDir()).verifyImage();
		} else if (cmd.isComment()) {
			// ignore comments
		} else if (SUITE_COMPONENTS.contains(cmd.getComponentType().toLowerCase())) {
			result = new PlaybackResult(
					PlaybackStatus.ERROR,
					"command '"
							+ cmd.getCommandName()
							+ "' is only allowed in a suite (maybe you need to change the file extension to "
							+ CommandWorld.SUITE_EXT + "?)", scope);
		} else if ("get".equalsIgnoreCase(cmd.getAction())
				|| "execandreturn".equalsIgnoreCase(cmd.getAction())) {
			if (cmd.getArgs().size() == 0) {
				result = new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
						+ "' must have a variable as its first arg", scope);
			} else if (!cmd.getArgs().get(0).matches(Vars.VALID_VARIABLE_PATTERN)) {
				result = new PlaybackResult(
						PlaybackStatus.ERROR,
						"command '"
								+ cmd.getCommand()
								+ "' has illegal variable '"
								+ cmd.getArgs().get(0)
								+ "' as its first arg -- variables must begin with a letter and contain only letters, numbers, and underscores",
						scope);
			} else {
				getPlaybackListener().onStart(scope);

				Response resp = runCommand(cmd);
				String key = cmd.getArgs().get(0);
				String val = resp.getMessage();

				if (scope.getVariables().containsKey(key)) {
					// local variable already in scope, so Get val into the local var
					scope.addVariable(key, val);
				} else if (com.gorillalogic.monkeytalk.processor.Globals.hasGlobal(key)) {
					// global of same name exists, so Get val into the global
					com.gorillalogic.monkeytalk.processor.Globals.setGlobal(key, val);
				} else {
					// no local exists & no global exists, so create a new local var
					scope.addVariable(key, val);
				}

				getPlaybackListener().onComplete(scope, resp);

				result = new PlaybackResult(resp, scope);

				if ("value".equals(cmd.getArgs().get(0))) {
					result.setWarning("command '"
							+ cmd.getCommand()
							+ "' uses variable 'value' -- did you mean to use it as a property instead?");
				}
			}
		} else if (cmd.getAction().toLowerCase().startsWith("waitfor")) {
			String substituteAction = cmd.getAction().toLowerCase()
					.replaceFirst("waitfor", "verify");
			long timeout = 10000; // DEFAULT_WAITFOR_TIMEOUT;
			if (cmd.getArgs().size() > 0 && cmd.getArgs().get(0) != null
					&& cmd.getArgs().get(0).length() > 0) {
				String arg = cmd.getArgs().get(0);
				// first arg is timeout
				int timeInSeconds = 0;
				try {
					timeInSeconds = Integer.parseInt(arg);
				} catch (NumberFormatException e) {
					result = new PlaybackResult(PlaybackStatus.ERROR, "command '"
							+ cmd.getCommand()
							+ "' must have a number of seconds to wait as its first arg, found: "
							+ arg, scope);
				}

				if (result == null) {
					if (timeInSeconds < 1) {
						result = new PlaybackResult(
								PlaybackStatus.ERROR,
								"command '"
										+ cmd.getCommand()
										+ "' must have a number of seconds to wait greater than zero, found: "
										+ arg, scope);
					} else {
						timeout = timeInSeconds * 1000;
					}
				}
			}

			if (result == null) {
				List<String> substituteArgs = new ArrayList<String>(cmd.getArgs());
				if (substituteArgs.size() > 0) {
					substituteArgs.remove(0);
				}
				Command substituteCommand = new Command(cmd.getComponentType(), cmd.getMonkeyId(),
						substituteAction, substituteArgs, cmd.getModifiers());
				substituteCommand.setModifier("timeout", Long.toString(timeout));
				result = playbackVanillaCommand(substituteCommand, scope);
			}

		} else {
			// vanilla command, so just play it
			result = playbackVanillaCommand(cmd, scope);
		}

		// save screenshot in the ran command
		if (saveScreenshots && result != null && result.getImageFile() != null) {
			this.saveResultImage(result);
		}

		// save before/after screenshots
		if (isTakeAfterScreenshot() && (result != null) && (result.getAfterImageFile() != null)) {
			saveAfterScreenshot(result);
		}

		// set timings
		if (result != null) {
			result.setStartTime(startTime);
			result.setStopTime(System.currentTimeMillis());
		} else {
			if (cmd != null && !cmd.isComment()) {
				System.err.println("NULL RESULT FOR COMMAND: " + cmd.getCommand());
			}
		}

		return result;
	}

	protected void saveAfterScreenshot(PlaybackResult result) {
		try {
			String afterFilename = "after_screenshot_" + screenshotFmt.format(new Date()) + ".png";
			File afterScreenshotFile = saveScreenshotImage(result.getAfterImageFile(),
					afterFilename);
			result.setAfterImageFile(afterScreenshotFile);
			if (firstCommand) {
				String beforeFilename = "before_screenshot_" + screenshotFmt.format(new Date())
						+ ".png";
				File beforeScreenshotFile = saveScreenshotImage(result.getBeforeImageFile(),
						beforeFilename);
				result.setBeforeImageFile(beforeScreenshotFile);
				beforeScreenshot = result.getAfterImageFile();
			} else {
				result.setBeforeImageFile(beforeScreenshot);
				beforeScreenshot = result.getAfterImageFile();
			}

		} catch (IOException ex) {
			result.setWarning("failed to save screenshot - " + ex.getMessage());
		}
	}

	protected void saveResultImage(PlaybackResult result) {
		try {
			String filename = "screenshot_" + screenshotFmt.format(new Date()) + ".png";
			File screenshotFile = saveScreenshotImage(result.getImageFile(), filename);
			getScreenshots().add(filename);
			result.setImageFile(screenshotFile);
		} catch (IOException ex) {
			result.setWarning("failed to save screenshot - " + ex.getMessage());
		}
	}

	protected File saveScreenshotImage(File sourceImage, String targetFilename) throws IOException {
		File dir = new File(world.getRootDir(), SCREENSHOTS_DIR);
		FileUtils.makeDir(dir, "failed to create " + dir.getAbsolutePath());

		FileUtils.makeDir(dir, "failed to create " + dir.getAbsolutePath());
		File screenshotFile = new File(dir, targetFilename);
		org.apache.commons.io.FileUtils.copyFile(sourceImage, screenshotFile);
		return screenshotFile;
	}

	protected PlaybackResult playbackVanillaCommand(Command cmd, Scope scope) {
		getPlaybackListener().onStart(scope);
		File before = null;
		File after = null;
		String metrics = null;

		if (isTakeAfterScreenshot() && firstCommand) {
			before = runCommand(SCREENSHOT_COMMAND).getImageFile();
		}

		Response resp = runCommand(cmd);

		if (isTakeAfterMetrics()) {
			metrics = runCommand(METRICS_COMMAND).getMessage();
		}
		if (isTakeAfterScreenshot()) {
			after = runCommand(SCREENSHOT_COMMAND).getImageFile();
		}

		getPlaybackListener().onComplete(scope, resp);
		PlaybackResult result = new PlaybackResult(resp, scope, (firstCommand ? before
				: beforeScreenshot), after, metrics);
		return result;
	}

	/**
	 * Helper to determine if we should abort playback.
	 * 
	 * @param result
	 *            the playback result
	 * @return true if we should abort, otherwise false
	 */
	private boolean shouldAbort(PlaybackResult result) {
		if (result == null) {
			return false;
		}
		if (abortOnFailure && result.getStatus() == PlaybackStatus.FAILURE) {
			return true;
		}
		if (abortOnError && result.getStatus() == PlaybackStatus.ERROR) {
			return true;
		}
		if (abortByRequest) {
			result.setStatus(PlaybackStatus.ERROR);
			result.setMessage(ABORT_BY_REQUEST);
			abortByRequest = false;
			return true;
		}
		return false;
	}

	/**
	 * <p>
	 * Play a single fully-substituted command and return a {@link Response} (aka send to the agent
	 * as JSON via HTTP POST). If the command is a comment (or {@code doc.vars}, {@code doc.script},
	 * etc.) it is not sent over the wire.
	 * </p>
	 * 
	 * <p>
	 * NOTE: it is the {@link CommandSender} that contains the logic of whether or not a given
	 * command should be sent over the wire.
	 * </p>
	 * 
	 * @param command
	 *            the MonkeyTalk command
	 * @return the result (OK, ERROR, or FAILURE)
	 */
	public Response runCommand(Command command) {
		abortOnError = true;
		abortOnFailure = true;

		if (!isGlobalScreenshotOnError()
				&& !command.getModifiers().containsKey(Command.SCREENSHOT_ON_ERROR)) {
			// If global screenshot is OFF & it hasn't been explicitly set on
			// command,
			// then explicitly set it to off on the command (so it'll be sent
			// over the wire)
			command.setScreenshotOnError(false);
		}

		if (command.shouldFail()
				&& !command.getModifiers().containsKey(Command.SCREENSHOT_ON_ERROR)) {
			// if shouldFail is on & screenshotonerror hasn't been explicitly
			// set on command,
			// then turn off screenshots
			command.setScreenshotOnError(false);
		}

		// %abort=error,fail,never on a per-command basis
		if (command.getModifiers().containsKey(Command.ABORT_MODIFIER)) {
			String abortModifierValue = command.getModifiers().get(Command.ABORT_MODIFIER);
			if (abortModifierValue.contains("error")) {
				abortOnError = false;
				abortOnFailure = true;
			}
			if (abortModifierValue.contains("fail")) {
				abortOnError = true;
				abortOnFailure = false;
			}
			if (abortModifierValue.contains("never")) {
				abortOnError = false;
				abortOnFailure = false;
			}
		}

		Response resp = agent.getCommandSender().play(command);
		// check if should fail, and rewrite response if necessary
		if (command.shouldFail()) {
			if (resp.getStatus() == ResponseStatus.OK) {
				// change OK to FAILURE
				resp = new Response(ResponseStatus.FAILURE, "expected failure, but was OK",
						resp.getWarning(), resp.getImage());
			} else if (resp.getStatus() == ResponseStatus.FAILURE) {
				// change FAILURE to OK
				resp = new Response(ResponseStatus.OK, "expected failure : " + resp.getMessage(),
						resp.getWarning(), resp.getImage());
			}
		}

		return resp;
	}

	/**
	 * From the given command, compute the filename as if it is a custom component. Return the
	 * computed filename lowercased.
	 * 
	 * @see Command#getCommandName()
	 * 
	 * @param cmd
	 *            the MonkeyTalk command
	 * @return the custom command filename ({@code componentType.action.mt})
	 */
	private String getCustomCommandFilename(Command cmd) {
		return cmd.getCommandName()
				+ (world.hasJavascriptOverride(cmd.getCommandName()) ? CommandWorld.JS_EXT
						: CommandWorld.SCRIPT_EXT);
	}

	/**
	 * Get the screenshots (as a base64 encoded strings).
	 * 
	 * @return the screenshots
	 */
	protected ArrayList<String> getScreenshots() {
		if (screenshots == null)
			screenshots = new ArrayList<String>();

		return screenshots;
	}

	/**
	 * Stop the running script as soon as possible. Set the {@code abortByRequest} flag to halt
	 * script execution.
	 */
	public void abort() {
		abortByRequest = true;
	}

	@Override
	public String toString() {
		return "ScriptProcessor:\n" + super.toString();
	}
}