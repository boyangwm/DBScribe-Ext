/*
 * Parser.java
 *
 * Created on 25 Èþëü 2002 ã., 12:44
 */

package fina2.javascript;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;

import fina2.javascript.element.Scope;
import fina2.javascript.function.AbstractFunctionList;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public class Parser {

    private String source;

    private Scope tree;

    private static AbstractFunctionList fl;

    /** Creates new Parser */
    public Parser(AbstractFunctionList fl) {
        this("", fl);
    }

    public Parser(String source, AbstractFunctionList fl) {
        this.source = source;
        this.fl = fl;
    }

    public static String findDummy(fina2.javascript.element.Function fun) {
        try {
            String s = fl.findFunction(fun).getDummy();
            if (!s.equals(""))
                return s;
        } catch (Exception e) {
        }
        return fun.getName();
    }

    public static String findNonDummy(fina2.javascript.element.Function fun) {
        try {
            String s = fl.findFunction(fun).getName();
            if (!s.equals(""))
                return s;
        } catch (Exception e) {
        }
        return fun.getName();
    }

    public void parse(String source) {
        this.source = source;
        parse();
    }

    public void parse() {
        tree = new Scope();
        Tokenizer tokenizer = new Tokenizer(source);
        try {
            tree.parse(tokenizer, null);
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public String print() {
        return tree.print(0, false);
    }

    public static ImageIcon getIcon(String name) {
        try {
            return new ImageIcon("./resources/javascript/" + name);
        } catch (Exception e) {
            return null;
        }
    }

    public DefaultTreeModel getTreeModel() {
        DefaultTreeModel model = new DefaultTreeModel(tree.getNode());
        return model;
    }
}
