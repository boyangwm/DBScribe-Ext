/*
 * Parameter.java
 *
 * Created on 28 Èþëü 2002 ã., 13:42
 */

package fina2.javascript.function;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class Parameter {

    private String name;

    private String description;

    /** Creates new Parameter */
    public Parameter(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
