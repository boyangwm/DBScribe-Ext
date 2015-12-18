/*
 * ReturnPK.java
 *
 * Created on November 7, 2001, 3:16 PM
 */

package fina2.returns;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ReturnPK implements Serializable {

    private int id;

    /** Creates new ReturnPK */
    public ReturnPK(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof ReturnPK) {
            ReturnPK otherKey = (ReturnPK) o;
            return id == otherKey.id;
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }

}
