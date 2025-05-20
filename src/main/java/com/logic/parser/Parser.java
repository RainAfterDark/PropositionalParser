package com.logic.parser;

import com.logic.ast.Expression;
import com.logic.input.InputString;
import com.logic.lexer.Token;
import com.logic.lexer.TokenType;
import com.logic.lexer.Tokenizer;
import com.logic.reducer.QmcMinimizer;

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
