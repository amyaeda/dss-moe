package com.iris.dss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

 

/*
id int(11) 
user varchar(45) 
module varchar(45) 
description varchar(100) 
status int(1) 
createdAt timestamp 
modifiedAt timestamp
 */

@Entity
@Table(name = "audit_trail")
public class AuditTrail {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="user")
	private String user;
	 
	@Column(name="module")
	private String module;

	@Column(name="description")
	private String description;
	
	@Column(name = "status")
	private int status;
	
	@Column(name="createdAt")
	private Date createdAt;
	
	@Column(name="modifiedAt")
	private Date modifiedAt;
		
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
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
	public Date getModifiedAt() {
		return modifiedAt;
	}
	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
	
	
}
