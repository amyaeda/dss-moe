package com.iris.dss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

 

/*
id int(11) AI PK 
requestId varchar(45) 
module varchar(45) 
description varchar(100) 
status int(1) 
createdAt timestamp
 */

@Entity
@Table(name = "rest_api_transaction")
public class RestApiRequestRecord {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="requestId")
	private String requestId;
	 
	@Column(name="module")
	private String module;

	@Column(name="description")
	private String description;
	
	@Column(name = "status")
	private int status;
	
	@Column(name="createdAt")
	private Date createdAt;
	
	@Column(name="pdfDocId")
	private int pdfDocId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public int getPdfDocId() {
		return pdfDocId;
	}
	public void setPdfDocId(int pdfDocId) {
		this.pdfDocId = pdfDocId;
	}
	
}
