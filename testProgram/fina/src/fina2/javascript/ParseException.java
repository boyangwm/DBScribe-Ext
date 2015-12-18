/*
 * ParseException.java
 *
 * Created on 28 Èþëü 2002 ã., 13:12
 */

package fina2.javascript;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class ParseException extends Exception {

    /**
     * Creates new <code>ParseException</code> without detail message.
     */
    public ParseException() {
    }

    /**
     * Constructs an <code>ParseException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ParseException(String msg) {
        super(msg);
    }
}
