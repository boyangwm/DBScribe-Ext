/*
 * BranchPK.java
 *
 * Created on March 17, 2002, 3:19 AM
 */

package fina2.bank;

/**
 *
 * @author  vasop
 * @version 
 */
public class BranchPK implements java.io.Serializable {

    private int id;

    /** Creates new BranchPK */
    public BranchPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BranchPK) {
            BranchPK otherKey = (BranchPK) o;
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
