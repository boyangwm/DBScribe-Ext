package fina2.upload;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class UploadedFileInfo implements Serializable{

	private int fileId;
	private String user;
	private String filename;
	private String status;
	private Date uploadTime;
	private String bank;

	public UploadedFileInfo(int id,String user,String filename,int statusNumber,Date uploadDate,String bank){
		this.fileId = id;
		this.filename = filename;
		this.user = user;
		this.status = uploadStatus(statusNumber);
		this.uploadTime = uploadDate;
		this.bank = bank;
	}
	
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
		
	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}
	
	private String uploadStatus(int statusNumber) {
		
		UploadFileStatus [] statuses = UploadFileStatus.values();
		return statuses[statusNumber].getCode();
	}

}
