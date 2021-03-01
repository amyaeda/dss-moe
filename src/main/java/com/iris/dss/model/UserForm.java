package com.iris.dss.model;

import java.util.HashSet;
import java.util.Set;

public class UserForm {

	private int id;	 
	
	private String usernameAdm;
	
	private String passwordAdm;
	
	private String firstName;
	
	private String roleId;
	
	private String email;
	
	private Set<Role> roles = new HashSet<Role>();

	public UserForm() {
	}

	public UserForm(UserForm user) {
		super();
		this.id = user.getId();
		this.firstName = user.getFirstName(); 
		//this.lastName = user.getLastName();
		this.usernameAdm = user.getUsernameAdm();
		this.passwordAdm = user.getPasswordAdm();
		this.roles = user.getRoles();
		this.roleId = user.getRoleId();
		this.email = user.getEmail();
		
	
	}
	
	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getUsernameAdm() {
		return usernameAdm;
	}

	public void setUsernameAdm(String usernameAdm) {
		this.usernameAdm = usernameAdm;
	}

	public String getPasswordAdm() {
		return passwordAdm;
	}

	public void setPasswordAdm(String passwordAdm) {
		this.passwordAdm = passwordAdm;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	

}