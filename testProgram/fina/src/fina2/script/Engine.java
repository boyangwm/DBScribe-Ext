/*
 * Engine.java
 *
 * Created on 4 Ноябрь 2001 г., 22:23
 */

package fina2.script;

import java.util.Collection;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public class Engine {

    /** Creates new Engine */
    public Engine() {
    }

    public static Collection evalNodeInterceptor(String source)
            throws JavaScriptException {
        if (source == null)
            return new java.util.Vector();

        source = "function fina2_mdt_node() {\n" + source + "\n}";

        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects(null);

            JSTreeInterceptor tree = new JSTreeInterceptor();

            Scriptable jsArgs = Context.toObject(tree, scope);
            scope.put("tree", scope, jsArgs);
            
            cx.evaluateString(scope, source, "<script>", 1, null);

            Object f = scope.get("fina2_mdt_node", scope);
            Object[] functionArgs = new Object[0];
            Object result = ((Function) f).call(cx, scope, scope, functionArgs);

            Context.exit();
            return tree.getDependentNodes();
        } catch (JavaScriptException e) {
            Context.exit();
            throw e;
        }
    }
}
