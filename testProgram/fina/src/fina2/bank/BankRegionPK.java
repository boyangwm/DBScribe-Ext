/*
 * BankRegionPK.java
 *
 * Created on March 25, 2002, 12:31 AM
 */

package fina2.bank;

/**
 *
 * @author  vasop
 * @version 
 */
@Deprecated
public class BankRegionPK implements java.io.Serializable {

    private int id;

    /** Creates new BankRegionPK */
    public BankRegionPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BankRegionPK) {
            BankRegionPK otherKey = (BankRegionPK) o;
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
