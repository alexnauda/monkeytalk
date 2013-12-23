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
package com.gorillalogic.monkeytalk.processor.command.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.processor.command.VerifyImage;
import com.gorillalogic.monkeytalk.server.JsonServer;
import com.gorillalogic.monkeytalk.utils.Base64;
import com.gorillalogic.monkeytalk.utils.ImageUtils;

public class VerifyImageCommandTest extends BaseCommandHelper {

	File scriptDir;

	@Before
	public void before2() throws IOException {
		this.scriptDir = new File(tempDir(), this.getClass().getSimpleName() + "_scripts");
		if (scriptDir.exists()) {
			scriptDir.delete();
		}
		scriptDir.mkdirs();
	}

	@Test
	public void testWithMissingExpected() throws IOException {
		tempScript("foo.mt", "Device * VerifyImage", scriptDir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, scriptDir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Device * VerifyImage' must have a file path as its first arg"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Device * VerifyImage' must have a file path as its first arg"));
	}

	@Test
	public void testWithDirectoryAsExpected() throws IOException {
		String directoryName = "testVerifyWithDirectoryAsExpected_directory";
		File directory = new File(scriptDir, directoryName);
		directory.mkdirs();
		tempScript("foo.mt", "Device * VerifyImage " + directoryName, scriptDir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, scriptDir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("command 'Device * VerifyImage " + directoryName
				+ "' - expectedImageFile '" + directoryName
				+ "' is not a regular file, perhaps a folder?"));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("COMPLETE : ERROR : command 'Device * VerifyImage "
				+ directoryName + "' - expectedImageFile '" + directoryName
				+ "' is not a regular file, perhaps a folder?"));
	}

	@Test
	public void testWithAbsoluteAsExpected() throws IOException {
		String absoluteName = "/testWithAbsoluteAsExpected_directory/1/2/3/banana.png";
		tempScript("foo.mt", "Device * VerifyImage " + absoluteName, scriptDir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, scriptDir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'Device * VerifyImage "
						+ absoluteName
						+ "' - expectedImageFile '"
						+ absoluteName
						+ "' is an absolute path reference"
						+ "' - the expected image file path must be specified relative to the project directory"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Device * VerifyImage "
						+ absoluteName
						+ "' - expectedImageFile '"
						+ absoluteName
						+ "' is an absolute path reference"
						+ "' - the expected image file path must be specified relative to the project directory"));
	}

	@Test
	public void testWithBadTolerance() throws IOException {
		doTestWithBadTolerance("rewrewrew");
		doTestWithBadTolerance("-1");
		doTestWithBadTolerance("-32876");
		doTestWithBadTolerance("256");
		doTestWithBadTolerance("25632");
		doTestWithBadTolerance("256323398219038209182098312");
		doTestWithBadTolerance("-256323398219038209182098312");
		doTestWithBadTolerance("16.8328");
		doTestWithBadTolerance("-116.8328");
		doTestWithBadTolerance("-0.8328");
		doTestWithBadTolerance(".8328");
		doTestWithBadTolerance("047AFE");
	}

	private void doTestWithBadTolerance(String badTolerance) throws IOException {
		tempScript("foo.mt", "Device * VerifyImage dummy.file " + badTolerance, scriptDir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, scriptDir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("command 'Device * VerifyImage dummy.file "
				+ badTolerance + "' - tolerance '" + badTolerance
				+ "' is invalid: must be an integer" + " between " + ImageUtils.MIN_TOLERANCE
				+ " and " + ImageUtils.MAX_TOLERANCE));

		assertThat(output, containsString("START"));
		assertThat(output,
				containsString("COMPLETE : ERROR : command 'Device * VerifyImage dummy.file "
						+ badTolerance + "' - tolerance '" + badTolerance
						+ "' is invalid: must be an integer" + " between "
						+ ImageUtils.MIN_TOLERANCE + " and " + ImageUtils.MAX_TOLERANCE));
	}

	@Test
	public void testWithAgentError() throws IOException {
		doTestWithErrorResponse("Device * VerifyImage testWithAgentError.png", " - SOME ERROR");
	}

	@Test
	public void testWithEmptyAgentMessage() throws IOException {
		doTestWithErrorResponse("Device * VerifyImage testWithEmptyAgentMessage.png",
				" - no message from agent");
	}

	@Test
	public void testWithNullAgentMessage() throws IOException {
		doTestWithErrorResponse("Device * VerifyImage testWithEmptyAgentMessageOk.png",
				" - no message from agent");
		doTestWithErrorResponse("Device * VerifyImage testWithEmptyAgentMessageError.png",
				" - no message from agent");
	}

	@Test
	public void testWithFailedScreenshot() throws IOException {
		doTestWithErrorResponse("Device * VerifyImage testWithFailedScreenshot.png",
				" - screenshot could not be taken");
	}

	@Test
	public void testWithMissingScreenshot() throws IOException {
		doTestWithOkResponse(
				"Device * VerifyImage testWithMissingScreenshot.png",
				" - file '"
						+ scriptDir.getPath()
						+ "/testWithMissingScreenshot.png' was not found, creating it with the just-captured image");
		doTestWithOkResponse(
				"Device * VerifyImage testWithMissingImage.png",
				" - file '"
						+ scriptDir.getPath()
						+ "/testWithMissingImage.png' was not found, creating it with the just-captured image");
		doTestWithErrorResponse("Device * VerifyImage testWithMissingScreenshotAndImage.png",
				" - no screenshot received");
	}

	@Test
	public void testWithInvalidScreenshot() throws IOException {
		doTestWithErrorResponse("Device * VerifyImage testWithInvalidScreenshot.png",
				" - error cropping image");
	}

	@Test
	public void testWithUnparseableCropBounds() throws IOException {
		PlaybackResult result = doTestWithOkResponse(
				"Device * VerifyImage testWithUnparseableCropBounds.png",
				" - file '"
						+ scriptDir.getPath()
						+ "/testWithUnparseableCropBounds.png' was not found, creating it with the just-captured image");

		assertThat(result.getWarning(),
				containsString("could not parse component rectangle from this string: "));
	}

	@Test
	public void testWithFailedComparison() throws IOException {
		byte[] fff = Base64.decode(GOOD_SCREENSHOT_STRING);
		FileUtils.writeByteArrayToFile(new File(scriptDir, "GOODSCREENSHOT2.PNG"), fff);

		PlaybackResult result = doTestWithFailureResponse(
				"Device * VerifyImage GOODSCREENSHOT2.PNG",
				" - expected and captured images do not match.");
		assertNull(result.getWarning());
	}

	@Test
	public void testWithSuccessfulComparison() throws IOException {
		byte[] fff = Base64.decode(GOOD_SCREENSHOT_STRING);
		FileUtils.writeByteArrayToFile(new File(scriptDir, "GOODSCREENSHOT.PNG"), fff);

		String cmd = "Device * VerifyImage GOODSCREENSHOT.PNG";
		PlaybackResult result = doTestWithOkResponse(cmd, null);
		assertThat(result.getMessage(), is(""));
	}

	@Test
	public void testInvalidComponent() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Fred * VerifyImage fred.png", dir);
		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		CommandServer server = new CommandServer(PORT);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		PlaybackResult result = processor.runScript("foo.mt");

		server.stop();

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Fred * VerifyImage fred.png"));
		// assertThat(output, containsString("Unrecognized component type: Fred"));
	}

	public PlaybackResult doTestWithFailureResponse(String script, String message)
			throws IOException {
		return doTestWithAgentResponse(script, message, PlaybackStatus.FAILURE);
	}

	public PlaybackResult doTestWithOkResponse(String script, String message) throws IOException {
		return doTestWithAgentResponse(script, message, PlaybackStatus.OK);
	}

	public PlaybackResult doTestWithErrorResponse(String script, String errormessage)
			throws IOException {
		return doTestWithAgentResponse(script, errormessage, PlaybackStatus.ERROR);
	}

	public PlaybackResult doTestWithAgentResponse(String script, String errormessage,
			PlaybackStatus expectedStatus) throws IOException {

		String host = "localhost";
		int port = 5432;
		// tempScript("foo.mt", script, scriptDir);
		Command command = new Command(script);

		ScriptProcessor processor = new ScriptProcessor(host, port, scriptDir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);

		VerifyImageServer agentMock = new VerifyImageServer(port);
		PlaybackResult result = null;
		try {
			Scope scope = new Scope();
			scope.setCurrentCommand(command);
			result = new VerifyImage(command, scope, LISTENER_WITH_OUTPUT, processor, scriptDir)
					.verifyImage();
		} finally {
			agentMock.stop();
		}

		assertThat("FAIL: " + result, result.getStatus(), is(expectedStatus));

		if (errormessage != null) {
			String _errormessage = "command '" + script + "'" + errormessage;
			assertThat(result.getMessage(), is(_errormessage));
			assertThat(output, containsString(expectedStatus.name() + " : " + _errormessage));

		}

		assertThat(agentMock.getCommands().toString(), containsString(script));

		return result;
	}

	public static class CommandServer extends JsonServer {
		private List<Command> commands;
		private List<String> jsons;

		public CommandServer(int port) throws IOException {
			super(port);
			commands = new ArrayList<Command>();
			jsons = new ArrayList<String>();
		}

		/**
		 * Get the list of all captured MonkeyTalk commands.
		 * 
		 * @return the commands
		 */
		public List<Command> getCommands() {
			return commands;
		}

		/**
		 * Get the list of all captured MonkeyTalk commands in their raw JSON format.
		 * 
		 * @return the commands
		 */
		public List<String> getRawJSONCommands() {
			return jsons;
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {

			jsons.add(json.toString());
			Command cmd = new Command(json);

			// ignore blank commands (aka PINGs)
			if (!"* * *".equals(cmd.toString())) {
				commands.add(new Command(json));
			}

			return new Response(HttpStatus.OK, "{result:\"OK\"}");
		}
	}

	private static class VerifyImageServer extends CommandServer {
		VerifyImageServer(int port) throws IOException {
			super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			super.serve(uri, method, headers, json);
			if (json.toString().contains("AgentError")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\",message=\"SOME ERROR\"}");

			} else if (json.toString().contains("FailedScreenshot")) {
				return new Response(HttpStatus.OK,
						"{result:\"ERROR\",message=\"screenshot could not be taken\"}");

			} else if (json.toString().contains("EmptyAgentMessage")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\",message=\"\"}");

			} else if (json.toString().contains("NullAgentMessageOk")) {
				return new Response(HttpStatus.OK, "{result:\"OK\"}");

			} else if (json.toString().contains("NullAgentMessageError")) {
				return new Response(HttpStatus.OK, "{result:\"ERROR\"}");

			} else if (json.toString().contains("MissingImage")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"2 2 20 20\"," + "\"screenshot\"=\""
						+ GOOD_SCREENSHOT_STRING + "\"}");

			} else if (json.toString().contains("MissingScreenshotAndImage")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"2 2 400 500\"}");

			} else if (json.toString().contains("MissingScreenshot")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"2 2 20 20\"," + "\"image\"=\"" + GOOD_SCREENSHOT_STRING
						+ "\"}");
			} else if (json.toString().contains("InvalidScreenshot")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"2 2 20 20\","
						+ "\"screenshot\"=\"iVBORw0KGg48i8JgP8PcEwFnYsg4J4AAAAASUVORK5CYII=\","
						+ "\"image\"=\"iVBORw0KGg48i8JgP8PcEwFnYsg4J4AAAAASUVORK5CYII=\"}");
			} else if (json.toString().contains("UnparseableCropBounds")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"this_is_a_bunch_of crap\"," + "\"screenshot\"=\""
						+ GOOD_SCREENSHOT_STRING + "\"," + "\"image\"=\"" + GOOD_SCREENSHOT_STRING
						+ "\"}");
			} else if (json.toString().contains("GOODSCREENSHOT.PNG")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"0 0 50 22\"," + "\"screenshot\"=\""
						+ GOOD_SCREENSHOT_STRING + "\"," + "\"image\"=\"" + GOOD_SCREENSHOT_STRING
						+ "\"}");
			} else if (json.toString().contains("GOODSCREENSHOT2.PNG")) {
				return new Response(HttpStatus.OK, "{\"result\":\"OK\","
						+ "\"message\":\"2 2 20 20\"," + "\"screenshot\"=\""
						+ GOOD_SCREENSHOT_STRING2 + "\"," + "\"image\"=\""
						+ GOOD_SCREENSHOT_STRING2 + "\"}");
			}
			return new Response(HttpStatus.OK, "{\"result\":\"OK\"," + "\"message\":\"2 2 20 20\","
					+ "\"screenshot\"=\"" + GOOD_SCREENSHOT_STRING + "\"," + "\"image\"=\""
					+ GOOD_SCREENSHOT_STRING + "\"}");
		}
	}

	private static final String GOOD_SCREENSHOT_STRING = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAWCAYAAACCAs"
			+ "+RAAADOUlEQVR42u2WSSh1YRjHr8zEgitDxsTCRpQMCSULWZDuwkISiVAUF1lYYKEsxEqKrEh"
			+ "KptzFpdQ1JFaIopQxw0YyZvh//Z+v83b18X26Vr7Ov06d9/Se93l+z3SOAf+JDDqIDqKDfA3k6ekJx8fHeH5"
			+ "+xsPDw88EGRwchNFoRElJCQYGBhAXF/czQdLT01FXVycPfixIZWUlfH19ERMTg87Ozj9ALi8vYTKZ4O/"
			+ "vj6ioKNnz8vKCq6srpKWlYWtrS/ZdXFzIenR0VL1bWlqKiYkJud/b20NeXh5CQ0ORm5uL1dVVtW9qagr0Y3"
			+ "JyEvHx8djd3cXZ2RnMZrPYjI2NRXt7O97e3j4HWVxcFIiKigpsbGy8A+GLPJiGbTYbxsbGBIiHUtHR0ejr65N"
			+ "7OmwwGFBUVCTru7s7uLq6Ynt7W6D9/PyQn5+P2dlZNDc3w9vbG5ubm6oKgoOD5eyamhrZX11djYKCAiwtLaG3"
			+ "txeBgYEYGRn5e2klJSUph+xBZmZmxDlmRVN/fz98fHwEsqqqSjne0NCAjIwMhIWFyXp+fh4hISFy39raioiIi"
			+ "HcRzc7ORn19vbJJOwykJoL39PSo9dzcnFwOgXR0dEi27LW+vi5GT05OJAvh4eHyPCUlRcDd3d1xeHiItrY2K"
			+ "S0qJydHbHR1dakrMzNTnms2WXL24uBhRplFBu/09PTfzf4ZCCPGyNmLUSPI0dERrq+v4ezsjP39fSmV29tbyQ"
			+ "pLICsrS5VCYmIiEhISpFzsr+7u7k8HzOvrKywWC4qLixEQEAAXFxcpS4dAmFoOAja3fWl5eHioZ6mpqaitr"
			+ "UVycrKsmYny8nJ4eXmpkiwsLFTR17S8vCx99xHIzc0NhoeHBYbit43DgFl0COTg4EDSq0X28fFRHOdg0E"
			+ "TH6XRjY6OsFxYW4OnpKVnQND4+LvBWq1VBsHnp7Ecg7KXIyEj5vmkBo82ysjLHQKihoSFxlBOKTc6S4VTR"
			+ "xKnCUpuenpb1/f093Nzc0NLS8s4QQZ2cnKRM2EdNTU2q+T8qLdrlOUFBQdI/HO07Ozvf+9diqtfW1uQX5jt"
			+ "iAFZWVuSb89X9zN75+bn+96uD6CA6yG/9ApyxZm12V7NxAAAAAElFTkSuQmCC";

	private static final String GOOD_SCREENSHOT_STRING2 = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAdCAIAAABnp6gFAAABY0lEQVR42u2WsWvCUBCHHTJkzOCQoUMdBRfBQddAF6GD2RQcSqYSn"
			+ "KSDZBPpFDoUpyJuz0FwEXSzf1u/ePIa7GAEY0J5xxuS8y75vPu9e6lUjBkzZuyiDYYDtVK7/U6tld/zS8HkPXmH7wNA47dxNImarWa"
			+ "WLPfBJTFHLFDAqjfqV2WFozD+jHPEmr3PwOLfZ0+xbXuz3eSF1Wl34o8YSYEli8rpn6JpRGdFcPidqqObjkensOj+jbH8vr9YLnh0"
			+ "8BJwjUcXg0UheeX8a04AYZKF+NgiiRxXihRWRjneoImJ1KzfW4pKjH493NwWr63uc5cYPTtKgGUlEDJE6FfBWH93Q/FY0jJRNHpCZ2"
			+ "yI4rEAOvOUoolyFqVjgtcyVOs4RR3nND9rjzVmWBoLIwBneojkjiW1AY6Dj1kPARdnWHKSUjDmLcq7y060EjKpGSNevhSoTRqLWpIo"
			+ "NSPYfBwaM/bP7Ad1jBSGO1V5MAAAAABJRU5ErkJggg==";

}