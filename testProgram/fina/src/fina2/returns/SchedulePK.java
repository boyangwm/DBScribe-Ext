/*
 * SchedulePK.java
 *
 * Created on November 6, 2001, 4:07 PM
 */

package fina2.returns;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class SchedulePK implements Serializable {

    private int id;

    /** Creates new SchedulePK */
    public SchedulePK(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof SchedulePK) {
            SchedulePK otherKey = (SchedulePK) o;
            return id == otherKey.id;
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }

}
