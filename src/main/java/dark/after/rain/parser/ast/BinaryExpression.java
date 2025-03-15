package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record BinaryExpression(Expression left, Token operator, Expression right)
        implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        boolean l = left.evaluate(context);
        boolean r = right.evaluate(context);

        return switch (operator.type()) {
            case AND -> l && r;
            case OR -> l || r;
            case IMPLIES -> (!l) || r; // p > q = ~p | q
            case EQUALS -> l == r;
            default -> throw new RuntimeException("Unknown operator: " + operator.value());
        };
    }

    @Override
    public Expression reduce(ReductionStep step) {
        Expression reducedLeft = left.reduce(step);
        Expression reducedRight = right.reduce(step);

        // For associative operators, flatten the expression tree
        if (step == ReductionStep.ASSOCIATIVE &&
                (operator.type() == TokenType.AND || operator.type() == TokenType.OR)) {
            List<Expression> operands = new ArrayList<>();
            operands.addAll(Expression.flatten(reducedLeft, operator.type()));
            operands.addAll(Expression.flatten(reducedRight, operator.type()));
            Expression r = new NaryExpression(operator,
                    Collections.unmodifiableList(operands));
            if (operands.size() > 2) {
                System.out.println("Associative: " + this + " -> " + r);
            }
            return r;
        }

        // Implication: p > q = ~p | q
        if (step == ReductionStep.IMPLICATION && operator.type() == TokenType.IMPLIES) {
            Expression r = new BinaryExpression(
                    new UnaryExpression(Token.of('~'), reducedLeft),
                    Token.of('|'), reducedRight);
            System.out.println("Implication: " + this + " -> " + r);
            return r;
        }

        // Biconditional: p = q = (p > q) & (q > p)
        if (step == ReductionStep.BICONDITIONAL && operator.type() == TokenType.EQUALS) {
            if (reducedLeft.equals(reducedRight)) {
                Expression r = new LiteralExpression('1');
                System.out.println("Equality: " + this + " -> " + r);
                return r;
            }
            // (p > q) = (q > p) = p = q
            if (reducedLeft instanceof BlockExpression(Expression inner) &&
                    inner instanceof BinaryExpression(Expression left1, Token op1, Expression right1) &&
                    op1.type() == TokenType.IMPLIES &&
                    reducedRight instanceof BlockExpression(Expression inner1) &&
                    inner1 instanceof BinaryExpression(Expression left2, Token op2, Expression right2) &&
                    op2.type() == TokenType.IMPLIES &&
                    left1.equals(right2) && right1.equals(left2)) {
                Expression r = new BinaryExpression(left1, Token.of('='), right1);
                System.out.println("Biconditional: " + this + " -> " + r);
                return r;
            }

            Expression leftImpl = new BlockExpression(new BinaryExpression(reducedLeft,
                    Token.of('>'), reducedRight));
            Expression rightImpl = new BlockExpression(new BinaryExpression(reducedRight,
                    Token.of('>'), reducedLeft));
            Expression r = new BinaryExpression(leftImpl,
                    Token.of('&'), rightImpl);
            System.out.println("Biconditional: " + this + " -> " + r);
            return r;
        }

        return new BinaryExpression(reducedLeft, operator, reducedRight);
    }

    @Override
    public List<Character> collectVariables() {
        List<Character> variables = new ArrayList<>();
        variables.addAll(left.collectVariables());
        variables.addAll(right.collectVariables());
        return variables.stream().distinct().sorted().toList();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operator.value(), right);
    }
}
