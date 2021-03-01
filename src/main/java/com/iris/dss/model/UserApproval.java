package com.iris.dss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "userapproval")
public class UserApproval {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;	 
	
	@Column(name = "user_id")
	@NotEmpty(message = "*Please provide an valid User Id")
	private String userId;
	
	@Column(name = "name")
	@NotEmpty(message = "*Please provide your name")
	private String name;
		
	@Column(name = "active")
	private int active;
	
	@Column(name = "email")
	@Email(message = "*Please provide a valid Email")
	@NotEmpty(message = "*Please provide your email")
	private String email;
	
	@Column(name = "validTo")
	private Date validTo;
	
	@Column(name = "validFrom")
	private Date validFrom;
	
	/*
	 * @Column(name = "modifyBy") private String modifyBy;
	 * 
	 * @Column(name = "modifyDate") private Date modifyDate;
	 * 
	 * @Column(name = "deleteBy") private String deleteBy;
	 * 
	 * @Column(name = "deleteDate") private Date deleteDate;
	 */
	

	public UserApproval() {
	}

	public UserApproval(UserApproval user) {
		super();
		this.id = user.getId();
		this.name = user.getName(); 
		this.email = user.getEmail();
		this.userId = user.getUserId();
		this.validTo= user.getValidTo();
		this.validFrom = user.getValidFrom();
		this.active = user.getActive();
		
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}


}