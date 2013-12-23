/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.java.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;

/**
 * Proxy handler that handles MonkeyTalk actions. Responsible for pulling all the parts together
 * (component, action, args, mods) and sending them to the player.
 */
public class ActionProxyHandler extends BaseProxyHandler implements InvocationHandler {
	private String component;
	private String monkeyId;

	public ActionProxyHandler(ScriptProcessor processor, Scope scope) {
		super(processor, scope);
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public void setMonkeyId(String monkeyId) {
		this.monkeyId = monkeyId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		// extract args & mods from call
		List<String> arguments = new ArrayList<String>();
		Map<String, String> modifiers = new HashMap<String, String>();
		if (args != null && args.length > 0) {
			int i = 0;
			for (Class<?> klass : m.getParameterTypes()) {
				if (i == (args.length - 1) && "Map".equals(klass.getSimpleName())) {
					// if last arg is a map, assume it is the modifiers map
					modifiers = (Map<String, String>) args[i];
				} else if (args[i] instanceof Float) {
					// float arg, so strip any trailing zeros
					String arg = args[i].toString().replaceFirst("\\.0+$", "");
					arguments.add(arg);
				} else if (args[i] instanceof int[]) {
					// arg is an array (happens for vararg methods like View.touchMove)
					for (int arg : (int[]) args[i]) {
						arguments.add("" + arg);
					}
				} else if (args[i] instanceof String[]) {
					// arg is an array (happens for vararg methods like App.Exec)
					for (String arg : (String[]) args[i]) {
						arguments.add(arg);
					}
				} else {
					// just a vanilla arg
					String arg = args[i].toString();
					arguments.add(arg);
				}
				i++;
			}
		}

		// for Get, just insert 'dummy' variable as first arg
		if (isGet(m.getName())) {
			arguments.add(0, "dummy");
		}

		return play(component, monkeyId, m.getName(), arguments, modifiers);
	}
}
