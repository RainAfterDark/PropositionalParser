package dark.after.rain.parser.ast;

import java.util.Map;

public record BlockExpression(Expression inner) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return inner.evaluate(context);
    }

    @Override
    public String toString() {
        return String.format("(%s)", inner);
    }
}
