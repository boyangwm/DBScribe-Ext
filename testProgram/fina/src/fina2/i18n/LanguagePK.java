/*
 * LanguagePK.java
 *
 * Created on October 15, 2001, 2:51 PM
 */

package fina2.i18n;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */

public class LanguagePK implements Serializable {

    private int id;

    public LanguagePK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof LanguagePK) {
            LanguagePK otherKey = (LanguagePK) o;
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
