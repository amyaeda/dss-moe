package com.iris.dss.schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;

import com.ibm.icu.util.Calendar;
import com.iris.dss.model.CertificateFile;
import com.iris.dss.model.SystemSetting;
import com.iris.dss.pdfbox.util.PdfSigningHelper;
import com.iris.dss.repo.SystemSettingRepository;
import com.iris.dss.service.CertificateFileService;
import com.iris.dss.service.UserApprovalService;
import com.iris.dss.utils.CertificateUtil;
import com.iris.dss.utils.SendmailUtils;

public class EmailTaskScheduler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//@Autowired
    //private JavaMailSender mailSender; 
	
	@Autowired
	private CertificateFileService certificateFileService;
	
	@Autowired
	private SystemSettingRepository systemSettingRepository;
	
	@Autowired
    ResourceLoader resourceLoader;
	
	@Autowired
	private UserApprovalService userApprovalService;
	
	@Value("${cron.email.notification.enabled}")
	private boolean enabledNotification;

	@Value("${notification.email.from}")
	String notificationEmailFromAddress;

	@Scheduled(cron="${cron.expression}")  
	public void execute()
	{	
		logger.info("EmailTaskScheduler executed. Mail notification is enabled = {}", enabledNotification);
		
		if(enabledNotification) 
		{
			logger.info("EmailTaskScheduler executed on "+ new Date());
			try 
			{
				//String from = notificationEmailFromAddress; 
				
				String from = ""; 
				String to = "";
				String to2 = null;
				String subject = "Notification from DSS Portal";
				String body = ""; 
				String toAddressList[] = new String[2];
				
				List<SystemSetting> systemSettingRepo = systemSettingRepository.findAll();
				SystemSetting setting = null;
				if(systemSettingRepo.size()>0) {
					setting = systemSettingRepo.get(0);
					from = setting.getEmailId(); //email daripada
					to = setting.getEmailAdmin(); //admin email
					logger.error("admin2"  +setting.getEmailAdmin2());
					if(setting.getEmailAdmin2()!=null|| setting.getEmailAdmin2()!="")
					{
						logger.error("array no ! null"  +setting.getEmailAdmin2());
						to2= setting.getEmailAdmin2();
						toAddressList[0] = to;
						toAddressList[1] = to2;
					}else {
						//toAddressList[0] = to;
						
					}
					
					    
				} else {
					logger.error("No settings are found for email. Please configure email via DSS portal.");
				}
								
				List<CertificateFile> list = certificateFileService.getAllActiveFile();
				//logger.info(">Found active token/certs: "+ list.size());
				
				String fileData = null;
				String fileType = null;
				String password = null;
				int userApprovalId = 0;
				//String emailApproval = null;
				for(CertificateFile certFile : list) 
				{
					fileData = certFile.getFileData();
					
					
					fileType = certFile.getFileType();					
					logger.info(">fileType: "+ fileType);
					logger.info(">list----: "+ list.size());
					logger.info(">certFile----: "+ certFile.getUserApproval().getName());
				if(fileData!=null || fileData!=null ) {
					if(fileType.trim().equalsIgnoreCase("p12")) 
					{
						// convert fileData to P12 token						
						password = certFile.getPasswordP12();
						KeyStore ks = CertificateUtil.byteArrayToP12Keystore(password, Base64.decode(fileData));						
						
						X509Certificate x509cert = getCertficate(ks);						
						
						int noficationInDays = setting.getNotification();
						logger.info(">Notification Setting (Hours): " + noficationInDays * 24);
																		
						Date future = getFutureCompareDate(noficationInDays);						
						logger.info(">Compare date: " + future);
						
						if(isExpired(x509cert, future)) 
						{
							logger.info(">certFile----: "+ certFile.getUserApproval().getName());
							//logger.info(">Cert is expired or going to expire.");
							userApprovalId = certFile.getUserApproval().getId();
							//logger.info(">userApprovalId"+userApprovalId);
							//emailApproval = userApprovalService.emailUserApproval(userApprovalId);
							//logger.info(">emailApproval ---"+emailApproval);
							// Send notification email
							logger.info(">Cert is expired or going to expire.");
							logger.info(">userApprovalId"+userApprovalId);
							logger.info(">userApprovalName"+userApprovalService.emailUserApproval(userApprovalId));
							String namePelulus = certFile.getUserApproval().getName();
							String idPelulus = certFile.getUserApproval().getUserId();
							String serialNum = x509cert.getSerialNumber().toString(16);
							String subjectDn = x509cert.getSubjectX500Principal().getName();
							Date validTo = x509cert.getNotAfter();
							
							body = getMessageBody(namePelulus,idPelulus,serialNum.toUpperCase(), subjectDn, validTo);
													
							
							//mailSender
							if(setting.getEmailAdmin2()!=null || setting.getEmailAdmin2()!="") {
								logger.error("array no! null"  );
								 SendmailUtils.getJavaMailSender(setting).send(constructEmail(from, toAddressList, subject, body));
						        }else {
						        	logger.error("array no 2 null"  );
						         SendmailUtils.getJavaMailSender(setting).send(constructEmail2(from, to, subject, body));
						        	
						        }
							
							//wait(11);
							//logger.info(">Email sent to " + to);
						}
					}
					else if(fileType.trim().equalsIgnoreCase("dongle")) 
					{
						logger.info(">certFile----: "+ certFile.getUserApproval().getName());
						byte[] certBinary = Base64.decode(fileData);
						
						X509Certificate x509cert = (X509Certificate) PdfSigningHelper.readDataAsCertificate(certBinary);
			    		logger.info(">Cert serialnumber=" + x509cert.getSerialNumber().toString(16).toUpperCase());
			    		
			    		Date future = getFutureCompareDate(setting.getNotification());						
						logger.info(">Compare date: " + future);
						
						if(isExpired(x509cert, future)) 
						{
							logger.info(">Cert is expired or going to expire.");
							// Send notification email
							userApprovalId = certFile.getUserApproval().getId();
							//logger.info(">Cert is expired or going to expire.");
							logger.info(">userApprovalId"+userApprovalId);
							logger.info(">userApprovalName"+userApprovalService.emailUserApproval(userApprovalId));
							//emailApproval = userApprovalService.emailUserApproval(userApprovalId);
							//logger.info(">Cert is expired or going to expire userApprovalId."+userApprovalId);
							//logger.info(">Cert is expired or going to expire. user Approval email ---"+emailApproval);
							String namePelulus = certFile.getUserApproval().getName();
							String idPelulus = certFile.getUserApproval().getUserId();
							String serialNum = x509cert.getSerialNumber().toString(16);
							String subjectDn = x509cert.getSubjectX500Principal().getName();
							Date validTo = x509cert.getNotAfter();
							
							body = getMessageBody(namePelulus,idPelulus,serialNum.toUpperCase(), subjectDn, validTo);
							
							//mailSender
							if(setting.getEmailAdmin2()!=null|| setting.getEmailAdmin2()!="") {
								logger.error("array no 2 !null"  );
								 SendmailUtils.getJavaMailSender(setting).send(constructEmail(from, toAddressList, subject, body));
						        }else {
						        	logger.error("array no 2 null"  );
						         SendmailUtils.getJavaMailSender(setting).send(constructEmail2(from, to, subject, body));
						        	
						        }
						}
					}
				}
					
				}				
				
			} 
			catch(Exception ex) 
			{
				ex.printStackTrace();
				logger.error("EmailTaskScheduler error. "+ ex.getMessage());
			}
		}		
		
	}
	
	private String getMessageBody(String appName, String appId, String serialNum, String subjectDn, Date validTo) {
		logger.info(">getMessageBody>serialNum: " + serialNum.toUpperCase());
		logger.info(">getMessageBody>subjectDn: " + subjectDn);
		logger.info(">getMessageBody>validTo: " + validTo);
		
		final String SERIAL_NUM = "<SERIAL_NUM>";
		final String SUBJECT_DN = "<SUBJECT_DN>";
		final String VALID_TO 	= "<VALID_TO>";
		final String APPROVAL_NAME = "<APPROVAL_NAME>";
		final String APPROVAL_ID 	= "<APPROVAL_ID>";
		
		String content = "";
		try {			
			logger.info(">getMessageBody>Loading mail template from classpath:mail/message_template.txt");
			Resource resource = resourceLoader.getResource("classpath:mail/message_template.txt");
			InputStream fileStream = resource.getInputStream();					
			content = readStringFromStream(fileStream); 
			content = content.replaceAll(APPROVAL_NAME, appName);
			content = content.replaceAll(APPROVAL_ID, appId);
			content = content.replaceAll(SERIAL_NUM, serialNum.toUpperCase());
			content = content.replaceAll(SUBJECT_DN, subjectDn);
			content = content.replaceAll(VALID_TO, validTo.toString());
			//logger.info(">getMessageBody>Mail Template: \n" + content);
		} catch(Exception ex) {
			ex.printStackTrace();
			logger.error(">getMessageBody>Reading mail template from resources failed. " + ex.getMessage());
		}
               
		return content;		
	}
	
	private String readStringFromStream(InputStream inputStream) throws IOException {
		StringBuffer sb = new StringBuffer();
		String lineSeparator = System.lineSeparator(); 
		try(Scanner sc = new Scanner(inputStream)) 
		{	
			while(sc.hasNext()) 
			{
				sb.append(sc.nextLine());
				sb.append(lineSeparator); // add line separator for each newline
			}
		}		
		String ret = sb.toString();
		logger.info(">readStringFromStream>Mail template: " + ret);
		return ret;		
	}      
	
	private Date getFutureCompareDate(int noficationInDays) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, noficationInDays * 24);		
		Date future = cal.getTime();								
		return future;
	}
	
	private X509Certificate getCertficate(KeyStore ks) throws Exception {
		Enumeration<String> enumerator = ks.aliases();
		X509Certificate x509cert = null;
		while(enumerator.hasMoreElements()) {
			String alias = enumerator.nextElement();
			logger.info(">alias: "+ alias);
			x509cert = (X509Certificate) ks.getCertificate(alias);							
			break;
		}
		
		return x509cert;
	}
	
	private boolean isExpired(X509Certificate x509cert, Date future) {
		try {
			
			return x509cert.getNotAfter().before(future);
		} catch(Exception ex) {
			logger.error(">isExpired>Certificate is going to expire on " + future);			
		}
		
		return false;
	}
	
	
	private SimpleMailMessage constructEmail(String from, String toAddressList[], String subject, String body) {
        System.out.println("email cc"+toAddressList.toString());
		
		
		final SimpleMailMessage email = new SimpleMailMessage();        
        email.setFrom(from);
        email.setTo(toAddressList);
        //email.setCc(cc);
        email.setSubject(subject);
        email.setText(body);            
        return email;
    }
	
	private SimpleMailMessage constructEmail2(String from, String to, String subject, String body) {
        //System.out.println("email cc"+cc);
		
		
		final SimpleMailMessage email = new SimpleMailMessage();        
        email.setFrom(from);
        email.setTo(to);
        //email.setCc(cc);
        email.setSubject(subject);
        email.setText(body);            
        return email;
    }
}


