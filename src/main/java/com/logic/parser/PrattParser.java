package com.logic.parser;

import com.logic.ast.*;
import com.logic.lexer.Token;
import com.logic.lexer.TokenType;

/**
 * A Pratt parser (Top-Down Operator Precedence Parser) for parsing propositional logic expressions.
 * This parser handles operator precedence elegantly by assigning binding powers to operators.
 * <p>
 * The parser supports the following operators with decreasing precedence:
 * <ul>
 *  <li> NOT (~) </li>
 *  <li> AND (&) </li>
 *  <li> OR (|) </li>
 *  <li> IMPLIES (>) </li>
 *  <li> EQUALS (=) </li>
 * </ul>
 * <p>
 * It also supports parentheses for grouping expressions.
 */
public class PrattParser extends Parser {
    private int pos = 0;

    /**
     * Constructs a PrattParser for the given input string.
     *
     * @param input The propositional logic expression to parse
     */
    public PrattParser(String input) {
        super(input);
    }

    /**
     * Consumes the current token and advances to the next token.
     *
     * @return The consumed token
     */
    private Token consume() {
        return tokens.get(pos++);
    }

    /**
     * Returns the current token without consuming it.
     *
     * @return The current token
     */
    private Token peek() {
        return tokens.get(pos);
    }

    /**
     * Expects a specific token type and consumes it.
     * Throws an error if the current token is not of the expected type.
     *
     * @param type The expected token type
     * @throws RuntimeException if the current token is not of the expected type
     */
    private void expect(TokenType type) {
        if (peek().type() != type) {
            throw error("Expected token " + type + " but found " + peek().type(), peek().index());
        }
        consume();
    }

    /**
     * Gets the binding power (precedence) of an operator.
     * Higher binding power means higher precedence.
     *
     * @param type The token type
     * @return The binding power as an integer
     */
    private int getBindingPower(TokenType type) {
        return switch (type) {
            case NOT -> 5;
            case AND -> 4;
            case OR -> 3;
            case IMPLIES -> 2;
            case EQUALS -> 1;
            default -> 0;
        };
    }

    /**
     * Null denotation (for single or prefixed expressions).
     * Handles variables, literals, NOT expressions, and parenthesized expressions.
     *
     * @param token The token to process
     * @return The parsed expression
     * @throws RuntimeException if the token is unexpected
     */
    private Expression nud(Token token) {
        return switch (token.type()) {
            case VARIABLE -> new VariableExpression(token.value());
            case LITERAL -> new LiteralExpression(token.value());
            case NOT -> new UnaryExpression(token,
                    parseExpression(getBindingPower(token.type())));
            case L_BLOCK -> {
                Expression expr = new BlockExpression(parseExpression(0));
                expect(TokenType.R_BLOCK);
                yield expr;
            }
            default -> throw error("Unexpected token " + token.type(), token.index());
        };
    }

    /**
     * Left denotation (for binary expressions).
     * Handles binary operators like AND, OR, IMPLIES, and EQUALS.
     *
     * @param token The operator token
     * @param left  The left-hand side expression
     * @return The parsed binary expression
     * @throws RuntimeException if the token is unexpected
     */
    private Expression led(Token token, Expression left) {
        switch (token.type()) {
            case AND, OR, IMPLIES, EQUALS -> {
                int lbp = getBindingPower(token.type());
                Expression right = parseExpression(lbp);
                return new BinaryExpression(left, token, right);
            }
            default -> throw error("Unexpected token " + token.type(), token.index());
        }
    }

    /**
     * Parses an expression with the given right binding power.
     * This is the core of the Pratt parsing algorithm.
     *
     * @param rbp The right binding power (minimum precedence to accept)
     * @return The parsed expression
     */
    private Expression parseExpression(int rbp) {
        Token token = consume();
        Expression left = nud(token);
        while (rbp < getBindingPower(peek().type())) {
            token = consume();
            left = led(token, left);
        }
        return left;
    }

    /**
     * Parses the entire input string into an expression.
     *
     * @return The parsed expression
     * @throws RuntimeException if the input is invalid
     */
    @Override
    public Expression parse() {
        Expression expr = parseExpression(0);
        expect(TokenType.EOF);
        return expr;
    }
}
