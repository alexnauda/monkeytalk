package com.gorillalogic.monkeytalk.processor.report.detail;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TimeUtils;

public class DetailReportHtml {
	private static final String DETAIL = "/templates/DetailHtmlTemplate.html";
	private static final String ROW = "/templates/DetailRowTemplate.html";
	private static final String EXTRA = "/templates/DetailExtraTemplate.html";
	private static final String SCREENSHOTS = "/templates/DetailScreenshotsTemplate.html";
	private static final String METRICS = "/templates/DetailMetricsTemplate.html";

	/** Custom bootstrap.css with 24 cols */
	private static final int SPAN_MAX = 24;

	/** Max offset allowed (so we don't indent too far) */
	private static final int OFFSET_MAX = 15;

	private String rowTmpl;
	private String extraTmpl;
	private String screenshotsTmpl;
	private String metricsTmpl;

	public DetailReportHtml() {
	}

	/** Build the html detail report directly from the given xml detail report. */
	public String createDetailReportHtml(PlaybackResult result, String xmlReport) throws Exception {
		rowTmpl = FileUtils.readStream(getClass().getResourceAsStream(ROW));
		extraTmpl = FileUtils.readStream(getClass().getResourceAsStream(EXTRA));
		screenshotsTmpl = FileUtils.readStream(getClass().getResourceAsStream(SCREENSHOTS));
		metricsTmpl = FileUtils.readStream(getClass().getResourceAsStream(METRICS));

		String html = FileUtils.readStream(getClass().getResourceAsStream(DETAIL));

		String title = "DETAIL-" + result.getScope().getFilename() + ".html";

		SAXReader reader = new SAXReader();
		Document doc = reader.read(new StringReader(xmlReport));

		Element detail = doc.getRootElement();
		String generated = detail.attributeValue("generated", TimeUtils.formatDate(new Date()));
		String content = getContent(detail, 0);

		String agent = detail.attributeValue("agent", "<i>unknown agent</i>");
		agent = agent.replaceFirst(" - .*$", "");

		String runner = detail.attributeValue("runner", "<i>unknown runner</i>");
		runner = runner.replaceFirst(" - .*$", "");

		html = html.replace("${title}", title).replace("${title}", title); // replace title twice
		html = html.replace("${generated}", generated);
		html = html.replace("${content}", content);
		html = html.replace("${raw_xml}", xmlReport.replaceAll("<", "&lt;"));
		html = html.replace("${agent}", agent);
		html = html.replace("${runner}", runner);
		return html;
	}

	/**
	 * Helper that outputs the children of the given root element as pretty html (and then
	 * recursively outputs all of children's children as well).
	 */
	private String getContent(Element elem, int level) throws Exception {
		StringBuilder sb = new StringBuilder();

		if (elem != null) {
			// compute offset and span
			int offset = (level > OFFSET_MAX ? OFFSET_MAX : level);

			for (int i = 0; i < elem.nodeCount(); i++) {
				Node node = elem.node(i);
				if (node instanceof Element) {
					Element child = (Element) node;

					// output it
					String html = getHtmlRow(child, offset);
					if (html.length() > 0) {
						sb.append('\n').append(html).append('\n');
					}

					// output any of its child nodes via recursion
					sb.append(getContent(child, level + 1));
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Helper that populates the html row template with the given element.
	 * 
	 * @throws IOException
	 */
	private String getHtmlRow(Element elem, int offset) throws IOException {
		String comp = elem.attributeValue("comp");
		if (comp == null || comp.length() == 0) {
			return "";
		}

		// grab row template & fill in all variables
		String contents = rowTmpl;

		// compute the span
		String span = (offset > 0 ? "offset" + offset + " " : "") + "span" + (SPAN_MAX - offset);

		// monkeyId might contain quotes that need html escaping (also monkeyId must be quoted if it
		// contains a space)
		String monkeyId = elem.attributeValue("id", "").replaceAll("\"", "&quot;");
		if (monkeyId.contains(" ")) {
			monkeyId = "&quot;" + monkeyId + "&quot;";
		}

		// args might contain quotes that need html escaping
		String args = elem.attributeValue("args", "").replaceAll("\"", "&quot;");

		// compute result and resultClass
		String result = elem.attributeValue("result", "unknown");
		String resultClass = ("error".equalsIgnoreCase(result) ? "text-error" : ("failure"
				.equalsIgnoreCase(result) ? "text-warning"
				: ("ok".equalsIgnoreCase(result) ? "text-success" : "text-info")));

		String idx = elem.attributeValue("idx", "0");

		contents = contents.replaceAll("\\$\\{idx\\}", idx);
		contents = contents.replace("${comp}", comp);
		contents = contents.replace("${monkeyId}", monkeyId);
		contents = contents.replace("${action}", elem.attributeValue("action", ""));
		contents = contents.replace("${args}", args);
		contents = contents.replace("${rightSide}", getRightSide(elem));
		contents = contents.replace("${span}", span);
		contents = contents.replace("${resultClass}", resultClass);
		contents = contents.replace("${result}", result);
		contents = contents.replace("${rowClass}", getRowClass(elem));

		// screenshots & metrics
		String extra = getExtra(elem, offset + 1);
		if (extra.length() > 0) {
			contents = contents.replace("${onclick}", " onclick=\"toggle_extra('" + idx + "');\"");
			contents += extra;
		} else {
			contents = contents.replace("${onclick}", "");
		}

		return contents;
	}

	/** Helper to get the "rightSide" attributes of the row (often suite info). Returns pretty html. */
	private String getRightSide(Element elem) {
		String right = "";
		if (elem != null && "suite".equalsIgnoreCase(elem.getName())) {
			String tests = getIntAttrAsHtml(elem, "tests", "test", null);
			String errors = getIntAttrAsHtml(elem, "errors", "error", "text-error");
			String failures = getIntAttrAsHtml(elem, "failures", "failure", "text-warning");
			String skipped = getIntAttrAsHtml(elem, "skipped", "skipped", "text-info");
			String duration = (elem.attributeValue("duration") != null ? elem
					.attributeValue("duration") + "s &mdash; " : "");

			right = tests + ", " + errors + ", " + failures + ", " + skipped + " &mdash; "
					+ duration;
		} else if (elem != null) {
			right = (elem.attributeValue("duration") != null ? elem.attributeValue("duration")
					+ "s &mdash; " : "");
		}
		return right;
	}

	/**
	 * Helper to add the before/after screenshots and system metrics
	 * 
	 * @throws IOException
	 */
	private String getExtra(Element elem, int offset) {
		if (elem == null) {
			return "";
		}
		if (elem.attributeValue("beforeScreenshot") == null
				&& elem.attributeValue("afterScreenshot") == null
				&& elem.attributeValue("battery") == null) {
			return "";
		}

		String contents = extraTmpl;

		String idx = elem.attributeValue("idx", "0");
		contents = contents.replaceAll("\\$\\{idx\\}", idx);
		contents = contents.replace("${metrics}", getMetrics(elem, offset));
		contents = contents.replace("${screenshots}", getScreenshots(elem, offset));

		return "\n\n" + contents;
	}

	private String getMetrics(Element elem, int offset) {
		if (elem == null) {
			return "";
		}
		if (elem.attributeValue("battery") == null) {
			return "";
		}

		String contents = metricsTmpl;

		// compute the offset & span
		contents = contents.replace("${span}", "offset" + offset + " span" + (SPAN_MAX - offset));

		contents = contents.replace("${memory}", elem.attributeValue("memory", "??"));
		contents = contents.replace("${cpu}", elem.attributeValue("cpu", "??"));
		contents = contents.replace("${diskspace}", elem.attributeValue("diskspace", "??"));
		contents = contents.replace("${battery}", elem.attributeValue("battery", "??"));

		return "\n" + contents;
	}

	private String getScreenshots(Element elem, int offset) {
		if (elem == null) {
			return "";
		}
		if (elem.attributeValue("beforeScreenshot") == null
				&& elem.attributeValue("afterScreenshot") == null) {
			return "";
		}

		String contents = screenshotsTmpl;

		// compute the offset & span
		int span = Double.valueOf(Math.floor(0.5f * (SPAN_MAX - offset))).intValue();
		contents = contents.replace("${span1}", "offset" + offset + " span" + span);
		contents = contents.replace("${span2}", "span" + (SPAN_MAX - offset - span));

		String before = "??";
		if (elem.attributeValue("beforeScreenshot") != null) {
			before = "<img src=\"" + elem.attributeValue("beforeScreenshot")
					+ "\" title=\"before\" class=\"screenshot\" />";
		}

		String after = "??";
		if (elem.attributeValue("afterScreenshot") != null) {
			after = "<img src=\"" + elem.attributeValue("afterScreenshot")
					+ "\" title=\"after\" class=\"screenshot\" />";
		}

		contents = contents.replace("${beforeScreenshot}", before);
		contents = contents.replace("${afterScreenshot}", after);

		return "\n" + contents;
	}

	/**
	 * Helper to compute the row color -- red for error, yellow for failure, etc.
	 */
	private String getRowClass(Element elem) {
		if ("test".equalsIgnoreCase(elem.getName())) {
			// test row color reflects the result:
			// (error=red, failure=yellow, success=green, skipped=blue)
			String result = elem.attributeValue("result", "unknown");
			return ("error".equalsIgnoreCase(result) ? " pbox-error" : ("failure"
					.equalsIgnoreCase(result) ? " pbox-warning"
					: ("ok".equalsIgnoreCase(result) ? " pbox-success" : " pbox-info")));
		} else if ("suite".equalsIgnoreCase(elem.getName())) {
			// suite row color reflects the aggregate results:
			// (any errors = red, any failures = yellow, otherwise green)
			int tests = getIntAttr(elem, "tests");
			int errors = getIntAttr(elem, "errors");
			int failures = getIntAttr(elem, "failures");
			return (errors > 0 ? " pbox-error" : (failures > 0 ? " pbox-warning"
					: (tests > 0 ? " pbox-success" : "")));
		}
		return "";
	}

	/** Helper to convert an integer attribute into a pretty html span. */
	private String getIntAttrAsHtml(Element elem, String attr, String singular, String klass) {
		int i = getIntAttr(elem, attr);
		return "<span"
				+ (i == 0 || klass != null ? " class=\"" + (i == 0 ? "muted" : klass) + "\">" : ">")
				+ i + " " + (i == 1 ? singular : attr) + "</span>";
	}

	/** Helper to get an integer attribute. */
	private int getIntAttr(Element elem, String attr) {
		int i = 0;
		try {
			i = Integer.parseInt(elem.attributeValue(attr, "0"));
		} catch (NumberFormatException ex) {
			i = 0;
		}
		return i;
	}
}
