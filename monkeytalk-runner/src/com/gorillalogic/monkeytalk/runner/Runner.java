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
package com.gorillalogic.monkeytalk.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.utils.AndroidUtils;

/**
 * Pure Java commandline runner that handles argument parse and then uses
 * {@link com.gorillalogic.monkeytalk.processor.Runner} to do the actually running.
 */
public class Runner {

	/**
	 * Parse the given commandline args, and run.
	 * 
	 * @param args
	 *            the commandline args
	 */
	public static void main(String[] args) {
		CommandlineParser parser = null;

		try {
			parser = new CommandlineParser(args);
		} catch (Exception ex) {
			System.out.println(BuildStamp.STAMP);
			System.out.println("\nERROR: Bad commandline args\n");
			return;
		}

		if (parser.version) {
			System.out.println(BuildStamp.STAMP);
			return;
		} else if (parser.help) {
			System.out.println(BuildStamp.STAMP);
			parser.printUsage();
			return;
		}

		com.gorillalogic.monkeytalk.processor.Runner runner = null;
		try {
			runner = new com.gorillalogic.monkeytalk.processor.Runner(parser.agent, parser.host,
					parser.port);
		} catch (RuntimeException ex) {
			System.out.println(BuildStamp.STAMP);
			System.out.println("\nERROR: " + ex.getMessage() + "\n");
			parser.printUsage();
		}

		if (runner != null) {
			try {
				runner.setGlobalTimeout(parser.timeout);
				runner.setGlobalThinktime(parser.thinktime);
				runner.setGlobalScreenshotOnError(parser.screenshotOnError);

				if (parser.adb != null) {
					runner.setAdb(parser.adb);
				} else if (parser.agent != null && parser.agent.equalsIgnoreCase("AndroidEmulator")) {
					runner.setAdb(AndroidUtils.getAdb());
				}

				runner.setAgentProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP, parser.adbSerial);
				runner.setAgentProperty(AndroidEmulatorAgent.ADB_LOCAL_PORT_PROP,
						(parser.adbLocalPort > 0 ? Integer.toString(parser.adbLocalPort) : null));
				runner.setAgentProperty(AndroidEmulatorAgent.ADB_REMOTE_PORT_PROP,
						(parser.adbRemotePort > 0 ? Integer.toString(parser.adbRemotePort) : null));

				runner.setVerbose(parser.verbose);
				runner.setReportdir(parser.reportdir);
				runner.setTakeAfterMetrics(parser.screenshots);
				runner.setTakeAfterScreenshot(parser.screenshots);
				if (parser.quiet && !parser.verbose) {
					System.setOut(new PrintStream(new ByteArrayOutputStream()));
				}

				if (parser.scripts == null) {
					throw new RuntimeException("You must specify a script or suite to run");
				} else {
					// wait until the agent is ready
					if (!runner.waitUntilReady(parser.startup)) {
						throw new RuntimeException(
								"Unable to startup MonkeyTalk connection - timeout after "
										+ parser.startup + "s");
					}

					// for each script, run it!
					for (File script : parser.scripts) {
						runner.run(script, parser.getGlobals());
					}
				}
			} catch (RuntimeException ex) {
				System.out.println(BuildStamp.STAMP);
				System.out.println("\nERROR: " + ex.getMessage() + "\n");
				parser.printUsage();
			} finally {
				if (runner.getAgent() != null) {
					runner.getAgent().close();
				}
			}
		}
	}

	/**
	 * Commandline arg parser based on JCommander.
	 */
	private static class CommandlineParser {
		private JCommander jcommander;

		@Parameter(names = "-agent", description = "Target agent [iOS, Android, AndroidEmulator]", required = true)
		private String agent;

		@Parameter(names = "-host", description = "Target host, defaults to localhost")
		private String host = "localhost";

		@Parameter(names = "-port", description = "Target port, defaults based on agent")
		private int port = -1;

		@Parameter(names = "-timeout", description = "Global timeout (in ms)")
		private int timeout = -1;

		@Parameter(names = "-thinktime", description = "Global thinktime (in ms)")
		private int thinktime = -1;

		@Parameter(names = "-startup", description = "Startup timeout (in s)")
		private int startup = -1;

		@Parameter(names = "-reportdir", converter = FileConverter.class, description = "Output folder for suite results")
		private File reportdir;

		@Parameter(names = "-adb", description = "Path to ADB", converter = FileConverter.class)
		private File adb;

		@Parameter(names = "-adbSerial", description = "ADB serial number")
		private String adbSerial = null;

		@Parameter(names = "-adbLocalPort", description = "ADB local port")
		private int adbLocalPort = -1;

		@Parameter(names = "-adbRemotePort", description = "ADB remote port")
		private int adbRemotePort = -1;

		@Parameter(names = "-screenshotOnError", description = "Take screenshot on error, defaults to on")
		private boolean screenshotOnError = true;

		@Parameter(names = "-screenshots", description = "Take before and after screenshots on every command")
		private boolean screenshots = false;

		@Parameter(names = "-help", description = "print help and exit", help = true)
		private boolean help = false;

		@Parameter(names = "-version", description = "print version and exit", help = true)
		private boolean version = false;

		@Parameter(converter = FileConverter.class, description = "scripts...")
		private List<File> scripts;

		@Parameter(names = "-quiet", description = "Turn off all output")
		private boolean quiet = false;

		@Parameter(names = "-verbose", description = "Turn on extra output")
		private boolean verbose = false;

		@DynamicParameter(names = "-D", description = "Global variables")
		private Map<String, String> globals = new LinkedHashMap<String, String>();

		public CommandlineParser(String[] args) {
			jcommander = new JCommander(this, args);
		}

		public void printUsage() {
			jcommander.usage();
		}

		/**
		 * Helper to guarantee that global variable values are not quoted.
		 * 
		 * @return the commandline global variables as a map
		 */
		private Map<String, String> getGlobals() {
			Map<String, String> m = new LinkedHashMap<String, String>();
			for (Map.Entry<String, String> entry : globals.entrySet()) {
				String val = entry.getValue();
				if (val.startsWith("\"") && val.endsWith("\"")) {
					val = val.substring(1, val.length() - 1);
				}
				m.put(entry.getKey(), val);
			}
			return m;
		}
	}
}