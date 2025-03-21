package dark.after.rain.ast;

import java.util.List;
import java.util.Map;

public record VariableExpression(char name) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return context.get(name);
    }

    @Override
    public List<Character> collectVariables() {
        return List.of(name);
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }
}
