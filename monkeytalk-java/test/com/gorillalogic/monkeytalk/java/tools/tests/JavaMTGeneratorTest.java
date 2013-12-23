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
package com.gorillalogic.monkeytalk.java.tools.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.java.tools.JavaMTGenerator;
import com.gorillalogic.monkeytalk.utils.FileUtils;

public class JavaMTGeneratorTest {

	@Test
	public void testRawGen() throws IOException {
		File dir = FileUtils.tempDir();
		File foo = new File(dir, "foo.mt");
		File out = new File(dir, "Foo.java");
		FileUtils.writeFile(foo, "Input username EnterText \"Bo Bo\"\n"
				+ "Input password EnterText pass\nButton LOGIN Tap");
		JavaMTGenerator.main(new String[] { foo.getAbsolutePath(), out.getAbsolutePath() });

		String java = FileUtils.readFile(out);
		assertThat(java, containsString("public class Foo {"));
		assertThat(java, containsString("public void testFoo() {"));
		assertThat(java, containsString("app.input(\"username\").enterText(\"Bo Bo\");"));
		assertThat(java, containsString("app.input(\"password\").enterText(\"pass\");"));
		assertThat(java, containsString("app.button(\"LOGIN\").tap();"));
	}

	@Test
	public void testGen() throws IOException {
		File dir = FileUtils.tempDir();
		File foo = new File(dir, "foo.mt");
		FileUtils.writeFile(foo, "Input username EnterText \"Bo Bo\"\n"
				+ "Input password EnterText pass\nButton LOGIN Tap");

		String java = JavaMTGenerator.genJavaTest(foo, "Foo");
		assertThat(java, not(containsString("import com.gorillalogic.monkeytalk.java.utils.Mods;")));
		assertThat(java, containsString("public class Foo {"));
		assertThat(java, containsString("public void testFoo() {"));
		assertThat(java, containsString("app.input(\"username\").enterText(\"Bo Bo\");"));
		assertThat(java, containsString("app.input(\"password\").enterText(\"pass\");"));
		assertThat(java, containsString("app.button(\"LOGIN\").tap();"));
	}

	@Test
	public void testGenWithMods() throws IOException {
		File dir = FileUtils.tempDir();
		File foo = new File(dir, "foo.mt");
		FileUtils.writeFile(foo, "Button FOO Tap %thinktime=123 %timeout=456");

		String java = JavaMTGenerator.genJavaTest(foo, "Foo");
		assertThat(java, containsString("import com.gorillalogic.monkeytalk.java.utils.Mods;"));
		assertThat(java, containsString("public class Foo {"));
		assertThat(java, containsString("public void testFoo() {"));
		assertThat(
				java,
				containsString("app.button(\"FOO\").tap(new Mods.Builder().thinktime(123).timeout(456).build());"));
	}

	@Test
	public void testSimpleCommand() throws Exception {
		assertThat(gen("Button * Tap"), is("app.button().tap();"));
		assertThat(gen("Device * Screenshot"), is("app.device().screenshot();"));
	}

	@Test
	public void testCommandWithArgs() throws Exception {
		assertThat(gen("Input username EnterText \"Bo Bo\""),
				is("app.input(\"username\").enterText(\"Bo Bo\");"));
	}

	@Test
	public void testCommandWithMultipleArgs() throws Exception {
		assertThat(gen("View * TouchMove 12 34 56 78 99 111"),
				is("app.view().touchMove(12, 34, 56, 78, 99, 111);"));
	}

	@Test
	public void testCommandWithIntArgs() throws Exception {
		assertThat(gen("View * TouchUp -12 34"), is("app.view().touchUp(-12, 34);"));
	}

	@Test
	public void testCommandWithFloatArgs() throws Exception {
		assertThat(gen("View * Pinch 123.45 -6.78"), is("app.view().pinch(123.45, -6.78);"));
	}

	@Test
	public void testCommandWithFloatArgsWithBadChars() throws Exception {
		assertThat(gen("View * Pinch 123.45fred bob-6."), is("app.view().pinch(123.45, -6.);"));
	}

	@Test
	public void testCommandWithMixedArgs() throws Exception {
		assertThat(gen("View * Pinch 123.45 -6.78 99"),
				is("app.view().pinch(123.45, -6.78, \"99\");"));
	}

	@Test
	public void testCommandWithMods() throws Exception {
		assertThat(
				gen("Button FOO Tap %thinktime=123 %timeout=456"),
				is("app.button(\"FOO\").tap(new Mods.Builder().thinktime(123).timeout(456).build());"));
	}

	@Test
	public void testCommandWithArgsAndMods() throws Exception {
		assertThat(
				gen("Input username EnterText \"Bo Bo\" %thinktime=123 %timeout=456 %ignore=true %screenshotonerror=false"),
				is("app.input(\"username\").enterText(\"Bo Bo\", new Mods.Builder().ignore(true).screenshotonerror(false).thinktime(123).timeout(456).build());"));
	}

	private String gen(String cmd) throws Exception {
		Method m = JavaMTGenerator.class.getDeclaredMethod("genCommand", Command.class);
		m.setAccessible(true);
		return m.invoke(null, new Command(cmd)).toString();
	}
}
