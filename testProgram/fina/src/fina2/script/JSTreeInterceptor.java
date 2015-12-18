/*
 * JSTreeInterceptor.java
 *
 * Created on 4 Ноябрь 2001 г., 22:25
 */

package fina2.script;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class JSTreeInterceptor {

    private Hashtable dependentNodes;

    /** Creates new JSTreeInterceptor */
    public JSTreeInterceptor() {
        dependentNodes = new Hashtable();
    }

    public Double lookup(String code) {
        if (code == null)
            return null;
        dependentNodes.put(code, code);
        return null; //new Double(1.0);
    }

    public Double lookup(String code, int rowNumber) {
        if (code == null)
            return null;
        dependentNodes.put(code, code);
        return null; //new Double(1.0);
    }

    public Double notrow(String code) {
        if (code == null)
            return null;
        dependentNodes.put(code, code);
        return null; //new Double(1.0);
    }

    public Double children(String code, String f) {
        //dependentNodes.put(code, code);
        return null; //new Double(1.0);
    }

    public String lookupString(String code) {
        if (code == null)
            return null;
        dependentNodes.put(code, code);
        return "";
    }
    public String lookupString(String code,int rowNumber) {
        if (code == null)
            return null;
        dependentNodes.put(code, code);
        return null;
    }
    public Vector getDependentNodes() {
        return new Vector(dependentNodes.values());
    }
}
