package com.iris.dss.repo;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.RestApiRequestRecord;


@Repository
public interface PaginationRestApiTransactionRepository extends PagingAndSortingRepository<RestApiRequestRecord, Integer>  {
	
	Page<RestApiRequestRecord>  findAll(Pageable pageable);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName  ", nativeQuery = true)
	Page<RestApiRequestRecord> searchByModuleAndIptsName(@Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName,Pageable pageable);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to  ", nativeQuery = true)
	Page<RestApiRequestRecord> searchByCreatedAtBetween(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName,Pageable pageable);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to AND c.approvalUserId = :userId ", nativeQuery = true)
	Page<RestApiRequestRecord> searchByApprovalUserId(@Param("userId") String userId, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to, Pageable pageable);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName  AND u.createdAt BETWEEN  :from AND :to AND u.status = :status  ", nativeQuery = true)
	Page<RestApiRequestRecord> searchByStatus(@Param("status") String status, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,Pageable pageable);
	
	@Query(value = "SELECT * FROM rest_api_transaction u  JOIN pdf_document c  ON u.pdfDocId=c.id WHERE u.module = :module AND c.iptsName = :iptsName AND c.moduleName = :moduleName AND u.createdAt BETWEEN  :from AND :to AND u.status = :status AND c.approvalUserId = :userId ", nativeQuery = true)
	Page<RestApiRequestRecord> searchAll(@Param("userId") String userId,@Param("status") String status, @Param("module") String module,@Param("iptsName") String iptsName,@Param("moduleName") String moduleName, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("to") Date to,Pageable pageable);
}
