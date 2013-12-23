package com.gorillalogic.monkeytalk.processor.report.detail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TimeUtils;

public class DetailReport extends ReportTemplate {

	public DetailReport() {
		super(TEMPLATE_NAME);
	}

	private String generated;
	private String agent;
	private String runner;
	private String timeout;
	private String thinkTime;
	private String projectPath;

	private List<ReportTemplate> steps;
	private static final String TEMPLATE_NAME = "/templates/XMLReport.xml";
	private static final String TAG_NAME = "detail";

	public String toXMLDocument() throws Exception {
		StringBuilder sb = new StringBuilder(ReportTemplate.XML_DOCUMENT_HEADER).append('\n');
		sb.append(XmlUtils.passablePrint(toXML()));
		//System.out.println("===========================");
		//System.out.println(sb.toString());
		return sb.toString();
	}
		
	public String toXML() throws Exception {

		String contents = FileUtils.readStream(getClass().getResourceAsStream(TEMPLATE_NAME));
		// replace all variables
		contents = contents.replace("${tag}", TAG_NAME);
		contents = contents.replace("${attributes}", getAttributes());
		contents = contents.replace("${content}", getContent());

		return contents;
	}

	public String getContent() {
		StringBuilder sb = new StringBuilder();
		for (ReportTemplate step : steps) {
			try {
				sb.append(step.toXML());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public String getAttributes() {
		StringBuilder sb = new StringBuilder();
		if (generated != null && generated.length()>0) {
			sb.append(" generated=\"" + StringEscapeUtils.escapeXml(generated) + "\"");
		}
		if (projectPath != null && projectPath.length()>0) {
			sb.append(" projectPath=\"" + StringEscapeUtils.escapeXml(projectPath) + "\"");
		}
		if (agent != null && agent.length()>0) {
			sb.append(" agent=\"" + StringEscapeUtils.escapeXml(agent) + "\"");
		}
		if (runner != null && runner.length()>0) {
			sb.append(" runner=\"" + StringEscapeUtils.escapeXml(runner) + "\"");
		}
		if (timeout != null && timeout.length()>0) {
			sb.append(" timeout=\"" + StringEscapeUtils.escapeXml(timeout) + "\"");
		}
		if (thinkTime != null && thinkTime.length()>0) {
			sb.append(" thinkTime=\"" + StringEscapeUtils.escapeXml(thinkTime) + "\"");
		}
		return sb.toString();
	}

	public String getGenerated() {
		return generated;
	}

	public void setGenerated(String generated) {
		this.generated = generated;
	}

	public void setGenerated(Date date) {
		this.generated = TimeUtils.formatDateWithTimezone(date);
	}

	public String getAgent() {
		return agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public String getRunner() {
		return runner;
	}

	public void setRunner(String runner) {
		this.runner = runner;
	}

	public String getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getThinkTime() {
		return thinkTime;
	}

	public void setThinkTime(String thinkTime) {
		this.thinkTime = thinkTime;
	}

	public List<ReportTemplate> getSteps() {
		return steps;
	}

	public void setSteps(List<ReportTemplate> steps) {
		this.steps = steps;
	}

	public static String getTemplateName() {
		return TEMPLATE_NAME;
	}

	public static String getTagName() {
		return TAG_NAME;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

}
