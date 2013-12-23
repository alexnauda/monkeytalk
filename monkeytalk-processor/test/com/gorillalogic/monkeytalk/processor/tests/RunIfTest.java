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
package com.gorillalogic.monkeytalk.processor.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.api.js.tools.JSHelper;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class RunIfTest extends TestHelper {
	private static final String HOST = "localhost";
	private static final int PORT = 18027;
	private static String output;

	private static final PlaybackListener LISTENER_WITH_OUTPUT = new PlaybackListener() {

		@Override
		public void onStart(Scope scope) {
			output += scope.getCurrentCommand();
		}

		@Override
		public void onScriptStart(Scope scope) {
			output += (output.length() > 0 ? "\n" : "") + "START\n";
		}

		@Override
		public void onScriptComplete(Scope scope, PlaybackResult r) {
			output += "COMPLETE : " + r;
		}

		@Override
		public void onComplete(Scope scope, Response resp) {
			output += " -> " + resp + "\n";
		}

		@Override
		public void onPrint(String message) {
			output += message;
		}
	};

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	@Before
	public void before() {
		output = "";
	}

	@Test
	public void testRunIfVerify() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("foo.mt", "Script bar.mt RunIf Device * Verify iOS os", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Device * Verify iOS os", "Button BAR Tap");

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify iOS os -> OK : running bar.mt...\n"));
		assertThat(output, containsString("Script bar.mt Run\n"));
		assertThat(output, containsString("Button BAR Tap -> OK\n"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testRunIfVerifyAndModifiers() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("foo.mt",
				"Script bar.mt RunIf Device * Verify iOS os %thinktime=5000 %timeout=5000", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Device * Verify iOS os %thinktime=5000 %timeout=5000",
				"Button BAR Tap");

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify iOS os %thinktime=5000 %timeout=5000 -> OK : running bar.mt...\n"));
		assertThat(output, containsString("Script bar.mt Run %thinktime=5000 %timeout=5000\n"));
		assertThat(output, containsString("Button BAR Tap -> OK\n"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testRunIfVerifyWithError() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("foo.mt", "Script bar.mt RunIf Device * Verify Joe", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));

		server.assertCommands("Device * Verify Joe");

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify Joe -> ERROR : verify error - error on Joe\n"));
		assertThat(output, not(containsString("Button BAR Tap")));
		assertThat(output, containsString("COMPLETE : ERROR"));
	}

	@Test
	public void testRunIfVerifyWithFailure() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("foo.mt", "Script bar.mt RunIf Device * Verify Fred", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Device * Verify Fred");

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify Fred -> OK : not running bar.mt - fail on Fred\n"));
		assertThat(output, not(containsString("Button BAR Tap")));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testRunIfWithMissingVerify() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		tempScript("foo.mt", "Script bar.mt RunIf", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'Script bar.mt RunIf' must have a valid verify command as its arguments"));

		assertThat(
				output,
				containsString("START\nScript bar.mt RunIf -> ERROR : command 'Script bar.mt RunIf' must have a valid verify command as its arguments\n"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Script bar.mt RunIf' must have a valid verify command as its arguments"));
	}

	@Test
	public void testRunIfWithInvalidVerify() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Button FOO Tap\nScript bar.mt RunIf FOOBAR", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Script bar.mt RunIf FOOBAR' has invalid verify command 'FOOBAR'"));

		server.assertCommands("Button FOO Tap");

		assertThat(output, containsString("START\n"));
		assertThat(output, containsString("Button FOO Tap -> OK\n"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf FOOBAR -> ERROR : command 'Script bar.mt RunIf FOOBAR' has invalid verify command 'FOOBAR'\n"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Script bar.mt RunIf FOOBAR' has invalid verify command 'FOOBAR'"));
	}

	@Test
	public void testRunIfWithMissingScript() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Script missing.mt RunIf Device * Verify iOS os", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("script 'missing.mt' not found"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("Script missing.mt RunIf Device * Verify iOS os -> OK : running missing.mt...\n"));
		assertThat(output, containsString("Script missing.mt Run -> OK"));
		assertThat(output, containsString("COMPLETE : ERROR : script 'missing.mt' not found"));
	}

	@Test
	public void testRunIfJavascript() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		File foo = tempScript("foo.mt",
				"Button FOO1 Tap\nScript bar.mt RunIf Device * Verify iOS os\nButton FOO2 Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");\n"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {\n"));
		assertThat(js, containsString("app.button(\"FOO1\").tap();\n"));
		assertThat(
				js,
				containsString("app.bar().runIf(\"Device\", \"*\", \"Verify\", \"iOS\", \"os\");\n"));
		assertThat(js, containsString("app.button(\"FOO2\").tap();\n"));

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.js");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO1 tap", "Device * Verify iOS os", "Button BAR Tap",
				"Button FOO2 tap");

		assertThat(output, containsString("Button FOO1 tap -> OK\n"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify iOS os -> OK : running bar.mt...\n"));
		assertThat(
				output,
				containsString("Script bar.mt Run\nSTART\nButton BAR Tap -> OK\nCOMPLETE : OK -> OK\n"));
		assertThat(output, containsString("Button FOO2 tap -> OK\n"));
	}

	@Test
	public void testRunIfJavascriptAndModifiers() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		File foo = tempScript(
				"foo.mt",
				"Button FOO1 Tap\nScript bar.mt RunIf Device * Verify iOS os  %thinktime=5000 %timeout=5000\nButton FOO2 Tap",
				dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");\n"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {\n"));
		assertThat(js, containsString("app.button(\"FOO1\").tap();\n"));
		assertThat(
				js,
				containsString("app.bar().runIf(\"Device\", \"*\", \"Verify\", \"iOS\", \"os\", {thinktime:\"5000\", timeout:\"5000\"});\n"));
		assertThat(js, containsString("app.button(\"FOO2\").tap();\n"));

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.js");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO1 tap",
				"Device * Verify iOS os %thinktime=5000 %timeout=5000", "Button BAR Tap",
				"Button FOO2 tap");

		assertThat(output, containsString("Button FOO1 tap -> OK\n"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify iOS os %thinktime=5000 %timeout=5000 -> OK : running bar.mt...\n"));
		assertThat(
				output,
				containsString("Script bar.mt Run %thinktime=5000 %timeout=5000\nSTART\nButton BAR Tap -> OK\nCOMPLETE : OK -> OK\n"));
		assertThat(output, containsString("Button FOO2 tap -> OK\n"));
	}

	@Test
	public void testRunIfJavascriptWithError() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		File foo = tempScript("foo.mt",
				"Button FOO1 Tap\nScript bar.mt RunIf Device * Verify Joe\nButton FOO2 Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");\n"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {\n"));
		assertThat(js, containsString("app.button(\"FOO1\").tap();\n"));
		assertThat(js, containsString("app.bar().runIf(\"Device\", \"*\", \"Verify\", \"Joe\");\n"));
		assertThat(js, containsString("app.button(\"FOO2\").tap();\n"));

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.js");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("verify error - error on Joe"));

		server.assertCommands("Button FOO1 tap", "Device * Verify Joe");

		assertThat(output, containsString("Button FOO1 tap -> OK\n"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify Joe -> ERROR : verify error - error on Joe\n"));
	}

	@Test
	public void testRunIfJavascriptWithFailure() throws IOException {
		File dir = tempDir();
		tempScript("bar.mt", "Button BAR Tap", dir);
		File foo = tempScript("foo.mt",
				"Button FOO1 Tap\nScript bar.mt RunIf Device * Verify Fred\nButton FOO2 Tap", dir);
		File fooJS = new File(dir, "foo.js");

		JSHelper.genAPIAndLib(dir);
		JSHelper.genJS(foo);

		String js = FileUtils.readFile(fooJS);
		assertThat(js, containsString("load(\"libs/MyProj.js\");\n"));
		assertThat(js, containsString("MyProj.foo.prototype.run = function() {\n"));
		assertThat(js, containsString("app.button(\"FOO1\").tap();\n"));
		assertThat(js,
				containsString("app.bar().runIf(\"Device\", \"*\", \"Verify\", \"Fred\");\n"));
		assertThat(js, containsString("app.button(\"FOO2\").tap();\n"));

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);

		CommandServer server = new ErrorOnJoeAndFailOnFredServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.js");
		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		server.assertCommands("Button FOO1 tap", "Device * Verify Fred", "Button FOO2 tap");

		assertThat(output, containsString("Button FOO1 tap -> OK\n"));
		assertThat(
				output,
				containsString("Script bar.mt RunIf Device * Verify Fred -> OK : not running bar.mt - fail on Fred\n"));
		assertThat(output, containsString("Button FOO2 tap -> OK\n"));
	}

	private class ErrorOnJoeAndFailOnFredServer extends CommandServer {
		public ErrorOnJoeAndFailOnFredServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			Response resp = super.serve(uri, method, headers, json);

			if (json.toString().toLowerCase().contains("joe")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\",message:\"error on Joe\"}");
			} else if (json.toString().toLowerCase().contains("fred")) {
				return new Response(HttpStatus.OK, "{result:\"FAILURE\",message:\"fail on Fred\"}");
			} else if (json.optString("action", "").equalsIgnoreCase("verify")) {
				return new Response(HttpStatus.OK, "{result:\"OK\",message:\"msg\"}");
			}

			return resp;
		}
	}

	private class CommandServer extends com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer {
		public CommandServer(int port) throws IOException {
			super(port);
		}

		public void assertCommands(String... cmds) {
			assertCommands(false, cmds);
		}

		public void assertCommands(boolean showDefaultTimings, String... cmds) {
			assertThat(getCommands(), notNullValue());
			assertThat(getCommands().size(), is(cmds.length));
			for (int i = 0; i < cmds.length; i++) {
				assertThat(getCommands().get(i).getCommand(showDefaultTimings), is(cmds[i]));
			}
		}
	}
}