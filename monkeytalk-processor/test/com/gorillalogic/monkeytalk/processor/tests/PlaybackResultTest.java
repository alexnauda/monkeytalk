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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;

public class PlaybackResultTest {

	@Test
	public void testDefaultConstructor() {
		PlaybackResult result = new PlaybackResult();
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.toString(), is("OK"));
	}

	@Test
	public void testConstructorWithStatus() {
		PlaybackResult result = new PlaybackResult(PlaybackStatus.FAILURE);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.toString(), is("FAILURE"));
	}

	@Test
	public void testConstructorWithStatusAndMessage() {
		PlaybackResult result = new PlaybackResult(PlaybackStatus.ERROR, "some err");
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("some err"));
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
		assertThat(result.toString(), is("ERROR : some err"));
	}

	@Test
	public void testConstructorWithStatusAndMessageAndWarning() {
		PlaybackResult result = new PlaybackResult(PlaybackStatus.OK, "some msg", null, "some warn");
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("some msg"));
		assertThat(result.getWarning(), is("some warn"));
		assertThat(result.getImage(), nullValue());
		assertThat(result.toString(), is("OK : some msg"));
	}

	@Test
	public void testConstructorWithStatusAndMessageAndWarningAndImage() {
		PlaybackResult result = new PlaybackResult(PlaybackStatus.FAILURE, "some fail", null,
				"some warn", "some img");
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), is("some fail"));
		assertThat(result.getWarning(), is("some warn"));
		assertThat(result.getImage(), is("some img"));
		assertThat(result.toString(), is("FAILURE : some fail"));
	}

	@Test
	public void testConstructorWithResponseOk() {
		Response resp = new Response();
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseOkAndMessage() {
		Response resp = new Response(200, "{result:\"OK\",message:\"some msg\"}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("some msg"));
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseOkAndMessageMessage() {
		Response resp = new Response(200, "{result:\"OK\",message:{message:\"some msg\"}}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("some msg"));
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseOkAndWarning() {
		Response resp = new Response(200, "{result:\"OK\",message:{warning:\"some warn\"}}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), is("some warn"));
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseOkAndImage() {
		Response resp = new Response(200, "{result:\"OK\",message:{image:\"some img\"}}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), is("some img"));
	}

	@Test
	public void testConstructorWithResponseOkAndScreenshot() {
		Response resp = new Response(200, "{result:\"OK\",message:{screenshot:\"some scrn\"}}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), is("some scrn"));
	}

	@Test
	public void testConstructorWithResponseOkAndMessageAndWarningAndImage() {
		Response resp = new Response(200,
				"{result:\"OK\",message:{warning:\"warn\",message:\"msg\",image:\"img\"}}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("msg"));
		assertThat(result.getWarning(), is("warn"));
		assertThat(result.getImage(), is("img"));
	}

	@Test
	public void testConstructorWithResponseOkAndMessageAndWarningAndScreenshot() {
		Response resp = new Response(200,
				"{result:\"OK\",message:{warning:\"warn\",message:\"msg\",screenshot:\"scrn\"}}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), is("msg"));
		assertThat(result.getWarning(), is("warn"));
		assertThat(result.getImage(), is("scrn"));
	}

	@Test
	public void testConstructorWithResponseFailure() {
		Response resp = new Response(200, "{result:\"FAILURE\"}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseFailureAndMessage() {
		Response resp = new Response(200, "{result:\"FAILURE\",message:\"bad verify\"}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.FAILURE));
		assertThat(result.getMessage(), is("bad verify"));
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseError() {
		Response resp = new Response(200, "{result:\"Error\"}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseErrorAndMessage() {
		Response resp = new Response(200, "{result:\"Error\",message:\"some err\"}");
		PlaybackResult result = new PlaybackResult(resp);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("some err"));
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithResponseAndScope() {
		Response resp = new Response(200, "{result:\"Error\",message:\"some err\"}");
		Scope scope = new Scope("foo.mt");
		PlaybackResult result = new PlaybackResult(resp, scope);
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("some err"));
		assertThat(result.getScope().getFilename(), is("foo.mt"));
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
	}

	@Test
	public void testSetters() {
		PlaybackResult result = new PlaybackResult();
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.OK));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getWarning(), nullValue());
		assertThat(result.getImage(), nullValue());
		assertThat(result.toString(), is("OK"));

		result.setStatus(PlaybackStatus.ERROR);
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.toString(), is("ERROR"));

		result.setMessage("");
		assertThat(result.getMessage(), is(""));
		assertThat(result.toString(), is("ERROR"));

		result.setMessage("bad error");
		assertThat(result.getMessage(), is("bad error"));
		assertThat(result.toString(), is("ERROR : bad error"));

		result.setWarning("some warn");
		assertThat(result.getWarning(), is("some warn"));

		Scope scope = new Scope("foo.mt");
		result.setScope(scope);
		assertThat(result.getScope().getFilename(), is("foo.mt"));
	}

	@Test
	public void testGetters() {
		PlaybackResult result = new PlaybackResult(PlaybackStatus.ERROR, "msg",
				new Scope("foo.mt"), "warn", "img");
		assertThat(result, notNullValue());
		assertThat(result.getStatus(), is(PlaybackStatus.ERROR));
		assertThat(result.getMessage(), is("msg"));
		assertThat(result.getScope().getFilename(), is("foo.mt"));
		assertThat(result.getWarning(), is("warn"));
		assertThat(result.getImage(), is("img"));
		assertThat(result.toString(), is("ERROR : msg"));
	}
}