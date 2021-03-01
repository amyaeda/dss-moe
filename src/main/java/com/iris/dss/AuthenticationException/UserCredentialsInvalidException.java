package com.iris.dss.AuthenticationException;

import org.springframework.security.core.AuthenticationException;

public class UserCredentialsInvalidException  extends AuthenticationException {
	private static final long serialVersionUID = 1L;
	public UserCredentialsInvalidException(String msg) {
		super(msg);			
	}
	public UserCredentialsInvalidException(String msg, Exception ex) {
		super(msg, ex);			
	}

}
