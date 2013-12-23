package com.gorillalogic.monkeytalk.utils.exec.tests;

import static com.gorillalogic.monkeytalk.test.matchers.RegexMatcher.regex;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.exec.Exec;

public class ExecAndBlockTest {

	@Test
	public void testExecAndBlock() throws IOException {
		String out = Exec.runAndBlock("/bin/echo", "foobar");
		assertThat(out, is("foobar"));
	}

	@Test
	public void testExecAndBlockWithMultipleLines() throws IOException {
		String out = Exec.runAndBlock("/usr/bin/printf", "foo\nbar");
		assertThat(out, is("foo\nbar"));
	}

	@Test
	public void testExecAndBlockWithNullCmds() throws IOException {
		String out = Exec.runAndBlock((String[]) null);
		assertThat(out, nullValue());
	}

	@Test
	public void testExecAndBlockWithNullCmd() throws IOException {
		String out = Exec.runAndBlock((String) null);
		assertThat(out, nullValue());
	}

	@Test
	public void testExecAndBlockWithTimeout() throws IOException {
		String out = Exec.runAndBlock(new String[] { "/bin/sleep", "5" }, 123L);
		assertThat(out, containsString("killed after 123ms"));
	}

	@Test(expected = IOException.class)
	public void testExecWithIOError() throws IOException {
		Exec.runAndBlock("jhasdgv70gqohiasv08y");
	}

	@Test
	public void testExecWithExecutionError() throws IOException {
		String out = Exec.runAndBlock("ls", "--illegal");
		assertThat(out, regex("(?s).* : error \\d$"));
	}
}