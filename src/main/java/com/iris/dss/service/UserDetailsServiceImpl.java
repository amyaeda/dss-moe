package com.iris.dss.service;
 
import java.net.URLEncoder;
import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.iris.dss.AuthenticationException.UsernamePasswordVerificationException;
import com.iris.dss.model.User;
import com.iris.dss.repo.UserRepository;

//@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
   
    @Autowired
    private UserRepository userRepository;
    
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        
    	
 			
 			User user = userRepository.findByUserNameAndActive(userName,1);
 	        
 	        if (user == null) {
 	        	throw new UsernameNotFoundException(String.format("User %s does not exist!", userName));
 	        	//this.getRedirectStrategy().sendRedirect(request, response, "/login.html?error=true&message="+ URLEncoder.encode("Username or/and password invalid (0xA001)", "UTF-8"));
 	        	//throw new UsernameNotFoundException(String.format("User %s does not exist!", userName));
 			}
 	        
 			return new UserRepositoryUserDetails(user);
 			
 		
    	
        
        
       // final List<GrantedAuthority> authorities = new ArrayList<>();
       // user.getRoles().forEach((r) -> authorities.add(new SimpleGrantedAuthority(r)));

		/*
		 * return new org.springframework.security.core.userdetails.User(
		 * user.getUserName(), user.getPassword(), true, true, true, true, null );
		 */
        
       // log.info("loadUserByUsername() userName: {}", userName);
       // new UserRepositoryUserDetails(user);
    }
    
    private final static class UserRepositoryUserDetails extends User implements UserDetails {

		private static final long serialVersionUID = 1L;

		private User user;
		
		private UserRepositoryUserDetails(User user) {
			this.user = user;
		}
			
		//private UserRepositoryUserDetails(User user) {
		//	super(user);
		//}
  
		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() { 
			//return getAuthorities();
			return user
					.getRoles()
					.stream()
					.map(role -> new SimpleGrantedAuthority(role.getRole().toString()))
					.collect(Collectors.toList());
		}

		@Override
		public String getPassword() { 
			return user.getPassword();
		}

		@Override
		public String getUsername() { 
			return user.getUserName();
		}

	}
}
