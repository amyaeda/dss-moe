package com.iris.dss.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iris.dss.model.AuditTrail;
import com.iris.dss.model.CertificateFile;
import com.iris.dss.model.UserApproval;
import com.iris.dss.repo.AuditTrailRepository;
import com.iris.dss.repo.CertificateFileRepository;
import com.iris.dss.utils.AuditTrailConstant;

@Service("certificateFileService")
public class CertificateFileService {

	@Autowired
	private AuditTrailRepository auditTrailRepository;

	@Autowired
	private CertificateFileRepository certificateFileRepository;

	public Map<String,String> validity(String password, byte[] p12Keystore) throws Exception {

		Provider BC = new BouncyCastleProvider();
		Security.addProvider(BC);
		Map<String,String> validityDate = new HashMap();

		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

		char[] pin = password.toCharArray();
		KeyStore keystore = KeyStore.getInstance("PKCS12", BC);		
		//File f = new File(p12Keystore.toString());
		
		try {

			if(p12Keystore!=null) { 
				
				keystore.load(new ByteArrayInputStream(p12Keystore), pin);
				Enumeration<String>aliases = keystore.aliases();
				
				if(aliases.hasMoreElements()) {
					
					String alias = aliases.nextElement();
					X509Certificate cert = (X509Certificate) 
							keystore.getCertificate(alias);
					Date validFrom = cert.getNotBefore();
					Date validTo = cert.getNotAfter();
					
					String stringValidTo = df.format(validTo);
					String stringValidFrom = df.format(validFrom);
					validityDate.put("validTo",stringValidTo);
					validityDate.put("validFrom",stringValidFrom);

				}
			}

		}catch (Exception e) {
          
			validityDate.put("invalid", e.toString());

		}
		
		return validityDate;

	}
	
	public byte[] p12KeystoreToByteArray(String password, byte[] p12Keystore) throws Exception {
		Provider BC = new BouncyCastleProvider();
		Security.addProvider(BC);

		char[] pin = password.toCharArray();
		KeyStore ks = KeyStore.getInstance("PKCS12", BC);		
		//File f = new File(p12Keystore.toString());
		if(p12Keystore!=null) { 
			ks.load(new ByteArrayInputStream(p12Keystore), pin);

		} else {			
			throw new Exception("Keystore not found.");
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		ks.store(baos, pin);	
		//baos.flush();
		return baos.toByteArray();
	}

	public KeyStore byteArrayToP12Keystore(String password, byte[] keystoreData, String keystoreOutputPath) throws Exception {
		Provider BC = new BouncyCastleProvider();
		Security.addProvider(BC);

		char[] pin = password.toCharArray();
		KeyStore ks = KeyStore.getInstance("PKCS12", BC);	 

		if(keystoreData!=null) { 
			ks.load(new ByteArrayInputStream(keystoreData), pin);
		} else {			
			throw new Exception("Keystore data not valid.");
		}

		ks.store(new FileOutputStream(keystoreOutputPath), pin);
		return ks;	  
	}


	public void storeFile(byte[] pathFile,String filename,UserApproval user, String password, String userloginName) throws Exception {

		CertificateFile dbFile = new CertificateFile();
		byte[] keystoreData = p12KeystoreToByteArray( password, pathFile);
		String fileP12inString = "";

		if(keystoreData!=null) {
			fileP12inString = Base64.toBase64String(keystoreData);
			dbFile.setFileName(filename);
			dbFile.setFileType("p12");
			dbFile.setUserApproval(user);
			dbFile.setPasswordP12(password);
			dbFile.setFileData(fileP12inString);
			dbFile.setActive(1);
			certificateFileRepository.save(dbFile);

			saveRequestRecord(
					userloginName, 
					AuditTrailConstant.FN_CREATE_CERTIFICATE, 
					AuditTrailConstant.FN_CREATE_CERTIFICATE+" "+AuditTrailConstant.MSG_SUCCESS, 
					AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
		}


	}
	
	public void storeCertificateDongle(
			X509Certificate cert, String filename, 
			UserApproval user, CertificateFile certFile, String userloginName) throws Exception {

		//CertificateFile dbFile = new CertificateFile();
		
		if(cert!=null) {
			String encodedCert = Base64.toBase64String(cert.getEncoded());
			certFile.setFileName(filename);			
			
			//set validTo and validFrom date for uploaded certificate current signer
			{
				String pattern = "yyyy/MM/dd";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);		
				user.setValidFrom(simpleDateFormat.parse(simpleDateFormat.format(cert.getNotBefore())));
				user.setValidTo(simpleDateFormat.parse(simpleDateFormat.format(cert.getNotAfter())));	
			}
			
			certFile.setUserApproval(user);			
			certFile.setFileData(encodedCert);
			certFile.setActive(1);
			//certFile.setPasswordP12("");
			//certFile.setFileType("dongle");
			
			certificateFileRepository.save(certFile);

			saveRequestRecord(
					userloginName, 
					AuditTrailConstant.FN_CREATE_CERTIFICATE, 
					AuditTrailConstant.FN_CREATE_CERTIFICATE +" "+ AuditTrailConstant.MSG_SUCCESS, 
					AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
		}


	}


	public List<CertificateFile> getFiles(UserApproval userPortal, String fileType) {

		List<CertificateFile> files =null;

		try {

			files = certificateFileRepository.findByUserApprovalAndFileType(userPortal, fileType);
			System.out.println(files);
			return files;

		}catch (Exception e)  {
			throw e;

		}


	}

	public List<CertificateFile> getFiles() {

		List<CertificateFile> files =null;

		try {

			files = certificateFileRepository.findAll();
			System.out.println(files.size());
			return files;

		}catch (Exception e)  {
			throw e;

		}
	}

	public List<CertificateFile> getFiles(int active) {

		List<CertificateFile> files =null;

		try {

			files = certificateFileRepository.findAllByActive(active);
			System.out.println(files.size());
			return files;

		}catch (Exception e)  {
			throw e;

		}
	}

	public CertificateFile getFile(int fileId) {

		CertificateFile file =null;

		try {

			file= certificateFileRepository.findById(fileId);

			return file;

		}catch (Exception e)  {
			throw e;

		}


	}
	
	
	public List<CertificateFile> getAllActiveFile() {

		List<CertificateFile> files =null;

		try {

			files = certificateFileRepository.queryActiveCertificateFile();
			//System.out.println(files.size());
			return files;

		}catch (Exception e)  {
			throw e;

		}
	}

	private void saveRequestRecord(
			String user, String module, String desc, int status, Date createAt, Date modifiedAt) {

		// Save request report to database
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUser(user);
		auditTrail.setModule(module);
		auditTrail.setDescription(desc);
		auditTrail.setStatus(status);
		auditTrail.setCreatedAt(createAt);
		auditTrail.setModifiedAt(modifiedAt);
		auditTrailRepository.save(auditTrail);
	}






}
