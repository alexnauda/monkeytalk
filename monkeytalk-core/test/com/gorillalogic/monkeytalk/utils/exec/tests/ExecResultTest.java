package com.gorillalogic.monkeytalk.utils.exec.tests;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.exec.ExecResult;
import com.gorillalogic.monkeytalk.utils.exec.ExecStatus;

public class ExecResultTest {

	@Test
	public void testConstructor() throws IOException {
		ExecResult result = new ExecResult(ExecStatus.OK);
		assertThat(result.getStatus(), is(ExecStatus.OK));
		assertThat(result.getMessage(), nullValue());
	}

	@Test
	public void testConstructorWithMessage() throws IOException {
		ExecResult result = new ExecResult(ExecStatus.ERROR, "some msg");
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), is("some msg"));
	}
}