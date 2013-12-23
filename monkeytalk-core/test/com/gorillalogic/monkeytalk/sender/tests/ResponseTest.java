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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;

public class ResponseTest {

	@Test
	public void testDefaultConstructor() {
		Response r = new Response();
		assertThat(r, notNullValue());
		assertThat(r.getCode(), is(200));
		assertThat(r.getBody(), nullValue());
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), nullValue());
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructor() {
		Response r = new Response(200, "{result:\"OK\",message:\"some msg\"}");
		assertThat(r, notNullValue());
		assertThat(r.getCode(), is(200));
		assertThat(r.getBody(), is("{result:\"OK\",message:\"some msg\"}"));
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());

		assertThat(r.toString(), containsString("OK"));
		assertThat(r.toString(), containsString("some msg"));
	}

	@Test
	public void testConstructorWithNullBody() {
		Response r = new Response(200, null);
		assertThat(r.getCode(), is(200));
		assertThat(r.getBody(), nullValue());
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), nullValue());
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithOnlyResult() {
		Response r = new Response(200, "{result:\"OK\"}");
		assertThat(r.getCode(), is(200));
		assertThat(r.getBody(), is("{result:\"OK\"}"));
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), nullValue());
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithEmptyMessage() {
		Response r = new Response(200, "{result:\"OK\",message:\"\"}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is(""));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithNullMessage() {
		Response r = new Response(200, "{result:\"OK\",message:null}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("null"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithNon200Code() {
		Response r = new Response(0, "{result:\"OK\",message:\"some msg\"}");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithNon200CodeNonJsonMessage() {
		Response r = new Response(0, "non-json msg");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("non-json msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWith200CodeNonJsonMessage() {
		Response r = new Response(200, "non-json msg");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("non-json msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithFailure() {
		Response r = new Response(200, "{result:\"FAILURE\",message:\"some msg\"}");
		assertThat(r.getStatus(), is(ResponseStatus.FAILURE));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithError() {
		Response r = new Response(200, "{result:\"ERROR\",message:\"some msg\"}");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithFailureCaseInsensitive() {
		Response r = new Response(200, "{result:\"failURE\",message:\"some msg\"}");
		assertThat(r.getStatus(), is(ResponseStatus.FAILURE));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithWarning() {
		Response r = new Response(200, "{result:\"OK\",message:{warning:\"some warn\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), nullValue());
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testConstructorWithImage() {
		Response r = new Response(200, "{result:\"OK\",message:{image:\"some img\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), nullValue());
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), is("some img"));
	}

	@Test
	public void testConstructorWithMessageAndWarningAndImage() {
		Response r = new Response(200,
				"{result:\"OK\",message:{message:\"some msg\",warning:\"some warn\",image:\"some img\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some img"));
	}

	@Test
	public void testConstructorWithMessageAndWarningAndScreenshot() {
		Response r = new Response(200,
				"{result:\"OK\",message:{message:\"some msg\",warning:\"some warn\",screenshot:\"some scrn\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some scrn"));
	}

	@Test
	public void testConstructorWithFailureAndMessageAndWarningAndImage() {
		Response r = new Response(200,
				"{result:\"failure\",message:{message:\"some msg\",warning:\"some warn\",image:\"some img\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.FAILURE));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some img"));
	}

	@Test
	public void testConstructorWithFailureAndMessageAndWarningAndScreenshot() {
		Response r = new Response(
				200,
				"{result:\"failure\",message:{message:\"some msg\",warning:\"some warn\",screenshot:\"some scrn\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.FAILURE));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some scrn"));
	}

	@Test
	public void testConstructorWithNon200CodeAndMessageAndWarningAndImage() {
		Response r = new Response(0,
				"{result:\"ok\",message:{message:\"some msg\",warning:\"some warn\",image:\"some img\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some img"));
	}

	@Test
	public void testConstructorWithMessageAndWarningAndImageWithoutResult() {
		Response r = new Response(200,
				"{message:{message:\"some msg\",warning:\"some warn\",image:\"some img\"}}");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some img"));
	}

	@Test
	public void testConstructorWithAlternateFormat() {
		Response r = new Response(200,
				"{result:\"ok\",message:\"some msg\",warning:\"some warn\",screenshot:\"some scrn\"}");
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some scrn"));
	}
	
	@Test
	public void testConstructorWithAlternateFormatWithImage() {
		Response r = new Response(200,
				"{result:\"unknown\",message:\"some msg\",warning:\"some warn\",image:\"some scrn\"}");
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some scrn"));
	}

	@Test
	public void testBuilder() {
		Response r = new Response.Builder().build();
		assertThat(r.getCode(), is(200));
		assertThat(r.getBody(), is("{\"result\":\"OK\"}"));
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), nullValue());
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testBuilderWithMessage() {
		Response r = new Response.Builder("some message").build();
		assertThat(r.getCode(), is(200));
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("some message"));
		assertThat(r.getWarning(), nullValue());
		assertThat(r.getImage(), nullValue());
	}

	@Test
	public void testBuilderWithOkAndMessageAndWarningAndImage() {
		Response r = new Response.Builder("some msg").warning("some warn").image("some img")
				.build();
		assertThat(r.getCode(), is(200));
		assertThat(r.getStatus(), is(ResponseStatus.OK));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some img"));
	}

	@Test
	public void testBuilderWithErrorAndMessageAndWarningAndImage() {
		Response r = new Response.Builder("some msg").error().warning("some warn")
				.image("some img").build();
		assertThat(r.getCode(), is(200));
		assertThat(r.getStatus(), is(ResponseStatus.ERROR));
		assertThat(r.getMessage(), is("some msg"));
		assertThat(r.getWarning(), is("some warn"));
		assertThat(r.getImage(), is("some img"));
	}
}