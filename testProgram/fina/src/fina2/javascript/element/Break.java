/*
 * Break.java
 *
 * Created on 25 Èþëü 2002 ã., 22:47
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
public class Break extends Element {

    /** Creates new Break */
    public Break() {
    }

    public String print(int level, boolean dummy) {
        return ";\n";
    }

    public Token parse(Tokenizer tokenizer, Element parent) throws EOFException {
        return null;
    }

    protected String getNodeName() {
        return "";
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return null;
    }

}
