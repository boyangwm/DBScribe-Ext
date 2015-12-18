/*
 * MenuPK.java
 *
 * Created on October 16, 2001, 1:54 PM
 */

package fina2.ui.menu;

import java.io.Serializable;

public class MenuPK implements Serializable {
    private int id;

    public MenuPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof MenuPK) {
            MenuPK otherKey = (MenuPK) o;
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
