/*
 * Assignment.java
 *
 * Created on 25 Èþëü 2002 ã., 15:00
 */

package fina2.javascript.element;

import fina2.javascript.EOFException;
import fina2.javascript.Token;
import fina2.javascript.Tokenizer;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class Assignment extends Element {

    private String left;
    private Element parent;
    private String operation;

    /** Creates new Assignment */
    public Assignment() {
        super();
    }

    public Assignment(String left) {
        super();
        this.left = left;
    }

    public String print(int level, boolean dummy) {
        String s = left + " " + operation + " ";
        try {
            s += ((Element) children.get(0)).print(level, dummy);
        } catch (Exception ex) {
        }
        if ((parent != null) && (parent instanceof Scope)) {
            s += ";\n";
        }

        //if(parent == null) {
        //    s += ";\n";
        //}
        return s;
    }

    public Token parse(Tokenizer tokenizer, Element parent)
            throws EOFException, fina2.javascript.ParseException {
        if (parent != null)
            parent.addChild(this);
        this.parent = parent;

        Token token = tokenizer.getLastToken();
        left = token.getText();
        operation = tokenizer.nextToken().getText();
        tokenizer.nextToken();
        Equation e = new Equation();
        token = e.parse(tokenizer, this);
        //addChild(e);

        while (children.size() > 1) {
            children.remove(children.size() - 1);
        }
        return token;
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("assignment.gif");
    }

    protected String getNodeName() {
        return left + operation;
    }
}
