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
package com.gorillalogic.monkeytalk.api.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gorillalogic.monkeytalk.api.View;

public class ViewTest {
	
	@Test
	public void testViewMethod() throws Exception {
		List<String> methodNames = new ArrayList<String>();
		
		for (Method m : View.class.getMethods()) {
			methodNames.add(m.getName());
		}
		
		assertThat(methodNames, hasItems("verify", "verifyNot", "verifyWildcard", "verifyNotWildcard", "verifyRegex", "verifyNotRegex"));
	}

	@Test
	public void testViewVerifyViaProxy() throws Exception {
		View v = getViewProxy();

		try {
			v.verify("foo", "value", "message");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), is("verify"));
			return;
		}
		fail("expected exception");
	}
	
	@Test
	public void testViewVerifyNotViaProxy() throws Exception {
		View v = getViewProxy();
		
		try {
			v.verifyNot("foo", "value", "message");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), is("verifyNot"));
			return;
		}
		fail("expected exception");
	}

	private View getViewProxy() {
		return (View) Proxy.newProxyInstance(View.class.getClassLoader(),
				new Class[] { View.class }, new InvocationHandler() {

					public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						throw new IllegalArgumentException(method.getName());
					}
				});
	}
}