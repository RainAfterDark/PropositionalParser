package com.logic.ast;

import java.util.List;
import java.util.Map;

/**
 * A literal value expression (either true or false, denoted as 1 and 0, respectively)
 *
 * @param value The literal value
 */
public record LiteralExpression(char value) implements Expression {
    public boolean isTrue() {
        return switch (value) {
            case '0' -> false;
            case '1' -> true;
            default -> throw new RuntimeException("Unexpected literal: " + value);
        };
    }

    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return isTrue();
    }

    @Override
    public List<Character> collectVariables() {
        return List.of();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
