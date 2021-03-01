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


@Repository
public interface PaginationAuditTrailRepository extends PagingAndSortingRepository<AuditTrail, Integer>  {
	
	Page<AuditTrail>  findAll(Pageable pageable); 
	Page<AuditTrail>  findAllByUser(String userId,Pageable pageable);
	Page<AuditTrail>  findAllByStatus(int status,Pageable pageable);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to ", nativeQuery = true)
	Page<AuditTrail> searchByCreatedAtBetween(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss ") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,Pageable pageable);
    
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to AND user = :user", nativeQuery = true)
	Page<AuditTrail> searchByDateAndUser(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,String user,Pageable pageable);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to AND status = :status", nativeQuery = true)
	Page<AuditTrail> searchByDateAndStatus(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,int status,Pageable pageable);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to AND status = :status AND user = :user", nativeQuery = true)
	Page<AuditTrail> searchAll(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,String user,int status,Pageable pageable);
}
