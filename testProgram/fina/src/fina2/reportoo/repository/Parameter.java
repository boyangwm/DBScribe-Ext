/*
 * Parameter.java
 *
 * Created on 6 Сентябрь 2002 г., 23:48
 */

package fina2.reportoo.repository;

/**
 *
 * @author  Shota Shalamberidze
 * @version 
 */
public class Parameter implements java.io.Serializable {

    private int id;
    private String name;
    private String description;
    private int type;

    /** Creates new Parameter */
    public Parameter(String name, String description, int type) {
        this.name = name;
        this.description = description;
        this.type = type;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public String toString() {
        return name;
    }
}
