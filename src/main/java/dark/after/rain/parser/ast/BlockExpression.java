package dark.after.rain.parser.ast;

import java.util.Map;

public record BlockExpression(Expression inner) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return inner.evaluate(context);
    }

    @Override
    public Expression reduce(ReductionStep step) {
        Expression reduced = inner.reduce(step);
        if (reduced instanceof VariableExpression ||
                reduced instanceof LiteralExpression)
            return reduced;
        return new BlockExpression(reduced);
    }

    @Override
    public String toString() {
        if (inner instanceof VariableExpression ||
                inner instanceof LiteralExpression)
            return inner.toString();
        return String.format("(%s)", inner);
    }
}
