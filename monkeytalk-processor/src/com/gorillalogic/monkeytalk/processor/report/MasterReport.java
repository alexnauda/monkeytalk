package com.gorillalogic.monkeytalk.processor.report;

import java.io.IOException;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class MasterReport extends BaseReportHelper {

	public MasterReport(String masterName, String templateName) {
		super(masterName, templateName);
	}

	/**
	 * Load the master report template, replace all variables with the given values, and return the
	 * fully-substituted report.
	 * 
	 * @return the master report template with all variables replaced
	 * @throws IOException
	 */
	public String getMasterContents(String name, String content) throws IOException {
		String contents = FileUtils.readStream(getClass().getResourceAsStream(templateName));

		// replace all variables
		contents = contents.replaceAll("\\$\\{name\\}", name);
		contents = contents.replaceAll("\\$\\{content\\}", content);
		return contents;
	}

}
