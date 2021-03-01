package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.CertificateFile;
import com.iris.dss.model.UserApproval;
 

@Repository("certificateFileRepository")
public interface CertificateFileRepository extends JpaRepository<CertificateFile, Long> {
	
	 List<CertificateFile> findByUserApprovalAndFileType(UserApproval userPortal,String fileType);
	 CertificateFile findByUserApprovalAndActive(UserApproval userPortal, int active);
	 List<CertificateFile> findAll();
	 CertificateFile findById(int id);
	 
	 List<CertificateFile> findAllByActive(int active);
	 
	 @Query(value = "SELECT * FROM certificatefile c JOIN userapproval u ON c.id=u.id WHERE c.active=1 and u.user_id = :userId", nativeQuery = true)
	 CertificateFile querySignerCertificateFile(@Param("userId") String approvalUserId);
	 
	 @Query(value = "SELECT * FROM certificatefile c JOIN userapproval u ON c.id=u.id WHERE u.active=1 and c.active=1", nativeQuery = true)
	 List<CertificateFile> queryActiveCertificateFile();
	 
	
	
}