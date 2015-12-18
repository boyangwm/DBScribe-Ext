/*
 * EOFException.java
 *
 * Created on 25 Èþëü 2002 ã., 20:15
 */

package fina2.javascript;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class EOFException extends Exception {

    /**
     * Creates new <code>EOFException</code> without detail message.
     */
    public EOFException() {
    }

    /**
     * Constructs an <code>EOFException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public EOFException(String msg) {
        super(msg);
    }
}
