/*
 * ReturnDefinitionPK.java
 *
 * Created on October 31, 2001, 11:27 AM
 */

package fina2.returns;

import java.io.Serializable;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ReturnDefinitionPK implements Serializable {

    private long id;

    /** Creates new ReturnDefinitionPK */
    public ReturnDefinitionPK(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean equals(Object o) {
        if (o instanceof ReturnDefinitionPK) {
            ReturnDefinitionPK otherKey = (ReturnDefinitionPK) o;
            return id == otherKey.id;
        } else
            return false;
    }

    public int hashCode() {
        return (int)id;
    }

}
