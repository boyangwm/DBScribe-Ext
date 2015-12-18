/*
 * ManagingBodyPK.java
 *
 * Created on 1 јпрель 2002 г., 9:52
 */

package fina2.bank;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ManagingBodyPK implements java.io.Serializable {

    /** Creates new ManagingBodyPK */

    private int id;

    /** Creates new BranchPK */
    public ManagingBodyPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof ManagingBodyPK) {
            ManagingBodyPK otherKey = (ManagingBodyPK) o;
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
