package com.gorillalogic.monkeyconsole.editors.utils;

public class CloudServiceException extends Exception {
	String message;
 public CloudServiceException(String message, Exception ex){
	 this.message = message;
 }
 public CloudServiceException(String message){
	 this.message = message;
 }
@Override
public String getMessage() {
	return message;
}
}
