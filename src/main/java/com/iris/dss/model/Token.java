package com.iris.dss.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/*Token ini digunakan untuk pengenalan kpd sistem*/

@Entity
@Table(name = "token")
public class Token {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="token_id")
	private int id;
	
	@Column(name="token")
	private String token;
	
	@Column(name="status")
	private int status;
	
	@Column(name="description")
	private String description;
	
	@Column(name="sha256Checksum")
	private String sha256Checksum;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSha256Checksum() {
		return sha256Checksum;
	}
	public void setSha256Checksum(String sha256Checksum) {
		this.sha256Checksum = sha256Checksum;
	}
	
	
	
}
