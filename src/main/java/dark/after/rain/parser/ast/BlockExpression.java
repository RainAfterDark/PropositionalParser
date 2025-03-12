package dark.after.rain.parser.ast;

import java.util.Map;
import java.util.Objects;

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
