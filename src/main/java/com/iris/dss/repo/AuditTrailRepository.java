package com.iris.dss.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.AuditTrail;


@Repository("auditTrailRepository")
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Integer>  {
	
	AuditTrail findByModule(String module); 
	AuditTrail findByStatus(int status);
	List<AuditTrail>  findAll(); 
	List<AuditTrail>  findAllByUser(String userId);
	List<AuditTrail>  findAllByStatus(int status);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to ", nativeQuery = true)
    List<AuditTrail> searchByCreatedAtBetween(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to AND user = :user", nativeQuery = true)
	List<AuditTrail> searchByDateAndUser(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,String user);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to AND status = :status", nativeQuery = true)
	List<AuditTrail> searchByDateAndStatus(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,int status);
	
	@Query(value = "SELECT * FROM audit_trail WHERE createdAt BETWEEN  :from AND :to AND status = :status AND user = :user", nativeQuery = true)
	List<AuditTrail> searchAll(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,String user,int status);

}
