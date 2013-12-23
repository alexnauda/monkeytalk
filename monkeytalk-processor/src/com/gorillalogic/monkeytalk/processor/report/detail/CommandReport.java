package com.gorillalogic.monkeytalk.processor.report.detail;

import java.io.File;
import java.io.IOException;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.report.TestResult;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class CommandReport extends ReportTemplate {
	protected String tagName = "cmd";
	protected String attributes = "";

	private String comp;
	private String id;
	private String action;
	private String args;
	private String mods;
	private String raw;
	private String idx;
	private String line;
	private String start;
	private String stop;
	private String duration;
	private String screenshot;
	private String result;
	private String msg;
	private String warning;
	private String debug;
	private String cpu;
	private String memory;
	private String diskspace;
	private String battery;
	private String beforeScreenshot;
	private String afterScreenshot;

	private File reportDir;

	private static final String COMMAND_NAME = "/templates/XMLReport.xml";

	public CommandReport(Command command, PlaybackResult result, int stepNumber, File reportDir) {
		this(command.getComponentType(), command.getMonkeyId(), command.getAction(),
				command.getArgsAsString(),
				new Command(command.getCommand(true)).getModifiersAsString(),
				command.getCommand(true),
				Integer.toString(stepNumber),
				null, // line in script file
				Long.toString(result.getStartTime()), Long.toString(result.getStopTime()), Double
						.toString(result.getDuration()), result.getImageFile() != null ? result
						.getImageFile().getPath() : null, TestResult
						.getTestResultFromPlaybackStatus(result).toString(), result.getMessage(),
				result.getWarning(), result.getDebug(), result.getCpu(), result.getMemory(), result
						.getDiskSpace(), result.getBattery(),
				result.getBeforeImageFile() != null ? result.getBeforeImageFile().getPath() : null,
				result.getAfterImageFile() != null ? result.getAfterImageFile().getPath() : null,
				reportDir);
	}

	public CommandReport(String comp, String id, String action, String args, String mods,
			String raw, String idx, String line, String start, String stop, String duration,
			String screenshot, String result, String msg, String warning, String debug, String cpu,
			String memory, String diskspace, String battery, String beforeScreenshot,
			String afterScreenshot, File reportDir) {
		super(COMMAND_NAME);
		this.comp = comp;
		this.id = id;
		this.action = action;
		this.args = args;
		this.mods = mods;
		this.raw = raw;
		this.idx = idx;
		this.line = line;
		this.start = start;
		this.stop = stop;
		this.duration = duration;
		this.screenshot = screenshot;
		this.result = result;
		this.msg = msg;
		this.warning = warning;
		this.debug = debug;
		this.cpu = cpu;
		this.memory = memory;
		this.diskspace = diskspace;
		this.battery = battery;
		this.beforeScreenshot = beforeScreenshot;
		this.afterScreenshot = afterScreenshot;
		this.reportDir = reportDir;
	}

	@Override
	public String toXML() throws Exception {
		String contents = FileUtils.readStream(getClass().getResourceAsStream(templateName));
		// replace all variables
		contents = contents.replace("${tag}", tagName);
		fixupScreenshotPaths(reportDir);
		contents = contents.replace("${attributes}", getAttributes());
		contents = contents.replace("${content}", getContent());
		return contents;
	}

	public String getAttributes() {
		StringBuilder sb = new StringBuilder();
		if (comp != null && comp.length() > 0) {
			sb.append(" comp=\"" + escapeXml(comp) + "\"");
		}
		if (id != null && id.length() > 0) {
			sb.append(" id=\"" + escapeXml(id) + "\"");
		}
		if (action != null && action.length() > 0) {
			sb.append(" action=\"" + escapeXml(action) + "\"");
		}
		if (args != null && args.length() > 0) {
			sb.append(" args=\"" + escapeXml(args) + "\"");
		}
		if (mods != null && mods.length() > 0) {
			sb.append(" mods=\"" + escapeXml(mods) + "\"");
		}
		if (raw != null && raw.length() > 0) {
			sb.append(" raw=\"" + escapeXml(raw) + "\"");
		}
		if (idx != null && idx.length() > 0) {
			sb.append(" idx=\"" + escapeXml(idx) + "\"");
		}
		if (line != null && line.length() > 0) {
			sb.append(" line=\"" + escapeXml(line) + "\"");
		}

		if (cpu != null && cpu.length() > 0) {
			sb.append(" cpu=\"" + escapeXml(cpu) + "\"");
		}
		if (memory != null && memory.length() > 0) {
			sb.append(" memory=\"" + escapeXml(memory) + "\"");
		}
		if (diskspace != null && diskspace.length() > 0) {
			sb.append(" diskspace=\"" + escapeXml(diskspace) + "\"");
		}
		if (battery != null && battery.length() > 0) {
			sb.append(" battery=\"" + escapeXml(battery) + "\"");
		}

		if (start != null && start.length() > 0) {
			sb.append(" start=\"" + escapeXml(start) + "\"");
		}
		if (stop != null && stop.length() > 0) {
			sb.append(" stop=\"" + escapeXml(stop) + "\"");
		}
		if (duration != null && duration.length() > 0) {
			sb.append(" duration=\"" + escapeXml(duration) + "\"");
		}
		if (screenshot != null && screenshot.length() > 0) {
			sb.append(" screenshot=\"" + escapeXml(screenshot) + "\"");
		}
		if (beforeScreenshot != null && beforeScreenshot.length() > 0) {
			sb.append(" beforeScreenshot=\"" + escapeXml(beforeScreenshot) + "\"");
		}
		if (afterScreenshot != null && afterScreenshot.length() > 0) {
			sb.append(" afterScreenshot=\"" + escapeXml(afterScreenshot) + "\"");
		}
		if (result != null && result.length() > 0) {
			sb.append(" result=\"" + escapeXml(result) + "\"");
		}
		return sb.toString();
	}

	public String getContent() {
		StringBuilder sb = new StringBuilder();
		if (msg != null && msg.length() > 0) {
			sb.append(" <msg><![CDATA[" + msg + "]]></msg> ");
		}
		if (warning != null && warning.length() > 0) {
			sb.append(" <warning><![CDATA[" + warning + "]]></warning> ");
		}
		if (debug != null && debug.length() > 0) {
			sb.append(" <debug><![CDATA[" + debug + "]]></debug> ");
		}

		return sb.toString();
	}

	/**
	 * Helper to convert the absolute paths of the screenshots to relative paths
	 * */
	private void fixupScreenshotPaths(File reportDir) {

		String screenshotPath = getScreenshot();
		if (screenshotPath != null) {
			try {
				setScreenshot(getScreenshotRelativeDir(reportDir, screenshotPath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		screenshotPath = getBeforeScreenshot();
		if (screenshotPath != null) {
			try {
				setBeforeScreenshot(getScreenshotRelativeDir(reportDir, screenshotPath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		screenshotPath = getAfterScreenshot();
		if (screenshotPath != null) {
			try {
				setAfterScreenshot(getScreenshotRelativeDir(reportDir, screenshotPath));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Helper to save the screenshots under report directory and get the relative path of the
	 * screenshot
	 */
	private String getScreenshotRelativeDir(File dir, String screenshot) throws IOException {
		// creates folder under report directory
		File currentFile = new File(screenshot);
		if (dir != null) {
			File screenshotDirectory = new File(dir, "screenshots");
			FileUtils.makeDir(screenshotDirectory, "screenshotDir");
			File newFile = new File(screenshotDirectory, currentFile.getName());
			FileUtils.copyFile(currentFile, newFile);
		}
		return "screenshots/" + currentFile.getName();
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	public String getIdx() {
		return idx;
	}

	public void setIdx(String idx) {
		this.idx = idx;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getStop() {
		return stop;
	}

	public void setStop(String stop) {
		this.stop = stop;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getScreenshot() {
		return screenshot;
	}

	public void setScreenshot(String screenshot) {
		this.screenshot = screenshot;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getDiskspace() {
		return diskspace;
	}

	public void setDiskspace(String diskspace) {
		this.diskspace = diskspace;
	}

	public String getBattery() {
		return battery;
	}

	public void setBattery(String battery) {
		this.battery = battery;
	}

	public String getBeforeScreenshot() {
		return beforeScreenshot;
	}

	public void setBeforeScreenshot(String beforeScreenshot) {
		this.beforeScreenshot = beforeScreenshot;
	}

	public String getAfterScreenshot() {
		return afterScreenshot;
	}

	public void setAfterScreenshot(String afterScreenshot) {
		this.afterScreenshot = afterScreenshot;
	}

}
