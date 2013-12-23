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
package com.gorillalogic.fonemonkey.automators.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.fonemonkey.automators.AppAutomator;
import com.gorillalogic.fonemonkey.exceptions.FoneMonkeyScriptFailure;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/** Test the AppAutomator reflection and invocation magic */
public class AppAutomatorTest {

	@BeforeClass
	public static void beforeClass() {
		// set this so everything is logged to the console
		Log.setShouldPrint(true);
	}

	@Test
	public void testPlay() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		String val = app.play(AutomatorConstants.ACTION_EXEC, "foo", "arg1", "arg2");
		assertThat(val, is("foo arg1,arg2"));
	}

	@Test
	public void testPlayExecAndReturn() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		String val = app.play(AutomatorConstants.ACTION_EXECANDRET, "myvar", "foo", "arg1", "arg2");
		assertThat(val, is("foo arg1,arg2"));
	}

	@Test
	public void testPlayMissingClass() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("missing.MyClass");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		try {
			app.play(AutomatorConstants.ACTION_EXEC, "foo", "arg1", "arg2");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), is("Class 'missing.MyClass' not found"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testPlayMissingMethod() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		try {
			app.play(AutomatorConstants.ACTION_EXEC, "missing", "arg1", "arg2");
		} catch (IllegalArgumentException ex) {
			assertThat(
					ex.getMessage(),
					is("Method 'missing' not found on Class 'com.gorillalogic.fonemonkey.automators.tests.Fred'"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testPlayBadPrivateMethod() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		try {
			app.play(AutomatorConstants.ACTION_EXEC, "argsToString", "arg1", "arg2");
		} catch (FoneMonkeyScriptFailure ex) {
			assertThat(
					ex.getMessage(),
					is("Method 'argsToString' on Class 'com.gorillalogic.fonemonkey.automators.tests.Fred' must be public static, but it is private static"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testPlayBadInstanceMethod() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		try {
			app.play(AutomatorConstants.ACTION_EXEC, "bar", "arg1", "arg2");
		} catch (FoneMonkeyScriptFailure ex) {
			assertThat(
					ex.getMessage(),
					is("Method 'bar' on Class 'com.gorillalogic.fonemonkey.automators.tests.Fred' must be public static, but it is public"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testPlayBadReturn() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		try {
			app.play(AutomatorConstants.ACTION_EXEC, "baz", "arg1", "arg2");
		} catch (FoneMonkeyScriptFailure ex) {
			assertThat(
					ex.getMessage(),
					is("Method 'baz' on Class 'com.gorillalogic.fonemonkey.automators.tests.Fred' must return String or void, but it returns int"));
			return;
		}
		fail("should have thrown exception");
	}

	@Test
	public void testPlayBarParam() {
		AppAutomator app = new AppAutomator();
		app.setMonkeyID("com.gorillalogic.fonemonkey.automators.tests.Fred");
		assertThat(app.getComponentType(), is(AutomatorConstants.TYPE_APP));

		try {
			app.play(AutomatorConstants.ACTION_EXEC, "buzz", "arg1", "arg2");
		} catch (FoneMonkeyScriptFailure ex) {
			assertThat(
					ex.getMessage(),
					is("Method 'buzz' on Class 'com.gorillalogic.fonemonkey.automators.tests.Fred' must take a single varargs String param"));
			return;
		}
		fail("should have thrown exception");
	}
}