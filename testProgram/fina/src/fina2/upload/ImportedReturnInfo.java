package fina2.upload;

import java.io.Serializable;
import java.util.Date;

import fina2.returns.ImportStatus;

@SuppressWarnings("serial")
public class ImportedReturnInfo implements Serializable{

	private int id;
	private String returnCode;
	private String version;
	private String bank;
	private String username;
	private Date fromDate;
	private Date toDate;
	private String status;
	private String message;

	public ImportedReturnInfo(int id, String returnCode, String version, String bank, String username, Date fromDate, Date toDate, int statusNumber, String message) {
		this.id = id;
		this.bank = bank;
		this.returnCode = returnCode;
		this.version = version;
		this.username = username;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.status = returnStatus(statusNumber);
		this.message = message;
	}
	
	public int getId() {
		return id;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public String getVersion() {
		return version;
	}

	public String getBank() {
		return bank;
	}

	public String getUsername() {
		return username;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private String returnStatus(int statusNumber){
		
		ImportStatus [] statuses = ImportStatus.values();
		return statuses[statusNumber].getCode();
	}
}
