/*
 * Equation.java
 *
 * Created on 25 Èþëü 2002 ã., 21:48
 */

package fina2.javascript.element;

import java.util.Iterator;

import fina2.javascript.EOFException;
import fina2.javascript.ParseException;
import fina2.javascript.Token;
import fina2.javascript.Tokenizer;

/**
 *
 * @author  David Shalamberidze
 * @version
 */
public class Equation extends Element {

    Assignment assignment = null;

    /** Creates new Equation */
    public Equation() {
        super();
    }

    public String print(int level, boolean dummy) {
        String s = "";
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Element e = (Element) iter.next();
            s += e.print(level, dummy);
            if (iter.hasNext())
                s += " ";
        }
        return s;
    }

    public Element getValidElement() {
        if (assignment == null)
            return this;
        else
            return assignment;
    }

    public Token parse(Tokenizer tokenizer, Element parent)
            throws EOFException, ParseException {
        if (parent != null)
            parent.addChild(this);

        assignment = null;
        children.removeAllElements();

        Token token = tokenizer.getLastToken();
        while (true) {
            /*if(token.getType() == Token.RIGHT_SCOPE) {
                System.out.println("_RSSSS");
                return token;
            }*/
            if (token.getType() == Token.LEFT_BRACKET) {
                token = parseBracket(tokenizer, parent);
            } else {
                if (token.getType() == Token.NAME) {
                    Token lt = tokenizer.getPreviousToken();
                    Token nt = null;
                    try {
                        nt = tokenizer.nextToken();
                        if (nt.getType() == Token.LEFT_BRACKET) {
                            if (lt != null) {
                                if ((lt.getType() != Token.OPERATION)
                                        && (lt.getType() != Token.LEFT_BRACKET)
                                        && (lt.getType() != Token.ASSIGNMENT)
                                        && (lt.getType() != Token.KEYWORD)) {
                                    tokenizer.back();
                                    token = tokenizer.getLastToken();
                                    break;
                                }
                            }
                            tokenizer.back();
                            Function f = new Function();
                            token = f.parse(tokenizer, this);
                            //addChild(f);
                        } else {
                            if (nt.getType() == Token.ASSIGNMENT) {
                                tokenizer.back();
                                token = tokenizer.getLastToken();

                                if (parent != null)
                                    parent.removeChild(this);
                                assignment = new Assignment();
                                token = assignment.parse(tokenizer, parent);
                                return token;
                                //break;
                            } else {
                                Text t = new Text(token.getText());
                                addChild(t);
                                token = nt;
                                /*if(nt.getType() == nt.RIGHT_BRACKET)
                                    return nt;*/
                            }
                        }
                    } catch (EOFException e) {
                        if (nt == null) {
                            Text t = new Text(token.getText());
                            addChild(t);
                        }
                        throw e;
                    }
                } else {
                    if ((token.getType() == Token.NUMBER)
                            || (token.getType() == Token.STRING)) {
                        String s = null;
                        if (token.getType() == Token.STRING) {
                            s = "\"" + token.getText() + "\"";
                        } else {
                            s = token.getText();
                        }
                        Text t = new Text(s);
                        addChild(t);
                        token = tokenizer.nextToken();
                    } else {
                        if ((token.getType() == Token.OPERATION)
                                || (token.getType() == Token.ASSIGNMENT)) {
                            Operation o = new Operation(token.getText());
                            if (token.getType() == Token.ASSIGNMENT)
                                o = new Operation("==");
                            addChild(o);
                            token = tokenizer.nextToken();
                        } else {
                            if (token.getType() == Token.RIGHT_BRACKET) {
                                break;
                            } else {
                                if (token.getType() == Token.RIGHT_SCOPE) {
                                    break;
                                } else {
                                    if (token.getType() == Token.BREAK) {
                                        break;
                                    } else {
                                        if (token.getType() == Token.KEYWORD) {
                                            throw new ParseException();
                                            //break;
                                        } else {
                                            if (token.getType() == Token.COMMA) {
                                                break;
                                            } else {
                                                throw new ParseException();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return token;
    }

    private Token parseBracket(Tokenizer tokenizer, Element parent)
            throws EOFException, ParseException {
        Operation o = new Operation("(");
        addChild(o);

        tokenizer.nextToken();

        Equation equation = new Equation();
        Token token = equation.parse(tokenizer, this);

        token = tokenizer.nextToken();

        o = new Operation(")");
        addChild(o);

        return token;
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("equation.gif");
    }

    protected String getNodeName() {
        return print(0, true);
    }

}
