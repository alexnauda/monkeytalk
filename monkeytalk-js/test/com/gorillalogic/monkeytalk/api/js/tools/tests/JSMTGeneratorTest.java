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
package com.gorillalogic.monkeytalk.api.js.tools.tests;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.js.tools.JSMTGenerator;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class JSMTGeneratorTest extends TestHelper {

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Test
	public void testScriptGen() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button OK Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"OK\").tap();"));
		assertThat(js, not(containsString("$")));
	}

	@Test
	public void testScriptGenWithArgs() throws IOException {
		File dir = tempDir();
		File bar = tempScript("bar.mt", "Button OK Tap a \"b c\"", dir);

		String js = JSMTGenerator.createScript("myproj", bar);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.bar.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"OK\").tap(\"a\", \"b c\");"));
	}

	@Test
	public void testScriptGenWithModifiers() throws IOException {
		File dir = tempDir();
		File baz = tempScript("baz.mt", "Button OK Tap %foo=123 %bar=654", dir);

		String js = JSMTGenerator.createScript("myproj", baz);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.baz.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"OK\").tap({foo:\"123\", bar:\"654\"});"));
	}

	@Test
	public void testScriptGenWithMutlilineScript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\nButton BAR Tap\nButton BAZ Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));
		assertThat(js, containsString("app.button(\"BAR\").tap();"));
		assertThat(js, containsString("app.button(\"BAZ\").tap();"));
	}

	@Test
	public void testScriptGenWithGet() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("var foo = app.button(\"FOO\").get(\"foo\");"));
	}

	@Test
	public void testScriptGenWithGetAndUsingTheGottenVariable() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo\nInput name EnterText ${foo}", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("var foo = app.button(\"FOO\").get(\"foo\");"));
		assertThat(js, containsString("app.input(\"name\").enterText(foo);"));
	}

	@Test
	public void testUseReservedVariable() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Input name EnterText ${this}", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.input(\"name\").enterText(_this);"));
	}

	@Test
	public void testPercentArgs() throws IOException {
		File dir = tempDir();
		File foo = tempScript(
				"foo.mt",
				"Input name EnterText %{1}\nInput name EnterText \"%{monkeyId} or %{somethingelse}\"",
				dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("enterText(arguments[0]);"));
		assertThat(js, containsString("this.monkeyId + \" or \" + \"somethingelse\")"));
	}

	@Test
	public void testVarsVerify() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Verify\nVars * Verify foo=1 bar=2", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("NOT SUPPORTED YET"));
	}

	@Test
	public void testVarsDefineReservedKeyword() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define in this=foo", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function(_in, _this)"));
		assertThat(js, containsString("_this != undefined"));
		assertThat(js, containsString("_in != undefined"));
	}

	@Test
	public void testScriptGenWithGetWithoutVar() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("\tapp.button(\"FOO\").get();"));
	}

	@Test
	public void testScriptGenWithGetWithProperty() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo someProp", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("var foo = app.button(\"FOO\").get(\"foo\", \"someProp\");"));
	}

	@Test
	public void testScriptGenWithGetWithNativeDotProperty() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo .numStars", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("var foo = app.button(\"FOO\").get(\"foo\", \".numStars\");"));
	}

	@Test
	public void testScriptGenWithGetWithPropertyAndModifiers() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Get foo someProp %foo=123 %bar=654", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(
				js,
				containsString("var foo = app.button(\"FOO\").get(\"foo\", \"someProp\", {foo:\"123\", bar:\"654\"});"));
	}

	@Test
	public void testScriptGenWithVarsDefine() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first=Joe last=\"Bo Bo\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}\n"
				+ "Button SAVE Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("myproj.foo.prototype.run = function(first, last)"));
		assertThat(js,
				containsString("first = (first != undefined && first != \"*\" ? first : \"Joe\");"));
		assertThat(js,
				containsString("last = (last != undefined && last != \"*\" ? last : \"Bo Bo\");"));
		assertThat(js, containsString("app.input(\"firstName\").enterText(first);"));
		assertThat(js, containsString("app.input(\"lastName\").enterText(last);"));
		assertThat(js, containsString("app.button(\"SAVE\").tap();"));
	}

	@Test
	public void testScriptGenWithVarsDefineWithoutDefaults() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Vars * Define first last\n"
				+ "Input name EnterText \"-${first}--${last}---\"", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function(first, last)"));
		assertThat(
				js,
				containsString("first = (first != undefined && first != \"*\" ? first : \"<first>\");"));
		assertThat(js,
				containsString("last = (last != undefined && last != \"*\" ? last : \"<last>\");"));
		assertThat(
				js,
				containsString("app.input(\"name\").enterText(\"-\" + first + \"--\" + last + \"---\");"));
	}

	@Test
	public void testScriptGenWithComments() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "#\n# some comment\n#\nButton OK Click\n# more comment",
				dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.button(\"OK\").click();"));
		assertThat(js, not(containsString("#")));
	}

	@Test
	public void testScriptGenWithCustomCommand() throws IOException {
		File dir = tempDir();
		File foo = tempScript("comp.foo.mt", "Vars * Define first=Joe last=\"Bo Bo\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}\n"
				+ "Button SAVE Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.comp.prototype.foo = function(first, last)"));
		assertThat(js,
				containsString("first = (first != undefined && first != \"*\" ? first : \"Joe\");"));
		assertThat(js,
				containsString("last = (last != undefined && last != \"*\" ? last : \"Bo Bo\");"));
		assertThat(js, containsString("app.input(\"firstName\").enterText(first);"));
		assertThat(js, containsString("app.input(\"lastName\").enterText(last);"));
		assertThat(js, containsString("app.button(\"SAVE\").tap();"));
	}

	@Test
	public void testScriptGenWithCustomCommandAndBlanksAndComments() throws IOException {
		File dir = tempDir();
		File foo = tempScript("comp.foo.mt",
				"# add a contact to the addressbook\nVars * Define first=Joe last=\"Bo Bo\"\n"
						+ "Input firstName EnterText ${first}\n"
						+ "Input lastName EnterText ${last}\n" + "# now save it\n"
						+ "Button SAVE Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.comp.prototype.foo = function(first, last)"));
		assertThat(js,
				containsString("first = (first != undefined && first != \"*\" ? first : \"Joe\");"));
		assertThat(js,
				containsString("last = (last != undefined && last != \"*\" ? last : \"Bo Bo\");"));
		assertThat(js, containsString("app.input(\"firstName\").enterText(first);"));
		assertThat(js, containsString("app.input(\"lastName\").enterText(last);"));
		assertThat(js, containsString("app.button(\"SAVE\").tap();"));
	}

	@Test
	public void testScriptGenWithUppercaseCustomCommand() throws IOException {
		File dir = tempDir();
		File foo = tempScript("COMP.FOO.mt", "Vars * Define first=Joe last=\"Bo Bo\"\n"
				+ "Input firstName EnterText ${first}\n" + "Input lastName EnterText ${last}\n"
				+ "Button SAVE Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.COMP.prototype.FOO = function(first, last)"));
		assertThat(js,
				containsString("first = (first != undefined && first != \"*\" ? first : \"Joe\");"));
		assertThat(js,
				containsString("last = (last != undefined && last != \"*\" ? last : \"Bo Bo\");"));
		assertThat(js, containsString("app.input(\"firstName\").enterText(first);"));
		assertThat(js, containsString("app.input(\"lastName\").enterText(last);"));
		assertThat(js, containsString("app.button(\"SAVE\").tap();"));
	}

	@Test
	public void testScriptGenWithComment() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "# some comment", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("// some comment"));
	}

	@Test
	public void testScriptGenWithStars() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO Tap\n* BAR Tap\n* * Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("app.button(\"FOO\").tap();"));
		assertThat(js, containsString("app.view(\"BAR\").tap();"));
		assertThat(js, containsString("app.view().tap();"));
	}

	@Test
	public void testScriptGenWithDebugPrint() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button FOO Tap\nDebug * Print foo bar baz\nButton BAR Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");\n"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function() {\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));
		assertThat(js, containsString("app.debug().print(\"foo\", \"bar\", \"baz\");\n"));
		assertThat(js, containsString("app.button(\"BAR\").tap();\n"));
	}

	@Test
	public void testScriptGenWithDebugVars() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo=123 bar=\"Bo Bo\"\nDebug * Vars\nButton FOO Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");\n"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function(foo, bar) {\n"));
		assertThat(js,
				containsString("foo = (foo != undefined && foo != \"*\" ? foo : \"123\");\n"));
		assertThat(js,
				containsString("bar = (bar != undefined && bar != \"*\" ? bar : \"Bo Bo\");\n"));
		assertThat(
				js,
				containsString("app.debug().print(\"foo=\" + foo + \"\\n\" + \"bar=\" + bar + \"\\n\");\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));
	}

	@Test
	public void testScriptGenWithDebugVarsAndMultipleVarDefines() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Vars * Define foo=123 bar=\"Bo Bo\"\nDebug * Vars\nButton FOO Tap\n"
						+ "Vars * Define baz=abc\nDebug * Vars", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");\n"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function(foo, bar, baz) {"));
		assertThat(js,
				containsString("foo = (foo != undefined && foo != \"*\" ? foo : \"123\");\n"));
		assertThat(js,
				containsString("bar = (bar != undefined && bar != \"*\" ? bar : \"Bo Bo\");\n"));
		assertThat(
				js,
				containsString("app.debug().print(\"foo=\" + foo + \"\\n\" + \"bar=\" + bar + \"\\n\");\n"));
		assertThat(js,
				containsString("baz = (baz != undefined && baz != \"*\" ? baz : \"abc\");\n"));
		assertThat(
				js,
				containsString("app.debug().print(\"foo=\" + foo + \"\\n\" + \"bar=\" + bar + \"\\n\" + \"baz=\" + baz + \"\\n\");\n"));
		assertThat(js, containsString("app.button(\"FOO\").tap();\n"));
	}

	@Test
	public void testScriptGenWithGlobals() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Globals * Set foobar=123\nButton ${foobar} Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.globals().set('foobar=\"123\"');"));
		assertThat(js, containsString("foobar = '123';"));
		assertThat(js, containsString("app.button(foobar).tap();"));
	}

	@Test
	public void testScriptGenWithDoubleQuotedGlobals() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Globals * Set foobar=123 bar=\"Bo Bo\"\nButton ${foobar} Tap ${bar}", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.globals().set('foobar=\"123\"');"));
		assertThat(js, containsString("foobar = '123';"));
		assertThat(js, containsString("app.globals().set('bar=\"Bo Bo\"');"));
		assertThat(js, containsString("bar = 'Bo Bo';"));
		assertThat(js, containsString("app.button(foobar).tap(bar);"));
	}

	@Test
	public void testScriptGenWithSingleQuotedGlobals() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Globals * Set foobar=isn't\nButton ${foobar} Tap", dir);

		String js = JSMTGenerator.createScript("myproj", foo);
		assertThat(js, containsString("load(\"libs/myproj.js\");"));
		assertThat(js, containsString("var app = this.app;"));
		assertThat(js, containsString("myproj.foo.prototype.run = function()"));
		assertThat(js, containsString("app.globals().set('foobar=\"isn\\'t\"');"));
		assertThat(js, containsString("foobar = 'isn\\'t';"));
		assertThat(js, containsString("app.button(foobar).tap();"));
	}
}