package com.logic.lexer;

import java.util.regex.Pattern;

/**
 * Types of valid tokens, defined by a regular expression to match a character.
 */
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

    /**
     * Helper method to get a TokenType from a char value.
     *
     * @param value The character to be tokenized
     * @return The token, if the character can be tokenized, otherwise null
     */
    public static TokenType valueOf(char value) {
        for (TokenType tokenType : values()) {
            if (tokenType.regex.matcher(String.valueOf(value)).find()) {
                return tokenType;
            }
        }
        return null;
    }
}
