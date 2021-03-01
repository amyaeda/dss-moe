package com.iris.dss.dto;

import java.util.Date;

public class DocReportDto {
	
	private int pdfId;
	private String approvalUserId;
	private String iptsName;
	private String fileName;
	private String qrcodeSerialNum;
	private Date pdfCreatedAt;
	private String approvalName;
	
	private int transId;
	private String requestId;
	private String module;
	private int status;
	private Date createdAt;
	private String moduleName; //modul pengajian
	
	public DocReportDto() {
	}
	public DocReportDto(DocReportDto dto)
	{
		super();
		this.pdfId = dto.getPdfId();
		this.approvalUserId = dto.getApprovalUserId();
		this.iptsName = dto.getIptsName();
		this.fileName = dto.getFileName();
		this.qrcodeSerialNum = dto.getQrcodeSerialNum();
		this.pdfCreatedAt = dto.getPdfCreatedAt();
		this.transId = dto.getTransId();
		this.requestId = dto.getRequestId();
		this.module = dto.getModule();
		this.status = dto.getStatus();
		this.createdAt = dto.getCreatedAt();
		this.approvalName = dto.getApprovalName();
		this.moduleName = dto.getModuleName();
	}
	public int getPdfId() {
		return pdfId;
	}
	public void setPdfId(int pdfId) {
		this.pdfId = pdfId;
	}
	public String getApprovalUserId() {
		return approvalUserId;
	}
	public void setApprovalUserId(String approvalUserId) {
		this.approvalUserId = approvalUserId;
	}
	public String getIptsName() {
		return iptsName;
	}
	public void setIptsName(String iptsName) {
		this.iptsName = iptsName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getQrcodeSerialNum() {
		return qrcodeSerialNum;
	}
	public void setQrcodeSerialNum(String qrcodeSerialNum) {
		this.qrcodeSerialNum = qrcodeSerialNum;
	}
	public Date getPdfCreatedAt() {
		return pdfCreatedAt;
	}
	public void setPdfCreatedAt(Date pdfCreatedAt) {
		this.pdfCreatedAt = pdfCreatedAt;
	}
	public int getTransId() {
		return transId;
	}
	public void setTransId(int transId) {
		this.transId = transId;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public String getApprovalName() {
		return approvalName;
	}
	public void setApprovalName(String approvalName) {
		this.approvalName = approvalName;
	}
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
}
