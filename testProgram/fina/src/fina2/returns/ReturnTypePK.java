/*
 * ReturnTypePK.java
 *
 * Created on October 31, 2001, 11:35 AM
 */

package fina2.returns;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ReturnTypePK implements Serializable {

    private int id;

    /** Creates new ReturnTypePK */
    public ReturnTypePK(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof ReturnTypePK) {
            ReturnTypePK otherKey = (ReturnTypePK) o;
            return id == otherKey.id;
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }

}
