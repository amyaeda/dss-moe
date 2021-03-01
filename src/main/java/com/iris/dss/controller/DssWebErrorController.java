package com.iris.dss.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DssWebErrorController implements ErrorController {

	private static final Logger log = LoggerFactory.getLogger(DssWebErrorController.class);
	
//	@RequestMapping("/error")
//	public String handleError() {
//		//TODO - Log error
//		return "error";
//	}
	
	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, HttpServletResponse response) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);		
		
		if (status != null) {
			//log.info(">handleError>status: "+ status.toString());			
			Integer statusCode = Integer.valueOf(status.toString());

			if(statusCode == HttpStatus.NOT_FOUND.value()) {
				return "error-404"; // Resource Not Found
			}
			else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				return "error"; // According to OWASP, don't display error 500 
			}
			else if(statusCode == HttpStatus.FORBIDDEN.value()) {
				try {
					response.sendRedirect(request.getContextPath() + "/login.html?message=ACCESS DENIED");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(statusCode == HttpStatus.UNAUTHORIZED.value()) {
				try {
					response.sendRedirect(request.getContextPath() + "/login.html?message=ACCESS DENIED");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "error";
	}
	

	@Override
	public String getErrorPath() {
		return "/error";
	}
	
}


