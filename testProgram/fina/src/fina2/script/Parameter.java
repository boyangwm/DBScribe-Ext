/*
 * Parameter.java
 *
 * Created on December 8, 2001, 9:07 PM
 */

package fina2.script;

/**
 *
 * @author  Administrator
 * @version 
 */
public class Parameter {

    public static final int BANK = 1;
    public static final int PEER_GROUP = 2;
    public static final int PERIOD = 3;
    public static final int NODE = 4;
    public static final int PERIOD_TYPE = 5;
    public static final int NUMBER = 6;
    public static final int MATH_FUNCTION = 7;

    private String name;
    private int type;
    private String value;

    /** Creates new Parameter */
    public Parameter(String name, int type) {
        this.name = name;
        this.type = type;
        value = new String();
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
