/*
 * Function.java
 *
 * Created on 28 Èþëü 2002 ã., 13:33
 */

package fina2.javascript.function;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class Function {

    private String name;

    private String fullName;

    private String description;

    private Vector parameters;

    /** Creates new Function */
    public Function(String name, String fullName, String description) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        parameters = new Vector();
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public int numberOfParameters() {
        return parameters.size();
    }

    public String getDescription() {
        return description;
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public Collection getParameters() {
        return parameters;
    }

    public String getFullDescription() {
        String s = fullName + "\n" + description + "\n\nParameters:\n";
        for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            Parameter p = (Parameter) iter.next();
            s += "    " + p.getName() + " - " + p.getDescription() + "\n";
        }
        return s;
    }

    public String getDummy() {
        if (fullName.indexOf('(') != -1)
            return fullName.substring(0, fullName.indexOf('('));
        else if (fullName.indexOf(' ') != -1)
            return fullName.substring(0, fullName.indexOf(' '));
        else
            return "";
    }

    public String toString() {
        return fullName;
    }
}
