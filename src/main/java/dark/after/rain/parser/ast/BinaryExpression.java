package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

import java.util.Map;
import java.util.function.BiFunction;

public record BinaryExpression(Expression left, Token operator, Expression right)
        implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        boolean l = left.evaluate(context);
        boolean r = right.evaluate(context);

        return switch (operator.type()) {
            case AND -> l && r;
            case OR -> l || r;
            case IMPLIES -> (!l) || r; // p > q is equivalent to ~p or q
            case EQUALS -> l == r;
            default -> throw new RuntimeException("Unknown operator: " + operator.value());
        };
    }

    // Helper to apply commutative laws
    private Expression applyBidirectionalLaw(BiFunction<Expression, Expression, Expression> law,
                                             Expression sLeft, Expression sRight) {
        Expression simplified = law.apply(sLeft, sRight);
        if (simplified == null) {
            return law.apply(sRight, sLeft);
        }
        return simplified;
    }

    private Expression applyMutualLaws(Expression sLeft, Expression sRight, Expression sThis) {
        // Idempotent: p &/| p = p
        if (sLeft.equals(sRight)) {
            System.out.println("Idempotent: " + sThis + " -> " + sLeft);
            return sLeft;
        }
        return null;
    }

    // Helper to generate p &/| (q |/& r)
    private Expression applyDistribution(Expression sThis,
                                         Expression left1, Expression right1, Expression right2,
                                         char innerOp, char outerOp) {
        Expression block = new BlockExpression(new BinaryExpression(right1, Token.of(innerOp), right2));
        Expression newExpr = new BinaryExpression(left1, Token.of(outerOp), block);
        System.out.println("Distributive: " + sThis + " -> " + newExpr);
        return newExpr;
    }

    // Helper to apply distributive laws
    private Expression applyDistributions(Expression sThis,
                                          Expression left1, Expression left2,
                                          Expression right1, Expression right2,
                                          char innerOp, char outerOp) {
        if (left1.equals(left2))
            return applyDistribution(sThis, left1, right1, right2, innerOp, outerOp);
        else if (right1.equals(right2))
            return applyDistribution(sThis, right1, left1, left2, innerOp, outerOp);
        else if (left1.equals(right2))
            return applyDistribution(sThis, left1, right1, left2, innerOp, outerOp);
        else if (right1.equals(left2))
            return applyDistribution(sThis, right1, left1, right2, innerOp, outerOp);
        return null;
    }

    @Override
    public Expression simplify() {
        Expression sLeft = left.simplify();
        Expression sRight = right.simplify();
        Expression sThis = new BinaryExpression(sLeft, operator, sRight);
        Expression s; // simplified

        switch (operator.type()) {
            case AND -> {
                if ((s = applyMutualLaws(sLeft, sRight, sThis)) != null) return s;

                // Identity: p & 1 = p
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr2 instanceof LiteralExpression literal && literal.isTrue()) {
                        System.out.println("Identity: " + sThis + " -> " + expr1);
                        return expr1;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Domination: p & 0 = 0
                if ((s = applyBidirectionalLaw((expr1, _) -> {
                    if (expr1 instanceof LiteralExpression literal && literal.isFalse()) {
                        System.out.println("Domination: " + sThis + " -> " + literal);
                        return literal;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Negation: p & ~p = 0
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr1 instanceof UnaryExpression(Token operator1, Expression operand)
                            && operator1.type() == TokenType.NOT
                            && operand.equals(expr2)) {
                        LiteralExpression falseValue = new LiteralExpression('0');
                        System.out.println("Negation: " + sThis + " -> " + falseValue);
                        return falseValue;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Absorption: p & (p | q) = p
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr2 instanceof BlockExpression(Expression inner)
                            && inner instanceof BinaryExpression(Expression left1, Token operator1, Expression right1)
                            && operator1.type() == TokenType.OR && (left1.equals(expr1) || right1.equals(expr1))) {
                        System.out.println("Absorption: " + sThis + " -> " + expr1);
                        return expr1;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Distributive: (p | q) & (p | r) = p | (q & r)
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr1 instanceof BlockExpression(Expression inner1)
                            && expr2 instanceof BlockExpression(Expression inner2)
                            && inner1 instanceof BinaryExpression(Expression left1, Token operator1, Expression right1)
                            && inner2 instanceof BinaryExpression(Expression left2, Token operator2, Expression right2)
                            && operator1.type() == TokenType.OR
                            && operator2.type() == TokenType.OR) {
                        return applyDistributions(sThis, left1, left2, right1, right2, '&', '|');
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Biconditional: (p > q) & (q > p) = (~p | q) & (~q | p) = p = q
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr1 instanceof BlockExpression(Expression inner1)
                            && inner1 instanceof BinaryExpression(Expression left1, Token operator1, Expression right1)
                            && operator1.type() == TokenType.OR
                            && expr2 instanceof BlockExpression(Expression inner2)
                            && inner2 instanceof BinaryExpression(Expression left2, Token operator2, Expression right2)
                            && operator2.type() == TokenType.OR
                            && left1 instanceof UnaryExpression(Token not1, Expression operand1)
                            && left2 instanceof UnaryExpression(Token not2, Expression operand2)
                            && not1.type() == TokenType.NOT && not2.type() == TokenType.NOT
                            && operand1.equals(right2) && operand2.equals(right1)) {
                        Expression newExpr = new BinaryExpression(operand1, Token.of('='), operand2);
                        System.out.println("Biconditional: " + sThis + " -> " + newExpr);
                        return newExpr;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;
            }

            case OR -> {
                if ((s = applyMutualLaws(sLeft, sRight, sThis)) != null) return s;

                // Identity: p | 0 = p
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr2 instanceof LiteralExpression literal && literal.isFalse()) {
                        System.out.println("Identity: " + sThis + " -> " + expr1);
                        return expr1;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Domination: p | 1 = 1
                if ((s = applyBidirectionalLaw((expr1, _) -> {
                    if (expr1 instanceof LiteralExpression literal && literal.isTrue()) {
                        System.out.println("Domination: " + sThis + " -> " + literal);
                        return literal;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Negation: p | ~p = 1
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr1 instanceof UnaryExpression(Token operator1, Expression operand)
                            && operator1.type() == TokenType.NOT
                            && operand.equals(expr2)) {
                        LiteralExpression falseValue = new LiteralExpression('1');
                        System.out.println("Negation: " + sThis + " -> " + falseValue);
                        return falseValue;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Absorption: p | (p & q) = p
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr2 instanceof BlockExpression(Expression inner)
                            && inner instanceof BinaryExpression(Expression left1, Token operator1, Expression right1)
                            && operator1.type() == TokenType.AND && (left1.equals(expr1) || right1.equals(expr1))) {
                        System.out.println("Absorption: " + sThis + " -> " + expr1);
                        return expr1;
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;

                // Distributive: (p & q) | (p & r) = p & (q | r)
                if ((s = applyBidirectionalLaw((expr1, expr2) -> {
                    if (expr1 instanceof BlockExpression(Expression inner1)
                            && expr2 instanceof BlockExpression(Expression inner2)
                            && inner1 instanceof BinaryExpression(Expression left1, Token operator1, Expression right1)
                            && inner2 instanceof BinaryExpression(Expression left2, Token operator2, Expression right2)
                            && operator1.type() == TokenType.AND
                            && operator2.type() == TokenType.AND) {
                        return applyDistributions(sThis, left1, left2, right1, right2, '|', '&');
                    }
                    return null;
                }, sLeft, sRight)) != null) return s;
            }

            case IMPLIES -> {
                // Implication: p > q = ~p | q
                Expression newExpr = new BinaryExpression(
                        new UnaryExpression(Token.of('~'), sLeft), Token.of('|'), sRight);
                System.out.println("Implication: " + sThis + " -> " + newExpr);
                return newExpr;
            }
        }

        return sThis;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operator.value(), right);
    }
}
