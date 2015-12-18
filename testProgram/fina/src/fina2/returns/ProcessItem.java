/*
 * ProcessItem.java
 *
 * Created on December 20, 2001, 10:41 PM
 */
package fina2.returns;

import java.io.Serializable;

public class ProcessItem implements Serializable {
	public long nodeID;
	public int nodeType;
	public int nodeEvalType;
	public long parentID;
	public int rowNumber;
	public int returnID;
	public int tableID;
	public int tableType;
	public String value;
	public String equation;
	public int tableEvalType;
	public String code;
	public int dataType;

	public String toString() {
		return "[nodeid=" + nodeID + ",code=" + code + ",equation=" + equation + "," + "value=" + value + "]";
	}
}
