package com.oy.tv.app;


public class UserMessage extends RuntimeException {
	    
	static final long serialVersionUID = 0;

	public UserMessage(String message, Exception cause){
		super(message, cause);
	}
	
	public UserMessage(String message){
		super(message);
	}
	
}
