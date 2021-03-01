package com.iris.dss.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.iris.dss.model.UserApproval;
import com.iris.dss.repo.UserApprovalRepository;

@Service("userApprovalService")
public class UserApprovalService {

	@Autowired
	private UserApprovalRepository userApprovalRepository;
	 
	public List<UserApproval> findAllUserApproval() { 
		 Sort sort = new Sort(Direction.ASC, "name");
		return userApprovalRepository.findAllByActive(1,sort);
	}
	
	public List<UserApproval> findAllUserApprovalActive(int active) { 
		 Sort sort = new Sort(Direction.ASC, "name");
		return userApprovalRepository.findAllByActive(active,sort);
	}
	
	public UserApproval findUserApprovalById(int id) {
		return userApprovalRepository.findById(id);
	}
	
	public String emailUserApproval(int id) {
		
		return userApprovalRepository.emailApproval(id);
	}
	
	
	
}
