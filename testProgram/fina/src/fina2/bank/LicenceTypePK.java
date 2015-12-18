/*
 * LicenceTypePK.java
 *
 * Created on October 29, 2001, 17:44 PM
 */

package fina2.bank;

/**
 *
 * @author  Administrator
 * @version 
 */
public class LicenceTypePK implements java.io.Serializable {

    private int id;

    public LicenceTypePK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof LicenceTypePK) {
            LicenceTypePK otherKey = (LicenceTypePK) o;
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
