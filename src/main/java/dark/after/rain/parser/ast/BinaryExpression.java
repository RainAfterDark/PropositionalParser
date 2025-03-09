package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;

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
            case IMPLIES ->
                // p > q is equivalent to ~p or q
                    (!l) || r;
            case EQUALS -> l == r;
            default -> throw new RuntimeException("Unknown operator: " + operator.value());
        };
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", left, operator.value(), right);
    }
}
