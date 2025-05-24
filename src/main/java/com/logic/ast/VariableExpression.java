package com.logic.ast;

import java.util.List;
import java.util.Map;

/**
 * A literal expression of a variable. The foundation of the AST.
 *
 * @param name The character name of the variable.
 */
public record VariableExpression(char name) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return context.get(name);
    }

    @Override
    public List<Character> collectVariables() {
        return List.of(name);
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
