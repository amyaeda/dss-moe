package com.iris.dss.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.UserApproval;


@Repository
public interface PaginationUserApprovalRepository extends PagingAndSortingRepository<UserApproval, Long>  {
	
	
	 @Query(value = "SELECT * FROM userapproval u WHERE u.active= :active order by id Desc", nativeQuery = true)
	 Page<UserApproval> queryfindAllByActive(int active,Pageable pageable); 
	 @Query(value = "SELECT * FROM userapproval u WHERE u.active=1 and u.name like %:name% ", nativeQuery = true)
	 Page<UserApproval> queryFindUserApprovalByName(@Param("name") String name,Pageable pageable);
}
