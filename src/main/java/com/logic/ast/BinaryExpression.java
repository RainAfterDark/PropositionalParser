package com.logic.ast;

import com.logic.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record BinaryExpression(Expression left, Token operator, Expression right)
        implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        boolean l = left.evaluate(context);
        boolean r = right.evaluate(context);

        return switch (operator.type()) {
            case AND -> l && r;
            case OR -> l || r;
            case IMPLIES -> (!l) || r; // p > q = ~p | q
            case EQUALS -> l == r;
            default -> throw new RuntimeException("Unknown operator: " + operator.value());
        };
    }

    @Override
    public List<Character> collectVariables() {
        List<Character> variables = new ArrayList<>();
        variables.addAll(left.collectVariables());
        variables.addAll(right.collectVariables());
        return variables.stream().distinct().sorted().toList();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operator.value(), right);
    }
}
