package fina2.returns.cbn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HEADER", propOrder = { "CALLREPORT_ID", "CALLREPORT_DESC", "INST_CODE", "INST_NAME", "AS_AT" })
public class HeaderType {
	private String CALLREPORT_ID;
	private String CALLREPORT_DESC;
	private String INST_CODE;
	private String INST_NAME;
	private String AS_AT;

	public String getCALLREPORT_ID() {
		return CALLREPORT_ID;
	}

	public void setCALLREPORT_ID(String cALLREPORTID) {
		CALLREPORT_ID = cALLREPORTID;
	}

	public String getCALLREPORT_DESC() {
		return CALLREPORT_DESC;
	}

	public void setCALLREPORT_DESC(String cALLREPORTDESC) {
		CALLREPORT_DESC = cALLREPORTDESC;
	}

	public String getINST_CODE() {
		return INST_CODE;
	}

	public void setINST_CODE(String iNSTCODE) {
		INST_CODE = iNSTCODE;
	}

	public String getINST_NAME() {
		return INST_NAME;
	}

	public void setINST_NAME(String iNSTNAME) {
		INST_NAME = iNSTNAME;
	}

	public String getAS_AT() {
		return AS_AT;
	}

	public void setAS_AT(String aSAT) {
		AS_AT = aSAT;
	}
}