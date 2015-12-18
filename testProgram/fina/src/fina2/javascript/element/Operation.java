/*
 * Operation.java
 *
 * Created on 25 Èþëü 2002 ã., 21:45
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
public class Operation extends Element {

    private String operation;

    /** Creates new Operation */
    public Operation(String operation) {
        super();
        this.operation = operation;
    }

    public String print(int level, boolean dummy) {
        return operation;
    }

    public Token parse(Tokenizer tokenizer, Element parent) throws EOFException {
        return tokenizer.nextToken();
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("operation.gif");
    }

    protected String getNodeName() {
        return operation;
    }
}
