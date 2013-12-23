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
package com.gorillalogic.monkeytalk.api.js.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class JSHelper {
	public static final List<String> RESERVED_WORDS = Arrays.asList("alert", "frames",
			"outerHeight", "all", "frameRate", "outerWidth", "anchor", "function", "packages",
			"anchors", "getClass", "pageXOffset", "area", "hasOwnProperty", "pageYOffset", "Array",
			"hidden", "parent", "assign", "history", "parseFloat", "blur", "image", "parseInt",
			"button", "images", "password", "checkbox", "Infinity", "pkcs11", "clearInterval",
			"isFinite", "plugin", "clearTimeout", "isNaN", "prompt", "clientInformation",
			"isPrototypeOf", "propertyIsEnum", "close", "java", "prototype", "closed", "JavaArray",
			"radio", "confirm", "JavaClass", "reset", "constructor", "JavaObject", "screenX",
			"crypto", "JavaPackage", "screenY", "Date", "innerHeight", "scroll", "decodeURI",
			"innerWidth", "secure", "decodeURIComponent", "layer", "select", "defaultStatus",
			"layers", "self", "document", "length", "setInterval", "element", "link", "setTimeout",
			"elements", "location", "status", "embed", "Math", "String", "embeds", "mimeTypes",
			"submit", "encodeURI", "name", "taint", "encodeURIComponent", "NaN", "text", "escape",
			"navigate", "textarea", "eval", "navigator", "top", "event", "Number", "toString",
			"fileUpload", "Object", "undefined", "focus", "offscreenBuffering", "unescape", "form",
			"open", "untaint", "forms", "opener", "valueOf", "frame", "option", "window",
			"abstract", "else", "instanceof", "super", "boolean", "enum", "int", "switch", "break",
			"export", "interface", "synchronized", "byte", "extends", "let", "this", "case",
			"false", "long", "throw", "catch", "final", "native", "throws", "char", "finally",
			"new", "transient", "class", "float", "null", "true", "const", "for", "package", "try",
			"continue", "function", "private", "typeof", "debugger", "goto", "protected", "var",
			"default", "if", "public", "void", "delete", "implements", "return", "volatile", "do",
			"import", "short", "while", "double", "in", "static", "with");

	/**
	 * Generate the MonkeyTalk Javascript libraries (MonkeyTalkAPI.js and MyProj.js) into the given
	 * project directory.
	 * 
	 * @param dir
	 *            the project directory
	 * @throws IOException
	 */
	public static void genAPIAndLib(File dir) throws IOException {
		genAPI(dir);
		genProjectLib(dir, "MyProj");
	}

	private static void genAPI(File dir) throws IOException {
		File libs = new File(dir, "libs");
		if (!libs.exists()) {
			libs.mkdir();
		}

		File target = new File(libs, "MonkeyTalkAPI.js");
		File api = new File("../monkeytalk-api/src");
		if (api.exists() && api.isDirectory()) {
			// regenerate directory from API
			FileUtils.writeFile(target, "tmp");
			JSAPIGenerator.main(new String[] { api.getAbsolutePath(), target.getAbsolutePath() });
		} else {
			// use bundled MonkeyTalkAPI.js instead
			copyAPI(target);
		}
	}

	private static void genProjectLib(File dir, String name) throws IOException {
		File libs = new File(dir, "libs");
		if (!libs.exists()) {
			libs.mkdir();
		}

		File target = new File(libs, name + ".js");
		JSLibGenerator.main(new String[] { dir.getAbsolutePath(), target.getAbsolutePath() });
	}

	/**
	 * Copy the bundled MonkeyTalkAPI.js into the given target file.
	 * 
	 * @param target
	 *            the MonkeyTalkAPI.js target
	 * @throws IOException
	 */
	public static void copyAPI(File target) throws IOException {
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("templates/MonkeyTalkAPI.js");
		FileUtils.writeFile(target, in);
	}

	/**
	 * Convert the given MonkeyTalk script into Javascript.
	 * 
	 * @param mt
	 *            the MonkeyTalk script
	 * @throws IOException
	 */
	public static void genJS(File mt) throws IOException {
		JSMTGenerator.main(new String[] { "MyProj", mt.getAbsolutePath() });
	}
}