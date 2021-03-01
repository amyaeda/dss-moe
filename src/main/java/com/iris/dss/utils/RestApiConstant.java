package com.iris.dss.utils;

public class RestApiConstant {

	// API STATUS
	public static final int STATUS_SUCCESS = 1;  
	public static final int STATUS_FAILED = 0;
	
	// Testing only
	public static final String FN_ENCRYPT_PDF = "ENCRYPT_PDF";
	public static final String MSG_SUCCESS_ENCRYPT_PDF = "PDF encrypt successful.";
	public static final String MSG_FAILED_ENCRYPT_PDF = "PDF encrypt failed.";
	
	// Production
	public static final String FN_VERIFY_PDF = "VERIFY_PDF";
	public static final String FN_SIGN_PDF = "SIGN_PDF";
	public static final String FN_VERIFY_QR = "VERIFY_QR";
	public static final String FN_UPDATE_PAYMENT_STATUS = "UPDATE_PAYMENT_STATUS";
	public static final String FN_PREVIEW_PDF = "PREVIEW_PDF";	
	
	public static final String MSG_SUCCESS_VERIFY_PDF = "PDF verification successful.";
	public static final String MSG_SUCCESS_SIGN_PDF = "PDF signing successful.";
	public static final String MSG_SUCCESS_VERIFY_QR = "QR verification successful.";
	public static final String MSG_SUCCESS_UPDATE_PAYMENT_STATUS = "Update payment status successful.";
	public static final String MSG_SUCCESS_PREVIEW_PDF_STATUS = "Generate preview pdf successful.";
		
	public static final String MSG_FAILED_VERIFY_PDF = "PDF verification failed.";
	public static final String MSG_FAILED_SIGN_PDF = "PDF signing failed.";
	public static final String MSG_FAILED_VERIFY_QR = "QR verification failed.";
	public static final String MSG_FAILED_UPDATE_PAYMENT_STATUS = "Update payment status failed.";
	public static final String MSG_FAILED_PREVIEW_PDF_STATUS = "Generate preview pdf failed.";
	
	public static final String FN_GET_SIGNING_DATA = "GET_SIGNING_DATA";
	public static final String MSG_SUCCESS_GET_SIGNING_DATA = "Get signing data successful.";
	public static final String MSG_FAILED_GET_SIGNING_DATA = "Get signing data failed.";
	
	public static final String FN_GET_PENDING_LIST = "GET_PENDING_LIST";
	public static final String MSG_SUCCESS_GET_PENDING_LIST = "Get pending list successful.";
	public static final String MSG_FAILED_GET_PENDING_LIST = "Get pending list failed.";
	
	public static final String FN_BUILD_SIGNED_PDF = "BUILD_SIGNED_PDF";
	public static final String MSG_SUCCESS_BUILD_SIGNED_PDF = "Build signed pdf successful.";
	public static final String MSG_FAILED_BUILD_SIGNED_PDF = "Build signed pdf failed.";
	
	public static final String FN_UPLOAD_PDF = "UPLOAD_PDF";
	public static final String MSG_SUCCESS_UPLOAD_PDF = "Upload pdf successful.";
	public static final String MSG_FAILED_UPLOAD_PDF = "Upload pdf failed.";
	
	
	
	
	
}
