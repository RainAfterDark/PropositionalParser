package dark.after.rain.parser.ast;

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
    public Expression reduce(ReductionStep step) {
        Expression reduced = operand.reduce(step);

        if (operator.type() == TokenType.NOT) {
            // Double Negation Law: ~~p = p
            if (step == ReductionStep.DOUBLE_NEGATION &&
                    reduced instanceof UnaryExpression(Token operator1, Expression operand1)) {
                if (operator1.type() == TokenType.NOT) {
                    Expression r = operand1.reduce(step);
                    System.out.println("Double Negation: " + this + " -> " + r);
                    return r;
                }
            }

            // De Morgan's Law:
            // ~(p & q) = ~p | ~q
            // ~(p | q) = ~p & ~q
            if (step == ReductionStep.DE_MORGAN &&
                    reduced instanceof BlockExpression(Expression inner) &&
                    inner instanceof NaryExpression(Token op, List<Expression> ops) &&
                    (op.type() == TokenType.AND || op.type() == TokenType.OR)) {
                List<Expression> negated = ops.stream()
                        .map(e -> (Expression) new UnaryExpression(operator, e))
                        .toList();
                Expression deMorgan = new NaryExpression(
                        Token.of(op.type() == TokenType.AND ? '|' : '&'), negated);
                System.out.println("De Morgan's: " + this + " -> " + deMorgan);
                return deMorgan;
            }
        }

        return new UnaryExpression(operator, reduced);
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
