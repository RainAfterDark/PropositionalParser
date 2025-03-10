package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

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
    public Expression simplify() {
        Expression simplified = operand.simplify();

        // De Morgan's Law: ~(p & q) = ~p | ~q
        if (simplified instanceof BlockExpression(Expression inner) &&
                inner instanceof BinaryExpression(Expression left, Token operator1, Expression right)) {
            if (operator1.type() == TokenType.AND && operator.type() == TokenType.NOT) {
                Expression deMorgan = new BinaryExpression(
                        new UnaryExpression(operator, left).simplify(),
                        Token.of('|'),
                        new UnaryExpression(operator, right).simplify());
                System.out.println("De Morgan's: " + this + " -> " + deMorgan);
                return deMorgan;
            }
        }

        // De Morgan's Law: ~(p | q) = ~p & ~q
        if (simplified instanceof BlockExpression(Expression inner) &&
                inner instanceof BinaryExpression(Expression left, Token operator1, Expression right)) {
            if (operator1.type() == TokenType.OR && operator.type() == TokenType.NOT) {
                Expression deMorgan = new BinaryExpression(
                        new UnaryExpression(operator, left).simplify(),
                        Token.of('&'),
                        new UnaryExpression(operator, right).simplify());
                System.out.println("De Morgan's: " + this + " -> " + deMorgan);
                return deMorgan;
            }
        }

        // Double Negation Law: ~~p = p
        if (simplified instanceof UnaryExpression(Token operator1, Expression operand1)) {
            if (operator1.type() == TokenType.NOT) {
                Expression doubleNeg = operand1.simplify();
                System.out.println("Double Negation: " + this + " -> " + doubleNeg);
                return doubleNeg;
            }
        }

        return new UnaryExpression(operator, simplified);
    }

    @Override
    public String toString() {
        return String.format("%s%s", operator, operand);
    }
}
