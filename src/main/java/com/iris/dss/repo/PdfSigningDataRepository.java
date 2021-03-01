package com.iris.dss.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.PdfDoc;
import com.iris.dss.model.PdfSigningData;


@Repository("pdfSigningDataRepository")
public interface PdfSigningDataRepository extends JpaRepository<PdfSigningData, Integer>  {
	
	List<PdfSigningData>  findAll();
	PdfSigningData findByRequestId(String requestId);
	
	
	@Query(value = "SELECT * FROM pdf_signing_data a WHERE a.requestId = :requestId ORDER BY a.signingTime DESC LIMIT 1", nativeQuery = true)
	PdfSigningData searchByRequestIdOrderBySigningTime(@Param("requestId") String requestId);
	
}
