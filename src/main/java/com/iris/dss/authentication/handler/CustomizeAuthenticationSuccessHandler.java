package com.iris.dss.authentication.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component; 
 
@Component
public class CustomizeAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {  
	private Logger logger = LoggerFactory.getLogger(this.getClass());
		
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		
		logger.info("CustomizeAuthenticationSuccessHandler");
				
        response.sendRedirect(request.getContextPath() + "/home");
        
        logger.info("CustomizeAuthenticationSuccessHandler. {}", "Redirect user to DSS home");
        
        return;        
        
        //super.onAuthenticationSuccess(request, response, authentication);
	}
}
