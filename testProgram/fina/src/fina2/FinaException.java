/*
 * FinaException.java
 *
 * Created on December 5, 2002, 9:08 PM
 */

package fina2;

/**
 *
 * @author  ZBokuchava
 */
public class FinaException extends java.lang.Exception {
    
    protected int code = -1;
    
    protected String message="General Error";
    
    protected String source="Fina Server";
    
    /**
     * Creates a new instance of <code>FinaException</code> without detail message.
     */
    public FinaException() {
    }
    public FinaException(int code) {
        this.code=code;
        this.message="General Error";
        this.source="Fina Server";
    }
    public FinaException(String message) {
        this.code=-1;
        this.message=message;
        this.source="Fina Server";
    }
    public FinaException(int code, String message) {
        this.code=code;
        this.message=message;
        this.source="Fina Server";
    }
    public FinaException(int code, String message, String source) {
        this.code=code;
        this.message=message;
        this.source=source;
    }
    public FinaException(Exception e) {
        this.message=e.getMessage();
    }
    
    
    /**
     * Constructs an instance of <code>FinaException</code> with the specified detail message.
     * @param msg the detail message.
     */
 //   public FinaException(String msg) {
 //       super(msg);
//    }
    
    public String toString() {
        return "Error: "+source+". "+message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setCode(int code) {
        this.code=code;
    }
    
    public void setMessage(String message) {
        this.message=message;
    }
    
    public void setSource(String source) {
        this.source=source;
    }
    
}
