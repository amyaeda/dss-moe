package com.iris.dss.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.RestApiRequestRecord;
 
@Repository("restApiTransactionRepository")
public interface RestApiTransactionRepository extends JpaRepository<RestApiRequestRecord, Integer>  {
	
	RestApiRequestRecord findByStatus(int status); 
	RestApiRequestRecord findByModule(String module); 
	List<RestApiRequestRecord>  findAll(); 
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName  ", nativeQuery = true)
	List<RestApiRequestRecord> searchByModuleAndIptsName(@Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to  ", nativeQuery = true)
	List<RestApiRequestRecord> searchByCreatedAtBetween(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to  AND c.approvalUserId = :userId ", nativeQuery = true)
	List<RestApiRequestRecord> searchByApprovalUserId(@Param("userId") String userId, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to  AND u.status = :status ", nativeQuery = true)
	List<RestApiRequestRecord> searchByStatus(@Param("status") String status, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to  AND u.status = :status AND c.approvalUserId = :userId  ", nativeQuery = true)
	List<RestApiRequestRecord> searchAll(@Param("userId") String userId,@Param("status") String status, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to);

}
