package com.logic.parser;

import com.logic.ast.Expression;
import com.logic.input.InputString;
import com.logic.lexer.Token;
import com.logic.lexer.TokenType;
import com.logic.lexer.Tokenizer;
import com.logic.reducer.QmcMinimizer;

import java.util.List;

/**
 * Abstract base class for parsers that process propositional logic expressions.
 * This class handles tokenization of the input string and provides the interface
 * for parsing expressions.
 * <p>
 * Concrete implementations must provide the parse() method to convert tokens
 * into an abstract syntax tree.
 */
public abstract class Parser extends InputString {
    /**
     * The list of tokens from the input string, with whitespace removed.
     */
    public final List<Token> tokens;

    /**
     * Constructs a Parser with the given input string.
     * Tokenizes the input and filters out whitespace tokens.
     *
     * @param input The propositional logic expression to parse
     */
    protected Parser(String input) {
        super(input);
        this.tokens = new Tokenizer(input).tokenize().stream()
                .filter(t -> t.type() != TokenType.SPACE)
                .toList();
    }

    /**
     * Parses the input string into an abstract syntax tree.
     * This method must be implemented by concrete subclasses.
     *
     * @return The root Expression of the abstract syntax tree
     */
    public abstract Expression parse();

    /**
     * Parses the input string and then minimizes the resulting expression
     * using the Quine-McCluskey algorithm.
     *
     * @return A minimized Expression in sum-of-products form
     */
    public Expression parseReduced() {
        Expression expr = parse();
        return new QmcMinimizer(expr).minimize();
    }
}
