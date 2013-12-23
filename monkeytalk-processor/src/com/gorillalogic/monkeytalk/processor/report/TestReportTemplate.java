package com.gorillalogic.monkeytalk.processor.report;

import java.io.IOException;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class TestReportTemplate extends BaseReportHelper {

	public TestReportTemplate(String masterName, String templateName) {
		super(masterName, templateName);
	}

	/**
	 * Load the master report template, replace all variables with the given values, and return the
	 * fully-substituted report.
	 * 
	 * @return the master report template with all variables replaced
	 * @throws IOException
	 */
	public String getContents(String idx, String name, String result, String duration,
			String message, String screenshot) throws IOException {
		String contents = FileUtils.readStream(getClass().getResourceAsStream(templateName));

		// replace all variables
		contents = contents.replaceAll("\\$\\{idx\\}", idx);
		contents = contents.replaceAll("\\$\\{name\\}", name);
		contents = contents.replaceAll("\\$\\{result\\}", result);
		contents = contents.replaceAll("\\$\\{duration\\}", duration);
		// if we don't have a message don't display the li
		if ("".equals(message)) {
			contents = contents.replaceAll("\\$\\{message\\}", "");
		} else {
			contents = contents.replaceAll("\\$\\{message\\}", "<li id=\"message\">" + message
					+ "<\\/li>");
		}
		contents = contents.replaceAll("\\$\\{screenshot\\}", screenshot);

		return contents;
	}

}
