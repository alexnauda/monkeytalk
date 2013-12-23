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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.processor.report.detail.DetailReportHtml;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.server.ServerConfig;

/**
 * Run the given script or suite against the given target agent.
 */
public class Runner {
	protected String agentName;
	protected IAgent agent;
	protected String host;
	protected int port;
	protected File adb;
	protected String adbSerial;
	protected File reportdir;
	protected PlaybackListener scriptListener;
	protected SuiteListener suiteListener;
	protected boolean verbose;
	protected int thinktime = -1;
	protected int timeout = -1;
	private boolean screenshotOnError = true;
	private boolean takeAfterScreenshot = false;
	private boolean takeAfterMetrics = false;
	private ScriptProcessor scriptProcessor;
	private SuiteProcessor suiteProcessor;

	private static PrintStream OUT;

	static {
		// System.out isn't UTF-8 by default, make it so!
		try {
			OUT = new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			OUT = null;
		}
		System.setOut(OUT);
	}

	private final PlaybackListener defaultScriptListener = new PlaybackListener() {

		@Override
		public void onScriptStart(Scope scope) {
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult result) {
		}

		@Override
		public void onStart(Scope scope) {
			if (verbose && !"debug".equalsIgnoreCase(scope.getCurrentCommand().getComponentType())) {
				onPrint(scope.getCurrentCommand().toString());
				if ("script".equalsIgnoreCase(scope.getCurrentCommand().getComponentType())) {
					onPrint("\n");
				}
			}
		}

		@Override
		public void onComplete(Scope scope, Response response) {
			if (verbose && !"debug".equalsIgnoreCase(scope.getCurrentCommand().getComponentType())) {
				onPrint(" -> "
						+ response.getStatus()
						+ (response.getMessage() != null && response.getMessage().length() > 0 ? " : "
								+ response.getMessage()
								: "") + "\n");
			}
		}

		@Override
		public void onPrint(String message) {
			System.out.print(message);
		}
	};

	private final SuiteListener defaultSuiteListener = new SuiteListener() {

		@Override
		public void onRunStart(int total) {

		}

		@Override
		public void onRunComplete(PlaybackResult result, Report report) {
		}

		@Override
		public void onTestStart(String name, int num, int total) {
			System.out.println(num + " : " + name);
		}

		@Override
		public void onTestComplete(PlaybackResult result, Report report) {
			System.out.println("test result: "
					+ result.getStatus()
					+ (result.getMessage() != null && result.getMessage().length() > 0 ? " : "
							+ result.getMessage() : ""));
		}

		@Override
		public void onSuiteStart(int total) {
			System.out.println("running suite : " + total + (total == 1 ? " test" : " tests"));
		}

		@Override
		public void onSuiteComplete(PlaybackResult result, Report report) {
		}
	};

	public Runner() {
		this(AgentManager.getDefaultAgent());
	}

	public Runner(String agent) {
		this(agent, null, -1);
	}

	public Runner(String agent, int port) {
		this(agent, null, port);
	}

	public Runner(String agentName, String host, int port) {
		this(AgentManager.getAgent(agentName, host, port));
	}

	public Runner(IAgent agent) {
		this.agentName = agent.getName();
		this.host = agent.getHost();
		this.port = agent.getPort();
		verbose = false;
	}

	/**
	 * Run the given script or suite with the given map of global variables.
	 * 
	 * @param in
	 *            the script or suite
	 * @param globals
	 *            the global variables
	 * @return the result
	 */
	public PlaybackResult run(File in, Map<String, String> globals) {
		if (getAgent() == null) {
			throw new RuntimeException("You must specify an agent, allowed values are: "
					+ getAgentNames());
		}

		getAgent().start();

		System.out.println(BuildStamp.STAMP);

		PlaybackResult result = null;
		if (in == null) {
			throw new RuntimeException("Bad input script.");
		} else if (!in.exists()) {
			throw new RuntimeException("Bad input script. File not found: " + in.getAbsolutePath());
		} else if (!in.isFile()) {
			throw new RuntimeException("Bad input script. Not a file: " + in.getAbsolutePath());
		} else {
			try {
				initGlobals(in.getParentFile(), globals);
			} catch (IOException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}

			if (in.getName().toLowerCase().endsWith(CommandWorld.SCRIPT_EXT)
					|| in.getName().toLowerCase().endsWith(CommandWorld.JS_EXT)) {
				result = runScript(in);
			} else if (in.getName().toLowerCase().endsWith(CommandWorld.SUITE_EXT)) {
				result = runSuite(in);
			} else {
				throw new RuntimeException(
						"Unrecognized input script file extension.  Allowed values are: "
								+ CommandWorld.SCRIPT_EXT + ", " + CommandWorld.SUITE_EXT + ", "
								+ CommandWorld.JS_EXT);
			}
		}

		System.out.println("result: " + result);

		File dir = getReportDir();
		if (dir == null) {
			dir = in.getParentFile();
		}
		writeDetailReport(result, new Scope(in.getName()), dir, in.getParentFile());

		return result;
	}

	/**
	 * Helper to init the globals for the current run via a simple three step process. First, we
	 * init the globals map to empty. Second, we load the globals.properties file if it exists.
	 * Last, we add any passed-in globals (typically from the commandline) which will override any
	 * globals from the file.
	 * 
	 * @param dir
	 *            the project dir
	 * @param globals
	 *            the passed in globals
	 */
	private void initGlobals(File dir, Map<String, String> globals) throws IOException {
		Globals.clear();
		if (dir != null && dir.exists() && dir.isDirectory()) {
			Globals.setGlobals(new File(dir, "globals.properties"));
		}
		if (globals != null) {
			Globals.setGlobals(globals);
		}
	}

	protected void writeDetailReport(PlaybackResult result, Scope scope, File reportDir,
			File projectDir) {

		String detailXml = getDetailReportXml(result, scope, projectDir, reportDir);
		FileWriter fw = null;
		File detailReportFile = new File(reportDir,
				ScriptReportHelper.getXMLDetailReportFilename(scope.getFilename()));
		try {
			fw = new FileWriter(detailReportFile);
			fw.write(detailXml);
		} catch (IOException e) {
			System.err.println("error writing detail report to file '" + detailReportFile.getPath()
					+ "': " + e.getMessage());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}

		String detailHtml = getDetailReportHtml(result, detailXml);
		fw = null;
		detailReportFile = new File(reportDir, ScriptReportHelper.getHTMLDetailReportFilename(scope
				.getFilename()));
		try {
			fw = new FileWriter(detailReportFile);
			fw.write(detailHtml);
		} catch (IOException e) {
			System.err.println("error writing detail report to file '" + detailReportFile.getPath()
					+ "': " + e.getMessage());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected String getDetailReportHtml(PlaybackResult result, String detailXml) {
		String report = null;
		try {
			report = new DetailReportHtml().createDetailReportHtml(result, detailXml);
		} catch (Exception ex) {
			report = "<html><head><title>ERROR</title></head>" + "<body><h1>REPORTING ERROR</h1>"
					+ "<p>" + ex.getMessage() + "</p></body></html>";
			ex.printStackTrace();
		}
		return report;
	}

	protected String getDetailReportXml(PlaybackResult result, Scope scope, File projectDir,
			File reportDir) {
		String report = null;
		try {
			report = new ScriptReportHelper().createDetailReport(result, scope, projectDir,
					reportDir, getRunnerVersion(),
					getAgent().getName() + " v" + getAgent().getAgentVersion()).toXMLDocument();
		} catch (Exception ex) {
			ex.printStackTrace();
			report = "<detail><msg><![CDATA[" + "REPORTING ERROR : " + ex.getMessage()
					+ "]]></msg></detail>";
		}
		return report;
	}

	protected String getRunnerVersion() {
		return this.getClass().getSimpleName()
				+ " v"
				+ BuildStamp.VERSION
				+ (BuildStamp.BUILD_NUMBER != null && BuildStamp.BUILD_NUMBER.length() > 0 ? "_"
						+ BuildStamp.BUILD_NUMBER : "") + " - " + BuildStamp.TIMESTAMP;
	}

	protected String getAgentNames() {
		return AgentManager.getAgentNames().toString();
	}

	public void setReportdir(File reportdir) {
		this.reportdir = reportdir;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public String getHost() {
		return (host == null ? ServerConfig.DEFAULT_PLAYBACK_HOST : host.trim());
	}

	public int getPort() {
		return (port < 1 ? ServerConfig.getPlaybackPort(agentName) : port);
	}

	public IAgent getAgent() {
		if (agent == null) {
			agent = AgentManager.getAgent(agentName, host, port);
		}
		return agent;
	}

	public String getAgentName() {
		return getAgent().getName();
	}

	public void setAgentProperty(String key, String val) {
		getAgent().setProperty(key, val);
	}

	public void setAdb(File adb) {
		setAgentProperty(AndroidEmulatorAgent.ADB_PROP, adb == null ? null : adb.getAbsolutePath());
	}

	/**
	 * Set the global timeout.
	 * 
	 * @param timeout
	 *            the timeout
	 */
	public void setGlobalTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Set the global thinktime.
	 * 
	 * @param thinktime
	 *            the thinktime
	 */
	public void setGlobalThinktime(int thinktime) {
		this.thinktime = thinktime;
	}

	/**
	 * Set screenshot on error, true to turn on, false to turn off.
	 * 
	 * @param screenshotOnError
	 *            true to turn on screenshot on error
	 */
	public void setGlobalScreenshotOnError(boolean screenshotOnError) {
		this.screenshotOnError = screenshotOnError;
	}

	/**
	 * Set take after screenshots on every command, true to turn on, false to turn off.
	 * 
	 * @param takeAfterScreenshot
	 *            true to turn on take after screenshot
	 */
	public void setTakeAfterScreenshot(boolean takeAfterScreenshot) {
		this.takeAfterScreenshot = takeAfterScreenshot;
	}

	/**
	 * Set take after metrics on every command, true to turn on, false to turn off.
	 * 
	 * @param takeAfterMetrics
	 *            true to turn on take after metrics
	 */
	public void setTakeAfterMetrics(boolean takeAfterMetrics) {
		this.takeAfterMetrics = takeAfterMetrics;
	}

	protected PlaybackListener getScriptListener() {
		return (scriptListener != null ? scriptListener : defaultScriptListener);
	}

	public void setScriptListener(PlaybackListener scriptListener) {
		this.scriptListener = scriptListener;
	}

	protected SuiteListener getSuiteListener() {
		return (suiteListener != null ? suiteListener : defaultSuiteListener);
	}

	public void setSuiteListener(SuiteListener suiteListener) {
		this.suiteListener = suiteListener;
	}

	/** Get a new script processor with the given project dir */
	private ScriptProcessor getScriptProcessor(File dir) {
		scriptProcessor = new ScriptProcessor(dir, getAgent());
		scriptProcessor.setPlaybackListener(getScriptListener());
		scriptProcessor.setGlobalTimeout(timeout);
		scriptProcessor.setGlobalThinktime(thinktime);
		scriptProcessor.setGlobalScreenshotOnError(screenshotOnError);
		scriptProcessor.setTakeAfterMetrics(takeAfterMetrics);
		scriptProcessor.setTakeAfterScreenshot(takeAfterScreenshot);
		return scriptProcessor;
	}

	/** Run the given script with a new script processor */
	protected PlaybackResult runScript(File script) {
		File dir = script.getAbsoluteFile().getParentFile();
		scriptProcessor = getScriptProcessor(dir);
		return scriptProcessor.runScript(script.getName());
	}

	/** Run the given suite with a new suite processor (and new script processor) */
	protected PlaybackResult runSuite(File suite) {
		File dir = suite.getAbsoluteFile().getParentFile();
		suiteProcessor = new SuiteProcessor(dir, getAgent(), getScriptProcessor(dir));
		suiteProcessor.setSuiteListener(getSuiteListener());
		suiteProcessor.setGlobalTimeout(timeout);
		suiteProcessor.setGlobalThinktime(thinktime);
		suiteProcessor.setGlobalScreenshotOnError(screenshotOnError);

		if (getReportDir() != null) {
			suiteProcessor.setReportDir(getReportDir());
		}

		return suiteProcessor.runSuite(suite.getName());
	}

	protected File getReportDir() {
		if (reportdir != null) {
			if (!reportdir.exists()) {
				boolean success = reportdir.mkdirs();
				if (!success) {
					throw new RuntimeException("Failed to make reportdir: "
							+ reportdir.getAbsolutePath());
				}
			}
			if (!reportdir.isDirectory()) {
				throw new RuntimeException("You must specify a valid reportdir. Not a directory.");
			}
		}
		return reportdir;
	}

	/**
	 * Poll until the agent is up and running in the app under test for the given timeout (in
	 * seconds). Up and running means the agent returns OK in response to a ping message.
	 * 
	 * @param timeout
	 *            the timeout (in seconds)
	 * @return true if the agent is up and running, otherwise false
	 */
	public boolean waitUntilReady(long timeout) {
		return getAgent().waitUntilReady(timeout * 1000);
	}

	/**
	 * Stop the running suite as soon as possible by calling {@link SuiteProcessor#abort()} (or
	 * script by calling {@link ScriptProcessor#abort()}).
	 */
	public void abort() {
		if (suiteProcessor != null) {
			suiteProcessor.abort();
		} else if (scriptProcessor != null) {
			scriptProcessor.abort();
		}
	}
}
