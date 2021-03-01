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
@Table(name = "ipts")
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ipts {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="idipts")
	private int id;
	
	@Column(name="iptsCode")
	private String iptsCode;
	
	@Column(name="iptsName")
	private String iptsName;
	
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
	
	@Column(name="jenisIpts")
	private String jenisIpts;
	
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIptsCode() {
		return iptsCode;
	}
	public void setIptsCode(String iptsCode) {
		this.iptsCode = iptsCode;
	}
	public String getIptsName() {
		return iptsName;
	}
	public void setIptsName(String iptsName) {
		this.iptsName = iptsName;
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
	public String getJenisIpts() {
		return jenisIpts;
	}
	public void setJenisIpts(String jenisIpts) {
		this.jenisIpts = jenisIpts;
	}
	
	
}
