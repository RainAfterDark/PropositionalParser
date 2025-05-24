package com.logic.ast;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * An expression wrapped in parentheses.
 *
 * @param inner The inner expression
 */
public record BlockExpression(Expression inner) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return inner.evaluate(context);
    }

    @Override
    public List<Character> collectVariables() {
        return inner.collectVariables();
    }

    @Override
    public String toString() {
        if (inner instanceof VariableExpression ||
                inner instanceof LiteralExpression)
            return inner.toString();
        return String.format("(%s)", inner);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlockExpression(Expression inner1)) {
            return Objects.equals(inner(), inner1);
        }
        if (o instanceof Expression e) {
            return Objects.equals(inner(), e);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner());
    }
}
