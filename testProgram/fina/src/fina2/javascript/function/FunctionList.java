/*
 * FunctionList.java
 *
 * Created on 28 Èþëü 2002 ã., 13:33
 */

package fina2.javascript.function;

import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author David Shalamberidze
 * @version
 */
public class FunctionList extends AbstractFunctionList {

	private fina2.ui.UIManager ui;

	/** Creates new FunctionList */
	public FunctionList(fina2.ui.UIManager ui) {
		this.ui = ui;
		categories = new Hashtable();
	}

	public void load() {
		Vector v = new Vector();
		Function f = new Function("tree.lookup", "LOOKUP(node)", ui.getString("fina2.javascript.functions.lookup1"));
		Parameter p = new Parameter("node", ui.getString("fina2.javascript.functions.lookup1.node"));
		f.addParameter(p);
		v.add(f);

		f = new Function("tree.lookup", "LOOKUP(node, row)", ui.getString("fina2.javascript.functions.lookup2"));
		p = new Parameter("node", ui.getString("fina2.javascript.functions.lookup2.node"));
		f.addParameter(p);
		p = new Parameter("row", ui.getString("fina2.javascript.functions.lookup2.row"));
		f.addParameter(p);
		v.add(f);

		f = new Function("tree.lookupString", "LOOKUPSTRING(node)", ui.getString("fina2.javascript.functions.lookupString"));
		p = new Parameter("node", ui.getString("fina2.javascript.functions.lookupString.node"));
		f.addParameter(p);
		v.add(f);

		f = new Function("tree.lookupString", "LOOKUPSTRING(node,rowNumber)", ui.getString("fina2.javascript.functions.lookupString"));
		p = new Parameter("node", ui.getString("fina2.javascript.functions.lookupString.node"));
		f.addParameter(p);
		p = new Parameter("rowNumber", ui.getString("fina2.javascript.functions.lookupString.row"));
		f.addParameter(p);
		v.add(f);
		
		f = new Function("tree.children", "CHILDREN(node, fun)", ui.getString("fina2.javascript.functions.children"));
		p = new Parameter("node", ui.getString("fina2.javascript.functions.children.node"));
		f.addParameter(p);
		p = new Parameter("fun", ui.getString("fina2.javascript.functions.children.fun"));
		f.addParameter(p);
		v.add(f);

		f = new Function("tree.notrow", "NOTROW(node)", ui.getString("fina2.javascript.functions.notrow"));
		p = new Parameter("node", ui.getString("fina2.javascript.functions.notrow.node"));
		f.addParameter(p);
		v.add(f);

		categories.put(ui.getString("fina2.javascript.category.metadata"), v);

		v = new Vector();
		f = new Function("if", "if(condition) { } else { }", ui.getString("fina2.javascript.functions.if"));
		p = new Parameter("condition", ui.getString("fina2.javascript.functions.if.condition"));
		f.addParameter(p);

		v.add(f);

		/*
		 * f = new Function( "else", "ELSE",
		 * ui.getString("fina2.javascript.functions.else") );
		 * 
		 * v.add(f);
		 */

		categories.put(ui.getString("fina2.javascript.category.conditional"), v);

		v = new Vector();
		f = new Function("while", "while(condition) { }", ui.getString("fina2.javascript.functions.while"));
		p = new Parameter("condition", ui.getString("fina2.javascript.functions.while.condition"));
		f.addParameter(p);

		v.add(f);

		f = new Function("for", "for(init; condition; operation) { }", ui.getString("fina2.javascript.functions.for"));
		p = new Parameter("init", ui.getString("fina2.javascript.functions.for.init"));
		f.addParameter(p);
		p = new Parameter("condition", ui.getString("fina2.javascript.functions.for.condition"));
		f.addParameter(p);
		p = new Parameter("operation", ui.getString("fina2.javascript.functions.for.operation"));
		f.addParameter(p);

		v.add(f);

		categories.put(ui.getString("fina2.javascript.category.loops"), v);

		v = new Vector();
		f = new Function("return", "return value", ui.getString("fina2.javascript.functions.return"));
		p = new Parameter("value", ui.getString("fina2.javascript.functions.return.value"));
		f.addParameter(p);

		v.add(f);

		categories.put(ui.getString("fina2.javascript.category.system"), v);

		v = new Vector();
		f = new Function("Math.max", "MAX(value1, value2)", ui.getString("fina2.javascript.functions.max"));
		p = new Parameter("value1", ui.getString("fina2.javascript.functions.max.val1"));
		f.addParameter(p);

		p = new Parameter("value1", ui.getString("fina2.javascript.functions.max.val2"));
		f.addParameter(p);
		v.add(f);

		f = new Function("Math.min", "MIN(value1, value2)", ui.getString("fina2.javascript.functions.min"));
		p = new Parameter("value1", ui.getString("fina2.javascript.functions.min.val1"));
		f.addParameter(p);

		p = new Parameter("value2", ui.getString("fina2.javascript.functions.min.val2"));
		f.addParameter(p);
		v.add(f);

		f = new Function("Math.pow", "POWER(value1, value2)", ui.getString("fina2.javascript.functions.pow"));
		p = new Parameter("value1", ui.getString("fina2.javascript.functions.pow.val1"));
		f.addParameter(p);

		p = new Parameter("value2", ui.getString("fina2.javascript.functions.pow.val2"));
		f.addParameter(p);
		v.add(f);

		f = new Function("Math.round", "ROUND(value)", ui.getString("fina2.javascript.functions.round"));
		p = new Parameter("value", ui.getString("fina2.javascript.functions.round.value"));
		f.addParameter(p);
		v.add(f);

		f = new Function("Math.sqrt", "SQUAREROOT(value)", ui.getString("fina2.javascript.functions.sqrt"));
		p = new Parameter("value", ui.getString("fina2.javascript.functions.sqrt.value"));
		f.addParameter(p);
		v.add(f);

		categories.put(ui.getString("fina2.javascript.category.mathematical"), v);
	}
}
