package com.iris.dss.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iris.dss.model.User;
import com.iris.dss.repo.UserRepository;
 

@Service("securityService")
public class SecurityService {
	private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

	@Autowired
	private UserRepository  userRepository;

	public boolean isLoggedInUser(String id, String userName) {
		
		User user = userRepository.findByUserNameAndActive(userName, 1);
		if(null!=user) {
			log.info("id = "+ user.getId() + "|Logged-in user = "+ userName); 
			if(id!=null && !id.isEmpty() ) {
				return user.getId() == Integer.parseInt(id);
			}else {
				return true;
			}
			
		}
		return false;
	}
}
