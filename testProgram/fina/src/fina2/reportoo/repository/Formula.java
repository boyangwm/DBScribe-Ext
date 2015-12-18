/*
 * Formula.java
 *
 * Created on 7 Сентябрь 2002 г., 17:31
 */

package fina2.reportoo.repository;

import java.util.Vector;

/**
 *
 * @author  Shota Shalamberidze
 * @version 
 */
public class Formula implements java.io.Serializable {

    private int id;
    private String name;
    private String description;
    private Vector parameters;
    private String formula;

    public Formula() {
        this("", "");
        formula = "";
    }

    /** Creates new Formula */
    public Formula(String name, String description) {
        this.name = name;
        this.description = description;
        parameters = new Vector();
        formula = "";
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormula() {
        return formula;
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Vector getParameters() {
        return parameters;
    }

    public String toString() {
        return name;
    }
}
