package com.iris.dss.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pdf_signing_data")
public class PdfSigningData {	
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="requestId")
	private String requestId;
	
	@Column(name="signerId")
	private String signerId;
	
	@Column(name="userCertBytes")
	private byte[] userCertBytes;
	
	@Column(name="mockPdfBytes")
	private byte[] mockPdfBytes;
	
	@Column(name="contentBytes")
	private byte[] contentBytes;
	
	@Column(name="rawBytes")
	private byte[] rawBytes;
	
	@Column(name="digestBytes")
	private byte[] digestBytes;
	
	@Column(name="signingTime")
	private long signingTime;
	
	@Column(name="signature")
	private byte[] signature;
	
	public PdfSigningData(String requestId, String signerId,
			byte[] userCertBytes, byte[] mockPdfBytes, 
			byte[] contentBytes, byte[] rawBytes, byte[] digestBytes,
			long signingTime, byte[] signature) {
		//super();
		
		this.requestId = requestId;
		this.signerId = signerId;
		this.userCertBytes = userCertBytes;
		this.mockPdfBytes = mockPdfBytes;
		this.contentBytes = contentBytes;
		this.rawBytes = rawBytes;
		this.digestBytes = digestBytes;
		this.signingTime = signingTime;
		this.signature = signature;
	}
	
	public PdfSigningData() {
		// TODO Auto-generated constructor stub
	}

	public String getUserId() {
		return signerId;
	}

	public void setUserId(String signerId) {
		this.signerId = signerId;
	}

	public byte[] getUserCertBytes() {
		return userCertBytes;
	}

	public void setUserCertBytes(byte[] userCertBytes) {
		this.userCertBytes = userCertBytes;
	}

	public byte[] getMockPdfBytes() {
		return mockPdfBytes;
	}

	public void setMockPdfBytes(byte[] mockPdfBytes) {
		this.mockPdfBytes = mockPdfBytes;
	}

	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public byte[] getContentBytes() {
		return contentBytes;
	}
	public void setContentBytes(byte[] contentBytes) {
		this.contentBytes = contentBytes;
	}
	public byte[] getRawBytes() {
		return rawBytes;
	}
	public void setRawBytes(byte[] rawBytes) {
		this.rawBytes = rawBytes;
	}
	public byte[] getDigestBytes() {
		return digestBytes;
	}
	public void setDigestBytes(byte[] digestBytes) {
		this.digestBytes = digestBytes;
	}
	public long getSigningTime() {
		return signingTime;
	}
	public void setSigningTime(long signingTime) {
		this.signingTime = signingTime;
	}
	public byte[] getSignature() {
		return signature;
	}
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	
}
