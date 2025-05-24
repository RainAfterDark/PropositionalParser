package com.logic.ast;

import com.logic.lexer.Token;
import com.logic.lexer.TokenType;

import java.util.List;
import java.util.Map;

/**
 * An expression with only one operand. Used for NOT expressions.
 *
 * @param operator The expression operator
 * @param operand  The single operand
 */
public record UnaryExpression(Token operator, Expression operand)
        implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        if (operator.type() == TokenType.NOT) {
            return !operand.evaluate(context);
        }
        throw new RuntimeException("Unknown unary operator: " + operator);
    }

    @Override
    public List<Character> collectVariables() {
        return operand.collectVariables();
    }

    @Override
    public String toString() {
        return String.format("%s%s", operator, operand);
    }
}
