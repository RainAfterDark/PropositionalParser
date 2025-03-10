package dark.after.rain.lexer;

import java.util.regex.Pattern;

public enum TokenType {
    NOT("~"),
    AND("&"),
    OR("\\|"),
    IMPLIES(">"),
    EQUALS("="),
    L_BLOCK("\\("),
    R_BLOCK("\\)"),
    EOF("\0"),
    VARIABLE("[A-Za-z]"),
    LITERAL("[01]"),
    SPACE("\\s");

    private final Pattern regex;

    TokenType(String regex) {
        this.regex = Pattern.compile(regex);
    }

    public static TokenType valueOf(char value) {
        for (TokenType tokenType : values()) {
            if (tokenType.regex.matcher(String.valueOf(value)).find()) {
                return tokenType;
            }
        }
        return null;
    }
}
