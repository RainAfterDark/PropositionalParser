package dark.after.rain.parser.ast;

import java.util.Map;

public record LiteralExpression(char value) implements Expression {
    public boolean isTrue() {
        return switch (value) {
            case '0' -> false;
            case '1' -> true;
            default -> throw new RuntimeException("Unexpected literal: " + value);
        };
    }

    public boolean isFalse() {
        return !isTrue();
    }

    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return isTrue();
    }

    @Override
    public Expression simplify() {
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
