package com.gorillalogic.monkeytalk.utils.exec;

public class ExecResult {
	public static final ExecResult OK = new ExecResult(ExecStatus.OK);
	public static final ExecResult ERROR = new ExecResult(ExecStatus.ERROR);

	private final ExecStatus status;
	private final String message;
	private int exitValue;
	private final String stdout;
	private final String stderr;
	private final boolean timedOut;

	public ExecResult(ExecStatus status) {
		this(status, -1, null, null, false);
	}

	public ExecResult(ExecStatus status, String message) {
		this(status, -1, message, null, false);
	}

	public ExecResult(ExecStatus status, int exitValue, String stdout, String stderr) {
		this(status, exitValue, stdout, stderr, false);
	}

	public ExecResult(ExecStatus status, int exitValue, String stdout, String stderr,
			boolean timedOut) {
		this.status = status;
		this.message = stdout;
		this.exitValue = exitValue;
		this.stdout = stdout;
		this.stderr = stderr;
		this.timedOut = timedOut;
	}

	public ExecStatus getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public boolean timedOut() {
		return timedOut;
	}

	public String getStderr() {
		return stderr;
	}

	public String getStdout() {
		return stdout;
	}

	public int getExitValue() {
		return exitValue;
	}

	@Override
	public String toString() {
		return "ExecResult (status=" + this.status + " message=" + this.message + " exitValue="
				+ (this.exitValue != -1 ? Integer.toString(this.exitValue) : "?") + " stdout="
				+ (stdout == null ? "not captured" : "(" + stdout.length() + " chars)")
				+ " stderr="
				+ (stderr == null ? "not captured" : "(" + stderr.length() + " chars)")
				+ " timedOut=" + timedOut + ")";
	}
}
