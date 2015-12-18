/*
 * Folder.java
 *
 * Created on 7 Сентябрь 2002 г., 19:55
 */

package fina2.reportoo.repository;

import java.util.Vector;

/**
 *
 * @author  Shota Shalamberidze
 * @version 
 */
public class Folder implements java.io.Serializable {

    private int id;
    private String name;
    private Vector children;

    /** Creates new Folder */
    public Folder(String name) {
        this.name = name;
        children = new Vector();
    }

    public Vector getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
