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
import java.lang.reflect.Proxy;

import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;

/**
 * Proxy handler that handles MonkeyTalk components. Responsible for pulling the component and
 * monkeyId together and passing them on to the {@link ActionProxyHandler}.
 */
public class ComponentProxyHandler extends BaseProxyHandler implements InvocationHandler {
	private ActionProxyHandler actionProxy;

	public ComponentProxyHandler(ScriptProcessor processor, Scope scope) {
		super(processor, scope);
		actionProxy = new ActionProxyHandler(processor, scope);
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		if ("raw".equals(m.getName()) && args != null && args.length > 0 && args[0] != null
				&& args[0].toString().length() > 0) {
			return play(args[0].toString());
		}

		// get the component
		String component = m.getName().substring(0, 1).toUpperCase() + m.getName().substring(1);
		String klassName = "com.gorillalogic.monkeytalk.java.api." + component;
		Class<?> klass = Class.forName(klassName);

		// get the monkeyId
		String monkeyId = "*";
		if (args != null && args.length > 0 && args[0] != null && args[0].toString().length() > 0) {
			monkeyId = args[0].toString();
		}

		actionProxy.setComponent(component);
		actionProxy.setMonkeyId(monkeyId);

		// return the action proxy
		return Proxy.newProxyInstance(klass.getClassLoader(), new Class[] { klass }, actionProxy);
	}
}
