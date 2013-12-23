package com.gorillalogic.monkeytalk.processor.command;

import java.util.Map;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.PlaybackStatus;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;

public class Debug extends BaseCommand {
	public Debug(Command cmd, Scope scope, PlaybackListener listener) {
		super(cmd, scope, listener);
	}

	public PlaybackResult print() {
		listener.onStart(scope);
		StringBuilder sb = new StringBuilder();
		for (String arg : cmd.getArgs()) {
			sb.append(arg).append(' ');
		}
		listener.onComplete(scope, new Response());
		String message=sb.substring(0, sb.length() - 1);
		listener.onPrint( message + "\n");
		PlaybackResult result = new PlaybackResult
				(PlaybackStatus.OK,
				null,
				scope);
		result.setDebug(message);
		return result;
						
	}

	public PlaybackResult vars() {
		listener.onStart(scope);
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : scope.getVariables().entrySet()) {
			sb.append(entry.getKey()).append('=').append(entry.getValue()).append("\n");
		}
		listener.onComplete(scope, new Response());
		String message=sb.toString();
		listener.onPrint(message);
		
		PlaybackResult result = new PlaybackResult
				(PlaybackStatus.OK,
				null,
				scope);
		result.setDebug(message);
		return result;
	}
}
