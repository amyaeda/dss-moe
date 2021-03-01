package com.iris.dss.repo;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.PdfDoc;

@Repository("pdfDocumentRepository")
public interface PdfDocumentRepository extends JpaRepository<PdfDoc, Integer>  {
	
	PdfDoc findBySha256Checksum(String sha256Checksum);
	PdfDoc findByQrcodeSerialNum(String qrcodeSerialNum);
	PdfDoc findByIptsName(String iptsName);
	List<PdfDoc>  findAll();
	List<PdfDoc> findAllByCheckPaymentAndPaid(int checkPayment, int paid);
	PdfDoc findById(int id);
	

	@Query(value = "SELECT * FROM pdf_document c JOIN rest_api_transaction u ON c.requestId=u.requestId WHERE u.module = :module AND c.iptsName = :iptsName  ", nativeQuery = true)
	List<PdfDoc> searchByModuleAndIptsName(@Param("module") String module,@Param("iptsName") String iptsName);
	
	@Query(value = "SELECT * FROM pdf_document c JOIN rest_api_transaction u ON c.requestId=u.requestId WHERE u.module = :module AND c.iptsName = :iptsName AND u.createdAt BETWEEN  :from AND :to  ", nativeQuery = true)
    List<PdfDoc> searchByCreatedAtBetween(@DateTimeFormat(pattern = "yyyy-MM-dd") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd") @Param("to") Date to, @Param("module") String module,@Param("iptsName") String iptsName);
	
	@Query(value = "SELECT * FROM pdf_document c JOIN rest_api_transaction u ON c.requestId=u.requestId WHERE u.module = :module AND c.iptsName = :iptsName AND c.approvalUserId = :userId ", nativeQuery = true)
    List<PdfDoc> searchByApprovalUserId(@Param("userId") String userId, @Param("module") String module,@Param("iptsName") String iptsName);
	
	@Query(value = "SELECT * FROM pdf_document c JOIN rest_api_transaction u ON c.requestId=u.requestId WHERE u.module = :module AND c.iptsName = :iptsName AND u.status = :status  ", nativeQuery = true)
    List<PdfDoc> searchByStatus(@Param("status") String status, @Param("module") String module,@Param("iptsName") String iptsName);
	
	@Query(value = "SELECT * FROM pdf_document WHERE createdAt BETWEEN  :from AND :to AND approvalUserId= :userId", nativeQuery = true)
    List<PdfDoc> searchByPriorityBetween(@DateTimeFormat(pattern = "yyyy-MM-dd") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd") @Param("to") Date to, @Param("userId") String userId);
	
	@Query(value = "SELECT * FROM pdf_document a WHERE a.requestId = :requestId AND a.status = :status ORDER BY createdAt DESC LIMIT 1", nativeQuery = true)
    PdfDoc searchByRequestIdAndStatus(@Param("requestId") String requestId, @Param("status") int status );
	
	
	/*
	 * @Query(value = "SELECT * FROM pdf_document WHERE approvalUserId :userId",
	 * nativeQuery = true) List<PdfDoc> searchByApprovaluserId(@Param("userId")
	 * String userId);
	 */
	
	@Query(value = "SELECT * FROM pdf_document WHERE createdAt BETWEEN  :from AND :to  ", nativeQuery = true)
    List<PdfDoc> searchfilesByCreatedAt(@DateTimeFormat(pattern = "yyyy-MM-dd") @Param("from") Date from, @DateTimeFormat(pattern = "yyyy-MM-dd") @Param("to") Date to);
	List<PdfDoc>  findAllByApprovalUserId(String approvalUserId);
	List<PdfDoc>  findAllByIptsName(String iptsName);
	List<PdfDoc>  findAllByStatus(int status);
	List<PdfDoc>  findAllByStatusAndApprovalUserId(int status, String approvalUserId);
	//new api request
	@Query(value = "SELECT * FROM pdf_document c WHERE c.docName = :docName AND c.iptsName = :iptsName ", nativeQuery = true)
	PdfDoc  searchPdfdoc(String docName, String iptsName);
	
	@Query(value = "SELECT * FROM pdf_document c WHERE c.docName = :docName OR c.sha256ChecksumB4Sign = :sha256ChecksumB4Sign ", nativeQuery = true)
	List<PdfDoc>  findAllByDocNameAndSha256ChecksumB4Sign(String docName, String sha256ChecksumB4Sign);
	
}
