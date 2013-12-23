package com.gorillalogic.monkeytalk.processor.command;

import java.lang.reflect.Method;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.CommandWorld;
import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.verify.Verify;

public class Vars extends BaseCommand {
	public static final String VALID_VARIABLE_PATTERN = "\\A[a-zA-Z][a-zA-Z0-9_]*\\Z";
	public static final String ILLEGAL_MSG = "variables must begin with a letter and contain only letters, numbers, and underscores";

	public Vars(Command cmd, Scope scope, PlaybackListener listener) {
		super(cmd, scope, listener);
	}

	public PlaybackResult define() {
		listener.onStart(scope);
		if (cmd.getArgs().size() == 0) {
			result = new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommandName()
					+ "' must define at least one variable", scope);
		} else {
			for (int i = 0; i < cmd.getArgs().size(); i++) {
				String arg = cmd.getArgs().get(i);
				String key = null;
				String val = null;
				if (arg.contains("=")) {
					String[] parts = arg.split("=");
					key = parts[0];
					val = parts[1];

					if (val.startsWith("\"") && val.endsWith("\"")) {
						val = val.substring(1, val.length() - 1);
					}
				} else {
					key = arg;
					val = "<" + arg + ">";
				}

				if (!key.matches(VALID_VARIABLE_PATTERN)) {
					result = new PlaybackResult(PlaybackStatus.ERROR, "command '"
							+ cmd.getCommandName() + "' has illegal variable '" + key + "' -- "
							+ ILLEGAL_MSG, scope);
					break;
				}

				if (scope.getVariables().containsKey(key)) {
					// we are data-driving, variables already set, so do nothing
				} else if (scope.getArgs().size() > i) {
					// we are running, so use parent command args
					String parentArg = scope.getArgs().get(i);
					if (parentArg.equals("*")) {
						// star, so use default value
						scope.addVariable(key, val);
					} else {
						if (i == 0 && parentArg.toLowerCase().endsWith(CommandWorld.DATA_EXT)) {
							// we are data driving and key not found, so it's trying to use the
							// datafile as the first val (which is bad)
							result = new PlaybackResult(PlaybackStatus.ERROR, "datafile '"
									+ parentArg + "' is missing column '" + key
									+ "' from the header row", scope);
						} else {
							// use arg as value
							scope.addVariable(key, parentArg);
						}
					}
				} else {
					// not data-driving, and no parent command ars, so just use default from
					// vars.define
					scope.addVariable(key, val);
				}
			}
		}
		listener.onComplete(scope, new Response());
		return result;
	}

	public PlaybackResult verify() {
		if (cmd.getArgs().size() == 0) {
			return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
					+ "' must have the expected value as its first arg", scope);
		} else if (cmd.getArgs().size() == 1) {
			return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
					+ "' must have a variable as its second arg", scope);
		} else {
			String expected = cmd.getArgs().get(0);
			String var = cmd.getArgs().get(1);

			boolean inScope = scope.getVariables().containsKey(var);

			if (!inScope && !Globals.hasGlobal(var)) {
				return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
						+ "' must have a valid variable as its second arg -- variable '" + var
						+ "' not found", scope);
			}

			String val = (inScope ? scope.getVariables().get(var) : Globals.getGlobal(var));
			String action = cmd.getAction().toLowerCase();

			// use reflection to find the correct static verify method
			for (Method m : Verify.class.getMethods()) {
				if (action.equalsIgnoreCase(m.getName())) {
					// found it, so do the verify
					boolean b = false;
					try {
						b = (Boolean) m.invoke(null, expected, val);
					} catch (Exception ex) {
						ex.printStackTrace();
						return new PlaybackResult(PlaybackStatus.ERROR, "command '"
								+ cmd.getCommand() + "' had unknown error");
					}

					// figure out the response
					Response resp = (b ? new Response() : new Response.Builder(
							getFailedVerifyMessage(action, expected, val)).failure().build());

					listener.onStart(scope);
					listener.onComplete(scope, resp);
					return new PlaybackResult(resp, scope);
				}
			}

			return new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommand()
					+ "' has unknown action '" + cmd.getAction() + "'");
		}
	}

	private String getFailedVerifyMessage(String action, String expected, String val) {
		if (action.equals("verifynot")) {
			return "Expected not \"" + expected + "\" but found \"" + val + "\"";
		} else if (action.equals("verifywildcard")) {
			return "Expected match to wildcard pattern \"" + expected + "\" but found \"" + val
					+ "\"";
		} else if (action.equals("verifynotwildcard")) {
			return "Expected non-match to wildcard pattern \"" + expected + "\" but found \"" + val
					+ "\"";
		} else if (action.equals("verifyregex")) {
			return "Expected match to regex pattern \"" + expected + "\" but found \"" + val + "\"";
		} else if (action.equals("verifynotregex")) {
			return "Expected non-match to regex pattern \"" + expected + "\" but found \"" + val
					+ "\"";
		}
		return "Expected \"" + expected + "\" but found \"" + val + "\"";
	}
}