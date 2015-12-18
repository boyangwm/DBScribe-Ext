/*
 * PeiordTypePK.java
 *
 * Created on October 20, 2001, 2:30 PM
 */

package fina2.period;

/**
 *
 * @author  Administrator
 * @version 
 */
public class PeriodTypePK implements java.io.Serializable {

    private int id;

    public PeriodTypePK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof PeriodTypePK) {
            PeriodTypePK otherKey = (PeriodTypePK) o;
            return id == otherKey.getId();
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
