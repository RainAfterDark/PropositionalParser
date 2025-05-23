package com.logic.output;

import com.logic.ast.*;
import com.logic.input.InputString;
import com.logic.parser.Parser;
import com.logic.parser.PrattParser;

import java.util.*;

/**
 * Generates a truth table for a propositional logic expression.
 * The truth table shows the evaluation of the expression and all its subexpressions
 * for all possible combinations of variable values.
 * <p>
 * The class also determines whether the expression is a tautology (always true),
 * a contradiction (always false), or a contingency (sometimes true, sometimes false).
 */
public class TruthTable extends InputString {
    private final Parser parser;

    /**
     * Constructs a TruthTable for the given propositional logic expression.
     *
     * @param input The propositional logic expression to generate a truth table for
     */
    public TruthTable(String input) {
        super(input);
        this.parser = new PrattParser(input);
    }

    /**
     * Recursively traverses an expression tree to collect all subexpressions.
     * The subexpressions are added to the provided set in post-order traversal.
     *
     * @param expr    The expression to traverse
     * @param exprSet The set to collect subexpressions into
     * @throws RuntimeException if an unexpected expression type is encountered
     */
    private void traverse(Expression expr, LinkedHashSet<Expression> exprSet) {
        switch (expr) {
            case UnaryExpression unary -> {
                traverse(unary.operand(), exprSet);
                exprSet.add(unary);
            }
            case BinaryExpression binary -> {
                traverse(binary.left(), exprSet);
                traverse(binary.right(), exprSet);
                exprSet.add(binary);
            }
            case BlockExpression block -> traverse(block.inner(), exprSet);
            case VariableExpression _, LiteralExpression _ -> {
                // Variables and literals are leaf nodes, no traversal needed
            }
            case null, default -> throw new
                    RuntimeException("Unexpected expression type: " + expr);
        }
    }

    /**
     * Collects all subexpressions from an expression tree.
     * The subexpressions are returned in post-order traversal order.
     *
     * @param root The root expression of the tree
     * @return A list of all subexpressions in the tree
     */
    private List<Expression> collectSubexpressions(Expression root) {
        LinkedHashSet<Expression> subExpressions = new LinkedHashSet<>();
        traverse(root, subExpressions);
        return subExpressions.stream().toList();
    }

    /**
     * Converts a boolean value to its character representation.
     *
     * @param truth The boolean value to convert
     * @return "1" for true, "0" for false
     */
    private String boolToChar(boolean truth) {
        return truth ? "1" : "0";
    }

    public String generate() {
        Expression root = parser.parse();
        List<Character> vars = root.collectVariables();
        if (vars.isEmpty()) return root + " = " + root;

        StringBuilder sb = new StringBuilder("┃ ");
        for (Character var : vars) {
            sb.append(var).append(" ");
        }
        sb.append("┃ ");

        List<Expression> expressions = collectSubexpressions(root);
        if (expressions.isEmpty()) return root + " = " + root;

        List<Integer> lengths = new ArrayList<>();
        for (Expression expr : expressions) {
            lengths.add(expr.toString().length());
            sb.append(expr).append(" ┃ ");
        }
        sb.append("\n");

        boolean tautology = true;
        boolean contradiction = true;
        int rows = 1 << vars.size(); // 2^n
        for (int i = rows - 1; i >= 0; i--) {
            sb.append("┃ ");
            Map<Character, Boolean> context = new HashMap<>();
            for (int j = 0; j < vars.size(); j++) {
                int digit = vars.size() - j - 1;
                boolean truth = ((i >> digit) & 1) == 1;
                context.put(vars.get(j), truth);
                sb.append(boolToChar(truth)).append(" ");
            }
            sb.append("┃ ");
            for (int j = 0; j < expressions.size(); j++) {
                Expression expr = expressions.get(j);
                int length = lengths.get(j);
                boolean truth = expr.evaluate(context);
                sb.append(String.format(
                        "%-" + length + "s ┃ ", boolToChar(truth)));
                if (j == expressions.size() - 1) {
                    if (!truth) tautology = false;
                    else contradiction = false;
                }

            }
            sb.append("\n");
        }

        sb.append("THE EXPRESSION IS A ");
        if (tautology) sb.append("TAUTOLOGY");
        else if (contradiction) sb.append("CONTRADICTION");
        else sb.append("CONTINGENCY");
        return sb.toString();
    }
}
