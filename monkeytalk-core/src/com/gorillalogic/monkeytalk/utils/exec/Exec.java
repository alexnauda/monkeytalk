package com.gorillalogic.monkeytalk.utils.exec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import com.gorillalogic.monkeytalk.utils.FileUtils;

public class Exec {

	/** how long to wait after process finishes for stdout and stderr to drain */
	private final static long PAUSE_FOR_STREAM_DRAIN = 5000;

	private static long buildTimeout(Long timeout) {
		return (timeout == null ? ExecuteWatchdog.INFINITE_TIMEOUT : timeout.longValue());
	}

	private static CommandLine buildCommandLine(String cmd) {
		if (cmd != null) {
			return CommandLine.parse(cmd);
		}
		return null;
	}

	private static CommandLine buildCommandLine(String[] cmds) {
		CommandLine cmd = null;
		if (cmds != null) {
			for (String s : cmds) {
				if (s == null || s.length() == 0) {
					// error for any null or blank command parts
					return null;
				}

				if (cmd == null) {
					cmd = new CommandLine(s);
				} else {
					cmd.addArgument(s, false);
				}
			}
		}
		return cmd;
	}

	@SuppressWarnings("rawtypes")
	private static ExecResult exec(CommandLine cmd, long timeout, Map env) {
		if (cmd == null) {
			return ExecResult.ERROR;
		}

		LogOutputStream out = new LogOutputStream() {
			private final StringBuilder sb = new StringBuilder();

			@Override
			protected void processLine(String line, int level) {
				sb.append(sb.length() > 0 ? "\n" : "").append(line);
			}

			@Override
			public String toString() {
				return sb.toString();
			}
		};

		LogOutputStream err = new LogOutputStream() {
			private final StringBuilder sb = new StringBuilder();

			@Override
			protected void processLine(String line, int level) {
				sb.append(sb.length() > 0 ? "\n" : "").append(line);
			}

			@Override
			public String toString() {
				return sb.toString();
			}
		};

		DefaultExecutor exec = new DefaultExecutor();
		exec.setStreamHandler(new PumpStreamHandler(out, err));
		exec.setProcessDestroyer(new ShutdownHookProcessDestroyer());

		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
		exec.setWatchdog(watchdog);

		int exitval = -1;
		try {
			exitval = exec.execute(cmd, env);
		} catch (ExecuteException ex) {
			if (watchdog.killedProcess()) {
				return new ExecResult(ExecStatus.ERROR, ex.getExitValue(), out.toString(),
						"killed after " + timeout + "ms", true);
			}
			String exceptionMessage = ex.getMessage();
			if (exceptionMessage!=null 
					&& exceptionMessage.startsWith("Process exited with an error:")) {
				exceptionMessage=err.toString();
			}
			return new ExecResult(ExecStatus.ERROR, ex.getExitValue(), out.toString(),
					exceptionMessage);
		} catch (IOException ex) {
			return new ExecResult(ExecStatus.ERROR, exitval, out.toString(), ex.getMessage());
		}

		return new ExecResult(ExecStatus.OK, exitval, out.toString(), err.toString());
	}

	private static String execAndBlock(CommandLine cmd, long timeout) throws IOException {
		if (cmd == null) {
			return null;
		}

		LogOutputStream out = new LogOutputStream() {
			private final StringBuilder sb = new StringBuilder();

			@Override
			protected void processLine(String line, int level) {
				sb.append(sb.length() > 0 ? "\n" : "").append(line);
			}

			@Override
			public String toString() {
				return sb.toString();
			}
		};

		DefaultExecutor exec = new DefaultExecutor();
		exec.setStreamHandler(new PumpStreamHandler(out, out));
		exec.setProcessDestroyer(new ShutdownHookProcessDestroyer());

		ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
		exec.setWatchdog(watchdog);

		try {
			exec.execute(cmd);
		} catch (ExecuteException ex) {
			if (watchdog.killedProcess()) {
				return out.toString() + (out.toString().length() > 0 ? " : " : "")
						+ "killed after " + timeout + "ms";
			}
			return out.toString() + (out.toString().length() > 0 ? " : " : "") + "error "
					+ ex.getExitValue();
		}

		return out.toString();
	}

	/**
	 * Execute the given command, block indefinitely for completion, and return the result
	 * (including exit code, stdout, and stderr).
	 * 
	 * @param cmd
	 *            the command to execute
	 * @return the result
	 */
	public static ExecResult run(String cmd) {
		return exec(buildCommandLine(cmd), ExecuteWatchdog.INFINITE_TIMEOUT, null);
	}

	/**
	 * Execute the given command, block for the given timeout, and return the result (including exit
	 * code, stdout, and stderr). If timeout is {@code null}, block indefinitely.
	 * 
	 * @param cmd
	 *            the command to execute
	 * @param timeout
	 *            the timeout (in ms)
	 * @return the result
	 */
	public static ExecResult run(String cmd, Long timeout) {
		return exec(buildCommandLine(cmd), buildTimeout(timeout), null);
	}

	/**
	 * Execute the given command set, block indefinitely for completion, and return the result
	 * (including exit code, stdout, and stderr).
	 * 
	 * @param cmds
	 *            the command set
	 * @return the result
	 */
	public static ExecResult run(String[] cmds) {
		return exec(buildCommandLine(cmds), ExecuteWatchdog.INFINITE_TIMEOUT, null);
	}

	/**
	 * Execute the given command set, block for the given timeout, and return the result (including
	 * exit code, stdout, and stderr). If timeout is {@code null}, block indefinitely.
	 * 
	 * @param cmds
	 *            the command set
	 * @param timeout
	 *            the timeout (in ms)
	 * @return the result
	 */
	public static ExecResult run(String[] cmds, Long timeout) {
		return exec(buildCommandLine(cmds), buildTimeout(timeout), null);
	}

	/**
	 * Execute the given command set with the given environment variables, block for the given
	 * timeout, and return the result (including exit code, stdout, and stderr). If timeout is
	 * {@code null}, block indefinitely.
	 * 
	 * @param cmds
	 *            the command set
	 * @param timeout
	 *            the timeout (in ms)
	 * @param env
	 *            the environment
	 * @return the result
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ExecResult run(String[] cmds, Long timeout, String[] env) {
		Map m = null;
		if (env != null) {
			m = new HashMap();
			for (String e : env) {
				String[] parts = e.split("=");
				m.put(parts[0], parts[1]);
			}
		}
		return exec(buildCommandLine(cmds), buildTimeout(timeout), m);
	}

	/**
	 * Execute the given command, block indefinitely for completion, and return the result as a
	 * String (a combo of stdout and stderr).
	 * 
	 * @param cmd
	 *            the command to execute
	 * @return the result
	 */
	public static String runAndBlock(String cmd) throws IOException {
		return execAndBlock(buildCommandLine(new String[] { cmd }),
				ExecuteWatchdog.INFINITE_TIMEOUT);
	}

	/**
	 * Execute the given command set, block indefinitely for completion, and return the result as a
	 * String (a combo of stdout and stderr).
	 * 
	 * @param cmd1
	 *            the first command part
	 * @param cmd2
	 *            the second command part
	 * @return the result
	 */
	public static String runAndBlock(String cmd1, String cmd2) throws IOException {
		return execAndBlock(buildCommandLine(new String[] { cmd1, cmd2 }),
				ExecuteWatchdog.INFINITE_TIMEOUT);
	}

	/**
	 * Execute the given command set, block indefinitely for completion, and return the result as a
	 * String (a combo of stdout and stderr).
	 * 
	 * @param cmd1
	 *            the first command part
	 * @param cmd2
	 *            the second command part
	 * @param cmd3
	 *            the third command part
	 * @return the result
	 */
	public static String runAndBlock(String cmd1, String cmd2, String cmd3) throws IOException {
		return execAndBlock(buildCommandLine(new String[] { cmd1, cmd2, cmd3 }),
				ExecuteWatchdog.INFINITE_TIMEOUT);
	}

	/**
	 * Execute the given command set, block indefinitely for completion, and return the result as a
	 * String (a combo of stdout and stderr).
	 * 
	 * @param cmd1
	 *            the first command part
	 * @param cmd2
	 *            the second command part
	 * @param cmd3
	 *            the third command part
	 * @param cmd4
	 *            the forth command part
	 * @return the result
	 */
	public static String runAndBlock(String cmd1, String cmd2, String cmd3, String cmd4)
			throws IOException {
		return execAndBlock(buildCommandLine(new String[] { cmd1, cmd2, cmd3, cmd4 }),
				ExecuteWatchdog.INFINITE_TIMEOUT);
	}

	/**
	 * Execute the given command set, block indefinitely for completion, and return the result as a
	 * String (a combo of stdout and stderr).
	 * 
	 * @param cmd1
	 *            the first command part
	 * @param cmd2
	 *            the second command part
	 * @param cmd3
	 *            the third command part
	 * @param cmd4
	 *            the forth command part
	 * @param cmd5
	 *            the fifth command part
	 * @return the result
	 */
	public static String runAndBlock(String cmd1, String cmd2, String cmd3, String cmd4, String cmd5)
			throws IOException {
		return execAndBlock(buildCommandLine(new String[] { cmd1, cmd2, cmd3, cmd4, cmd5 }),
				ExecuteWatchdog.INFINITE_TIMEOUT);
	}

	/**
	 * Execute the given command set, block indefinitely for completion, and return the result as a
	 * String (a combo of stdout and stderr).
	 * 
	 * @param cmds
	 *            the command set to execute
	 * @return the result
	 */
	public static String runAndBlock(String[] cmds) throws IOException {
		return execAndBlock(buildCommandLine(cmds), ExecuteWatchdog.INFINITE_TIMEOUT);
	}

	/**
	 * Execute the given command set, block for the given timeout, and return the result as a String
	 * (a combo of stdout and stderr). If timeout is {@code null}, block indefinitely.
	 * 
	 * @param cmds
	 *            the command set to execute
	 * @param timeout
	 *            the timeout (in ms)
	 * @return the result
	 */
	public static String runAndBlock(String[] cmds, Long timeout) throws IOException {
		return execAndBlock(buildCommandLine(cmds), buildTimeout(timeout));
	}

	public static AsyncExecResult runAsync(String cmd) {
		return runAsync(new String[] { cmd }, null);
	}

	public static AsyncExecResult runAsync(String[] cmd) {
		return runAsync(cmd, null);
	}

	public static AsyncExecResult runAsync(String[] cmd, Long timeout) {
		try {
			final Process p = Runtime.getRuntime().exec(cmd);
			final AsyncExecResult asyncResult = new AsyncExecResult(p);
			final Long timeout2 = timeout == null ? null : new Long(timeout);
			new Thread(new Runnable() {
				public void run() {
					try {
						manageProcess(p, timeout2, asyncResult);
					} catch (Exception e) {
						asyncResult.setResult(new ExecResult(ExecStatus.ERROR, e.getMessage()));
					}
				}
			}).start();
			return asyncResult;
		} catch (Exception ex) {
			AsyncExecResult asyncResult = new AsyncExecResult(null);
			asyncResult.setResult(new ExecResult(ExecStatus.ERROR, ex.getMessage()));
			System.err.println("Error caught in Exec.runAsync(): " + ex.getMessage()
					+ " -- returning ERROR, stack trace for diagnostic purposes: ");
			ex.printStackTrace();
			return asyncResult;
		}
	}

	private static ExecResult manageProcess(Process p, Long timeout, AsyncExecResult asyncResult)
			throws Exception {
		ExecStatus status = ExecStatus.OK;
		String message;
		FileUtils.StreamEater stdoutEater = new FileUtils.StreamEater(p.getInputStream());
		FileUtils.StreamEater stderrEater = new FileUtils.StreamEater(p.getErrorStream());

		if (timeout == null) {
			p.waitFor();
		} else {
			ExecWorker worker = new ExecWorker(p);
			worker.start();
			try {
				worker.join(timeout);
				if (worker.getExitValue() == null) {
					throw new Exception("Exec() timed out");
				}
			} catch (InterruptedException ex) {
				worker.interrupt();
				Thread.currentThread().interrupt();
				throw ex;
			} finally {
				p.destroy();
			}
		}

		int exitValue = p.exitValue();
		try {
			Thread t=stderrEater.getEaterThread();
			t.join(PAUSE_FOR_STREAM_DRAIN);
			if (t.isAlive()) {
				t.interrupt();
			}

			t=stdoutEater.getEaterThread();
			t.join(PAUSE_FOR_STREAM_DRAIN);
			if (t.isAlive()) {
				t.interrupt();
			}
		} catch (InterruptedException ex) {
			// ignore
		}
		String stderr = stderrEater.toString();
		String stdout = stdoutEater.toString();

		if (exitValue != 0) {
			status = ExecStatus.ERROR;
			message = stderr;
			if (message == null || message.length() == 0) {
				message = stdout;
			}
		} else {
			status = ExecStatus.OK;
			message = stdout;
			if (message == null || message.length() == 0) {
				message = stderr;
			}
		}

		if (message == null) {
			message = "";
		}

		ExecResult result = new ExecResult(status, exitValue, stdout, stderr);
		if (asyncResult != null) {
			asyncResult.setResult(result);
		}
		return result;
	}

	private static class ExecWorker extends Thread {
		private final Process process;
		private Integer exitValue;

		private ExecWorker(Process process) {
			this.process = process;
		}

		private Integer getExitValue() {
			return exitValue;
		}

		public void run() {
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}
	}
}
