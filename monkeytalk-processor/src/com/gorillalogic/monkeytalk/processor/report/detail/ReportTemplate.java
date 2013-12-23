package com.gorillalogic.monkeytalk.processor.report.detail;

import java.util.Date;

import com.gorillalogic.monkeytalk.utils.TimeUtils;

public abstract class ReportTemplate {
	// replace it by the real path
	public static final String TEMPLATE_PATH = "/templates/";
	public static final String XML_DOCUMENT_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	protected String templateName;

	public ReportTemplate(String templateName) {
		this.templateName = templateName;
	}

	public abstract String toXML() throws Exception;
	
	public String toXMLDocument() throws Exception {
		StringBuilder sb = new StringBuilder(XML_DOCUMENT_HEADER).append('\n');
		sb.append(passablePrint(toXML()));
		return sb.toString();
	}
	
	public static String formatDate(Date date) {
		return TimeUtils.formatDateWithTimezone(date);
	}
	
	protected String escapeXml(String str) {
		return XmlUtils.escapeXml(str);
	}
	
	protected String passablePrint(String xml) {
		return XmlUtils.passablePrint(xml);
	}
}
