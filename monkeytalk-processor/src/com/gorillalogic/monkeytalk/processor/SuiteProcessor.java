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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.finder.Finder;
import com.gorillalogic.monkeytalk.processor.report.Report;

/**
 * Run a suite and output an JUnit-compatible XML report.
 */
public class SuiteProcessor extends BaseProcessor {
	private final static String BAD_SUITE_COMMAND = "only Test, Setup, Teardown, and Suite are allowed";

	private ScriptProcessor processor;
	private SuiteListener listener;
	private File reportDir;
	private File reportFile;
	private boolean abortByRequest = false;

	/**
	 * Default suite listener -- all callbacks do nothing.
	 */
	private static final SuiteListener DEFAULT_LISTENER = new SuiteListener() {

		@Override
		public void onRunStart(int total) {
		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {
		}

		@Override
		public void onTestStart(String name, int num, int total) {
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
		}

		@Override
		public void onSuiteStart(int total) {
		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
		}
	};

	/**
	 * Default playback result.
	 */
	private static final PlaybackResult PLAYBACK_OK = new PlaybackResult();

	/**
	 * Instantiate a suite processor with the given host, port, and project root folder.
	 * 
	 * @param host
	 *            the target host
	 * @param port
	 *            the target port
	 * @param rootDir
	 *            the project root directory
	 */
	public SuiteProcessor(String host, int port, File rootDir) {
		this(rootDir, AgentManager.getDefaultAgent(host, port));
	}

	/**
	 * Instantiate a suite processor with the given project root folder and agent.
	 * 
	 * @param rootDir
	 *            the project root directory
	 * @param agent
	 *            the agent
	 */
	public SuiteProcessor(File rootDir, IAgent agent) {
		this(rootDir, agent, null);
	}

	/**
	 * Instantiate a suite processor with the given project root folder, agent, and ScriptProcessor.
	 * 
	 * @param rootDir
	 *            the project root directory
	 * @param agent
	 *            the agent Wparam processor the {@code ScriptProcessor} to use. If null, a newly
	 *            created ScriptProcessor will be used.
	 */
	public SuiteProcessor(File rootDir, IAgent agent, ScriptProcessor processor) {
		super(rootDir, agent);
		this.processor = (processor == null ? new ScriptProcessor(rootDir, agent) : processor);
	}

	/**
	 * Get the suite listener callbacks. If not set, return the default suite listener and never
	 * return {@code null}.
	 * 
	 * @see ScriptProcessor#DEFAULT_LISTENER
	 * 
	 * @return the playback listener
	 */
	public SuiteListener getSuiteListener() {
		if (listener == null) {
			listener = DEFAULT_LISTENER;
		}
		return listener;
	}

	/**
	 * Get the script processor used by this suite processor.
	 * 
	 * @return the script processor
	 * 
	 */
	public ScriptProcessor getScriptProcessor() {
		return processor;
	}

	/**
	 * Set the suite listener.
	 * 
	 * @param listener
	 *            the suite listener
	 */
	public void setSuiteListener(SuiteListener listener) {
		this.listener = listener;
	}

	/**
	 * Set the test report output directory (aka the directory where the XML report will be saved).
	 * 
	 * @param reportDir
	 *            the report directory
	 */
	public void setReportDir(File reportDir) {
		this.reportDir = reportDir;
	}

	/**
	 * Get the test report file.
	 * 
	 * @return the test report file
	 */
	public File getReportFile() {
		return reportFile;
	}

	/**
	 * Run the given suite and output a JUnit-compatible XML report named
	 * <code>TEST-&lt;filename>.xml</code>. The XML report is saved to the {@code reportDir} if it
	 * exists, otherwise it is saved to the same directory as the suite.
	 * 
	 * @see Report
	 * 
	 * @param filename
	 *            the script (or suite) filename
	 * @return the playback result (OK, ERROR, or FAILURE)
	 */
	public PlaybackResult runSuite(String filename) {
		long startTime = System.currentTimeMillis();
		int total = new SuiteFlattener(this.getWorld()).flatten(filename);
		getSuiteListener().onRunStart(total);
		Report report = new Report(filename);

		PlaybackResult result = null;
		result = runSuite(filename, report, result);

		if (result.getStatus() == PlaybackStatus.ERROR) {
			getSuiteListener().onRunComplete(result, report);
			return result;
		}

		try {
			report.saveReport((reportDir != null ? reportDir : world.getRootDir()));
			reportFile = report.getReportFile();
		} catch (IOException ex) {
			result = errorResult("failed to save XML report - " + ex.getMessage(), new Scope(
					filename), startTime);
			getSuiteListener().onRunComplete(result, report);
			return result;
		}

		getSuiteListener().onRunComplete(PLAYBACK_OK, report);
		return result;
	}

	protected PlaybackResult runSuite(String filename, Report report, PlaybackResult result) {
		long startTime = System.currentTimeMillis();
		getAgent().start();

		reportFile = null;

		if (filename == null) {
			return errorResult("suite filename is null", startTime);
		}

		List<Command> commands = world.getSuite(filename);

		Scope scope = new Scope(filename);

		if (commands == null) {
			if (filename.toLowerCase().endsWith(CommandWorld.SCRIPT_EXT)) {
				return errorResult("running script '" + filename + "' as a suite is not allowed",
						scope, startTime);
			}
			return errorResult("suite '" + filename + "' not found", scope, startTime);
		} else if (commands.size() == 0) {
			getSuiteListener().onSuiteStart(0);
			result = errorResult("suite '" + filename + "' is empty", scope, startTime);
			getSuiteListener().onSuiteComplete(result, report);
			return result;
		}

		List<Command> setupArray = Finder.findCommandsByComponentType(commands, "setup");
		List<Command> teardownArray = Finder.findCommandsByComponentType(commands, "teardown");

		int total = new SuiteFlattener(this.getWorld()).flatten(filename);
		getSuiteListener().onSuiteStart(total);

		List<Step> steps = new ArrayList<Step>();

		int stepNumber = 1;
		scope.setCurrentIndex(0);
		for (Command cmd : commands) {
			Command full = scope.substituteCommand(cmd);

			Step step = new Step(full, scope, scope.getCurrentIndex());
			steps.add(step);
			result = runSuiteCommand(full, scope, report, stepNumber++, total, setupArray,
					teardownArray);
			step.setResult(result);

			if (shouldAbort(result)) {
				break;
			}
		}

		getSuiteListener().onSuiteComplete(result, report);

		PlaybackResult suiteResult = new PlaybackResult(PlaybackStatus.OK);
		if (result != null && shouldAbort(result)) {
			suiteResult = copyResult(result, scope, startTime);
		}
		suiteResult.setStartTime(startTime);
		suiteResult.setStopTime(System.currentTimeMillis());
		suiteResult.setScope(scope);
		suiteResult.setSteps(steps);
		return suiteResult;
	}

	private boolean shouldAbort(PlaybackResult result) {
		if (result == null || result.getStatus() == null) {
			return false;
		}
		if (abortByRequest) {
			// user abort, so halt the suite!
			result.setStatus(PlaybackStatus.ERROR);
			result.setMessage(ABORT_BY_REQUEST);
			return true;
		}
		if (result.getStatus() == PlaybackStatus.ERROR) {
			if (result.getMessage() != null) {
				if (result.getMessage().contains(BAD_SUITE_COMMAND)) {
					// if we error with a bad command, then halt the suite!
					return true;
				}
			}
		}
		return false;
	}

	protected PlaybackResult runSuiteCommand(Command full, Scope scope, Report report,
			int stepNumber, int total, List<Command> setupArray, List<Command> teardownArray) {

		// capture start time
		long startTime = System.currentTimeMillis();

		if (scope != null) {
			scope.setCurrentCommand(full);
		}

		PlaybackResult result = null;

		if ("test".equalsIgnoreCase(full.getComponentType()) && full.isIgnored()) {
			report.startTest(full);
			getSuiteListener().onTestStart(report.getCurrentTest().getName(), stepNumber, total);

			result = new PlaybackResult(PlaybackStatus.OK, "ignored", scope);

			report.stopTest(full, result);
			getSuiteListener().onTestComplete(result, report);
		} else if ("test.run".equalsIgnoreCase(full.getCommandName())) {
			report.startTest(full);
			getSuiteListener().onTestStart(report.getCurrentTest().getName(), stepNumber, total);

			result = runTest(full, stepNumber, setupArray, teardownArray, scope, null);

			report.stopTest(full, result);
			getSuiteListener().onTestComplete(result, report);
		} else if ("test.runwith".equalsIgnoreCase(full.getCommandName())) {
			if (full.getArgs().size() == 0) {
				report.startTest(full);
				getSuiteListener()
						.onTestStart(report.getCurrentTest().getName(), stepNumber, total);

				result = new PlaybackResult(PlaybackStatus.ERROR,
						"datafile arg missing in command '" + full + "'", scope);

				report.stopTest(full, result);
				getSuiteListener().onTestComplete(result, report);
			} else {
				String datafile = full.getArgs().get(0);
				List<Map<String, String>> data = world.getData(datafile);
				if (data == null) {
					report.startTest(full);
					getSuiteListener().onTestStart(report.getCurrentTest().getName(), stepNumber,
							total);

					result = new PlaybackResult(PlaybackStatus.ERROR, "datafile '" + datafile
							+ "' not found", scope);

					report.stopTest(full, result);
					getSuiteListener().onTestComplete(result, report);
				} else if (data.size() == 0) {
					report.startTest(full);
					getSuiteListener().onTestStart(report.getCurrentTest().getName(), stepNumber,
							total);

					result = new PlaybackResult(PlaybackStatus.ERROR, "datafile '" + datafile
							+ "' has no data", scope);

					report.stopTest(full, result);
					getSuiteListener().onTestComplete(result, report);
				} else {
					result = new PlaybackResult(PlaybackStatus.OK);
					result.setScope(scope);
					List<Step> steps = new ArrayList<Step>();
					result.setSteps(steps);
					int dataIndex = 1;
					for (Map<String, String> datum : data) {
						report.startTest(full, datum);
						getSuiteListener().onTestStart(report.getCurrentTest().getName(),
								dataIndex, total);
						Command stepCommand = new Command(full.getCommand().replaceAll(datafile,
								datafile + "\\[\\@" + dataIndex + "\\]"));
						Step step = new Step(stepCommand, scope, dataIndex);
						steps.add(step);
						PlaybackResult r = runTest(full, dataIndex, setupArray, teardownArray,
								scope, datum);
						step.setResult(r);

						report.stopTest(full, r);
						getSuiteListener().onTestComplete(r, report);
						dataIndex++;

						if (shouldAbort(r)) {
							break;
						}
					}
				}
			}
		} else if ("suite".equalsIgnoreCase(full.getComponentType())
				&& full.getModifiers().containsKey(Command.IGNORE_MODIFIER)) {
			// entire sub-suite is ignored
		} else if ("suite.run".equalsIgnoreCase(full.getCommandName())) {
			Report recurseReport = new Report(full.getMonkeyId());
			result = this.runSuite(full.getMonkeyId(), recurseReport, null);
			report.getMainSuite().addSuite(recurseReport.getMainSuite());
		} else if ("suite.runwith".equalsIgnoreCase(full.getCommandName())) {
			result = errorResult("command '" + full.getCommandName()
					+ "' is illegal -- only suite.run is allowed", scope, startTime);
			return result;
		} else if (full.isComment()) {
			// ignore comments
		} else if ("setup".equalsIgnoreCase(full.getComponentType())) {
			// ignore setup
		} else if ("teardown".equalsIgnoreCase(full.getComponentType())) {
			// ignore teardown
		} else {
			result = errorResult("command '" + full.getCommandName() + "' is illegal -- "
					+ BAD_SUITE_COMMAND, scope, startTime);
			return result;
		}

		if (result != null) {
			result.setStartTime(startTime);
			result.setStopTime(System.currentTimeMillis());
		}
		return result;
	}

	protected PlaybackResult errorResult(String msg) {
		return errorResult(msg, null, -1);
	}

	protected PlaybackResult errorResult(String msg, long startTime) {
		return errorResult(msg, null, startTime);
	}

	protected PlaybackResult errorResult(String msg, Scope scope) {
		return errorResult(msg, scope, -1);
	}

	protected PlaybackResult errorResult(String msg, Scope scope, long startTime) {
		if (startTime == -1) {
			startTime = System.currentTimeMillis();
		}
		PlaybackResult result = new PlaybackResult(PlaybackStatus.ERROR, msg, scope);
		result.setScope(scope);
		result.setStartTime(startTime);
		result.setStopTime(System.currentTimeMillis());
		return result;
	}

	/*
	 * Run the given test command, deferring to the {@link ScriptProcessor} to do the actual work.
	 * Also, run the {@code Setup} command before and the {@code Teardown} command after.
	 * 
	 * @param cmd the test command
	 * 
	 * @param setupArray the setup commands
	 * 
	 * @param teardownArray the teardown commands
	 * 
	 * @param scope the scope
	 * 
	 * @param datum the named variables (if the script is being data-driven)
	 * 
	 * @return
	 */
	protected PlaybackResult runTest(Command cmd, int stepNumber, List<Command> setupArray,
			List<Command> teardownArray, Scope scope, Map<String, String> datum) {
		PlaybackResult setupResult = null;
		PlaybackResult teardownResult = null;
		PlaybackResult testResult = null;
		long runTestStartTime = System.currentTimeMillis();

		List<Step> setupSteps = new ArrayList<Step>();
		if (setupArray != null && !cmd.isIgnored("setup")) {
			setupResult = runSetupOrTeardown(setupArray, scope, setupSteps);
		}

		// abort after setup?
		if (setupResult != null && setupResult.getStatus() != PlaybackStatus.OK) {
			PlaybackResult runTestResult = copyResult(setupResult, scope, runTestStartTime);
			runTestResult.setSteps(setupSteps);
			runTestResult.setStopTime(System.currentTimeMillis());
			return runTestResult;
		}

		testResult = processor.runScript(cmd.getMonkeyId(), new Scope(cmd, scope, datum));

		List<Step> teardownSteps = new ArrayList<Step>();
		if (teardownArray != null && !cmd.isIgnored("teardown")) {
			teardownResult = runSetupOrTeardown(teardownArray, scope, teardownSteps);
		}

		if (testResult != null) {
			// add in steps from Setup and Teardown, if they occurred
			if (setupResult != null || teardownResult != null) {
				List<Step> testSteps = new ArrayList<Step>();
				for (Step setupStep : setupSteps) {
					testSteps.add(setupStep);
				}
				if (testResult.getSteps() != null) {
					for (Step testStep : testResult.getSteps()) {
						testSteps.add(testStep);
					}
				}
				for (Step teardownStep : teardownSteps) {
					testSteps.add(teardownStep);
				}
				if (teardownResult != null && !teardownResult.getStatus().equals(PlaybackStatus.OK)
						&& testResult.getStatus().equals(PlaybackStatus.OK)) {
					// error in testdown but no error in
					testResult = copyResult(teardownResult, scope, runTestStartTime);
				}
				testResult.setSteps(testSteps);
			}
		} else {
			// no test result - should not happen
			testResult = errorResult("No valid result returned for command: " + cmd.getCommand());
			testResult.setScope(scope);
		}
		testResult.setStartTime(runTestStartTime);
		testResult.setStopTime(System.currentTimeMillis());
		return testResult;

	}

	protected PlaybackResult runSetupOrTeardown(List<Command> commands, Scope scope,
			List<Step> stepsForOverallTestResult) {
		if (commands == null || commands.size() == 0) {
			return null;
		}
		PlaybackResult result = new PlaybackResult(PlaybackStatus.OK);
		for (Command cmd : commands) { // while we have another setup-or-teardown command, run it
			long startTime = System.currentTimeMillis();
			Scope fixtureScope = new Scope(cmd, scope);
			Step fixtureStep = new Step(cmd, fixtureScope, scope.getCurrentIndex());
			stepsForOverallTestResult.add(fixtureStep);
			if ("run".equalsIgnoreCase(cmd.getAction())) {
				result = processor.runScript(cmd.getMonkeyId(), fixtureScope);
				fixtureStep.setResult(result);
			} else if ("runwith".equalsIgnoreCase(cmd.getAction())) {
				if (cmd.getArgs().size() == 0) {
					result = errorResult("datafile arg missing in command '" + cmd + "'",
							fixtureScope, startTime);
				} else {
					String datafile = cmd.getArgs().get(0);
					List<Map<String, String>> data = world.getData(datafile);

					if (data == null) {
						result = errorResult("datafile '" + datafile + "' not found", fixtureScope,
								startTime);
					} else if (data.size() == 0) {
						result = errorResult("datafile '" + datafile + "' has no data",
								fixtureScope, startTime);
					} else {
						result = new PlaybackResult(PlaybackStatus.OK);
						result.setStartTime(System.currentTimeMillis());
						result.setScope(fixtureScope);
						List<Step> runWithSteps = new ArrayList<Step>();
						result.setSteps(runWithSteps);
						int dataIndex = 1;
						for (Map<String, String> dataFixture : data) {
							Command stepCommand = new Command(cmd.getCommand().replaceAll(datafile,
									datafile + "\\[\\@" + dataIndex + "\\]"));
							Step runWithStep = new Step(stepCommand, fixtureScope, dataIndex++);
							runWithSteps.add(runWithStep);
							PlaybackResult r = processor.runScript(cmd.getMonkeyId(), new Scope(
									cmd, scope, dataFixture));
							runWithStep.setResult(r);

							if (r.getStatus() != PlaybackStatus.OK) {
								// error during data-driven setup, so abort
								result.setStatus(r.getStatus());
								result.setMessage(r.getMessage());
								break;
							}
						}
						result.setStopTime(System.currentTimeMillis());
					}
				}
				fixtureStep.setResult(result);
			} else {
				result = errorResult("command '" + cmd.getCommandName()
						+ "' is illegal -- only Teardown.Run and Teardown.RunWith are allowed",
						fixtureScope, startTime);
				fixtureStep.setResult(result);
			}
		}
		return result;
	}

	@Override
	public void setGlobalTimeout(int timeout) {
		processor.setGlobalTimeout(timeout);
		super.setGlobalTimeout(timeout);
	}

	@Override
	public void setGlobalThinktime(int thinktime) {
		processor.setGlobalThinktime(thinktime);
		super.setGlobalThinktime(thinktime);
	}

	@Override
	public void setTakeAfterMetrics(boolean takeAfterMetrics) {
		processor.setTakeAfterMetrics(takeAfterMetrics);
		super.setTakeAfterMetrics(takeAfterMetrics);
	}

	@Override
	public void setTakeAfterScreenshot(boolean takeAfterScreenshot) {
		processor.setTakeAfterScreenshot(takeAfterScreenshot);
		super.setTakeAfterScreenshot(takeAfterScreenshot);
	}

	/**
	 * Stop the running suite as soon as possible by setting the {@code abortByRequest} flag to halt
	 * suite execution. Also call {@link ScriptProcessor#abort()} to halt the underlying test
	 * execution.
	 */
	public void abort() {
		abortByRequest = true;

		// and also abort the running processor
		if (processor != null) {
			processor.abort();
		}
	}

	@Override
	public String toString() {
		return "SuiteProcessor:\n" + super.toString();
	}
}