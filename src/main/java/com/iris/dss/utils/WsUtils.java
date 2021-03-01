package com.iris.dss.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;


//enum JsonDataType {
//	String_Type,
//	Boolean_Type,
//	Number_Type	
//}

public class WsUtils {	

	private static final Logger log = LoggerFactory.getLogger(WsUtils.class);

	//private static final String TMP_DIR = System.getProperty("java.io.tmpdir");		
					
	public static JSONObject parseRequestToJsonObject(String request) {
		log.info(">WSUtil>parseRequestJson(...) ");
		try {    	
    		return new JSONObject(request);
		} catch (JSONException e) {
			log.error(">WSUtil>parseRequestJson>Error on construct JSONObject from request. "+ e.getMessage());
			e.printStackTrace();
		}		
		return null;
	}
	
	public static byte[] readBytesFromFile(String filePath) {
    	FileInputStream fileInputStream = null;
    	byte[] bytesArray = null;
    	try {
    		File file = new File(filePath);
    		bytesArray = new byte[(int) file.length()];
    		fileInputStream = new FileInputStream(file);
    		fileInputStream.read(bytesArray);
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		if (fileInputStream != null) {
    			try {
    				fileInputStream.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	return bytesArray;
    }
	
	public static String getFileDownloadURLPath(
			HttpServletRequest request, String fileName,String hostname) throws UnsupportedEncodingException {
		log.info(">getFileDownloadURLPath");
		String uRLPath = 				
				request.getScheme() + "://" +
						hostname+request.getServletContext().getContextPath();
						//request.getServerName() + ":" + 		
						//request.getServerPort() + 				
						
		log.info(">uRLPath"+uRLPath);
		//https://eiptscertdev.mohe.gov/dss-mohe/api/1/nfs/file?filePath=signed_1608174626058.pdf
        
        return uRLPath+"/api/1/nfs/file?filePath="+ URLEncoder.encode(fileName, "UTF-8");
	}

}
