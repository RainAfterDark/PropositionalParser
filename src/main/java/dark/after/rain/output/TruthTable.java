package dark.after.rain.output;

import dark.after.rain.input.InputString;
import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;
import dark.after.rain.parser.*;
import dark.after.rain.parser.ast.*;

import java.util.*;

public class TruthTable extends InputString {
    private final Parser parser;

    public TruthTable(String input) {
        super(input);
        this.parser = new PrattParser(input);
    }

    private List<Character> collectVariables() {
        return parser.tokens.stream()
                .filter(t -> t.type() == TokenType.VARIABLE)
                .map(Token::value).distinct().sorted().toList();
    }

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
            case VariableExpression _, LiteralExpression _ -> {}
            case null, default -> throw new
                    RuntimeException("Unexpected expression type: " + expr);
        }
    }

    private List<Expression> collectSubexpressions(Expression root) {
        LinkedHashSet<Expression> subExpressions = new LinkedHashSet<>();
        traverse(root, subExpressions);
        return subExpressions.stream().toList();
    }

    private String boolToChar(boolean truth) {
        return truth ? "1" : "0";
    }

    public String generate() {
        StringBuilder sb = new StringBuilder("┃ ");
        List<Character> vars = collectVariables();
        for (Character var : vars) {
            sb.append(var).append(" ");
        }
        sb.append("┃ ");

        Expression root = parser.parse();
        List<Expression> expressions = collectSubexpressions(root);
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

        sb.append("The expression is a ");
        if (tautology) sb.append("tautology");
        else if (contradiction) sb.append("contradiction");
        else sb.append("contingency");
        return sb.toString();
    }
}
