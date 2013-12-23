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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.utils.Base64;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * JUnit-compatible XML report generator.
 */
public class Report {
	private static final String SCREENSHOTS_DIR = "screenshots";

	private Suite mainSuite;
	private Test currTest;
	private File reportFile;
	private File reportDirectory;

	/**
	 * Instantiate a new report with the given suite name.
	 * 
	 * @param name
	 *            the suite name
	 */
	public Report(String name) {
		mainSuite = new Suite(name);
	}

	/**
	 * Instantiate a new report with the given mainSuite.
	 * 
	 * @param mainSuite
	 *            the main suite
	 */
	public Report(Suite mainSuite) {
		this.mainSuite = mainSuite;
	}

	/**
	 * Get the suite.
	 * 
	 * @return the suite
	 */
	public Suite getMainSuite() {
		return mainSuite;
	}

	/**
	 * Get the current test.
	 */
	public Test getCurrentTest() {
		return currTest;
	}

	/**
	 * Get the saved report file. Valid only after {@link Report#saveReport(File)} has been called.
	 * 
	 * @return the report file
	 */
	public File getReportFile() {
		return reportFile;
	}

	/**
	 * Start a test with the given MonkeyTalk {@code Test} command (and any args). For example,
	 * {@code Test foo.mt Run} starts test {@code foo.mt}.
	 * 
	 * @param cmd
	 *            the MonkeyTalk test command
	 */
	public void startTest(Command cmd) {
		currTest = new Test(getName(cmd));
		currTest.startTimer();
		currTest.stopTimer();
		mainSuite.addTest(currTest);
	}

	/**
	 * Start a new test (and start the test timer) from the given MonkeyTalk test command and a row
	 * of test data.
	 * 
	 * @param cmd
	 *            the MonkeyTalk test command
	 * @param datum
	 *            the test data
	 */
	public void startTest(Command cmd, Map<String, String> datum) {
		currTest = new Test(getName(cmd, datum));
		currTest.startTimer();
		currTest.stopTimer();
		mainSuite.addTest(currTest);
	}

	/**
	 * Stop the current test (and stop its timer) and set the test result from given the given
	 * MonkeyTalk test command and its playback result.
	 * 
	 * @see Test#setResult(Command, PlaybackResult)
	 * 
	 * @param cmd
	 *            the MonkeyTalk test command
	 * @param result
	 *            the playback result
	 */
	public void stopTest(Command cmd, PlaybackResult result) {
		if (currTest != null) {
			currTest.stopTimer();
			currTest.setResult(cmd, result);
			currTest = null;
		}
	}

	/**
	 * Save the suite report to the given folder. Saves both the JUnit-compatible XML report and the
	 * more user friendly HTML report. Also, saves any screenshots to the {@code screenshots} child
	 * folder.
	 * 
	 * @param reportDir
	 *            the report folder
	 * @throws IOException
	 */
	public void saveReport(File reportDir) throws IOException {
		FileUtils.makeDir(reportDir, "reportDir");
		reportDirectory = reportDir;

		String name = "TEST-" + mainSuite.getName();

		// write the XML report
		reportFile = new File(reportDir, name + ".xml");
		FileUtils.writeFile(reportFile, mainSuite.toXML());

		// write the HTML report
		FileUtils.writeFile(new File(reportDir, name + ".html"), mainSuite.toHTML());

		// save all screenshots
		if (mainSuite.hasScreenshots()) {
			saveScreenshots(new File(reportDir, SCREENSHOTS_DIR));
		}
	}

	private void saveErrorScreenshot(File dir, Test t) throws IOException {
		// Save and add error screenshot to screenshots.html
		if (t.getScreenshot() != null) {
			String screenshot = t.getScreenshot();
			if (screenshot != null) {
				File f = new File(dir, t.getScreenshotFilename());

				// Save on error screenshot to /reports/screenshots
				Base64.decodeToFile(screenshot, f.getAbsolutePath());
			}
		}
	}

	private void saveScreenshots(File dir) throws IOException {
		File directory = new File(reportDirectory.getParent(), SCREENSHOTS_DIR);
		// FileUtils.makeDir(directory, SCREENSHOTS_DIR);
		FileUtils.makeDir(dir, "screenshotDir");
		ArrayList<String> screenshots = new ArrayList<String>();

		for (IReport ro : mainSuite.getTests()) {
			if (ro instanceof Test) {
				Test t = (Test) ro;

				// Save and add screenshots to screenshots.heml
				if (t.getScreenshots() != null && t.getScreenshots().size() > 0) {
					for (String screenshot : t.getScreenshots()) {
						if (screenshot != null) {
							// Add screenshot to html
							screenshots.add(screenshot);
						}
					}
				}

				// Save screenshot for report
				saveErrorScreenshot(dir, t);
			} else if (ro instanceof Suite) {
				Suite s = (Suite) ro;

				for (IReport r : s.getTests()) {
					if (r instanceof Test) {
						Test t = (Test) r;

						// Save screenshot for report
						saveErrorScreenshot(dir, t);
					}
				}
			}
		}

		if (!directory.exists())
			return;
	}

	public void saveScreenshotsToHTML(ArrayList<String> screenshots, File dir) throws IOException {
		if (screenshots == null || screenshots.size()==0) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<!doctype html>\n<html lang=\"en\">\n<head>\n");
		sb.append("<title>").append(getMainSuite().getName()).append(" Screenshots</title>\n");
		sb.append("<meta charset=\"UTF-8\" />\n");

		// Lightbox ToDo: Save lightbox to gl server and access it from there
		// sb.append("<script src=\"http://lokeshdhakar.com/projects/lightbox2/js/jquery-1.7.2.min.js\"></script>\n");
		// sb.append("<script src=\"http://lokeshdhakar.com/projects/lightbox2/js/lightbox.js\"></script>\n");
		// sb.append("<link href=\"http://lokeshdhakar.com/projects/lightbox2/css/lightbox.css\" rel=\"stylesheet\" />\n");

		sb.append("<style type=\"text/css\">\n");
		sb.append("body { font-family: Cambria, Georgia, serif; font-size:14pt; margin:0;\n");
		sb.append("padding:20px; background-color:#eee; width:900px; margin-left:auto;\n");
		sb.append("margin-right:auto; }\n");
		sb.append("ul { list-style-type: none; padding:0px; margin-top:5px; margin-bottom:5px;\n");
		sb.append("height:300px; overflow-x: scroll; overflow-y:hidden; white-space:nowrap;\n");
		sb.append("padding-top:5px; padding-bottom:5px; border-top:1px solid #555;\n");
		sb.append("border-bottom:1px solid #555; }\n");
		sb.append("li { display:inline; padding-right:10px; }\n");
		sb.append("</style>\n");

		sb.append("</head>\n<body>\n");

		sb.append(getMainSuite().getName() + " Screenshots\n");
		sb.append("<ul>\n");

		// String name = "SCREENSHOTS-" + mainSuite.getName();
		String name = "screenshots";

		for (String screenshot : screenshots) {
			sb.append("<li><a rel=\"lightbox\" href=\"" + screenshot + "\">");
			sb.append("<img height=\"300px\" src=\"" + screenshot + "\" title=\"" + screenshot
					+ "\" /></a></li>\n");
		}

		sb.append("</ul>\n</body>\n</html>");

		if (!dir.exists()) {
			dir.mkdirs();
			if (!dir.exists()) {
				System.err.println("canot create screenshots directory at " + dir.getAbsolutePath());;
			}
		}
		
		// write the screenshot to screenshots.html
		FileUtils.writeFile(new File(dir, name + ".html"), sb.toString());
	}

	@Override
	public String toString() {
		return mainSuite.toString();
	}

	/**
	 * Helper to get the name of a test given the MonkeyTalk test command (and any args).
	 * 
	 * @param cmd
	 *            the MonkeyTalk test command
	 * @return the name of the test
	 */
	private String getName(Command cmd) {
		if (cmd == null) {
			return null;
		}
		String name = cmd.getMonkeyId();
		if (cmd.getArgs().size() > 0) {
			name += "[" + cmd.getArgsAsString().replaceAll("\"", "'") + "]";
		}
		return name;
	}

	/**
	 * Helper to get the name of a test given the MonkeyTalk test command and row of test data.
	 * 
	 * @param cmd
	 *            the MonkeyTalk test command
	 * @param datum
	 *            the test data
	 * @return the name of the test
	 */
	private String getName(Command cmd, Map<String, String> datum) {
		if (cmd == null) {
			return null;
		}

		String name = cmd.getMonkeyId();
		if (datum == null) {
			name += "[null]";
		} else if (datum.size() == 0) {
			name += "[empty]";
		} else {
			StringBuilder sb = new StringBuilder();
			for (String key : datum.keySet()) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(key).append("='").append(datum.get(key)).append("'");
			}
			name += "[" + sb.toString() + "]";
		}
		return name;
	}
}