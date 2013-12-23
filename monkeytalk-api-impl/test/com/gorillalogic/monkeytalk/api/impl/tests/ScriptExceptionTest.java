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
package com.gorillalogic.monkeytalk.api.impl.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.gorillalogic.monkeytalk.api.impl.ScriptErrorException;
import com.gorillalogic.monkeytalk.api.impl.ScriptFailureException;

public class ScriptExceptionTest {

	@Test
	public void testError() {
		ScriptErrorException ex = new ScriptErrorException(null);
		assertThat(ex.getMessage(), is("ScriptError"));
		
		ex = new ScriptErrorException("");
		assertThat(ex.getMessage(), is("ScriptError"));
		
		ex = new ScriptErrorException("some error");
		assertThat(ex.getMessage(), is("ScriptError: some error"));
	}
	
	@Test
	public void testFailure() {
		ScriptFailureException ex = new ScriptFailureException(null);
		assertThat(ex.getMessage(), is("ScriptFailure"));
		
		ex = new ScriptFailureException("");
		assertThat(ex.getMessage(), is("ScriptFailure"));
		
		ex = new ScriptFailureException("some failure");
		assertThat(ex.getMessage(), is("ScriptFailure: some failure"));
	}
}