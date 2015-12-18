/*
 * FinaException.java
 *
 * Created on 1 январь 2004 г., 17:02
 */

package fina2.web;

/**
 *
 * @author  zuka
 */
public class FinaException extends javax.servlet.jsp.JspException{
//fina2.FinaException {
    protected int code = -1;
    
    public static final String INITIAL_MESSAGE="General Error";
    
    protected String message=INITIAL_MESSAGE;
    protected String fullMessage=INITIAL_MESSAGE;
    
    protected String source="Fina Web Server";
    
    public static final int LEVEL_ERROR=0;
    public static final int LEVEL_WARNING=1;
    public static final int LEVEL_MESSAGE=2;
    
    public static final String STR_LEVEL_ERROR="fina2.web.error";
    public static final String STR_LEVEL_WARNING="fina2.web.warning";
    public static final String STR_LEVEL_MESSAGE="fina2.web.message";

    private int level=LEVEL_ERROR;
    private String strLevel=STR_LEVEL_ERROR;
    
    /** Creates a new instance of FinaException */
    public FinaException() {
        source="Fina Web Server";
    }
    public FinaException(Exception e) {
        super(e);
        this.fullMessage=e.getMessage();
    }
    public FinaException(int code) {
        this.code=code;
        this.message="General Error";
        this.source="Fina Web Server";
    }
    public FinaException(String message) {
        this.code=-1;
        this.message=message;
        this.source="Fina Web Server";
    }
    public FinaException(int code, String message) {
        this.code=code;
        this.message=message;
        this.source="Fina Web Server";
    }
    public FinaException(int code, String message, String source) {
        this.code=code;
        this.message=message;
        this.source=source;
    }
    
    
    /**
     * Constructs an instance of <code>FinaException</code> with the specified detail message.
     * @param msg the detail message.
     */
 //   public FinaException(String msg) {
 //       super(msg);
//    }
    
    /** Getter for property level.
     * @return Value of property level.
     *
     */
    public int getLevel() {
        return level;
    }
    
    /** Setter for property level.
     * @param level New value of property level.
     *
     */
    public void setLevel(int level) {
        this.level = level;
    }
    
    /** Getter for property strLevel.
     * @return Value of property strLevel.
     *
     */
    public java.lang.String getStrLevel() {
        switch(level){
            case LEVEL_ERROR:
                return STR_LEVEL_ERROR;
            case LEVEL_WARNING:
                return STR_LEVEL_WARNING;
            case LEVEL_MESSAGE:
                return STR_LEVEL_MESSAGE;
            default :
                return STR_LEVEL_ERROR;
        }
    }
    
    /** Setter for property strLevel.
     * @param strLevel New value of property strLevel.
     *
     */
    private void setStrLevel(java.lang.String strLevel) {
        this.strLevel = strLevel;
    }
    public String getDefStrLevel(){
        switch(level){
            case LEVEL_ERROR:
                return "Error";
            case LEVEL_WARNING:
                return "Warning";
            case LEVEL_MESSAGE:
                return "Message";
            default:
                return "Error";
        }
    }
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
    
    /** Getter for property fullMessage.
     * @return Value of property fullMessage.
     *
     */
    public java.lang.String getFullMessage() {
        return fullMessage;
    }
    
    /** Setter for property fullMessage.
     * @param fullMessage New value of property fullMessage.
     *
     */
    public void setFullMessage(java.lang.String fullMessage) {
        this.fullMessage = fullMessage;
    }
}
