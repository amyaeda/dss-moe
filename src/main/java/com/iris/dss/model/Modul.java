package com.iris.dss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "modul")
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Modul {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="idmodul")
	private int id;
	
	@Column(name="modulCode")
	private String modulCode;
	
	@Column(name="modulDesc")
	private String modulDesc;
	
	@Column(name="createdDate")
	private Date createdDate;
	 	
	@Column(name="createdBy")
	private String createdBy;
	
	@Column(name="updatedDate")
	private Date updatedDate;
	 	
	@Column(name="updatedBy")
	private String updatedBy;
	
	@Column(name = "status")
	private int status;
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getModulCode() {
		return modulCode;
	}
	public void setModulCode(String modulCode) {
		this.modulCode = modulCode;
	}
	public String getModulDesc() {
		return modulDesc;
	}
	public void setModulDesc(String modulDesc) {
		this.modulDesc = modulDesc;
	}
	
	
}
