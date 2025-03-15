package dark.after.rain.parser.ast;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;

import java.util.*;
import java.util.stream.Stream;

public record NaryExpression(Token operator, List<Expression> operands) implements Expression {
    @Override
    public boolean evaluate(Map<Character, Boolean> context) {
        return switch (operator.type()) {
            case AND -> operands.stream().allMatch(e -> e.evaluate(context));
            case OR -> operands.stream().anyMatch(e -> e.evaluate(context));
            default -> throw new RuntimeException("Unexpected operator: " + operator);
        };
    }

    // Identity:
    // p & 1 = p
    // p | 0 = p
    private Expression reduceIdentity(Expression rNary, List<Expression> rOps) {
         char identity = operator.type() == TokenType.AND ? '1' : '0';
        if (rOps.contains(new LiteralExpression(identity))) {
            Expression r = new NaryExpression(operator, rOps.stream()
                    .filter(e -> !e.equals(new LiteralExpression(identity)))
                    .toList());
            System.out.println("Identity: " + rNary + " -> " + r);
            return r;
        }
        return null;
    }

    // Domination:
    // p & 0 = 0
    // p | 1 = 1
    private Expression reduceDomination(Expression rNary, List<Expression> rOps) {
        char dominator = operator.type() == TokenType.AND ? '0' : '1';
        if (rOps.contains(new LiteralExpression(dominator))) {
            Expression r = new LiteralExpression(dominator);
            System.out.println("Domination: " + rNary + " -> " + r);
            return r;
        }
        return null;
    }

    // Negation:
    // p & ~p = 0
    // p | ~p = 1
    private Expression reduceNegation(Expression rNary, List<Expression> rOps) {
        char value = operator.type() == TokenType.AND ? '0' : '1';
        for (Expression e : rOps) {
            for (Expression f : rOps) {
                if (e.equals(f)) continue;
                if (f instanceof UnaryExpression(Token op, Expression operand)
                        && op.type() == TokenType.NOT && operand.equals(e)) {
                    List<Expression> negated = new ArrayList<>(rOps);
                    negated.remove(e);
                    negated.remove(f);
                    negated.add(new LiteralExpression(value));
                    Expression r = new NaryExpression(operator,
                            Collections.unmodifiableList(negated));
                    System.out.println("Negation: " + rNary + " -> " + r);
                    return r;
                }
            }
        }
        return null;
    }

    // Absorption:
    // p & (p | q) = p
    // p | (p & q) = p
    private Expression reduceAbsorption(Expression rNary, List<Expression> rOps) {
        TokenType innerOp = operator.type() == TokenType.AND ? TokenType.OR : TokenType.AND;
        for (Expression e : rOps) {
            for (Expression f : rOps) {
                if (e.equals(f)) continue;
                if (f instanceof BlockExpression(Expression inner) &&
                        inner instanceof NaryExpression(Token op, List<Expression> ops)
                        && op.type() == innerOp && ops.contains(e)) {
                    List<Expression> unAbsorbed = new ArrayList<>(rOps);
                    unAbsorbed.remove(f);
                    Expression r = new NaryExpression(operator,
                            Collections.unmodifiableList(unAbsorbed));
                    System.out.println("Absorption: " + rNary + " -> " + r);
                    return r;
                }
            }
        }
        return null;
    }

    // Distribution:
    // (p | q) & (p | r) = p | (q & r)
    // (p & q) | (p & r) = p & (q | r)
    private Expression reduceDistribution(Expression rNary, List<Expression> rOps) {
        char outerOp = operator.type() == TokenType.AND ? '&' : '|';
        List<Expression> consumed = new ArrayList<>();
        Expression distribution = null;

        for (Expression e : rOps) {
            for (Expression f : rOps) {
                if (e.equals(f)) continue;
                if (e instanceof BlockExpression(Expression inner1) &&
                        inner1 instanceof NaryExpression(Token op1, List<Expression> ops1) &&
                        f instanceof BlockExpression(Expression inner2) &&
                        inner2 instanceof NaryExpression(Token op2, List<Expression> ops2) &&
                        op1.type() == op2.type()) {
                    Expression distributed = null;
                    for (Expression g : ops1) {
                        for (Expression h : ops2) {
                            if (g.equals(h)) {
                                distributed = g;
                                consumed.add(e);
                                consumed.add(f);
                                break;
                            }
                        }
                        if (distributed != null) break;
                    }
                    if (distributed == null) continue;
                    Expression finalDistributed = distributed;
                    List<Expression> onto = Stream.concat(ops1.stream(), ops2.stream())
                            .filter(x -> !x.equals(finalDistributed)).toList();
                   distribution = new NaryExpression(Token.of(outerOp),
                            List.of(distributed, new BlockExpression(new NaryExpression(op1, onto))));
                   break;
                }
            }
            if (distribution != null) break;
        }

        if (distribution != null) {
            List<Expression> undistributed = new ArrayList<>(rOps);
            undistributed.removeAll(consumed);
            if (undistributed.isEmpty()) return distribution;
            undistributed.add(distribution);
            Expression r = new NaryExpression(operator, undistributed);
            System.out.println("Distribution: " + rNary + " -> " + r);
            return r;
        }
        return null;
    }

    private Expression extractNotExpression(List<Expression> ops) {
        for (Expression expr : ops) {
            if (expr instanceof UnaryExpression(Token not, Expression operand) && not.type() == TokenType.NOT) {
                return operand;
            }
        }
        return null;
    }

    private Expression extractImplExpression(List<Expression> ops, Expression notExpr) {
        for (Expression expr : ops) {
            if (!expr.equals(new UnaryExpression(Token.of('~'), notExpr))) {
                return expr;
            }
        }
        return null;
    }

    // Biconditional:
    // (~p | q) & (~q | p) = p = q
    private Expression reduceBiconditional(Expression rNary, List<Expression> rOps) {
        if (rOps.size() == 2 && operator.type() == TokenType.AND) {
            Expression left = rOps.get(0);
            Expression right = rOps.get(1);

            if (left instanceof BlockExpression(Expression inner1) &&
                    inner1 instanceof NaryExpression(Token op1, List<Expression> ops1) &&
                    right instanceof BlockExpression(Expression inner2) &&
                    inner2 instanceof NaryExpression(Token op2, List<Expression> ops2) &&
                    op1.type() == TokenType.OR && op2.type() == TokenType.OR) {

                if (ops1.size() == 2 && ops2.size() == 2) {
                    Expression notLeft = extractNotExpression(ops1);
                    Expression implLeft = extractImplExpression(ops1, notLeft);
                    if (notLeft == null || implLeft == null) return null;

                    Expression notRight = extractNotExpression(ops2);
                    Expression implRight = extractImplExpression(ops2, notRight);
                    if (notRight == null || implRight == null) return null;

                    if (notLeft.equals(implRight) && notRight.equals(implLeft)) {
                        Expression r = new BinaryExpression(notLeft, Token.of('='), notRight);
                        System.out.println("Biconditional: " + rNary + " -> " + r);
                        return r;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Expression reduce(ReductionStep step) {
        List<Expression> rOps = new ArrayList<>();
        for (Expression e : operands) {
            rOps.addAll(Expression.flatten(e.reduce(step), operator.type()));
        }
        rOps = rOps.stream()
                .distinct()
                .sorted(Comparator.comparing(e -> e.toString()
                        .replaceAll("[^a-z]", "")))
                .toList();

        Expression rNary = new NaryExpression(operator, rOps);

        if (rOps.size() < operands.size())
            System.out.println("Idempotent: " + this + " -> " + rNary);

        if (rOps.size() == 1)
            return rOps.getFirst();

        if (operator.type() == TokenType.AND || operator.type() == TokenType.OR) {
            Expression reduced = switch (step) {
                case IDENTITY -> reduceIdentity(rNary, rOps);
                case DOMINATION -> reduceDomination(rNary, rOps);
                case NEGATION -> reduceNegation(rNary, rOps);
                case ABSORPTION -> reduceAbsorption(rNary, rOps);
                case DISTRIBUTIVE -> reduceDistribution(rNary, rOps);
                case BICONDITIONAL -> reduceBiconditional(rNary, rOps);
                default -> null;
            };
            if (reduced != null) return reduced;
        }

        return rNary;
    }

    @Override
    public List<Character> collectVariables() {
        List<Character> variables = new ArrayList<>();
        for (Expression e : operands) {
            variables.addAll(e.collectVariables());
        }
        return variables.stream().distinct().sorted().toList();
    }

    @Override
    public String toString() {
        return String.format("%s",
                String.join(" " + operator.value() + " ",
                operands.stream().map(Object::toString).toList()));
    }
}
