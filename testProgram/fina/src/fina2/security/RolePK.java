/*
 * RolePK.java
 *
 * Created on October 30, 2001, 2:38 PM
 */

package fina2.security;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class RolePK implements Serializable {

    private int id;

    public RolePK(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof RolePK) {
            RolePK otherKey = (RolePK) o;
            return id == otherKey.id;
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }

}
