package com.iris.dss.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iris.dss.utils.DateTimeUtil;
import com.iris.dss.utils.PdfUtil;

@RestController
@RequestMapping("/api/1")
public class NasStorageController {

	private static final Logger log = LoggerFactory.getLogger(NasStorageController.class);
	
	//@Value("\\\\172.12.0.7\\DAT\\Users\\trlok\\")
	@Value("${app.file.storage.path}")
	private String nsfFolder;

	@RequestMapping(value = "/nfs/file", method = RequestMethod.POST)
	public String writeNFSFile() throws IOException {
		
		String newPath = nsfFolder + "/"+ "file_"+ DateTimeUtil.getFormattedTimeStamp(new Date())+ ".txt";
		log.info(newPath);
		
		FileOutputStream fos = new FileOutputStream(newPath);
		fos.write("This is some dummy data.".getBytes(Charset.forName("UTF-8")));
		fos.flush();
		fos.close();
		
		return newPath; 
	}
	
	@RequestMapping(value = "/nfs/file", method = RequestMethod.GET)
	public void readNFSFile(HttpServletResponse response, @RequestParam String filePath) throws IOException {
		//String newPath = nasDir + "/" + "file_"+ DateTimeUtil.getFormattedTimeStamp(new Date())+ ".txt";
		log.info(">readNFSFile");
		
		String nfsFilePath = nsfFolder + "/" + filePath;
		log.info(">readNFSFile>Download from NFS path -> " + nfsFilePath);
		
		File file = new File(nfsFilePath);
		
		if(file.exists()) {
			byte[] buffers = PdfUtil.fileToByteArray(file);
			
			response.setHeader("Content-disposition", "attachment;filename="+filePath);
			response.setContentType("application/pdf");
			response.getOutputStream().write(buffers);
			response.flushBuffer();
		}
			
	}
}
