package com.logic.lexer;

import com.logic.input.InputString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tokenizer extends InputString {
    private int pos = 0;
    private char currentChar;

    public Tokenizer(String input) {
        super(input);
        this.currentChar = !this.input.isEmpty() ?
                this.input.charAt(0)
                : '\0'; // EOF
    }

    private void advance() {
        pos++;
        if (pos < input.length()) {
            currentChar = input.charAt(pos);
            return;
        }
        currentChar = '\0'; // EOF
    }

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

    public void checkParentheses(List<Token> tokens) {
        int balance = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.type() == TokenType.L_BLOCK) balance++;
            else if (token.type() == TokenType.R_BLOCK) balance--;
            if (balance < 0) throw error("Invalid parentheses!", i);
        }
        if (balance != 0) throw error("Unclosed parentheses!", pos);
    }
}

