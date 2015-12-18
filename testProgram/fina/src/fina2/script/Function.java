/*
 * Function.java
 *
 * Created on December 8, 2001, 9:04 PM
 */

package fina2.script;

import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class Function {

    private String name;
    private Vector parameters;

    /** Creates new Function */
    public Function(String name) {
        this.name = name;
        parameters = new Vector();
    }

    public void addParameter(Parameter param) {
        parameters.add(param);
    }

    public Vector getParameters() {
        return parameters;
    }

    public String valuesToString() {
        String s = name + "(";
        for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            Parameter p = (Parameter) iter.next();
            s += p.getValue();
            if (iter.hasNext())
                s += ", ";
        }
        s += ")";
        return s;
    }

    public String toString() {
        String s = name + "(";
        for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            Parameter p = (Parameter) iter.next();
            s += p.getName();
            if (iter.hasNext())
                s += ", ";
        }
        s += ")";
        return s;
    }
}
