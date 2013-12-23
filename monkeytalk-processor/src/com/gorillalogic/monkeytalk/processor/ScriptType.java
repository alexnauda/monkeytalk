package com.gorillalogic.monkeytalk.processor;

public enum ScriptType {
	SCRIPT, SETUP, TEARDOWN, TEST, SUITE;

	@Override
	public String toString() {
		// only capitalize the first letter
		String s = super.toString();
		return s.toLowerCase();
	}
}
