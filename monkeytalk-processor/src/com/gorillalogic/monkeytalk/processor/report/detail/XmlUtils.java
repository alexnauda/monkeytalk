package com.gorillalogic.monkeytalk.processor.report.detail;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class XmlUtils {

	/**
	 * Pretty-prints xml, supplied as a string.
	 * <p/>
	 * eg.
	 * <code>
	 * String formattedXml = XmlUtil.format("<tag><nested>hello</nested></tag>");
	 * </code>
	 */
	public static String format(String xml) {

		try {
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
			final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));
			
			//May need this: System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
			
			
			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();
			
			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.
			
			return writer.writeToString(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String escapeXml(String str) {
		return StringEscapeUtils.escapeXml(str);
	}
	
	public static void main(String[] args) {
		String unformattedXml =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><QueryMessage\n" +
						"        xmlns=\"http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message\"\n" +
						"        xmlns:query=\"http://www.SDMX.org/resources/SDMXML/schemas/v2_0/query\">\n" +
						"    <Query>\n" +
						"        <query:CategorySchemeWhere>\n" +
						"   \t\t\t\t\t         <query:AgencyID>ECB\n\n\n\n</query:AgencyID>\n" +
						"        </query:CategorySchemeWhere>\n" +
						"    </Query>\n\n\n\n\n" +
						"</QueryMessage>";

		System.out.println(XmlUtils.format(unformattedXml));
	}
	
	/** prints "all-tag" XML with nested indentation but no other "niceties" 
	 *  took this approach to avoid format()'s re-ordering of attributes
	 * 
	 */
	public static String passablePrint(String xml) {
		if (xml==null || xml.length()==0) {
			return xml;
		}
		StringBuilder sb = new StringBuilder();
		String state="NOT_IN_TAG";
		int level = 0;
		String indent = "    ";
		String cdataOpenSig = "![CDATA[";
		StringBuilder cdataOpen = new StringBuilder();
		for (int i=0; i<xml.length(); i++) {
			char c = xml.charAt(i);
			
			if (state.equals("CDATA_CLOSE_PENDING")) {
				sb.append(c);
				if (c=='>') {
					state="NOT_IN_TAG";
				} else if (c!=']') {
					state="IN_CDATA";
				}
				continue;
			}
			
			if (state.equals("CDATA_OPEN_PENDING")) {
				cdataOpen.append(c);
				if (!cdataOpenSig.equals(cdataOpen.toString())) {
					sb.append(cdataOpen.toString());
					cdataOpen.setLength(0);
					state="IN_CDATA";
				} else if (!(cdataOpenSig.startsWith(cdataOpen.toString()))) {
					// not a match
					sb.append(cdataOpen.toString());
					cdataOpen.setLength(0);
					state="IN_TAG";
				}
				continue;
			}
			
			if (state.equals("IN_CDATA")) {
				sb.append(c);
				if (c==']') {
					state="CDATA_CLOSE_PENDING";
				}
				continue;
			} 
			
			if (state.equals("TAG_PENDING")) {
				if (c=='/') {
					state="IN_CLOSE_TAG";
					sb.append("\n");
					level--;
					for (int j=0; j<level; j++) {
						sb.append(indent);
					}
					sb.append("</");
				} else if (c=='!') {
					state="CDATA_OPEN_PENDING";
					sb.append('<');
					cdataOpen.append(c);
					continue;
				} else {
					state="IN_TAG";
					sb.append("\n");
					for (int j=0; j<level; j++) {
						sb.append(indent);
					}
					level++;
					sb.append("<");
					sb.append(c);
				}
				continue;
			}
			if (c=='<') {
				state="TAG_PENDING";
				continue;
			}
			
			if (c=='>') {
				sb.append(c);
				state="NOT_IN_TAG";
				continue;
			}

			if (state=="IN_TAG" || state=="IN_CLOSE_TAG") {
				sb.append(c);
				continue;
			}
			
			if (state=="NOT_IN_TAG") {
				// skip
				continue;
			}
			
			System.err.println("what about me, boss? '" + c + "'   state is: " + state + " at offset " + i);
		}
		return sb.toString();
		//return XmlUtils.format(xml);
	}

}