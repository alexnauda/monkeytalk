package com.gorillalogic.monkeytalk.utils.exec.tests;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.exec.Exec;
import com.gorillalogic.monkeytalk.utils.exec.ExecResult;
import com.gorillalogic.monkeytalk.utils.exec.ExecStatus;

public class ExecTest {

	@Test
	public void testExec() throws IOException {
		ExecResult result = Exec.run("/bin/echo foobar");
		assertThat(result.getStatus(), is(ExecStatus.OK));
		assertThat(result.getMessage(), is("foobar"));
		assertThat(result.getStdout(), is("foobar"));
		assertThat(result.getStderr(), is(""));
		assertThat(result.getExitValue(), is(0));
		assertThat(result.timedOut(), is(false));
		assertThat(
				result.toString(),
				is("ExecResult (status=OK message=foobar exitValue=0 stdout=(6 chars) stderr=(0 chars) timedOut=false)"));
	}

	@Test
	public void testExecWithMultiline() throws IOException {
		ExecResult result = Exec.run("/usr/bin/printf \"foo\nbar\"");
		assertThat(result.getStatus(), is(ExecStatus.OK));
		assertThat(result.getMessage(), is("foo\nbar"));
		assertThat(result.getStdout(), is("foo\nbar"));
		assertThat(result.getStderr(), is(""));
		assertThat(result.getExitValue(), is(0));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecWithPassingTimeout() throws IOException {
		ExecResult result = Exec.run("/bin/echo foobar", 5000L);
		assertThat(result.getStatus(), is(ExecStatus.OK));
		assertThat(result.getMessage(), is("foobar"));
		assertThat(result.getStdout(), is("foobar"));
		assertThat(result.getStderr(), is(""));
		assertThat(result.getExitValue(), is(0));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecWithFailingTimeout() throws IOException {
		ExecResult result = Exec.run("/bin/sleep 5", 123L);
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), is(""));
		assertThat(result.getStdout(), is(""));
		assertThat(result.getStderr(), containsString("killed after 123ms"));
		assertThat(result.getExitValue(), not(equalTo(0)));
		assertThat(result.timedOut(), is(true));
	}

	@Test
	public void testRepeatedExecWithTimeout() throws Throwable {
		int N = 2000;
		for (int i = 1; i <= N; i++) {
			try {
				if (i % 100 == 0) {
					System.out.println("testRepeatedExecWithTimeout: " + i + " of " + N);
				}
				testExecWithPassingTimeout();
			} catch (Throwable ex) {
				System.out.println("testRepeatedExecWithTimeout: fail at " + i + " of " + N);
				throw ex;
			}
		}
	}

	@Test
	public void testExecWithNull() throws IOException {
		ExecResult result = Exec.run((String) null);
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getStdout(), nullValue());
		assertThat(result.getStderr(), nullValue());
		assertThat(result.getExitValue(), not(equalTo(0)));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecWithBadCommand() throws IOException {
		ExecResult result = Exec.run("24t0984tqgbflmnasdb");
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), notNullValue());
		assertThat(result.getStdout(), is(""));
		assertThat(result.getStderr(), notNullValue());
		assertThat(result.getExitValue(), not(equalTo(0)));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecWithError() throws IOException {
		ExecResult result = Exec.run("ls --illegal");
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), notNullValue());
		assertThat(result.getStdout(), is(""));
		assertThat(result.getStderr(), notNullValue());
		assertThat(result.getExitValue() > 0, is(true));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArray() throws IOException {
		ExecResult result = Exec.run(new String[] { "/bin/echo", "foobar" });
		assertThat(result.getStatus(), is(ExecStatus.OK));
		assertThat(result.getMessage(), is("foobar"));
		assertThat(result.getStdout(), is("foobar"));
		assertThat(result.getStderr(), is(""));
		assertThat(result.getExitValue(), is(0));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArrayWithMultiline() throws IOException {
		ExecResult result = Exec.run(new String[] { "/usr/bin/printf", "foo\nbar" });
		assertThat(result.getStatus(), is(ExecStatus.OK));
		assertThat(result.getMessage(), is("foo\nbar"));
		assertThat(result.getStdout(), is("foo\nbar"));
		assertThat(result.getStderr(), is(""));
		assertThat(result.getExitValue(), is(0));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArrayWithNull() throws IOException {
		ExecResult result = Exec.run((String[]) null);
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getStdout(), nullValue());
		assertThat(result.getStderr(), nullValue());
		assertThat(result.getExitValue(), is(-1));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArrayWithEmpty() throws IOException {
		ExecResult result = Exec.run(new String[] {});
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getStdout(), nullValue());
		assertThat(result.getStderr(), nullValue());
		assertThat(result.getExitValue(), is(-1));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArrayWithNullArg() throws IOException {
		ExecResult result = Exec.run(new String[] { null });
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), nullValue());
		assertThat(result.getStdout(), nullValue());
		assertThat(result.getStderr(), nullValue());
		assertThat(result.getExitValue(), is(-1));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArrayWithBadCommand() throws IOException {
		ExecResult result = Exec.run(new String[] { "24t0984tqgbflmnasdb", "312498gafhaf" });
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), notNullValue());
		assertThat(result.getStdout(), is(""));
		assertThat(result.getStderr(), notNullValue());
		assertThat(result.getExitValue(), not(equalTo(0)));
		assertThat(result.timedOut(), is(false));
	}

	@Test
	public void testExecCmdArrayWithError() throws IOException {
		ExecResult result = Exec.run(new String[] { "ls", "--illegal" });
		assertThat(result.getStatus(), is(ExecStatus.ERROR));
		assertThat(result.getMessage(), notNullValue());
		assertThat(result.getStdout(), is(""));
		assertThat(result.getStderr(), notNullValue());
		assertThat(result.getExitValue(), not(equalTo(0)));
		assertThat(result.timedOut(), is(false));
	}
}