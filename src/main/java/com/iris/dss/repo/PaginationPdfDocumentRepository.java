package com.iris.dss.repo;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.PdfDoc;


@Repository
public interface PaginationPdfDocumentRepository extends PagingAndSortingRepository<PdfDoc, Integer>  {
	
	Page<PdfDoc>  findAll(Pageable pageable);
	@Query(value = "SELECT * FROM pdf_document WHERE not status=0 AND  createdAt BETWEEN  :from AND :to  ", nativeQuery = true)
	Page<PdfDoc> searchfilesByCreatedAt(@DateTimeFormat(pattern = "yyyy-MM-dd") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd") @Param("to") Date to,Pageable pageable);
	@Query(value = "SELECT * FROM pdf_document WHERE  not status=0 AND createdAt BETWEEN  :from AND :to  ", nativeQuery = true)
	Page<PdfDoc> searchfilesByCreatedDate(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd") @Param("to") Date to,Pageable pageable);
	//Page<PdfDoc>  findAllBycreatedAt(@DateTimeFormat(pattern = "yyyy-MM-dd") @Param("from") Date from,Pageable pageable);
	Page<PdfDoc>  findAllByApprovalUserIdAndStatusNot(String approvalUserId,int status,Pageable pageable);
	Page<PdfDoc>  findAllByIptsNameAndStatusNot(String iptsName,int status,Pageable pageable);
	Page<PdfDoc>  findAllByStatus(int status,Pageable pageable);
	Page<PdfDoc>  findAllByStatusNot(int status,Pageable pageable);
	Page<PdfDoc>  findAllByModuleNameAndStatusNot(String moduleName,int status,Pageable pageable);
}
