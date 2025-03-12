package dark.after.rain.parser;

import dark.after.rain.input.InputString;
import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;
import dark.after.rain.lexer.Tokenizer;
import dark.after.rain.parser.ast.BlockExpression;
import dark.after.rain.parser.ast.Expression;
import dark.after.rain.parser.ast.ReductionStep;

import java.util.ArrayList;
import java.util.List;

public abstract class Parser extends InputString {
    private static final List<ReductionStep> REDUCTION_ORDER = List.of(
            ReductionStep.ASSOCIATIVE,
            ReductionStep.DOUBLE_NEGATION,
            ReductionStep.IDENTITY,
            ReductionStep.DOMINATION,
            ReductionStep.NEGATION,
            ReductionStep.DE_MORGAN,

            ReductionStep.ABSORPTION,
            ReductionStep.DISTRIBUTIVE,

            ReductionStep.ASSOCIATIVE,
            ReductionStep.DOUBLE_NEGATION,
            ReductionStep.IDENTITY,
            ReductionStep.DOMINATION,
            ReductionStep.NEGATION,
            ReductionStep.DE_MORGAN,

            ReductionStep.BICONDITIONAL,
            ReductionStep.IMPLICATION
    );

    public final List<Token> tokens;

    protected Parser(String input) {
        super(input);
        this.tokens = new Tokenizer(input).tokenize().stream()
                .filter(t -> t.type() != TokenType.SPACE)
                .toList();
    }

    // Returns the root of abstract syntax tree (Expression)
    public abstract Expression parse();

    public Expression parseReduced() {
        Expression expr = parse();
        List<Expression> explored = new ArrayList<>();
        while (true) {
            for (ReductionStep step : REDUCTION_ORDER) {
                Expression reduced = expr.reduce(step);
                if (reduced instanceof BlockExpression(Expression inner))
                    reduced = inner;
                expr = reduced;
            }
            System.out.println("Pass: " + expr);
            if (explored.contains(expr)) {
                return explored.stream().min((a, b) -> {
                    int aLen = a.toString().length();
                    int bLen = b.toString().length();
                    return Integer.compare(aLen, bLen);
                }).get();
            }
            explored.add(expr);
        }
    }
}
