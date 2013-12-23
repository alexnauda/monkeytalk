package com.gorillalogic.monkeytalk.processor.report.detail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptType;
import com.gorillalogic.monkeytalk.processor.Step;

public class ScriptReportHelper {
	public static String getXMLDetailReportFilename(String filename) {
		return "DETAIL-" + filename + ".xml";
	}

	public static String getHTMLDetailReportFilename(String filename) {
		return "DETAIL-" + filename + ".html";
	}

	public DetailReport createDetailReport(PlaybackResult result, Scope scope, File projectDir,
			File reportDir) {
		return createDetailReport(result, scope, projectDir, reportDir, null, null);
	}

	public DetailReport createDetailReport(PlaybackResult result, Scope scope, File projectDir,
			File reportDir, String runnerVersion, String agentVersion) {
		DetailReport report = new DetailReport();
		report.setGenerated(new Date());
		report.setRunner(runnerVersion);
		report.setAgent(agentVersion);
		if (projectDir != null) {
			report.setProjectPath(projectDir.getAbsolutePath());
		}
		if (result != null) {
			ReportTemplate contents = reportScriptSteps(result, scope, reportDir);
			if (contents != null) {
				List<ReportTemplate> steps = new ArrayList<ReportTemplate>();
				steps.add(contents);
				report.setSteps(steps);
			} else {
				String errMsg = "result: \'" + result + "' produced no detail report ";
				System.out.println(errMsg);
				// report.setMessage(errMsg);
			}
		} else {
			String errMsg = "no detail report: no results were provided";
			System.out.println(errMsg);
			// report.setMessage(errMsg);
		}

		return report;
	}

	// only public for test use
	public DetailReport createDetailReport(PlaybackResult result) {
		return createDetailReport(result, null, null, null);
	}

	// only public for test use
	public ReportTemplate reportScriptSteps(PlaybackResult scriptResult) {
		return reportScriptSteps(scriptResult, null, null);
	}

	// only public for test use
	public ReportTemplate reportScriptSteps(PlaybackResult scriptResult, Scope scope, File reportDir) {
		if (scriptResult == null) {
			return null;
		}
		if (scope == null) {
			scope = scriptResult.getScope();
		}
		if (scriptResult.getSteps() == null || scriptResult.getSteps().size() == 0) {
			// this was a single-step script
			return reportSingleStepScript(scriptResult, scope, reportDir);
		} else {
			return reportSteps(new Step(createTopLevelCommand(scriptResult, scope), scriptResult,
					scope, 0), reportDir);
		}
	}

	protected Command createTopLevelCommand(PlaybackResult scriptResult, Scope scope) {
		String monkeyId = "cannotDetermineScriptName";
		String component = "Script";
		if (scope != null) {
			String filename = scope.getFilename();
			if (filename != null && filename.length() > 0) {
				monkeyId = filename;
				if (filename.toLowerCase().endsWith(CommandWorld.SUITE_EXT)) {
					component = "Suite";
				} else if (scriptResult.getStatus().equals(PlaybackStatus.ERROR)
						&& scriptResult.getMessage() != null
						&& scriptResult.getMessage().contains(filename)
						&& scriptResult.getMessage().contains(" as a suite")) {
					component = "Suite";
				}
			}
		}
		return new Command(component + " \"" + monkeyId + "\" Run");
	}

	protected ReportTemplate reportSingleStepScript(PlaybackResult scriptResult, Scope scope,
			File reportDir) {
		PlaybackResult scriptInvocationResult = new PlaybackResult(scriptResult.getStatus());
		scriptInvocationResult.setStartTime(scriptResult.getStartTime());
		scriptInvocationResult.setStopTime(scriptResult.getStopTime());
		scriptInvocationResult.setSteps(new ArrayList<Step>());

		Command topLevelCommand = createTopLevelCommand(scriptResult, scope);
		Command scriptCommand = null;
		if (scope != null) {
			scriptCommand = scope.getCurrentCommand();
		}
		if (scriptCommand == null) {
			return reportSteps(new Step(topLevelCommand, scriptResult, scope, 0), reportDir);
		} else {
			Step topLevelStep = new Step(topLevelCommand, scriptInvocationResult, scope, 0);
			scriptInvocationResult.getSteps().add(new Step(scriptCommand, scriptResult, scope, 0));
			return reportSteps(topLevelStep, reportDir);
		}
	}

	protected ReportTemplate reportSteps(Step step, File reportDir) {
		return reportSteps(step, new Counter(), reportDir);
	}

	protected ReportTemplate reportSteps(Step step, Counter idx, File reportDir) {
		ReportTemplate reportTemplate = null;
		Command command = step.getCommand();
		PlaybackResult result = step.getResult();
		if (result == null || command == null) {
			return null;
		}
		if (result.getSteps() == null || result.getSteps().size() == 0) {
			if (isScriptCommand(command)) {
				reportTemplate = new ScriptReport(command, result, idx.count++, reportDir);
			} else {
				// regular command
				reportTemplate = new CommandReport(command, result, idx.count++, reportDir);
			}
		} else {
			// Run or RunWith
			ScriptReport scriptReport = new ScriptReport(step.getCommand(), step.getResult(),
					idx.count++, reportDir);
			List<ReportTemplate> subSteps = new ArrayList<ReportTemplate>();
			scriptReport.setSteps(subSteps);
			for (Step subStep : step.getResult().getSteps()) {
				ReportTemplate report = reportSteps(subStep, idx, reportDir);
				if (report != null) {
					subSteps.add(report);
				}
				if (scriptReport.getType().equals(ScriptType.SUITE)) {
					setSuiteCounts(scriptReport, subStep, report);
				}
			}
			reportTemplate = scriptReport;
		}
		return reportTemplate;
	}

	protected boolean isScriptCommand(Command cmd) {
		String comp = cmd.getComponentType();
		if (comp != null) {
			comp = comp.toLowerCase();
			if (comp.equals(ScriptType.SCRIPT.toString())
					|| comp.equals(ScriptType.SETUP.toString())
					|| comp.equals(ScriptType.SUITE.toString())
					|| comp.equals(ScriptType.TEARDOWN.toString())
					|| comp.equals(ScriptType.TEST.toString())) {
				return true;
			}
		}
		return false;
	}

	private void setSuiteCounts(ScriptReport suiteReport, Step subStep, ReportTemplate report) {
		if (suiteReport == null || subStep == null || report == null || subStep.getResult() == null
				|| subStep.getCommand() == null) {
			return;
		}
		PlaybackStatus status = subStep.getResult().getStatus();
		try {
			// capture results
			if (status.equals(PlaybackStatus.FAILURE)) {
				suiteReport
						.setFailures(Integer.toString(Integer.parseInt(suiteReport.getFailures()) + 1));
			} else if (status.equals(PlaybackStatus.ERROR)) {
				suiteReport
						.setErrors(Integer.toString(Integer.parseInt(suiteReport.getErrors()) + 1));
			} else if (subStep.getCommand().isIgnored()) {
				suiteReport
						.setSkipped(Integer.toString(Integer.parseInt(suiteReport.getSkipped()) + 1));
			}

			// count tests
			Command command = subStep.getCommand();
			if (ScriptType.TEST.toString().toLowerCase()
					.equals(command.getComponentType().toLowerCase())) {
				if ("runwith".equals(command.getAction().toLowerCase())) {
					// "Test script.mt RunWith data.csv", e.g.
					if (command.getArgsAsString().matches(".*\\[@[0-9]+\\].*")) {
						// an actual run
						suiteReport.setTests(Integer.toString(Integer.parseInt(suiteReport
								.getTests()) + 1));
					} else {
						if (subStep.getResult().getSteps() != null
								&& subStep.getResult().getSteps().size() > 0) {
							// get the nested Tests
							CommandReport dummyReport = new CommandReport(null, null, null, null,
									null, null, null, null, null, null, null, null, null, null,
									null, null, null, null, null, null, null, null, null);
							for (Step runWithStep : subStep.getResult().getSteps()) {
								setSuiteCounts(suiteReport, runWithStep, dummyReport);
							}
						} else {
							// a Test RunWith, but no sub-steps, probably an
							// ERROR with the command
							// itself
							suiteReport.setTests(Integer.toString(Integer.parseInt(suiteReport
									.getTests()) + 1));
						}
					}
				} else {
					// regular test
					suiteReport
							.setTests(Integer.toString(Integer.parseInt(suiteReport.getTests()) + 1));
				}
			}

			// add counts from nested suites
			if (report instanceof ScriptReport) {
				ScriptReport nestedReport = (ScriptReport) report;
				String comp = nestedReport.getComp().toLowerCase();
				if (comp.equals(ScriptType.SUITE.toString().toLowerCase())) {
					setSuiteCountsFromNestedSuite(suiteReport, nestedReport);
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("SUITE report has non-integer test counts: "
					+ suiteReport.getAttributes());
		}
	}

	private void setSuiteCountsFromNestedSuite(ScriptReport suiteReport, ScriptReport nestedReport)
			throws NumberFormatException {
		suiteReport.setTests(Integer.toString(Integer.parseInt(suiteReport.getTests())
				+ Integer.parseInt(nestedReport.getTests())));
		suiteReport.setFailures(Integer.toString(Integer.parseInt(suiteReport.getFailures())
				+ Integer.parseInt(nestedReport.getFailures())));
		suiteReport.setErrors(Integer.toString(Integer.parseInt(suiteReport.getErrors())
				+ Integer.parseInt(nestedReport.getErrors())));
		suiteReport.setSkipped(Integer.toString(Integer.parseInt(suiteReport.getSkipped())
				+ Integer.parseInt(nestedReport.getSkipped())));
	}

	private static class Counter {
		public int count = 1;
	}

}
