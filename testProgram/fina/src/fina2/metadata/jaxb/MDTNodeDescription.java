package fina2.metadata.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DESCRIPTION")
public class MDTNodeDescription implements Serializable {
	private String LANG_CODE;
	private String VALUE;

	public String getLANG_CODE() {
		return LANG_CODE;
	}

	public void setLANG_CODE(String lANGCODE) {
		LANG_CODE = lANGCODE;
	}

	public String getVALUE() {
		return VALUE;
	}

	public void setVALUE(String vALUE) {
		VALUE = vALUE;
	}

}
