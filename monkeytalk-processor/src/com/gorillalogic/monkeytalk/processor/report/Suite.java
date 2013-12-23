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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.utils.FileUtils;

/**
 * JUnit-compatible XML report generator for TestSuite.
 */
public class Suite implements IReport {
	private String name;
	private List<IReport> tests;
	private static final DecimalFormat decimalFmt = new DecimalFormat("0.000");
	private final String MASTER_NAME = "MTHtmlTemplate.html";
	private final String TEMPLATE_NAME = "/templates/MTHtmlTemplate.html";

	private int count = 1;

	/**
	 * Instantiate a new suite with the given name.
	 * 
	 * @param name
	 *            the suite name
	 */
	public Suite(String name) {
		this.name = name;
		tests = new ArrayList<IReport>();
	}

	/**
	 * Get the suite name minus the .mts extension.
	 * 
	 * @return the suite name
	 */
	public String getName() {
		return getName(true);
	}

	/**
	 * Get the suite name minus the .mts extension.
	 * 
	 * @return the suite name
	 */
	public String getName(boolean removeExtension) {
		if (name == null) {
			return "";
		}

		return (removeExtension ? FileUtils.removeExt(name, CommandWorld.SUITE_EXT) : name);
	}

	/**
	 * Compute the duration (in seconds) of the suite by summing the duration of all child tests.
	 * 
	 * @return the duration (in seconds) formatted into a string with three decimals places
	 */
	public String getDuration() {
		long sum = 0;
		for (IReport t : tests) {
			if (t instanceof Test)
				sum += ((Test) t).getDuration();
			else if (t instanceof Suite) {
				Suite s = (Suite) t;

				for (IReport te : s.tests) {
					if (te instanceof Test)
						sum += ((Test) te).getDuration();
				}
			}
		}
		return decimalFmt.format(sum / 1000.0);
	}

	/**
	 * Finds the start time of the first test. If there are no tests, return 0
	 * 
	 * @return the time, in ms of the first test's begining
	 */
	public long getFirstStartTime() {
		long first = Long.MAX_VALUE;
		for (IReport t : tests) {
			if (t instanceof Test) {
				if (((Test) t).getStartTime() < first) {
					first = ((Test) t).getStartTime();
				}
			} else if (t instanceof Suite) {
				Suite s = (Suite) t;
				if (s.getFirstStartTime() < first) {
					first = s.getFirstStartTime();
				}
			}
		}
		if (first == Long.MAX_VALUE)
			first = 0;
		return first;
	}

	/**
	 * Finds the stop time of the last test. If there are no tests, return 0
	 * 
	 * @return the time, in ms of the last test's end
	 */
	public long getLastStopTime() {
		long last = 0;
		for (IReport t : tests) {
			if (t instanceof Test) {
				if (((Test) t).getStopTime() > last) {
					last = ((Test) t).getStopTime();
				}
			} else if (t instanceof Suite) {
				Suite s = (Suite) t;
				if (s.getLastStopTime() > last) {
					last = s.getLastStopTime();
				}
			}
		}
		return last;
	}

	public String getTimestamp() {
		long unixtime = getFirstStartTime();
		Date time = new Date(unixtime);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return format.format(time);
	}

	/**
	 * Get the list of all tests in the suite.
	 * 
	 * @return the tests
	 */
	public List<IReport> getTests() {
		return tests;
	}

	/**
	 * Add a test to the suite.
	 * 
	 * @param t
	 *            the test
	 */
	public void addTest(Test t) {
		tests.add(t);
	}

	/**
	 * Add a test to the suite.
	 * 
	 * @param t
	 *            the test
	 */
	public void addSuite(Suite t) {
		tests.add(t);
	}

	/**
	 * Get a test in the suite by name.
	 * 
	 * @param name
	 *            the test name
	 * @return the test, or null if no match is found
	 */
	public Test getTest(String name) {
		if (name != null) {
			for (IReport t : tests) {
				if (t instanceof Test && t.getName().equals(name)) {
					return (Test) t;
				}
			}
		}
		return null;
	}

	/**
	 * True if one or more of the tests in this suite have a screenshot, otherwise false.
	 * 
	 * @return true if the suite has any screenshots
	 */
	public boolean hasScreenshots() {
		for (IReport r : tests) {
			if (r instanceof Test
			        && (((Test) r).getScreenshot() != null || ((Test) r).getScreenshots() != null
			                && ((Test) r).getScreenshots().size() > 0)) {
				return true;
			} else if (r instanceof Suite) {
				for (IReport ro : ((Suite) r).tests) {
					if (ro instanceof Test
					        && (((Test) ro).getScreenshot() != null || ((Test) ro).getScreenshots() != null
					                && ((Test) ro).getScreenshots().size() > 0)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return toXML("\t", this, "");
	}

	public String toXML() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" + toXML("\t", this, "");
	}

	/**
	 * Output the <code>&lt;testsuite></code> tag with attributes for the number of tests, errors,
	 * failures, etc., plus all child <code>&lt;testcase></code> tags.
	 * 
	 * @param indent
	 *            the indent string for the {@code testcase}
	 * @return the test as xml
	 */
	public String toXML(String indent) {
		return toXML(indent, this, "");
	}

	public String toXML(String indentAmount, Suite s, String currentIndent) {
		StringBuilder sb = new StringBuilder();
		sb.append(currentIndent + "<testsuite ");
		if (name != null) {
			sb.append("name=\"").append(escapeXML(s.getName())).append("\" ");
		}

		int numTests = 0;
		int numErrors = 0;
		int numFailures = 0;
		int numSkipped = 0;
		int numSuites = 0;

		StringBuilder out = new StringBuilder();
		for (IReport ro : s.tests) {
			if (ro instanceof Suite) {
				String innerSuite = toXML(indentAmount, ((Suite) ro), currentIndent + indentAmount);
				// Roll up counts to parent
				numTests += getValueOfFirstAttributeByName("tests", innerSuite);
				numErrors += getValueOfFirstAttributeByName("errors", innerSuite);
				numFailures += getValueOfFirstAttributeByName("failures", innerSuite);
				numSkipped += getValueOfFirstAttributeByName("skipped", innerSuite);
				numSuites += getValueOfFirstAttributeByName("suites", innerSuite);

				out.append(innerSuite);

				out.append("\n");
				numSuites++;
			} else {
				Test t = (Test) ro;
				numTests++;
				if (t.getResult() == TestResult.ERROR) {
					numErrors++;
				} else if (t.getResult() == TestResult.FAILURE) {
					numFailures++;
				} else if (t.getResult() == TestResult.SKIPPED) {
					numSkipped++;
				}
				out.append(t.toXML(indentAmount + currentIndent)).append("\n");
			}

		}

		sb.append("tests=\"").append(numTests).append("\" ");
		sb.append("suites=\"").append(numSuites).append("\" ");
		sb.append("errors=\"").append(numErrors).append("\" ");
		sb.append("failures=\"").append(numFailures).append("\" ");
		sb.append("skipped=\"").append(numSkipped).append("\" ");
		sb.append("starttime=\"").append(getFirstStartTime()).append("\" ");
		sb.append("stoptime=\"").append(getLastStopTime()).append("\" ");
		sb.append("timestamp=\"").append(getTimestamp()).append("\" ");
		sb.append("time=\"").append(getDuration()).append("\">\n");

		sb.append(out).append(currentIndent + "</testsuite>");
		return sb.toString();
	}

	public String toHTML() {
		String html = null;
		count = 1;
		MasterReport masterReport = new MasterReport(MASTER_NAME, TEMPLATE_NAME);
		try {
			html = masterReport.getMasterContents(getName(false),
			        toHTML(new StringBuilder(), this, 0, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return html;
	}

	public String toHTML(StringBuilder sb, Suite suitero, int padding, String duration) {
		int numTests = 0;
		int numErrors = 0;
		int numFailures = 0;
		int numSkipped = 0;

		StringBuilder out = new StringBuilder();
		String result = "";

		for (IReport ro : suitero.getTests()) {
			if (ro instanceof Suite) {
				Suite s = (Suite) ro;
				out.append("<li>\n");

				for (IReport r : s.getTests()) {
					if (r instanceof Test) {
						Test t = (Test) r;
						numTests++;
						if (t.getResult() == TestResult.ERROR) {
							numErrors++;
						} else if (t.getResult() == TestResult.FAILURE) {
							numFailures++;
						} else if (t.getResult() == TestResult.SKIPPED) {
							numSkipped++;
						}
					}
				}

				out.append(toHTML(out, s, padding + 50, s.getDuration()));
				out.append("</li>\n");
			} else {
				Test t = (Test) ro;
				numTests++;
				if (t.getResult() == TestResult.ERROR) {
					numErrors++;
				} else if (t.getResult() == TestResult.FAILURE) {
					numFailures++;
				} else if (t.getResult() == TestResult.SKIPPED) {
					numSkipped++;
				}
				out.append(((Test) t).toHTML(count));
				count++;
			}

		}

		if (duration == null) {
			duration = getDuration();
		}

		SuiteReportTemplate template = new SuiteReportTemplate("SuiteHtmlTemplate.html",
		        "/templates/SuiteHtmlTemplate.html");

		try {
			result = template.getContents(out.toString(), "" + padding, suitero.getName(false), ""
			        + numTests, "" + numErrors, "" + numFailures, "" + numSkipped, "" + duration);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

	public int getValueOfFirstAttributeByName(String name, String xmlString) {
		Pattern pattern = Pattern.compile("( +" + name + "=\")([0-9]*)");
		Matcher matcher = pattern.matcher(xmlString);
		matcher.find();
		if (matcher.group(2).trim().length() == 0)
			return 0;
		return Integer.parseInt(matcher.group(2));
	}
	
	private String escapeXML(String s) {
		return com.gorillalogic.monkeytalk.processor.report.detail.XmlUtils.escapeXml(s);
	}
}