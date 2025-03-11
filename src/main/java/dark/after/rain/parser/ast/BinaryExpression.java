package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

import java.util.ArrayList;
import java.util.Collections;
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
    public Expression reduce(ReductionStep step) {
        Expression reducedLeft = left.reduce(step);
        Expression reducedRight = right.reduce(step);

        // For associative operators, flatten the expression tree
        if (step == ReductionStep.ASSOCIATIVE &&
                (operator.type() == TokenType.AND || operator.type() == TokenType.OR)) {
            List<Expression> operands = new ArrayList<>();
            operands.addAll(Expression.flatten(reducedLeft, operator.type()));
            operands.addAll(Expression.flatten(reducedRight, operator.type()));
            Expression r = new NaryExpression(operator,
                    Collections.unmodifiableList(operands));
            if (operands.size() > 2) {
                System.out.println("Associative: " + this + " -> " + r);
            }
            return r;
        }

        // Implication: p > q = ~p | q
        if (step == ReductionStep.IMPLICATION && operator.type() == TokenType.IMPLIES) {
            Expression r = new BinaryExpression(
                    new UnaryExpression(Token.of('~'), reducedLeft),
                    Token.of('|'), reducedRight).reduce(step);
            System.out.println("Implication: " + this + " -> " + r);
            return r;
        }

        // Biconditional: p = q = (p > q) & (q > p)
        if (step == ReductionStep.BICONDITIONAL && operator.type() == TokenType.EQUALS) {
            Expression leftImplication = new BinaryExpression(reducedLeft,
                    Token.of('>'), reducedRight).reduce(step);
            Expression rightImplication = new BinaryExpression(reducedRight,
                    Token.of('>'), reducedLeft).reduce(step);
            Expression r = new BinaryExpression(leftImplication,
                    Token.of('&'), rightImplication).reduce(step);
            System.out.println("Biconditional: " + this + " -> " + r);
            return r;
        }

        return new BinaryExpression(reducedLeft, operator, reducedRight);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operator.value(), right);
    }
}
