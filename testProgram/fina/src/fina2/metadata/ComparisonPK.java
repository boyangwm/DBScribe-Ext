/*
 * ComparisonPK.java
 *
 * Created on December 24, 2001, 11:58 PM
 */

package fina2.metadata;

/**
 *
 * @author  Shota Shalamberidze
 * @version 
 */
public class ComparisonPK implements java.io.Serializable {

    private int id;
    private int nodeID;

    /** Creates new ComparisonPK */
    public ComparisonPK(int id, int nodeID) {
        this.id = id;
        this.nodeID = nodeID;
    }

    public boolean equals(Object o) {
        if (o instanceof ComparisonPK) {
            ComparisonPK otherKey = (ComparisonPK) o;
            return ((id == otherKey.getId()) && (nodeID == otherKey.getNodeID()));
        } else
            return false;
    }

    public int hashCode() {
        return nodeID * 1000 + id;
    }

    public int getId() {
        return id;
    }

    public int getNodeID() {
        return nodeID;
    }
}
