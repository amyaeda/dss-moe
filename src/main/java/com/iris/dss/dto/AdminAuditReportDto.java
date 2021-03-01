package com.iris.dss.dto;

import java.util.Date;

public class AdminAuditReportDto {
	
	private int id;
	private String module;
	private int status;
	private Date createdAt;
	private String user;
	private String firstName;
	private String userName;
	
	public AdminAuditReportDto() {
	}
	
	public AdminAuditReportDto(AdminAuditReportDto dto)
	{
		super();
		System.out.println("dlm dto "+dto);
		this.id = dto.getId();
		this.module = dto.getModule();
		this.status = dto.getStatus();
		this.createdAt = dto.getCreatedAt();
		this.user = dto.getUser();
		this.firstName = dto.getFirstName();
		this.userName = dto.getUserName();
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getFirstName() { return firstName; } 
	
	public void setFirstName(String firstName) { 
		this.firstName = firstName; 
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	

}
