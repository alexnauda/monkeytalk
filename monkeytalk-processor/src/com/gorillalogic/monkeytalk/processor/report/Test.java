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
package com.gorillalogic.monkeytalk.processor.report;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;

/**
 * JUnit-compatible XML report generator for TestCase.
 */
public class Test implements IReport {
	private String name;
	private TestResult result;
	private long startTime;
	private long stopTime;
	private String type;
	private String message;
	private String userdata;
	private String trace;
	private ArrayList<String> screenshots;
	private PlaybackResult playbackResult;
	private static final String SCREENSHOTS_DIR = "screenshots";

	private static final DecimalFormat decimalFmt = new DecimalFormat("0.000");
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd_HHmmss");

	/**
	 * Instantiate a new test with the given name, typically the name of the MonkeyTalk script.
	 * 
	 * @param name
	 *            the test name
	 */
	public Test(String name) {
		this.name = name;
		result = TestResult.OK;
		startTime = 0;
		stopTime = 0;
	}

	/**
	 * Get the name of the test, typically the name of the MonkeyTalk script.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the screenshot (as a base64 encoded string).
	 * 
	 * @return the screenshot
	 */
	public String getScreenshot() {
		return playbackResult.getImage();
	}

	/**
	 * Get the screenshots (as a base64 encoded strings).
	 * 
	 * @return the screenshots
	 */
	public ArrayList<String> getScreenshots() {
		return screenshots;
	}

	/**
	 * Get the screenshot filename.
	 * 
	 * @return the screenshot filename
	 */
	public String getScreenshotFilename() {
		return "screenshot_" + dateFmt.format(stopTime) + ".png";
	}

	/**
	 * Get the screenshot filename for count.
	 * 
	 * @return the screenshot filename
	 */
	public String getScreenshotFilename(int count) {
		return "screenshot_" + dateFmt.format(stopTime) + "_" + count + ".png";
	}

	/**
	 * Get the test result of a completed test.
	 * 
	 * @return the test result
	 */
	public TestResult getResult() {
		return result;
	}

	public String getUserdata() {
		return userdata;
	}

	public void setUserdata(String userdata) {
		this.userdata = userdata;
	}

	/**
	 * Start the test timer.
	 */
	public void startTimer() {
		startTime = System.currentTimeMillis();
	}

	/**
	 * Stop the test timer.
	 */
	public void stopTimer() {
		stopTime = System.currentTimeMillis();
	}

	/**
	 * Set the test result from the given {@link Command}, and {@link PlaybackResult}.
	 * 
	 * @param cmd
	 *            the MonkeyTalk command
	 * @param result
	 *            the playback result
	 */
	public void setResult(Command cmd, PlaybackResult result) {
		if (cmd != null)
			userdata = cmd.getModifiers().get("userdata");
		if (result == null) {
			this.result = TestResult.OK;
		} else {
			this.playbackResult = result;
			this.result = TestResult.getTestResultFromPlaybackStatus(result);
			message = result.getMessage();
			screenshots = result.getImages();
		}

		if (cmd != null) {
			type = cmd.getCommandName();
			trace = "  at " + cmd;
		}

		if (result != null && result.getScope() != null) {
			trace = result.getScope().getScopeTrace();
		}
	}

	/**
	 * Compute the duration (in milliseconds).
	 * 
	 * @return the duration
	 */
	public long getDuration() {
		long delta = stopTime - startTime;
		return (delta > 0 ? delta : 0);
	}

	/**
	 * Return the start time (in milliseconds).
	 * 
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Return the stop time (in milliseconds).
	 * 
	 * @return the stop time
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * Compute the duration (in seconds), format it into a string with three decimals, and return
	 * it.
	 * 
	 * @return the duration
	 */
	public String getDurationAsString() {
		return decimalFmt.format(getDuration() / 1000.0);
	}

	@Override
	public String toString() {
		return toXML("\t");
	}

	/**
	 * Output the <code>&lt;testcase></code> tag, including any child <code>&lt;error></code> or
	 * <code>&lt;failure></code> tags.
	 * 
	 * @param indent
	 *            the indent string for the {@code testcase}
	 * @return the testcase xml
	 */
	public String toXML(String indent) {
		StringBuilder sb = new StringBuilder();

		sb.append(indent).append("<testcase");
		sb.append(" name=\"").append(escapeXML(name)).append("\"");
		sb.append(" starttime=\"").append(getStartTime()).append("\"");
		sb.append(" stoptime=\"").append(getStopTime()).append("\"");
		sb.append(" time=\"").append(getDurationAsString()).append("\"");
		if (userdata != null && !userdata.trim().equalsIgnoreCase(""))
			sb.append(" userdata=\"").append(escapeXML(userdata)).append("\"");
		if (result == TestResult.OK) {
			sb.append(" />");
		} else {
			sb.append(">\n").append(indent).append(indent);
			sb.append("<" + result);
			if (result == TestResult.SKIPPED) {
				sb.append(" />");
			} else {
				if (message != null) {
					String msg = message.replaceAll("\"", "'").replaceAll("\n", ": ")
							.replaceAll("<", "(").replaceAll(">", ")");
					sb.append(" message=\"").append(escapeXML(msg)).append("\"");
				}
				if (type != null) {
					sb.append(" type=\"").append(escapeXML(type)).append("\"");
				}
				if (trace != null) {
					String msg=message!=null?message.replaceAll("\"", "'"):"";
					sb.append("><![CDATA[").append(msg).append('\n')
							.append(trace).append("]]></").append(result).append(">");
				} else {
					sb.append(" />");
				}
			}
			sb.append("\n").append(indent).append("</testcase>");
		}

		return sb.toString();
	}

	public String toHTML(int idx) {
		String htlmResult = "";
		String screenshotTemplate = "<li id=\"screenshot\">screenshot:<br /><img src=\"%1$s/%2$s\" title=\"%2$s\" /></li>";
		String screenshotText = "";
		String htmlMessage = "";

		TestReportTemplate report = new TestReportTemplate("TestHtmlTemplate.html",
				"/templates/TestHtmlTemplate.html");

		if (result == TestResult.OK && message != null) {
			htmlMessage = message;
		}
		if (result != TestResult.OK && trace != null) {
			if (message==null) {
				message="";
			}
			htmlMessage = message.replaceAll("\"", "'") + '\n' + trace;
		}

		if (hasScreenshot()) {
			screenshotText = String.format(screenshotTemplate, SCREENSHOTS_DIR,
					getScreenshotFilename());
		}

		try {
			htlmResult = report.getContents("" + idx, name, result.toString(),
					getDurationAsString(), htmlMessage, screenshotText);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return htlmResult;
	}

	private boolean hasScreenshot() {
		return playbackResult != null && playbackResult.getImageFile() != null;
	}
	
	private String escapeXML(String s) {
		return com.gorillalogic.monkeytalk.processor.report.detail.XmlUtils.escapeXml(s);
	}

}