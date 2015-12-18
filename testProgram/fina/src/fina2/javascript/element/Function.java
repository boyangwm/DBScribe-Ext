/*
 * Function.java
 *
 * Created on 25 Èþëü 2002 ã., 15:01
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
public class Function extends Element {

    protected Element parameters;
    protected String name;

    /** Creates new Function */
    public Function() {
        this("");
    }

    public Function(String name) {
        super();
        this.name = name;

        parameters = new Element() {

            public Token parse(Tokenizer tokenizer, Element parent)
                    throws EOFException, fina2.javascript.ParseException {

                Token token = tokenizer.getLastToken();
                while (true) {
                    Token prevToken = token;
                    if (token.getType() == Token.RIGHT_BRACKET) {
                        Function.this.endPosition = token.getEndPosition();
                        break;
                    } else if ((token.getType() == Token.COMMA)
                            || (token.getType() == Token.BREAK)) {

                        token = tokenizer.nextToken();
                    } else if (token.getType() == Token.NAME) {
                        Token nt = tokenizer.nextToken();
                        tokenizer.back();

                        if (nt.getType() == Token.ASSIGNMENT) {
                            Assignment a = new Assignment();
                            token = a.parse(tokenizer, this);
                        } else {
                            Equation e = new Equation();
                            token = e.parse(tokenizer, this);
                        }
                    } else {
                        Equation e = new Equation();
                        token = e.parse(tokenizer, this);
                    }

                    if (token == prevToken)
                        throw new fina2.javascript.ParseException();
                }
                return token;
            }

            public String print(int level, boolean dummy) {
                String s = "";
                for (Iterator iter = children(); iter.hasNext();) {
                    Element e = ((Element) iter.next()).getValidElement();
                    s += e.print(level, dummy);
                    if (iter.hasNext())
                        s += ", ";
                }
                return s;
            }

            protected String getNodeName() {
                return "()";
            }

            public javax.swing.ImageIcon getNodeIcon() {
                return fina2.javascript.Parser.getIcon("parameters.gif");
            }
        };
    }

    public String getName() {
        return name;
    }

    public void removeChildren() {
        super.removeChildren();
        parameters.removeChildren();
    }

    public Element getParameters() {
        return parameters;
    }

    public String print(int level, boolean dummy) {
        String s = null;
        if (dummy)
            s = fina2.javascript.Parser.findDummy(this);
        else
            s = fina2.javascript.Parser.findNonDummy(this);
        //s = name;
        s += "(";
        for (Iterator iter = parameters.children(); iter.hasNext();) {
            Element e = ((Element) iter.next()).getValidElement();
            s += e.print(level, dummy);
            if (iter.hasNext())
                s += ", ";
        }
        s += ")";
        return s;
    }

    public Token parse(Tokenizer tokenizer, Element parent)
            throws EOFException, fina2.javascript.ParseException {
        if (parent != null)
            parent.addChild(this);

        startPosition = tokenizer.getLastToken().getStartPosition();
        endPosition = 0;

        name = tokenizer.getLastToken().getText();
        Token token = tokenizer.nextToken();

        token = tokenizer.nextToken();

        try {
            token = parameters.parse(tokenizer, parent);
        } finally {

            if (endPosition == 0) {
                if (tokenizer.getPreviousToken() != null) {
                    endPosition = tokenizer.getPreviousToken().getEndPosition();
                } else {
                    endPosition = tokenizer.getCurPosition();
                }
            }

            token = tokenizer.nextToken();
        }
        return token;
    }

    protected String getNodeName() {
        return fina2.javascript.Parser.findDummy(this);
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("function.gif");
    }
}
