package fina2.returns;

import java.io.Serializable;
import java.util.Arrays;

import javax.ejb.Handle;

@SuppressWarnings("serial")
public class ImportedReturn implements Serializable {

	private int id;
	private byte[] xml;
	private String version;
	private String bankCode;
	private ImportStatus status;
	private Handle userHandle;
	private Handle languageHandle;

	public int getId() {
		return id;
	}

	public Handle getLanguageHandle() {
		return languageHandle;
	}

	public Handle getUserHandle() {
		return userHandle;
	}

	public byte[] getXml() {
		return xml;
	}

	public String getVersion() {
		return version;
	}

	public ImportStatus getStatus() {
		return status;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLanguageHandle(Handle languageHandle) {
		this.languageHandle = languageHandle;
	}

	public void setUserHandle(Handle userHandle) {
		this.userHandle = userHandle;
	}

	public void setXml(byte[] xml) {
		this.xml = xml;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setStatus(ImportStatus status) {
		this.status = status;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bankCode == null) ? 0 : bankCode.hashCode());
		result = prime * result + id;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + Arrays.hashCode(xml);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImportedReturn other = (ImportedReturn) obj;
		if (bankCode == null) {
			if (other.bankCode != null)
				return false;
		} else if (!bankCode.equals(other.bankCode))
			return false;
		if (id != other.id)
			return false;
		if (status != other.status)
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (!Arrays.equals(xml, other.xml))
			return false;
		return true;
	}

	public String toString() {
		return "[id=" + id + ";bankcode=" + bankCode + ";Status=" + status + ";Version=" + version + "]";
	}
}
