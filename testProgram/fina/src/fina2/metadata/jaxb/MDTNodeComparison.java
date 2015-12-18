package fina2.metadata.jaxb;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "NODE_COMPARISON", propOrder = { "ID", "NODEID", "EQUATION", "CONDITION" })
public class MDTNodeComparison implements Serializable{
	private long ID;

	private long NODEID;

	private String EQUATION;

	private long CONDITION;

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public long getNODEID() {
		return NODEID;
	}

	public void setNODEID(long nODEID) {
		NODEID = nODEID;
	}

	public String getEQUATION() {
		return EQUATION;
	}

	public void setEQUATION(String eQUATION) {
		EQUATION = eQUATION;
	}

	public long getCONDITION() {
		return CONDITION;
	}

	public void setCONDITION(long cONDITION) {
		CONDITION = cONDITION;
	}

}
