package com.iris.dss.service;
 
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.iris.dss.model.PasswordResetToken;
import com.iris.dss.model.Role;
import com.iris.dss.model.User;
import com.iris.dss.repo.PaginationUserRepository;
import com.iris.dss.repo.PasswordResetTokenRepository;
import com.iris.dss.repo.RoleRepository;
import com.iris.dss.repo.UserRepository;
 


@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private RoleRepository roleRepository;
	
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
 
    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;
    
    @Autowired
    private PaginationUserRepository paginationUserRepository;
    
    @Override
    public void createPasswordResetTokenForUser(final User user, final String token) {
        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }
    
    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return bCryptPasswordEncoder.matches(oldPassword, user.getPassword());
    }    
      
	@Override
	public User findUserByUserName(String userName) {
		return userRepository.findByUserName(userName);
	} 

	@Override
	public void saveUser(User user) { 
		userRepository.save(user);
	}
	
	public List<User> findAllUser() { 
		return userRepository.findAll();
	}
	
	public User findUserById(int id) {
		return userRepository.findById(id);
	} 
	
	public List<User> findAllByActiveAndRoles(int active,Role roleId) { 
		 Sort sort = new Sort(Direction.ASC, "userName");
		return userRepository.findAllByActiveAndRoles(active,roleId,sort);
	}
	
	/*
	 * public Page<User> findAllAdminPageable(int active,Role roleId, Pageable
	 * pageable) {
	 * 
	 * Page<User> list = paginationUserRepository.findAllByActiveAndRoles(active,
	 * roleId, pageable);
	 * 
	 * return list;
	 * 
	 * }
	 */
@Override
public List<User> findAllByActive(int active) {
	// TODO Auto-generated method stub
	return userRepository.findAllByActive(active);
}

public User findUserByUserNameAndActive(String username, int active) {
	// TODO Auto-generated method stub
	return userRepository.findByUserNameAndActive(username,active);
}



	
	

}