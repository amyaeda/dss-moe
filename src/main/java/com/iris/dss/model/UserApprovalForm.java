package com.iris.dss.model;

import org.springframework.web.multipart.MultipartFile;

public class UserApprovalForm {

	private int id;	 
	
	private String userId;
	
	private String password;
	
	private String name;
	
	private String email;
	
	private String jenisFail;
	
	private MultipartFile  file;
	
	private String dateTo;
	
	

	public UserApprovalForm() {
	}

	public UserApprovalForm(UserApprovalForm user) {
		super();
		this.id = user.getId();
		this.userId = user.getUserId(); 
		this.name = user.getName();
		this.password = user.getPassword();
		this.jenisFail = user.getJenisFail();
		this.file = user.getFile();
		this.dateTo = user.getDateTo();
	
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getJenisFail() {
		return jenisFail;
	}

	public void setJenisFail(String jenisFail) {
		this.jenisFail = jenisFail;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	
	

}