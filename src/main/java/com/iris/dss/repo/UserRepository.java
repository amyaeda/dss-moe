package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.Role;
import com.iris.dss.model.User;
 

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
	 User findByUserName(String userName); 
	 User findByUserNameAndActive(String userName,int active); 
	 User findById(int id); 
	 List<User> findAll(); 
	 List<User> findAllByActiveAndRoles(int active,Role roleId,Sort sort); 
	 List<User> findAllByActive(int active);
	
}