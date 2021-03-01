package com.iris.dss.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "certificatefile")
public class CertificateFile {

	  @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    @Column(name = "idcertificatefile")
	    private int id;

	    @Column(name = "fileName")
	    private String fileName;
	    
	    @Column(name = "fileType")
	    private String fileType;
	    
	    @Column(name = "fileData")
	    private String fileData;

	    @OneToOne(targetEntity = UserApproval.class, fetch = FetchType.EAGER)
	    @JoinColumn(nullable = false, name = "id")
	    private UserApproval userApproval;
	    
	    @Column(name = "passwordP12")
		private String passwordP12;
	    
	    @Column(name = "active")
		private int active;
	  

	    public CertificateFile() {
		}

	    public CertificateFile(CertificateFile file) {
	        super();

	        this.id = file.getId();
	        this.fileName = file.getFileName();
	        this.fileType = file.getFileType();
	        this.fileData = file.getFileData();
	        this.userApproval = file.getUserApproval();
	        this.passwordP12 = file.getPasswordP12();
	        this.active = file.getActive();
	       
	    }

	    public int getId() {
	        return id;
	    }
	   		
		public UserApproval getUserApproval() {
			return userApproval;
		}

		public void setUserApproval(UserApproval userApproval) {
			this.userApproval = userApproval;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileType() {
			return fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}

		public String getFileData() {
			return fileData;
		}

		public void setFileData(String fileData) {
			this.fileData = fileData;
		}

		public void setId(int id) {
			this.id = id;
		}
		
		public String getPasswordP12() {
			return passwordP12;
		}

		public void setPasswordP12(String passwordP12) {
			this.passwordP12 = passwordP12;
		}
		
		public int getActive() {
			return active;
		}

		public void setActive(int active) {
			this.active = active;
		}

		@Override
	    public String toString() {
	        final StringBuilder builder = new StringBuilder();
	       // builder.append("Token [String=").append(token).append("]").append("[Expires").append(expiryDate).append("]");
	        return builder.toString();
	    }
}
