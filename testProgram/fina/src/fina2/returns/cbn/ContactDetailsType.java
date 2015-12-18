package fina2.returns.cbn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CONTACT_DETAILS",propOrder={"NAME","TEL_NO"})
public class ContactDetailsType {
	private String NAME;
	private String TEL_NO;

	public String getNAME() {
		return NAME;
	}

	public void setNAME(String nAME) {
		NAME = nAME;
	}

	public String getTEL_NO() {
		return TEL_NO;
	}

	public void setTEL_NO(String tELNO) {
		TEL_NO = tELNO;
	}
}