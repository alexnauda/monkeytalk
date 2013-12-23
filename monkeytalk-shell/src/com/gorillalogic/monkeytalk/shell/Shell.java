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
package com.gorillalogic.monkeytalk.shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.NullCompleter;
import jline.console.completer.StringsCompleter;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.History.Entry;
import jline.console.history.MemoryHistory;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.AndroidEmulatorAgent;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.api.meta.API;
import com.gorillalogic.monkeytalk.api.meta.Component;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.shell.command.Help;
import com.gorillalogic.monkeytalk.shell.command.Ping;
import com.gorillalogic.monkeytalk.shell.command.Thinktime;
import com.gorillalogic.monkeytalk.shell.command.Timeout;
import com.gorillalogic.monkeytalk.shell.command.Tree;
import com.gorillalogic.monkeytalk.shell.command.Vars;
import com.gorillalogic.monkeytalk.utils.AndroidUtils;

/**
 * MonkeyTalk shell.
 */
public class Shell {
	private static final String HISTORY_FILENAME = ".mt_history";
	private static final int HISTORY_MAXSIZE = 100;
	private static final List<String> EXCLUDED_COMPONENTS = Arrays.asList("Suite", "Test", "Setup",
			"Teardown", "Verifiable", "Debug", "Doc");

	public static void main(String[] args) {
		CommandlineParser parser = null;

		try {
			parser = new CommandlineParser(args);
		} catch (RuntimeException ex) {
			parser.printUsage();
			return;
		}

		// color?
		Print.setColor(parser.color);

		// print stamp
		Print.info(BuildStamp.STAMP);

		// version?
		if (parser.version) {
			return;
		}

		// help?
		if (parser.help) {
			parser.printUsage();
			return;
		}

		// working dir
		File dir = new File("").getAbsoluteFile();
		Print.info("  workingDir=" + dir.getAbsolutePath());

		// init agent
		IAgent agent = AgentManager.getAgent(parser.agent, parser.host, parser.port);

		if (parser.adb != null) {
			agent.setProperty(AndroidEmulatorAgent.ADB_PROP, parser.adb.getAbsolutePath());
		} else if (parser.agent != null && parser.agent.equalsIgnoreCase("AndroidEmulator")) {
			agent.setProperty(AndroidEmulatorAgent.ADB_PROP, AndroidUtils.getAdbPath());
		}

		agent.setProperty(AndroidEmulatorAgent.ADB_SERIAL_PROP, parser.adbSerial);
		agent.setProperty(AndroidEmulatorAgent.ADB_LOCAL_PORT_PROP,
				(parser.adbLocalPort > 0 ? Integer.toString(parser.adbLocalPort) : null));
		agent.setProperty(AndroidEmulatorAgent.ADB_REMOTE_PORT_PROP,
				(parser.adbRemotePort > 0 ? Integer.toString(parser.adbRemotePort) : null));

		agent.start();

		// init processor
		ScriptProcessor processor = new ScriptProcessor(dir, agent);
		processor.setGlobalTimeout(parser.timeout);
		processor.setGlobalThinktime(parser.thinktime);
		processor.setGlobalScreenshotOnError(parser.screenshotOnError);

		// init scope
		Scope scope = new Scope("shell");

		try {
			// init globals
			Globals.clear();
			if (dir != null && dir.exists() && dir.isDirectory()) {
				Globals.setGlobals(new File(dir, "globals.properties"));
			}
			Globals.setGlobals(parser.getGlobals());

			// init shell
			ConsoleReader reader = initShell(dir, parser.color, true);

			String line = null;
			while (true) {
				try {
					line = reader.readLine();
					if (line != null) {
						line = line.trim();

						if (line.toLowerCase().startsWith("/h")) {
							Print.println(Help.HELP);
						} else if (line.toLowerCase().startsWith("/hi")) {
							StringBuilder sb = new StringBuilder("HISTORY:\n");
							for (Entry e : reader.getHistory()) {
								sb.append(' ').append(e.index() + 1).append(": ");
								sb.append(e.value()).append('\n');
							}
							Print.println(sb);
						} else if (line.toLowerCase().startsWith("/p")) {
							Ping ping = new Ping(line, processor);
							ping.run();
						} else if (line.toLowerCase().startsWith("/q")) {
							break;
						} else if (line.toLowerCase().startsWith("/th")) {
							Thinktime thinktime = new Thinktime(line, processor);
							thinktime.run();
						} else if (line.toLowerCase().startsWith("/ti")) {
							Timeout timeout = new Timeout(line, processor);
							timeout.run();
						} else if (line.toLowerCase().startsWith("/tr")) {
							Tree tree = new Tree(line, processor);
							tree.run();
						} else if (line.toLowerCase().startsWith("/v")) {
							Vars vars = new Vars(line, processor, scope);
							vars.run();
						} else {
							Command cmd = new Command(line);
							Command full = scope.substituteCommand(cmd);
							Print.print(full);
							PlaybackResult result = processor.runScript(full, scope);
							Print.print(" -> ");
							Print.println(result);
						}
					}
				} catch (Throwable t) {
					Print.err("\nERROR" + (t.getMessage() != null ? ": " + t.getMessage() : ""));
					History historyOld = reader.getHistory();
					reader = initShell(dir, parser.color, false);
					if (historyOld != null) {
						reader.setHistory(historyOld);
					}
				}
			}

			// flush history to disk
			if (reader.getHistory() instanceof FileHistory) {
				Print.info("...save history");
				FileHistory history = (FileHistory) reader.getHistory();
				history.flush();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			agent.close();
		}
		Print.info("BYE");
	}

	/**
	 * Init the JLine2 reader with an in-memory command history.
	 */
	private static ConsoleReader initShell(File dir, boolean color, boolean loadHistory)
			throws IOException {

		// reader
		ConsoleReader reader = new ConsoleReader();
		reader.setPrompt(color ? "\u001B[35m" + "mt>" + "\u001B[0m " : "mt> ");

		// command history
		if (loadHistory) {
			reader.setHistory(getHistory(dir));
		}

		// tab completion
		List<String> components = new ArrayList<String>();
		for (Component c : API.getComponents()) {
			if (!EXCLUDED_COMPONENTS.contains(c.getName())) {
				components.add(c.getName());
			}
		}
		reader.addCompleter(new ArgumentCompleter(new StringsCompleter(components),
				new NullCompleter()));

		return reader;
	}

	private static History getHistory(File dir) {
		try {

			String home = System.getenv("HOME");
			if (home == null) {
				// no HOME environment variable, so just use the current working dir
				home = dir.getAbsolutePath();
			}
			File homeDir = new File(home);

			if (homeDir.exists() && homeDir.isDirectory()) {
				File historyFile = new File(homeDir, HISTORY_FILENAME);
				Print.info("  history=" + historyFile.getAbsolutePath());
				FileHistory history = new FileHistory(historyFile);
				history.setMaxSize(HISTORY_MAXSIZE);
				history.setAutoTrim(true);
				history.setIgnoreDuplicates(false);
				return history;
			}
		} catch (Exception ex) {
			// ignore
		}

		// something went wrong, so just use an in-memory history
		MemoryHistory mem = new MemoryHistory();
		mem.setMaxSize(HISTORY_MAXSIZE);
		mem.setAutoTrim(true);
		mem.setIgnoreDuplicates(false);
		return mem;
	}

	/** Commandline arg parser based on JCommander. */
	private static class CommandlineParser {
		private JCommander jcommander;

		@Parameter(names = "-agent", description = "target agent [iOS, Android, AndroidEmulator]", required = true)
		private String agent;

		@Parameter(names = "-host", description = "target host, defaults to localhost")
		private String host = "localhost";

		@Parameter(names = "-port", description = "target port, defaults based on agent")
		private int port = -1;

		@Parameter(names = "-timeout", description = "global timeout (in ms)")
		private int timeout = -1;

		@Parameter(names = "-thinktime", description = "global thinktime (in ms)")
		private int thinktime = -1;

		@Parameter(names = "-adb", description = "the path to adb", converter = FileConverter.class)
		private File adb;

		@Parameter(names = "-adbSerial", description = "adb serial number")
		private String adbSerial = null;

		@Parameter(names = "-adbLocalPort", description = "adb local port")
		private int adbLocalPort = -1;

		@Parameter(names = "-adbRemotePort", description = "adb remote port")
		private int adbRemotePort = -1;

		@Parameter(names = "-help", description = "print help and exit", help = true)
		private boolean help = false;

		@Parameter(names = "-version", description = "print version and exit", help = true)
		private boolean version = false;

		@Parameter(names = "-screenshotOnError", description = "global screenshot on error")
		private boolean screenshotOnError = true;

		@Parameter(names = "-color", description = "turn on colored prompt and output")
		private boolean color = false;

		@DynamicParameter(names = "-D", description = "global variables")
		private Map<String, String> globals = new LinkedHashMap<String, String>();

		public CommandlineParser(String[] args) {
			jcommander = new JCommander(this, args);
		}

		/** Print the auto-generated commandline usage. */
		public void printUsage() {
			jcommander.usage();
		}

		/** Helper to guarantee that global variable values are <i>not</i> quoted. */
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
