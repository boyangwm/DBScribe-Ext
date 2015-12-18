/*
 * AbstractFunctionList.java
 *
 * Created on December 10, 2003, 5:16 PM
 */

package fina2.javascript.function;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author  Vano
 */
public abstract class AbstractFunctionList {
    protected Hashtable categories;

    public Function findFunction(fina2.javascript.element.Function fun) {
        Function ff = null;
        for (Iterator iter = categories.values().iterator(); iter.hasNext();) {
            Vector v = (Vector) iter.next();
            for (Iterator _iter = v.iterator(); _iter.hasNext();) {
                Function f = (Function) _iter.next();
                //System.out.print("Iterator "+f.getName()+" ");
                //System.out.println(f.getDummy());

                if ((f.getName().equals(fun.getName()) || f.getDummy().equals(
                        fun.getName()))
                        && (ff == null)) {
                    ff = f;
                }
                if ((f.getName().equals(fun.getName()) || f.getDummy().equals(
                        fun.getName()))
                        && (f.numberOfParameters() == fun.getParameters()
                                .numberOfChildren())) {
                    return f;
                }
            }
        }
        return ff;
    }

    public Collection getCategories() {
        return categories.keySet();
    }

    public Collection getFunctions(String categoryName) {
        return (Collection) categories.get(categoryName);
    }

    public abstract void load();
}
