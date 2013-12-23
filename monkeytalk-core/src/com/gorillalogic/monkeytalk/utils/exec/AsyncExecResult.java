package com.gorillalogic.monkeytalk.utils.exec;

public class AsyncExecResult {
	public void kill() {
		if (process != null) {
			process.destroy();
		}
	}

	public boolean isDone() {
		return result != null;
	}

	public ExecResult getResult() {
		return result;
	}

	private ExecResult result = null;
	private final Process process;

	// package scope
	AsyncExecResult(Process p) {
		this.process = p;
	}

	void setResult(ExecResult result) {
		this.result = result;
	}

}
