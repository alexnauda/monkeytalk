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
package com.gorillalogic.monkeytalk.api.doc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.gorillalogic.monkeytalk.api.meta.API;
import com.gorillalogic.monkeytalk.api.meta.Action;
import com.gorillalogic.monkeytalk.api.meta.Arg;
import com.gorillalogic.monkeytalk.api.meta.Component;
import com.gorillalogic.monkeytalk.api.meta.Property;

/**
 * Generate html doc for the API.
 */
public class APIDocGenerator {

	private static String printAPIDoc(String version) {
		String title = "MonkeyTalk API Docs" + (version != null ? " v" + version : "");

		return "<!DOCTYPE html>\n<html>\n<head>\n<title>" + title + "</title>\n" + CSS
				+ "</head>\n" + "<body>\n<h1>" + title + "</h1>\n" + printIndex()
				+ printComponents() + JS + "</body>\n</html>";
	}

	private static String CSS = "<style type=\"text/css\">\n"
			+ "ul#components { padding:0 0 8px 0; margin:0 0 0 8px; list-style:none; }\n"
			+ "div#idx { float:right; }\n"
			+ "div#idx ul { margin-right:12px; background-color:#ccc; padding:8px; list-style:none; }\n"
			+ "div#idx ul li a { color:#333; text-decoration:none; }\n"
			+ "div#idx ul li a:hover { color:#666; text-decoration:underline; }\n"
			+ "h3 { padding:0; margin:16px 0 0 0; }\n"
			+ ".desc { color:#333; padding:4px 0 6px 0; margin:0; }\n" + ".idxlbl { }\n"
			+ ".idxchk { margin-right:6px; }\n" + ".flex { display:none; }\n" + "</style>\n";

	private static String JS = "<script>\n" + "if (!document.getElementsByClassName) {\n"
			+ "  document.getElementsByClassName = function(cn) {\n"
			+ "    var allT = document.getElementsByTagName('*'), allCN=[], i=0, a;\n"
			+ "    while(a=allT[i++]) {\n" + "      a.className==cn?allCN[allCN.length]=a:null;\n"
			+ "    }\n" + "    return allCN;\n" + "  }\n" + "}\n" + "function showIt(cn) {\n"
			+ "  chk = document.getElementById(cn + 'chk');\n"
			+ "  elems = document.getElementsByClassName(cn);\n"
			+ "  for (var i=0; i<elems.length; i++) {\n" + "    var e = elems[i];\n"
			+ "    e.style.display = (chk.checked ? 'block' : 'none');\n" + "  }\n" + "}\n"
			+ "</script>\n";

	private static String IDX = "<ul>\n"
			+ "  <li>Show:</li>\n"
			+ "  <li><label class=\"idxlbl\" for=\"ioschk\"><input id=\"ioschk\" class=\"idxchk\" name=\"ioschk\" type=\"checkbox\" checked=\"checked\" disabled=\"disabled\">iOS</label></li>\n"
			+ "  <li><label class=\"idxlbl\" for=\"androidchk\"><input id=\"androidchk\" class=\"idxchk\" name=\"androidchk\" type=\"checkbox\" checked=\"checked\" disabled=\"disabled\">Android</label></li>\n"
			+ "  <li><label class=\"idxlbl\" for=\"flexchk\"><input id=\"flexchk\" class=\"idxchk\" name=\"flexchk\" type=\"checkbox\" onclick=\"showIt('flex');\">Flex</label></li>\n"
			+ "</ul>\n";

	// Checks to see if a class is a child of a class that eventually extends the given class.
	// ex. Grid -> ItemSelector -> IndexedSelector -> View
	// checkForEventualSuperClass(Grid, View) would return true.
	private static boolean checkForEventualSuperClass(Component c, String klass) {
		if (c.getSuper() != null) {
			if (c.getSuper().getName().equalsIgnoreCase(klass)) {
				return true;
			} else
				return checkForEventualSuperClass(c.getSuper(), klass);
		}
		return false;
	}

	private static String printComponents() {
		StringBuilder sb = new StringBuilder();

		List<String> viewActions = API.getComponent("view").getActionNames();
		List<String> verifyActions = API.getComponent("verifiable").getActionNames();
		viewActions.removeAll(verifyActions);

		for (Component c : API.getComponents()) {
			System.out.println(c.getName()
					+ (c.getSuper() != null ? " : " + c.getSuper().getName() : "")
					+ " eventually part of View = " + checkForEventualSuperClass(c, "view"));

			String n = c.getName();
			String klass = (n.startsWith("Flex") || n.startsWith("Spark") ? "flex" : null);

			sb.append("  ").append(printLi(n, klass)).append(printAnchor(n))
					.append(printDesc(c.getDescription())).append("<ul>\n");
			for (Action a : c.getActions()) {
				if (("view".equalsIgnoreCase(n) && !verifyActions.contains(a.getName()))
						|| ("verifiable".equalsIgnoreCase(n) || (!viewActions.contains(a.getName()) && !verifyActions
								.contains(a.getName())))) {
					sb.append("    <li>").append(printAction(c, a)).append("</li>\n");
				}
			}
			if (c.getSuper() != null && checkForEventualSuperClass(c, "verifiable")) {
				if (!c.getName().equalsIgnoreCase("view"))
					sb.append("    <li><a href=\"#View\">more actions inherited from View</a></li>\n");
				sb.append("    <li><a href=\"#Verifiable\">verify actions inherited from Verifiable</a></li>\n");
			}

			if (c.getProperties().size() > 0) {
				sb.append("    <li><i>Properties:</i><ul>\n");
				for (Property p : c.getProperties()) {
					sb.append(printProperty(p)).append('\n');
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("</ul></li>\n");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("</ul></li>\n");
		}

		return "<ul id=\"components\">\n" + sb.toString() + "</ul>\n";
	}

	private static String printIndex() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div id=\"idx\">\n");
		sb.append(API.IGNORE_FLEX ? "" : IDX);
		sb.append("<ul>\n");
		for (Component c : API.getComponents()) {
			String n = c.getName();
			String klass = (n.startsWith("Flex") || n.startsWith("Spark") ? "flex" : "");

			sb.append("  ").append(printLi(null, klass)).append("<a href=\"#").append(n)
					.append("\">").append(n).append("</a></li>\n");
		}
		sb.append("</ul>\n");
		sb.append("</div>\n");
		return sb.toString();
	}

	private static String printDesc(String desc) {
		return "<p class=\"desc\">" + desc + "</p>";
	}

	private static String printAction(Component c, Action a) {
		StringBuilder sb = new StringBuilder();
		StringBuilder args = new StringBuilder();
		for (Arg arg : a.getArgs()) {
			sb.append(sb.length() > 0 ? ", " : "").append(arg.toString(false));
			args.append(args.length() > 0 ? "\n" : "").append(printArg(arg));
		}
		return "<b><a name=\"" + c.getName() + "." + a.getName() + "\"></a>" + a.getName() + "("
				+ sb.toString() + ")" + (a.getReturnType() != null ? ":" + a.getReturnType() : "")
				+ "</b>" + (a.getDescription() != null ? " - " + a.getDescription() : "")
				+ (args.length() > 0 ? "<ul>\n" + args + "</ul>" : "");
	}

	private static String printArg(Arg a) {
		return "      <li><i>" + a.getName() + "</i>"
				+ (a.getDescription() != null ? " - " + a.getDescription() : "") + "</li>";
	}

	private static String printAnchor(String name) {
		return "<a name=\"" + name + "\"></a><h3>" + name + "</h3>";
	}

	private static String printLi(String id, String klass) {
		return "<li" + (id != null ? " id=\"" + id + "\"" : "")
				+ (klass != null ? " class=\"" + klass + "\"" : "") + ">";
	}

	private static String printProperty(Property p) {
		return "      <li><b>"
				+ p.getName()
				+ "</b>"
				+ (p.getArgs() != null && p.getArgs().length() > 0 ? "(" + p.getArgs() + ")" : "")
				+ (p.getDescription() != null && p.getDescription().length() > 0 ? " - "
						+ p.getDescription() : "") + "</li>";
	}

	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.err.println("Usage: java APIDocGenerator <output> [version]");
			System.exit(1);
		}

		File out = new File(args[0]);

		String doc = printAPIDoc(args.length == 2 ? args[1] : null);

		try {
			writeFile(out, doc);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static void writeFile(File f, String contents) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write(contents);
		out.close();
	}
}