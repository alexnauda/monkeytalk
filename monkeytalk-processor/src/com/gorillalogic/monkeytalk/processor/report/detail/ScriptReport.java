package com.gorillalogic.monkeytalk.processor.report.detail;

import java.io.File;
import java.util.List;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.ScriptType;

public class ScriptReport extends CommandReport {

	private String tests = "0";
	private String errors = "0";
	private String failures = "0";
	private String skipped = "0";

	private ScriptType type = ScriptType.SCRIPT;

	private List<ReportTemplate> steps;

	public ScriptReport(Command command, PlaybackResult result, int stepNumber, File reportDir) {
		super(command, result, stepNumber, reportDir);
		initType();
	}

	public ScriptReport(String comp, String id, String action, String args, String mods,
			String raw, String idx, String line, String start, String stop, String duration,
			String screenshot, String result, String msg, String warning, String debug, String cpu,
			String memmory, String diskspace, String battery, String beforeScreenshot,
			String afterScreenshot, File reportDir) {
		super(comp, id, action, args, mods, raw, idx, line, start, stop, duration, screenshot,
				result, msg, warning, debug, cpu, memmory, diskspace, battery, beforeScreenshot,
				afterScreenshot, reportDir);
		initType();
	}

	private void initType() {
		if (getComp() != null) {
			if (getComp().toLowerCase().equals(ScriptType.SCRIPT.toString().toLowerCase())) {
				type = ScriptType.SCRIPT;
			} else if (getComp().toLowerCase().equals(ScriptType.SUITE.toString().toLowerCase())) {
				type = ScriptType.SUITE;
			} else if (getComp().toLowerCase().equals(ScriptType.SETUP.toString().toLowerCase())) {
				type = ScriptType.SETUP;
			} else if (getComp().toLowerCase().equals(ScriptType.TEARDOWN.toString().toLowerCase())) {
				type = ScriptType.TEARDOWN;
			} else if (getComp().toLowerCase().equals(ScriptType.TEST.toString().toLowerCase())) {
				type = ScriptType.TEST;
			}
		}
	}

	public String getAttributes() {
		StringBuilder sb = new StringBuilder(super.getAttributes());
		if (type == ScriptType.SUITE) {
			sb.append(" tests=\"" + escapeXml(tests) + "\"");
			sb.append(" errors=\"" + escapeXml(errors) + "\"");
			sb.append(" failures=\"" + escapeXml(failures) + "\"");
			sb.append(" skipped=\"" + escapeXml(skipped) + "\"");
		}

		String dataIndex = getDataIndex();
		if (dataIndex != null && dataIndex.length() > 0) {
			sb.append(" dataIndex=\"" + escapeXml(dataIndex) + "\"");
		}

		return sb.toString();
	}

	public String getDataIndex() {
		if (getAction() != null && getAction().trim().toLowerCase().contains("runwith")) {
			String a = this.getArgs();
			if (a.matches(".*\\[\\@[0-9]*\\].*")) {
				a = a.substring(a.indexOf("[@") + 2);
				a = a.substring(0, a.indexOf("]"));
				return a;
			}
		}
		return null;
	}

	@Override
	public String toXML() throws Exception {
		tagName = type.toString();
		return super.toXML();
	}

	public String getContent() {
		StringBuilder sb = new StringBuilder();
		if (steps != null) {
			for (ReportTemplate step : steps) {
				try {
					sb.append(step.toXML()).append('\n');
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			sb.append(super.getContent());
		}

		return sb.toString();
	}

	public ScriptType getType() {
		return type;
	}

	public void setType(ScriptType type) {
		this.type = type;
	}

	public List<ReportTemplate> getSteps() {
		return steps;
	}

	public void setSteps(List<ReportTemplate> steps) {
		this.steps = steps;
	}

	public String getTests() {
		return tests;
	}

	public void setTests(String tests) {
		this.tests = tests;
	}

	public String getErrors() {
		return errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}

	public String getFailures() {
		return failures;
	}

	public void setFailures(String failures) {
		this.failures = failures;
	}

	public String getSkipped() {
		return skipped;
	}

	public void setSkipped(String skipped) {
		this.skipped = skipped;
	}

}
