package com.logic.ast;

import com.logic.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An expression with an arbitrary number of operands.
 * A better abstraction for associativity compared to chains of {@link BinaryExpression}.
 *
 * @param operator The expression operator
 * @param operands The list of operands
 */
public record NaryExpression(Token operator, List<Expression> operands) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return switch (operator.type()) {
            case AND -> operands.stream().allMatch(e -> e.evaluate(context));
            case OR -> operands.stream().anyMatch(e -> e.evaluate(context));
            default -> throw new RuntimeException("Unexpected operator: " + operator);
        };
    }

    @Override
    public List<Character> collectVariables() {
        List<Character> variables = new ArrayList<>();
        for (Expression e : operands) {
            variables.addAll(e.collectVariables());
        }
        return variables.stream().distinct().sorted().toList();
    }

    @Override
    public String toString() {
        return String.format("%s",
                String.join(" " + operator.value() + " ",
                        operands.stream().map(Object::toString).toList()));
    }
}
