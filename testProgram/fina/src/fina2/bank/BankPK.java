/*
 * BankPK.java
 *
 * Created on October 16, 2001, 11:49 AM
 */

package fina2.bank;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class BankPK implements java.io.Serializable {

    private int id;

    /** Creates new BankPK */
    public BankPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BankPK) {
            BankPK otherKey = (BankPK) o;
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
