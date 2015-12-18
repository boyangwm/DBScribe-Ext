/*
 * Token.java
 *
 * Created on 25 Èþëü 2002 ã., 20:07
 */

package fina2.javascript;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class Token {

    public static final int NAME = 1;
    public static final int NUMBER = 2;
    public static final int OPERATION = 3;
    public static final int ASSIGNMENT = 4;
    public static final int LEFT_BRACKET = 5;
    public static final int RIGHT_BRACKET = 6;
    public static final int LEFT_SCOPE = 7;
    public static final int RIGHT_SCOPE = 8;
    public static final int BREAK = 9;
    public static final int COMMA = 10;
    public static final int STRING = 11;
    public static final int KEYWORD = 12;

    private int type;
    private String text;

    private int startPosition = 0;
    private int endPosition = 0;

    /** Creates new Token */
    public Token(int startPosition, int endPosition, int type, String text) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.type = type;
        this.text = text;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

}
