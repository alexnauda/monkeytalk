package com.gorillalogic.agents.html.processor;

import com.gorillalogic.monkeytalk.Command;


public class CommandResponse {
	
	private boolean _success = false;
	private Command _command;
	private VerifyState _verify;
	
	public CommandResponse(Command cmd) {
		_command = cmd;
	}	
	
	public Boolean isSuccess() {
		if(_verify != null) {
			_success = _verify.isSuccess();
		}
		return _success;
	}
	
	public boolean isVerify() {
		return (_verify != null) ? true : false;
	}
	
	public Command getCommand() {
		return _command;
	}
	
	public VerifyState getVerify() {
		return _verify;
	}
	
	protected void setVerify(VerifyState vfy) {
		_verify = vfy;
	}
	
	protected void setSuccess(boolean success) {
		_success = success;
	}
}
