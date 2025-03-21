package dark.after.rain.parser;

import dark.after.rain.ast.Expression;
import dark.after.rain.input.InputString;
import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;
import dark.after.rain.lexer.Tokenizer;
import dark.after.rain.reducer.QmcMinimizer;

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
        return new QmcMinimizer(expr).minimize();
    }
}
