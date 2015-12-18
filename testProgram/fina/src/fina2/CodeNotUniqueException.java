/*
 * CodeNotUniqueException.java
 *
 * Created on October 15, 2001, 3:59 PM
 */

package fina2;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class CodeNotUniqueException extends fina2.FinaException{

    /**
     * Creates new <code>CodeNotUniqueException</code> without detail message.
     */
    public CodeNotUniqueException() {
    }


    /**
     * Constructs an <code>CodeNotUniqueException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CodeNotUniqueException(String msg) {
        super(msg);
    }
}


