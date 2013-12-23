package com.gorillalogic.monkeytalk.processor.command;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;

/**
 * The globals command. Supports Globals.Define (and its alias Globals.Set).
 */
public class Globals extends BaseCommand {
	public Globals(Command cmd, Scope scope, PlaybackListener listener) {
		super(cmd, scope, listener);
	}

	public PlaybackResult define() {
		listener.onStart(scope);
		if (cmd.getArgs().size() == 0) {
			result = new PlaybackResult(PlaybackStatus.ERROR, "command '" + cmd.getCommandName()
					+ "' must " + cmd.getAction().toLowerCase() + " at least one global variable",
					scope);
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
					result = new PlaybackResult(PlaybackStatus.ERROR, "command '"
							+ cmd.getCommandName() + "' has bad argument '" + arg
							+ "' -- arguments must be in the form of name=value", scope);
					break;
				}

				try {
					com.gorillalogic.monkeytalk.processor.Globals.validateName(key, "command '"
							+ cmd.getCommandName() + "' has");
				} catch (RuntimeException ex) {
					result = new PlaybackResult(PlaybackStatus.ERROR, ex.getMessage(), scope);
					break;
				}

				// add the global variable to the map
				com.gorillalogic.monkeytalk.processor.Globals.setGlobal(key, val);
			}
		}
		listener.onComplete(scope, new Response());
		return result;
	}
}
