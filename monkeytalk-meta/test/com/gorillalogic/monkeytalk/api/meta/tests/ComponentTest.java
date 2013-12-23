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
package com.gorillalogic.monkeytalk.api.meta.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

import com.gorillalogic.monkeytalk.api.meta.Component;

public class ComponentTest {

	@Test
	public void testConstructor() {
		Component c = new Component("Foo", "foo desc", null, null, null);
		assertThat(c, notNullValue());
		assertThat(c.getName(), is("Foo"));
		assertThat(c.getDescription(), is("foo desc"));
		assertThat(c.getSuper(), nullValue());
		assertThat(c.getActions(), notNullValue());
		assertThat(c.getActions().size(), is(0));
		assertThat(c.toString(), containsString("Foo - foo desc"));
	}
	
	@Test
	public void testConstructorWithParent() {
		Component c = new Component("Foo", "foo desc", "Button", null, null);
		assertThat(c, notNullValue());
		assertThat(c.getName(), is("Foo"));
		assertThat(c.getDescription(), is("foo desc"));
		assertThat(c.getSuper(), notNullValue());
		assertThat(c.getSuper().getName(), is("Button"));
		assertThat(c.getActions(), notNullValue());
		assertThat(c.getActions().size(), is(not(0)));
		assertThat(c.toString(), containsString("Foo - foo desc"));
	}
	
	@Test
	public void testConstructorWithNulls() {
		Component c = new Component(null, null, null, null, null);
		assertThat(c, notNullValue());
		assertThat(c.getName(), nullValue());
		assertThat(c.getDescription(), nullValue());
		assertThat(c.getSuper(), nullValue());
		assertThat(c.getActions(), notNullValue());
		assertThat(c.getActions().size(), is(0));
		assertThat(c.toString(), containsString("null"));
	}
}