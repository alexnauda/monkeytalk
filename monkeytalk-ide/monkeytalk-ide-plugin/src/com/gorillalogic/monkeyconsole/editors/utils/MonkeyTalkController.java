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
package com.gorillalogic.monkeyconsole.editors.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.log.Log;

import com.gorillalogic.monkeyconsole.ADBHelper;
import com.gorillalogic.monkeyconsole.componentview.ui.UIContainerView;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;
import com.gorillalogic.monkeyconsole.server.RecordServer;
import com.gorillalogic.monkeyconsole.tableview.MonkeyTalkTabularEditor;
import com.gorillalogic.monkeytalk.BuildStamp;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.agents.AgentManager;
import com.gorillalogic.monkeytalk.agents.IAgent;
import com.gorillalogic.monkeytalk.automators.ActionFilter;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Runner;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.SuiteListener;
import com.gorillalogic.monkeytalk.processor.report.Report;
import com.gorillalogic.monkeytalk.processor.report.detail.DetailReportHtml;
import com.gorillalogic.monkeytalk.processor.report.detail.ScriptReportHelper;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.server.ServerConfig;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class MonkeyTalkController {

	private static final int MAX_CONNECTION_RETRIES = 30;
	private static final long CONNECTION_RETRY_DELAY = 1000;
	public IPreferenceStore preferenceStore;
	private ActionFilter recordFilter = new ActionFilter();
	private MonkeyTalkTabularEditor tabularEditor;
	private TextEditor textEditor;
	private FoneMonkeyConsoleHelper fmch;
	private boolean currentlyConnected = false;
	private Timer timer;
	private final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	private Runner runner;
	private ScriptProcessor processor;
	private CommandSender sender;

	private long replayCommandStartTime;

	Thread commandProcessorThread;
	public final RecordServer recordServer;
	private boolean replayON = false;

	// final String LICENSE =
	// "Copyright 2011 Gorilla Logic, Inc. - www.gorillalogic.com\n\nFoneMonkey is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\nFoneMonkey is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.";

	public MonkeyTalkController() {
		preferenceStore = FoneMonkeyPlugin.getDefault().getPreferenceStore();
		getRecordFilter().set(AutomatorConstants.TOUCH_DOWN, false);
		getRecordFilter().set(AutomatorConstants.TOUCH_UP, false);
		getRecordFilter().set(AutomatorConstants.TOUCH_MOVE, false);
		RecordServer temp;
		try {
			temp = new RecordServer();
		} catch (IOException e) {
			Log.warn("could not create RecordServer: " + e.getMessage());
			temp = null;
		}
		recordServer = temp;
	}

	public void stopRecordServer() {
		if (recordServer != null) {
			recordServer.stop();

		}
	}

	public void startReplayAll() {
		startReplayRange(0, tabularEditor.getCommands().size());
	}

	// public void playOnWeb(){
	// com.gorillalogic.agents.html.SeliniumCommandProcessor ssss = new
	// com.gorillalogic.agents.html.SeliniumCommandProcessor();
	// for(Command c : tabularEditor.getCommands()){
	// ssss.processACommand(c);
	// }
	// }
	public void startReplayRange(final int from3, final int to3) {
		final File scriptFile = ((FileEditorInput) tabularEditor.getEditorInput()).getPath()
				.toFile();

		Globals.clear();
		processor = new ScriptProcessor(scriptFile.getParentFile(), getAgent(getConnectionType()));
		processor.setGlobalThinktime(Integer.parseInt(preferenceStore
				.getString(PreferenceConstants.P_THINKTIME)));
		processor.setGlobalTimeout(Integer.parseInt(preferenceStore
				.getString(PreferenceConstants.P_DEFAULTTIMEOUT)));
		processor.setGlobalScreenshotOnError(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_TAKEERRORSCREENSHOTS));
		processor.setTakeAfterScreenshot(preferenceStore
				.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));
		processor.setTakeAfterMetrics(preferenceStore
				.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));

		runner = new Runner(getAgent(getConnectionType()));
		runner.setGlobalThinktime(Integer.parseInt(preferenceStore
				.getString(PreferenceConstants.P_THINKTIME)));
		runner.setGlobalTimeout(Integer.parseInt(preferenceStore
				.getString(PreferenceConstants.P_DEFAULTTIMEOUT)));
		runner.setGlobalScreenshotOnError(FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.P_TAKEERRORSCREENSHOTS));
		runner.setTakeAfterScreenshot(preferenceStore
				.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));
		runner.setTakeAfterMetrics(preferenceStore
				.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));

		final int from2 = from3 - tabularEditor.getBlankCommandOffset(from3);
		final int to2 = to3 - tabularEditor.getBlankCommandOffset(to3);

		tabularEditor.deleteBlankRows();
		PlaybackListener listener = new PlaybackListener() {

			@Override
			public void onStart(final Scope scope) {
				replayCommandStartTime = System.currentTimeMillis();
				fmch.writeToConsole(scope.getCurrentCommand().toString());

				MonkeyTalkUtils.runOnGUI(new Runnable() {
					public void run() {
						if (from2 != to2) {
							if (scope.getFilename() == null) {
								tabularEditor.setSelection(from2 + scope.getCurrentIndex() - 1);
							}
						}
					}
				}, tabularEditor.getSite().getShell().getDisplay());
			}

			@Override
			public void onScriptStart(Scope scope) {
				fmch.writeToConsole("Started Script Playback");
				fmch.bringToFront();
				setReplayON(true);
				setPlaybackControlsState();
			}

			@Override
			public void onScriptComplete(Scope scope, PlaybackResult r) {
				String msg = "Completed Script Playback" + " - " + r.getStatus().toString();
				if (r.getMessage() != null) {
					msg = msg + " " + r.getMessage();
				}
				fmch.writeToConsole(msg);

				setReplayON(false);
				setPlaybackControlsState();
			}

			@Override
			public void onComplete(final Scope scope, final Response response) {
				// long elapsed = System.currentTimeMillis() -
				// replayCommandStartTime;
				if (response.getStatus() == ResponseStatus.FAILURE) {
					MonkeyTalkUtils.runOnGUI(new Runnable() {
						public void run() {
							if (scope.getFilename() == null) {
								tabularEditor.setSelection(from2 + scope.getCurrentIndex() - 1);
								tabularEditor.markRowAsError(from2 + scope.getCurrentIndex() - 1);
							}
							fmch.writeToConsole("FAILURE: " + response.getMessage(), true);
						}
					}, tabularEditor.getSite().getShell().getDisplay());

				} else if (response.getStatus() == ResponseStatus.ERROR) {
					MonkeyTalkUtils.runOnGUI(new Runnable() {
						public void run() {
							if (scope.getFilename() == null) {
								tabularEditor.setSelection(from2 + scope.getCurrentIndex() - 1);
								tabularEditor.markRowAsError(from2 + scope.getCurrentIndex() - 1);
							}
							fmch.writeToConsole("ERROR: " + response.getMessage(), true);
						}
					}, tabularEditor.getSite().getShell().getDisplay());
				} else {
					String m = response.getStatus().toString();
					String msg = response.getMessage();
					if (msg != null && msg.length() > 0) {
						m = m + " - " + msg;
						fmch.writeToConsole(m);
					}
				}
			}

			@Override
			public void onPrint(String message) {
				fmch.writeToConsole(message, SWT.COLOR_DARK_BLUE);
			}
		};
		processor.setPlaybackListener(listener);
		runner.setScriptListener(listener);

		commandProcessorThread = new Thread(new Runnable() {
			public void run() {
				if (tabularEditor.getCommands().size() >= to2
						&& tabularEditor.getCommands().size() > 0) {
					PlaybackResult result = null;
					if (from2 == to2) {
						// play just a single row
						result = processor.runScript(tabularEditor.getCommands().get(from2),
								new Scope());
					} else if (from2 == 0 && to2 == tabularEditor.getCommands().size()) {
						// play all!
						result = runner.run(scriptFile, null);
					} else if (from2 >= 0 && to2 >= 0) {
						// play range of rows
						result = processor.runScript(tabularEditor.getCommands()
								.subList(from2, to2), new Scope());
					}
					writeDetailReport(result, new Scope("ide"));
				}
			}
		});
		commandProcessorThread.start();
	}

	public void startJScriptReplay() {

		Job job = new Job("MonkeyTalk JSTest") {
			protected IStatus run(final IProgressMonitor monitor) {

				// monitor.worked(50);
				File jsFile = ((FileEditorInput) jsEditor.getEditorInput()).getPath().toFile();

				Runner runner = new Runner(getAgent(getConnectionType()));
				runner.setGlobalThinktime(Integer.parseInt(preferenceStore
						.getString(PreferenceConstants.P_THINKTIME)));
				runner.setGlobalTimeout(Integer.parseInt(preferenceStore
						.getString(PreferenceConstants.P_DEFAULTTIMEOUT)));
				runner.setGlobalScreenshotOnError(FoneMonkeyPlugin.getDefault()
						.getPreferenceStore()
						.getBoolean(PreferenceConstants.P_TAKEERRORSCREENSHOTS));
				runner.setTakeAfterScreenshot(preferenceStore
						.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));
				runner.setTakeAfterMetrics(preferenceStore
						.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));
				runner.setScriptListener(new PlaybackListener() {

					@Override
					public void onStart(Scope scope) {
						monitor.subTask(scope.getCurrentCommand().getCommand());
						fmch.writeToConsole(scope.getCurrentCommand().getCommand());
					}

					@Override
					public void onScriptStart(Scope scope) {
					}

					@Override
					public void onScriptComplete(Scope scope, PlaybackResult result) {
					}

					@Override
					public void onComplete(Scope scope, Response response) {
						fmch.writeToConsole(response.getStatus().toString(),
								response.getStatus() == ResponseStatus.ERROR);

						if (monitor.isCanceled()) {
							throw new OperationCanceledException("Run cancelled");
						}

					}

					@Override
					public void onPrint(String message) {
						fmch.writeToConsole(message, SWT.COLOR_DARK_BLUE);
					}
				});

				PlaybackResult p = runner.run(jsFile, null);
				try {
					writeDetailReport(p, new Scope("ide"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();

	}

	protected void writeDetailReport(PlaybackResult result, Scope scope) {
		File reportdir = getReportDir();
		if (reportdir == null) {
			return;
		}

		if (result == null) {
			result = new PlaybackResult(PlaybackStatus.ERROR, "no results returned", scope);
		}
		try {
			// Save the xml detail report
			saveXmlDetailReport(reportdir, result, scope);
			// Save the hmtl detail report
			saveHtmlDetailReport(reportdir, result, getDetailReportXml(result, scope, reportdir));

		} catch (Exception e) {
			System.out.println("error writing the detail report: " + e.getMessage());
			e.printStackTrace();
		}

		// last, we refresh the workspace to pick everything up
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			if (workspace != null && workspace.getRoot() != null) {
				workspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			} else {
				System.err
						.println("cannot refresh workspace after detail report creation: unable to obtain workspace root");
			}
		} catch (CoreException e) {
			// Could not refresh workspace, no biggie
			System.out.println("error refreshing workspace after detail report creation: "
					+ e.getMessage());
		}
	}

	protected void saveXmlDetailReport(File reportdir, PlaybackResult result, Scope scope)
			throws Exception {
		File detailReportFile = null;
		detailReportFile = new File(reportdir, getXMLDetailReportFilename(result.getScope()
				.getFilename()));

		if (!reportdir.exists()) {
			if (!reportdir.mkdirs()) {
				System.err.println("error writing detail report to file '"
						+ detailReportFile.getPath() + "': directory '" + reportdir.getPath()
						+ "' does not exist and cannot be created");
				return;
			}
		}

		String report = getDetailReportXml(result, scope, reportdir);

		FileWriter fw = null;
		try {
			fw = new FileWriter(detailReportFile);
			fw.write(report);
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

	protected void saveHtmlDetailReport(File reportdir, PlaybackResult result, String xmlReport)
			throws Exception {
		File detailReportFile = null;
		if (result.getScope().getFilename() != null) {
			detailReportFile = new File(reportdir, getHTMLDetailReportFilename(result.getScope()
					.getFilename()));
		} else {
			detailReportFile = new File(reportdir, getHTMLDetailReportFilename(result.getScope()
					.getFilename()));
			result.setScope(new Scope("DETAIL"));
		}
		if (!reportdir.exists()) {
			if (!reportdir.mkdirs()) {
				System.err.println("error writing detail report to file '"
						+ detailReportFile.getPath() + "': directory '" + reportdir.getPath()
						+ "' does not exist and cannot be created");
				return;
			}
		}
		String detailHtml = getDetailReportHtml(result, xmlReport);
		FileWriter fw = null;

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

	protected String getDetailReportXml(PlaybackResult result, Scope scope, File reportDir) {
		String report = null;
		try {
			String agentVersion = "";
			if (runner != null && runner.getAgent() != null) {
				IAgent agent = runner.getAgent();
				agentVersion = agent.getName() + " v" + agent.getAgentVersion();
			}
			report = new ScriptReportHelper().createDetailReport(result, scope, getProjectDir(),
					reportDir, getRunnerVersion(), agentVersion).toXMLDocument();
		} catch (Exception e) {
			e.printStackTrace();
			report = "<detail result=\"ERROR\"><msg><![CDATA[" + "REPORTING ERROR : "
					+ e.getMessage() + "]]></msg></detail>";
		}
		return report;
	}

	protected String getDetailReportHtml(PlaybackResult result, String detailXml) {
		String report = null;
		try {
			report = new DetailReportHtml().createDetailReportHtml(result, detailXml);
		} catch (Exception ex) {
			ex.printStackTrace();
			report = "<html><head><title>ERROR</title></head>" + "<body><h1>REPORTING ERROR</h1>"
					+ "<p>" + ex.getMessage() + "</p></body></html>";
		}
		return report;
	}

	protected String getRunnerVersion() {
		String runner = "MonkeyTalk IDE"
				+ " v"
				+ BuildStamp.VERSION
				+ (BuildStamp.BUILD_NUMBER != null && BuildStamp.BUILD_NUMBER.length() > 0 ? "_"
						+ BuildStamp.BUILD_NUMBER : "") + " - " + BuildStamp.TIMESTAMP;
		;
		return runner;
	}

	// public static String DETAIL_REPORT_FILENAME="detail.xml";
	// protected String getDetailReportFilename() {
	// return DETAIL_REPORT_FILENAME;
	// }
	public static String getXMLDetailReportFilename(String filename) {
		String name;
		if (filename != null) {
			name = "DETAIL-" + filename + ".xml";
		} else {
			name = "DETAIL.xml";
		}
		return name;
	}

	public static String getHTMLDetailReportFilename(String filename) {
		String name;
		if (filename != null) {
			name = "DETAIL-" + filename + ".html";
		} else {
			name = "DETAIL.html";
		}
		return name;
	}

	/**
	 * Stop the replay
	 */
	public void stopReplay() {
		try {
			if (null != commandProcessorThread && commandProcessorThread.isAlive()) {
				runner.abort();
				processor.abort();
			}
		} finally {
			this.setReplayON(false);
			setPlaybackControlsState();
		}
	}

	private File getReportDir() {
		return new File(getProjectDir(), "reports");
	}

	private File getProjectDir() {
		return ((FileEditorInput) tabularEditor.getEditorInput()).getPath().toFile()
				.getParentFile();
	}

	public void startSuiteReplay() {

		Job job = new Job("MonkeyTalk TestSuite") {
			protected IStatus run(final IProgressMonitor monitor) {

				monitor.worked(50);
				final File f = getProjectDir();
				final File reportdir = getReportDir();
				if (!reportdir.exists()) {
					reportdir.mkdir();
				}

				// System.out.println(f.getAbsolutePath());
				Runner runner = new Runner(getAgent(getConnectionType()));
				runner.setReportdir(reportdir);
				runner.setGlobalThinktime(Integer.parseInt(preferenceStore
						.getString(PreferenceConstants.P_THINKTIME)));
				runner.setGlobalTimeout(Integer.parseInt(preferenceStore
						.getString(PreferenceConstants.P_DEFAULTTIMEOUT)));
				runner.setGlobalScreenshotOnError(FoneMonkeyPlugin.getDefault()
						.getPreferenceStore()
						.getBoolean(PreferenceConstants.P_TAKEERRORSCREENSHOTS));
				runner.setTakeAfterScreenshot(preferenceStore
						.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));
				runner.setTakeAfterMetrics(preferenceStore
						.getBoolean(PreferenceConstants.P_TAKEAFTERSCREENSHOTS));
				runner.setSuiteListener(new SuiteListener() {

					@Override
					public void onSuiteStart(int total) {
						monitor.beginTask("Running TestSuite", total);

					}

					@Override
					public void onSuiteComplete(PlaybackResult result, Report report) {
						monitor.worked(1);
						try {

							FileUtils.writeFile(getReportFile(reportdir), report.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (result != null)
							updateJUnitView(getReportFile(reportdir), result.getMessage());
					}

					@Override
					public void onTestStart(String name, int num, int total) {
						monitor.setTaskName(name);

					}

					@Override
					public void onTestComplete(PlaybackResult result, Report report) {
						monitor.worked(1);
						try {
							FileUtils.writeFile(getReportFile(reportdir), report.toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						updateJUnitView(getReportFile(reportdir), result.getMessage());
					}

					@Override
					public void onRunStart(int total) {
					}

					@Override
					public void onRunComplete(PlaybackResult result, Report report) {
					}
				});

				runner.setScriptListener(new PlaybackListener() {

					@Override
					public void onStart(Scope scope) {
						monitor.subTask(scope.getCurrentCommand().getCommand());
					}

					@Override
					public void onScriptStart(Scope scope) {
					}

					@Override
					public void onScriptComplete(Scope scope, PlaybackResult result) {
					}

					@Override
					public void onComplete(Scope scope, Response response) {
						if (monitor.isCanceled()) {
							throw new OperationCanceledException("Run cancelled");
						}
					}

					@Override
					public void onPrint(String message) {
					}
				});

				File suiteFile = ((FileEditorInput) tabularEditor.getEditorInput()).getPath()
						.toFile();
				String suiteName = suiteFile.getName();
				final PlaybackResult p = runner.run(suiteFile, null);
				updateJUnitView(f, p.getMessage());
				writeDetailReport(p, new Scope(suiteName));
				return Status.OK_STATUS;
			}

			public void updateJUnitView(final File reportfile, final String msg) {
				MonkeyTalkUtils.runOnGUI(new Runnable() {
					public void run() {
						try {

							((FileEditorInput) tabularEditor.getEditorInput()).getFile()
									.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

							IWorkbenchWindow window = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow();
							IWorkbenchPage page = window.getActivePage();
							org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart v = (org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart) page
									.showView("org.eclipse.jdt.junit.ResultView");
							JUnitModel.importTestRunSession(reportfile);
							if (msg != null && !msg.equalsIgnoreCase("null"))
								fmch.writeToConsole(msg);
						} catch (CoreException e) {

						}

					}
				}, tabularEditor.getSite().getShell().getDisplay());
			}

			private File getReportFile(File reportdir) {
				String fileName = ((FileEditorInput) tabularEditor.getEditorInput()).getPath()
						.toFile().getName();
				return new File(reportdir.getAbsolutePath() + "/TEST-"
						+ fileName.substring(0, fileName.lastIndexOf(".")) + ".xml");
			}
		};
		job.setUser(true);
		job.schedule();

	}

	/**
	 * Clear all rows
	 */
	public void clear() {
		// TODO add dialog
		tabularEditor.clear();
		textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput())
				.set(tabularEditor.getCommandsAsString());
	}

	public void addARow(final Command command, final boolean b) {
		MonkeyTalkUtils.runOnGUI(new Runnable() {
			public void run() {
				tabularEditor.appendRow(command, b);

				textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput())
						.set(tabularEditor.getCommandsAsString());
			}
		}, tabularEditor.getSite().getShell().getDisplay());

	}

	public ActionFilter getRecordFilter() {
		return recordFilter;
	}

	public void toggleRecordFilter(String action) {
		ActionFilter f = getRecordFilter();
		f.set(action, !f.get(action));
	}

	private Action clearButton;
	private Action componentTreeButton;
	private Action stopbutton;
	private Action playbutton;
	private Action recordbutton;
	private Action playOnCloudButton;
	private String extention;

	public void setContextualData(MonkeyTalkTabularEditor tabularEditor, TextEditor textEditor,
			Action clearButton, Action componentTreeButton, Action playbutton, Action stopbutton,
			Action recordbutton, Action playOnCloudButton) {
		// Editor
		fmch = new FoneMonkeyConsoleHelper(tabularEditor.getEditorSite());
		this.textEditor = textEditor;
		this.tabularEditor = tabularEditor;
		// buttons
		this.stopbutton = stopbutton;
		this.playbutton = playbutton;
		this.recordbutton = recordbutton;
		this.clearButton = clearButton;
		this.componentTreeButton = componentTreeButton;
		this.playOnCloudButton = playOnCloudButton;

	}

	public void setPlaybackControlsState() {
		try {
			clearButton.setEnabled(true);
			if (cloudIsConfigured()) {
				playOnCloudButton.setEnabled(true);
			}
			if (extention.equalsIgnoreCase("mt")) {
				if (isCurrentlyConnected()) {
					componentTreeButton.setEnabled(true);
					if (isRecordingON() || isReplayON()) {
						stopbutton.setEnabled(true);
						recordbutton.setEnabled(false);
						playbutton.setEnabled(false);
					} else {
						stopbutton.setEnabled(false);
						recordbutton.setEnabled(true);
						playbutton.setEnabled(true);
					}
				} else {
					componentTreeButton.setEnabled(false);
					stopbutton.setEnabled(false);
					recordbutton.setEnabled(false);
					playbutton.setEnabled(false);
				}
			} else if (extention.equalsIgnoreCase("mts")) {
				stopbutton.setEnabled(false);
				recordbutton.setEnabled(false);
				if (isCurrentlyConnected()) {
					componentTreeButton.setEnabled(true);
					playbutton.setEnabled(true);
				} else {
					componentTreeButton.setEnabled(false);
					playbutton.setEnabled(false);
				}
			} else if (extention.equalsIgnoreCase("js")) {
				stopbutton.setEnabled(false);
				recordbutton.setEnabled(false);
				playOnCloudButton.setEnabled(true);
				clearButton.setEnabled(false);
				if (isCurrentlyConnected()) {
					componentTreeButton.setEnabled(true);
					playbutton.setEnabled(true);
				} else {
					componentTreeButton.setEnabled(false);
					playbutton.setEnabled(false);
				}
			} else {
				componentTreeButton.setEnabled(false);
				playbutton.setEnabled(false);
				stopbutton.setEnabled(false);
				recordbutton.setEnabled(false);
				clearButton.setEnabled(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void resetbuttons(Action stopbutton, Action playbutton, Action recordbutton) {
		this.stopbutton = stopbutton;
		this.playbutton = playbutton;
		this.recordbutton = recordbutton;
	}

	public void setExtention(String extention) {
		this.extention = extention;
	}

	public boolean isCurrentlyConnected() {
		return currentlyConnected;
	}

	public void setCurrentlyConnected(boolean c) {
		currentlyConnected = c;
	}

	public void setHost(String host) {
		preferenceStore.setValue(PreferenceConstants.C_HOST, host);
	}

	public void setCloudHost(String host) {
		preferenceStore.setValue(PreferenceConstants.C_CLOUD_HOST, host);
	}

	private boolean cloudIsConfigured() {
		return (preferenceStore.getString(PreferenceConstants.C_CLOUD_HOST) != null)
				|| !(preferenceStore.getString(PreferenceConstants.C_CLOUD_HOST).isEmpty()) ? true
				: false;
	}

	/**
	 * @return
	 */
	public ConnectionTypesEnum getConnectionType() {
		String value = preferenceStore.getString(PreferenceConstants.C_CONNECTION_TYPE);
		if (value.equalsIgnoreCase(ConnectionTypesEnum.EMULATOR.toString())) {
			return ConnectionTypesEnum.EMULATOR;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.SIMULATOR.toString())) {
			return ConnectionTypesEnum.SIMULATOR;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.NETWORKED_ANDROID.toString())) {
			return ConnectionTypesEnum.NETWORKED_ANDROID;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.NETWORKED_IOS.toString())) {
			return ConnectionTypesEnum.NETWORKED_IOS;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.FLEX.toString())) {
			return ConnectionTypesEnum.FLEX;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.WEB.toString())) {
			return ConnectionTypesEnum.WEB;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.CHROME.toString())) {
			return ConnectionTypesEnum.CHROME;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.SAFARI.toString())) {
			return ConnectionTypesEnum.SAFARI;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.IE.toString())) {
			return ConnectionTypesEnum.IE;
		} else if (value.equalsIgnoreCase(ConnectionTypesEnum.CLOUD_ANDROID.toString())) {
			return ConnectionTypesEnum.CLOUD_ANDROID;
		} else {
			return ConnectionTypesEnum.NO_DEVICE;
		}
	}

	/**
	 * This new code defaults the address to the Android loopback address, it might be more
	 * acceptable to use something else, but this is most convenient now. We will go through all the
	 * network devices and look for an IP that is not a loopback address. This could fail if the
	 * recording computer has more than one NIC and we choose the NIC that is not serving up
	 * monkeytalk. If we find this to be an issue it will be worthwhile to ping the record service
	 * at this point and be sure it is listening. We explicitly exclude VirtualBox or VMware
	 * interfaces since they were giving us problems on Windows 7. Lastly, we just return the first
	 * valid match.
	 */
	public String getIpAddress() {
		// short circuit if we are connecting to iOS simulator -- they are
		// always on localhost
		ConnectionTypesEnum type = getConnectionType();
		if (ConnectionTypesEnum.SIMULATOR == type) {
			return "localhost";
		}

		List<String> rejectedHostAddresses = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();

				if (iface.isUp() && !iface.isLoopback() && !iface.isVirtual()
						&& !iface.getDisplayName().startsWith("VirtualBox")
						&& !iface.getDisplayName().startsWith("VMware")) {
					Enumeration<InetAddress> addresses = iface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress addr = addresses.nextElement();
						String hostAddress = addr.getHostAddress();
						if (!hostAddress.equalsIgnoreCase("127.0.0.1")
								&& !hostAddress.substring(0, 7).equalsIgnoreCase("169.254")
								&& hostAddress.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
								&& addr instanceof Inet4Address) {
							return hostAddress; // return the first good match!
						} else {
							rejectedHostAddresses.add(hostAddress);
						}
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		// Log.warn("could not find acceptable IP address for recording in: "
		// + rejectedHostAddresses.toString() + ", will use default of "
		// + ServerConfig.DEFAULT_RECORD_HOST);
		return ServerConfig.DEFAULT_RECORD_HOST; // return 10.0.2.2 if no match
													// found
	}

	private String getHost() {
		return getHost(false);
	}

	public String getHost(boolean prompt) {
		if (preferenceStore.getString(PreferenceConstants.C_HOST) == null || prompt) {
			String host = enterText(
					"Please enter IP or hostname of networked iOS or Android device",
					preferenceStore.getString(PreferenceConstants.C_HOST) == null ? ""
							: preferenceStore.getString(PreferenceConstants.C_HOST));
			if (host != null)
				preferenceStore.setValue(PreferenceConstants.C_HOST, host);
		}
		// System.out.println(preferenceStore.getString(PreferenceConstants.C_HOST));
		return preferenceStore.getString(PreferenceConstants.C_HOST);
	}

	public String getCloudHost() {
		return preferenceStore.getString(PreferenceConstants.C_CLOUD_HOST);
	}

	String siteToTest = null;

	public String getWebsiteToTest(boolean prompt) {
		if (siteToTest == null || siteToTest.trim().length() == 0 || prompt) {
			String host = enterText("Please enter the url of the website to test",
					siteToTest == null ? "" : siteToTest);
			if (host != null)
				siteToTest = host;
		}
		return siteToTest;
	}

	// Need to provide default via IAgent
	private String getConnectedHost() {
		if (getConnectionType() == ConnectionTypesEnum.EMULATOR) {
			return "localhost";
		}

		if (getConnectionType() == ConnectionTypesEnum.SIMULATOR) {
			return "localhost";
		}

		if (getConnectionType() == ConnectionTypesEnum.FLEX) {
			return "localhost";
		}

		if (getConnectionType() == ConnectionTypesEnum.NO_DEVICE) {
			return "nodevice";
		}
		if (getConnectionType() == ConnectionTypesEnum.WEB) {
			// return getWebsiteToTest(false);
			return "localhost";
		}
		if (getConnectionType() == ConnectionTypesEnum.CHROME) {
			// return getWebsiteToTest(false);
			return "localhost";
		}
		if (getConnectionType() == ConnectionTypesEnum.SAFARI) {
			// return getWebsiteToTest(false);
			return "localhost";
		}
		if (getConnectionType() == ConnectionTypesEnum.IE) {
			// return getWebsiteToTest(false);
			return "localhost";
		}

		return getHost();
	}

	class AndroidHomePathValidator implements IInputValidator {
		public String isValid(String newText) {
			return ADBHelper.validateAndroidSdkPath(newText);
		}
	}

	String enterText(String msg, String value) {
		InputDialog dlg = new InputDialog(tabularEditor.getEditorSite().getShell(), "", msg, value,
				null);

		if (dlg.open() == Window.OK)
			return dlg.getValue();

		return null;
	}

	private String getConnectionStatus() {

		if (getConnectionType() == null) {
			return "Please select a device";
		}

		String type = getConnectionType().humanReadableFormat;
		// If we are connected to a network device, print out the IP address of the device.
		if (getConnectionType() == ConnectionTypesEnum.NETWORKED_ANDROID
				|| getConnectionType() == ConnectionTypesEnum.NETWORKED_IOS) {
			type = getHost(false);
		}

		if (type != null && !type.contains("unknown")) {
			return "Connected to Device: " + type;
		}

		return "Please select a connection type";
	}

	AbstractDecoratedTextEditor jsEditor;

	public void setJSContextualData(AbstractDecoratedTextEditor jsEditor) {
		this.jsEditor = jsEditor;
		fmch = new FoneMonkeyConsoleHelper(jsEditor.getEditorSite());
	}

	ConnectionTypesEnum connectionType;

	public void connect(ConnectionTypesEnum type) {
		connectionType = type;
		preferenceStore.setValue(PreferenceConstants.C_CONNECTION_TYPE, type.toString());
		String message = _connect(type);
		if (message != null) {
			if (message.contains("device not found")) {
				message = "Device could not be found!";
			} else if (message.length() == 0) {
				message = "unspecified";
			}
			fmch.writeToConsole(type.humanReadableFormat + " connection error: " + message, true);
		} else {
			setStatus(getConnectionStatus());
		}
		this.setPlaybackControlsState();
	}

	CommandSender commandSender;

	private String _connect(ConnectionTypesEnum type) {
		// we must validate ADB first (before getting the AndroidEmulator agent)
		if (type == ConnectionTypesEnum.EMULATOR) {
			String msg = ADBHelper.validate();
			if (msg != null) {
				return msg;
			}
		}
		IAgent agent = getAgent(type);

		File f = new File(((FileEditorInput) tabularEditor.getEditorInput()).getPath().toString());
		f = f.getParentFile();

		runner = new Runner(agent);
		commandSender = runner.getAgent().getCommandSender();
		this.getRecordServer().setCurrentAgent(agent);

		doHeartbeat();
		if (type != ConnectionTypesEnum.NO_DEVICE) {
			if (waitForConnection(DEFAULT_CONNECTION_TIMEOUT) == false) {
				return "connection to agent timed out";
			}
		}

		return null;
	}

	private IAgent getAgent(ConnectionTypesEnum type) {
		IAgent agent;
		if (type == ConnectionTypesEnum.WEB) {
			agent = AgentManager.getAgent("WebDriver");

		} else if (type == ConnectionTypesEnum.CHROME) {
			agent = AgentManager.getAgent("Chrome");

		} else if (type == ConnectionTypesEnum.SAFARI) {
			agent = AgentManager.getAgent("Safari");

		} else if (type == ConnectionTypesEnum.IE) {
			agent = AgentManager.getAgent("IE");

		} else if (type == ConnectionTypesEnum.EMULATOR) {
			agent = AgentManager.getAgent("AndroidEmulator");
			agent.setProperty("adb", ADBHelper.getAdbPath());

		} else if (connectionType == ConnectionTypesEnum.SIMULATOR
				|| connectionType == ConnectionTypesEnum.NETWORKED_IOS) {
			agent = AgentManager.getAgent("iOS", getConnectedHost());
		} else if (connectionType == ConnectionTypesEnum.FLEX) {
			agent = AgentManager.getAgent("Flex", getConnectedHost());
		} else if (connectionType == ConnectionTypesEnum.CLOUD_ANDROID) {
			agent = AgentManager.getAgent("CloudAndroid", getConnectedHost());
		} else {
			agent = AgentManager.getAgent("Android", getConnectedHost());
		}
		final IAgent agt = agent;

		Job job = new Job("MonkeyTalk") {
			protected IStatus run(final IProgressMonitor monitor) {
				monitor.beginTask("Starting browser", IProgressMonitor.UNKNOWN);
				agt.start();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return agent;
	}

	private boolean waitForConnection(int timeout) {
		long t = System.currentTimeMillis() + timeout;
		while (System.currentTimeMillis() < t) {
			if (this.isCurrentlyConnected()) {
				return true;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return false;
			}
		}
		return true;
	}

	public static File[] fileFinder(String dirName, final String extNamer) {
		File dir = new File(dirName);

		return dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(extNamer);
			}
		});

	}

	void doHeartbeat() {
		// if (getConnectionType() == ConnectionTypesEnum.WEB) {
		// currentlyConnected = true;
		// MonkeyTalkUtils.runOnGUI(new Runnable() {
		// public void run() {
		// setPlaybackControlsState();
		// }
		// }, false, tabularEditor.getSite().getShell().getDisplay());
		// return;
		// }
		final int[] attempts = { 0 };
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {

			private String prevPathToApp = "";

			private void checkForNewApp(String os, String pathToApp) {
				if (pathToApp != null && pathToApp.length() > 0) {
					if (!pathToApp.equals(prevPathToApp)) {
						prevPathToApp = pathToApp;
						notifyNewAppConnect(os, pathToApp);
					}
				}
			}

			private void notifyNewAppConnect(String os, String pathToApp) {
				String data = "username="
						+ FoneMonkeyPlugin.getDefault().getPreferenceStore()
								.getString(PreferenceConstants.P_CLOUDUSR);
				data += ",os=" + os;
				data += ",isDemoApp=" + MonkeyTalkUtils.isDemoApp(pathToApp);
				CloudServices.logEventAsync("IDE_CONNECT_TO_NEW_APP", data);
			}

			public void run() {

				boolean wasConnected = currentlyConnected;
				JSONObject body = null;

				// CommandSender cs = new CommandSender(getConnectedHost(),
				// getPlaybackPort());
				CommandSender cs = runner.getAgent().getCommandSender();

				Response r = cs.ping(isRecordingON(), getIpAddress(),
						ServerConfig.DEFAULT_RECORD_PORT);

				if (r.getStatus() == ResponseStatus.OK) {
					currentlyConnected = true;
					String pathToApp;
					try {
						currentlyConnected = true;
						body = r.getBodyAsJSON();
						if (body != null && body.has("message")) {
							Object msg = body.get("message");
							if (msg instanceof JSONObject) {
								String os = body.getJSONObject("message").getString("os");
								if (os != null) {
									if (os.equalsIgnoreCase("ios")) {
										pathToApp = body.getJSONObject("message").getString(
												"pathToApp");
										File[] list = MonkeyTalkController.fileFinder(pathToApp,
												".app");
										if (list != null && list.length > 0) {
											pathToApp = list[0].getAbsolutePath();
											preferenceStore.setValue(PreferenceConstants.C_APPNAME,
													pathToApp);
											if (pathToApp != null && pathToApp.length() > 0) {
												checkForNewApp(os, pathToApp);
											}
										}
									}
								}
							} else {
								try {
									if (MonkeyTalkController.this.isRecordingON()) {
										JSONArray list = body.getJSONArray("message");
										for (int i = 0; i < list.length(); i++) {
											JSONObject ob = list.getJSONObject(i);
											getRecordServer().getRecordListener().onRecord(
													new Command(ob), ob);
										}
									}
								} catch (JSONException e) {
									// if (!msg.contains("end of file")) {
									System.out.println("Received invalid recording msg " + msg);
									// }
								}
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					currentlyConnected = false;
					if (attempts[0]++ == MAX_CONNECTION_RETRIES) {
						timer.cancel();
					}
					try {
						Thread.sleep(CONNECTION_RETRY_DELAY);
					} catch (InterruptedException e) {
						// OK
					}

				}
				if (wasConnected != currentlyConnected) {
					final JSONObject jsonBody = body;
					MonkeyTalkUtils.runOnGUI(new Runnable() {
						public void run() {
							setPlaybackControlsState();
							if (currentlyConnected) {
								// we have just successfully connected
								String msg = getConnectMessage(jsonBody);
								if (msg != null) {
									fmch.writeToConsole(msg, false);
								} else {
									fmch.writeToConsole(
											"invalid response connecting to MonkeyTalk agent", true);
								}
							} else {
								// we have just disconnected
								fmch.writeToConsole("disconnected from MonkeyTalk agent", false);
							}
						}

						private String getConnectMessage(JSONObject jsonBody) {
							String msg = null;
							if (jsonBody != null) {
								try {
									if (jsonBody.has("message")) {
										JSONObject o = jsonBody.getJSONObject("message");
										String os = null;
										if (o.has("os")) {
											os = o.getString("os");
										}
										String mtversion = null;
										if (o.has("mtversion")) {
											mtversion = o.getString("mtversion");
										}
										if (os != null) {
											msg = "Connected to MonkeyTalk: " + os + " agent";
											if (mtversion != null) {
												msg += "(" + mtversion + ")";
											}
										}
									}
								} catch (JSONException e) {
									System.err.println("error formatting connect message:");
									e.printStackTrace();
								}
							}
							return msg;
						}
					}, false, tabularEditor.getSite().getShell().getDisplay());
				}
			}
		}, 0, // initial delay
				1 * 2500); // subsequent rate
	}

	public void setStatus(String msg) {
		setStatus(msg, false);
	}

	void setStatus(final String msg, final boolean isError) {
		MonkeyTalkUtils.runOnGUI(new Runnable() {
			public void run() {
				fmch.writeToConsole(msg, isError);
			}
		}, tabularEditor.getSite().getShell().getDisplay());
	}

	/**
	 * Get the running RecordServer, or run a new server.
	 * 
	 * @return the record server
	 */
	public RecordServer getRecordServer() {
		return recordServer;
	}

	// Need to use IAgent
	public int getPlaybackPort() {
		ConnectionTypesEnum type = getConnectionType();
		if (ConnectionTypesEnum.SIMULATOR == type || ConnectionTypesEnum.NETWORKED_IOS == type) {
			return ServerConfig.DEFAULT_PLAYBACK_PORT_IOS;
		}
		if (ConnectionTypesEnum.FLEX == type) {
			return ServerConfig.DEFAULT_PLAYBACK_PORT_FLEX;
		}
		if (ConnectionTypesEnum.WEB == type) {
			return ServerConfig.DEFAULT_PLAYBACK_PORT_WEB;
		}
		if (ConnectionTypesEnum.CHROME == type) {
			return ServerConfig.DEFAULT_PLAYBACK_PORT_WEB;
		}
		if (ConnectionTypesEnum.SAFARI == type) {
			return ServerConfig.DEFAULT_PLAYBACK_PORT_HTML5;
		}
		if (ConnectionTypesEnum.IE == type) {
			return ServerConfig.DEFAULT_PLAYBACK_PORT_WEB;
		}

		return ServerConfig.DEFAULT_PLAYBACK_PORT_ANDROID;
	}

	public void startRecording() {
		RecordServer server = getRecordServer();

		if (server != null) {
			server.record(true, getConnectedHost(), getPlaybackPort(), getIpAddress(),
					ServerConfig.DEFAULT_RECORD_PORT);
		}
		setPlaybackControlsState();
		fmch.bringToFront();
	}

	public void stopRecording() {
		try {
			RecordServer server = getRecordServer();
			if (server != null) {
				server.record(false, getConnectedHost(), getPlaybackPort(), getIpAddress(),
						ServerConfig.DEFAULT_RECORD_PORT);
			}
		} finally {
			setPlaybackControlsState();
		}
	}

	public boolean isRecordingON() {
		if (null == getRecordServer()) {
			return false;
		}
		return getRecordServer().isRecording();
	}

	public boolean isReplayON() {
		return replayON;
	}

	private void setReplayON(boolean replayON) {
		this.replayON = replayON;
	}

	public void fetchAndShowComponentTree() {
		if (commandSender == null) {
			return;
		}

		Response r = commandSender.dumpTree();
		JSONObject jo = null;
		try {
			jo = new JSONObject(r.getBody());
		} catch (JSONException e) {
			// Unparsable JSON
			e.printStackTrace();
		}
		try {
			tabularEditor.getSite().getPage()
					.showView("com.gorillalogic.monkeyconsole.componentview.ui.UIContainerView");

			UIContainerView mbv = (UIContainerView) PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findView("com.gorillalogic.monkeyconsole.componentview.ui.UIContainerView");

			mbv.setInput(jo);
		} catch (JSONException e) {
			System.out.println("DANGME: " + e.getMessage());
			// e.printStackTrace();
		} catch (PartInitException e1) {
			System.out.println("HANGME: " + e1.getMessage());
			// e1.printStackTrace();
		}
	}

	public void connectToCloudHost(String host) {
		if (!(ConnectionTypesEnum.CLOUD_ANDROID.equals(connectionType))) {
			return;
		}
		this.stopReplay();
		setCloudHost(host);
		setHost(host);
		connect(ConnectionTypesEnum.CLOUD_ANDROID);

	}

}