package com.iris.dss.utils;
 

public class ApiError {
	private int code;
	private boolean success;
	private String message;
	
	public ApiError(int code, boolean success, String message) {
		super();
		this.code = code;
		this.success = success;
		this.message = message;
	}
//	public ApiError(HttpStatus status, String message, String error) {
//		super();
//		this.status = status;
//		this.message = message;
//		errors = Arrays.asList(error);
//	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}