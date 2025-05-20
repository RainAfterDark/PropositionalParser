package com.logic.lexer;

import java.util.Objects;

public record Token(TokenType type, char value, int index) {
    public static Token of(char value) {
        return new Token(TokenType.valueOf(value), value, -1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Token(TokenType type1, char value1, _))) return false;
        return value() == value1 && type() == type1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type(), value());
    }
}
