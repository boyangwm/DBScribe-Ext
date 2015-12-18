package fina2.returns.cbn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FOOTER")
public class FooterType {
	private AuthSignatoryType AUTH_SIGNATORY;
	private ContactDetailsType CONTACT_DETAILS;
    private String DESC;
    
	public AuthSignatoryType getAUTH_SIGNATORY() {
		return AUTH_SIGNATORY;
	}

	public void setAUTH_SIGNATORY(AuthSignatoryType aUTHSIGNATORY) {
		AUTH_SIGNATORY = aUTHSIGNATORY;
	}

	public ContactDetailsType getCONTACT_DETAILS() {
		return CONTACT_DETAILS;
	}

	public void setCONTACT_DETAILS(ContactDetailsType cONTACTDETAILS) {
		CONTACT_DETAILS = cONTACTDETAILS;
	}

	public String getDESC() {
		return DESC;
	}

	public void setDESC(String dESC) {
		DESC = dESC;
	}
}