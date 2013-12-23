package com.gorillalogic.monkeytalk.processor;

import com.gorillalogic.monkeytalk.Command;

public class Step {
	private Command command;
	private PlaybackResult result;
	private Scope scope;
	private int stepNumber;

	public Step(Command command, Scope scope, int stepNumber) {
		this(command, null, scope, stepNumber);
	}

	public Step(Command command, PlaybackResult result, Scope scope, int stepNumber) {
		this.command = command;
		this.result = result;
		this.scope = scope;
		this.stepNumber = stepNumber;
	}

	public Command getCommand() {
		return command;
	}

	public PlaybackResult getResult() {
		return result;
	}

	public Scope getScope() {
		return scope;
	}

	public int getStepNumber() {
		return stepNumber;
	}

	public void setResult(PlaybackResult result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return stepNumber + ": " + command.getCommand() + " -> " + result + " (substeps="
				+ (result != null && result.getSteps() != null ? result.getSteps().size() : 0)
				+ ")";
	}
}