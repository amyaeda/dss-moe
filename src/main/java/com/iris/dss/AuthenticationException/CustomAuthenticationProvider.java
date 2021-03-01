package com.iris.dss.AuthenticationException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.iris.dss.model.User;
import com.iris.dss.service.LoginAttemptService;
import com.iris.dss.service.UserService;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

	public final String ERROR_MESSAGE = "Authentication Failed!";

	@Autowired
	private UserService userService;	

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;	

	private static final String Anonymous_Role = "ANONYMOUS";

	@Autowired
	private LoginAttemptService loginAttemptService;

	public CustomAuthenticationProvider() {
		super();
	}

	@Override
	public Authentication authenticate(final Authentication auth) throws AuthenticationException 
	{    	    	
		try {
			return performAuthentication(auth);
		} catch (Exception ex) {
			throw new BadCredentialsException("Authentication Provider Error", ex);
		}        
	}

	/**
	 * Authenticate user via username, password
	 * 
	 * @param auth current Authentication Token
	 * @return latest Authentication Token
	 * @throws Exception categorized by username, password
	 */
	public Authentication auth(final Authentication auth) throws Exception 
	{    	    	
		return performAuthentication(auth);        
	}

	private Authentication performAuthentication(final Authentication authentication) throws Exception {
		CustomUsernamePassword customToken = (CustomUsernamePassword) authentication;

		if(customToken.isAuthenticated()) {
			return authentication;
		}

		String loginId = customToken.getPrincipal().toString();
		String password = customToken.getCredentials().toString();    		

		//log.info(">performAuthentication>login Id:"+ loginId);
		
		//log.info(">authentication>login Id:"+ authentication.getName());
		//log.info(">authentication>login Id:"+ authentication.getPrincipal());
		

		try 
		{
			if (loginId!=null && !loginId.trim().isEmpty()) 
			{	       

				//  Password authentication goes here... kawal disini

				User user = userService.findUserByUserNameAndActive(loginId,1);
				User user2 = userService.findUserByUserName(loginId);


				if(user2==null) {
					throw new UsernameNotFoundException(String.format("Pengguna %s tidak wujud!", loginId));
				}
				if(user==null) {
					throw new UsernameNotFoundException(String.format("Pengguna %s tidak aktif!", loginId));
				}
				else {	        			
					String encodedPassword = user.getPassword();

					List<GrantedAuthority> grantedAuths = new ArrayList<>();        		
					user.getRoles().iterator().forEachRemaining( roles -> { 
						String role = roles.getRole();
						if(role != null) { grantedAuths.add(new SimpleGrantedAuthority(role)); }
					} );        		  

					if(grantedAuths.size() < 1) { grantedAuths.add(new SimpleGrantedAuthority(Anonymous_Role)); }  

					if(loginAttemptService.isBlocked(loginId)) {
						log.info("User " + loginId + " is blocked.");
						throw new UserAccountBlockedException("Account is blocked. Please try again after "+ loginAttemptService.getLockedDuration() +" seconds.");
					}
					
					if(!bCryptPasswordEncoder.matches(password, encodedPassword) ) 
					{						
						loginAttemptService.loginFailed(loginId);
						throw new UsernamePasswordVerificationException("Password verification failed");
					}

					//final UserDetails principal = new User(name, password, grantedAuths);                               
					CustomUsernamePassword authenticatedToken = new CustomUsernamePassword(loginId, password, grantedAuths);
					authenticatedToken.setAuthenticated(true);
					authenticatedToken.setDetails(user.getFirstName());
					authenticatedToken.setRoles(grantedAuths);
					//log.info("Details "+authenticatedToken.getDetails());
					return authenticatedToken;

				}

			}
			else {
				throw new UsernamePasswordVerificationException("Login id is empty");  
			} // END -- ELSE --	



		} catch(Exception ex) {
			//ex.printStackTrace();
			//throw new CustomAuthenticationProviderException("Authentication Failed. Reason: " + ex.getMessage(), ex);
			throw ex;			
		} 
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(CustomUsernamePassword.class);
	}

}
