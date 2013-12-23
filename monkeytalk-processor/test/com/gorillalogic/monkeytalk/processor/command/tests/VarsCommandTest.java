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
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;

public class VarsCommandTest extends BaseCommandHelper {

	@Test
	public void testVerifyWithMissingExpected() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Verify", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Vars * Verify' must have the expected value as its first arg"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Vars * Verify' must have the expected value as its first arg"));
	}

	@Test
	public void testVerifyWithMissingVar() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Verify 123", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Vars * Verify 123' must have a variable as its second arg"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Vars * Verify 123' must have a variable as its second arg"));
	}

	@Test
	public void testVerifyWithBadVar() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Verify 123 foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(
				result.getMessage(),
				is("command 'Vars * Verify 123 foo' must have a valid variable as its second arg -- variable 'foo' not found"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Vars * Verify 123 foo' must have a valid variable as its second arg -- variable 'foo' not found"));
	}

	@Test
	public void testVerifyWithBadAction() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nVars * VerifyFoo 123 foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(),
				is("command 'Vars * VerifyFoo 123 foo' has unknown action 'VerifyFoo'"));

		assertThat(output, containsString("START"));
		assertThat(
				output,
				containsString("COMPLETE : ERROR : command 'Vars * VerifyFoo 123 foo' has unknown action 'VerifyFoo'"));
	}

	@Test
	public void testVerify() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nVars * Verify 123 foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 -> OK"));
		assertThat(output, containsString("Vars * Verify 123 foo -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testVerifyFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nVars * Verify 456 foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), is("Expected \"456\" but found \"123\""));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 -> OK"));
		assertThat(
				output,
				containsString("Vars * Verify 456 foo -> FAILURE : Expected \"456\" but found \"123\""));
		assertThat(output,
				containsString("COMPLETE : FAILURE : Expected \"456\" but found \"123\""));
	}

	@Test
	public void testVerifyNot() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nVars * VerifyNot 456 foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 -> OK"));
		assertThat(output, containsString("Vars * VerifyNot 456 foo -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testVerifyNotFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=123\nVars * VerifyNot 123 foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), is("Expected not \"123\" but found \"123\""));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=123 -> OK"));
		assertThat(
				output,
				containsString("Vars * VerifyNot 123 foo -> FAILURE : Expected not \"123\" but found \"123\""));
		assertThat(output,
				containsString("COMPLETE : FAILURE : Expected not \"123\" but found \"123\""));
	}

	@Test
	public void testVerifyWildcard() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Vars * Define foo=\"i like cheese\"\nVars * VerifyWildcard \"i ???? ch*\" foo",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"i like cheese\" -> OK"));
		assertThat(output, containsString("Vars * VerifyWildcard \"i ???? ch*\" foo -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testVerifyWildcardFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Vars * Define foo=\"i like cheese\"\nVars * VerifyWildcard \"i ???? ch*ABC\" foo",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(
				result.getMessage(),
				is("Expected match to wildcard pattern \"i ???? ch*ABC\" but found \"i like cheese\""));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"i like cheese\" -> OK"));
		assertThat(
				output,
				containsString("Vars * VerifyWildcard \"i ???? ch*ABC\" foo -> FAILURE : Expected match to wildcard pattern \"i ???? ch*ABC\" but found \"i like cheese\""));
		assertThat(
				output,
				containsString("COMPLETE : FAILURE : Expected match to wildcard pattern \"i ???? ch*ABC\" but found \"i like cheese\""));
	}

	@Test
	public void testVerifyNotWildcard() throws IOException {
		File dir = tempDir();
		tempScript(
				"foo.mt",
				"Vars * Define foo=\"i like cheese\"\nVars * VerifyNotWildcard \"i ???? ch*ABC\" foo",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"i like cheese\" -> OK"));
		assertThat(output, containsString("Vars * VerifyNotWildcard \"i ???? ch*ABC\" foo -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testVerifyNotWildcardFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Vars * Define foo=\"i like cheese\"\nVars * VerifyNotWildcard \"i ???? ch*\" foo",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(
				result.getMessage(),
				is("Expected non-match to wildcard pattern \"i ???? ch*\" but found \"i like cheese\""));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"i like cheese\" -> OK"));
		assertThat(
				output,
				containsString("Vars * VerifyNotWildcard \"i ???? ch*\" foo -> FAILURE : Expected non-match to wildcard pattern \"i ???? ch*\" but found \"i like cheese\""));
		assertThat(
				output,
				containsString("COMPLETE : FAILURE : Expected non-match to wildcard pattern \"i ???? ch*\" but found \"i like cheese\""));
	}

	@Test
	public void testVerifyRegex() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=\"abc123\"\nVars * VerifyRegex \\w{3}\\d{3} foo",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"abc123\" -> OK"));
		assertThat(output, containsString("Vars * VerifyRegex \\w{3}\\d{3} foo -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testVerifyRegexFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt", "Vars * Define foo=\"abc123\"\nVars * VerifyRegex \\w{3}\\d{4} foo",
				dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(),
				is("Expected match to regex pattern \"\\w{3}\\d{4}\" but found \"abc123\""));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"abc123\" -> OK"));
		assertThat(
				output,
				containsString("Vars * VerifyRegex \\w{3}\\d{4} foo -> FAILURE : Expected match to regex pattern \"\\w{3}\\d{4}\" but found \"abc123\""));
		assertThat(
				output,
				containsString("COMPLETE : FAILURE : Expected match to regex pattern \"\\w{3}\\d{4}\" but found \"abc123\""));
	}

	@Test
	public void testVerifyNotRegex() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Vars * Define foo=\"abc123\"\nVars * VerifyNotRegex \\w{3}\\d{4} foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.OK));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"abc123\" -> OK"));
		assertThat(output, containsString("Vars * VerifyNotRegex \\w{3}\\d{4} foo -> OK"));
		assertThat(output, containsString("COMPLETE : OK"));
	}

	@Test
	public void testVerifyNotRegexFailure() throws IOException {
		File dir = tempDir();
		tempScript("foo.mt",
				"Vars * Define foo=\"abc123\"\nVars * VerifyNotRegex \\w{3}\\d{3} foo", dir);

		ScriptProcessor processor = new ScriptProcessor(HOST, PORT, dir);
		processor.setPlaybackListener(LISTENER_WITH_OUTPUT);
		PlaybackResult result = processor.runScript("foo.mt");

		assertThat("FAIL: " + result, result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(),
				is("Expected non-match to regex pattern \"\\w{3}\\d{3}\" but found \"abc123\""));

		assertThat(output, containsString("START"));
		assertThat(output, containsString("Vars * Define foo=\"abc123\" -> OK"));
		assertThat(
				output,
				containsString("Vars * VerifyNotRegex \\w{3}\\d{3} foo -> FAILURE : Expected non-match to regex pattern \"\\w{3}\\d{3}\" but found \"abc123\""));
		assertThat(
				output,
				containsString("COMPLETE : FAILURE : Expected non-match to regex pattern \"\\w{3}\\d{3}\" but found \"abc123\""));
	}
}