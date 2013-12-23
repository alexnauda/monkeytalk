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
package com.gorillalogic.monkeytalk.runner.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.runner.Runner;

public class GlobalsRunnerTest extends BaseHelper {

	@Test
	public void testCommandlineArgs() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap\nButton ${bar} Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-Dfoo=123", "-Dbar=\"Bo Bo\"", foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button 123 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button \"Bo Bo\" Tap -> OK\n"));
		assertThat(output.toString(), containsString("result: OK"));

		assertThat(Globals.getGlobals().size(), is(2));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("Bo Bo"));
	}

	@Test
	public void testCommandlineArgsWithDeepNestedScript() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt",
				"Button 1FOO${foo} Tap\nScript bar.mt Run\nButton 2FOO${foo} Tap", dir);
		tempScript(
				"bar.mt",
				"Button 1BAR${foo} Tap\nGlobals * Set foo=234\nScript baz.mt Run\nButton 2BAR${foo} Tap",
				dir);
		tempScript("baz.mt", "Button 1BAZ${foo} Tap\nGlobals * Set foo=345\nButton 2BAZ${foo} Tap",
				dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-Dfoo=123", foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(6));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 1FOO123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button 1BAR123 Tap"));
		assertThat(server.getCommands().get(2).getCommand(), is("Button 1BAZ234 Tap"));
		assertThat(server.getCommands().get(3).getCommand(), is("Button 2BAZ345 Tap"));
		assertThat(server.getCommands().get(4).getCommand(), is("Button 2BAR345 Tap"));
		assertThat(server.getCommands().get(5).getCommand(), is("Button 2FOO345 Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button 1FOO123 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button 1BAR123 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button 1BAZ234 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button 2BAZ345 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button 2BAR345 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button 2FOO345 Tap -> OK\n"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testCommandlineArgsWithBadVarName() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-D1foo=123", foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("ERROR: illegal global variable '1foo' -- "
				+ Globals.ILLEGAL_MSG));
	}

	@Test
	public void testJavascriptWithCommandlineArgs() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-Dfoo=123", "-Dbar=\"Bo Bo\"", fooJS.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(1));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 tap \"Bo Bo\""));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button 123 tap \"Bo Bo\" -> OK"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testJavascriptWithGlobalsSet() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button ${foo} Tap ${bar}\n"
				+ "Globals * Set foo=234\nButton ${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-Dfoo=123", "-Dbar=\"Bo Bo\"", fooJS.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 tap \"Bo Bo\""));
		assertThat(server.getCommands().get(1).getCommand(), is("Button 234 tap \"Bo Bo\""));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button 123 tap \"Bo Bo\" -> OK\n"));
		assertThat(output.toString(), containsString("Globals * set foo=\"234\" -> OK\n"));
		assertThat(output.toString(), containsString("Button 234 tap \"Bo Bo\" -> OK\n"));
		assertThat(output.toString(), containsString("result: OK"));
	}

	@Test
	public void testJSCallsMTCallsJS() throws IOException {
		File dir = tempDir();
		File foo = tempScript("foo.mt", "Button FOO${foo} Tap ${bar}\n"
				+ "Globals * Set foo=234 bar=\"Bo Bo 2\"\n"
				+ "Script bar Run\nButton FOO${foo} Tap ${bar}", dir);
		File fooJS = new File(dir, "foo.js");

		tempScript("bar.mt", "Button BAR${foo} Tap ${bar}\n"
				+ "Globals * Set foo=345 bar=\"Bo Bo 3\"\n"
				+ "Script baz Run\nButton BAR${foo} Tap ${bar}", dir);

		File baz = tempScript("baz.mt", "Button BAZ${foo} Tap ${bar}\n"
				+ "Globals * Set foo=456 bar=\"Bo Bo 4\"\n" + "Button BAZ${foo} Tap ${bar}", dir);

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);
		JSHelper.genJS(baz);

		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-Dfoo=123", "-Dbar=\"Bo Bo\"", fooJS.getAbsolutePath() };

		Runner.main(args);

		List<Command> cmds = server.getCommands();
		assertThat(cmds, notNullValue());
		assertThat(cmds.size(), is(6));
		assertThat(cmds.get(0).getCommand(), is("Button FOO123 tap \"Bo Bo\""));
		assertThat(cmds.get(1).getCommand(), is("Button BAR234 Tap \"Bo Bo 2\""));
		assertThat(cmds.get(2).getCommand(), is("Button BAZ345 tap \"Bo Bo 3\""));
		assertThat(cmds.get(3).getCommand(), is("Button BAZ456 tap \"Bo Bo 4\""));
		assertThat(cmds.get(4).getCommand(), is("Button BAR456 Tap \"Bo Bo 4\""));
		assertThat(cmds.get(5).getCommand(), is("Button FOO456 tap \"Bo Bo 4\""));

		String out = output.toString();
		assertThat(out, containsString("www.gorillalogic.com"));
		assertThat(out, containsString("Button FOO123 tap \"Bo Bo\" -> OK\n"));
		assertThat(out, containsString("Globals * set foo=\"234\" -> OK\n"));
		assertThat(out, containsString("Globals * set bar=\"Bo Bo 2\" -> OK\n"));
		assertThat(out, containsString("Script bar.mt Run\n"));
		assertThat(out, containsString("Button BAR234 Tap \"Bo Bo 2\" -> OK\n"));
		assertThat(out, containsString("Globals * Set foo=345 bar=\"Bo Bo 3\" -> OK\n"));
		assertThat(out, containsString("Script baz Run\n"));
		assertThat(out, containsString("Button BAZ345 tap \"Bo Bo 3\" -> OK\n"));
		assertThat(out, containsString("Globals * set foo=\"456\" -> OK\n"));
		assertThat(out, containsString("Globals * set bar=\"Bo Bo 4\" -> OK\n"));
		assertThat(out, containsString("Button BAZ456 tap \"Bo Bo 4\" -> OK\n"));
		assertThat(out, containsString("Button BAR456 Tap \"Bo Bo 4\" -> OK\n"));
		assertThat(out, containsString("Button FOO456 tap \"Bo Bo 4\" -> OK\n"));
		assertThat(out, containsString("result: OK"));
	}

	@Test
	public void testPropertiesFile() throws IOException {
		File dir = tempDir();
		tempScript("globals.properties", "foo=123\nbar=Bo Bo", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap\nButton ${bar} Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));
		assertThat(server.getCommands().get(0).getCommand(), is("Button 123 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button 123 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button \"Bo Bo\" Tap -> OK\n"));
		assertThat(output.toString(), containsString("result: OK"));

		assertThat(Globals.getGlobals().size(), is(2));
		assertThat(Globals.getGlobal("foo"), is("123"));
		assertThat(Globals.getGlobal("bar"), is("Bo Bo"));
	}

	@Test
	public void testPropertiesFileWithBadVarName() throws IOException {
		File dir = tempDir();
		tempScript("globals.properties", "1foo=123", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(0));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(
				output.toString(),
				containsString("ERROR: globals file 'globals.properties' has illegal global variable '1foo' -- "
						+ Globals.ILLEGAL_MSG));
	}

	@Test
	public void testBothCommandlineArgsAndPropertiesFile() throws IOException {
		File dir = tempDir();
		tempScript("globals.properties", "foo=123\nbar=\"Bo Bo\"", dir);
		File foo = tempScript("foo.mt", "Button ${foo} Tap\nButton ${bar} Tap", dir);
		String[] args = { "-agent", "ios", "-port", Integer.toString(PORT), "-verbose",
				"-Dfoo=234", foo.getAbsolutePath() };

		Runner.main(args);

		assertThat(server.getCommands(), notNullValue());
		assertThat(server.getCommands().size(), is(2));

		// because the commandline arg overrides the properties file...
		assertThat(server.getCommands().get(0).getCommand(), is("Button 234 Tap"));
		assertThat(server.getCommands().get(1).getCommand(), is("Button \"Bo Bo\" Tap"));

		assertThat(output.toString(), containsString("www.gorillalogic.com"));
		assertThat(output.toString(), containsString("Button 234 Tap -> OK\n"));
		assertThat(output.toString(), containsString("Button \"Bo Bo\" Tap -> OK\n"));
		assertThat(output.toString(), containsString("result: OK"));
	}
}