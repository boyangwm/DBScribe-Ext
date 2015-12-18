/*
 * BranchManagementPK.java
 *
 * Created on March 19, 2002, 1:54 AM
 */

package fina2.bank;

/**
 *
 * @author  vasop
 * @version 
 */
public class BranchManagPK implements java.io.Serializable {

    private int id;

    /** Creates new BranchManagPK */
    public BranchManagPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BranchManagPK) {
            BranchManagPK otherKey = (BranchManagPK) o;
            return (id == otherKey.getId());
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

}
