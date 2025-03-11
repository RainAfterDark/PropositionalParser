package dark.after.rain.parser.ast;

import java.util.Map;

public record VariableExpression(char name) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return context.get(name);
    }

    @Override
    public Expression reduce(ReductionStep step) {
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
