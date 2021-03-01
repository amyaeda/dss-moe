package com.iris.dss.AuthenticationException;

import org.springframework.security.core.AuthenticationException;

public class UsernamePasswordVerificationException extends AuthenticationException {
	private static final long serialVersionUID = 1L;
	public UsernamePasswordVerificationException(String msg) {
		super(msg);			
	}
	public UsernamePasswordVerificationException(String msg, Exception ex) {
		super(msg, ex);			
	}		

}
