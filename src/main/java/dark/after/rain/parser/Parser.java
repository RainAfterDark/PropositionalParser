package dark.after.rain.parser;

import dark.after.rain.input.InputString;
import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;
import dark.after.rain.lexer.Tokenizer;
import dark.after.rain.parser.ast.BlockExpression;
import dark.after.rain.parser.ast.Expression;

import java.util.List;

public abstract class Parser extends InputString {
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
        Expression simplified = expr.simplify();
        while (!simplified.equals(expr)) {
            expr = simplified;
            simplified = expr.simplify();
        }
        if (simplified instanceof BlockExpression(Expression inner))
            return inner;
        return simplified;
    }
}
