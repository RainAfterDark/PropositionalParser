package dark.after.rain.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

import java.util.List;
import java.util.Map;

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
