package dark.after.rain.parser;

import dark.after.rain.lexer.Token;
import dark.after.rain.lexer.TokenType;
import dark.after.rain.parser.ast.*;

// Recursive Descent Parser
public class RDParser extends Parser {
    private int pos = 0;

    public RDParser(String input) {
        super(input);
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private boolean eof() {
        return peek().type() == TokenType.EOF;
    }

    private boolean check(TokenType type) {
        return peek().type() == type;
    }

    private void advance() {
        if (!eof()) pos++;
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private void expect(TokenType type) {
        if (!match(type))
            throw error("Expected " + type + " but found " + peek().type(), peek().index());
    }

    // Variables and Blocks
    private Expression parsePrimary() {
        if (match(TokenType.VARIABLE)) {
            return new VariableExpression(previous().value());
        } else if (match(TokenType.LITERAL)) {
            return new LiteralExpression(previous().value());
        } else if (match(TokenType.L_BLOCK)) {
            Expression expr = new BlockExpression(parseEquality());
            expect(TokenType.R_BLOCK);
            return expr;
        }
        throw error("Unexpected token: " + peek().type(), peek().index());
    }

    // Primary > NOT
    private Expression parseNegation() {
        if (match(TokenType.NOT)) {
            Token op = previous();
            Expression expr = parseNegation();
            return new UnaryExpression(op, expr);
        }
        return parsePrimary();
    }

    // NOT > AND
    private Expression parseConjunction() {
        Expression expr = parseNegation();
        while (match(TokenType.AND)) {
            Token op = previous();
            Expression right = parseNegation();
            expr = new BinaryExpression(expr, op, right);
        }
        return expr;
    }

    // AND > OR
    private Expression parseDisjunction() {
        Expression expr = parseConjunction();
        while (match(TokenType.OR)) {
            Token op = previous();
            Expression right = parseConjunction();
            expr = new BinaryExpression(expr, op, right);
        }
        return expr;
    }

    // OR > IMPLIES
    private Expression parseImplication() {
        Expression expr = parseDisjunction();
        while (match(TokenType.IMPLIES)) {
            Token op = previous();
            Expression right = parseDisjunction();
            expr = new BinaryExpression(expr, op, right);
        }
        return expr;
    }

    // IMPLIES > EQUALS
    private Expression parseEquality() {
        Expression expr = parseImplication();
        while (match(TokenType.EQUALS)) {
            Token op = previous();
            Expression right = parseImplication();
            expr = new BinaryExpression(expr, op, right);
        }
        return expr;
    }

    // Start from the lowest precedence (equality)
    @Override
    public Expression parse() {
        Expression expr = parseEquality();
        expect(TokenType.EOF);
        return expr;
    }
}
