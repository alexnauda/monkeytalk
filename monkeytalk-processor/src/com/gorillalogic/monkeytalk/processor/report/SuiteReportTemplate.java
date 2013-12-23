package com.gorillalogic.monkeytalk.processor.report;

import java.io.IOException;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class SuiteReportTemplate extends BaseReportHelper {

	public SuiteReportTemplate(String masterName, String templateName) {
		super(masterName, templateName);
	}

	/**
	 * Load the master report template, replace all variables with the given values, and return the
	 * fully-substituted report.
	 * 
	 * @return the master report template with all variables replaced
	 * @throws IOException
	 */
	public String getContents(String tests, String padding, String name, String numTests,
			String numErrors, String numFailures, String numSkipped, String duration)
			throws IOException {
		String contents = FileUtils.readStream(getClass().getResourceAsStream(templateName));

		// replace all variables
		contents = contents.replaceAll("\\$\\{tests\\}", tests);
		contents = contents.replaceAll("\\$\\{padding\\}", padding);
		contents = contents.replaceAll("\\$\\{name\\}", name);
		contents = contents.replaceAll("\\$\\{numTests\\}", numTests);
		contents = contents.replaceAll("\\$\\{numErrors\\}", numErrors);
		contents = contents.replaceAll("\\$\\{numFailures\\}", numFailures);
		contents = contents.replaceAll("\\$\\{numSkipped\\}", numSkipped);
		contents = contents.replaceAll("\\$\\{duration\\}", duration);

		return contents;
	}

}
