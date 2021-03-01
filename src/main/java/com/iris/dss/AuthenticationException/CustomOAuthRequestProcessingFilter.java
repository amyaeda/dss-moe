package com.iris.dss.AuthenticationException;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



/**
Security filter chain: [
  WebAsyncManagerIntegrationFilter
  SecurityContextPersistenceFilter
  HeaderWriterFilter
  LogoutFilter
  CustomOAuthRequestProcessingFilter
  UsernamePasswordAuthenticationFilter
  RequestCacheAwareFilter
  SecurityContextHolderAwareRequestFilter
  AnonymousAuthenticationFilter
  SessionManagementFilter
  ExceptionTranslationFilter
  FilterSecurityInterceptor
]

 * @author trlok
 * 
 */
public class CustomOAuthRequestProcessingFilter 
					extends UsernamePasswordAuthenticationFilter { // GenericFilterBean, OncePerRequestFilter
		
	private static final boolean __DEBUG = false;
	
	private AuthenticationManager mAuthManager =  null;
	private CustomAuthenticationProvider mCustAuthProvider = null;
	
	public CustomOAuthRequestProcessingFilter() { }
	
	public CustomOAuthRequestProcessingFilter(AuthenticationManager authManager, CustomAuthenticationProvider custAuthProvider) {
		//super();
		this.mAuthManager = authManager;	
		this.mCustAuthProvider = custAuthProvider;
	}	
		
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) 
			throws AuthenticationException 	{
		
		//System.out.println(">CustomOAuthRequestProcessingFilter --- START ---" + new Date());
		
		String userId = request.getParameter("username");				
		String password = request.getParameter("password");	
				
		boolean usePassword = false;
		usePassword = (password != null) && (!password.isEmpty()); 		
						
		try	{	
			//System.out.println("masuk dlm ni");
			if(userId != null)	{
				
				userId = userId.trim();
				
				CustomUsernamePassword authRequest = new CustomUsernamePassword(userId, password); 	

				// Check if user login with selfie photo
				// data from <input type="file" name="facial"> 				
				
				if(!usePassword) 
				{					
					// we will use password for authentication
					// throw exception if password if not available 
					
					if(password == null || password.isEmpty()) 
					{	
						throw new UserCredentialsInvalidException(
								"No sufficient information supplied. Please provide either password or video or photo" );
					}
				}
									
				// Allow subclasses to set the "details" property
				setDetails(request, authRequest);									
				// Invoke Authentication Provider 
				CustomUsernamePassword authToken = (CustomUsernamePassword) this.mCustAuthProvider.auth(authRequest);
				// Inject token into SecurityContextHolder object
				SecurityContextHolder.getContext().setAuthentication(authToken);						
							
				//System.out.println(">CustomOAuthRequestProcessingFilter --- END ---" + new Date());
				
				return authToken;								
				
			} // END IF	
			
		} catch(AuthenticationException ae) {			
			System.out.println(">CustomOAuthRequestProcessingFilter AuthenticationException. Reason: "+ ae.getMessage());	
			//throw new UsernameNotFoundException(String.format("Pengguna %s tidak wujud!", userId));
			throw ae;
		} catch(Exception ex) {		
			//SecurityContextHolder.clearContext();				
			System.out.println(">CustomOAuthRequestProcessingFilter Exception. Reasonnnnnnnnnnnnnnn: "+ ex.getMessage());	
			throw new CustomOAuthRequestProcessingFilterException(ex.getMessage(), ex);
		}	
					
		System.out.println(">CustomOAuthRequestProcessingFilter --- END ---" + new Date());		
		return null;
		
	}
	

	
	
	
		
}
