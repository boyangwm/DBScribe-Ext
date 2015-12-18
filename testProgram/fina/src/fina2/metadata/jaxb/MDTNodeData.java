package fina2.metadata.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "NODE", propOrder = { "ID", "CODE", "PARENTID", "TYPE", "DATATYPE", "EQUATION", "SEQUENCE", "EVALMETHOD", "DISABLED", "REQUIRED", "DEPENDENT_NODES", "DESCRIPTIONS", "COMPARISONS" })
public class MDTNodeData implements Serializable {
	private long ID;
	private String CODE;
	private long PARENTID;
	private long TYPE;
	private long DATATYPE;
	private String EQUATION;
	private long SEQUENCE;
	private long EVALMETHOD;
	private long DISABLED;
	private long REQUIRED;
	private MDTDependentNode DEPENDENT_NODES;
	private MDTDescription DESCRIPTIONS;
	private MDTComparison COMPARISONS;

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public String getCODE() {
		return CODE;
	}

	public void setCODE(String cODE) {
		CODE = cODE;
	}

	public long getPARENTID() {
		return PARENTID;
	}

	public void setPARENTID(long pARENTID) {
		PARENTID = pARENTID;
	}

	public long getTYPE() {
		return TYPE;
	}

	public void setTYPE(long tYPE) {
		TYPE = tYPE;
	}

	public long getDATATYPE() {
		return DATATYPE;
	}

	public void setDATATYPE(long dATATYPE) {
		DATATYPE = dATATYPE;
	}

	public String getEQUATION() {
		return EQUATION;
	}

	public void setEQUATION(String eQUATION) {
		EQUATION = eQUATION;
	}

	public long getSEQUENCE() {
		return SEQUENCE;
	}

	public void setSEQUENCE(long sEQUENCE) {
		SEQUENCE = sEQUENCE;
	}

	public long getEVALMETHOD() {
		return EVALMETHOD;
	}

	public void setEVALMETHOD(long eVALMETHOD) {
		EVALMETHOD = eVALMETHOD;
	}

	public long getDISABLED() {
		return DISABLED;
	}

	public void setDISABLED(long dISABLED) {
		DISABLED = dISABLED;
	}

	public long getREQUIRED() {
		return REQUIRED;
	}

	public void setREQUIRED(long rEQUIRED) {
		REQUIRED = rEQUIRED;
	}


	public MDTDependentNode getDEPENDENT_NODES() {
		return DEPENDENT_NODES;
	}

	public void setDEPENDENT_NODES(MDTDependentNode dEPENDENTNODES) {
		DEPENDENT_NODES = dEPENDENTNODES;
	}
	
	public MDTDescription getDESCRIPTIONS() {
		return DESCRIPTIONS;
	}

	public void setDESCRIPTIONS(MDTDescription dESCRIPTIONS) {
		DESCRIPTIONS = dESCRIPTIONS;
	}

	public MDTComparison getCOMPARISONS() {
		return COMPARISONS;
	}

	public void setCOMPARISONS(MDTComparison cOMPARISONS) {
		COMPARISONS = cOMPARISONS;
	}

}