package com.iris.dss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*Token ini digunakan untuk pengenalan kpd sistem*/

/*
id int(11) AI PK 
pageNo varchar(45) 
active int(1) 
createdAt varchar(45)
 */

@Entity
@Table(name = "pdf_preview_setting")
public class PreviewPdfSetting {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="pageNo")
	private String pageNo;
	
	@Column(name="active")
	private String active;
	
	
	@Column(name="createdAt")
	private Date createdAt;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	 
	public String getPageNo() {
		return pageNo;
	}
	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	
	
	
}
