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
package com.gorillalogic.fonemonkey.automators;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyScriptFailure;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 */
public class AppAutomator extends AutomatorBase {

	static {
		Log.log("Initializing AppAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_APP;
	}

	@Override
	public String play(String action, final String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_EXEC)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_EXECANDRET)) {

			// build a list of params
			List<String> params = new ArrayList<String>(Arrays.asList(args));
			if (action.equalsIgnoreCase(AutomatorConstants.ACTION_EXECANDRET)) {
				// pop off first param (returned variable name)
				params.remove(0);
			}

			// get class name and method name
			String className = getMonkeyID();
			String methodName = params.remove(0);
			String name = "Method '" + methodName + "' on Class '" + className + "'";
			System.out.println("AppAutomator: looking for " + name);

			Class<?> clazz = null;
			try {
				// find the class and method -> looking for a static method with varargs
				// ex: public static String foo(String... args)
				clazz = Class.forName(className);
				Method method = clazz.getDeclaredMethod(methodName, String[].class);
				System.out.println("AppAutomator: found '" + method.toGenericString() + "'"
						+ ", isPublic=" + Modifier.isPublic(method.getModifiers()) + ", isStatic="
						+ Modifier.isStatic(method.getModifiers()) + ", isVarArgs="
						+ method.isVarArgs() + ", returns="
						+ method.getReturnType().getSimpleName());

				// force method to be both public AND static
				if (!Modifier.isPublic(method.getModifiers())
						|| !Modifier.isStatic(method.getModifiers())) {
					throw new IllegalArgumentException(name + " must be public static, but it is "
							+ Modifier.toString(method.getModifiers() & ~Modifier.TRANSIENT));
				}

				if (method.getReturnType() != String.class && method.getReturnType() != void.class) {
					throw new IllegalArgumentException(name
							+ " must return String or void, but it returns "
							+ method.getReturnType().getSimpleName());
				}

				// invoke it
				String val = (String) method.invoke(null,
						(Object) params.toArray(new String[params.size()]));

				System.out.println("AppAutomator: val=" + val);
				return val;
			} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Class '" + className + "' not found");
			} catch (NoSuchMethodException ex) {
				if (clazz != null) {
					for (Method m : clazz.getMethods()) {
						if (m.getName().equals(methodName)) {
							throw new FoneMonkeyScriptFailure(name
									+ " must take a single varargs String param");
						}
					}
				}
				throw new IllegalArgumentException("Method '" + methodName
						+ "' not found on Class '" + className + "'");
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new FoneMonkeyScriptFailure(ex.getMessage());
			}
		}
		return null;
	}
}