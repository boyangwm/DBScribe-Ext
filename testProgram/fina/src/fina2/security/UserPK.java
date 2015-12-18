/*
 * UserPK.java
 *
 * Created on October 16, 2001, 11:45 AM
 */

package fina2.security;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */

public class UserPK implements Serializable {

    private int id;

    public UserPK(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof UserPK) {
            UserPK otherKey = (UserPK) o;
            return id == otherKey.id;
        } else
            return false;
    }

    public int hashCode() {
        return id;
    }
}
