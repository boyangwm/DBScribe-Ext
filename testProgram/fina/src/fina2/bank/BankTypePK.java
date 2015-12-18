/*
 * BankTypePK.java
 *
 * Created on October 19, 2001, 7:44 PM
 */

package fina2.bank;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class BankTypePK implements java.io.Serializable {

    private int id;

    public BankTypePK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof BankTypePK) {
            BankTypePK otherKey = (BankTypePK) o;
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
