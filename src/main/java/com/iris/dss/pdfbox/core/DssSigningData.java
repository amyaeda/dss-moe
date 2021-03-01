package com.iris.dss.pdfbox.core;

import java.util.Date;

public class DssSigningData {

	private byte[] mockPdfBytes = { };
	private byte[] contentBytes = { };
	private byte[] rawBytes = { };
	private byte[] digestBytes = { };
	private Date signingTime = null;
	private byte[] signature = { };
	
	public DssSigningData() {
		
	}
	
	public DssSigningData(byte[] mockPdfBytes, byte[] contentBytes, byte[] rawBytes, byte[] digestBytes, Date signingTime, byte[] signature) {
		//super();
		this.mockPdfBytes = mockPdfBytes;
		this.contentBytes = contentBytes;
		this.rawBytes = rawBytes;
		this.digestBytes = digestBytes;
		this.signingTime = signingTime;
		this.signature = signature;
	}
	
	public byte[] getMockPdfBytes() {
		return this.mockPdfBytes;
	}

	public void setMockPdfBytes(byte[] mockPdfBytes) {
		this.mockPdfBytes = mockPdfBytes;
	}

	/**
	 * set PDF signing buffers
	 * @param data
	 */
	public void setContentBytes(byte[] data) {
		this.contentBytes = data;
	}	
	public byte[] getContentBytes() {
		return this.contentBytes;
	}
	
	/**
	 * set PDF signing buffers (ASN1 Encoded)
	 * @param data
	 */
	public void setRawBytes(byte[] data) {
		this.rawBytes = data;
	}	
	public byte[] getRawBytes() {
		return this.rawBytes;
	}
	
	/**
	 * digest for signing buffers
	 * @param data
	 */
	public void setDigestBytes(byte[] data) {
		this.digestBytes = data;
	}	
	public byte[] getDigestBytes() {
		return this.digestBytes;
	}
	
	/**
	 * set signing time 
	 * @param signingTime
	 */
	public void setSigningTime(Date signingTime) {
		this.signingTime = signingTime;
	}
	public Date getSigningTime() {
		return this.signingTime;
	}
	
	public byte[] getSignature() {
		return this.signature;
	}
	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
}
