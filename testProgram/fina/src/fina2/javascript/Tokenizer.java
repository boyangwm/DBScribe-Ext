/*
 * Tokenizer.java
 *
 * Created on 25 Èþëü 2002 ã., 19:45
 */

package fina2.javascript;

/**
 *
 * @author  David Shalamberidze
 * @version 
 */
public class Tokenizer {

    private final String whitespace = " \t\n\r";
    private final String delimiters = "+-=!/*(){};&|,<>";
    private final String operations = "+-=!/*&|<>";
    private final String numeric = "0123456789.";
    private final String[] keywords = { "if", "else", "for", "while", "do",
            "break", "continue", "switch", "case", "return", "var" };

    private StringBuffer sb;

    private Token lastToken;
    private Token previousToken;

    private int curPosition;
    private int prevPosition;
    private int startPosition;

    /** Creates new Tokenizer */
    public Tokenizer(String source) {
        sb = new StringBuffer(source);
        curPosition = 0;
        startPosition = 0;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getCurPosition() {
        return curPosition;
    }

    private boolean isOperation(char c) {
        return (operations.indexOf(c) != -1);
    }

    private boolean isDelimiter(char c) {
        return (delimiters.indexOf(c) != -1);
    }

    private boolean isWhitespace(char c) {
        return (whitespace.indexOf(c) != -1);
    }

    private void skipWhitespace() throws IndexOutOfBoundsException {
        while (isWhitespace(sb.charAt(curPosition)))
            curPosition++;
    }

    private boolean isNumeric(char c) {
        return (numeric.indexOf(c) != -1);
    }

    private boolean isKeyword(String s) {
        for (int i = 0; i < keywords.length; i++) {
            if (s.equals(keywords[i]))
                return true;
        }
        return false;
    }

    private boolean isNumeric(StringBuffer s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!isNumeric(c))
                return false;
        }
        return true;
    }

    private StringBuffer token() throws IndexOutOfBoundsException {
        StringBuffer s = new StringBuffer();
        skipWhitespace();

        //startPosition = curPosition;

        char c = sb.charAt(curPosition);
        //System.out.println("STRING check");
        if (c == '"') {
            //System.out.println("STRING start"+s);
            do {
                s.append(c);
                curPosition++;
                if (curPosition == sb.length())
                    break;
                c = sb.charAt(curPosition);
                //System.out.println("STRING = "+s);
            } while ((c != '"') && (c != '\n') && (c != '\r'));

            s.append('"');
            if (curPosition != sb.length()) {
                curPosition++;
                c = sb.charAt(curPosition);
            }
        } else {
            while ((!isWhitespace(c)) && (!isDelimiter(c))) {
                s.append(c);
                curPosition++;
                c = sb.charAt(curPosition);
            }
        }

        return s;
    }

    public Token nextToken() throws EOFException {
        prevPosition = curPosition;
        Token tok = null;
        try {
            skipWhitespace();

            startPosition = curPosition;

            char c = sb.charAt(curPosition);
            if (isDelimiter(c)) {
                if (c == ';') {
                    tok = new Token(startPosition, curPosition, Token.BREAK,
                            ";");
                }
                if (c == '(') {
                    tok = new Token(startPosition, curPosition,
                            Token.LEFT_BRACKET, "(");
                }
                if (c == ')') {
                    tok = new Token(startPosition, curPosition,
                            Token.RIGHT_BRACKET, ")");
                }
                if (c == '{') {
                    tok = new Token(startPosition, curPosition,
                            Token.LEFT_SCOPE, "{");
                }
                if (c == '}') {
                    tok = new Token(startPosition, curPosition,
                            Token.RIGHT_SCOPE, "}");
                }
                if (c == ',') {
                    tok = new Token(startPosition, curPosition, Token.COMMA,
                            ",");
                }
                if (c == '=') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "==");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.ASSIGNMENT, "=");
                    }
                }
                if (c == '+') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.ASSIGNMENT, "+=");
                    } else {
                        if (sb.charAt(curPosition + 1) == '+') {
                            curPosition++;
                            tok = new Token(startPosition, curPosition,
                                    Token.OPERATION, "++");
                        } else {
                            tok = new Token(startPosition, curPosition,
                                    Token.OPERATION, "+");
                        }
                    }
                }
                if (c == '-') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.ASSIGNMENT, "-=");
                    } else {
                        if (sb.charAt(curPosition + 1) == '-') {
                            curPosition++;
                            tok = new Token(startPosition, curPosition,
                                    Token.OPERATION, "--");
                        } else {
                            tok = new Token(startPosition, curPosition,
                                    Token.OPERATION, "-");
                        }
                    }
                }
                if (c == '/') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.ASSIGNMENT, "/=");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "/");
                    }
                }
                if (c == '*') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.ASSIGNMENT, "*=");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "*");
                    }
                }
                if (c == '!') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "!=");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "!");
                    }
                }
                if (c == '|') {
                    if (sb.charAt(curPosition + 1) == '|') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "||");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "|");
                    }
                }
                if (c == '&') {
                    if (sb.charAt(curPosition + 1) == '&') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "&&");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "&");
                    }
                }
                if (c == '>') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, ">=");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, ">");
                    }
                }
                if (c == '<') {
                    if (sb.charAt(curPosition + 1) == '=') {
                        curPosition++;
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "<=");
                    } else {
                        tok = new Token(startPosition, curPosition,
                                Token.OPERATION, "<");
                    }
                }
                if (tok == null) {
                    tok = new Token(startPosition, curPosition,
                            Token.OPERATION, String.valueOf(c));
                }
                curPosition++;
            } else {
                StringBuffer s = token();
                if (isNumeric(s)) {
                    tok = new Token(startPosition, curPosition, Token.NUMBER, s
                            .toString());
                } else {
                    if ((s.length() > 1) && (s.charAt(0) == '"')
                            && (s.charAt(s.length() - 1) == '"')) {
                        //if( (s.charAt(0) == '"') && (s.charAt(s.length()-1) == '"') ) {
                        s.deleteCharAt(0);
                        s.deleteCharAt(s.length() - 1);
                        tok = new Token(startPosition, curPosition,
                                Token.STRING, s.toString());
                    } else {
                        if (isKeyword(s.toString())) {
                            tok = new Token(startPosition, curPosition,
                                    Token.KEYWORD, s.toString());
                        } else {
                            tok = new Token(startPosition, curPosition,
                                    Token.NAME, s.toString());
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            //e.printStackTrace();
            throw new EOFException();
        }
        previousToken = lastToken;
        lastToken = tok;
        return tok;
    }

    public Token getLastToken() {
        return lastToken;
    }

    public Token getPreviousToken() {
        return previousToken;
    }

    public void back() {
        curPosition = prevPosition;
        lastToken = previousToken;
        previousToken = null;
    }
}
