package com.iris.dss.controller;
  
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.aspectj.util.FileUtil;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;
import com.iris.dss.model.CertificateFile;
import com.iris.dss.model.PdfDoc;
import com.iris.dss.model.PdfSigningData;
import com.iris.dss.model.PreviewPdfSetting;
import com.iris.dss.model.RestApiRequestRecord;
import com.iris.dss.model.SystemSetting;
import com.iris.dss.model.Token;
import com.iris.dss.model.Ipts;
import com.iris.dss.model.Modul;
import com.iris.dss.model.UserApproval;
import com.iris.dss.pdfbox.IrisPDFBox;
import com.iris.dss.pdfbox.core.DssSigningData;
import com.iris.dss.pdfbox.util.PdfSigningHelper;
import com.iris.dss.repo.CertificateFileRepository;
import com.iris.dss.repo.PdfDocumentRepository;
import com.iris.dss.repo.PdfPreviewSettingRepository;
import com.iris.dss.repo.PdfSigningDataRepository;
import com.iris.dss.repo.RestApiTransactionRepository;
import com.iris.dss.repo.SystemSettingRepository;
import com.iris.dss.repo.TokenRepository;
import com.iris.dss.repo.IptsRepository;
import com.iris.dss.repo.ModulRepository;
import com.iris.dss.repo.UserApprovalRepository;
import com.iris.dss.service.CertificateFileService;
import com.iris.dss.utils.AESKeyUtil;
import com.iris.dss.utils.ApiError;
import com.iris.dss.utils.AuditTrailConstant;
import com.iris.dss.utils.CRLVerifier;
import com.iris.dss.utils.CreateVisibleSignature;
import com.iris.dss.utils.DateTimeUtil;
import com.iris.dss.utils.PdfUtil;
import com.iris.dss.utils.QRCodeUtil;
import com.iris.dss.utils.RestApiConstant;
import com.iris.dss.utils.SendmailUtils;
import com.iris.dss.utils.UUIDUtil;
import com.iris.dss.utils.WSError;
import com.iris.dss.utils.WsUtils;

/**
 * This class consist of all the REST API.
 * 
 * @since 2019
 * @version 0.0.0.1
 * @author trlok
 *
 */
@RestController
@RequestMapping("/api/1")
public class RestApiController {
	
	private static final Logger log = LoggerFactory.getLogger(RestApiController.class);

	@Autowired
	private TokenRepository tokenRepository;
	
	@Autowired
	PdfDocumentRepository pdfDocRepository;
	
	@Autowired
	PdfPreviewSettingRepository previewSettingRepository;
	
	@Autowired
	CertificateFileRepository certFileRepository;
	
	@Autowired
	RestApiTransactionRepository restApiTranRepository;
	
	@Autowired
	UserApprovalRepository userApprovalRepository;
	
	@Autowired
	PdfSigningDataRepository signingDataRepository;
	
	@Autowired
	private CertificateFileService certificateFileService;
	
	@Autowired
	private SystemSettingRepository systemSettingRepository;
	
	@Autowired
	private IptsRepository iptsRepository;
	
 	
	@Value("${app.file.storage.path}")
	String fileStoragePath;
	
	@Value("${message.success}")
	String success;
	
	@Value("${pdf.footer.text}")
	String footer;
	
	@Value("${proxy.enabled}")
	boolean enabledProxy;		
	@Value("${proxy.host}")
	String proxyHost;	
	@Value("${proxy.port}")
	String proxyPort;
	//@Value("${proxy.auth}")
	//boolean proxyRequiredAuthentication;
	//@Value("${proxy.user}")
	//String proxyUser;	
	//@Value("${proxy.pass}")
	//String proxyPass;
	
	@Value("${qrcode.pos.x}")
	float qrcodePosX;
	@Value("${qrcode.pos.y}")
	float qrcodePosY;
	@Value("${qrcode.height}")
	float qrcodeHeight;
	@Value("${qrcode.width}")
	float qrcodeWidth;

	@Value("${message.1000}")
	String message1000;
	@Value("${message.1001}")
	String message1001;
	@Value("${message.1002}")
	String message1002;
	@Value("${message.1003}")
	String message1003;
	@Value("${message.1004}")
	String message1004;
	@Value("${message.1005}")
	String message1005;
	@Value("${message.1008}")
	String message1008;
	
	@Value("${message.1009}")
	String message1009;
	
	
	@Value("${message.2000}")
	String message2000;
	@Value("${message.2001}")
	String message2001;
	@Value("${message.2002}")
	String message2002;
	@Value("${message.2003}")
	String message2003;

	@Value("${message.3000}")
	String message3000;
	@Value("${message.3001}")
	String message3001;
	
	@Value("${message.4000}")
	String message4000;
	@Value("${message.4001}")
	String message4001;
	@Value("${message.4002}")
	String message4002;
	@Value("${message.4003}")
	String message4003;
	
	@Value("${message.5000}")
	String message5000;
	
	//englisg term
	@Value("${2000}")
	String m2000;
	@Value("${1003}")
	String m1003;
	@Value("${4002}")
	String m4002;
	@Value("${4003}")
	String m4003;
	
	//new api request 29/09/2020
	@Value("${message.signing.success}")
	String msgSigningSuccess;
	@Value("${message.signing.failed}")
	String msgSigningFailed;
	@Value("${message.signing.pending}")
	String msgSigningPending;
	@Value("${message.norecordfound}")
	String msgNoRecordFound;
	@Value("${message.4004}")
	String message4004;
	
	 
	static final int PDF_PREVIEW_SETTING_DEFAULT_VALUE = -1;
	static final long FULL_PDF_PREVIEW_MAX_FILE_SIZE = 1000000L; // 1MB
	
	@Value("${hostname}")
	String hostname;
	
	private static final float FONT_SIZE = 8.0f;
	
	@GetMapping("/mail/test/{recipientAddress}/")
	@ResponseBody
	String testMail(@PathVariable String recipientAddress) {
		log.info(">testMail. recipient = {}", recipientAddress); 
		try {
			List<SystemSetting> emailSetting = systemSettingRepository.findAll();
			log.info(">testMail. emailSetting.size() = {}", emailSetting.size()); 
			if(emailSetting.size() > 0) {
				SimpleMailMessage simpleMail = new SimpleMailMessage();        
				simpleMail.setFrom("irisca.noreply@iris.com.my");
				simpleMail.setTo(recipientAddress);
				simpleMail.setSubject("DSS - Test");
				simpleMail.setText("This is a test mail sending from MOE-DSS REST API.");
				SendmailUtils.getJavaMailSender(emailSetting.get(0)).send(simpleMail);
				return "{\"success\":true, \"message\":\"\"}";
			} else {
				return "{\"success\":false, \"message\":\"Send email error. emailSetting.size()<1\"}";
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			return "{\"success\":false, \"message\":\""+ ex.getMessage() +"\"}";
		}
	}
	
/**/
	//This function is for internal test only. 
	//Use this function to encrypt the pdf doc before calling the rest of the API functions in this class.	 
	
	@PostMapping("/pdf/encrypt")
	@ResponseBody
	Map<String, Object> encryptPdf(@RequestParam MultipartFile doc, String sha256Checksum ) {
		log.info(">encryptPdf"); 
		
		Map<String, Object> ret = new HashMap<String, Object>(); 		 	
		String requestId = UUIDUtil.getId();
		
		try {	  
			
			byte[] data = doc.getBytes(); 
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256Checksum, 1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 	
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
			}			
			SecretKey skey = AESKeyUtil.getSecretKeyFromBytes(Base64.decode(token.getToken())); 
			log.info("encryptPdf>Key = {}", Base64.toBase64String(skey.getEncoded())); 
			
			byte[] encryptedPdf = AESKeyUtil.encrypt(skey, AESKeyUtil.Initialization_Vector, data); 	
 
			// Save transaction record
			//saveRequestRecord(requestId, RestApiConstant.FN_ENCRYPT_PDF, RestApiConstant.MSG_SUCCESS_ENCRYPTPDF, RestApiConstant.STATUS_SUCCESS, new Date(), 0);
			
			ret.put("requestId", requestId);
			ret.put("encryptedPdf", encryptedPdf);
			ret.put("success", true);
			ret.put("message", "success"); 	
			ret.put("code", WSError.SUCCESS);
			return ret;	
		} catch(Exception ex) {
			ex.printStackTrace();
			// Save transaction record
			//saveRequestRecord(requestId, RestApiConstant.FN_ENCRYPT_PDF, RestApiConstant.MSG_FAILED_ENCRYPT_PDF, RestApiConstant.STATUS_FAILED, new Date(), 0);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("success", false);
			ret.put("message", ex.getMessage()); 	 
			return ret;	
		}
				
	}

	
	@PostMapping("/pdf/sign/get-pending-list")
	@ResponseBody
	Map<String, Object> getPendingList(HttpServletRequest httpRequest, @RequestBody String request) {
		
		log.info(">getPendingList");
		
		// TODO - extract parameter from JSON Object
		// TODO - search pending list DB
		// TODO - return pending list to caller 
		
		Map<String, Object> ret = new HashMap<String, Object>();
		
		final String requestId = UUIDUtil.getId();
        log.info(">getPendingList>requestId = " + requestId);
        
		try {
				
			
			if(request.isEmpty()) {
	    		ret.put("success", false);    		
				ret.put("message", message1005); 
				ret.put("code", WSError.REQUEST_EMPTY_ERROR);
	    		return ret;
			}
			
			JSONObject joRequest = WsUtils.parseRequestToJsonObject(request);
    		if(null == joRequest) {
    			ret.put("success", false);    		
    			ret.put("message", message1005); 
    			ret.put("code", WSError.REQUEST_PARSING_ERROR);
        		return ret;
    		}
    		
    		String signerId = joRequest.getString("signerId");   
    		log.info(">getPendingList>signerId: "+ signerId);
    		if(null != signerId) {
    			if(signerId.isEmpty()) {
    				ret.put("success", false);    		
        			ret.put("message", "REQUEST_PARAM_ERROR"); 
        			ret.put("code", WSError.REQUEST_PARAM_ERROR);
            		return ret;
    			}
    		}    		
    		
			List<PdfDoc> pdfDocs = pdfDocRepository.findAllByStatusAndApprovalUserId(PdfUtil.NEW_FILE, signerId);
			log.info(">getPendingList>Total: "+ pdfDocs.size());
			if(pdfDocs.size() == 0) {
				ret.put("success", false);    		
    			ret.put("message", "RECORD_NOT_FOUND"); 
    			ret.put("code", WSError.RECORD_NOT_FOUND);
        		return ret;
			}
			
			List<Map<String, Object>> root = new ArrayList<Map<String, Object>>();
			Map<String, Object> child = null;
			for(PdfDoc doc: pdfDocs) {
				child = new HashMap<String, Object>();
				child.put("requestId", doc.getRequestId());
				child.put("originalFileName", doc.getOriginalFileName());
				child.put("fileName", doc.getFileName());
				child.put("moduleName", doc.getModuleName());
				child.put("approvalUserId", doc.getApprovalUserId());
				child.put("status", doc.getStatus());
				
				String downloadFilePath = WsUtils.getFileDownloadURLPath(httpRequest, doc.getOriginalFileName(),hostname.trim());
				child.put("filePath", downloadFilePath);
				
				root.add(child);
			}
						
			// Save transaction record
            saveRequestRecord(
					requestId.toString(), 
					RestApiConstant.FN_GET_PENDING_LIST, 
					RestApiConstant.MSG_SUCCESS_GET_PENDING_LIST, 
					RestApiConstant.STATUS_SUCCESS, 
					new Date(), 0);
            
			ret.put("success", true);    		
			ret.put("message", "success"); 
			ret.put("code", WSError.SUCCESS);
			ret.put("data", root); //pdfDocs);
			return ret;
			
		} catch(Exception ex) {
			ex.printStackTrace();
			
			log.error("getPendingList error. {}", ex.getMessage());
			
			if(null!=requestId) {
				// Save transaction record
				saveRequestRecord(
						requestId.toString(), 
						RestApiConstant.FN_GET_PENDING_LIST, 
						RestApiConstant.MSG_FAILED_GET_PENDING_LIST, 
						RestApiConstant.STATUS_FAILED, 
						new Date(), 0);
			}
			
			ret.put("success", false);    		
			ret.put("message", "" + ex.getMessage()); 
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			return ret;
		}
		
	}
	
	@PostMapping("/pdf/sign/get-signing-data")
	@ResponseBody
	Map<String, Object> getSigningData(@RequestBody String request) {
		
		log.info(">RestApiController>getSigningData");
		
		// TODO - extract base64 PDF arrays from JSON Object
		// TODO - save doc into network storage
		// TODO - calculate checksum one by one
		// TODO - save checksum?
		// TODO - return list of checksum to caller 
				
		Map<String, Object> ret = new HashMap<String, Object>();
		   
		String documentRequestId = null;
		//final String requestId = UUIDUtil.getId();
        //log.info(">getSigningData>requestId = " + requestId);
        
    	if(request.isEmpty()) {
    		ret.put("success", false);    		
			ret.put("message", message1005); 
			ret.put("code", WSError.REQUEST_EMPTY_ERROR);
    		return ret;
		}
    	
    	try {
    		
    		JSONObject joRequest = WsUtils.parseRequestToJsonObject(request);
    		if(null == joRequest) {
    			ret.put("success", false);    		
    			ret.put("message", message1005); 
    			ret.put("code", WSError.REQUEST_PARSING_ERROR);
        		return ret;
    		}
    		
    		documentRequestId = joRequest.getString("documentRequestId");  
    		log.info(">getSigningData>documentRequestId = " + documentRequestId);
    		if(null != documentRequestId) {
    			if(documentRequestId.isEmpty()) {
    				ret.put("success", false);    		
        			ret.put("message", "INVALID_REQUEST_ID"); 
        			ret.put("code", WSError.INVALID_REQUEST_ID);
            		return ret;
    			}
    		}
    		
    		String signerId = joRequest.getString("signerId");  
    		log.info(">getSigningData>signerId = " + signerId);
    		if(null != signerId) {
    			if(signerId.isEmpty()) {
    				ret.put("success", false);    		
        			ret.put("message", "REQUEST_PARAM_ERROR"); 
        			ret.put("code", WSError.REQUEST_PARAM_ERROR);
            		return ret;
    			}
    		}
    		
    		UserApproval approval = userApprovalRepository.findByUserIdAndActive(signerId, 1);
    		if(null == approval) {
    			ret.put("success", false);    		
    			ret.put("message", "RECORD_NOT_FOUND"); 
    			ret.put("code", WSError.RECORD_NOT_FOUND);
        		return ret;
    		}
    		
    		String signerCertBase64 = null; 
    		
    		//String hardToken = joRequest.getString("hardToken");
    		//boolean useHardToken = Boolean.parseBoolean(hardToken.trim());
    		
    		//if(useHardToken) 
    		{
    			signerCertBase64 = joRequest.getString("signerCertBase64");
    			if(null != signerCertBase64) {
        			if(signerCertBase64.isEmpty()) {
        				ret.put("success", false);    		
            			ret.put("message", "REQUEST_PARAM_ERROR"); 
            			ret.put("code", WSError.REQUEST_PARAM_ERROR);
                		return ret;
        			}
        		}
    		} 
    		//else {
    			// Query signer soft-token from the database
    			
    		//}
    		byte[] decodedSignerCert = Base64.decode(signerCertBase64); 
    		
    		    		
    		// TODO - query previously uploaded document file's path from database test
    		// TODO - read stored document file from the disk
    		//String pdfBase64 = joRequest.getString("pdfBase64");    		
    		//if(null != pdfBase64) {
    		//	if(pdfBase64.isEmpty()) {
    		//		ret.put("success", false);    		
        	//		ret.put("message", "REQUEST_PARAM_ERROR"); 
        	//		ret.put("code", WSError.REQUEST_PARAM_ERROR);
            //		return ret;
    		//	}
    		//}
    		
    		
    		PdfDoc pdfDoc = pdfDocRepository.searchByRequestIdAndStatus(documentRequestId, PdfUtil.NEW_FILE);
    		if(pdfDoc==null) { 
    			ret.put("success", false);    		
    			ret.put("message", "RECORD_NOT_FOUND"); 
    			ret.put("code", WSError.RECORD_NOT_FOUND);
        		return ret;
    		}
    		
    		
			String pdfFilePath = pdfDoc.getFilePath();
			File f = new File(pdfFilePath);
			if(!f.exists()) {
				log.error(">Error on reading Pdf file from disk, "+ pdfFilePath +" not found");
				ret.put("success", false);    		
    			ret.put("message", "Error on reading Pdf file from disk, "+ pdfFilePath +" not found"); 
    			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
        		return ret;
			}
			
			byte[] originalPdfData = FileUtil.readAsByteArray(f);
    		log.info(">getSigningData>originalPdfData:"+ originalPdfData.length);
    		
    		    		       		  		
    		PDDocument originalDoc = null;			
			try {
				originalDoc = PDDocument.load(originalPdfData);
				log.info(">getSigningData>Document loaded successfully.");
			} catch(Exception ex) {
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);    				
				return ret;
			}    	
			
			// Save file
			//String origFilePath = fileStoragePath + "/" + "uploaded.pdf";    			
			//saveFile(origFilePath, decodedOriginalPdfData);   
			
			    		
    		IrisPDFBox api = new IrisPDFBox(); 
    		
    		// TODO - compare signer certificate checksum with the one in database
    		// TODO - verify signer certificate chaining with the correct SubCA
    		
    		//byte[] signerCertData = PdfSigningHelper.readInputStreamtoBuffer(new FileInputStream("C:/Users/trlok/Desktop/Materials from MAMPU/SurayaToken32019.cer"));
    		    		 		
    		CertificateFile certFile = certFileRepository.findByUserApprovalAndActive(approval, 1);
    		if(null==certFile) {
    			log.error(">getSigningData>No record of token(active) for approval "+ approval.getUserId());
    			
    			ret.put("success", false);
				ret.put("message", message1002);
				ret.put("code", WSError.TOKEN_NOT_FOUND);    				
				return ret;
    		}
    		
    		// check if pelulus token is 'dongle' type
    		String tokenType = certFile.getFileType();
    		if(!tokenType.trim().toLowerCase().equals("dongle")) {
    			log.error(">getSigningData>Registered token file type is not 'dongle'");
    			
    			ret.put("success", false);
				ret.put("message", message1005);
				ret.put("code", WSError.TOKEN_TYPE_INVALID);    				
				return ret;
    		}
    		
    		if(certFile.getFileData() == null) 
    		{
        		//boolean enabledProxy = true;
        		if(enabledProxy) {
        			System.setProperty("http.proxyHost", proxyHost); //"172.16.252.31");
        	        System.setProperty("http.proxyPort", proxyPort); //"3128");
        	        log.info(">getSigningData>Use network proxy "+ proxyHost + ":" + proxyPort);
        	        
        	        //if(proxyRequiredAuthentication) {
        	        	//log.info(">getSigningData>Proxy authentication not supported!");
        	        	//System.setProperty("http.proxyHost", proxyHost); 
            	        //System.setProperty("http.proxyPort", proxyPort); 
        	        //}
        		}
        		
        		X509Certificate signerCert = (X509Certificate) PdfSigningHelper.readDataAsCertificate(decodedSignerCert); //(signerCertData);
        		log.info(">getSigningData>signerCert serialnumber= " + signerCert.getSerialNumber().toString(16));
        		
        		log.info(">getSigningData>verifyCertificateCRLs...");
        		CRLVerifier.verifyCertificateCRLs(signerCert);    		
        		log.info(">getSigningData>verifyCertificateCRLs passed.");
        		
        		certificateFileService.storeCertificateDongle(
    					signerCert, signerCert.getSerialNumber().toString(16)+".cer", approval, certFile, signerId);
    			    
    			log.info(">getSigningData>Certificate is stored"); 
    			
    			
        		
    		}
			    		 		    					
			final String qrcodeSerialNum = UUIDUtil.getId();	
			log.info(">getSigningData>qrcodeSerialNum:"+ qrcodeSerialNum);
			
			// generate QR Code
			byte[] qrcodeData = QRCodeUtil.constructQRCode(qrcodeSerialNum);
			    					
            // process pdf document to generate raw bytes for signing on signing function
            byte[] mockPdfData = api.processDocument( attachQRCodeToPdf(originalDoc, qrcodeData) );	
                        
           		            
            PdfSigningData data = new PdfSigningData( 
            		documentRequestId, 
            		signerId,
            		decodedSignerCert,
            		mockPdfData,
            		api.dssSigningData.getContentBytes(), 
            		api.dssSigningData.getRawBytes(), 
            		api.dssSigningData.getDigestBytes(),
            		api.dssSigningData.getSigningTime().getTime(), 
            		api.dssSigningData.getSignature());
            
            
            signingDataRepository.save(data);
            
            if(pdfDoc!=null) { 
    			pdfDoc.setTokenId(certFile.getId());
    			pdfDoc.setQrcode(qrcodeData);
    			pdfDoc.setQrcodeSerialNum(qrcodeSerialNum);
      	        pdfDocRepository.save(pdfDoc);
    	     }
            
            log.info(">getSigningData>signingDataRepository.save");            
            
            
            {
            	// save QR Code serial number to DB
            	pdfDoc.setQrcodeSerialNum(qrcodeSerialNum);
        		pdfDocRepository.save(pdfDoc);
            }
            
            // Save transaction record
            saveRequestRecord(
            		documentRequestId, 
					RestApiConstant.FN_GET_SIGNING_DATA, 
					RestApiConstant.MSG_SUCCESS_GET_SIGNING_DATA, 
					RestApiConstant.STATUS_SUCCESS, 
					new Date(), 
					0);
                		           
            
            ret.put("requestId", data.getRequestId());
    		ret.put("signerId", data.getUserId());
    		ret.put("signingData", data.getRawBytes());
    		ret.put("success", true);    		
    		ret.put("message", "success"); 
    		ret.put("code", WSError.SUCCESS);
    		
    		return ret;
    		
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		
    		log.error("getSigningData error. {}", ex.getMessage());
			
    		if(null!=documentRequestId) {
				// Save transaction record
				saveRequestRecord(
						documentRequestId, 
						RestApiConstant.FN_GET_SIGNING_DATA, 
						RestApiConstant.MSG_FAILED_GET_SIGNING_DATA, 
						RestApiConstant.STATUS_FAILED, 
						new Date(), 0);
    		}
    		
    		ret.put("success", false);    		
    		ret.put("message", "" + ex.getMessage()); 
    		ret.put("code", WSError.INTERNAL_SERVER_ERROR);
    		 
    		return ret;
    		
    	}
		
		
	}
	
	@PostMapping("/pdf/sign/build-pdf")
	@ResponseBody
	Map<String, Object> buildSignedPdf(HttpServletRequest httpRequest, @RequestBody String request) {
		
		log.info(">buildSignedPdf");
		
		// TODO - extract base64 signature arrays from JSON Object
		// TODO - get doc from network storage
		// TODO - build pdf + signature one by one
		// TODO - save checksum?
		// TODO - return list of checksum to caller 
		
		Map<String, Object> ret = new HashMap<String, Object>();
		
		//final String requestId = UUIDUtil.getId();
        //log.info(">getPendingList>requestId = " + requestId);
		String documentRequestId = null;
		
		if(request.isEmpty()) {
			ret.put("success", false);    		
			ret.put("message", "REQUEST_PARAM_ERROR"); 
			ret.put("code", WSError.REQUEST_PARSING_ERROR);
			return ret;
		}
		
		PdfDoc pdfDoc = null;
		
		try {
			
			JSONObject joRequest = WsUtils.parseRequestToJsonObject(request);
			if(null == joRequest) {
				ret.put("success", false);    		
				ret.put("message", "REQUEST_PARAM_ERROR"); 
				ret.put("code", WSError.REQUEST_PARSING_ERROR);
				return ret;
			}
			
			documentRequestId = joRequest.getString("documentRequestId");
			log.info(">buildSignedPdf>documentRequestId = " + documentRequestId);
			
			if(null != documentRequestId) {
				if(documentRequestId.isEmpty()) {
					ret.put("success", false);    		
					ret.put("message", "INVALID_REQUEST_ID"); 
					ret.put("code", WSError.INVALID_REQUEST_ID);
					return ret;
				}
			}
			
			pdfDoc = pdfDocRepository.searchByRequestIdAndStatus(documentRequestId, PdfUtil.NEW_FILE);
    		if(pdfDoc==null) {
    			ret.put("success", false);    		
				ret.put("message", "INVALID_REQUEST_ID"); 
				ret.put("code", WSError.INVALID_REQUEST_ID);
				return ret;
    		}
			
			
			PdfSigningData pdfSigningData = signingDataRepository.searchByRequestIdOrderBySigningTime(documentRequestId);
			if(null == pdfSigningData) {
				ret.put("success", false);    		
				ret.put("message", "RECORD_NOT_FOUND"); 
				ret.put("code", WSError.RECORD_NOT_FOUND);
				return ret;
			}
			
			String  encodedSignature = joRequest.getString("signature");
			log.debug(">buildSignedPdf>signature = " + encodedSignature);
			if(null != encodedSignature) {
				if(encodedSignature.isEmpty()) {
					ret.put("success", false);    		
					ret.put("message", "REQUEST_PARAM_ERROR"); 
					ret.put("code", WSError.REQUEST_PARAM_ERROR);
					return ret;
				}
			}
			
			byte[] signatureFromRequest = null;
			try {
				signatureFromRequest = Base64.decode(encodedSignature);
			} catch(Exception ex) {				
				ret.put("success", false);    		
				ret.put("message", "" + ex.getMessage()); 
				ret.put("code", WSError.BASE64_CONTENT_DECODING_ERROR);
				return ret;
			}
					
            
    		DssSigningData dssSigningData = new DssSigningData(
    				pdfSigningData.getMockPdfBytes(),
    				pdfSigningData.getContentBytes(), 
    				pdfSigningData.getRawBytes(), 
    				pdfSigningData.getDigestBytes(), 
    				new Date(pdfSigningData.getSigningTime()), 
    				signatureFromRequest);
    		
    		
    		
    		
    		log.info(">buildSignedPdf>pdfSigningData -> signingTime: " + pdfSigningData.getSigningTime());
            
    		IrisPDFBox api = new IrisPDFBox(); 
            
            // TODO retrieve usercert from DB
            Certificate userCert = PdfSigningHelper.readDataAsCertificate( pdfSigningData.getUserCertBytes() );
            api.setSignerCertificate(userCert);    		
            api.setSignerCertificateChain(new Certificate[]{ userCert });
            
            byte[] buffers = dssSigningData.getMockPdfBytes() ;
            log.info(">buildSignedPdf>getMockPdfBytes: "+ buffers.length);
            
            List<Integer> byteRange = IrisPDFBox.getSignatureByteRange(buffers, IrisPDFBox.SIGNER_KEY_ALIAS); 
    		byte[] doc = PdfSigningHelper.replacePDFSignature(byteRange, api.buildCMSSignedData(dssSigningData), buffers);    	        
    		log.info(">buildSignedPdf>replacePDFSignature done.");	
						
    		String sha256Checksum = PdfUtil.getSHA256Checksum(doc);
    		log.info(">sha256Checksum=========================="+sha256Checksum);
    		Date d = new Date();
    		
    		String signedFileName = "signed_"+ d.getTime() +".pdf";
			// Save signed file to disk
    		String signedFilePath = fileStoragePath + "/" + signedFileName;       		    		
    		this.saveFileToDisk(signedFilePath, doc);
    		
    		// Update status + signedFilePath into pdf_document table
    		pdfDoc.setFileName(signedFileName);
    		
    		pdfDoc.setSignedFilePath(signedFilePath);
    		pdfDoc.setStatus(RestApiConstant.STATUS_SUCCESS);
    		pdfDoc.setSha256Checksum(sha256Checksum);
    		pdfDoc.setCreatedAt(d);
    		pdfDocRepository.save(pdfDoc);
    		
			// Save transaction record
            saveRequestRecord(
            		documentRequestId, 
					RestApiConstant.FN_SIGN_PDF, 
					RestApiConstant.MSG_SUCCESS_SIGN_PDF, 
					RestApiConstant.STATUS_SUCCESS, 
					new Date(), 
					pdfDoc.getId());
            
            String fileDownloadURL = WsUtils.getFileDownloadURLPath(httpRequest, signedFileName,hostname.trim());
            log.info(">File download URL:" + fileDownloadURL);
            
			ret.put("success", true);    		
			ret.put("message", "success"); 
			ret.put("code", WSError.SUCCESS);
			ret.put("filePath", fileDownloadURL); //signedFilePath);
			ret.put("fileName", signedFileName);
			return ret;
			
		}  catch(Exception ex) {
			ex.printStackTrace();
			
			log.error("buildSignedPdf error. {}", ex.getMessage());
			
			if(pdfDoc!=null) {
				pdfDoc.setStatus(RestApiConstant.STATUS_FAILED);
	    		pdfDoc.setCreatedAt(new Date());
	    		pdfDocRepository.save(pdfDoc);
			}
			
			if(null!=documentRequestId) {
				// Save transaction record
				saveRequestRecord(
						documentRequestId, 
						RestApiConstant.FN_SIGN_PDF, 
						RestApiConstant.MSG_FAILED_BUILD_SIGNED_PDF, 
						RestApiConstant.STATUS_FAILED, 
						new Date(), 0);
			}
			
			
			ret.put("success", false);    		
			ret.put("message", "" + ex.getMessage()); 
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			return ret;
		}
		
		
	}
	

	/**
	 * This function is for PDF uploading encrypted with token generated from DSS portal.
	 * 
	 * @param encPdfDoc
	 * @param sha256TokenChecksum
	 * @param docName
	 * @param iptsName
	 * @param moduleName
	 * @param signerId
	 * @param checkPaymentOnVerify
	 * @return
	 */
	@PostMapping("/pdf/upload")
	@ResponseBody
	Map<String, Object> uploadPdf(
			HttpServletRequest httpRequest,
			@RequestParam MultipartFile encPdfDoc, 
			@RequestParam String sha256TokenChecksum,
			@RequestParam(required=false) String docName1, 
			@RequestParam String iptsName, 
			@RequestParam String moduleName,
			@RequestParam String signerId, 
			@RequestParam(required=false) boolean checkPaymentOnVerify) { 
		
		log.info(">uploadPdf>signer id = {}", signerId);
		log.info(">uploadPdf>moduleName = {}", moduleName);
		log.info(">uploadPdf>checkPaymentOnVerify = {}", checkPaymentOnVerify);
		
		Map<String, Object> ret = new HashMap<String, Object>(); 
		 
		final String requestId = UUIDUtil.getId();
		
		try {		  			
						
			String approvalUserId = signerId.trim();
			
			byte[] bytesToBeDecrypt = encPdfDoc.getBytes();
			 		
			SecretKey skey = null;
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); 
			if(token == null) {
				log.error(">uploadPdf>Token not found for checksum -> "+ sha256TokenChecksum);
				
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
			}
			
			// Fetch signer's token from DB			 
			CertificateFile certFile = certFileRepository.querySignerCertificateFile(signerId);
			//check sama ada certificate dah expired or not
			UserApproval userApproval =userApprovalRepository.findByUserIdAndActive(signerId,1); // findByUserIdAndActive
			
			if(certFile==null) { //invalid certificate
				ret.put("success", false);
				ret.put("message", message1002); 
				ret.put("code", WSError.SIGNER_CERT_NOT_FOUND);
				return ret;
			}else {
				
				if(!certFile.getFileType().equals("dongle")) {
					ret.put("success", false);
					ret.put("message", message1009); 
					ret.put("code", WSError.SIGNER_ID_NOT_VALID);
					return ret;
				}
			}
			if(userApproval==null) { //invalid pelulus
				ret.put("success", false);
				ret.put("message", message1000); 
				ret.put("code", WSError.SIGNER_ID_NOT_FOUND);
				return ret;
			}			
						
		
			//check file pdf or not
			String isPDF = Files.getFileExtension(encPdfDoc.getOriginalFilename());
			log.info(">uploadPdf>isPDF = {}", isPDF);
			if(!isPDF.equals("pdf")) {
				log.error(">uploadPdf>File not have an valid .pdf extension.");
				
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);				
				return ret;
			}
			
			skey = AESKeyUtil.getSecretKeyFromBytes(Base64.decode(token.getToken())); 
			log.info(">uploadPdf>secret key = {}", Base64.toBase64String(skey.getEncoded()));
									
			byte[] decryptedPdf = null;
			String Sha256ChecksumB4Sign = "";
			List<PdfDoc> findAllByDocNameAndSha256ChecksumB4Sign = null;
			try {			
				// decrypt using current active token/key
				decryptedPdf = AESKeyUtil.decrypt(skey, AESKeyUtil.Initialization_Vector, bytesToBeDecrypt); 	
				
				Sha256ChecksumB4Sign = PdfUtil.getSHA256Checksum(decryptedPdf);
				log.error("checksumb4sign===="+Sha256ChecksumB4Sign);
				log.error("docName===="+encPdfDoc.getOriginalFilename());
				 findAllByDocNameAndSha256ChecksumB4Sign = pdfDocRepository.findAllByDocNameAndSha256ChecksumB4Sign(encPdfDoc.getOriginalFilename(), Sha256ChecksumB4Sign);
				 if(findAllByDocNameAndSha256ChecksumB4Sign.size()>0) {
					 log.error("===ada data====");
					    ret.put("success", false);
						ret.put("message", message4004); 
						ret.put("code", WSError.PARAMETER_INVALID);
						return ret;
				 }
				
			} catch(Exception ex) {
				ex.printStackTrace();
				
				log.error(">uploadPdf>AESKeyUtil.decrypt error. "+ ex.getMessage());
				
				ret.put("success", false);
				//ret.put("message", message3001);
				//ret.put("code", WSError.UNAUTHORIZED);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);	
				
				return ret;
			}
			
							 
			// Check whether this is a valid PDF document
			try(PDDocument doc = PDDocument.load(decryptedPdf)) {
				log.info(">uploadPdf>Uploaded document is a valid PDF file.");
			}catch(Exception ex) {
				log.error(">uploadPdf>Uploaded document is an invalid PDF file."+ ex.getMessage());
				
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);				
				return ret;
			}
			
			String fileName = "original_"+ new Date().getTime() +".pdf";
			String savedFile = fileStoragePath + "/"+ fileName;
                        
            // Save file to disk
            FileOutputStream fos = new FileOutputStream(savedFile);
            fos.write(decryptedPdf);
            fos.flush();
            fos.close();			
            
            log.info(">uploadPdf>File save in {}", savedFile);            
            
            // Store document signing information into DB
            PdfDoc newDoc = new PdfDoc();
            newDoc.setRequestId(requestId);
            newDoc.setApprovalUserId(approvalUserId);
            newDoc.setTokenId(-1); // -1 = use dongle + JAVA app to sign
            newDoc.setIptsName(iptsName);
            newDoc.setOriginalFileName(fileName);
            newDoc.setFileName(""); 
            newDoc.setModuleName(moduleName);
            //newDoc.setOriginalFilePath
            //newDoc.setSignedFilePath
            newDoc.setFilePath(savedFile); 
            newDoc.setQrcode(new byte[]{ 0x00 }); 
            newDoc.setQrcodeSerialNum(""); 
            newDoc.setSha256Checksum("");  
            newDoc.setSha256ChecksumB4Sign(Sha256ChecksumB4Sign);
            newDoc.setDocName(encPdfDoc.getOriginalFilename());
            newDoc.setCheckPayment((checkPaymentOnVerify) ? 1 : 0); // 1 = check payment required during doc verification
            newDoc.setPaid(0); // Not Paid = 0
            newDoc.setPreviewSettingId(PDF_PREVIEW_SETTING_DEFAULT_VALUE);
            newDoc.setStatus(PdfUtil.NEW_FILE); // FAILED = 0, SUCCESS = 1, NEW_FILE = 2
            newDoc.setCreatedAt(new Date());            
            pdfDocRepository.save(newDoc);
            
            log.info(">uploadPdf>Record saved.");
            
            // Save transaction record
            saveRequestRecord(
					requestId, 
					RestApiConstant.FN_UPLOAD_PDF, 
					RestApiConstant.MSG_SUCCESS_UPLOAD_PDF, 
					RestApiConstant.STATUS_SUCCESS, 
					new Date(),
					0);
                                   
            String fileDownloadURL = WsUtils.getFileDownloadURLPath(httpRequest, fileName,hostname.trim());
            log.info(">File download URL:" + fileDownloadURL);
                        
            // return JSON object
            ret.put("requestId", requestId);  
            ret.put("filePath", fileDownloadURL); //savedFile);
            ret.put("fileName", fileName);
            ret.put("success", true);
    		ret.put("code", WSError.SUCCESS);
    		ret.put("message", success);
    		
            return ret;
            
		} catch (Exception ex) {			 
			ex.printStackTrace();
			log.error(">uploadPdf>error. {}", ex.getMessage());
			
			if(null!=requestId) {
				// Save transaction record
				saveRequestRecord(
						requestId, 
						RestApiConstant.FN_UPLOAD_PDF, 
						RestApiConstant.MSG_FAILED_UPLOAD_PDF, 
						RestApiConstant.STATUS_FAILED, 
						new Date(), 0);
			}
			
			ret.put("success", false);
			ret.put("message", "Upload pdf error. " + ex.getMessage());
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
		}
		return ret;
	}
	
	/**
	 * This function is for PDF signing using approval P12 token or key + certificate.
	 * 
	 * @param encPdfDoc
	 * @param docName
	 * @param iptsName
	 * @param signerId
	 * @param checkPaymentOnVerify true if the payment status verification is required. 
	 * @return
	 */
	@PostMapping("/pdf/sign")
	@ResponseBody
	Map<String, Object> signPdf(
			HttpServletRequest request,
			@RequestParam MultipartFile encPdfDoc, 
			@RequestParam String sha256TokenChecksum,
			@RequestParam(required=false) String docName, 
			@RequestParam String iptsName, 
			@RequestParam String moduleName,
			@RequestParam String signerId, 
			@RequestParam(required=false) boolean checkPaymentOnVerify) { 
		
		// TODO - Add param for payment
		
		log.info("RestApiController, /pdf/sign");
		log.info("RestApiController, signer id = {}", signerId);
		log.info("RestApiController, moduleName = {}", moduleName);
		log.info("RestApiController, checkPaymentOnVerify = {}", checkPaymentOnVerify);
		
		Map<String, Object> ret = new HashMap<String, Object>(); 
		 
		final String requestId = UUIDUtil.getId();
		
		try {		  			
						
			String approvalUserId = signerId.trim();
			
			byte[] bytesToBeDecrypt = encPdfDoc.getBytes();
			 		
			SecretKey skey = null;
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
			}
		
			//check file pdf or not
			String isPDF = Files.getFileExtension(encPdfDoc.getOriginalFilename());
			log.info("RestApiController, isPDF = {}", isPDF);
			if(!isPDF.equals("pdf")) {
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);
				
				return ret;
			}
			
			skey = AESKeyUtil.getSecretKeyFromBytes(Base64.decode(token.getToken())); 
			log.info("signPdf Key = {}", Base64.toBase64String(skey.getEncoded()));
						
			PDDocument userDoc = null;
			List<PdfDoc>  findAllByDocNameAndSha256ChecksumB4Sign = null;
			byte[] plainPdf = null;
			String Sha256ChecksumB4Sign = "";
			try {			
				// decrypt using current active token/key
				plainPdf = AESKeyUtil.decrypt(skey, AESKeyUtil.Initialization_Vector, bytesToBeDecrypt); 
				
				Sha256ChecksumB4Sign = PdfUtil.getSHA256Checksum(plainPdf);
				log.error("checksumb4sign===="+Sha256ChecksumB4Sign);
				log.error("docName===="+encPdfDoc.getOriginalFilename());
				 findAllByDocNameAndSha256ChecksumB4Sign = pdfDocRepository.findAllByDocNameAndSha256ChecksumB4Sign(encPdfDoc.getOriginalFilename(), Sha256ChecksumB4Sign);
				 if(findAllByDocNameAndSha256ChecksumB4Sign.size()>0) {
					 log.error("ada data");
					    ret.put("success", false);
						ret.put("message", message4004); 
						ret.put("code", WSError.PARAMETER_INVALID);
						return ret;
				 }
				
				 
				
				
			} catch(Exception ex) {
				ex.printStackTrace();
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);	
				
				return ret;
			}
			
			byte[] originalPdfClone = null;
			
			try {		
				// Keep a copy of the original document 
				originalPdfClone = ArrayUtils.clone(plainPdf);
				
				// Convert bytes to pdf 
				userDoc = PDDocument.load(plainPdf);
	            
			} catch(Exception ex) {
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);
				
				return ret;
			}
			
			String originalFileName = "";
			String originalPdfFile = "";
			if(null!=originalPdfClone)
			{
				
				originalFileName = "original_"+ new Date().getTime() +".pdf";
	            originalPdfFile = fileStoragePath + "/"+ originalFileName;
	            
	            // Save file to disk for future reference purposes
	            FileOutputStream fos = new FileOutputStream(originalPdfFile);
	            fos.write(originalPdfClone);
	            fos.flush();
	            fos.close();
			}
			
			
			// Fetch signer's token from DB			 
			
			CertificateFile certFile = certFileRepository.querySignerCertificateFile(approvalUserId);
			//check sama ada certificate dah expired or not
			UserApproval userApproval =userApprovalRepository.findByUserIdAndActive(signerId, 1);
			if(certFile==null) { //invalid certificate
				ret.put("success", false);
				ret.put("message", message1002); 
				ret.put("code", WSError.SIGNER_CERT_NOT_FOUND);
				return ret;
			}
			if(userApproval==null) { //invalid pelulus
				ret.put("success", false);
				ret.put("message", message1000); 
				ret.put("code", WSError.SIGNER_ID_NOT_FOUND);
				return ret;
			}else {
				
				if(userApproval.getValidTo()!=null) {
					
					Date date = userApproval.getValidTo();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					
					java.util.Date dateToday= new Date();
					Calendar cal2 = Calendar.getInstance();
					
					Date dateValidTo = new Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
				    Date today = new Date(cal2.get(Calendar.YEAR),cal2.get(Calendar.MONTH),cal2.get(Calendar.DAY_OF_MONTH));
									
				    boolean isExpiredDate = dateValidTo.before(today);
					System.out.println("error" + isExpiredDate);
					if(isExpiredDate==true) {
						
						ret.put("success", false);
						ret.put("message", message1004); 
						ret.put("code", WSError.SIGNER_CERT_EXPIRED);
						return ret;
					}
					
				}else {
					//no cert
					ret.put("success", false);
					ret.put("message", message1002); 
					ret.put("code", WSError.SIGNER_CERT_NOT_FOUND);
					return ret;
				}
				
			}
			
			
			String p12TokenData = certFile.getFileData();
			String tokenPassword = certFile.getPasswordP12();
			
			//log.info("signPdf>p12TokenData {} ", p12TokenData);
			//log.info("signPdf>tokenPassword {} ", tokenPassword);
			
			final char[] pin = tokenPassword.trim().toCharArray();			
			//Resource rsKeyStoreFile = resourceLoader.getResource(signerKSFile); 				
			//Resource rsSignatureImage = resourceLoader.getResource(signerSignatureImage);
			
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(new ByteArrayInputStream(Base64.decode(p12TokenData)), pin); 
			X509Certificate signerCert = (X509Certificate) keystore.getCertificate(keystore.aliases().nextElement());
			//log.info("signPdf>signerCert Subject(RFC2253): {}, SerialNumber: {} ", signerCert.getSubjectX500Principal().getName(X500Principal.RFC2253), signerCert.getSerialNumber().toString(16)); 
			
			X500Name x500name = new JcaX509CertificateHolder(signerCert).getSubject();
			RDN cn = x500name.getRDNs(BCStyle.CN)[0];
			String commonName = IETFUtils.valueToString(cn.getFirst().getValue());
			
        	final String signerName = commonName.toUpperCase(); 
        	//String footerText = footer + " " + DateTimeUtil.getFormattedTimeStampForPdfFooter(new Date());
        	final String location = "Kementerian Pengajian Tinggi";//"Kementerian Pendidikan Malaysia"; 
        	final String purposeOfSigning = "Document Integrity";
        	
        	//log.info("signPdf>signerName:"+ signerName +",location:"+ location +",purposeOfSigning:"+ purposeOfSigning);
        			
        	CreateVisibleSignature signing = new CreateVisibleSignature(keystore, pin.clone());  
        	
        	final String qrcodeSerialNum = UUIDUtil.getId();	
			// generate QR Code
			byte[] qrcodeData = QRCodeUtil.constructQRCode(qrcodeSerialNum);
			// attached QR Code to pdf
			byte[] userDocWithQR = attachQRCodeToPdf(userDoc, qrcodeData);
			// sign pdf file
            byte[] signedPdf = signing.signDoc(userDocWithQR, signerName, location, purposeOfSigning);
           
            String checksum = PdfUtil.getSHA256Checksum(signedPdf);
            
            
            
            String signedFileName = "signed_"+ new Date().getTime() +".pdf";
            String signedPdfFile = fileStoragePath + "/"+ signedFileName;
            
            // Store document signing information into DB
            PdfDoc newDoc = new PdfDoc();
            newDoc.setRequestId(requestId);
            newDoc.setApprovalUserId(approvalUserId);
            newDoc.setTokenId(certFile.getId());
            newDoc.setIptsName(iptsName);
            newDoc.setOriginalFileName(originalFileName);
            newDoc.setFileName(signedFileName);
            newDoc.setModuleName(moduleName);
            newDoc.setFilePath(originalPdfFile);   
            newDoc.setSignedFilePath(signedPdfFile);
            newDoc.setQrcode(qrcodeData);
            newDoc.setQrcodeSerialNum(qrcodeSerialNum);
            newDoc.setSha256Checksum(checksum);  // Store checksum in DB as unique ID for this document         
            newDoc.setCheckPayment((checkPaymentOnVerify) ? 1 : 0); // 1 = check payment required during doc verification
            newDoc.setPaid(0); // Not Paid = 0
            newDoc.setPreviewSettingId(PDF_PREVIEW_SETTING_DEFAULT_VALUE);
            newDoc.setStatus(PdfUtil.SIGN_SUCCESS); // FAILED = 0, SUCCESS = 1
            newDoc.setCreatedAt(new Date()); 
            newDoc.setSha256ChecksumB4Sign(Sha256ChecksumB4Sign);
            newDoc.setDocName(encPdfDoc.getOriginalFilename());
            pdfDocRepository.save(newDoc);
            
            
            // Save file to disk
            FileOutputStream fos = new FileOutputStream(signedPdfFile);
            fos.write(signedPdf);
            fos.flush();
            fos.close();
            
            //log.info("signPdf>file save to {}", signedPdf);
            
            
            // Save transaction record
            saveRequestRecord(
					requestId, RestApiConstant.FN_SIGN_PDF, 
					RestApiConstant.MSG_SUCCESS_SIGN_PDF, RestApiConstant.STATUS_SUCCESS, 
					new Date(),newDoc.getId());
            
            
            // Encrypt file before returned
            //byte[] encSignedPdf = AESKeyUtil.encrypt(skey, AESKeyUtil.Initialization_Vector, signedPdf);
                
            String fileDownloadURL = WsUtils.getFileDownloadURLPath(request, signedFileName,hostname.trim());
            log.info(">File download URL:" + fileDownloadURL);
            
            // return JSON object
            ret.put("requestId", requestId);  
            ret.put("fileName", signedFileName);  
            ret.put("signedPdfFile", fileDownloadURL); //signedPdfFile);
            ret.put("success", true);
    		ret.put("code", WSError.SUCCESS);
    		ret.put("message", success);
    		
            return ret;
            
		} catch (Exception e) {			 
			e.printStackTrace();
			log.error("signPdf error. {}", e.getMessage());
			
			if(null!=requestId) {
				// Save transaction record
				saveRequestRecord(
						requestId, RestApiConstant.FN_SIGN_PDF, 
						RestApiConstant.MSG_FAILED_SIGN_PDF, RestApiConstant.STATUS_FAILED, 
						new Date(),0);
			}
			
			ret.put("success", false);
			ret.put("message", e.getMessage());
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
		}
		return ret;
	}
	
	/**
	 * This function is for PDF doc verification. 
	 * PDF is encrypted using the secret key generated from the DSS portal by admin.
	 * Payment status will be verified in this function.
	 * 
	 * @param encSignedPdfDoc
	 * @return
	 */
	@PostMapping("/pdf/verify")
	@ResponseBody
	Map<String, Object> verifyPdf(@RequestParam MultipartFile encSignedPdfDoc, @RequestParam String sha256TokenChecksum) {
		log.info("RestApiController, /pdf/verify");
		
		Map<String, Object> ret = new HashMap<String, Object>(); 
		final String requestId = UUIDUtil.getId();
		
		try {			
				 		
			SecretKey skey = null;
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
			}
						
			skey = AESKeyUtil.getSecretKeyFromBytes(Base64.decode(token.getToken())); 
			System.out.println("token" + token);
						 
			byte[] userPdfDecrypted = null;			
			try {	
				byte[] bytesToBeDecrypt = encSignedPdfDoc.getBytes();
				// decrypt using current active token/key
				userPdfDecrypted = AESKeyUtil.decrypt(skey, AESKeyUtil.Initialization_Vector, bytesToBeDecrypt); 			
			} catch(Exception ex) {
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);	
				
				return ret;
			}
									
			// Fetch signer's cert from DB by document hash
			X509Certificate signerCert = null;
			
			String userPdfChecksum = PdfUtil.getSHA256Checksum(userPdfDecrypted);
			log.info("verifyPdf>userPdfChecksum {} ", userPdfChecksum);
			
			PdfDoc pdfDoc = pdfDocRepository.findBySha256Checksum(userPdfChecksum);
			if(null == pdfDoc) {
				ret.put("success", false);
				ret.put("code", WSError.INVALID_DOCUMENT);
				ret.put("message", message2000);
				
				return ret;
			}
			
			// retrieve payment checking indicator from DB			 
			boolean checkPaymentStatus = (pdfDoc.getCheckPayment() == 1) ? true : false;
			 if(checkPaymentStatus) {
				 if(pdfDoc.getPaid() != 1) {
					ret.put("success", false);
					ret.put("code", WSError.PENDING_PAYMENT);
					ret.put("message", message1003);
					return ret;
				 }
			 }
			
			String approvalUserId = pdfDoc.getApprovalUserId();
			CertificateFile certFile = certFileRepository.querySignerCertificateFile(approvalUserId);
			String p12TokenData = certFile.getFileData();
			String fileData = null; 
			String fileType = null; 
			String password = null;
			if(certFile==null) { //invalid certificate
				ret.put("success", false);
				ret.put("message", message1002); 
				ret.put("code", WSError.SIGNER_CERT_NOT_FOUND);
				return ret;
			 }else {
				 fileData = certFile.getFileData(); 
				 fileType = certFile.getFileType();
				 if(fileData!=null || !fileData.isEmpty() ) {
					if(fileType.trim().equalsIgnoreCase("p12")) 
					{
						log.info("verifyPdf>dlm p12 {} ");
						String tokenPassword = certFile.getPasswordP12();
						log.info("verifyPdf>p12TokenData {} ", p12TokenData);
						log.info("verifyPdf>tokenPassword {} ", tokenPassword);
						final char[] pin = tokenPassword.trim().toCharArray();
						//Resource rsKeyStoreFile = resourceLoader.getResource(signerKSFile); 				
						//Resource rsSignatureImage = resourceLoader.getResource(signerSignatureImage);
						KeyStore keystore = KeyStore.getInstance("PKCS12");
						keystore.load(new ByteArrayInputStream(Base64.decode(p12TokenData)), pin); 
						signerCert = (X509Certificate) keystore.getCertificate(keystore.aliases().nextElement());
						log.info("verifyPdf>signerCert Subject(RFC2253): {}, SerialNumber: {} ", signerCert.getSubjectX500Principal().getName(X500Principal.RFC2253), signerCert.getSerialNumber().toString(16)); 
										
						try {
						// verify document signature
							PdfUtil.checkSignature(userPdfDecrypted, signerCert);
						} catch(Exception e1) {
							e1.printStackTrace();
							log.info("verifyPdf>Signature verification failed. {}", e1.getMessage());
											
							// Save transaction record
							saveRequestRecord(
								requestId, RestApiConstant.FN_VERIFY_PDF, 
								RestApiConstant.MSG_FAILED_VERIFY_PDF, RestApiConstant.STATUS_FAILED,new Date(),0);
											
							ret.put("success", false);
							ret.put("code", WSError.INVALID_SIGNATURE);
							ret.put("message", message2003);
											
							return ret;
				         }
									
						// Save transaction record
						saveRequestRecord(
								requestId, RestApiConstant.FN_VERIFY_PDF, 
								RestApiConstant.MSG_SUCCESS_VERIFY_PDF, RestApiConstant.STATUS_SUCCESS,new Date(),pdfDoc.getId());
								ret.put("requestId", requestId);
								ret.put("success", true);
								ret.put("code", WSError.SUCCESS);
								ret.put("message", success);
								  
					} //end p12 
					else if(fileType.trim().equalsIgnoreCase("dongle")) {
						log.info("verifyPdf>dlm dongle {} ");
						byte[] certBinary = Base64.decode(fileData);  
						X509Certificate x509cert = (X509Certificate) PdfSigningHelper.readDataAsCertificate(certBinary);
						try {
							// verify document signature
								PdfUtil.checkSignature(userPdfDecrypted, x509cert);
							} catch(Exception e1) {
								e1.printStackTrace();
								log.info("verifyPdf>Signature verification failed. {}", e1.getMessage());
												
								// Save transaction record
								saveRequestRecord(
									requestId, RestApiConstant.FN_VERIFY_PDF, 
									RestApiConstant.MSG_FAILED_VERIFY_PDF, RestApiConstant.STATUS_FAILED,new Date(),0);
												
								ret.put("success", false);
								ret.put("code", WSError.INVALID_SIGNATURE);
								ret.put("message", message2003);
												
								return ret;
					         }
										
							// Save transaction record
							saveRequestRecord(
									requestId, RestApiConstant.FN_VERIFY_PDF, 
									RestApiConstant.MSG_SUCCESS_VERIFY_PDF, RestApiConstant.STATUS_SUCCESS,new Date(),pdfDoc.getId());
									ret.put("requestId", requestId);
									ret.put("success", true);
									ret.put("code", WSError.SUCCESS);
									ret.put("message", success);		  
					} //end dongle
						  
				}
				
			  }//end else
			  
		} catch(Exception e) {
			e.printStackTrace();
			log.error("verifyPdf error. {}", e.getMessage());
			
			if(null!=requestId) {
				// Save transaction record
				saveRequestRecord(
						requestId, RestApiConstant.FN_VERIFY_PDF, 
						RestApiConstant.MSG_FAILED_VERIFY_PDF, RestApiConstant.STATUS_FAILED, 
						new Date(),0);
			}
			
			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}
		
		return ret;
		
	}
	
	/**
	 * This function is for payment status update. 
	 * This function should be called after the PDF signing is done as it required the admin to update the status in the DSS portal.
	 * Payment status check in the PDF verification function and QR code verification function.
	 * 
	 * @param encSignedPdfDoc
	 * @param paid
	 * @return
	 */
	@PostMapping("/payment/update")
	@ResponseBody
	Map<String, Object> updatePaymentStatus(@RequestParam MultipartFile encSignedPdfDoc, @RequestParam String sha256TokenChecksum, @RequestParam boolean paid) {
		log.info("RestApiController, /payment/update");
		
		Map<String, Object> ret = new HashMap<String, Object>();
		final String requestId = UUIDUtil.getId();
		
		try {			
						
			SecretKey skey = null;
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
				/*
				 * ret.put("success", false); ret.put("message", message1001); ret.put("code",
				 * WSError.SIGNER_KEY_NOT_FOUND); return ret;
				 */
			}
						
			skey = AESKeyUtil.getSecretKeyFromBytes(Base64.decode(token.getToken())); 
			log.info("updatePaymentStatus Key = {}", Base64.toBase64String(skey.getEncoded()));
						 
			byte[] userPdfDecrypted = null;			
			try {	
				byte[] bytesToBeDecrypt = encSignedPdfDoc.getBytes();
				// decrypt using current active token/key
				userPdfDecrypted = AESKeyUtil.decrypt(skey, AESKeyUtil.Initialization_Vector, bytesToBeDecrypt); 			
			} catch(Exception ex) {
				ret.put("success", false);
				ret.put("message", message2000);
				ret.put("code", WSError.INVALID_DOCUMENT);	
				
				return ret;
			}
			
			String userPdfChecksum = PdfUtil.getSHA256Checksum(userPdfDecrypted);
			// Lookup the document in the DB using the checksum
			PdfDoc pdfDoc = pdfDocRepository.findBySha256Checksum(userPdfChecksum);
			if(null == pdfDoc) {
				ret.put("success", false);
				ret.put("code", WSError.INVALID_DOCUMENT);
				ret.put("message", message2000);
				
				return ret;
			}
			
		 
			pdfDoc.setPaid( (paid) ? 1 : 0 ); // paid = 1
			
			pdfDocRepository.save(pdfDoc);
			
			
			// Save transaction record
			saveRequestRecord(
					requestId, RestApiConstant.FN_UPDATE_PAYMENT_STATUS, 
					RestApiConstant.MSG_SUCCESS_UPDATE_PAYMENT_STATUS, RestApiConstant.STATUS_SUCCESS, 
					new Date(),pdfDoc.getId());
						
			
			ret.put("requestId", requestId);
			ret.put("success", true);
			ret.put("code", WSError.SUCCESS);
			ret.put("message", success);
		} catch(Exception e) {
			e.printStackTrace();
			log.error("updatePaymentStatus error. {}", e.getMessage());
			
			if(null!=requestId) {
				// Save transaction record
				saveRequestRecord(
						requestId, RestApiConstant.FN_UPDATE_PAYMENT_STATUS, 
						RestApiConstant.MSG_FAILED_UPDATE_PAYMENT_STATUS, RestApiConstant.STATUS_FAILED, 
						new Date(),0);
			}
			
			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}
		
		return ret;
	}
	
	/**
	 * This function is for QR code verification.
	 * The content of the QR code is a unique serial number.
	 * Serial number will be verified in this function.
	 * Payment status will be verified in this function.
	 * 
	 * @param docSerialNumber
	 * @return
	 */
//	@GetMapping("/QR/verify/{docSerialNumber}")
//	@ResponseBody
//	Map<String, Object> verifyQR(@PathVariable final String docSerialNumber) { //@RequestParam String serialNumber) {
//		log.info("RestApiController, /QR/verify/{docSerialNumber}, docSerialNumber: {}", docSerialNumber);
//
//		Map<String, Object> ret = new HashMap<String, Object>();
//		final String requestId = UUIDUtil.getId();
//		
//		try {
//
//			// Load pdf from DB	by QRCode Serial Number 
//			PdfDoc pdfDoc = pdfDocRepository.findByQrcodeSerialNum(docSerialNumber);
//			if(null == pdfDoc) {
//				ret.put("success", false);
//				ret.put("code", WSError.INVALID_DOCUMENT);
//				ret.put("message", m2000 );//sementara lok balik drp japan message2000
//			
//
//				return ret;
//			}
//
//
//			// check payment status
//			// retrieve payment checking indicator from DB			 
//			boolean checkPaymentStatus = (pdfDoc.getCheckPayment() == 1) ? true : false;
//			if(checkPaymentStatus) {
//				if(pdfDoc.getPaid() != 1) {
//					ret.put("success", false);
//					ret.put("code", WSError.PENDING_PAYMENT);
//					//ret.put("message", message1003);// sementara
//					ret.put("message", m1003);
//					return ret;
//				}
//			}
//			
//			// Save transaction record
//			saveRequestRecord(
//					requestId, RestApiConstant.FN_VERIFY_QR, 
//					RestApiConstant.MSG_SUCCESS_VERIFY_QR, RestApiConstant.STATUS_SUCCESS, 
//					new Date(),pdfDoc.getId());
//
//			ret.put("requestId", requestId);
//			ret.put("success", true);
//			ret.put("code", 0);
//			ret.put("message", success);
//
//			return ret;
//		} catch (Exception e) {			 
//			e.printStackTrace();
//			log.error("verifyQR error. {}", e.getMessage());
//
//			if(null!=requestId) {
//				// Save transaction record
//				saveRequestRecord(
//						requestId, RestApiConstant.FN_VERIFY_QR, 
//						RestApiConstant.MSG_FAILED_VERIFY_QR, RestApiConstant.STATUS_FAILED, 
//						new Date(),0);
//			}
//			
//			ret.put("success", false);
//			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
//			ret.put("message", e.getMessage());
//		}
//
//		return ret;
//	}
	
	/**
	 * This function is for generating the PDF doc for viewing in the mobile APP.
	 * QR code content will be verified in this function.
	 * Payment status will be verified in this function.
	 * 
	 * @param docSerialNumber
	 * @return
	 */	
//	@GetMapping("/pdf/preview/{docSerialNumber}")
//	@ResponseBody
//	Map<String, Object> previewPdf(@PathVariable String docSerialNumber) {
//		log.info("RestApiController, /pdf/preview/{docSerialNumber}, docSerialNumber: {}", docSerialNumber);
//		
//		Map<String, Object> ret = new HashMap<String, Object>();
//		final String requestId = UUIDUtil.getId();
//		
//		try {			
//			
//			// Load pdf from DB	by QRCode Serial Number 
//			PdfDoc pdfDoc = pdfDocRepository.findByQrcodeSerialNum(docSerialNumber);
//			if(null == pdfDoc) {
//				ret.put("success", false);
//				ret.put("code", WSError.INVALID_DOCUMENT);
//				//ret.put("message", message2000);
//				ret.put("message", m2000);
//				
//				return ret;
//			}
//			
//			
//			// check payment status
//			// retrieve payment checking indicator from DB			 
//			boolean checkPaymentStatus = (pdfDoc.getCheckPayment() == 1) ? true : false;
//			 if(checkPaymentStatus) {
//				 if(pdfDoc.getPaid() != 1) {
//					ret.put("success", false);
//					ret.put("code", WSError.PENDING_PAYMENT);
//					ret.put("message", m1003);
//					return ret;
//				 }
//			 }
//			
//			String signedFileName = pdfDoc.getFileName(); //"signed_"+ new Date().getTime() +".pdf";
//	        String signedPdfFile = fileStoragePath + "/"+ signedFileName;
//			//String fileAtServer = pdfDoc.getSignedFilePath(); //.getFilePath();
//			// check file path whether is empty or null
//			if(null == signedPdfFile || signedPdfFile.isEmpty()) {
//				ret.put("success", false);
//				ret.put("code", WSError.FILE_NOT_FOUND);
//				ret.put("message", m4002);
//				
//				return ret;
//			}
//			
//			// check whether is a valid file
//			File docFile = new File(signedPdfFile);
//			if(!docFile.exists()) { // file not exist
//				ret.put("success", false);
//				ret.put("code", WSError.FILE_NOT_FOUND);
//				ret.put("message", m4002);
//				
//				return ret;
//			}					
//			    
//			int[] previewPages = new int[] { 1 }; // by default preview page 1
//			
//			byte[] previewDoc = null;
//			
//			log.info("previewPdf>Check doc size: {}", docFile.length());
//			
//			// if doc size > 3MB, generate preview pdf using preset page number
//			if(docFile != null && docFile.length() > FULL_PDF_PREVIEW_MAX_FILE_SIZE ) {
//				
//				PDDocument pdfAtServer = null;
//				int numberOfPages = 0;
//				try {
//					
//					// check whether is valid PDF Document
//					pdfAtServer = PDDocument.load(docFile);
//					numberOfPages = pdfAtServer.getNumberOfPages();
//					log.info("previewPdf>NumberOfPages: {}", numberOfPages); 				
//				} catch(Exception e) {
//					ret.put("success", false);
//					ret.put("code", WSError.INVALID_DOCUMENT_FOUND);
//					ret.put("message", m4003);					
//					return ret;
//				}
//									
//					
//				// Retrieve preview page number setting
//				if(PDF_PREVIEW_SETTING_DEFAULT_VALUE != pdfDoc.getPreviewSettingId()) {
//					PreviewPdfSetting setting = previewSettingRepository.findById(pdfDoc.getPreviewSettingId());
//					String pageNo = setting.getPageNo();
//					log.info("previewPdf>doc preview page: {}", pageNo);
//					// skip number if pageNo > numberOfPages
//					previewPages = PdfUtil.getPreviewPages(pageNo, numberOfPages);
//				} else {
//					// Page number 1 will be use for preview by default. Refer to variable 'previewPages'
//				}
//				  
//				
//				// generate preview copy of the pdf
//				previewDoc = PdfUtil.generatePreviewPdf(previewPages, pdfAtServer);
//				log.info("previewPdf>Generated preview doc size: {}", previewDoc.length);
//				
//					
//			} else {		
//				// Display whole Pdf if size < 3MB
//				previewDoc = PdfUtil.fileToByteArray(docFile); 
//			}
//			
//			
//			// Save transaction record
//			saveRequestRecord(
//					requestId, RestApiConstant.FN_PREVIEW_PDF, 
//					RestApiConstant.MSG_SUCCESS_PREVIEW_PDF_STATUS, RestApiConstant.STATUS_SUCCESS, 
//					new Date(),pdfDoc.getId());
//			
//			
//			ret.put("success", true);
//			ret.put("code", 0); //WSError.SUCCESS			
//			ret.put("message", success);
//			ret.put("previewPageNum", previewPages );
//			ret.put("pdf", previewDoc);
//			
//		} catch (Exception e) {			 
//			e.printStackTrace();
//			log.error("previewPdf error. {}", e.getMessage());
//			
//			if(null!=requestId) {
//				// Save transaction record
//				saveRequestRecord(
//						requestId, RestApiConstant.FN_PREVIEW_PDF, 
//						RestApiConstant.MSG_FAILED_PREVIEW_PDF_STATUS, RestApiConstant.STATUS_FAILED, 
//						new Date(),0);
//			}
//			
//			ret.put("success", false);
//			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
//			ret.put("message", e.getMessage());
//			
//		}
//		return ret;
//	}
		
	@GetMapping(value = "/pdf/{docSerialNumber}", produces = MediaType.APPLICATION_PDF_VALUE)
	public @ResponseBody byte[] getPdfFile(@PathVariable String docSerialNumber) throws Exception { 
		log.info("RestApiController, /pdf/{docSerialNumber}, docSerialNumber: {}", docSerialNumber);
		final String requestId = UUIDUtil.getId();
		
		try {		
			// Load pdf from DB	by QRCode Serial Number 
			PdfDoc pdfDoc = pdfDocRepository.findByQrcodeSerialNum(docSerialNumber);
			if(null == pdfDoc) {
				 throw new Exception("Document not found!");
			}						
			String pdfFile =  fileStoragePath + "/" +pdfDoc.getFileName(); //getFilePath();		
			
			// Save record
			saveRequestRecord(
					requestId, 
					AuditTrailConstant.FN_DOWNLOAD_PDF, 
					AuditTrailConstant.MSG_SUCCESS_DOWNLOAD_PDF + " " + AuditTrailConstant.MSG_SUCCESS,  
					AuditTrailConstant.STATUS_SUCCESS, 
					new Date(), 
					0);
						
			return PdfUtil.fileToByteArray(new File(pdfFile));					
		} catch (Exception e) {			 
			log.error(e.getMessage(), e); 
			throw new Exception("Fail to retrieve document. "+ e.getMessage()); 
		}
	}
	
	@GetMapping(value = "/pdfId/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
	public @ResponseBody byte[] getPdfFileId(@PathVariable String id) throws Exception { 
		log.info("RestApiController, /pdf/{id}, id: {}", id);
		final String requestId = UUIDUtil.getId();
		
		try {		
			// Load pdf from DB	by QRCode Serial Number 
			PdfDoc pdfDoc = pdfDocRepository.findById(Integer.parseInt(id));
			if(null == pdfDoc) {
				 throw new Exception("Document not found!");
			}			
			
			String pdfFile = "";
			if(pdfDoc.getFileName().isEmpty()) {
				pdfFile = fileStoragePath + "/" +pdfDoc.getOriginalFileName(); //getFilePath();
			}else {
				
				pdfFile = fileStoragePath + "/" +pdfDoc.getFileName(); //getFilePath();
			}
					
			//log.info("RestApiController, /pdf/{id}, id: {}", pdfFile);
			// Save record
			saveRequestRecord(
					requestId, 
					AuditTrailConstant.FN_DOWNLOAD_PDF, 
					AuditTrailConstant.MSG_SUCCESS_DOWNLOAD_PDF + " " + AuditTrailConstant.MSG_SUCCESS,  
					AuditTrailConstant.STATUS_SUCCESS, 
					new Date(), 
					0);
						
			return PdfUtil.fileToByteArray(new File(pdfFile));					
		} catch (Exception e) {			 
			log.error(e.getMessage(), e); 
			throw new Exception("Fail to retrieve document. "+ e.getMessage()); 
		}
	}
	
	/**
	 * Common function to save transaction record into database
	 * 
	 * @param requestId
	 * @param module
	 * @param desc
	 * @param status
	 * @param createAt
	 */
	private void saveRequestRecord(
			String requestId, String module, String desc, int status, Date createAt,int pdfDocId) {
		
		// Save request report to database
		RestApiRequestRecord requestRecord = new RestApiRequestRecord();
		requestRecord.setRequestId(requestId);
		requestRecord.setModule(module);
		requestRecord.setDescription(desc);
		requestRecord.setStatus(status);
		requestRecord.setCreatedAt(createAt);
		requestRecord.setPdfDocId(pdfDocId);
		restApiTranRepository.save(requestRecord);
		
		log.info(">saveRequestRecord>AuditTrail created for requestId -> "+ requestId);
	}
	
	
	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
		//ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), "error occurred");
		//return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
		
		ApiError apiError = new ApiError(WSError.INTERNAL_SERVER_ERROR, false, ex.getLocalizedMessage());
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	private byte[] attachQRCodeToPdf(PDDocument userDoc, byte[] qrcodeData) throws Exception {
		try {
			if(null == userDoc) {
				log.error(">attachQRCodeToPdf>Invalid PDF format.");
				throw new Exception("Invalid PDF format.");
			}

			int pages = userDoc.getNumberOfPages();
			log.info(">attachQRCodeToPdf>pages {}", pages);
			
			if(pages > 0) 
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				InputStream is = PDDocument.class.getResourceAsStream(
						"/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf");
				
				PDFont font = PDType0Font.load(userDoc, is, true);

				String footerText = footer + " " + DateTimeUtil.getFormattedTimeStampForPdfFooter(new Date());

				

				for(int i=0; i < pages; i++) 
				{
					PDPage page = userDoc.getPage(i);
					// Get the width we have to justify in.
					PDRectangle pageSize = page.getMediaBox();

					PDPageContentStream pdfContent = new PDPageContentStream(userDoc, page, true, true);		
					////qrcodeData = QRCodeUtil.constructQRCode(qrcodeSerialNum);
					pdfContent.drawImage(PDImageXObject.createFromByteArray(userDoc, qrcodeData, null), qrcodePosX, qrcodePosY, qrcodeWidth, qrcodeHeight);
					
					// Get the non-justified string width in text space units.
					float stringWidth = font.getStringWidth(footerText) * FONT_SIZE;
					// Get the string height in text space units.
					float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * FONT_SIZE;
					log.debug("Footer text height: " + stringHeight / 1000.0f);
					log.debug("Footer text width	: " + stringWidth / 1000.0f);
					log.debug("Page height	: " + pageSize.getHeight());
					log.debug("Page width	: " + pageSize.getWidth());

					pdfContent.beginText();
					pdfContent.setFont(font, FONT_SIZE);		                
					pdfContent.setTextMatrix(
							Matrix.getTranslateInstance( (pageSize.getWidth() - (stringWidth / 1000.0f)) / 2, 15.0f) 
							);		                
					pdfContent.showText(footerText);
					pdfContent.endText();	
					pdfContent.close();
				}
				
				userDoc.save(baos);
				baos.flush();					

				return baos.toByteArray();
			}

		} catch(Exception ex) {
			log.error("Fail to attach QR code to PDF.");
			ex.printStackTrace();
			throw new Exception("Fail to attach QR code to PDF.");
		} finally {
			try {
				userDoc.close();
			} catch (IOException e) {
			}
		}

		return null;
	}
	
	private void saveFileToDisk(String filePath, byte[] data) throws Exception {
		log.info(">saveFileToDisk");   
		 // Save original file to disk
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(data);
        fos.flush();
        fos.close();                
        log.info(">saveFileToDisk>file save to {}", filePath);   
	}
	
	//for ipts 
	@PostMapping("/ipts/save")
	@ResponseBody
	Map<String, Object> saveIpts(@RequestParam String iptsCode, @RequestParam String sha256TokenChecksum, @RequestParam String iptsName,@RequestParam String jenisIpts) {
		log.info("RestApiController, /ipts/save");
		
		Map<String, Object> ret = new HashMap<String, Object>();
	    Ipts ipts = iptsRepository.findByIptsCode(iptsCode);
		
		try {			
						
			
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
				
			}
			
			if(ipts!=null){
				//update
				ipts.setUpdatedDate(new Date());
				ipts.setUpdatedBy("Admin Ipts");
				ipts.setIptsName(iptsName);
				ipts.setJenisIpts(jenisIpts);
				ipts.setStatus(1); //1 - active 0 - deactivate
				iptsRepository.save(ipts);
				
			}else{
				//insert
				ipts = new Ipts();
				ipts.setCreatedDate(new Date());
				ipts.setCreatedBy("Admin Ipts");
				ipts.setIptsName(iptsName);
				ipts.setIptsCode(iptsCode);
				ipts.setJenisIpts(jenisIpts);
				ipts.setStatus(1); //1 - active 0 - deactivate
				iptsRepository.save(ipts);
			}
				
			ret.put("success", true);
			ret.put("code", WSError.SUCCESS);
			ret.put("message", success);
			ret.put("ipts", ipts);
		} catch(Exception e) {
			e.printStackTrace();
			log.error("updatePaymentStatus error. {}", e.getMessage());
			
			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}
		
		return ret;
	}
	
	@GetMapping("/ipts/getIptsDetail")
	@ResponseBody
	Map<String, Object> getIptsDetail(@RequestParam String iptsCode, @RequestParam String sha256TokenChecksum) {
		log.info("RestApiController, /ipts/getIptsDetail");
		
		Map<String, Object> ret = new HashMap<String, Object>();
	    Ipts ipts = iptsRepository.findByIptsCode(iptsCode);
		
		try {			
						
			
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
				
			}
			
			if(ipts!=null){
				ret.put("success", true);
				ret.put("code", WSError.SUCCESS);
				ret.put("message", success);
				ret.put("ipts", ipts);
				
			}else{
				ret.put("success", false);
				ret.put("code","Tiada rekod ipts dijumpai");
				ret.put("message", "Tiada rekod ipts dijumpai");
				ret.put("ipts", null);
			}
				
			
			
		} catch(Exception e) {
			e.printStackTrace();
			log.error("updatePaymentStatus error. {}", e.getMessage());
			
			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}
		
		return ret;
	}
	
	@GetMapping("/ipts/delete")
	@ResponseBody
	Map<String, Object> deleteIpts(@RequestParam String iptsCode, @RequestParam String sha256TokenChecksum) {
		log.info("RestApiController, /ipts/delete");
		
		Map<String, Object> ret = new HashMap<String, Object>();
	    Ipts ipts = iptsRepository.findByIptsCode(iptsCode);
		
		try {			
						
			
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
				
			}
			
			if(ipts!=null){
				//update
				ipts.setUpdatedDate(new Date());
				ipts.setUpdatedBy("Admin Ipts");
				ipts.setStatus(0); //1 - active 0 - deactivate
				iptsRepository.save(ipts);
				
			}
				
			ret.put("success", true);
			ret.put("code", WSError.SUCCESS);
			ret.put("message", "Hapuskan Ipts berjaya");
			
		} catch(Exception e) {
			e.printStackTrace();
			log.error("updatePaymentStatus error. {}", e.getMessage());
			
			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}
		
		return ret;
	}
	
	@GetMapping("/ipts/getList")
	@ResponseBody
	Map<String, Object> getListIpts(@RequestParam String sha256TokenChecksum) {
		log.info("RestApiController, /ipts/getList");
		
		Map<String, Object> ret = new HashMap<String, Object>();
	    List <Ipts> getListIpts = new ArrayList<Ipts>();
		
		try {			
						
			
			// Fetch secret key from DB
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;
				
			}
			
			getListIpts = iptsRepository.findAll();
				
			ret.put("success", true);
			ret.put("code", WSError.SUCCESS);
			ret.put("message", success);
			ret.put("list", getListIpts);
			
			log.info("RestApiController, list=============="+ret);
			
		} catch(Exception e) {
			e.printStackTrace();
			log.error("updatePaymentStatus error. {}", e.getMessage());
			
			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}
		
		return ret;
	}
	
	//new request 29092020
	@PostMapping("/pdf")
	@ResponseBody
	Map<String, Object> getPdfStatusAndFile(HttpServletRequest requestHttp, @RequestParam String docName, @RequestParam String iptsName, @RequestParam String sha256TokenChecksum) {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {	
			Token token = tokenRepository.findBySha256ChecksumAndStatus(sha256TokenChecksum, 1); //.findByStatus(1);
			if(token == null) {
				ret.put("success", false);
				ret.put("message", message1005); 
				ret.put("code", WSError.TOKEN_INVALID);
				return ret;

			}

			PdfDoc pdfDoc = pdfDocRepository.searchPdfdoc(docName, iptsName);
			if(pdfDoc == null) {
				ret.put("success", false);
				ret.put("code", WSError.RECORD_NOT_FOUND);
				ret.put("message", msgNoRecordFound);
				ret.put("pdfUrl", "");
				return ret;
			}
			
			if(pdfDoc.getStatus() == PdfUtil.SIGN_SUCCESS) 
			{
				String uRLPath = requestHttp.getScheme() + "://"+hostname.trim()+requestHttp.getServletContext().getContextPath();
				
								
				log.info(">Signed File Name:" + pdfDoc.getFileName());
				String fileDownloadURL = uRLPath+"/api/1/nfs/file?filePath="+ URLEncoder.encode(pdfDoc.getFileName(), "UTF-8");
				log.info(">File download URL:" + fileDownloadURL);
				ret.put("success", true);
				ret.put("code", 1);
				ret.put("message", msgSigningSuccess);
				ret.put("pdfUrl", fileDownloadURL);
				return ret;
			}
			else
			{
				if(pdfDoc.getStatus() == PdfUtil.NEW_FILE) 
				{
					ret.put("success", false);
					ret.put("code", -100);
					ret.put("message", msgSigningPending);
					ret.put("pdfUrl", "");
					return ret;
				}
				else if(pdfDoc.getStatus() == PdfUtil.SIGN_FAILED) 
				{
					ret.put("success", false);
					ret.put("code", -200);
					ret.put("message", msgSigningFailed);
					ret.put("pdfUrl", "");
					return ret;
				}
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			log.error("getPdfStatusAndFile error. {}", e.getMessage());

			ret.put("success", false);
			ret.put("code", WSError.INTERNAL_SERVER_ERROR);
			ret.put("message", e.getMessage());
		}

		return ret;
	}
			

}
