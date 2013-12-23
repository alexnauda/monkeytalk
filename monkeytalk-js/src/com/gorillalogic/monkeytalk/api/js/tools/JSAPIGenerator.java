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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;

public class JSAPIGenerator {
	/**
	 * When true, ignore all meta API sources in the {@code com.gorillalogic.monkeytalk.api.flex}
	 * package when generating the MonkeyTalkAPI.js file. NOTE: To fully ignore or un-ignore Flex,
	 * you must also see {@code MetaAPIGenerator}.
	 */
	private static final boolean IGNORE_FLEX = true;

	// Lib
	private final static String MONKEY_TALK_CORE = "MONKEY_TALK_CORE";
	private final static String CLASSES = "CLASSES";

	// Class
	private final static String CLASS_DESC = "CLASS_DESC";
	private final static String CLASS_EXTENDS = "CLASS_EXTENDS";
	private final static String CLASS_NAME = "CLASS_NAME";
	private final static String CLASS_FACTORY = "CLASS_FACTORY";
	private final static String METHODS = "METHODS";

	private final static String METHOD_NAME = "METHOD_NAME";
	private final static String METHOD_DESC = "METHOD_DESC";
	private final static String METHOD_ARGS = "METHOD_ARGS";
	private final static String METHOD_RETURN = "METHOD_RETURN";
	private final static String METHOD_RETURN_DESC = "METHOD_RETURN_DESC";
	private final static String PARAMS = "PARAMS";

	private final static String PARAM_NAME = "PARAM_NAME";
	private final static String PARAM_TYPE = "PARAM_TYPE";
	private final static String PARAM_DESC = "PARAM_DESC";

	private static final String GEN_TIME = "GEN_TIME";

	// private static final Template lib = new Template("templates/lib.template.js");
	// private static final Template core = new Template("templates/MonkeyTalkCore.js");
	// private static final Template cls = new Template("templates/class.template.js");
	// private static final Template mth = new Template("templates/method.template.js");
	// private static final Template prm = new Template("templates/param.template.js");

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public static String createAPI(File srcDir) {
		Template lib = new Template("templates/lib.template.js");
		Template core = new Template("templates/MonkeyTalkCore.js");
		Template cls = new Template("templates/class.template.js");
		Template mth = new Template("templates/method.template.js");
		Template prm = new Template("templates/param.template.js");

		lib.init();
		core.init();
		cls.init();
		mth.init();
		prm.init();

		JavaDocBuilder builder = new JavaDocBuilder();
		builder.addSourceTree(srcDir);

		HashMap<String, String[]> classMap = new HashMap<String, String[]>();
		StringBuffer classes = new StringBuffer();
		for (JavaSource src : builder.getSources()) {
			if (IGNORE_FLEX && src.getPackageName().endsWith(".flex")) {
				// for (JavaClass clazz : src.getClasses()) {
				// System.out.println("IGNORE " + clazz.getFullyQualifiedName());
				// }
				continue;
			}

			for (JavaClass clazz : src.getClasses()) {
				if (clazz.getTagByName("ignoreJS") != null) {
					continue;
				}
				System.out.println(clazz.getFullyQualifiedName());

				String name = clazz.getName();
				String extendz = (clazz.getImplements() == null
						|| clazz.getImplements().length == 0
						|| clazz.getImplements()[0].getJavaClass() == null
						|| clazz.getImplements()[0].getJavaClass().getName()
								.equalsIgnoreCase("mtobject") ? "MTObject"
						: clazz.getImplements()[0].getJavaClass().getName());
				// System.err.println(name + " extends " + extendz);

				cls.replace(CLASS_NAME, name);
				cls.replace(CLASS_DESC, cleanString(clazz.getComment()));
				cls.replace(CLASS_EXTENDS, extendz);
				cls.replace(CLASS_FACTORY, Template.lowerCamel(name));

				StringBuilder methods = new StringBuilder();
				for (JavaMethod meth : clazz.getMethods()) {
					if (meth.getTagByName("ignoreJS") != null) {
						continue;
					}
					mth.replace(CLASS_NAME, clazz.getName());
					mth.replace(METHOD_NAME, meth.getName());
					mth.replace(METHOD_DESC, cleanString(meth.getComment()));
					Type rt = meth.getReturnType();
					String rtype = rt == null ? "" : rt.getJavaClass().getName();
					mth.replace(METHOD_RETURN, rtype);
					DocletTag returns = meth.getTagByName("returns");
					String r = returns != null ? returns.getValue() : "";
					mth.replace(METHOD_RETURN_DESC, r);

					String params = addParams(meth, clazz.getName(), prm);
					mth.replace(PARAMS, params.toString());

					// Actual arg declaration
					StringBuilder arglist = new StringBuilder();
					for (JavaParameter parameter : meth.getParameters()) {
						if (arglist.length() > 0) {
							arglist.append(", ");
						}
						arglist.append(parameter.getName());
					}
					mth.replace(METHOD_ARGS, arglist.toString());
					methods.append(mth.toString());
					mth.init();
				}
				cls.replace(METHODS, methods.toString());
				classMap.put(name, new String[] { extendz, cls.toString() });
				cls.init();
			}

		}
		for (String name : classMap.keySet()) {
			writeClass(classMap, classes, name);
		}
		lib.replace(MONKEY_TALK_CORE, core.toString());
		lib.replace(CLASSES, classes.toString());
		lib.replace(GEN_TIME, sdf.format(new Date()));

		return lib.toString();
	}

	private static String cleanString(String s) {
		if (s == null) {
			return "";
		}
		return s.trim().replaceAll("\"", "'").replaceAll("\\n", "").replaceAll("\\s+", " ");
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java JSAPIGenerator <src dir> <target>");
			System.exit(1);
		}

		File srcDir = new File(args[0]);
		if (!srcDir.exists()) {
			System.err.println("ERROR: srcDir '" + args[0] + "' not found");
			System.err.println("ERROR: workingDir='" + new File(".").getAbsolutePath() + "'");
			System.exit(1);
		}
		if (!srcDir.isDirectory()) {
			System.err.println("ERROR: srcDir '" + srcDir.getAbsolutePath() + "' not directory");
			System.exit(1);
		}

		File target = new File(args[1]);
		if (!target.exists()) {
			System.err.println("ERROR: target '" + args[1] + "' not found");
			System.exit(1);
		}
		if (!target.isFile()) {
			System.err.println("ERROR: target '" + args[1] + "' not file");
			System.exit(1);
		}

		String api = createAPI(srcDir);

		try {
			FileUtils.writeFile(target.getAbsolutePath(), api);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static String addParams(JavaMethod meth, String cname, Template prm) {
		// For JSDoc params
		HashSet<String> args = new HashSet<String>();
		StringBuffer params = new StringBuffer();
		for (DocletTag param : meth.getTagsByName("param")) {
			String value = param.getValue();

			int idx = value.indexOf("\n");
			if (idx == -1) {
				throw new RuntimeException("ERROR: " + cname + "#" + meth.getName()
						+ "() - bad @param javadoc");
			}

			String pname = value.substring(0, idx);
			String pdesc = value.substring(idx + 1);

			String pnameTrimmed = pname;
			if (pnameTrimmed != null) {
				pnameTrimmed = pnameTrimmed.trim();
			}
			JavaParameter p = meth.getParameterByName(pnameTrimmed);
			if (p == null) {
				throw new RuntimeException("ERROR: " + cname + "#" + meth.getName()
						+ "() - @param \"" + pnameTrimmed + "\" has no matching method argument");
			}
			String ptype = p.getType().getJavaClass().getName();

			prm.replace(PARAM_NAME, pname);
			prm.replace(PARAM_TYPE, ptype);
			prm.replace(PARAM_DESC, cleanString(pdesc));

			params.append(prm.toString());
			prm.init();
			args.add(pname);
		}
		for (JavaParameter parameter : meth.getParameters()) {
			if (!args.contains(parameter.getName())) {
				throw new RuntimeException("Unable to find javadoc @param for actual param \""
						+ parameter.getName() + "\" in method " + cname + "#" + meth.getName());
			}
		}
		return params.toString();
	}

	private static void writeClass(HashMap<String, String[]> classMap, StringBuffer classes,
			String name) {
		String[] value = classMap.get(name);
		if (value[1] == null) {
			return;
		}
		String extendz = value[0];
		if (!extendz.equals("MTObject")) {
			writeClass(classMap, classes, extendz);
		}
		if (value[1] != null) {
			classes.append(value[1]);
			value[1] = null;
		}
	}
}