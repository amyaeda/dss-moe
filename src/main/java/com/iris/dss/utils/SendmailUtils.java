package com.iris.dss.utils;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.iris.dss.model.SystemSetting;

public class SendmailUtils {

	private static final Logger log = LoggerFactory.getLogger(SendmailUtils.class);
			
	public static JavaMailSender getJavaMailSender(SystemSetting emailSetting) {
    	
		log.info(">getJavaMailSender. Host: {}, Port: {}", emailSetting.getEmailSMTP(), emailSetting.getEmailPort());
    			
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
      //  mailSender.setHost(emailSetting.getEmailSMTP()); 
        mailSender.setHost(""); 
        if(emailSetting.getEmailPort()!=null|| !emailSetting.getEmailPort().isEmpty()) {
        mailSender.setPort(Integer.parseInt(emailSetting.getEmailPort()));}
        
        String smtpAuth = "false";
        
        //if ssl true 
        String userName = emailSetting.getUserName();
        String password = emailSetting.getEmailPassword(); 
        int ssl = emailSetting.getSslCert();
        if(ssl==1 && password!=null && !password.trim().isEmpty()) {
        	
            mailSender.setUsername(userName);
        	mailSender.setPassword(password);
        	smtpAuth = "true";
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        if(smtpAuth == "true") {
        	props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.debug", "true");
        
        if(smtpAuth == "true") {
        	// creates a new session with an authenticator
            javax.mail.Authenticator auth = new javax.mail.Authenticator() {
                public javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(userName, password);
                }
            };
            javax.mail.Session session = javax.mail.Session.getInstance(props, auth);
            mailSender.setSession(session);
        }
        
        
        return mailSender;
    }

	public static void main(String[] args) {

		
	}

}
