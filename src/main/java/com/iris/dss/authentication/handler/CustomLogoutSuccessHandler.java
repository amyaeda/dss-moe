package com.iris.dss.authentication.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);
	
	//@Autowired
	//private AuditService auditService;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
			throws IOException, ServletException {

		if(null != authentication) {
			log.info("CustomLogoutSuccessHandler {}", "User: "+ authentication.getName());
		}
				
		String refererUrl = request.getHeader("Referer");
		log.info("CustomLogoutSuccessHandler {}", "refererUrl: "+ refererUrl);
		
		//auditService.track("Logout from: " + refererUrl);
		super.onLogoutSuccess(request,response,authentication);
	}

}
