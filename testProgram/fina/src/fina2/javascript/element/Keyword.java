/*
 * Keyword.java
 *
 * Created on 25 Èþëü 2002 ã., 22:04
 */

package fina2.javascript.element;

import java.util.Iterator;

import fina2.javascript.EOFException;
import fina2.javascript.Token;
import fina2.javascript.Tokenizer;

/**
 *
 * @author  David Shalambreridze
 * @version 
 */
public class Keyword extends Function {

    /** Creates new Keyword */
    public Keyword() {
        this("");
    }

    public Keyword(String name) {
        super(name);
    }

    public String print(int level, boolean dummy) {
        String delimiter = ",";
        if (name.equals("for"))
            delimiter = ";";
        String s = name;
        if (name.equals("var"))
            return s + " ";
        if (!name.equals("return") && !name.equals("else"))
            s += "(";
        else if (name.equals("return"))
            s += " ";

        for (Iterator iter = parameters.children(); iter.hasNext();) {
            Element e = ((Element) iter.next()).getValidElement();
            if ((e instanceof Scope) && (level <= 0))
                s += e.print(1, dummy);
            else
                s += e.print(level, dummy);
            if (iter.hasNext())
                s += delimiter + " ";
        }
        if (!name.equals("return") && !name.equals("else"))
            s += ")";
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Element e = (Element) iter.next();
            //if(e instanceof Scope) {
            //    s += e.print(level+1);
            //} else {
            s += e.print(level, dummy);
            //}
        }
        if (name.equals("return"))
            s += ";\n";
        return s;
    }

    public Token parse(Tokenizer tokenizer, Element parent)
            throws EOFException, fina2.javascript.ParseException {
        Token token = tokenizer.getLastToken();
        startPosition = token.getStartPosition();
        //System.out.println("start "+startPosition);
        endPosition = tokenizer.getCurPosition();
        if (token.getText().toLowerCase().equals("var")
                || token.getText().toLowerCase().equals("else")) {
            if (parent != null) {
                if (token.getText().toLowerCase().equals("else")) {
                    for (Iterator iter = parent.children(); iter.hasNext();) {
                        Element o = (Element) iter.next();
                        if (o.getNodeName().toLowerCase().equals("else"))
                            parent.removeChild((Element) o);
                    }
                }
                parent.addChild(this);
            }
            name = token.getText();
            //endPosition = tokenizer.getCurPosition();
            return tokenizer.nextToken();
        }
        if (token.getText().equals("return")) {
            if (parent != null)
                parent.addChild(this);
            name = token.getText();
            token = tokenizer.nextToken();
            if (token.getType() == Token.BREAK) {
                Equation e = new Equation();
                e.addChild(new Text(""));
                parameters.addChild(e);
                //endPosition = tokenizer.getCurPosition();
                /*if(tokenizer.getPreviousToken() != null)
                    endPosition = tokenizer.getPreviousToken().getStartPosition();
                else */
                endPosition = tokenizer.getCurPosition();
                return token;
            } else {
                Equation e = new Equation();
                token = e.parse(tokenizer, parameters); //parameters);

                try {
                    if (e.getChildAt(e.numberOfChildren() - 1) instanceof Break) {
                        e.removeChildAt(e.numberOfChildren() - 1);
                    }
                } catch (Exception ex) {
                }
                ;
                //endPosition = tokenizer.getCurPosition();
                if (tokenizer.getPreviousToken() != null)
                    endPosition = tokenizer.getPreviousToken()
                            .getStartPosition();
                else
                    endPosition = tokenizer.getCurPosition();
                //System.out.println("end "+endPosition);
                return token;
            }
        } else {
            return super.parse(tokenizer, parent);
        }
    }

    /*public Token parse(Tokenizer tokenizer, Element parent) throws EOFException {
        name = tokenizer.getLastToken().getText();
        tokenizer.nextToken();
        Token token = tokenizer.nextToken();
        while(true) {
            if(token.getType() == Token.RIGHT_BRACKET) {
                token = tokenizer.nextToken();
                break;
            } else {
                if(token.getType() == Token.COMMA) {
                    token = tokenizer.nextToken();
                } else {
                    Equation e = new Equation();
                    token = e.parse(tokenizer, parameters);
                    //parameters.addChild(e);
                    if( (token.getType() != Token.COMMA) && (tokenizer.getPreviousToken().getType() == Token.RIGHT_BRACKET) ) {
                        tokenizer.back();
                        token = tokenizer.getLastToken();
                    }
                }
            }
        }
        if(parent != null)
            parent.addChild(this);
        return token;
    }*/

    protected String getNodeName() {
        return name;
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("keyword.gif");
    }

    public Element getFirstParameter() {
        Element e = null;
        try {
            e = (Element) parameters.getChildAt(0);
        } catch (Exception ex) {
            e = new Equation();
            parameters.addChild(e);
        }
        return e;
    }
}
