package com.iris.dss.utils;

/**
 * This class provides a list of all the status codes for the REST API.
 * 
 * @since 2019
 * @version 0.0.0.1
 * @author trlok
 *
 */
public class WSError {

	public static final int SUCCESS = 1; //1 success 0 failed
	
	public static final int SIGNER_ID_NOT_FOUND = 1000;
	public static final int SIGNER_KEY_NOT_FOUND = 1001;
	public static final int SIGNER_CERT_NOT_FOUND = 1002;
	public static final int PENDING_PAYMENT = 1003;
	public static final int SIGNER_CERT_EXPIRED = 1004;
	public static final int TOKEN_INVALID = 1005;	
	public static final int TOKEN_NOT_FOUND = 1006;	
	public static final int TOKEN_TYPE_INVALID = 1007;
	public static final int FILETYPE_CERT_NOT_FOUND = 1008;
	public static final int SIGNER_ID_NOT_VALID = 1009;
	
	public static final int INVALID_DOCUMENT = 2000;
	public static final int INVALID_QR_CONTENT = 2001;
	public static final int INVALID_QR_FORMAT = 2002;
	public static final int INVALID_SIGNATURE = 2003; //new
	
	public static final int INVALID_DOCUMENT_FORMAT = 3000;
	public static final int UNAUTHORIZED = 3001;
	
	public static final int PARAMETER_INVALID = 4000;
	public static final int INVALID_JSON_FORMAT = 4001;
	public static final int FILE_NOT_FOUND = 4002;
	public static final int INVALID_DOCUMENT_FOUND = 4003;
	public static final int INTERNAL_SERVER_ERROR = 5000;
	
	public static final int REQUEST_PARAM_ERROR = -2;
	public static final int REQUEST_PARSING_ERROR = -3;
	public static final int REQUEST_EMPTY_ERROR = -4;
	
	
	public static final int BASE64_CONTENT_DECODING_ERROR = -6;
	public static final int RECORD_NOT_FOUND = -7;
	public static final int INVALID_REQUEST_ID = -8;

}