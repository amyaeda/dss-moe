package com.iris.dss.AuthenticationException;

import org.springframework.security.core.AuthenticationException;

public class CustomOAuthRequestProcessingFilterException extends AuthenticationException {
	private static final long serialVersionUID = 1L;
	public CustomOAuthRequestProcessingFilterException(String msg) {
		super(msg);			
	}
	public CustomOAuthRequestProcessingFilterException(String msg, Exception ex) {
		super(msg, ex);			
	}		
}
