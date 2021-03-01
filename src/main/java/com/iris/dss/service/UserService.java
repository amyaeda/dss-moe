package com.iris.dss.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iris.dss.model.Role;
import com.iris.dss.model.User;

public interface UserService {
	public User findUserByUserName(String userName); 
	public void saveUser(User user);
		
	void createPasswordResetTokenForUser(User user, String token);
	void changeUserPassword(User user, String password);
    boolean checkIfValidOldPassword(User user, String password); 
    public List<User>findAllUser();
    public User findUserById(int id); 
    
    public List<User> findAllByActiveAndRoles(int active,Role roleId);
    public List<User> findAllByActive(int active);
    //public Page<User> findAllAdminPageable(int active,Role roleId, Pageable pageable);
    
    public User findUserByUserNameAndActive(String userName, int active); 
}