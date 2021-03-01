package com.iris.dss.authentication.filters; 

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;  
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.iris.dss.model.User; 
import com.iris.dss.service.UserService; 

/**
 * 
 * @author trlok
 *
 */
public class CustomLoginRequestProcessingFilter  extends UsernamePasswordAuthenticationFilter {  
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//private static final String TMP_DIR = System.getProperty("java.io.tmpdir");		
	//private static final boolean __DEBUG = false;
	@Autowired
	UserService userService;
	
	//@Autowired
	//UserDetailsService userDetailsService;
	
	//@Autowired
	//private BCryptPasswordEncoder passwordEncoder;
	
	private AuthenticationManager mAuthManager =  null;
	private AuthenticationProvider mAuthProvider = null;
	
	public CustomLoginRequestProcessingFilter() { 		
	}
	
	public CustomLoginRequestProcessingFilter(AuthenticationManager authManager, AuthenticationProvider custAuthProvider) {
 
		this.mAuthManager = authManager;	
		this.mAuthProvider = custAuthProvider;
	}
	 
			
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
			throws AuthenticationException 	{
		
		logger.info(">CustomLoginRequestProcessingFilter");
	
		String username = request.getParameter("user");				
		String password = request.getParameter("tok");	
		 						
		try	{		
			
			
			UsernamePasswordAuthenticationToken userAndPasswordToken = new UsernamePasswordAuthenticationToken(username, password); 	
						
			// Allow subclasses to set the "details" property
			setDetails(request, userAndPasswordToken);			
			
			
			User user = userService.findUserByUserName(username);
			//UserDetails userDetails = userDetailsService.loadUserByUsername(username); 
				
			List<GrantedAuthority> grantedAuths = new ArrayList<>();                		
			user.getRoles().iterator().forEachRemaining( roles -> { 
				String role = roles.getRole();
    			if(role != null) {  
    				grantedAuths.add(new SimpleGrantedAuthority(role));  
    			}
			} );                 		

    		if(grantedAuths.size() < 1) { 
    			grantedAuths.add(new SimpleGrantedAuthority("ANONYMOUS")); 
    		}
    		
    		
    		UsernamePasswordAuthenticationToken userPasswordToken = new UsernamePasswordAuthenticationToken(username.trim(), password.trim());
			Authentication authenticatedToken = this.mAuthManager.authenticate(userPasswordToken);
			 
			//Authentication authenticatedToken = this.mAuthProvider.authenticate(token);
			
			// Inject token into SecurityContextHolder object
			SecurityContextHolder.getContext().setAuthentication(authenticatedToken);						
						 			
			return authenticatedToken;				
			
		} catch(AuthenticationException ae) {	
			ae.printStackTrace();
			logger.error(">CustomLoginRequestProcessingFilter>AuthenticationException. {}"+ ae.getMessage());			 
		} catch(Exception ex) {		
			ex.printStackTrace();
			logger.error(">CustomLoginRequestProcessingFilter>Exception. {}"+ ex.getMessage());	
			throw new CustomLoginRequestProcessingFilterException(ex.getMessage(), ex);
		}	
					
		logger.error(">CustomLoginRequestProcessingFilter end.");		
		return null;
		
	} 
	 
	class CustomLoginRequestProcessingFilterException extends AuthenticationException {
		private static final long serialVersionUID = 1L;
		public CustomLoginRequestProcessingFilterException(String msg) {
			super(msg);			
		}
		public CustomLoginRequestProcessingFilterException(String msg, Exception ex) {
			super(msg, ex);			
		}		
	}
		
}
