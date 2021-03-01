package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.UserApproval;
 

@Repository("userApprovalRepository")
public interface UserApprovalRepository extends JpaRepository<UserApproval, Long> {
	 UserApproval findById(int id); 
	 UserApproval findByIdAndActive(int id, int active); 
	 UserApproval findByUserId(String userId);
	 UserApproval findByUserIdAndActive(String userId, int active);
	 List<UserApproval> findAll(); 
	 //Sort sort = new Sort(Direction.ASC, "name");
	 List<UserApproval> findAllByActive(int active,Sort sort); 
	 Long countByActive(int active);
	 
	/*
	 * @Query("SELECT COUNT(*) FROM UserApproval u where u.active=1 ") Long
	 * approvalOfNo();
	 */
	 @Query(value = "SELECT email FROM  userapproval u WHERE u.id = :id", nativeQuery = true)
	 String emailApproval(@Param("id") int id );
}