package com.logic.lexer;

import com.logic.input.InputString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Converts a string input to a valid list of tokens to be used by an implementation of {@link com.logic.parser.Parser}.
 */
public class Tokenizer extends InputString {
    private int pos = 0;
    private char currentChar;

    /**
     * Create a Tokenizer from a string input.
     *
     * @param input The string input
     */
    public Tokenizer(String input) {
        super(input);
        this.currentChar = !this.input.isEmpty() ?
                this.input.charAt(0)
                : '\0'; // EOF
    }

    /**
     * Advance the Tokenizer by a single character. Sets the currentChar to EOF at the end of the input string.
     */
    private void advance() {
        pos++;
        if (pos < input.length()) {
            currentChar = input.charAt(pos);
            return;
        }
        currentChar = '\0'; // EOF
    }

    /**
     * Checks if the generated tokens have a balanced number of parentheses.
     * Throws a runtime error if unbalanced.
     *
     * @param tokens The list of tokens to be checked
     */
    private void checkParentheses(List<Token> tokens) {
        int balance = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type() == TokenType.L_BLOCK) balance++;
            else if (token.type() == TokenType.R_BLOCK) balance--;
            if (balance < 0) throw error("Invalid parentheses!", i);
        }
        if (balance != 0) throw error("Unclosed parentheses!", pos);
    }

    /**
     * Generates a list of tokens from the input string.
     * Throws a runtime error for invalid tokens.
     *
     * @return A list of tokens
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            TokenType type = TokenType.valueOf(currentChar);
            if (type == null) throw error("Invalid character encountered!", pos);
            tokens.add(new Token(type, currentChar, pos));
            if (type == TokenType.EOF) break;
            advance();
        }

        checkParentheses(tokens);
        return Collections.unmodifiableList(tokens);
    }
}

