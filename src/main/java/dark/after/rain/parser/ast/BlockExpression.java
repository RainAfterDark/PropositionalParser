package dark.after.rain.parser.ast;

import java.util.Map;

public record BlockExpression(Expression inner) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return inner.evaluate(context);
    }

    @Override
    public Expression simplify() {
        Expression simplified = inner.simplify();
        if (simplified instanceof VariableExpression ||
                simplified instanceof LiteralExpression)
            return simplified;
        return new BlockExpression(simplified);
    }

    @Override
    public String toString() {
        if (inner instanceof VariableExpression ||
                inner instanceof LiteralExpression)
            return inner.toString();
        return String.format("(%s)", inner);
    }
}
