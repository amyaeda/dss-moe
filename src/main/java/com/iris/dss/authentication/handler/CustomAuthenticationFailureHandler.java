package com.iris.dss.authentication.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.iris.dss.AuthenticationException.UserAccountBlockedException;
//import com.iris.dss.AuthenticationException.UsernameNotFoundException;
import com.iris.dss.AuthenticationException.UsernamePasswordVerificationException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler { 
	private Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

	public CustomAuthenticationFailureHandler() {
		super();
	}

	public CustomAuthenticationFailureHandler(String defaultFailureUrl) {
		super(defaultFailureUrl);
	}

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest request, 
			HttpServletResponse response, AuthenticationException ex) throws IOException, ServletException {

		//String username =  obtainUsername(request);		
		//log.info(">CustomAuthenticationFailureHandler>onAuthenticationFailure>username: " + username);

		if(ex instanceof UsernamePasswordVerificationException) 
		{
			log.info("UsernamePasswordVerificationException"); 
			getRedirectStrategy().sendRedirect(request, response, "/?error=true&message=ID Daftar Masuk / Kata Laluan Tidak Sah!");
			//this.getRedirectStrategy().sendRedirect(request, response, "/login.html?error=true&message="+ URLEncoder.encode("Username or/and password invalid (0xA001)", "UTF-8"));
		} 
		else if(ex instanceof UsernameNotFoundException) 
		{
			log.info("UsernameNotFoundException"); 
			getRedirectStrategy().sendRedirect(request, response, "/?error=true&message="+ ex.getMessage()); 

		}	
		else if(ex instanceof UserAccountBlockedException) {
			log.info("UserAccountBlockedException"); 
			getRedirectStrategy().sendRedirect(request, response, "/?error=true&message="+ ex.getMessage());
		}

	}

	protected String obtainUsername(HttpServletRequest request)  {
		return request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY);
	}

}