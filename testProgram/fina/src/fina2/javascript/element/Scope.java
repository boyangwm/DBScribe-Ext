/*
 * Scope.java
 *
 * Created on 25 Èþëü 2002 ã., 19:31
 */

package fina2.javascript.element;

import java.util.Iterator;

import fina2.javascript.EOFException;
import fina2.javascript.Token;
import fina2.javascript.Tokenizer;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public class Scope extends Element {

    private Element parent;

    /** Creates new Scope */
    public Scope() {
        super();
    }

    public String print(int level, boolean dummy) {
        String s = "";

        if (level == -1) {
            s += "{\n";
            level = 1;
        } else {
            if (level > 0)
                s += " {\n";
        }
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Element e = (Element) iter.next();
            if (!(e instanceof Break) && !(e instanceof Scope)) {
                for (int i = 0; i < level; i++) {
                    s += "    ";
                }
            }
            s += e.print(level + 1, dummy);
        }
        if (level > 0) {
            for (int i = 0; i < level - 1; i++) {
                s += "    ";
            }
            s += "}";
            try {
                if (parent.numberOfChildren() == 1) {
                    s += "\n";
                } else {
                    s += " ";
                }
            } catch (Exception ex) {
                s += "\n";
            }
        }
        return s;
    }

    public Token parse(Tokenizer tokenizer, Element parent)
            throws EOFException, fina2.javascript.ParseException {
        if (parent != null) {
            for (Iterator iter = parent.children(); iter.hasNext();) {
                Object o = iter.next();
                if (o instanceof Scope)
                    parent.removeChild((Element) o);
            }
            parent.addChild(this);
        }

        this.parent = parent;

        Keyword k = null;
        Token token = tokenizer.nextToken();
        while (true) {
            if (token.getType() == Token.NAME) {
                Token nt = tokenizer.nextToken();
                tokenizer.back();
                if (nt.getType() == Token.ASSIGNMENT) {
                    Assignment a = new Assignment();
                    token = a.parse(tokenizer, this);
                } else {
                    if (nt.getType() == Token.OPERATION) {
                        Equation e = new Equation();
                        e.addChild(new Text(token.getText()));
                        token = tokenizer.nextToken();
                        e.addChild(new Operation(token.getText()));
                        e.addChild(new Break());
                        addChild(e);
                        token = tokenizer.nextToken();
                    } else {
                        Function f = new Function();
                        token = f.parse(tokenizer, this);
                        Break b = new Break();
                        addChild(b);
                    }
                }
            } else {
                if (token.getType() == Token.KEYWORD) {
                    if (token.getText().equals("else")) {
                        Keyword kk = new Keyword();
                        token = kk.parse(tokenizer, k);
                        k = kk;
                    } else {
                        k = new Keyword();
                        token = k.parse(tokenizer, this);
                    }
                } else {
                    if (token.getType() == Token.LEFT_SCOPE) {
                        Scope s = new Scope();
                        token = s.parse(tokenizer, k); //(Element)children.get(children.size()-2));
                    } else {
                        if (token.getType() == Token.RIGHT_SCOPE) {
                            //System.out.println("RIGHT_SCOPE "+k.getName());
                            token = tokenizer.nextToken();
                            break;
                        }
                        token = tokenizer.nextToken();
                    }
                }
            }
        }

        return token;
    }

    protected String getNodeName() {
        return "{}";
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("scope.gif");
    }

}
