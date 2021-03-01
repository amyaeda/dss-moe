package com.iris.dss.repo;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.AuditTrail;
import com.iris.dss.model.Role;
import com.iris.dss.model.User;


@Repository
public interface PaginationUserRepository extends PagingAndSortingRepository<User, Long>  {
	
	/*
	 * AuditTrail findByModule(String module); AuditTrail findByStatus(int status);
	 * List<AuditTrail> findAll(); List<AuditTrail> findAllByUser(String userId);
	 * List<AuditTrail> findAllByStatus(int status);
	 * 
	 * @Query(value =
	 * "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to ",
	 * nativeQuery = true) List<AuditTrail>
	 * searchByCreatedAtBetween(@DateTimeFormat(pattern =
	 * "yyyy-MM-dd") @Param("from") Date from, @DateTimeFormat(pattern =
	 * "yyyy-MM-dd") @Param("to") Date to);
	 * 
	 */
	/*
	 * SELECT E.*,D.*
FROM user AS E
    LEFT JOIN user_role AS ED ON E.user_id  = ED.user_id 
    LEFT JOIN role AS D ON ED.role_id  = D.role_id 
	 */
	
	

	
	 Page<User> findAllByActiveAndRolesOrderByIdDesc(int active,Role roleId,Pageable pageable); 
	 Page<User> findAllByActiveAndRolesAndFirstNameContaining(int active ,Role roleId,@Param("nameSearch") String nameSearch, Pageable pageable);
		
	
}
