package com.iris.dss.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "systemsetting")
public class SystemSetting {
	
	 	@Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    @Column(name = "idsetting")
	    private int id;

	    @Column(name = "emailSMTP")
	    private String emailSMTP;
	    
	    @Column(name = "emailPort")
	    private String emailPort;
	    
	    @Column(name = "emailId")
	    private String emailId;
	    
	    @Column(name = "emailPassword")
		private String emailPassword;
	    
	    @Column(name = "notification")
	    private int notification;
	    
	    @Column(name = "userName")
		private String userName;
	    
	
	    @Column(name = "sslCert") 
	    private int sslCert;
	    
	    @Column(name = "emailAdmin")
	  	private String emailAdmin;
	    
	    @Column(name = "emailAdmin2")
	  	private String emailAdmin2;
	 

	    public SystemSetting() {
		}

	    public SystemSetting(SystemSetting system) {
	        super();

	        this.id = system.getId();
	        this.emailSMTP = system.getEmailSMTP();
	        this.emailPort = system.getEmailPort();
	        this.emailId = system.getEmailId();
	        this.emailPassword = system.getEmailPassword();
	        this.notification = system.getNotification();
	        this.userName =  system.getUserName();
	        this.sslCert =  system.getSslCert();
	        this.emailAdmin =  system.getEmailAdmin();
	        this.emailAdmin2 =  system.getEmailAdmin2();
	        	       
	    }

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getEmailSMTP() {
			return emailSMTP;
		}

		public void setEmailSMTP(String emailSMTP) {
			this.emailSMTP = emailSMTP;
		}

		public String getEmailPort() {
			return emailPort;
		}

		public void setEmailPort(String emailPort) {
			this.emailPort = emailPort;
		}

		public String getEmailId() {
			return emailId;
		}

		public void setEmailId(String emailId) {
			this.emailId = emailId;
		}

		public String getEmailPassword() {
			return emailPassword;
		}

		public void setEmailPassword(String emailPassword) {
			this.emailPassword = emailPassword;
		}

		public int getNotification() {
			return notification;
		}

		public void setNotification(int notification) {
			this.notification = notification;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

	
	    public int getSslCert() { 
		  return sslCert; 
	    }
	  
	    public void setSslCert(int sslCert) { this.sslCert = sslCert; }

		public String getEmailAdmin() {
			return emailAdmin;
		}

		public void setEmailAdmin(String emailAdmin) {
			this.emailAdmin = emailAdmin;
		}

		public String getEmailAdmin2() {
			return emailAdmin2;
		}

		public void setEmailAdmin2(String emailAdmin2) {
			this.emailAdmin2 = emailAdmin2;
		}
	    
		
}


