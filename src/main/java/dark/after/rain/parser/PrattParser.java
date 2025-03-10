package dark.after.rain.parser;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;
import dark.after.rain.parser.ast.*;

// Top-Down Operator Precedence Parser
public class PrattParser extends Parser {
    private int pos = 0;

    public PrattParser(String input) {
        super(input);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private void expect(TokenType type) {
        if (peek().type() != type) {
            throw error("Expected token " + type + " but found " + peek().type(), peek().index());
        }
        consume();
    }

    // We can express operator precedence with binding power instead
    // of having to write chained functions with recursive descent
    private int getBindingPower(TokenType type) {
        return switch (type) {
            case NOT     -> 5;
            case AND     -> 4;
            case OR      -> 3;
            case IMPLIES -> 2;
            case EQUALS  -> 1;
            default      -> 0;
        };
    }

    // Null denotation (for single or prefixed expressions)
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

    // Left denotation (for binary expressions)
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

    private Expression parseExpression(int rbp) {
        Token token = consume();
        Expression left = nud(token);
        while (rbp < getBindingPower(peek().type())) {
            token = consume();
            left = led(token, left);
        }
        return left;
    }

    @Override
    public Expression parse() {
        Expression expr = parseExpression(0);
        expect(TokenType.EOF);
        return expr;
    }
}
