package com.iris.dss.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iris.dss.model.PreviewPdfSetting; 

@Repository("pdfPreviewSettingRepository")
public interface PdfPreviewSettingRepository extends JpaRepository<PreviewPdfSetting, Integer>  {
	
	PreviewPdfSetting findById(int id); 
	

}
