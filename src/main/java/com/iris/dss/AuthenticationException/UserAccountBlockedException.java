package com.iris.dss.AuthenticationException;

import org.springframework.security.core.AuthenticationException;

public class UserAccountBlockedException extends AuthenticationException {
	private static final long serialVersionUID = 1L;
	public UserAccountBlockedException(String msg) {
		super(msg);			
	}
	public UserAccountBlockedException(String msg, Exception ex) {
		super(msg, ex);			
	}		

}
