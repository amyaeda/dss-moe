package com.iris.dss.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*Token ini digunakan untuk pengenalan kpd sistem*/

/*
id int(11) AI PK 
filename varchar(45) 
filepath varchar(100) 
qrcode blob 
createdAt timestamp
 */

@Entity
@Table(name = "pdf_document")
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdfDoc {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private int id;
	
	@Column(name="requestId")
	private String requestId;
	
	@Column(name="approvalUserId")
	private String approvalUserId;
	
	@Column(name="tokenId")
	private int tokenId;
	 	
	@Column(name="iptsName")
	private String iptsName;
	
	@Column(name="originalfileName")
	private String originalFileName;
	
	@Column(name="fileName")
	private String fileName;
	
	@Column(name="moduleName")
	private String moduleName;
	
	@Column(name="filePath")
	private String filePath;
	
	@Column(name="signedFilePath")
	private String signedFilePath;
	
	@Column(name="qrcode")
	private byte[] qrcode;
	
	@Column(name="sha256Checksum")
	private String sha256Checksum;
		
	@Column(name="sha256ChecksumB4Sign")
	private String sha256ChecksumB4Sign;
	
	@Column(name="qrcodeSerialNum")
	private String qrcodeSerialNum;
	
	@Column(name="checkPayment")
	private int checkPayment;
	
	@Column(name="paid")
	private int paid;
	
	@Column(name="previewSettingId")
	private int previewSettingId;
	
	@Column(name="createdAt")
	private Date createdAt;
	
	@Column(name = "status")
	private int status;
	
	@Column(name="docName")
	private String docName;
	
	
	public String getSha256ChecksumB4Sign() {
		return sha256ChecksumB4Sign;
	}
	public void setSha256ChecksumB4Sign(String sha256ChecksumB4Sign) {
		this.sha256ChecksumB4Sign = sha256ChecksumB4Sign;
	}
	public String getDocName() {
		return docName;
	}
	public void setDocName(String docName) {
		this.docName = docName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getApprovalUserId() {
		return approvalUserId;
	}
	public void setApprovalUserId(String approvalUserId) {
		this.approvalUserId = approvalUserId;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public int getTokenId() {
		return tokenId;
	}
	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getSignedFilePath() {
		return signedFilePath;
	}
	public void setSignedFilePath(String signedFilePath) {
		this.signedFilePath = signedFilePath;
	}
	public byte[] getQrcode() {
		return qrcode;
	}
	public void setQrcode(byte[] qrcode) {
		this.qrcode = qrcode;
	}
	public String getIptsName() {
		return iptsName;
	}
	public void setIptsName(String iptsName) {
		this.iptsName = iptsName;
	}
	
	public String getOriginalFileName() {
		return originalFileName;
	}
	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getSha256Checksum() {
		return sha256Checksum;
	}
	public void setSha256Checksum(String sha256Checksum) {
		this.sha256Checksum = sha256Checksum;
	}
	public String getQrcodeSerialNum() {
		return qrcodeSerialNum;
	}
	public void setQrcodeSerialNum(String qrcodeSerialNum) {
		this.qrcodeSerialNum = qrcodeSerialNum;
	}
	public int getCheckPayment() {
		return checkPayment;
	}
	public void setCheckPayment(int checkPayment) {
		this.checkPayment = checkPayment;
	}
	public int getPaid() {
		return paid;
	}
	public void setPaid(int paid) {
		this.paid = paid;
	}
	public int getPreviewSettingId() {
		return previewSettingId;
	}
	public void setPreviewSettingId(int previewSettingId) {
		this.previewSettingId = previewSettingId;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
		
	
}
