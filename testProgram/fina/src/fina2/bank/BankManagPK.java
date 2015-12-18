/*
 * BankManagPK.java
 *
 * Created on April 3, 2002, 9:45 PM
 */

package fina2.bank;

/**
 *
 * @author  vasop
 * @version 
 */
public class BankManagPK implements java.io.Serializable {

    private int id;

    /** Creates new BankManagPK */
    public BankManagPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BankManagPK) {
            BankManagPK otherKey = (BankManagPK) o;
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
