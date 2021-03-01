package com.iris.dss.AuthenticationException;

import org.springframework.security.core.AuthenticationException;

public class CustomAuthenticationProviderException extends AuthenticationException {
	private static final long serialVersionUID = 1L;
	public CustomAuthenticationProviderException(String msg) {
		super(msg);			
	}
	public CustomAuthenticationProviderException(String msg, Exception ex) {
		super(msg, ex);			
	}		
}
