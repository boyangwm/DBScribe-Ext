/*
 * Text.java
 *
 * Created on 25 Èþëü 2002 ã., 19:39
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
public class Text extends Element {

    String text;

    /** Creates new Text */
    public Text(String text) {
        super();
        this.text = text;
    }

    public Token parse(Tokenizer tokenizer, Element parent) throws EOFException {
        text = tokenizer.getLastToken().getText();
        return tokenizer.nextToken();
    }

    public String print(int level, boolean dummy) {
        return text;
    }

    public String getNodeName() {
        return text;
    }

    public javax.swing.ImageIcon getNodeIcon() {
        return fina2.javascript.Parser.getIcon("text.gif");
    }
}
