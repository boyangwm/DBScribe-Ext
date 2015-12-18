/*
 * ReturnDefinitionTablePK.java
 *
 * Created on 11 јпрель 2002 г., 17:40
 */

package fina2.returns;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ReturnDefinitionTablePK implements java.io.Serializable {

    private int definitionID;
    private int tableID;

    private int type;
    private int nodeID;

    /** Creates new ReturnDefinitionTablePK */
    public ReturnDefinitionTablePK(int definitionID, int tableID) {
        this.definitionID = definitionID;
        this.tableID = tableID;
    }

    public ReturnDefinitionTablePK(int definitionID, int tableID, int type,
            int nodeID) {
        this.definitionID = definitionID;
        this.tableID = tableID;
        this.type = type;
        this.nodeID = nodeID;
    }

    public int getDefinitionID() {
        return definitionID;
    }

    public int getTableID() {
        return tableID;
    }

    public int getType() {
        return type;
    }

    public int getNodeID() {
        return nodeID;
    }

    public boolean equals(Object o) {
        if (o instanceof ReturnDefinitionTablePK) {
            ReturnDefinitionTablePK otherKey = (ReturnDefinitionTablePK) o;
            return ((definitionID == otherKey.getDefinitionID()) && (tableID == otherKey
                    .getTableID()));
        } else
            return false;
    }

    public int hashCode() {
        return definitionID + tableID * 100000;
    }

}
