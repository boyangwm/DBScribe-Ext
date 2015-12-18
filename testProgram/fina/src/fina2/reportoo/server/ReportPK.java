/*
 * ReportPK.java
 *
 * Created on January 7, 2002, 3:39 PM
 */

package fina2.reportoo.server;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public class ReportPK implements java.io.Serializable {

    private int id;

    public ReportPK(int id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (o instanceof ReportPK) {
            ReportPK otherKey = (ReportPK) o;
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
