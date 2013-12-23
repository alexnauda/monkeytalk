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
package com.gorillalogic.monkeytalk.sender.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.sender.CommandSenderFactory;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.server.JsonServer;

public class CommandSenderTest {
	private static final int PORT = 18011;
	private static JsonServer server;
	private CommandSender commandSender;
	private long now;

	@BeforeClass
	public static void beforeClass() {
		try {
			server = new JsonServer(PORT);
		} catch (IOException ex) {
			fail("server failed to start");
		}
		System.out.println("server running on " + server.getPort() + "...");
	}

	@AfterClass
	public static void afterClass() {
		assertThat(server.isRunning(), is(true));
		server.stop();
		assertThat(server.isRunning(), is(false));
		System.out.println("server stopped on " + server.getPort() + "...");
	}

	@Before
	public void before() {
		assertThat(server, notNullValue());
		assertThat(server.isRunning(), is(true));
		assertThat(server.getPort(), is(PORT));

		commandSender = CommandSenderFactory.createCommandSender("localhost", PORT);
		now = System.currentTimeMillis();
	}

	@Test
	public void testConstructor() throws JSONException {
		commandSender = CommandSenderFactory.createCommandSender("localhost", 1234, "/foo");
		String url = "http://localhost:" + 1234 + "/foo";
		assertThat(commandSender.toString(), containsString(url));
	}

	@Test
	public void testConstructorWithNullHostAndNegativePort() throws JSONException {
		commandSender = CommandSenderFactory.createCommandSender(null, -1);
		String url = "http://localhost:" + 0 + "/fonemonkey";
		assertThat(commandSender.toString(), containsString(url));
	}

	@Test
	public void testConstructorWithNullHostAndAgentAndPath() throws JSONException {
		commandSender = CommandSenderFactory.createCommandSender(null, 1234, null);
		String url = "http://localhost:" + 1234 + "/fonemonkey";
		assertThat(commandSender.toString(), containsString(url));
	}

	@Test
	public void testSendPlay() throws JSONException {
		Command cmd = new Command("Button OK Click");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getString("componentType"), is(cmd.getComponentType()));
		assertThat(echo.getString("monkeyId"), is(cmd.getMonkeyId()));
		assertThat(echo.getString("action"), is(cmd.getAction()));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PLAY));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPlayWithUnicode() throws JSONException {
		Command cmd = new Command("Button Héìíô\u21D0\u21D1\u21DD\u21DC Click");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getString("componentType"), is(cmd.getComponentType()));
		assertThat(echo.getString("monkeyId"), is(cmd.getMonkeyId()));
		assertThat(echo.getString("action"), is(cmd.getAction()));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PLAY));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPlayWithRegex() throws JSONException {
		Command cmd = new Command("Label * VerifyRegex \"\\w+ \\w*\" prop");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getString("componentType"), is(cmd.getComponentType()));
		assertThat(echo.getString("monkeyId"), is(cmd.getMonkeyId()));
		assertThat(echo.getString("action"), is(cmd.getAction()));
		assertThat(echo.getJSONArray("args").getString(0), is("\\w+ \\w*"));
		assertThat(echo.getJSONArray("args").getString(1), is("prop"));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PLAY));
		assertThat(echo.getLong("timestamp") >= now, is(true));

		String json = body.toString();
		assertThat(json, containsString("\"mtcommand\":\"PLAY\""));
		assertThat(json, containsString("\"componentType\":\"Label\""));
		assertThat(json, containsString("\"monkeyId\":\"*\""));
		assertThat(json, containsString("\"action\":\"VerifyRegex\""));
		assertThat(json, containsString("\"args\":[\"\\\\w+ \\\\w*\",\"prop\"]"));
	}

	@Test
	public void testSendPlayWithCommandParts() throws JSONException {
		Response resp = commandSender.play("Button", "Héìíô\u21D0\u21D1\u21DD\u21DC", "Click",
				null, null);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getString("componentType"), is("Button"));
		assertThat(echo.getString("monkeyId"), is("Héìíô\u21D0\u21D1\u21DD\u21DC"));
		assertThat(echo.getString("action"), is("Click"));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PLAY));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPlayWithCommandPartsWithUnicode() throws JSONException {
		Response resp = commandSender.play("Button", "OK", "Click", null, null);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getString("componentType"), is("Button"));
		assertThat(echo.getString("monkeyId"), is("OK"));
		assertThat(echo.getString("action"), is("Click"));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PLAY));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testPlayIgnoresNullCommand() throws JSONException {
		Response resp = commandSender.play(null);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));
		assertThat(body.getString("message"), is("ignore blank command"));
	}

	@Test
	public void testPlayIgnoresDefaultCommand() throws JSONException {
		Command cmd = new Command();
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));
		assertThat(body.getString("message"), is("ignore blank command"));
	}

	@Test
	public void testPlayIgnoresBlankCommand() throws JSONException {
		Command cmd = new Command("");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));
		assertThat(body.getString("message"), is("ignore blank command"));
	}

	@Test
	public void testPlayIgnoresComment() throws JSONException {
		Command cmd = new Command("# some comment");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));
		assertThat(body.getString("message"), is("ignore comment"));
	}

	@Test
	public void testPlayIgnoresDocVars() throws JSONException {
		Command cmd = new Command(
				"Doc * Vars firstName=\"The user's first name.\" lastName=\"The user's last name.\"");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));
		assertThat(body.getString("message"), is("ignore doc.vars"));
	}

	@Test
	public void testPlayIgnoresDocScript() throws JSONException {
		Command cmd = new Command("Doc * Script \"some script doc\"");
		Response resp = commandSender.play(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));
		assertThat(body.getString("message"), is("ignore doc.script"));
	}

	@Test
	public void testSendRecord() throws JSONException {
		Command cmd = new Command("Button OK Click");
		Response resp = commandSender.record(cmd);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getString("componentType"), is(cmd.getComponentType()));
		assertThat(echo.getString("monkeyId"), is(cmd.getMonkeyId()));
		assertThat(echo.getString("action"), is(cmd.getAction()));
		assertThat(echo.getString("mtcommand"), is(CommandSender.RECORD));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPingRecordOn() throws JSONException {
		Response resp = commandSender.ping(true);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getInt("mtversion"), is(1));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PING));
		assertThat(echo.getString("record"), is("ON"));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPingRecordOnWithHostAndPort() throws JSONException {
		Response resp = commandSender.ping(true, "host", 1234);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getInt("mtversion"), is(1));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PING));
		assertThat(echo.getString("record"), is("ON"));
		assertThat(echo.getString("recordhost"), is("host"));
		assertThat(echo.getInt("recordport"), is(1234));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPingRecordOff() throws JSONException {
		Response resp = commandSender.ping(false);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getInt("mtversion"), is(1));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PING));
		assertThat(echo.getString("record"), is("OFF"));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendPingRecordOffWithHostAndPort() throws JSONException {
		Response resp = commandSender.ping(false, "host", 1234);

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getInt("mtversion"), is(1));
		assertThat(echo.getString("mtcommand"), is(CommandSender.PING));
		assertThat(echo.getString("record"), is("OFF"));
		assertThat(echo.has("recordhost"), is(false));
		assertThat(echo.has("recordport"), is(false));
		assertThat(echo.getLong("timestamp") >= now, is(true));
	}

	@Test
	public void testSendDumpTree() throws JSONException {
		Response resp = commandSender.dumpTree();

		assertThat(resp, notNullValue());
		assertThat(resp.getCode(), is(200));

		JSONObject body = resp.getBodyAsJSON();
		assertThat(body, notNullValue());
		assertThat(body.getString("result"), is("OK"));

		JSONObject message = body.getJSONObject("message");
		assertThat(message, notNullValue());
		assertThat(message.getString("uri"), is("/fonemonkey"));
		assertThat(message.getString("method"), is("POST"));

		JSONObject echo = message.getJSONObject("body");
		assertThat(echo.getInt("mtversion"), is(1));
		assertThat(echo.getString("mtcommand"), is(CommandSender.DUMPTREE));
		assertThat(echo.getLong("timestamp") >= now, is(true));
		assertThat(echo.names().length(), is(3));
	}
}