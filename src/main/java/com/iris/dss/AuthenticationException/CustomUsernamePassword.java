package com.iris.dss.AuthenticationException;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class CustomUsernamePassword extends UsernamePasswordAuthenticationToken { //AbstractAuthenticationToken | UsernamePasswordAuthenticationToken

	private static final long serialVersionUID = 1L;

	private String username;
	public String getUsername() {
		return username;
	}

	private String password;
	
	private boolean authenticated;
  
	Collection<GrantedAuthority> roles = new ArrayList<>(); 
	
	public CustomUsernamePassword(String principal, String credentials) {
		super(principal, credentials);

		this.username = principal;
		this.password = credentials;
		this.authenticated = false;
	}

	public CustomUsernamePassword( String principal, String credentials, Collection<GrantedAuthority> authorities) {
		super(principal, credentials, authorities);
		//super(principal, credentials, authorities);
		this.username = principal;
		this.password = credentials;
		this.authenticated = false;
		this.roles = authorities;
	}	

	//    public CustomUsernamePasswordAuthToken( String username, String password, List<GrantedAuthority> authorities) {
	//    	super(authorities);
	//    	//super(username, password, authorities);
	//        this.username = username;
	//        this.password = password;
	//    }   



	@Override
	public boolean isAuthenticated() {
		return this.authenticated;
	}

	@Override
	public void setAuthenticated(boolean authenticated) {		
		this.authenticated = authenticated;
	}

	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return this.password;
	}

	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return this.username;
	}

	public Collection<GrantedAuthority> getRoles() {
		return roles;
	}

	public void setRoles(Collection<GrantedAuthority> roles) {
		this.roles = roles;
	}
	

	
}