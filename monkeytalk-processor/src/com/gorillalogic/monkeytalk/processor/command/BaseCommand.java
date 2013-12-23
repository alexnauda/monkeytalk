package com.gorillalogic.monkeytalk.processor.command;

import java.util.Date;

import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.utils.TimeUtils;

public class BaseCommand {
	protected Command cmd;
	protected Scope scope;
	protected PlaybackListener listener;
	protected PlaybackResult result;
	
	public BaseCommand(Command cmd, Scope scope, PlaybackListener listener) {
		this.cmd = cmd;
		this.scope = scope;
		this.listener = listener;
		result = new PlaybackResult();
	}
	
	public void log(String s) {
		System.out.println(TimeUtils.formatDate(new Date()) + " " + getClass().getSimpleName()
				+ ": " + s);
	}
}
